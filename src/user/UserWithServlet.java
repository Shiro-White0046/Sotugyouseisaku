package user;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.mindrot.jbcrypt.BCrypt;

import bean.User;
import dao.UserDAO;
import infra.AuditLogger;   // ★ 追加

@WebServlet("/user/withdraw")
public class UserWithServlet extends HttpServlet {

  private final UserDAO userDAO = new UserDAO();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    HttpSession ses = req.getSession(false);
    User user = (ses != null) ? (User) ses.getAttribute("user") : null;
    if (user == null) {
      resp.sendRedirect(req.getContextPath() + "/auth/login");
      return;
    }

    req.getRequestDispatcher("/user/user_with.jsp")
       .forward(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    req.setCharacterEncoding("UTF-8");

    HttpSession ses = req.getSession(false);
    User user = (ses != null) ? (User) ses.getAttribute("user") : null;
    if (user == null) {
      resp.sendRedirect(req.getContextPath() + "/auth/login");
      return;
    }

    String password = req.getParameter("password");
    if (password == null || password.isEmpty()) {
      req.setAttribute("error", "パスワードを入力してください。");
      req.getRequestDispatcher("/user/user_with.jsp")
         .forward(req, resp);
      return;
    }

    String hash = user.getPasswordHash();

    boolean ok = BCrypt.checkpw(password, hash);
    if (!ok) {
      req.setAttribute("error", "パスワードが違います。");
      req.getRequestDispatcher("/user/user_with.jsp")
         .forward(req, resp);
      return;
    }

    // ▼ 退会処理
    UUID userId = user.getId();
    userDAO.withdrawUser(userId);

    // ★ 操作ログ：退会
    AuditLogger.logGuardianFromSession(
        req,
        "withdraw_account",
        "users",
        userId.toString()
    );

    // セッション破棄
    ses.invalidate();

    // 完了メッセージ
    HttpSession newSes = req.getSession(true);
    newSes.setAttribute("flashMessage", "退会が完了しました。ご利用ありがとうございました。");

    resp.sendRedirect(req.getContextPath() + "/");
  }
}
