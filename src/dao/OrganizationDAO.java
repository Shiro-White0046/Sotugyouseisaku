package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import bean.Organization;
import infra.ConnectionFactory;

public class OrganizationDAO {

  /** 組織コードで1件検索 */
  public Optional<Organization> findByCode(String code) {
    String sql = "SELECT id, code, name, created_at FROM organizations WHERE code = ? LIMIT 1";
    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setString(1, code);
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) return Optional.empty();
        Organization o = new Organization();
        o.setId((UUID) rs.getObject("id"));
        o.setCode(rs.getString("code"));
        o.setName(rs.getString("name"));
        o.setCreatedAt(rs.getObject("created_at", java.time.OffsetDateTime.class));
        return Optional.of(o);
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
