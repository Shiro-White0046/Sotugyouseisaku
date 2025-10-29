// src/bean/Allergen.java
package bean;

public class Allergen {
  private short id;       // SMALLSERIAL -> short
  private String code;
  private String nameJa;
  private String nameEn;
  private boolean isActive;

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
}
