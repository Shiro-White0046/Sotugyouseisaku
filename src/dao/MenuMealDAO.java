package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import bean.MenuMeal;
import infra.ConnectionFactory;

/**
 * menu_meals 用 DAO
 *
 * 想定スキーマ:
 *   - id UUID PK
 *   - day_id UUID (FK -> menu_days.id)
 *   - meal_slot meal_slot  (enum: 'breakfast' / 'lunch' / 'dinner') ← 小文字！
 *   - name TEXT
 *   - description TEXT
 *   - image_path TEXT
 *
 * 重要:
 *   受け取る slot は大文字/小文字どちらでも来るため、SQLに渡す前に必ず小文字へ正規化する。
 *   ORDER BY も meal_slot::text を小文字リテラルで比較する。
 */
public class MenuMealDAO {

  /** 指定日の全スロットを Map(slot -> MenuMeal) で返す。存在しないスロットは Map に入らない。 */
  public Map<String, MenuMeal> findByDayAsMap(UUID dayId) {
    final String sql =
        "SELECT id, day_id, meal_slot, name, description, image_path " +
        "FROM menu_meals WHERE day_id=? " +
        "ORDER BY CASE meal_slot::text " +
        "  WHEN 'breakfast' THEN 1 " +
        "  WHEN 'lunch' THEN 2 " +
        "  WHEN 'dinner' THEN 3 " +
        "  ELSE 9 END";

    Map<String, MenuMeal> result = new HashMap<>();
    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

      ps.setObject(1, dayId);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          MenuMeal m = map(rs);
          // Map のキーは既存コードに合わせて大文字（BREAKFAST/LUNCH/DINNER）
          result.put(m.getSlot(), m);
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("menu_meals の取得に失敗しました(findByDayAsMap)", e);
    }
    return result;
  }

  /** 指定日の指定スロット（BREAKFAST/LUNCH/DINNER など）を返す。無ければ empty。 */
  public Optional<MenuMeal> findByDayAndSlot(UUID dayId, String slot) {
    final String sql =
        "SELECT id, day_id, meal_slot, name, description, image_path " +
        "FROM menu_meals WHERE day_id=? AND meal_slot = ?::meal_slot LIMIT 1";
    final String slotLower = normalizeSlot(slot);
    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setObject(1, dayId);
      ps.setString(2, slotLower);
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) return Optional.empty();
        return Optional.of(map(rs));
      }
    } catch (SQLException e) {
      throw new RuntimeException("menu_meals の取得に失敗しました(findByDayAndSlot)", e);
    }
  }

  /**
   * 指定 dayId/slot の Meal を upsert して id を返す。
   * name/description をセットし、既存なら更新・無ければ作成する。
   */
  public UUID upsertMeal(UUID dayId, String slot, String name, String description) {
    final String sel =
        "SELECT id FROM menu_meals WHERE day_id=? AND meal_slot = ?::meal_slot";
    final String upd =
        "UPDATE menu_meals SET name=?, description=? WHERE day_id=? AND meal_slot = ?::meal_slot";
    final String ins =
        "INSERT INTO menu_meals (id, day_id, meal_slot, name, description) " +
        "VALUES (?, ?, ?::meal_slot, ?, ?)";

    final String slotLower = normalizeSlot(slot);

    try (Connection con = ConnectionFactory.getConnection()) {
      con.setAutoCommit(false);
      try {
        UUID id = null;

        try (PreparedStatement ps = con.prepareStatement(sel)) {
          ps.setObject(1, dayId);
          ps.setString(2, slotLower);
          try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) id = (UUID) rs.getObject("id");
          }
        }

        if (id != null) {
          try (PreparedStatement ps = con.prepareStatement(upd)) {
            ps.setString(1, name);
            ps.setString(2, description);
            ps.setObject(3, dayId);
            ps.setString(4, slotLower);
            ps.executeUpdate();
          }
        } else {
          id = UUID.randomUUID();
          try (PreparedStatement ps = con.prepareStatement(ins)) {
            ps.setObject(1, id);
            ps.setObject(2, dayId);
            ps.setString(3, slotLower);
            ps.setString(4, name);
            ps.setString(5, description);
            ps.executeUpdate();
          }
        }

        con.commit();
        return id;

      } catch (SQLException e) {
        try { con.rollback(); } catch (SQLException ignore) {}
        throw new RuntimeException("menu_meals の upsert に失敗しました(upsertMeal)", e);
      } finally {
        try { con.setAutoCommit(true); } catch (SQLException ignore) {}
      }
    } catch (SQLException e) {
      throw new RuntimeException("menu_meals の upsert に失敗しました(upsertMeal)", e);
    }
  }

  /** 指定 dayId/slot の Meal を物理削除（存在しなくてもOK）。 */
  public void deleteByDayAndSlot(UUID dayId, String slot) {
    final String sql = "DELETE FROM menu_meals WHERE day_id=? AND meal_slot = ?::meal_slot";
    final String slotLower = normalizeSlot(slot);
    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setObject(1, dayId);
      ps.setString(2, slotLower);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("menu_meals の削除に失敗しました(deleteByDayAndSlot)", e);
    }
  }

  /** slot ごとの画像パスを更新（image_path）。 */
  public void updateImagePath(UUID dayId, String slot, String imagePath) {
    final String sql = "UPDATE menu_meals SET image_path=? WHERE day_id=? AND meal_slot = ?::meal_slot";
    final String slotLower = normalizeSlot(slot);
    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setString(1, imagePath);
      ps.setObject(2, dayId);
      ps.setString(3, slotLower);
      int updated = ps.executeUpdate();
      if (updated == 0) {
        // レコードが無ければ作成してから再更新（保険）
        upsertMeal(dayId, slotLower, "", "");
        try (PreparedStatement ps2 = con.prepareStatement(sql)) {
          ps2.setString(1, imagePath);
          ps2.setObject(2, dayId);
          ps2.setString(3, slotLower);
          ps2.executeUpdate();
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("menu_meals の画像パス更新に失敗しました(updateImagePath)", e);
    }
  }

  // -------------------------------------------------------
  // 共通マッピング
  // -------------------------------------------------------
  private static MenuMeal map(ResultSet rs) throws SQLException {
    MenuMeal m = new MenuMeal();
    m.setId((UUID) rs.getObject("id"));
    m.setDayId((UUID) rs.getObject("day_id"));
    // UI 側のキー互換性のため、返却は大文字（BREAKFAST/LUNCH/DINNER）
    String slotLower = rs.getString("meal_slot");
    m.setSlot(slotLower == null ? null : slotLower.toUpperCase(Locale.ROOT));
    m.setName(rs.getString("name"));
    m.setDescription(rs.getString("description"));
    m.setImagePath(rs.getString("image_path"));
    return m;
  }

  /** null/空でなければ小文字化。 */
  private static String normalizeSlot(String slot) {
    if (slot == null) return null;
    String s = slot.trim();
    if (s.isEmpty()) return s;
    return s.toLowerCase(Locale.ROOT);
  }
  /** 指定日に meal が1件でもあれば 1件返す（優先順：breakfast→lunch→dinner）。無ければ empty。 */
  public Optional<MenuMeal> findAnyByDay(UUID dayId) {
    final String sql =
        "SELECT id, day_id, meal_slot, name, description, image_path " +
        "FROM menu_meals " +
        "WHERE day_id=? " +
        "ORDER BY CASE meal_slot::text " +
        "  WHEN 'breakfast' THEN 1 " +
        "  WHEN 'lunch' THEN 2 " +
        "  WHEN 'dinner' THEN 3 " +
        "  ELSE 9 END " +
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

  //
  public List<MenuMeal> listByOrgDateAndSlot(UUID orgId, LocalDate date, String mealSlot) {
	    String sql =
	        "SELECT m.id, m.day_id, m.meal_slot, m.name, m.description, m.image_path " +
	        "FROM menu_days d " +
	        "JOIN menu_meals m ON m.day_id = d.id " +
	        "WHERE d.org_id = ? " +
	        "  AND d.menu_date = ? " +
	        "  AND m.meal_slot = ? " +
	        "ORDER BY m.id";

	    List<MenuMeal> list = new ArrayList<>();

	    try (Connection con = ConnectionFactory.getConnection();
	         PreparedStatement ps = con.prepareStatement(sql)) {

	        ps.setObject(1, orgId);
	        ps.setObject(2, date);
	        ps.setString(3, mealSlot);  // breakfast / lunch / dinner

	        try (ResultSet rs = ps.executeQuery()) {
	            while (rs.next()) {
	                MenuMeal m = new MenuMeal();
	                m.setId((UUID) rs.getObject("id"));
	                m.setDayId((UUID) rs.getObject("day_id"));
	                m.setSlot(rs.getString("meal_slot"));
	                m.setName(rs.getString("name"));
	                m.setDescription(rs.getString("description"));
	                m.setImagePath(rs.getString("image_path"));
	                list.add(m);
	            }
	        }
	    } catch (SQLException e) {
	        throw new RuntimeException("menu_meals 取得失敗", e);
	    }

	    return list;
	}

  /** ある日（menu_days.id）に紐づく食事（朝/昼/夕）を取得 */
  public List<MenuMeal> listByDay(UUID dayId) {
    final String sql =
        "SELECT id, day_id, meal_slot, name, description, image_path " +
        "FROM menu_meals " +
        "WHERE day_id = ? " +
        "ORDER BY CASE meal_slot " +
        "  WHEN 'breakfast' THEN 1 " +
        "  WHEN 'lunch'     THEN 2 " +
        "  WHEN 'dinner'    THEN 3 " +
        "  ELSE 99 END, id";

    List<MenuMeal> list = new ArrayList<>();
    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

      ps.setObject(1, dayId);

      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          MenuMeal m = new MenuMeal();
          m.setId((UUID) rs.getObject("id"));
          m.setDayId((UUID) rs.getObject("day_id"));
          m.setSlot(rs.getString("meal_slot"));              // enum → String で受ける
          m.setName(rs.getString("name"));
          m.setDescription(rs.getString("description"));
          // 画像列が無いスキーマなら下2行は削除
          try { m.getClass().getMethod("setImagePath", String.class); m.setImagePath(rs.getString("image_path")); } catch (Exception ignore) {}
          list.add(m);
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("menu_meals 日別一覧の取得に失敗しました", e);
    }
    return list;
  }

}
