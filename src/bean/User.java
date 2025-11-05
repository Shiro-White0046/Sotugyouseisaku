// src/bean/User.java
package bean;

import java.time.OffsetDateTime;
import java.util.UUID;

public class User {
  private UUID id;
  private UUID orgId;
  private String email;
  private String passwordHash;
  private String name;
  private String phone;
  /** 'single' or 'multi'（DBはENUM） */
  private String accountType;
  private boolean isActive;
  private OffsetDateTime createdAt;
  private String loginId;
  private boolean mustChangePassword;

  public User() {}

  public UUID getId() { return id; }
  public void setId(UUID id) { this.id = id; }

  public UUID getOrgId() { return orgId; }
  public void setOrgId(UUID orgId) { this.orgId = orgId; }

  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }

  public String getPasswordHash() { return passwordHash; }
  public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  public String getPhone() { return phone; }
  public void setPhone(String phone) { this.phone = phone; }

  public String getAccountType() { return accountType; }
  public void setAccountType(String accountType) { this.accountType = accountType; }

  public boolean isActive() { return isActive; }
  public void setActive(boolean active) { isActive = active; }

  public OffsetDateTime getCreatedAt() { return createdAt; }
  public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

  public String getLoginId() { return loginId; }
  public void setLoginId(String loginId) { this.loginId = loginId; }
  public boolean isMustChangePassword() {
	  return mustChangePassword;
	}
	public void setMustChangePassword(boolean mustChangePassword) {
	  this.mustChangePassword = mustChangePassword;
	}
}
