// src/bean/Menu.java
package bean;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public class Menu {
  private UUID id;
  private UUID orgId;
  private LocalDate menuDate;
  private String name;
  private String description;
  private String imagePath;
  private boolean published;
  private OffsetDateTime createdAt;

  public Menu() {}

  public UUID getId() { return id; }
  public void setId(UUID id) { this.id = id; }

  public UUID getOrgId() { return orgId; }
  public void setOrgId(UUID orgId) { this.orgId = orgId; }

  public LocalDate getMenuDate() { return menuDate; }
  public void setMenuDate(LocalDate menuDate) { this.menuDate = menuDate; }

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  public String getDescription() { return description; }
  public void setDescription(String description) { this.description = description; }

  public String getImagePath() { return imagePath; }
  public void setImagePath(String imagePath) { this.imagePath = imagePath; }

  public boolean isPublished() { return published; }
  public void setPublished(boolean published) { this.published = published; }

  public OffsetDateTime getCreatedAt() { return createdAt; }
  public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
