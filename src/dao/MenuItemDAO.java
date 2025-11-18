package dao;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import bean.MenuItem;
import infra.ConnectionFactory;

/**
 * menu_items + menu_item_allergens DAO
 * 1つの食事スロット（meal_id）に属する品目を管理
 */
public class MenuItemDAO {

  /** 単一Mealの品目＋アレルゲン一覧を取得 */
  public List<MenuItem> listWithAllergens(UUID mealId) {
    List<MenuItem> list = new ArrayList<MenuItem>();

    String sql =
        "SELECT i.id, i.meal_id, i.item_order, i.name, i.note, "
      + "array_agg(a.allergen_id ORDER BY a.allergen_id) AS allergen_ids "
      + "FROM menu_items i "
      + "LEFT JOIN menu_item_allergens a ON a.item_id = i.id "
      + "WHERE i.meal_id = ? "
      + "GROUP BY i.id "
      + "ORDER BY i.item_order";

    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

      ps.setObject(1, mealId);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          MenuItem it = mapRow(rs);
          // array列を読み取る
          Array arr = rs.getArray("allergen_ids");
          if (arr != null) {
            Short[] sArr = (Short[]) arr.getArray();
            it.setAllergenIds(Arrays.asList(sArr));
          } else {
            it.setAllergenIds(new ArrayList<Short>());
          }
          list.add(it);
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("menu_items一覧の取得に失敗しました", e);
    }
    return list;
  }

  /** meal_id単位での差分保存（全削除→再挿入でもOK） */
  public void saveMealItems(UUID mealId, List<ItemForm> forms) {
    Connection con = null;
    try {
      con = ConnectionFactory.getConnection();
      con.setAutoCommit(false);

      // 既存のアイテム全削除
      try (PreparedStatement ps = con.prepareStatement(
              "DELETE FROM menu_items WHERE meal_id = ?")) {
        ps.setObject(1, mealId);
        ps.executeUpdate();
      }

      // INSERTし直し
      String sqlItem = "INSERT INTO menu_items (meal_id, item_order, name, note) "
                     + "VALUES (?, ?, ?, ?) RETURNING id";
      String sqlAlg  = "INSERT INTO menu_item_allergens (item_id, allergen_id) VALUES (?, ?)";
      for (ItemForm f : forms) {
        UUID newItemId = null;
        try (PreparedStatement ps = con.prepareStatement(sqlItem)) {
          ps.setObject(1, mealId);
          ps.setInt(2, f.order);
          ps.setString(3, f.name);
          ps.setString(4, null);
          try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
              newItemId = (UUID) rs.getObject("id");
            }
          }
        }
        // アレルゲンを登録
        if (newItemId != null && f.allergenIds != null) {
          for (Short a : f.allergenIds) {
            try (PreparedStatement ps = con.prepareStatement(sqlAlg)) {
              ps.setObject(1, newItemId);
              ps.setShort(2, a);
              ps.executeUpdate();
            }
          }
        }
      }

      con.commit();

    } catch (SQLException e) {
      try { if (con != null) con.rollback(); } catch (Exception ignore) {}
      throw new RuntimeException("menu_itemsの保存に失敗しました", e);
    } finally {
      try { if (con != null) con.close(); } catch (Exception ignore) {}
    }
  }

  // 内部クラス: Servletで使うフォーム形式
  public static class ItemForm {
    public UUID id;
    public int order;
    public String name;
    public List<Short> allergenIds;

    public ItemForm(UUID id, int order, String name, List<Short> allergenIds) {
      this.id = id;
      this.order = order;
      this.name = name;
      this.allergenIds = allergenIds;
    }
  }

  private MenuItem mapRow(ResultSet rs) throws SQLException {
    MenuItem it = new MenuItem();
    it.setId((UUID) rs.getObject("id"));
    it.setMealId((UUID) rs.getObject("meal_id"));
    it.setItemOrder(rs.getInt("item_order"));
    it.setName(rs.getString("name"));
    it.setNote(rs.getString("note"));
    return it;
  }
  public List<bean.MenuItem> listByMeal(UUID mealId) {
	  return listWithAllergens(mealId);
	}

//org + 日付 + 朝/昼/夜 からメニュー一覧を取る
  public List<MenuItem> listByOrgDateAndMealType(UUID orgId, LocalDate date, String mealType) {
	  String sql =
	      "SELECT i.id, i.meal_id, i.item_order, i.name, i.note " +
	      "FROM menu_days d " +
	      "JOIN menu_meals m ON m.day_id = d.id " +
	      "JOIN menu_items i ON i.meal_id = m.id " +
	      "WHERE d.org_id = ? " +
	      // "  AND d.menu_date = ? " +   // ★ いったん日付条件を外す
	      "  AND m.meal_slot = CAST(? AS meal_slot) " +
	      "ORDER BY i.item_order, i.id";

	  List<MenuItem> list = new ArrayList<>();

	  try (Connection con = ConnectionFactory.getConnection();
	       PreparedStatement ps = con.prepareStatement(sql)) {

	    // パラメータ位置に注意！（2番目が mealType になる）
	    ps.setObject(1, orgId);
	    // ps.setObject(2, date);  // ← これもコメントアウト
	    ps.setString(2, mealType);  // "breakfast" / "lunch" / "dinner"

	    try (ResultSet rs = ps.executeQuery()) {
	      while (rs.next()) {
	        MenuItem item = new MenuItem();
	        item.setId((UUID) rs.getObject("id"));
	        item.setMealId((UUID) rs.getObject("meal_id"));
	        item.setItemOrder(rs.getInt("item_order"));
	        item.setName(rs.getString("name"));
	        item.setNote(rs.getString("note"));
	        list.add(item);
	      }
	    }
	  } catch (SQLException e) {
	    throw new RuntimeException("menu_items 一覧取得に失敗しました", e);
	  }

	  return list;
	}

  public List<MenuItem> listByMealId(UUID mealId) {
	    String sql =
	        "SELECT id, meal_id, item_order, name, note " +
	        "FROM menu_items " +
	        "WHERE meal_id = ? " +
	        "ORDER BY item_order, id";

	    List<MenuItem> list = new ArrayList<>();

	    try (Connection con = ConnectionFactory.getConnection();
	         PreparedStatement ps = con.prepareStatement(sql)) {

	        ps.setObject(1, mealId);

	        try (ResultSet rs = ps.executeQuery()) {
	            while (rs.next()) {
	                MenuItem item = new MenuItem();
	                item.setId((UUID) rs.getObject("id"));
	                item.setMealId((UUID) rs.getObject("meal_id"));
	                item.setItemOrder(rs.getInt("item_order"));
	                item.setName(rs.getString("name"));
	                item.setNote(rs.getString("note"));
	                list.add(item);
	            }
	        }
	    } catch (SQLException e) {
	        throw new RuntimeException("menu_items 取得失敗", e);
	    }

	    return list;
	}

//指定した meal_slot（breakfast / lunch / dinner）に属する menu_items 一覧
  public List<MenuItem> listByMealSlot(String mealSlot) {
	  final String sql =
	      "SELECT i.id, i.meal_id, i.name, i.note " +
	      "FROM menu_items i " +
	      "JOIN menu_meals m ON i.meal_id = m.id " +
	      "WHERE m.meal_slot = ?::meal_slot " +
	      "ORDER BY i.id";

	  try (Connection con = ConnectionFactory.getConnection();
	       PreparedStatement ps = con.prepareStatement(sql)) {

	    ps.setString(1, mealSlot); // "breakfast" / "lunch" / "dinner" の小文字が入る想定

	    System.out.println("[MenuItemDAO] mealSlot = " + mealSlot);

	    try (ResultSet rs = ps.executeQuery()) {
	      List<MenuItem> list = new ArrayList<>();
	      while (rs.next()) {
	    	  MenuItem mi = new MenuItem();
	    	  mi.setId((UUID) rs.getObject("id"));

	    	  // ★ Bean は mealId なので、ここも mealId 用の setter を使う
	    	  mi.setMealId((UUID) rs.getObject("meal_id")); // or "meal_id" 実テーブル名に合わせる

	    	  mi.setName(rs.getString("name"));

	    	  mi.setNote(rs.getString("note"));
	    	  list.add(mi);
	    	}
	      System.out.println("[MenuItemDAO] items.size = " + list.size()); // ★追加
	      return list;
	    }
	  } catch (SQLException e) {
	    throw new RuntimeException("menu_items 一覧取得に失敗しました", e);
	  }
	}

//MenuItemDAO に追加
public List<MenuItem> listByOrgDateAndSlot(UUID orgId, LocalDate date, String mealSlot) {
	String sql =
		      "SELECT i.id, i.meal_id, i.item_order, i.name, i.note " +
		      "FROM menu_items i " +
		      "JOIN menu_meals m ON m.id = i.meal_id " +
		      "JOIN menu_days  d ON d.id = m.day_id " +
		      "WHERE d.org_id = ? " +
		      "  AND d.menu_date = ? " +
		      "  AND m.meal_slot = CAST(? AS meal_slot) " +   // ★ここをキャスト付きに
		      "ORDER BY i.item_order";

 List<MenuItem> list = new ArrayList<>();

 try (Connection con = ConnectionFactory.getConnection();
      PreparedStatement ps = con.prepareStatement(sql)) {

   ps.setObject(1, orgId);
   ps.setObject(2, java.sql.Date.valueOf(date));
   ps.setString(3, mealSlot); // "breakfast" / "lunch" / "dinner"

   try (ResultSet rs = ps.executeQuery()) {
     while (rs.next()) {
       MenuItem mi = new MenuItem();
       mi.setId((UUID) rs.getObject("id"));
       mi.setMealId((UUID) rs.getObject("meal_id"));
       mi.setItemOrder(rs.getInt("item_order"));
       mi.setName(rs.getString("name"));
       mi.setNote(rs.getString("note"));
       list.add(mi);
     }
   }
 } catch (SQLException e) {
   throw new RuntimeException("menu_items 一覧取得に失敗しました", e);
 }

 return list;
}

}
