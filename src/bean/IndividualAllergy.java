// src/bean/IndividualAllergy.java
package bean;

import java.time.LocalDate;
import java.util.UUID;

public class IndividualAllergy {
  private UUID personId;
  private short allergenId;   // SMALLINT -> short
  private String note;
  private LocalDate confirmedAt;

  public IndividualAllergy() {}

  public UUID getPersonId() { return personId; }
  public void setPersonId(UUID personId) { this.personId = personId; }

  public short getAllergenId() { return allergenId; }
  public void setAllergenId(short allergenId) { this.allergenId = allergenId; }

  public String getNote() { return note; }
  public void setNote(String note) { this.note = note; }

  public LocalDate getConfirmedAt() { return confirmedAt; }
  public void setConfirmedAt(LocalDate confirmedAt) { this.confirmedAt = confirmedAt; }
}
