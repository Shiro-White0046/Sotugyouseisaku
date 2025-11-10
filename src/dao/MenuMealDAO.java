package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import bean.MenuMeal;
import infra.ConnectionFactory;

/**
 * menu_meals テーブル用 DAO
 * 朝・昼・夜の献立管理
 */
public class MenuMealDAO {

  /** 指定日の朝昼夜を一覧取得 */
  public List<MenuMeal> listByDay(UUID dayId) {
    final String sql =
        "SELECT id, day_id, meal_slot, name, description " +
        "FROM menu_meals WHERE day_id = ? ORDER BY meal_slot";

    List<MenuMeal> list = new ArrayList<>();
    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setObject(1, dayId);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) list.add(map(rs));
      }
    } catch (SQLException e) {
      throw new RuntimeException("menu_meals の取得に失敗しました", e);
    }
    return list;
  }

  /** 指定スロット（朝/昼/夜）の取得 */
  public Optional<MenuMeal> find(UUID dayId, String slot) {
    final String sql =
        "SELECT id, day_id, meal_slot, name, description " +
        "FROM menu_meals WHERE day_id = ? AND meal_slot = ?";
    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setObject(1, dayId);
      ps.setString(2, slot);
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) return Optional.empty();
        return Optional.of(map(rs));
      }
    } catch (SQLException e) {
      throw new RuntimeException("menu_meals の検索に失敗しました", e);
    }
  }

  /** 新規登録 */
  public MenuMeal insert(UUID dayId, String slot, String name, String desc) {
    final String sql =
        "INSERT INTO menu_meals (day_id, meal_slot, name, description) " +
        "VALUES (?, ?, ?, ?) RETURNING id, day_id, meal_slot, name, description";
    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setObject(1, dayId);
      ps.setString(2, slot);
      ps.setString(3, name);
      ps.setString(4, desc);
      try (ResultSet rs = ps.executeQuery()) {
        rs.next();
        return map(rs);
      }
    } catch (SQLException e) {
      throw new RuntimeException("menu_meals の登録に失敗しました", e);
    }
  }

  /** 更新 */
  public void update(UUID id, String name, String desc) {
    final String sql = "UPDATE menu_meals SET name = ?, description = ? WHERE id = ?";
    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setString(1, name);
      ps.setString(2, desc);
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

  private MenuMeal map(ResultSet rs) throws SQLException {
    MenuMeal m = new MenuMeal();
    m.setId((UUID) rs.getObject("id"));
    m.setDayId((UUID) rs.getObject("day_id"));
    m.setMealSlot(rs.getString("meal_slot"));
    m.setName(rs.getString("name"));
    m.setDescription(rs.getString("description"));
    return m;
  }
}
