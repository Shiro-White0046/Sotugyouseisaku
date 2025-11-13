// src/dao/IndividualAllergyDAO.java
package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import bean.Allergen;
import bean.IndividualAllergy;
import infra.ConnectionFactory;

public class IndividualAllergyDAO {

  public List<IndividualAllergy> listByPerson(java.util.UUID personId) {
    String sql =
        "SELECT person_id, allergen_id, note, confirmed_at " +
        "FROM individual_allergies " +
        "WHERE person_id = ? " +
        "ORDER BY allergen_id";

    List<IndividualAllergy> list = new ArrayList<>();
    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setObject(1, personId);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          IndividualAllergy ia = new IndividualAllergy();
          ia.setPersonId((java.util.UUID) rs.getObject("person_id"));
          ia.setAllergenId(rs.getShort("allergen_id"));
          ia.setNote(rs.getString("note"));
          ia.setConfirmedAt(rs.getObject("confirmed_at", LocalDate.class));
          list.add(ia);
        }
      }
      return list;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public List<Allergen> listAllergensOfPerson(java.util.UUID personId) {
    String sql =
        "SELECT a.id, a.code, a.name_ja, a.name_en, a.is_active, a.category, a.subcategory " +
        "FROM individual_allergies ia " +
        "JOIN allergens a ON a.id = ia.allergen_id " +
        "WHERE ia.person_id = ? " +
        "ORDER BY a.id";

    List<Allergen> list = new ArrayList<>();
    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setObject(1, personId);
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

  /** person に紐づく現在の allergen_id セットを取得 */
  public Set<Short> findAllergenIds(UUID personId) {
    final String sql = "SELECT allergen_id FROM individual_allergies WHERE person_id=? ORDER BY allergen_id";
    Set<Short> set = new LinkedHashSet<>();
    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setObject(1, personId);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) set.add(rs.getShort(1));
      }
    } catch (SQLException e) {
      throw new RuntimeException("findAllergenIds 失敗", e);
    }
    return set;
  }



  /** 追加/更新（severityなし版） */
  public boolean upsert(IndividualAllergy ia) {
    String sql =
        "INSERT INTO individual_allergies (person_id, allergen_id, note, confirmed_at) " +
        "VALUES (?, ?, ?, ?) " +
        "ON CONFLICT (person_id, allergen_id) " +
        "DO UPDATE SET note = EXCLUDED.note, confirmed_at = EXCLUDED.confirmed_at";

    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setObject(1, ia.getPersonId());
      ps.setShort(2, ia.getAllergenId());
      ps.setString(3, ia.getNote());
      if (ia.getConfirmedAt() != null) {
        ps.setObject(4, ia.getConfirmedAt());
      } else {
        ps.setNull(4, Types.DATE);
      }
      return ps.executeUpdate() == 1;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }


  /** 複数追加 */

  public void upsertMultiple(UUID personId, Collection<Short> allergenIds, String note) {
	  final String sql =
	      "INSERT INTO individual_allergies (person_id, allergen_id, note, confirmed_at) " +
	      "VALUES (?, ?, ?, now()) " +
	      "ON CONFLICT (person_id, allergen_id) " +
	      "DO UPDATE SET note = EXCLUDED.note, confirmed_at = EXCLUDED.confirmed_at";

	  try (Connection con = ConnectionFactory.getConnection();
	       PreparedStatement ps = con.prepareStatement(sql)) {

	    for (Short allergenId : allergenIds) {
	      ps.setObject(1, personId);
	      ps.setShort(2, allergenId);
	      ps.setString(3, note);
	      ps.addBatch();
	    }
	    ps.executeBatch();

	  } catch (SQLException e) {
	    throw new RuntimeException("individual_allergies 複数upsert 失敗", e);
	  }
	}



  public void delete(java.util.UUID personId) {
    String sql = "DELETE FROM individual_allergies WHERE person_id = ?";

    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setObject(1, personId);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

/** 指定 person・カテゴリのレコードだけ削除（他カテゴリは残す） */
public void deleteByCategory(UUID personId, String category) {
  final String sql =
      "DELETE FROM individual_allergies ia " +
      "USING allergens a " +
      "WHERE ia.allergen_id = a.id " +
      "  AND ia.person_id = ? " +
      "  AND a.category = ?";
  try (Connection con = ConnectionFactory.getConnection();
       PreparedStatement ps = con.prepareStatement(sql)) {
    ps.setObject(1, personId);
    ps.setString(2, category);
    ps.executeUpdate();
  } catch (SQLException e) {
    throw new RuntimeException("deleteByCategory 失敗", e);
  }
}
  public void clearIndividualAllergies() {
	    String sql = "DELETE FROM individual_allergies";
	    try (Connection con = ConnectionFactory.getConnection();
	         PreparedStatement ps = con.prepareStatement(sql)) {
	      ps.executeUpdate();
	    } catch (SQLException e) {
	      throw new RuntimeException("individual_allergies の削除に失敗しました。", e);
	    }
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
  /** 指定 person・カテゴリのレコードが1件でもあるか */
  public boolean existsByCategory(UUID personId, String category) {
    final String sql =
        "SELECT 1 " +
        "FROM individual_allergies ia " +
        "JOIN allergens a ON a.id = ia.allergen_id " +
        "WHERE ia.person_id = ? AND a.category = ? " +
        "LIMIT 1";
    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setObject(1, personId);
      ps.setString(2, category);
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next();
      }
    } catch (SQLException e) {
      throw new RuntimeException("existsByCategory 失敗", e);
    }
  }





	//返却DTO
	public class AllergyView {
	 private final java.util.UUID individualId;
	 private final String displayName;
	 private final String foods;    // category='FOOD' の名前連結
	 private final String contacts; // 'CONTACT'
	 private final String avoids;   // 'AVOID'
	 public AllergyView(java.util.UUID id, String name, String foods, String contacts, String avoids){
	   this.individualId=id; this.displayName=name;
	   this.foods = (foods==null?"":foods);
	   this.contacts = (contacts==null?"":contacts);
	   this.avoids = (avoids==null?"":avoids);
	 }
	 public java.util.UUID getIndividualId(){ return individualId; }
	 public String getDisplayName(){ return displayName; }
	 public String getFoods(){ return foods; }
	 public String getContacts(){ return contacts; }
	 public String getAvoids(){ return avoids; }
	}





	// 追加：カテゴリ別集約一覧（名前検索対応）
	public List<AllergyView> aggregateByCategory(java.util.UUID orgId, String keyword) {
	  String sql =
	    "SELECT i.id, i.display_name, " +
	    "  string_agg(CASE WHEN a.category='FOOD' THEN a.name_ja END, '・') AS foods, " +
	    "  string_agg(CASE WHEN a.category='CONTACT' THEN a.name_ja END, '・') AS contacts, " +
	    "  string_agg(CASE WHEN a.category='AVOID' THEN a.name_ja END, '・') AS avoids " +
	    "FROM individuals i " +
	    "LEFT JOIN individual_allergies ia ON ia.person_id = i.id " +
	    "LEFT JOIN allergens a ON a.id = ia.allergen_id " +
	    "WHERE i.org_id = ? " +
	    "  AND (COALESCE(?, '') = '' OR i.display_name ILIKE '%' || ? || '%') " +
	    "GROUP BY i.id, i.display_name " +
	    "ORDER BY i.id";

	  List<AllergyView> list = new ArrayList<>();
	  try (Connection con = ConnectionFactory.getConnection();
	       PreparedStatement ps = con.prepareStatement(sql)) {
	    ps.setObject(1, orgId);
	    String q = (keyword == null) ? "" : keyword.trim();
	    ps.setString(2, q);
	    ps.setString(3, q);
	    try (ResultSet rs = ps.executeQuery()) {
	      while (rs.next()) {
	        list.add(new AllergyView(
	          (java.util.UUID) rs.getObject("id"),
	          rs.getString("display_name"),
	          rs.getString("foods"),
	          rs.getString("contacts"),
	          rs.getString("avoids")
	        ));
	      }
	    }
	  } catch (SQLException e) {
	    throw new RuntimeException("アレルギー集約の取得に失敗しました", e);
	  }
	  return list;
	}


}
