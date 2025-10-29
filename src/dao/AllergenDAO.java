package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
}
