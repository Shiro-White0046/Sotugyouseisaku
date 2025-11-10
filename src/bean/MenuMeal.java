package bean;

import java.util.UUID;

/**
 * 朝・昼・夜の献立
 */
public class MenuMeal {
  private UUID id;
  private UUID dayId;
  private String mealSlot;   // 'breakfast', 'lunch', 'dinner'
  private String name;
  private String description;

  // --- getter/setter ---
  public UUID getId() { return id; }
  public void setId(UUID id) { this.id = id; }

  public UUID getDayId() { return dayId; }
  public void setDayId(UUID dayId) { this.dayId = dayId; }

  public String getMealSlot() { return mealSlot; }
  public void setMealSlot(String mealSlot) { this.mealSlot = mealSlot; }

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  public String getDescription() { return description; }
  public void setDescription(String description) { this.description = description; }
}
