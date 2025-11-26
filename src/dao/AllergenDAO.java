package dao;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import bean.Allergen;
import infra.ConnectionFactory;

public class AllergenDAO {

  /** 全有効アレルゲン一覧 */
  public List<Allergen> listActive() {
    String sql =
        "SELECT id, code, name_ja, name_en, is_active, category, subcategory "
      + "FROM allergens "
      + "WHERE is_active = TRUE "
      + "ORDER BY id";
    return queryList(sql, null);
  }

  /** カテゴリ別一覧（FOOD, CONTACT, AVOID） */
  public List<Allergen> listByCategory(String category) {
    String sql =
        "SELECT id, code, name_ja, name_en, is_active, category, subcategory "
      + "FROM allergens "
      + "WHERE is_active = TRUE AND category = ? "
      + "ORDER BY id";
    return queryList(sql, category);
  }

  /** CONTACTカテゴリの小分類別 */
  public List<Allergen> listContactBySub(String subcategory) {
    String sql =
        "SELECT id, code, name_ja, name_en, is_active, category, subcategory "
      + "FROM allergens "
      + "WHERE is_active = TRUE AND category = 'CONTACT' AND subcategory = ? "
      + "ORDER BY id";
    return queryList(sql, subcategory);
  }


  public List<Allergen> listAvoidBySub(String subcategory) {
	    String sql =
	        "SELECT id, code, name_ja, name_en, is_active, category, subcategory "
	      + "FROM allergens "
	      + "WHERE is_active = TRUE AND category = 'AVOID' AND subcategory = ? "
	      + "ORDER BY id";
	    return queryList(sql, subcategory);
	  }









  /** 共通クエリ処理 */
  private List<Allergen> queryList(String sql, String param) {
    List<Allergen> list = new ArrayList<>();
    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
      if (param != null) ps.setString(1, param);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          Allergen a = new Allergen();
          a.setId(rs.getShort("id"));
          a.setCode(rs.getString("code"));
          a.setNameJa(rs.getString("name_ja"));
          a.setNameEn(rs.getString("name_en"));
          a.setActive(rs.getBoolean("is_active"));
          a.setCategory(rs.getString("category"));
          a.setSubcategory(rs.getString("subcategory"));
          list.add(a);
        }
      }
      return list;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }




  /** 指定ID群を“受け取った順”で返す */
  public List<Allergen> findByIdsPreserveOrder(List<Short> ids) {
    if (ids == null || ids.isEmpty()) return java.util.Collections.emptyList();

    String inPlaceholders = ids.stream().map(x -> "?").collect(java.util.stream.Collectors.joining(","));
    // CASE WHEN id = ? THEN 1 ... の動的生成
    StringBuilder order = new StringBuilder("CASE id ");
    for (int i = 0; i < ids.size(); i++) {
      order.append("WHEN ? THEN ").append(i + 1).append(" ");
    }
    order.append("END");

    String sql =
        "SELECT id, code, name_ja, name_en, is_active, category, subcategory " +
        "FROM allergens " +
        "WHERE id IN (" + inPlaceholders + ") " +
        "ORDER BY " + order;  // ここで受け取った順を適用

    List<Allergen> list = new ArrayList<>();
    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

      int idx = 1;
      // IN 句の ? をセット
      for (Short id : ids) ps.setShort(idx++, id);
      // ORDER BY CASE の ? をセット（同じID列をもう一度）
      for (Short id : ids) ps.setShort(idx++, id);

      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          Allergen a = new Allergen();
          a.setId(rs.getShort("id"));
          a.setCode(rs.getString("code"));
          a.setNameJa(rs.getString("name_ja"));
          a.setNameEn(rs.getString("name_en"));
          a.setActive(rs.getBoolean("is_active"));
          a.setCategory(rs.getString("category"));
          a.setSubcategory(rs.getString("subcategory"));
          list.add(a);
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    return list;
  }

  public boolean exists(UUID individualId) {
	    final String sql = "SELECT 1 FROM individual_allergies WHERE person_id=?";
	    try (Connection con = ConnectionFactory.getConnection();
	         PreparedStatement ps = con.prepareStatement(sql)) {
	      ps.setObject(1, individualId);
	      try (ResultSet rs = ps.executeQuery()) {
	        return rs.next();
	      }
	    } catch (SQLException e) {
	      throw new RuntimeException("exists 失敗", e);
	    }
	  }


  /** 主キーでアレルゲン1件取得 */
  public Optional<Allergen> findById(short id) {
      final String sql =
          "SELECT id, code, name_ja, name_en, is_active, category, subcategory " +
          "FROM allergens WHERE id = ?";

      try (Connection con = ConnectionFactory.getConnection();
           PreparedStatement ps = con.prepareStatement(sql)) {

          ps.setShort(1, id);

          try (ResultSet rs = ps.executeQuery()) {
              if (rs.next()) {
                  Allergen a = new Allergen();
                  a.setId(rs.getShort("id"));
                  a.setCode(rs.getString("code"));
                  a.setNameJa(rs.getString("name_ja"));
                  a.setNameEn(rs.getString("name_en"));
                  a.setActive(rs.getBoolean("is_active"));
                  a.setCategory(rs.getString("category"));
                  a.setSubcategory(rs.getString("subcategory"));
                  return Optional.of(a);
              }
          }
      } catch (SQLException e) {
          throw new RuntimeException("findById failed", e);
      }

      return Optional.empty();
  }
  public List<Allergen> findAllByIds(List<Short> ids) {
	  if (ids == null || ids.isEmpty()) {
	    return Collections.emptyList();
	  }

	  final String sql =
	      "SELECT id, code, name_ja, name_en, is_active, category, subcategory " +
	      "FROM allergens WHERE id = ANY (?) AND is_active = true";

	  List<Allergen> list = new ArrayList<>();

	  try (Connection con = ConnectionFactory.getConnection();
	       PreparedStatement ps = con.prepareStatement(sql)) {

	    Short[] arr = ids.toArray(new Short[0]);
	    Array sqlArray = con.createArrayOf("smallint", arr);
	    ps.setArray(1, sqlArray);

	    try (ResultSet rs = ps.executeQuery()) {
	      while (rs.next()) {
	        Allergen a = new Allergen();
	        a.setId(rs.getShort("id"));
	        a.setCode(rs.getString("code"));
	        a.setNameJa(rs.getString("name_ja"));
	        a.setNameEn(rs.getString("name_en"));
	        a.setActive(rs.getBoolean("is_active"));
	        a.setCategory(rs.getString("category"));
	        a.setSubcategory(rs.getString("subcategory"));
	        list.add(a);
	      }
	    }
	  } catch (Exception e) {
	    throw new RuntimeException("findAllByIds error", e);
	  }

	  return list;
	}

  /** 管理画面用：名前・カテゴリ・サブカテゴリで部分一致検索 */
  public List<Allergen> searchForAdmin(String keyword) {
    List<Allergen> list = new ArrayList<>();

    String sql =
        "SELECT id, code, name_ja, name_en, is_active, category, subcategory " +
        "FROM allergens ";

    boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
    if (hasKeyword) {
      sql += "WHERE (name_ja ILIKE ? OR category ILIKE ? OR subcategory ILIKE ?) ";
    }
    sql += "ORDER BY id";

    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

      if (hasKeyword) {
        String like = "%" + keyword.trim() + "%";
        ps.setString(1, like);
        ps.setString(2, like);
        ps.setString(3, like);
      }

      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          Allergen a = new Allergen();
          a.setId(rs.getShort("id"));
          a.setCode(rs.getString("code"));
          a.setNameJa(rs.getString("name_ja"));
          a.setNameEn(rs.getString("name_en"));
          a.setActive(rs.getBoolean("is_active"));
          a.setCategory(rs.getString("category"));
          a.setSubcategory(rs.getString("subcategory"));
          list.add(a);
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("allergens 検索に失敗しました", e);
    }

    return list;
  }

  /** 管理画面用：アレルギーを追加 */
  public void insertForAdmin(String nameJa, String category, String subcategory) {

    final String sql =
        "INSERT INTO allergens (code, name_ja, name_en, is_active, category, subcategory) " +
        "VALUES (?, ?, NULL, TRUE, ?, ?)";

    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

      String code = generateCode(nameJa); // 簡易コード生成

      ps.setString(1, code);
      ps.setString(2, nameJa);
      ps.setString(3, category);
      ps.setString(4, subcategory);

      ps.executeUpdate();

    } catch (SQLException e) {
      throw new RuntimeException("allergens 追加に失敗しました", e);
    }
  }

  /** code を簡易自動生成（必要なら好きなロジックに変更してOK） */
  private String generateCode(String nameJa) {
    if (nameJa == null || nameJa.isEmpty()) return "ALGN";
    // 記号などを消して大文字にするだけの簡易版
    return nameJa.replaceAll("\\s+", "").toUpperCase();
  }




}


