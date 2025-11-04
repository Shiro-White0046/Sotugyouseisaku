package dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import bean.Individual;
import infra.ConnectionFactory;

/**
 * individuals テーブル用 DAO
 * - user_id: users への FK（ON DELETE CASCADE）
 * - pin_code_hash は必要な時だけ扱う（本DAOでは基本項目を中心に）
 */
public class IndividualDAO {

  /** 1件追加して ID を返す（単体追加用） */
  public UUID addOne(UUID orgId, UUID userId, String displayName) {
    final String sql =
        "INSERT INTO individuals (org_id, user_id, display_name) " +
        "VALUES (?, ?, ?) RETURNING id";

    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

      ps.setObject(1, orgId);
      ps.setObject(2, userId);
      ps.setString(3, displayName);

      try (ResultSet rs = ps.executeQuery()) {
        rs.next();
        return (UUID) rs.getObject("id");
      }
    } catch (SQLException e) {
      throw new RuntimeException("individuals 追加に失敗しました", e);
    }
  }

  /** 複数名を一括登録（空文字は事前に除去して渡してください） */
  public void bulkInsert(UUID orgId, UUID userId, List<String> names) {
    if (names == null || names.isEmpty()) return;

    final String sql =
        "INSERT INTO individuals (org_id, user_id, display_name) VALUES (?, ?, ?)";

    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

      for (String name : names) {
        ps.setObject(1, orgId);
        ps.setObject(2, userId);
        ps.setString(3, name);
        ps.addBatch();
      }
      ps.executeBatch();

    } catch (SQLException e) {
      throw new RuntimeException("individuals の一括登録に失敗しました", e);
    }
  }

  /** ユーザー配下の個人一覧 */
  public List<Individual> listByUser(UUID userId) {
    final String sql =
        "SELECT id, org_id, user_id, display_name, birthday, note, pin_code_hash, created_at " +
        "FROM individuals WHERE user_id = ? ORDER BY created_at ASC";

    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

      ps.setObject(1, userId);

      List<Individual> list = new ArrayList<>();
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) list.add(mapIndividual(rs));
      }
      return list;

    } catch (SQLException e) {
      throw new RuntimeException("individuals の取得に失敗しました", e);
    }
  }

  /** 件数（任意） */
  public int countByUser(UUID userId) {
    final String sql = "SELECT COUNT(*) FROM individuals WHERE user_id = ?";

    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

      ps.setObject(1, userId);
      try (ResultSet rs = ps.executeQuery()) {
        rs.next();
        return rs.getInt(1);
      }

    } catch (SQLException e) {
      throw new RuntimeException("個人数の取得に失敗しました", e);
    }
  }

  /** 表示名の変更 */
  public void rename(UUID individualId, String newDisplayName) {
    final String sql = "UPDATE individuals SET display_name = ? WHERE id = ?";

    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

      ps.setString(1, newDisplayName);
      ps.setObject(2, individualId);
      ps.executeUpdate();

    } catch (SQLException e) {
      throw new RuntimeException("個人名の更新に失敗しました", e);
    }
  }

  /** 誕生日・備考の更新（任意で利用） */
  public void updateProfile(UUID individualId, LocalDate birthday, String note) {
    final String sql = "UPDATE individuals SET birthday = ?, note = ? WHERE id = ?";

    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

      if (birthday == null) ps.setNull(1, Types.DATE);
      else ps.setObject(1, Date.valueOf(birthday));
      if (note == null || note.trim().isEmpty()) ps.setNull(2, Types.VARCHAR);
      else ps.setString(2, note.trim());
      ps.setObject(3, individualId);
      ps.executeUpdate();

    } catch (SQLException e) {
      throw new RuntimeException("個人プロフィール更新に失敗しました", e);
    }
  }

  /** 1件削除 */
  public void delete(UUID individualId) {
    final String sql = "DELETE FROM individuals WHERE id = ?";

    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

      ps.setObject(1, individualId);
      ps.executeUpdate();

    } catch (SQLException e) {
      throw new RuntimeException("個人の削除に失敗しました", e);
    }
  }

  /** ユーザー配下をまとめて削除（アカウント削除時など） */
  public void deleteByUser(UUID userId) {
    final String sql = "DELETE FROM individuals WHERE user_id = ?";

    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

      ps.setObject(1, userId);
      ps.executeUpdate();

    } catch (SQLException e) {
      throw new RuntimeException("個人一括削除に失敗しました", e);
    }
  }

  // ===== 内部共通マッピング =====
  private Individual mapIndividual(ResultSet rs) throws SQLException {
    Individual i = new Individual();
    i.setId((UUID) rs.getObject("id"));
    i.setOrgId((UUID) rs.getObject("org_id"));
    i.setUserId((UUID) rs.getObject("user_id"));
    i.setDisplayName(rs.getString("display_name"));
    Date bd = rs.getDate("birthday");
    if (bd != null) i.setBirthday(bd.toLocalDate());
    i.setNote(rs.getString("note"));
    i.setPinCodeHash(rs.getString("pin_code_hash"));
    OffsetDateTime odt = rs.getObject("created_at", OffsetDateTime.class);
    if (odt != null) i.setCreatedAt(odt);
    return i;
  }
}
