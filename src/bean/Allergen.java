package bean;

public class Allergen {
  private short id;
  private String code;
  private String nameJa;
  private String nameEn;
  private boolean isActive;

  // 追加フィールド
  /** 'FOOD' / 'CONTACT' / 'AVOID' */
  private String category;
  /** CONTACT のときのみ: 'ANIMAL' / 'METAL' / 'PLANT' / 'CHEMICAL' / 'OTHER'、それ以外は null */
  private String subcategory;

  public Allergen() {}

  public short getId() { return id; }
  public void setId(short id) { this.id = id; }
  public String getCode() { return code; }
  public void setCode(String code) { this.code = code; }
  public String getNameJa() { return nameJa; }
  public void setNameJa(String nameJa) { this.nameJa = nameJa; }
  public String getNameEn() { return nameEn; }
  public void setNameEn(String nameEn) { this.nameEn = nameEn; }
  public boolean isActive() { return isActive; }
  public void setActive(boolean active) { isActive = active; }

  public String getCategory() { return category; }
  public void setCategory(String category) { this.category = category; }
  public String getSubcategory() { return subcategory; }
  public void setSubcategory(String subcategory) { this.subcategory = subcategory; }
}
