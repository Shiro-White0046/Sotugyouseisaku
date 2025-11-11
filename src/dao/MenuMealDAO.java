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
 * DBの meal_slot は小文字('breakfast','lunch','dinner')の ENUM 型を想定。
 * Java側では大文字('BREAKFAST','LUNCH','DINNER')で扱い、DB I/O時に変換する。
 */
public class MenuMealDAO {

  /** 指定日の全スロットを Map(slot -> MenuMeal) で返す。存在しないスロットは Map に入らない。 */
  public Map<String, MenuMeal> findByDayAsMap(UUID dayId) {
    final String sql =
        "SELECT id, day_id, meal_slot, name, description " +
        "FROM menu_meals WHERE day_id=? " +
        "ORDER BY CASE meal_slot " +
        "  WHEN 'breakfast' THEN 1 WHEN 'lunch' THEN 2 WHEN 'dinner' THEN 3 ELSE 9 END, id";

    Map<String, MenuMeal> result = new HashMap<>();
    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

      ps.setObject(1, dayId);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          MenuMeal m = map(rs);
          // DB値は小文字 → Bean は大文字
          m.setSlot(m.getSlot().toUpperCase());
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
        "SELECT id, day_id, meal_slot, name, description " +
        "FROM menu_meals WHERE day_id=? AND meal_slot=?::meal_slot LIMIT 1"; // ★ENUMにキャスト

    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setObject(1, dayId);
      ps.setString(2, slot.toLowerCase()); // DBは小文字ENUM
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) return Optional.empty();
        MenuMeal m = map(rs);
        m.setSlot(m.getSlot().toUpperCase());
        return Optional.of(m);
      }
    } catch (SQLException e) {
      throw new RuntimeException("menu_meals の取得に失敗しました(findByDayAndSlot)", e);
    }
  }

  /**
   * 指定日に1件でも meal があれば 1件返す（優先順：BREAKFAST→LUNCH→DINNER）。無ければ empty。
   */
  public Optional<MenuMeal> findAnyByDay(UUID dayId) {
    final String sql =
        "SELECT id, day_id, meal_slot, name, description " +
        "FROM menu_meals WHERE day_id=? " +
        "ORDER BY CASE meal_slot " +
        "  WHEN 'breakfast' THEN 1 WHEN 'lunch' THEN 2 WHEN 'dinner' THEN 3 ELSE 9 END, id " +
        "LIMIT 1";

    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setObject(1, dayId);
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) return Optional.empty();
        MenuMeal m = map(rs);
        m.setSlot(m.getSlot().toUpperCase());
        return Optional.of(m);
      }
    } catch (SQLException e) {
      throw new RuntimeException("menu_meals の取得に失敗しました(findAnyByDay)", e);
    }
  }

  /** 指定 dayId/slot の Meal を upsert して id を返す。 */
  public UUID upsertMeal(UUID dayId, String slot, String name, String description) {
    // DBは小文字ENUM
    final String dbSlot = slot.toLowerCase();

    final String update =
        "UPDATE menu_meals SET name=?, description=? " +
        "WHERE day_id=? AND meal_slot=?::meal_slot"; // ★ENUMにキャスト

    final String insert =
        // 挿入は列型が meal_slot ENUM なので、JDBCの setString でそのまま渡してOK
        "INSERT INTO menu_meals (id, day_id, meal_slot, name, description) " +
        "VALUES (?, ?, ?::meal_slot, ?, ?)"; // ★安全のためここもキャスト

    try (Connection con = ConnectionFactory.getConnection()) {
      con.setAutoCommit(false);
      try {
        UUID id;

        Optional<MenuMeal> exists = findByDayAndSlot(dayId, slot);
        if (exists.isPresent()) {
          try (PreparedStatement ps = con.prepareStatement(update)) {
            ps.setString(1, name);
            ps.setString(2, description);
            ps.setObject(3, dayId);
            ps.setString(4, dbSlot);
            ps.executeUpdate();
          }
          id = exists.get().getId();
        } else {
          id = UUID.randomUUID();
          try (PreparedStatement ps = con.prepareStatement(insert)) {
            ps.setObject(1, id);
            ps.setObject(2, dayId);
            ps.setString(3, dbSlot);
            ps.setString(4, name);
            ps.setString(5, description);
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

  /** 指定 dayId/slot の Meal を削除 */
  public void deleteByDayAndSlot(UUID dayId, String slot) {
    final String sql =
        "DELETE FROM menu_meals WHERE day_id=? AND meal_slot=?::meal_slot"; // ★ENUMにキャスト

    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setObject(1, dayId);
      ps.setString(2, slot.toLowerCase());
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("menu_meals の削除に失敗しました(deleteByDayAndSlot)", e);
    }
  }

  // 共通マッピング
  private static MenuMeal map(ResultSet rs) throws SQLException {
    MenuMeal m = new MenuMeal();
    m.setId((UUID) rs.getObject("id"));
    m.setDayId((UUID) rs.getObject("day_id"));
    // ここでは DBの小文字値をそのまま一旦受ける
    m.setSlot(rs.getString("meal_slot"));
    m.setName(rs.getString("name"));
    m.setDescription(rs.getString("description"));
    return m;
  }
}
