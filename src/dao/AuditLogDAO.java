package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import bean.AuditLog;
import infra.ConnectionFactory;

public class AuditLogDAO {

  private static final String INSERT_SQL =
      "INSERT INTO audit_logs (" +
      "  org_id, actor_type, actor_id, action, entity, entity_id, ip" +
      ") VALUES (" +
      "  ?, ?, ?, ?, ?, ?, CAST(? AS inet)" +
      ")";

  /** 1件追加 */
  public void insert(AuditLog log) {
    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(INSERT_SQL)) {

      ps.setObject(1, log.getOrgId());       // uuid
      ps.setString(2, log.getActorType());   // 'admin' or 'guardian'
      ps.setObject(3, log.getActorId());     // uuid
      ps.setString(4, log.getAction());      // 例: "login"
      ps.setString(5, log.getEntity());      // 例: "administrators"
      ps.setString(6, log.getEntityId());    // 例: adminId.toString()

      String ip = log.getIp();
      if (ip == null || ip.isEmpty()) {
        ps.setNull(7, java.sql.Types.VARCHAR);
      } else {
        ps.setString(7, ip);                 // String → CAST(? AS inet) で保存
      }

      ps.executeUpdate();

    } catch (SQLException e) {
      e.printStackTrace();
      throw new RuntimeException("操作ログの保存に失敗しました", e);
    }
  }

  /** 組織ごとの直近 N 件を取得（ログ一覧画面用） */
  public List<AuditLog> listRecentByOrg(UUID orgId, int limit) {
    String sql =
        "SELECT id, org_id, actor_type, actor_id, action, entity, entity_id, ip, created_at " +
        "FROM audit_logs " +
        "WHERE org_id = ? " +
        "ORDER BY created_at DESC " +
        "LIMIT ?";

    List<AuditLog> list = new ArrayList<>();

    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

      ps.setObject(1, orgId);
      ps.setInt(2, limit);

      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          AuditLog log = new AuditLog();

          // ★ bean.AuditLog に合わせて long / OffsetDateTime でセット
          log.setId(rs.getLong("id"));                                   // bigint → long
          log.setOrgId((UUID) rs.getObject("org_id"));                   // uuid
          log.setActorType(rs.getString("actor_type"));
          log.setActorId((UUID) rs.getObject("actor_id"));               // uuid
          log.setAction(rs.getString("action"));
          log.setEntity(rs.getString("entity"));
          log.setEntityId(rs.getString("entity_id"));
          log.setIp(rs.getString("ip"));                                 // inet → String

          OffsetDateTime odt = rs.getObject("created_at", OffsetDateTime.class);
          if (odt != null) {
            log.setCreatedAt(odt);                                      // OffsetDateTime
          }

          list.add(log);
        }
      }

    } catch (SQLException e) {
      throw new RuntimeException("操作ログ一覧の取得に失敗しました", e);
    }

    return list;
  }
}
