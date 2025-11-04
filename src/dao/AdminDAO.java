package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import bean.Administrator;
import infra.ConnectionFactory;

/**
 * administrators テーブル用 DAO（PostgreSQL）
 * - admin_no は DB トリガで自動採番（org_counters を使用）
 * - email は org 内でユニーク（NULL 許可）
 * - created_at は TIMESTAMPTZ（OffsetDateTime で受け取る）
 */
public class AdminDAO {

  // ========= 作成（仮/本登録どちらでも） =========
  public Administrator create(UUID orgId, String email, String passwordHash, String name, boolean active) {
    final String sql =
        "INSERT INTO administrators (org_id, email, password_hash, name, is_active) " +
        "VALUES (?, ?, ?, ?, ?) " +
        "RETURNING id, org_id, email, password_hash, name, admin_no, is_active, created_at";
    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

      ps.setObject(1, orgId);
      if (email == null || email.trim().isEmpty()) ps.setNull(2, Types.VARCHAR);
      else ps.setString(2, email.trim());
      ps.setString(3, passwordHash);
      ps.setString(4, name);
      ps.setBoolean(5, active);

      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) throw new SQLException("INSERT RETURNING が空でした");
        return mapAdmin(rs, /*includeHash*/ true);
      }
    } catch (SQLException e) {
      throw new RuntimeException("管理者の作成に失敗しました", e);
    }
  }

  // ========= 有効化（メールコード検証後）＋再取得 =========
  public Administrator activateAndFetch(UUID adminId) {
    final String sql =
        "UPDATE administrators SET is_active = TRUE " +
        "WHERE id = ? " +
        "RETURNING id, org_id, email, password_hash, name, admin_no, is_active, created_at";
    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setObject(1, adminId);
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) throw new SQLException("対象の管理者が見つかりません");
        return mapAdmin(rs, /*includeHash*/ true);
      }
    } catch (SQLException e) {
      throw new RuntimeException("管理者の有効化に失敗しました", e);
    }
  }

  // ========= 単一取得 =========
  public Optional<Administrator> findById(UUID id) {
    final String sql =
        "SELECT id, org_id, email, password_hash, name, admin_no, is_active, created_at " +
        "FROM administrators WHERE id = ?";
    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setObject(1, id);
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) return Optional.empty();
        return Optional.of(mapAdmin(rs, /*includeHash*/ true));
      }
    } catch (SQLException e) {
      throw new RuntimeException("管理者の取得に失敗しました", e);
    }
  }

  public Optional<Administrator> findByOrgAndAdminNo(UUID orgId, String adminNo) {
    final String sql =
        "SELECT id, org_id, email, password_hash, name, admin_no, is_active, created_at " +
        "FROM administrators WHERE org_id = ? AND admin_no = ?";
    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setObject(1, orgId);
      ps.setString(2, adminNo);
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) return Optional.empty();
        return Optional.of(mapAdmin(rs, /*includeHash*/ true));
      }
    } catch (SQLException e) {
      throw new RuntimeException("管理者の取得に失敗しました", e);
    }
  }

  /** 組織コード＋個人番号で取得（ログイン時向け） */
  public Optional<Administrator> findByOrgCodeAndAdminNo(String orgCode, String adminNo) {
    final String sql =
        "SELECT a.id, a.org_id, a.email, a.password_hash, a.name, a.admin_no, a.is_active, a.created_at " +
        "FROM administrators a " +
        "JOIN organizations o ON a.org_id = o.id " +
        "WHERE o.code = ? AND a.admin_no = ?";
    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setString(1, orgCode);
      ps.setString(2, adminNo);
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) return Optional.empty();
        return Optional.of(mapAdmin(rs, /*includeHash*/ true));
      }
    } catch (SQLException e) {
      throw new RuntimeException("管理者の取得（orgCode+adminNo）に失敗しました", e);
    }
  }

  // ========= 一覧（org内の管理者） =========
  public List<Administrator> listByOrg(UUID orgId, int limit, int offset) {
    final String sql =
        "SELECT id, org_id, email, password_hash, name, admin_no, is_active, created_at " +
        "FROM administrators WHERE org_id = ? " +
        "ORDER BY created_at DESC " +
        "LIMIT ? OFFSET ?";
    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setObject(1, orgId);
      ps.setInt(2, Math.max(1, limit));
      ps.setInt(3, Math.max(0, offset));
      List<Administrator> list = new ArrayList<>();
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) list.add(mapAdmin(rs, /*includeHash*/ false));
      }
      return list;
    } catch (SQLException e) {
      throw new RuntimeException("管理者一覧の取得に失敗しました", e);
    }
  }

  // ========= 更新系 =========
  /** プロフィール（名前・メール）更新。email は org 内ユニーク（NULL 可）。 */
  public void updateProfile(UUID id, String name, String email) {
    final String sql =
        "UPDATE administrators SET name = ?, email = ? WHERE id = ?";
    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setString(1, name);
      if (email == null || email.trim().isEmpty()) ps.setNull(2, Types.VARCHAR);
      else ps.setString(2, email.trim());
      ps.setObject(3, id);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("プロフィール更新に失敗しました", e);
    }
  }

  /** パスワードハッシュ更新 */
  public void updatePasswordHash(UUID id, String newHash) {
    final String sql = "UPDATE administrators SET password_hash = ? WHERE id = ?";
    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setString(1, newHash);
      ps.setObject(2, id);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("パスワード更新に失敗しました", e);
    }
  }

  /** 有効/無効 切替 */
  public void setActive(UUID id, boolean active) {
    final String sql = "UPDATE administrators SET is_active = ? WHERE id = ?";
    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setBoolean(1, active);
      ps.setObject(2, id);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("有効/無効の更新に失敗しました", e);
    }
  }

  // ========= ユーティリティ =========
  /** org 内で同メールが存在するか（自身を除外したチェック。NULL 同値判定に IS NOT DISTINCT FROM を使用） */
  public boolean emailExistsInOrg(UUID orgId, String email, UUID excludeId) {
    final String sql =
        "SELECT 1 FROM administrators " +
        "WHERE org_id = ? " +
        "AND email IS NOT DISTINCT FROM ? " +
        (excludeId != null ? "AND id <> ? " : "") +
        "LIMIT 1";
    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setObject(1, orgId);
      if (email == null || email.trim().isEmpty()) ps.setNull(2, Types.VARCHAR);
      else ps.setString(2, email.trim());
      if (excludeId != null) ps.setObject(3, excludeId);
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next();
      }
    } catch (SQLException e) {
      throw new RuntimeException("メール重複チェックに失敗しました", e);
    }
  }

  // ========= 内部マッピング =========
  private Administrator mapAdmin(ResultSet rs, boolean includeHash) throws SQLException {
    Administrator a = new Administrator();
    a.setId((UUID) rs.getObject("id"));
    a.setOrgId((UUID) rs.getObject("org_id"));
    a.setEmail(rs.getString("email"));
    if (includeHash) a.setPasswordHash(rs.getString("password_hash")); // ログイン系で必要
    a.setName(rs.getString("name"));
    a.setAdminNo(rs.getString("admin_no"));
    a.setActive(rs.getBoolean("is_active"));
    OffsetDateTime odt = rs.getObject("created_at", OffsetDateTime.class);
    if (odt != null) a.setCreatedAt(odt);
    return a;
  }
}
