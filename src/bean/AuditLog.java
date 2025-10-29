// src/bean/AuditLog.java
package bean;

import java.time.OffsetDateTime;
import java.util.UUID;

public class AuditLog {
  private long id;
  private UUID orgId;
  /** 'admin' / 'guardian'（用語統一するなら 'administrator' / 'user'） */
  private String actorType;
  private UUID actorId;
  private String action;
  private String entity;
  private String entityId;
  private OffsetDateTime createdAt;
  /** INET は文字列で扱うのが簡単 */
  private String ip;

  public AuditLog() {}

  public long getId() { return id; }
  public void setId(long id) { this.id = id; }

  public UUID getOrgId() { return orgId; }
  public void setOrgId(UUID orgId) { this.orgId = orgId; }

  public String getActorType() { return actorType; }
  public void setActorType(String actorType) { this.actorType = actorType; }

  public UUID getActorId() { return actorId; }
  public void setActorId(UUID actorId) { this.actorId = actorId; }

  public String getAction() { return action; }
  public void setAction(String action) { this.action = action; }

  public String getEntity() { return entity; }
  public void setEntity(String entity) { this.entity = entity; }

  public String getEntityId() { return entityId; }
  public void setEntityId(String entityId) { this.entityId = entityId; }

  public OffsetDateTime getCreatedAt() { return createdAt; }
  public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

  public String getIp() { return ip; }
  public void setIp(String ip) { this.ip = ip; }
}
