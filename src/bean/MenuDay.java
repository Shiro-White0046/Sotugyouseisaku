package bean;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 1日単位の献立（日付・画像・公開状態）
 */
public class MenuDay {
  private UUID id;
  private UUID orgId;
  private LocalDate menuDate;
  private String imagePath;
  private boolean published;
  private OffsetDateTime createdAt;

  // --- getter/setter ---
  public UUID getId() { return id; }
  public void setId(UUID id) { this.id = id; }

  public UUID getOrgId() { return orgId; }
  public void setOrgId(UUID orgId) { this.orgId = orgId; }

  public LocalDate getMenuDate() { return menuDate; }
  public void setMenuDate(LocalDate menuDate) { this.menuDate = menuDate; }

  public String getImagePath() { return imagePath; }
  public void setImagePath(String imagePath) { this.imagePath = imagePath; }

  public boolean isPublished() { return published; }
  public void setPublished(boolean published) { this.published = published; }

  public OffsetDateTime getCreatedAt() { return createdAt; }
  public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
