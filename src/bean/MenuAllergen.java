// src/bean/MenuAllergen.java
package bean;

import java.util.UUID;

public class MenuAllergen {
  private UUID menuId;
  private short allergenId;

  public MenuAllergen() {}

  public UUID getMenuId() { return menuId; }
  public void setMenuId(UUID menuId) { this.menuId = menuId; }

  public short getAllergenId() { return allergenId; }
  public void setAllergenId(short allergenId) { this.allergenId = allergenId; }
}
