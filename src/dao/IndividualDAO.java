package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import bean.Individual;
import infra.ConnectionFactory;

public class IndividualDAO {

  public Optional<Individual> findById(UUID id) {
    String sql = "SELECT * FROM individuals WHERE id = ? LIMIT 1";
    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setObject(1, id);
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) return Optional.empty();
        return Optional.of(map(rs));
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public List<Individual> listByUser(UUID userId) {
    String sql = "SELECT * FROM individuals WHERE user_id = ? ORDER BY created_at DESC";
    List<Individual> list = new ArrayList<>();
    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setObject(1, userId);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) list.add(map(rs));
      }
      return list;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public UUID create(Individual i) {
	  String sql =
			    "INSERT INTO individuals (id, org_id, user_id, display_name, birthday, note, created_at) "
			  + "VALUES (gen_random_uuid(), ?, ?, ?, ?, ?, now()) "
			  + "RETURNING id";

    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setObject(1, i.getOrgId());
      ps.setObject(2, i.getUserId());
      ps.setString(3, i.getDisplayName());
      if (i.getBirthday() != null) ps.setObject(4, i.getBirthday());
      else ps.setNull(4, Types.DATE);
      ps.setString(5, i.getNote());
      try (ResultSet rs = ps.executeQuery()) {
        rs.next();
        return (UUID) rs.getObject(1);
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public boolean update(Individual i) {
    String sql = "UPDATE individuals SET display_name=?, birthday=?, note=? WHERE id=?";
    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setString(1, i.getDisplayName());
      if (i.getBirthday() != null) ps.setObject(2, i.getBirthday());
      else ps.setNull(2, Types.DATE);
      ps.setString(3, i.getNote());
      ps.setObject(4, i.getId());
      return ps.executeUpdate() == 1;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public boolean delete(UUID id) {
    String sql = "DELETE FROM individuals WHERE id = ?";
    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setObject(1, id);
      return ps.executeUpdate() == 1;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private Individual map(ResultSet rs) throws SQLException {
    Individual x = new Individual();
    x.setId((UUID) rs.getObject("id"));
    x.setOrgId((UUID) rs.getObject("org_id"));
    x.setUserId((UUID) rs.getObject("user_id"));
    x.setDisplayName(rs.getString("display_name"));
    x.setBirthday(rs.getObject("birthday", LocalDate.class));
    x.setNote(rs.getString("note"));
    x.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
    return x;
  }
}
