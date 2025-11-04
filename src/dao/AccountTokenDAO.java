package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import infra.ConnectionFactory;

/**
 * account_tokens テーブル用 DAO
 * - token: 一時パスコード（6桁など）
 * - expires_at: 有効期限（TIMESTAMPTZ）
 * - used_at: 使用済み日時（NULL = 未使用）
 */
public class AccountTokenDAO {

  /** トークン発行（INSERT） */
  public void issue(UUID orgId, String accountType, UUID accountId, String token, Instant expiresAt) {
    final String sql =
        "INSERT INTO account_tokens(id, org_id, account_type, account_id, token, expires_at) " +
        "VALUES (gen_random_uuid(), ?, ?, ?, ?, ?)";
    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

      ps.setObject(1, orgId);
      ps.setString(2, accountType);
      ps.setObject(3, accountId);
      ps.setString(4, token);
      ps.setObject(5, Timestamp.from(expiresAt));
      ps.executeUpdate();

    } catch (SQLException e) {
      throw new RuntimeException("トークン発行に失敗しました", e);
    }
  }

  /**
   * トークンの検証＋使用済み化
   * 有効: account_type + account_id + token 一致、未使用、期限内
   * 成功時 → used_at に現在時刻を設定し org_id を返す
   */
  public Optional<UUID> verifyAndConsume(String accountType, UUID accountId, String token, Instant now) {
    final String sql =
        "UPDATE account_tokens " +
        "SET used_at = now() " +
        "WHERE account_type = ? AND account_id = ? AND token = ? " +
        "AND used_at IS NULL AND expires_at > now() " +
        "RETURNING org_id";

    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

      ps.setString(1, accountType);
      ps.setObject(2, accountId);
      ps.setString(3, token);

      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          // PostgreSQLはUUID型をそのまま返せる
          UUID orgId = rs.getObject("org_id", UUID.class);
          return Optional.of(orgId);
        } else {
          return Optional.empty();
        }
      }

    } catch (SQLException e) {
      throw new RuntimeException("トークン検証に失敗しました", e);
    }
  }

  /** 古いトークンを削除（任意） */
  public int deleteExpired() {
    final String sql = "DELETE FROM account_tokens WHERE expires_at < now()";
    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
      return ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("期限切れトークンの削除に失敗しました", e);
    }
  }
}
