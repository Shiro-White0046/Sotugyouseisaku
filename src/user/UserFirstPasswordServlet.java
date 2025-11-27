package user;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import bean.User;
import dao.UserDAO;
import infra.AuditLogger;   // ★ 追加
import infra.Password;

@WebServlet(urlPatterns = {"/user/first-password"})
public class UserFirstPasswordServlet extends HttpServlet {
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    User u = (User) req.getSession().getAttribute("user");
    if (u == null) {
      resp.sendRedirect(req.getContextPath() + "/user/login");
      return;
    }
    if (!u.isMustChangePassword()) {
      resp.sendRedirect(req.getContextPath() + "/user/home");
      return;
    }

    req.getRequestDispatcher("/user/first_password.jsp").forward(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    User u = (User) req.getSession().getAttribute("user");
    if (u == null) {
      resp.sendRedirect(req.getContextPath() + "/user/login");
      return;
    }

    String pw  = req.getParameter("newPassword");
    String pw2 = req.getParameter("confirmPassword");

    if (pw == null || pw2 == null || !pw.equals(pw2)) {
      req.setAttribute("error", "新しいパスワードが一致しません。");
      doGet(req, resp);
      return;
    }

    // 強度チェック
    if (!pw.matches("^(?=\\S+$)(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d]{8,16}$")) {
      req.setAttribute("error", "8〜16文字、英大/小/数字を含み、記号・空白不可。");
      doGet(req, resp);
      return;
    }

    // 更新
    new UserDAO().updatePasswordAndClearFlag(u.getId(), Password.hash(pw));

    // セッション中のフラグも false に
    u.setMustChangePassword(false);

    // ★ 操作ログ：初回パスワード設定完了
    AuditLogger.logGuardianFromSession(
        req,
        "update_password_first",
        "users",
        u.getId().toString()
    );

    resp.sendRedirect(req.getContextPath() + "/user/home");
  }
}
