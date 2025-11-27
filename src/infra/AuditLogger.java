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

  public static void logAdmin(HttpServletRequest req,
                              Organization org,
                              Administrator admin,
                              String action,
                              String entity,
                              String entityId) {
    if (org == null || admin == null) return;

    AuditLog log = new AuditLog();
    log.setOrgId(org.getId());
    log.setActorType("admin");              // ★ DB制約に合わせる
    log.setActorId(admin.getId());
    log.setAction(action);
    log.setEntity(entity);
    log.setEntityId(entityId);
    log.setIp(req.getRemoteAddr());

    dao.insert(log);
  }

  public static void logGuardian(HttpServletRequest req,
                                 Organization org,
                                 User user,
                                 String action,
                                 String entity,
                                 String entityId) {
    if (user == null) return;

    // ★ org が null の場合は user.getOrgId() から補完する
    java.util.UUID orgId = null;
    if (org != null) {
      orgId = org.getId();
    } else {
      orgId = user.getOrgId();
    }
    if (orgId == null) return;

    AuditLog log = new AuditLog();
    log.setOrgId(orgId);
    log.setActorType("guardian");
    log.setActorId(user.getId());
    log.setAction(action);
    log.setEntity(entity);
    log.setEntityId(entityId);
    log.setIp(req.getRemoteAddr());

    dao.insert(log);
  }

  public static void logAdminFromSession(HttpServletRequest req,
                                         String action,
                                         String entity,
                                         String entityId) {
    HttpSession ses = req.getSession(false);
    if (ses == null) return;
    Organization org = (Organization) ses.getAttribute("org");
    Administrator admin = (Administrator) ses.getAttribute("admin");
    logAdmin(req, org, admin, action, entity, entityId);
  }

  public static void logGuardianFromSession(HttpServletRequest req,
                                            String action,
                                            String entity,
                                            String entityId) {
    HttpSession ses = req.getSession(false);
    if (ses == null) return;
    Organization org = (Organization) ses.getAttribute("org");
    User user = (User) ses.getAttribute("user");
    logGuardian(req, org, user, action, entity, entityId);
  }
}
