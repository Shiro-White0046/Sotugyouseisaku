package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import bean.Allergen;
import infra.ConnectionFactory;

/**
 * menu_item_allergens テーブル用 DAO
 * 品目ごとのアレルゲン設定
 */
public class MenuItemAllergenDAO {

  /** 品目に紐づくアレルゲンを取得 */
  public List<Allergen> listByItem(UUID itemId) {
    final String sql =
        "SELECT a.id, a.code, a.name_ja, a.name_en, a.category, a.subcategory " +
        "FROM menu_item_allergens mia " +
        "JOIN allergens a ON a.id = mia.allergen_id " +
        "WHERE mia.item_id = ? ORDER BY a.id";
    List<Allergen> list = new ArrayList<>();
    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setObject(1, itemId);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          Allergen a = new Allergen();
          a.setId(rs.getShort("id"));
          a.setCode(rs.getString("code"));
          a.setNameJa(rs.getString("name_ja"));
          a.setNameEn(rs.getString("name_en"));
          a.setCategory(rs.getString("category"));
          a.setSubcategory(rs.getString("subcategory"));
          list.add(a);
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("menu_item_allergens の取得に失敗しました", e);
    }
    return list;
  }

  /** 品目のアレルゲンを入れ替え */
  public void replace(UUID itemId, List<Short> allergenIds) {
    final String del = "DELETE FROM menu_item_allergens WHERE item_id = ?";
    final String ins = "INSERT INTO menu_item_allergens (item_id, allergen_id) VALUES (?, ?)";
    try (Connection con = ConnectionFactory.getConnection()) {
      con.setAutoCommit(false);
      try (PreparedStatement psDel = con.prepareStatement(del);
           PreparedStatement psIns = con.prepareStatement(ins)) {

        psDel.setObject(1, itemId);
        psDel.executeUpdate();

        for (Short id : allergenIds) {
          psIns.setObject(1, itemId);
          psIns.setShort(2, id);
          psIns.addBatch();
        }
        psIns.executeBatch();
        con.commit();
      } catch (SQLException e) {
        con.rollback();
        throw new RuntimeException("menu_item_allergens の入れ替えに失敗しました", e);
      } finally {
        con.setAutoCommit(true);
      }
    } catch (SQLException e) {
      throw new RuntimeException("menu_item_allergens の更新処理に失敗しました", e);
    }
  }

  /** 品目単位で削除 */
  public void deleteByItem(UUID itemId) {
    final String sql = "DELETE FROM menu_item_allergens WHERE item_id = ?";
    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setObject(1, itemId);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("menu_item_allergens の削除に失敗しました", e);
    }
  }


  //メニューに紐づくアレルゲン ID のセット
  public Set<Integer> findByItemIdAsSet(UUID itemId) {
	  String sql =
	      "SELECT allergen_id " +
	      "FROM menu_item_allergens " +
	      "WHERE item_id = ?";

	  Set<Integer> set = new HashSet<>();

	  try (Connection con = ConnectionFactory.getConnection();
	       PreparedStatement ps = con.prepareStatement(sql)) {

	    ps.setObject(1, itemId);

	    try (ResultSet rs = ps.executeQuery()) {
	      while (rs.next()) {
	        int allergenId = rs.getInt("allergen_id");
	        if (!rs.wasNull()) {
	          set.add(allergenId);
	        }
	      }
	    }
	  } catch (SQLException e) {
	    throw new RuntimeException("menu_item_allergens 取得失敗", e);
	  }

	  return set;
	}


  /** 品目に紐づくアレルゲンID（short）だけが欲しいとき */
  public List<Short> listAllergenIdsByItem(UUID itemId) {
    final String sql =
        "SELECT allergen_id FROM menu_item_allergens WHERE item_id = ? ORDER BY allergen_id";
    List<Short> ids = new ArrayList<>();
    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setObject(1, itemId);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) ids.add(rs.getShort(1));
      }
    } catch (SQLException e) {
      throw new RuntimeException("menu_item_allergens ID取得に失敗しました", e);
    }
    return ids;
  }

}
