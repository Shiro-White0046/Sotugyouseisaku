package bean;

import java.time.OffsetDateTime;
import java.util.UUID;

/** 朝・昼・夜の献立 */
public class MenuMeal {
  private UUID id;
  private UUID dayId;
  private String slot;   // BREAKFAST / LUNCH / DINNER
  private String name;
  private String description;
  private String imagePath;   // ←追加
  private OffsetDateTime createdAt;
  private OffsetDateTime updatedAt;

  // --- getter/setter ---
  public UUID getId() { return id; }
  public void setId(UUID id) { this.id = id; }

  public UUID getDayId() { return dayId; }
  public void setDayId(UUID dayId) { this.dayId = dayId; }

  public String getSlot() { return slot; }
  public void setSlot(String slot) { this.slot = slot; }

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  public String getDescription() { return description; }
  public void setDescription(String description) { this.description = description; }

  public String getImagePath() { return imagePath; }
  public void setImagePath(String imagePath) { this.imagePath = imagePath; }

  public OffsetDateTime getCreatedAt() { return createdAt; }
  public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

  public OffsetDateTime getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
