package admin;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import bean.Administrator;
import bean.Organization;
import infra.AuditLogger;   // ★ 追加

@WebServlet(urlPatterns = {"/admin/logout"})
public class LogoutServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    // ★ ログアウト前にログを書き込む
    HttpSession ses = req.getSession(false);
    if (ses != null) {
      Organization org = (Organization) ses.getAttribute("org");
      Administrator admin = (Administrator) ses.getAttribute("admin");

      if (org != null && admin != null) {
        AuditLogger.logAdmin(
            req,
            org,
            admin,
            "logout",
            "administrators",
            admin.getId().toString()
        );
      }

      // セッション破棄（ログアウト処理）
      ses.invalidate();
    }

    // 戻るボタン対策：キャッシュ無効化
    resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    resp.setHeader("Pragma", "no-cache");
    resp.setDateHeader("Expires", 0);

    // admin配下の index.jsp に戻す
    resp.sendRedirect(req.getContextPath() + "/admin/index.jsp");
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    doGet(req, resp);
  }
}
