package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import bean.Administrator;
import infra.ConnectionFactory;

public class AdminDAO {

  /** 組織コード＋メールで管理者を取得（ログイン用） */
  public Optional<Administrator> findByOrgCodeAndEmail(String orgCode, String email) {
	  String sql =
			    "SELECT a.* "
			  + "FROM administrators a "
			  + "JOIN organizations o ON o.id = a.org_id "
			  + "WHERE o.code = ? AND LOWER(a.email) = LOWER(?) AND a.is_active = TRUE "
			  + "LIMIT 1";

    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setString(1, orgCode);
      ps.setString(2, email);
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) return Optional.empty();
        return Optional.of(map(rs));
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private Administrator map(ResultSet rs) throws SQLException {
    Administrator a = new Administrator();
    a.setId((UUID) rs.getObject("id"));
    a.setOrgId((UUID) rs.getObject("org_id"));
    a.setEmail(rs.getString("email"));
    a.setPasswordHash(rs.getString("password_hash"));
    a.setName(rs.getString("name"));
    a.setActive(rs.getBoolean("is_active"));
    a.setCreatedAt(rs.getObject("created_at", java.time.OffsetDateTime.class));
    return a;
  }
}
