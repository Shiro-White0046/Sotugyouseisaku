package user;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import bean.Organization;      // ★ 追加
import bean.User;
import infra.AuditLogger;     // ★ 追加

@WebServlet(urlPatterns = {"/user/logout"})
public class UserLogoutServlet extends HttpServlet {
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    HttpSession ses = req.getSession(false);

    // ★ ログアウト時にログを残す（user が存在している場合のみ）
    if (ses != null) {
      User user = (User) ses.getAttribute("user");
      Organization org = (Organization) ses.getAttribute("org");

      if (user != null && org != null) {
        AuditLogger.logGuardian(
            req,
            org,
            user,
            "logout",
            "users",
            user.getId().toString()
        );
      }

      // セッション破棄
      ses.invalidate();
    }

    resp.sendRedirect(req.getContextPath() + "/user");
  }
}
