package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import bean.Allergen;
import bean.Menu;
import infra.ConnectionFactory;

public class MenuDAO {

  /** 日付指定で献立取得（1件） */
  public Optional<Menu> findByDate(UUID orgId, LocalDate date, boolean onlyPublished) {
    String sql =
        "SELECT id, org_id, menu_date, name, description, image_path, published, created_at "
      + "FROM menus "
      + "WHERE org_id = ? AND menu_date = ? "
      + (onlyPublished ? "AND published = TRUE " : "")
      + "ORDER BY created_at DESC LIMIT 1";

    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setObject(1, orgId);
      ps.setObject(2, date);
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) return Optional.empty();
        return Optional.of(mapMenu(rs));
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  /** 今日の献立を取得 */
  public Optional<Menu> findToday(UUID orgId, boolean onlyPublished) {
    return findByDate(orgId, LocalDate.now(), onlyPublished);
  }

  /** 献立に含まれるアレルゲン一覧 */
  public List<Allergen> listAllergens(UUID menuId) {
    String sql =
        "SELECT a.id, a.code, a.name_ja, a.name_en, a.is_active, a.category, a.subcategory "
      + "FROM menu_allergens ma "
      + "JOIN allergens a ON a.id = ma.allergen_id "
      + "WHERE ma.menu_id = ? "
      + "ORDER BY a.id";
    List<Allergen> list = new ArrayList<>();
    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setObject(1, menuId);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) list.add(mapAllergen(rs));
      }
      return list;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private Menu mapMenu(ResultSet rs) throws SQLException {
    Menu m = new Menu();
    m.setId(rs.getObject("id", UUID.class));
    m.setOrgId(rs.getObject("org_id", UUID.class));
    m.setMenuDate(rs.getObject("menu_date", java.time.LocalDate.class));
    m.setName(rs.getString("name"));
    m.setDescription(rs.getString("description"));
    m.setImagePath(rs.getString("image_path"));
    m.setPublished(rs.getBoolean("published"));
    m.setCreatedAt(rs.getObject("created_at", java.time.OffsetDateTime.class));
    return m;
  }

  private Allergen mapAllergen(ResultSet rs) throws SQLException {
    Allergen a = new Allergen();
    a.setId(rs.getShort("id"));
    a.setCode(rs.getString("code"));
    a.setNameJa(rs.getString("name_ja"));
    a.setNameEn(rs.getString("name_en"));
    a.setActive(rs.getBoolean("is_active"));
    a.setCategory(rs.getString("category"));
    a.setSubcategory(rs.getString("subcategory"));
    return a;
  }
//追加：当月の日付→主要アレルゲン名（複数）のマップ
public Map<LocalDate, List<String>> findAllergenLabelsForMonth(UUID orgId, java.time.YearMonth ym, boolean onlyPublished) {
 String sql =
   "SELECT m.menu_date, string_agg(a.name_ja, ',' ORDER BY a.name_ja) AS labels " +
   "FROM menus m " +
   "JOIN menu_allergens ma ON ma.menu_id = m.id " +
   "JOIN allergens a ON a.id = ma.allergen_id " +
   "WHERE m.org_id = ? AND m.menu_date >= ? AND m.menu_date < ? " +
   (onlyPublished ? "AND m.published = TRUE " : "") +
   "GROUP BY m.menu_date";

 Map<LocalDate, List<String>> map = new HashMap<>();
 try (Connection con = ConnectionFactory.getConnection();
      PreparedStatement ps = con.prepareStatement(sql)) {
   java.time.LocalDate start = ym.atDay(1);
   java.time.LocalDate end   = ym.plusMonths(1).atDay(1);
   ps.setObject(1, orgId);
   ps.setObject(2, start);
   ps.setObject(3, end);
   try (ResultSet rs = ps.executeQuery()) {
     while (rs.next()) {
       LocalDate d = rs.getDate("menu_date").toLocalDate();
       String csv = rs.getString("labels");
       List<String> list = new ArrayList<>();
       if (csv != null && !csv.isEmpty()) {
         for (String s : csv.split(",")) list.add(s);
       }
       map.put(d, list);
     }
   }
 } catch (SQLException e) {
   throw new RuntimeException(e);
 }
 return map;
}
}
