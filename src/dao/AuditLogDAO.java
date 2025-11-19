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

  /** 直近のログを org 単位で取得（created_at DESC） */
  public List<AuditLog> listRecentByOrg(UUID orgId, int limit) {
    List<AuditLog> list = new ArrayList<>();

    String sql =
        "SELECT id, org_id, actor_type, actor_id, action, entity, entity_id, " +
        "       created_at, ip " +
        "FROM audit_logs " +
        "WHERE org_id = ? " +
        "ORDER BY created_at DESC " +
        "LIMIT ?";

    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

      ps.setObject(1, orgId);
      ps.setInt(2, limit);

      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          AuditLog log = new AuditLog();
          log.setId(rs.getLong("id"));
          log.setOrgId((UUID) rs.getObject("org_id"));
          log.setActorType(rs.getString("actor_type"));
          log.setActorId((UUID) rs.getObject("actor_id"));
          log.setAction(rs.getString("action"));
          log.setEntity(rs.getString("entity"));
          log.setEntityId(rs.getString("entity_id"));
          log.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
          log.setIp(rs.getString("ip"));
          list.add(log);
        }
      }

    } catch (SQLException e) {
      throw new RuntimeException("audit_logs 取得に失敗しました", e);
    }

    return list;
  }
}
