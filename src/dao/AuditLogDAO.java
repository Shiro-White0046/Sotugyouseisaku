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
  public void insert(AuditLog log) {
	    String sql =
	        "INSERT INTO audit_logs "
	      + "  (org_id, actor_type, actor_id, action, entity, entity_id, ip) "
	      + "VALUES "
	      + "  (?, ?, ?, ?, ?, ?, ?)";

	    try (Connection con = ConnectionFactory.getConnection();
	         PreparedStatement ps = con.prepareStatement(sql)) {

	      ps.setObject(1, log.getOrgId());      // UUID
	      ps.setString(2, log.getActorType());  // "admin" or "guardian"
	      ps.setObject(3, log.getActorId());    // UUID
	      ps.setString(4, log.getAction());
	      ps.setString(5, log.getEntity());
	      ps.setString(6, log.getEntityId());
	      ps.setString(7, log.getIp());         // null OK

	      ps.executeUpdate();

	    } catch (SQLException e) {
	      throw new RuntimeException("操作ログの保存に失敗しました", e);
	    }
	  }

}
