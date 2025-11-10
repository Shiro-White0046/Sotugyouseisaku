package bean;

import java.util.UUID;

/**
 * 品目×アレルゲン対応
 */
public class MenuItemAllergen {
  private UUID itemId;
  private short allergenId;

  // --- getter/setter ---
  public UUID getItemId() { return itemId; }
  public void setItemId(UUID itemId) { this.itemId = itemId; }

  public short getAllergenId() { return allergenId; }
  public void setAllergenId(short allergenId) { this.allergenId = allergenId; }
}
