package admin;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import bean.Administrator;
import bean.AuditLog;          // ★ 追加
import dao.AccountTokenDAO;
import dao.AdminDAO;
import dao.AuditLogDAO;       // ★ 追加

@WebServlet(urlPatterns = {"/admin/register/verify"})
public class AdminRegisterVerifyServlet extends HttpServlet {
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    req.getRequestDispatcher("/admin/register_verify.jsp").forward(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    req.setCharacterEncoding("UTF-8");
    HttpSession ses = req.getSession(false);
    if (ses == null || ses.getAttribute("registerAdminId") == null) {
      resp.sendRedirect(req.getContextPath() + "/admin/register");
      return;
    }
    UUID adminId = (UUID) ses.getAttribute("registerAdminId");
    String code = req.getParameter("code");

    try {
      AccountTokenDAO tokenDao = new AccountTokenDAO();
      Optional<UUID> okOrg = tokenDao.verifyAndConsume("admin", adminId, code, Instant.now());
      if (!okOrg.isPresent()) {
        req.setAttribute("error", "パスコードが無効、または有効期限切れです。");
        doGet(req, resp);
        return;
      }
      // 有効 → 管理者を有効化
      AdminDAO adminDao = new AdminDAO();
      Administrator a = adminDao.activateAndFetch(adminId);

      // ★ 操作ログ（管理者アカウント認証）
      AuditLog log = new AuditLog();
      log.setOrgId(okOrg.get());
      log.setActorType("admin");
      log.setActorId(a.getId());
      log.setAction("verify_admin");
      log.setEntity("administrators");
      log.setEntityId(a.getId().toString());
      log.setIp(req.getRemoteAddr());
      new AuditLogDAO().insert(log);

      // 完了画面へ admin_no を渡す
      ses.setAttribute("createdAdmin", a);
      resp.sendRedirect(req.getContextPath() + "/admin/register/done");
    } catch (Exception e) {
      req.setAttribute("error", "検証に失敗しました：" + e.getMessage());
      doGet(req, resp);
    }
  }
}
