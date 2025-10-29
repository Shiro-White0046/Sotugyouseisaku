// src/bean/AccountToken.java
package bean;

import java.time.OffsetDateTime;
import java.util.UUID;

public class AccountToken {
  private UUID id;
  private UUID orgId;
  /** 'admin' / 'guardian'（用語統一するなら 'administrator' / 'user'） */
  private String accountType;
  private UUID accountId;
  private String token;
  private OffsetDateTime expiresAt;
  private OffsetDateTime usedAt; // null の場合は未使用

  public AccountToken() {}

  public UUID getId() { return id; }
  public void setId(UUID id) { this.id = id; }

  public UUID getOrgId() { return orgId; }
  public void setOrgId(UUID orgId) { this.orgId = orgId; }

  public String getAccountType() { return accountType; }
  public void setAccountType(String accountType) { this.accountType = accountType; }

  public UUID getAccountId() { return accountId; }
  public void setAccountId(UUID accountId) { this.accountId = accountId; }

  public String getToken() { return token; }
  public void setToken(String token) { this.token = token; }

  public OffsetDateTime getExpiresAt() { return expiresAt; }
  public void setExpiresAt(OffsetDateTime expiresAt) { this.expiresAt = expiresAt; }

  public OffsetDateTime getUsedAt() { return usedAt; }
  public void setUsedAt(OffsetDateTime usedAt) { this.usedAt = usedAt; }
}
