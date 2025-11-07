package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
// import java.sql.Timestamp; // ← 不要になったら消す
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import bean.User;
import infra.ConnectionFactory;

/** users テーブル用 DAO */
public class UserDAO {

  /** アカウント新規作成（仮 PW ハッシュ保存、is_active=TRUE） */
  public User create(UUID orgId, String name, String passwordHash, String accountType) {
    final String sql =
        "INSERT INTO users (org_id, email, password_hash, name, account_type, is_active) " +
        "VALUES (?, NULL, ?, ?, ?::account_type, TRUE) " +
        "RETURNING id, org_id, email, password_hash, name, phone, account_type, is_active, login_id, must_change_password, created_at";
    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setObject(1, orgId);
      ps.setString(2, passwordHash);
      ps.setString(3, name);
      ps.setString(4, accountType);
      try (ResultSet rs = ps.executeQuery()) {
        rs.next();
        return mapUser(rs, /*includeHash*/ true);
      }
    } catch (SQLException e) {
      throw new RuntimeException("利用者アカウントの作成に失敗しました", e);
    }
  }

  /** 組織コード＋login_id で取得（ログイン時に使用） */
  public Optional<User> findByOrgCodeAndLoginId(String orgCode, String loginId) {
    final String sql =
        "SELECT u.* FROM users u " +
        "JOIN organizations o ON o.id = u.org_id " +
        "WHERE o.code = ? AND u.login_id = ?";
    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setString(1, orgCode);
      ps.setString(2, loginId);
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) return Optional.empty();

        User u = new User();
        u.setId((UUID) rs.getObject("id"));
        u.setOrgId((UUID) rs.getObject("org_id"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setName(rs.getString("name"));
        u.setPhone(rs.getString("phone"));
        u.setAccountType(rs.getString("account_type"));
        u.setActive(rs.getBoolean("is_active"));
        u.setLoginId(rs.getString("login_id"));
        u.setMustChangePassword(rs.getBoolean("must_change_password"));
        // ← created_at は OffsetDateTime に統一
        OffsetDateTime odt = rs.getObject("created_at", OffsetDateTime.class);
        if (odt != null) u.setCreatedAt(odt);

        return Optional.of(u);
      }
    } catch (SQLException e) {
      throw new RuntimeException("ユーザー取得に失敗しました", e);
    }
  }

  /** 主キーで取得 */
  public Optional<User> findById(UUID id) {
    final String sql =
        "SELECT id, org_id, email, password_hash, name, phone, account_type, is_active, login_id, must_change_password, created_at " +
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

  /** 初回ログイン：プロフィール＆パスワード更新（※must_change_passwordは別メソッドでfalse化） */
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

  /** 初回PW変更完了時にフラグを下ろす */
  public void updatePasswordAndClearFlag(UUID userId, String newHash) {
    final String sql = "UPDATE users SET password_hash = ?, must_change_password = FALSE WHERE id = ?";
    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setString(1, newHash);
      ps.setObject(2, userId);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("パスワード更新に失敗しました", e);
    }
  }

  // ===== 共通マッピング =====
  private User mapUser(ResultSet rs, boolean includeHash) throws SQLException {
    User u = new User();
    u.setId((UUID) rs.getObject("id"));
    u.setOrgId((UUID) rs.getObject("org_id"));
    u.setEmail(rs.getString("email"));
    if (includeHash) u.setPasswordHash(rs.getString("password_hash"));
    u.setName(rs.getString("name"));
    u.setPhone(rs.getString("phone"));
    u.setAccountType(rs.getString("account_type"));
    u.setActive(rs.getBoolean("is_active"));
    u.setLoginId(rs.getString("login_id"));
    u.setMustChangePassword(rs.getBoolean("must_change_password"));
    OffsetDateTime odt = rs.getObject("created_at", OffsetDateTime.class);
    if (odt != null) u.setCreatedAt(odt);
    return u;
  }
	//dao/UserDAO.java に追記
	public void updateNameAndType(java.util.UUID userId, String name, String accountType) {
	 final String sql = "UPDATE users SET name = ?, account_type = ?::account_type WHERE id = ?";
	 try (Connection con = ConnectionFactory.getConnection();
	      PreparedStatement ps = con.prepareStatement(sql)) {
	   ps.setString(1, name);
	   ps.setString(2, accountType);
	   ps.setObject(3, userId);
	   ps.executeUpdate();
	 } catch (SQLException e) {
	   throw new RuntimeException("ユーザーの更新に失敗しました", e);
	 }
	}
}
