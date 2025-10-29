// src/bean/Administrator.java
package bean;

import java.time.OffsetDateTime;
import java.util.UUID;

public class Administrator {
  private UUID id;
  private UUID orgId;
  private String email;
  private String passwordHash;
  private String name;
  private boolean isActive;
  private OffsetDateTime createdAt;

  public Administrator() {}

  public UUID getId() { return id; }
  public void setId(UUID id) { this.id = id; }

  public UUID getOrgId() { return orgId; }
  public void setOrgId(UUID orgId) { this.orgId = orgId; }

  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }

  public String getPasswordHash() { return passwordHash; }
  public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  public boolean isActive() { return isActive; }
  public void setActive(boolean active) { isActive = active; }

  public OffsetDateTime getCreatedAt() { return createdAt; }
  public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
