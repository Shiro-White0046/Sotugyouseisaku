package admin;

import java.io.IOException;
import java.util.Optional;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import bean.Administrator;
import bean.Organization;
import dao.AdminDAO;
import infra.Password;

@WebServlet("/admin/login/cred")
public class AdminLoginCredServlet extends HttpServlet {
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    // 組織が未設定なら戻す
    Organization org = (Organization) req.getSession().getAttribute("org");
    if (org == null) {
      resp.sendRedirect(req.getContextPath() + "/admin/login");
      return;
    }
    RequestDispatcher rd = req.getRequestDispatcher("/admin/login_cred.jsp");
    rd.forward(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    req.setCharacterEncoding("UTF-8");
    Organization org = (Organization) req.getSession().getAttribute("org");
    if (org == null) {
      resp.sendRedirect(req.getContextPath() + "/admin/login");
      return;
    }

    String adminNo = req.getParameter("adminNo");
    String password = req.getParameter("password");

    if (adminNo == null || password == null || adminNo.trim().isEmpty() || password.isEmpty()) {
      req.setAttribute("error", "個人番号とパスワードを入力してください。");
      doGet(req, resp);
      return;
    }
    adminNo = adminNo.trim();

    // 8桁チェック（任意）
    if (!adminNo.matches("\\d{8}")) {
      req.setAttribute("error", "個人番号は8桁の数字で入力してください。");
      doGet(req, resp);
      return;
    }

    AdminDAO adminDao = new AdminDAO();
    Optional<Administrator> adminOpt = adminDao.findByOrgCodeAndAdminNo(org.getCode(), adminNo);
    if (!adminOpt.isPresent()) {
      req.setAttribute("error", "個人番号またはパスワードが不正です。");
      doGet(req, resp);
      return;
    }

    Administrator admin = adminOpt.get();
    if (!Password.check(password, admin.getPasswordHash())) {
      req.setAttribute("error", "個人番号またはパスワードが不正です。");
      doGet(req, resp);
      return;
    }

    // 認証成功：セッションに保存して管理TOPへ
    HttpSession session = req.getSession();
    session.setAttribute("admin", admin);

    resp.sendRedirect(req.getContextPath() + "/admin/home");

  }
}
