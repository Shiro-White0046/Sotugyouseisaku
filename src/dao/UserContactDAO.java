package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import bean.UserContact;
import infra.ConnectionFactory;

public class UserContactDAO {

  /** 1件追加して ID を返す */
  public UUID addOne(UUID userId, String label, String phone) {
    final String sql =
        "INSERT INTO user_contacts (user_id, label, phone) " +
        "VALUES (?, ?, ?) " +
        "RETURNING id";

    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

      ps.setObject(1, userId);
      ps.setString(2, label);
      ps.setString(3, phone);

      try (ResultSet rs = ps.executeQuery()) {
        rs.next();
        return (UUID) rs.getObject("id");
      }
    } catch (SQLException e) {
      throw new RuntimeException("user_contacts の追加に失敗しました", e);
    }
  }

  /** 主キーで取得 */
  public Optional<UserContact> findById(UUID id) {
    final String sql =
        "SELECT id, user_id, label, phone, created_at " +
        "FROM user_contacts WHERE id = ?";

    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

      ps.setObject(1, id);

      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) return Optional.empty();

        UserContact c = new UserContact();
        c.setId((UUID) rs.getObject("id"));
        c.setUserId((UUID) rs.getObject("user_id"));
        c.setLabel(rs.getString("label"));
        c.setPhone(rs.getString("phone"));
        OffsetDateTime odt = rs.getObject("created_at", OffsetDateTime.class);
        if (odt != null) c.setCreatedAt(odt);

        return Optional.of(c);
      }

    } catch (SQLException e) {
      throw new RuntimeException("user_contacts の取得に失敗しました", e);
    }
  }

  /** ラベル・電話番号の更新 */
  public void update(UUID id, String label, String phone) {
    final String sql =
        "UPDATE user_contacts SET label = ?, phone = ? WHERE id = ?";

    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

      ps.setString(1, label);
      ps.setString(2, phone);
      ps.setObject(3, id);
      ps.executeUpdate();

    } catch (SQLException e) {
      throw new RuntimeException("user_contacts の更新に失敗しました", e);
    }
  }
}
