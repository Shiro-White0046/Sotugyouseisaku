package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import bean.User;
import infra.ConnectionFactory;

/**
 * users テーブル用 DAO
 * - login_id は DB トリガで 6 桁採番（org_counters）
 * - email は NULL 許可（管理者登録時は使わない）
 * - created_at は TIMESTAMPTZ
 */
public class UserDAO {

  /** アカウント新規作成（仮 PW ハッシュ保存、name/email は NULL、is_active=TRUE） */
	public User create(UUID orgId, String name, String passwordHash, String accountType) {
		  final String sql =
		      "INSERT INTO users (org_id, email, password_hash, name, account_type, is_active) " +
		      "VALUES (?, NULL, ?, ?, ?::account_type, TRUE) " +  // ← name を ? に置き換え
		      "RETURNING id, org_id, email, password_hash, name, account_type, is_active, login_id, created_at";

		  try (Connection con = ConnectionFactory.getConnection();
		       PreparedStatement ps = con.prepareStatement(sql)) {

		    ps.setObject(1, orgId);
		    ps.setString(2, passwordHash);
		    ps.setString(3, name);
		    ps.setString(4, accountType);

		    try (ResultSet rs = ps.executeQuery()) {
		      rs.next();
		      return mapUser(rs, true);
		    }

		  } catch (SQLException e) {
		    throw new RuntimeException("利用者アカウントの作成に失敗しました", e);
		  }
		}



  /** 組織コード＋login_id で取得（ログイン時に使用） */
  public Optional<User> findByOrgCodeAndLoginId(String orgCode, String loginId) {
    final String sql =
        "SELECT u.id, u.org_id, u.email, u.password_hash, u.name, u.account_type, u.is_active, u.login_id, u.created_at " +
        "FROM users u JOIN organizations o ON u.org_id = o.id " +
        "WHERE o.code = ? AND u.login_id = ?";

    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

      ps.setString(1, orgCode);
      ps.setString(2, loginId);

      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) return Optional.empty();
        return Optional.of(mapUser(rs, /*includeHash*/ true));
      }
    } catch (SQLException e) {
      throw new RuntimeException("ユーザー取得（orgCode+loginId）に失敗しました", e);
    }
  }

  /** 主キーで取得（管理系で使うことあり） */
  public Optional<User> findById(UUID id) {
    final String sql =
        "SELECT id, org_id, email, password_hash, name, account_type, is_active, login_id, created_at " +
        "FROM users WHERE id = ?";

    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

      ps.setObject(1, id);

      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) return Optional.empty();
        return Optional.of(mapUser(rs, /*includeHash*/ true));
      }
    } catch (SQLException e) {
      throw new RuntimeException("ユーザー取得（id）に失敗しました", e);
    }
  }

  /** 初回ログインでのプロフィール＆パスワード更新（name と password_hash を同時更新） */
  public void updateProfileAndPassword(UUID userId, String name, String newPasswordHash) {
    final String sql = "UPDATE users SET name = ?, password_hash = ? WHERE id = ?";

    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

      ps.setString(1, name);
      ps.setString(2, newPasswordHash);
      ps.setObject(3, userId);
      ps.executeUpdate();

    } catch (SQLException e) {
      throw new RuntimeException("プロフィール・パスワード更新に失敗しました", e);
    }
  }

  /** 表示名だけ更新（管理者側の編集など） */
  public void updateName(UUID userId, String name) {
    final String sql = "UPDATE users SET name = ? WHERE id = ?";

    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

      ps.setString(1, name);
      ps.setObject(2, userId);
      ps.executeUpdate();

    } catch (SQLException e) {
      throw new RuntimeException("ユーザー名の更新に失敗しました", e);
    }
  }

  /** パスワードハッシュだけ更新（任意運用） */
  public void updatePasswordHash(UUID userId, String newPasswordHash) {
    final String sql = "UPDATE users SET password_hash = ? WHERE id = ?";

    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

      ps.setString(1, newPasswordHash);
      ps.setObject(2, userId);
      ps.executeUpdate();

    } catch (SQLException e) {
      throw new RuntimeException("パスワード更新に失敗しました", e);
    }
  }

  /** 有効/無効切替（任意運用） */
  public void setActive(UUID userId, boolean active) {
    final String sql = "UPDATE users SET is_active = ? WHERE id = ?";

    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

      ps.setBoolean(1, active);
      ps.setObject(2, userId);
      ps.executeUpdate();

    } catch (SQLException e) {
      throw new RuntimeException("有効/無効の更新に失敗しました", e);
    }
  }

  // ===== 内部共通マッピング =====
  private User mapUser(ResultSet rs, boolean includeHash) throws SQLException {
    User u = new User();
    u.setId((UUID) rs.getObject("id"));
    u.setOrgId((UUID) rs.getObject("org_id"));
    u.setEmail(rs.getString("email"));
    if (includeHash) u.setPasswordHash(rs.getString("password_hash"));
    u.setName(rs.getString("name"));
    u.setAccountType(rs.getString("account_type"));
    u.setActive(rs.getBoolean("is_active"));
    u.setLoginId(rs.getString("login_id"));
    OffsetDateTime odt = rs.getObject("created_at", OffsetDateTime.class);
    if (odt != null) u.setCreatedAt(odt);
    return u;
  }
}
