package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import bean.User;
import infra.ConnectionFactory;

public class UserDAO {

  /** 組織コード＋メールで利用者を取得（ログイン用） */
  public Optional<User> findByOrgCodeAndEmail(String orgCode, String email) {
	  String sql =
			    "SELECT u.* "
			  + "FROM users u "
			  + "JOIN organizations o ON o.id = u.org_id "
			  + "WHERE o.code = ? AND LOWER(u.email) = LOWER(?) AND u.is_active = TRUE "
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

  private User map(ResultSet rs) throws SQLException {
    User u = new User();
    u.setId((UUID) rs.getObject("id"));
    u.setOrgId((UUID) rs.getObject("org_id"));
    u.setEmail(rs.getString("email"));
    u.setPasswordHash(rs.getString("password_hash"));
    u.setName(rs.getString("name"));
    u.setPhone(rs.getString("phone"));
    u.setAccountType(rs.getString("account_type"));
    u.setActive(rs.getBoolean("is_active"));
    u.setCreatedAt(rs.getObject("created_at", java.time.OffsetDateTime.class));
    return u;
  }
}
