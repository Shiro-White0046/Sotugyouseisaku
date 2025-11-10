package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import bean.MenuItem;
import infra.ConnectionFactory;

/**
 * menu_items テーブル用 DAO
 * 献立内の品目
 */
public class MenuItemDAO {

  /** 食事ごとの品目一覧 */
  public List<MenuItem> listByMeal(UUID mealId) {
    final String sql =
        "SELECT id, meal_id, item_order, name, note " +
        "FROM menu_items WHERE meal_id = ? ORDER BY item_order";
    List<MenuItem> list = new ArrayList<>();
    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setObject(1, mealId);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) list.add(map(rs));
      }
    } catch (SQLException e) {
      throw new RuntimeException("menu_items の取得に失敗しました", e);
    }
    return list;
  }

  /** 追加 */
  public MenuItem insert(UUID mealId, String name, int order, String note) {
    final String sql =
        "INSERT INTO menu_items (meal_id, item_order, name, note) " +
        "VALUES (?, ?, ?, ?) RETURNING id, meal_id, item_order, name, note";
    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setObject(1, mealId);
      ps.setInt(2, order);
      ps.setString(3, name);
      ps.setString(4, note);
      try (ResultSet rs = ps.executeQuery()) {
        rs.next();
        return map(rs);
      }
    } catch (SQLException e) {
      throw new RuntimeException("menu_items の登録に失敗しました", e);
    }
  }

  /** 更新 */
  public void update(UUID id, String name, String note) {
    final String sql = "UPDATE menu_items SET name = ?, note = ? WHERE id = ?";
    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setString(1, name);
      ps.setString(2, note);
      ps.setObject(3, id);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("menu_items の更新に失敗しました", e);
    }
  }

  /** 削除 */
  public void delete(UUID id) {
    final String sql = "DELETE FROM menu_items WHERE id = ?";
    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setObject(1, id);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("menu_items の削除に失敗しました", e);
    }
  }

  private MenuItem map(ResultSet rs) throws SQLException {
    MenuItem m = new MenuItem();
    m.setId((UUID) rs.getObject("id"));
    m.setMealId((UUID) rs.getObject("meal_id"));
    m.setItemOrder(rs.getInt("item_order"));
    m.setName(rs.getString("name"));
    m.setNote(rs.getString("note"));
    return m;
  }
}
