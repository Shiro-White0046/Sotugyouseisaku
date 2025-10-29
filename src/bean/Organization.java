// src/bean/Organization.java
package bean;

import java.time.OffsetDateTime;
import java.util.UUID;

public class Organization {
  private UUID id;
  private String code;
  private String name;
  private OffsetDateTime createdAt;

  public Organization() {}

  public UUID getId() { return id; }
  public void setId(UUID id) { this.id = id; }

  public String getCode() { return code; }
  public void setCode(String code) { this.code = code; }

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  public OffsetDateTime getCreatedAt() { return createdAt; }
  public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
