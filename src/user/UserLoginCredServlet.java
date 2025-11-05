package user;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import bean.User;

@WebServlet("/user/login/cred")
public class UserLoginCredServlet extends HttpServlet {
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    // 直リンク対策：org 未選択なら 1画面目へ戻す
    HttpSession ses = req.getSession(false);
    bean.Organization org = (ses != null) ? (bean.Organization) ses.getAttribute("org") : null;
    if (org == null) { resp.sendRedirect(req.getContextPath() + "/user/login"); return; }

    req.getRequestDispatcher("/user/login_cred.jsp").forward(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    req.setCharacterEncoding("UTF-8");

    HttpSession ses = req.getSession(false);
    bean.Organization org = (ses != null) ? (bean.Organization) ses.getAttribute("org") : null;
    if (org == null) { resp.sendRedirect(req.getContextPath() + "/user/login"); return; }

    String loginId = req.getParameter("loginId");
    String password = req.getParameter("password");

    if (loginId == null || loginId.isEmpty() || password == null || password.isEmpty()) {
      req.setAttribute("error", "アカウントIDとパスワードを入力してください。");
      doGet(req, resp); return;
    }

    java.util.Optional<User> opt =
        new dao.UserDAO().findByOrgCodeAndLoginId(org.getCode(), loginId.trim());
    if (!opt.isPresent() || !infra.Password.check(password, opt.get().getPasswordHash())) {
      req.setAttribute("error", "アカウントIDまたはパスワードが違います。");
      doGet(req, resp); return;
    }

    User u = opt.get();
    if (!u.isActive()) {
      req.setAttribute("error", "このアカウントは無効化されています。");
      doGet(req, resp); return;
    }

    ses.setAttribute("user", u);
    if (u.isMustChangePassword()) {
      resp.sendRedirect(req.getContextPath() + "/user/first-password");
    } else {
      resp.sendRedirect(req.getContextPath() + "/user/home");
    }
  }
}
