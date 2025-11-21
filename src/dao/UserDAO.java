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

/** users テーブル用 DAO */
public class UserDAO {

  /** アカウント新規作成（仮 PW ハッシュ保存、is_active=TRUE） */
  public User create(UUID orgId, String name, String passwordHash, String accountType) {
    final String sql =
        "INSERT INTO users (org_id, email, password_hash, name, account_type, is_active) " +
        "VALUES (?, NULL, ?, ?, ?::account_type, TRUE) " +
        // phone を RETURNING から削除し、main_contact_id を追加
        "RETURNING id, org_id, email, password_hash, name, account_type, is_active, " +
        "login_id, must_change_password, main_contact_id, created_at";

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

        // phone はすでに存在しない
        // u.setPhone(rs.getString("phone"));

        u.setAccountType(rs.getString("account_type"));
        u.setActive(rs.getBoolean("is_active"));
        u.setLoginId(rs.getString("login_id"));
        u.setMustChangePassword(rs.getBoolean("must_change_password"));

        OffsetDateTime odt = rs.getObject("created_at", OffsetDateTime.class);
        if (odt != null) u.setCreatedAt(odt);

        // ★ main_contact_id 追加
        Object mc = rs.getObject("main_contact_id");
        if (mc instanceof UUID) {
          u.setMainContactId((UUID) mc);
        } else if (mc instanceof String) {
          try { u.setMainContactId(UUID.fromString((String) mc)); } catch (Exception ignore) {}
        }

        return Optional.of(u);
      }
    } catch (SQLException e) {
      throw new RuntimeException("ユーザー取得に失敗しました", e);
    }
  }

  /** 主キーで取得 */
  public Optional<User> findById(UUID id) {
    final String sql =
        // phone を削除し main_contact_id を追加
        "SELECT id, org_id, email, password_hash, name, account_type, is_active, " +
        "login_id, must_change_password, main_contact_id, created_at " +
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

  /** 初回ログイン：プロフィール＆パスワード更新 */
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
    final String sql =
        "UPDATE users SET password_hash = ?, must_change_password = FALSE WHERE id = ?";
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

    // phone 削除済み
    // u.setPhone(rs.getString("phone"));

    u.setAccountType(rs.getString("account_type"));
    u.setActive(rs.getBoolean("is_active"));
    u.setLoginId(rs.getString("login_id"));
    u.setMustChangePassword(rs.getBoolean("must_change_password"));

    OffsetDateTime odt = rs.getObject("created_at", OffsetDateTime.class);
    if (odt != null) u.setCreatedAt(odt);

    // ★ main_contact_id 追加
    Object mc = rs.getObject("main_contact_id");
    if (mc instanceof UUID) {
      u.setMainContactId((UUID) mc);
    } else if (mc instanceof String) {
      try { u.setMainContactId(UUID.fromString((String) mc)); } catch (Exception ignore) {}
    }

    return u;
  }

  public void updateNameAndType(UUID userId, String name, String accountType) {
    final String sql =
        "UPDATE users SET name = ?, account_type = ?::account_type WHERE id = ?";

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
  /** main_contact_id を更新する（緊急連絡先の紐づけ）!!!!ワンチャン消す */
  public void updateMainContactId(UUID userId, UUID contactId) {
	  final String sql = "UPDATE users SET main_contact_id = ? WHERE id = ?";

	  try (Connection con = ConnectionFactory.getConnection();
	       PreparedStatement ps = con.prepareStatement(sql)) {

	    ps.setObject(1, contactId);
	    ps.setObject(2, userId);
	    ps.executeUpdate();

	  } catch (SQLException e) {
	    throw new RuntimeException("main_contact_id の更新に失敗しました", e);
	  }
	}

 public void withdrawUser(UUID userId) {
   // 個人アレルギー
   String sqlDeleteIa =
       "DELETE FROM individual_allergies " +
       "WHERE person_id IN (SELECT id FROM individuals WHERE user_id = ?)";

   // 個人（子ども）
   String sqlDeleteInd =
       "DELETE FROM individuals WHERE user_id = ?";

   // 緊急連絡先
   String sqlDeleteContacts =
       "DELETE FROM user_contacts WHERE user_id = ?";

   // アカウントトークン（保護者分）
   String sqlDeleteTokens =
       "DELETE FROM account_tokens " +
       "WHERE account_type = 'guardian' AND account_id = ?";

   // ユーザー本体
   String sqlDeleteUser =
       "DELETE FROM users WHERE id = ?";

   try (Connection con = ConnectionFactory.getConnection()) {
     try {
       con.setAutoCommit(false);

       // 1) 子どもに紐づくアレルギー削除
       try (PreparedStatement ps1 = con.prepareStatement(sqlDeleteIa)) {
         ps1.setObject(1, userId);
         ps1.executeUpdate();
       }

       // 2) 子どもレコード削除
       try (PreparedStatement ps2 = con.prepareStatement(sqlDeleteInd)) {
         ps2.setObject(1, userId);
         ps2.executeUpdate();
       }

       // 3) 緊急連絡先削除
       try (PreparedStatement ps3 = con.prepareStatement(sqlDeleteContacts)) {
         ps3.setObject(1, userId);
         ps3.executeUpdate();
       }

       // 4) guardian 向けトークン削除
       try (PreparedStatement ps4 = con.prepareStatement(sqlDeleteTokens)) {
         ps4.setObject(1, userId);
         ps4.executeUpdate();
       }

       // 5) ユーザー本体削除
       try (PreparedStatement ps5 = con.prepareStatement(sqlDeleteUser)) {
         ps5.setObject(1, userId);
         ps5.executeUpdate();
       }

       con.commit();
     } catch (SQLException e) {
       con.rollback();
       throw new RuntimeException("退会処理に失敗しました", e);
     } finally {
       con.setAutoCommit(true);
     }
   } catch (SQLException e) {
     throw new RuntimeException("退会処理に失敗しました", e);
   }
 }

 public void updateEmail(UUID userId, String email) {
	    final String sql = "UPDATE users SET email = ? WHERE id = ?";
	    try (Connection con = ConnectionFactory.getConnection();
	         PreparedStatement ps = con.prepareStatement(sql)) {

	      if (email == null || email.isEmpty()) {
	        ps.setNull(1, java.sql.Types.VARCHAR);
	      } else {
	        ps.setString(1, email);
	      }
	      ps.setObject(2, userId);
	      ps.executeUpdate();

	    } catch (SQLException e) {
	      throw new RuntimeException("メールアドレス更新に失敗しました", e);
	    }
	}

}
