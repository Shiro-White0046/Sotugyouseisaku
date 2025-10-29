// src/bean/Individual.java
package bean;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public class Individual {
  private UUID id;
  private UUID orgId;
  private UUID userId;          // 現行DDLは1ユーザーに紐づく
  private String displayName;
  private LocalDate birthday;
  private String note;
  private OffsetDateTime createdAt;

  public Individual() {}

  public UUID getId() { return id; }
  public void setId(UUID id) { this.id = id; }

  public UUID getOrgId() { return orgId; }
  public void setOrgId(UUID orgId) { this.orgId = orgId; }

  public UUID getUserId() { return userId; }
  public void setUserId(UUID userId) { this.userId = userId; }

  public String getDisplayName() { return displayName; }
  public void setDisplayName(String displayName) { this.displayName = displayName; }

  public LocalDate getBirthday() { return birthday; }
  public void setBirthday(LocalDate birthday) { this.birthday = birthday; }

  public String getNote() { return note; }
  public void setNote(String note) { this.note = note; }

  public OffsetDateTime getCreatedAt() { return createdAt; }
  public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
