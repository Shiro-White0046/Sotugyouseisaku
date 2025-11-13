package bean;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

public class User implements Serializable {
  private static final long serialVersionUID = 1L;

  private UUID id;
  private UUID orgId;
  private String email;
  private String passwordHash;
  private String name;
  private String accountType;        // "single" or "multi"
  private boolean active;
  private String loginId;           // 6桁
  private boolean mustChangePassword;
  private OffsetDateTime createdAt;

  // ★ 新しいカラム
  private UUID mainContactId;       // user_contacts.id を指す（NULL可）

  public User() {}

  public UUID getId() {
    return id;
  }
  public void setId(UUID id) {
    this.id = id;
  }

  public UUID getOrgId() {
    return orgId;
  }
  public void setOrgId(UUID orgId) {
    this.orgId = orgId;
  }

  public String getEmail() {
    return email;
  }
  public void setEmail(String email) {
    this.email = email;
  }

  public String getPasswordHash() {
    return passwordHash;
  }
  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
  }

  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  public String getAccountType() {
    return accountType;
  }
  public void setAccountType(String accountType) {
    this.accountType = accountType;
  }

  public boolean isActive() {
    return active;
  }
  public void setActive(boolean active) {
    this.active = active;
  }

  public String getLoginId() {
    return loginId;
  }
  public void setLoginId(String loginId) {
    this.loginId = loginId;
  }

  public boolean isMustChangePassword() {
    return mustChangePassword;
  }
  public void setMustChangePassword(boolean mustChangePassword) {
    this.mustChangePassword = mustChangePassword;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }
  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public UUID getMainContactId() {
    return mainContactId;
  }
  public void setMainContactId(UUID mainContactId) {
    this.mainContactId = mainContactId;
  }
}
