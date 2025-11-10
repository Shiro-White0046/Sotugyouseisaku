package bean;

import java.util.UUID;

/**
 * 献立内の品目
 */
public class MenuItem {
  private UUID id;
  private UUID mealId;
  private int itemOrder;
  private String name;
  private String note;

  // --- getter/setter ---
  public UUID getId() { return id; }
  public void setId(UUID id) { this.id = id; }

  public UUID getMealId() { return mealId; }
  public void setMealId(UUID mealId) { this.mealId = mealId; }

  public int getItemOrder() { return itemOrder; }
  public void setItemOrder(int itemOrder) { this.itemOrder = itemOrder; }

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  public String getNote() { return note; }
  public void setNote(String note) { this.note = note; }
}
