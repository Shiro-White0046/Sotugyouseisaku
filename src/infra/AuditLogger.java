package infra;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import bean.Administrator;
import bean.AuditLog;
import bean.Organization;
import bean.User;
import dao.AuditLogDAO;

public class AuditLogger {

  private static final AuditLogDAO dao = new AuditLogDAO();

  /* ========================
       取得：IP アドレス
     ======================== */
  private static String resolveIp(HttpServletRequest req) {
    // リバースプロキシ・ロードバランサ対応
    String ip = req.getHeader("X-Forwarded-For");
    if (ip != null && !ip.isEmpty()) {
      // カンマ区切りで複数あることがある → 先頭を採用
      int comma = ip.indexOf(',');
      if (comma > 0) ip = ip.substring(0, comma).trim();
      return ip;
    }
    return req.getRemoteAddr();
  }

  /* ========================
       管理者ログ
     ======================== */
  public static void logAdmin(
      HttpServletRequest req,
      Organization org,
      Administrator admin,
      String action,
      String entity,
      String entityId
  ) {
    if (req == null || org == null || admin == null) return;
    if (action == null || action.isEmpty()) return;
    if (entity == null || entity.isEmpty()) return;

    try {
      AuditLog log = new AuditLog();
      log.setOrgId(org.getId());
      log.setActorType("admin");
      log.setActorId(admin.getId());
      log.setAction(action);
      log.setEntity(entity);
      log.setEntityId(entityId == null ? "(none)" : entityId);
      log.setIp(resolveIp(req));

      dao.insert(log);

    } catch (Exception e) {
      // ログ記録失敗は本編の処理を止めない
      e.printStackTrace();
    }
  }

  /* ========================
       保護者ログ
     ======================== */
  public static void logGuardian(
      HttpServletRequest req,
      Organization org,
      User user,
      String action,
      String entity,
      String entityId
  ) {
    if (req == null || org == null || user == null) return;
    if (action == null || action.isEmpty()) return;
    if (entity == null || entity.isEmpty()) return;

    try {
      AuditLog log = new AuditLog();
      log.setOrgId(org.getId());
      log.setActorType("guardian");
      log.setActorId(user.getId());
      log.setAction(action);
      log.setEntity(entity);
      log.setEntityId(entityId == null ? "(none)" : entityId);
      log.setIp(resolveIp(req));

      dao.insert(log);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /* ========================
       セッション版（管理者）
     ======================== */
  public static void logAdminFromSession(
      HttpServletRequest req,
      String action,
      String entity,
      String entityId
  ) {
    HttpSession ses = req.getSession(false);
    if (ses == null) return;

    Organization org = (Organization) ses.getAttribute("org");
    Administrator admin = (Administrator) ses.getAttribute("admin");

    logAdmin(req, org, admin, action, entity, entityId);
  }

  /* ========================
       セッション版（保護者）
     ======================== */
  public static void logGuardianFromSession(
      HttpServletRequest req,
      String action,
      String entity,
      String entityId
  ) {
    HttpSession ses = req.getSession(false);
    if (ses == null) return;

    Organization org = (Organization) ses.getAttribute("org");
    User user = (User) ses.getAttribute("user");

    logGuardian(req, org, user, action, entity, entityId);
  }
}
