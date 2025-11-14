package bean;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

public class UserContact implements Serializable {
  private static final long serialVersionUID = 1L;

  private UUID id;
  private UUID userId;
  private String label;
  private String phone;
  private OffsetDateTime createdAt;

  public UserContact() {}

  public UUID getId() { return id; }
  public void setId(UUID id) { this.id = id; }

  public UUID getUserId() { return userId; }
  public void setUserId(UUID userId) { this.userId = userId; }

  public String getLabel() { return label; }
  public void setLabel(String label) { this.label = label; }

  public String getPhone() { return phone; }
  public void setPhone(String phone) { this.phone = phone; }

  public OffsetDateTime getCreatedAt() { return createdAt; }
  public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
