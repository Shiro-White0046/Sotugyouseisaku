package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import bean.MenuMeal;
import infra.ConnectionFactory;

/**
 * menu_meals 用 DAO
 * テーブル構成:
 *   id UUID PK
 *   day_id UUID NOT NULL
 *   meal_slot meal_slot NOT NULL  -- 'breakfast' | 'lunch' | 'dinner'
 *   name TEXT NOT NULL
 *   description TEXT NULL
 *
 * 制約:
 *   UNIQUE(day_id, meal_slot)
 */
public class MenuMealDAO {

  /** day_id + meal_slot で 1件取得 */
  public Optional<MenuMeal> findByDayAndSlot(UUID dayId, String slot) {
    final String sql =
        "SELECT id, day_id, meal_slot, name, description " +
        "FROM menu_meals WHERE day_id = ? AND meal_slot = ?::meal_slot";

    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setObject(1, dayId);
      ps.setString(2, slot);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) return Optional.of(map(rs));
        return Optional.empty();
      }
    } catch (SQLException e) {
      throw new RuntimeException("menu_meals の取得に失敗しました (findByDayAndSlot)", e);
    }
  }

  /** 指定日のどれか 1件（朝→昼→夜 の優先度で） */
  public Optional<MenuMeal> findAnyByDay(UUID dayId) {
    final String sql =
        "SELECT id, day_id, meal_slot, name, description " +
        "FROM menu_meals WHERE day_id = ? " +
        "ORDER BY CASE meal_slot " +
        "  WHEN 'breakfast' THEN 1 " +
        "  WHEN 'lunch'     THEN 2 " +
        "  WHEN 'dinner'    THEN 3 " +
        "  ELSE 99 END " +
        "LIMIT 1";

    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setObject(1, dayId);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) return Optional.of(map(rs));
        return Optional.empty();
      }
    } catch (SQLException e) {
      throw new RuntimeException("menu_meals の取得に失敗しました (findAnyByDay)", e);
    }
  }

  /** 指定日のすべての食事（朝→昼→夜 の順） */
  public List<MenuMeal> listByDay(UUID dayId) {
    final String sql =
        "SELECT id, day_id, meal_slot, name, description " +
        "FROM menu_meals WHERE day_id = ? " +
        "ORDER BY CASE meal_slot " +
        "  WHEN 'breakfast' THEN 1 " +
        "  WHEN 'lunch'     THEN 2 " +
        "  WHEN 'dinner'    THEN 3 " +
        "  ELSE 99 END";

    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setObject(1, dayId);
      try (ResultSet rs = ps.executeQuery()) {
        List<MenuMeal> list = new ArrayList<>();
        while (rs.next()) list.add(map(rs));
        return list;
      }
    } catch (SQLException e) {
      throw new RuntimeException("menu_meals の一覧取得に失敗しました (listByDay)", e);
    }
  }

  /** INSERT（UNIQUE(day_id, meal_slot) なので upsert 運用なら upsert を使う） */
  public MenuMeal insert(UUID dayId, String slot, String name, String description) {
    final String sql =
        "INSERT INTO menu_meals (day_id, meal_slot, name, description) " +
        "VALUES (?, ?::meal_slot, ?, ?) " +
        "RETURNING id, day_id, meal_slot, name, description";

    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setObject(1, dayId);
      ps.setString(2, slot);
      ps.setString(3, name);
      if (description == null) ps.setNull(4, Types.VARCHAR); else ps.setString(4, description);

      try (ResultSet rs = ps.executeQuery()) {
        rs.next();
        return map(rs);
      }
    } catch (SQLException e) {
      throw new RuntimeException("menu_meals の作成に失敗しました (insert)", e);
    }
  }

  /** UPSERT（同一 day_id&slot があれば更新） */
  public MenuMeal upsert(UUID dayId, String slot, String name, String description) {
    final String sql =
        "INSERT INTO menu_meals (day_id, meal_slot, name, description) " +
        "VALUES (?, ?::meal_slot, ?, ?) " +
        "ON CONFLICT (day_id, meal_slot) DO UPDATE " +
        "  SET name = EXCLUDED.name, description = EXCLUDED.description " +
        "RETURNING id, day_id, meal_slot, name, description";

    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setObject(1, dayId);
      ps.setString(2, slot);
      ps.setString(3, name);
      if (description == null) ps.setNull(4, Types.VARCHAR); else ps.setString(4, description);

      try (ResultSet rs = ps.executeQuery()) {
        rs.next();
        return map(rs);
      }
    } catch (SQLException e) {
      throw new RuntimeException("menu_meals の upsert に失敗しました", e);
    }
  }

  /** 更新 */
  public void update(UUID id, String name, String description) {
    final String sql = "UPDATE menu_meals SET name = ?, description = ? WHERE id = ?";

    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setString(1, name);
      if (description == null) ps.setNull(2, Types.VARCHAR); else ps.setString(2, description);
      ps.setObject(3, id);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("menu_meals の更新に失敗しました", e);
    }
  }

  /** 削除 */
  public void delete(UUID id) {
    final String sql = "DELETE FROM menu_meals WHERE id = ?";

    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setObject(1, id);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("menu_meals の削除に失敗しました", e);
    }
  }

  // --- mapper ---
  private static MenuMeal map(ResultSet rs) throws SQLException {
    MenuMeal m = new MenuMeal();
    // Java8互換: getObject(..., UUID.class) が無い JDBC なら (UUID) rs.getObject("id") でOK
    m.setId((UUID) rs.getObject("id"));
    m.setDayId((UUID) rs.getObject("day_id"));
    m.setMealSlot(rs.getString("meal_slot"));  // enum → String で保持（Bean側も String でOK）
    m.setName(rs.getString("name"));
    m.setDescription(rs.getString("description"));
    return m;
  }
}
