package bean;

import java.util.List;
import java.util.UUID;

/**
 * menu_items テーブル + 関連するアレルゲン情報
 */
public class MenuItem {
  private UUID id;
  private UUID mealId;
  private int itemOrder;
  private String name;
  private String note;

  /** この品目に紐づくアレルゲンID一覧 */
  private List<Short> allergenIds;

  // === Getter / Setter ===
  public UUID getId() {
    return id;
  }
  public void setId(UUID id) {
    this.id = id;
  }

  public UUID getMealId() {
    return mealId;
  }
  public void setMealId(UUID mealId) {
    this.mealId = mealId;
  }

  public int getItemOrder() {
    return itemOrder;
  }
  public void setItemOrder(int itemOrder) {
    this.itemOrder = itemOrder;
  }

  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  public String getNote() {
    return note;
  }
  public void setNote(String note) {
    this.note = note;
  }

  /** アレルゲンID一覧のgetter/setter（←これがJSP側で必要） */
  public List<Short> getAllergenIds() {
    return allergenIds;
  }
  public void setAllergenIds(List<Short> allergenIds) {
    this.allergenIds = allergenIds;
  }
}
