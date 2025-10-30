// src/dao/IndividualAllergyDAO.java
package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

  public boolean delete(java.util.UUID personId, short allergenId) {
    String sql = "DELETE FROM individual_allergies WHERE person_id = ? AND allergen_id = ?";

    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setObject(1, personId);
      ps.setShort(2, allergenId);
      return ps.executeUpdate() == 1;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
