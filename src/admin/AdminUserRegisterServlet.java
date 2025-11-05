package admin;

import java.io.IOException;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import bean.Administrator;
import bean.User;
import dao.UserDAO;
import infra.Password;

@WebServlet(urlPatterns = {"/admin/users/register"})
public class AdminUserRegisterServlet extends HttpServlet {

  private static final Pattern PW_PATTERN =
      Pattern.compile("^(?=\\S+$)(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d]{8,16}$");

  // ✅ これを追加（GETアクセス用）
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    req.getRequestDispatcher("/admin/user_register.jsp").forward(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    req.setCharacterEncoding("UTF-8");
    String name = req.getParameter("userName");
    String tmpPw = req.getParameter("tempPassword");
    String type  = req.getParameter("accountType");

    HttpSession ses = req.getSession(false);
    Administrator admin = (ses != null) ? (Administrator) ses.getAttribute("admin") : null;
    if (admin == null) {
      resp.sendRedirect(req.getContextPath() + "/admin/login");
      return;
    }

    // --- 入力バリデーション ---
    if (name == null || name.trim().isEmpty()) {
      ses.setAttribute("error", "利用者名を入力してください。");
      resp.sendRedirect(req.getContextPath() + "/admin/users/register");
      return;
    }
    if (tmpPw == null || !PW_PATTERN.matcher(tmpPw).matches()) {
      ses.setAttribute("error", "仮パスワードが条件を満たしていません。");
      resp.sendRedirect(req.getContextPath() + "/admin/users/register");
      return;
    }
    if (!("single".equals(type) || "multi".equals(type))) {
      ses.setAttribute("error", "アカウント種別を選択してください。");
      resp.sendRedirect(req.getContextPath() + "/admin/users/register");
      return;
    }

    // --- 成功処理 ---
    try {
      String hash = Password.hash(tmpPw);
      User created = new UserDAO().create(admin.getOrgId(), name.trim(), hash, type);

      ses.setAttribute("createdUser", created);
      ses.setAttribute("tempPwPlain", tmpPw);

      resp.sendRedirect(req.getContextPath() + "/admin/users/register/done");
    } catch (RuntimeException e) {
      ses.setAttribute("error", "アカウント作成中にエラーが発生しました。");
      resp.sendRedirect(req.getContextPath() + "/admin/users/register");
    }
  }
}

