package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import bean.MenuMeal;
import infra.ConnectionFactory;

/**
 * menu_meals 用 DAO
 * - 列: id UUID, day_id UUID, meal_slot meal_slot(enum), name text, description text, image_path text
 * - ENUM 値は DB では小文字('breakfast','lunch','dinner')を想定。
 *   Java(Bean)では大文字('BREAKFAST','LUNCH','DINNER')で扱い、I/Oで相互変換する。
 */
public class MenuMealDAO {

  /* ===========================
   *  取得系
   * =========================== */

  /** 指定日の全スロットを Map(slot -> MenuMeal) で返す。存在しないスロットは入らない。 */
  public Map<String, MenuMeal> findByDayAsMap(UUID dayId) {
    final String sql =
        "SELECT id, day_id, meal_slot, name, description, image_path " +
        "FROM menu_meals WHERE day_id=? " +
        "ORDER BY CASE meal_slot " +
        "  WHEN 'breakfast' THEN 1 WHEN 'lunch' THEN 2 WHEN 'dinner' THEN 3 ELSE 9 END, id";

    Map<String, MenuMeal> result = new HashMap<String, MenuMeal>();
    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

      ps.setObject(1, dayId);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          MenuMeal m = map(rs);                 // Bean側は大文字スロットにして返す
          result.put(m.getSlot(), m);
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("menu_meals の取得に失敗しました(findByDayAsMap)", e);
    }
    return result;
  }

  /** 指定日の指定スロット（BREAKFAST/LUNCH/DINNER）を返す。無ければ empty。 */
  public Optional<MenuMeal> findByDayAndSlot(UUID dayId, String slot) {
    final String sql =
        "SELECT id, day_id, meal_slot, name, description, image_path " +
        "FROM menu_meals WHERE day_id=? AND meal_slot = CAST(? AS meal_slot) " +
        "LIMIT 1";
    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setObject(1, dayId);
      ps.setString(2, toDbSlot(slot)); // 小文字に変換してバインド
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) return Optional.empty();
        return Optional.of(map(rs));
      }
    } catch (SQLException e) {
      throw new RuntimeException("menu_meals の取得に失敗しました(findByDayAndSlot)", e);
    }
  }

  /** 指定日に1件でも meal があれば 1件返す（優先順：BREAKFAST→LUNCH→DINNER）。無ければ empty。 */
  public Optional<MenuMeal> findAnyByDay(UUID dayId) {
    final String sql =
        "SELECT id, day_id, meal_slot, name, description, image_path " +
        "FROM menu_meals WHERE day_id=? " +
        "ORDER BY CASE meal_slot " +
        "  WHEN 'breakfast' THEN 1 WHEN 'lunch' THEN 2 WHEN 'dinner' THEN 3 ELSE 9 END, id " +
        "LIMIT 1";
    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setObject(1, dayId);
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) return Optional.empty();
        return Optional.of(map(rs));
      }
    } catch (SQLException e) {
      throw new RuntimeException("menu_meals の取得に失敗しました(findAnyByDay)", e);
    }
  }

  /* ===========================
   *  更新系
   * =========================== */

  /**
   * 指定 dayId/slot の Meal を upsert して id を返す。
   * - name/description/imagePath をセット（null はそのままDBに入る想定）
   * - 既存があれば UPDATE、無ければ INSERT
   */
  public UUID upsertMeal(UUID dayId, String slot, String name, String description, String imagePath) {
    final String update =
        "UPDATE menu_meals SET name=?, description=?, image_path=? " +
        "WHERE day_id=? AND meal_slot = CAST(? AS meal_slot)";
    final String insert =
        "INSERT INTO menu_meals (id, day_id, meal_slot, name, description, image_path) " +
        "VALUES (?, ?, ?::meal_slot, ?, ?, ?)";

    try (Connection con = ConnectionFactory.getConnection()) {
      con.setAutoCommit(false);
      try {
        UUID id;
        Optional<MenuMeal> exists = findByDayAndSlot(dayId, slot);
        if (exists.isPresent()) {
          id = exists.get().getId();
          try (PreparedStatement ps = con.prepareStatement(update)) {
            ps.setString(1, name);
            ps.setString(2, description);
            ps.setString(3, imagePath);
            ps.setObject(4, dayId);
            ps.setString(5, toDbSlot(slot));
            ps.executeUpdate();
          }
        } else {
          id = UUID.randomUUID();
          try (PreparedStatement ps = con.prepareStatement(insert)) {
            ps.setObject(1, id);
            ps.setObject(2, dayId);
            ps.setString(3, toDbSlot(slot));
            ps.setString(4, name);
            ps.setString(5, description);
            ps.setString(6, imagePath);
            ps.executeUpdate();
          }
        }
        con.commit();
        return id;
      } catch (SQLException e) {
        try { con.rollback(); } catch (SQLException ignore) {}
        throw e;
      } finally {
        try { con.setAutoCommit(true); } catch (SQLException ignore) {}
      }
    } catch (SQLException e) {
      throw new RuntimeException("menu_meals の upsert に失敗しました(upsertMeal)", e);
    }
  }

  /** 指定 dayId / slot の meal を削除（存在しなくてもOK）。 */
  public void deleteByDayAndSlot(UUID dayId, String slot) {
    final String sql = "DELETE FROM menu_meals WHERE day_id = ? AND meal_slot = CAST(? AS meal_slot)";
    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setObject(1, dayId);
      ps.setString(2, toDbSlot(slot));
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("menu_meals の削除に失敗しました(deleteByDayAndSlot)", e);
    }
  }

  /* ===========================
   *  ヘルパ
   * =========================== */

  /** DB → Bean 変換（meal_slot: 小文字） */
  private static MenuMeal map(ResultSet rs) throws SQLException {
    MenuMeal m = new MenuMeal();
    m.setId((UUID) rs.getObject("id"));
    m.setDayId((UUID) rs.getObject("day_id"));
    m.setSlot(fromDbSlot(rs.getString("meal_slot"))); // Bean側は大文字
    m.setName(rs.getString("name"));
    m.setDescription(rs.getString("description"));
    m.setImagePath(rs.getString("image_path"));
    return m;
  }

  /** Bean側（BREAKFAST/LUNCH/DINNER）→ DB側（lowercase） */
  private static String toDbSlot(String slot) {
    if (slot == null) return null;
    return slot.trim().toLowerCase(); // "BREAKFAST" → "breakfast"
  }

  /** DB側（lowercase）→ Bean側（UPPERCASE） */
  private static String fromDbSlot(String dbSlot) {
    if (dbSlot == null) return null;
    return dbSlot.trim().toUpperCase(); // "breakfast" → "BREAKFAST"
  }
  /** slotごとの画像パスを更新 */
  public void updateImagePath(UUID dayId, String slot, String imagePath) {
    final String sql = "UPDATE menu_meals SET image_path=? WHERE day_id=? AND slot=?";
    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setString(1, imagePath);
      ps.setObject(2, dayId);
      ps.setString(3, slot);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("menu_meals の画像パス更新に失敗しました(updateImagePath)", e);
    }
  }

}
