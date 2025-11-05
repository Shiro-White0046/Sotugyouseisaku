package user;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/user/login")
public class UserLoginOrgServlet extends HttpServlet {
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    req.getRequestDispatcher("/user/login_org.jsp").forward(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    req.setCharacterEncoding("UTF-8");
    String orgCode = req.getParameter("orgCode");
    if (orgCode == null || orgCode.trim().isEmpty()) {
      req.setAttribute("error", "組織コードを入力してください。");
      doGet(req, resp);
      return;
    }

    // 組織コードを確認してセッションへ
    java.util.Optional<bean.Organization> opt =
        new dao.OrganizationDAO().findByCode(orgCode.trim());
    if (!opt.isPresent()) {
      req.setAttribute("error", "組織コードが見つかりません。");
      doGet(req, resp);
      return;
    }
    HttpSession ses = req.getSession(true);
    ses.setAttribute("org", opt.get());

    // 2画面目へ
    resp.sendRedirect(req.getContextPath() + "/user/login/cred");
  }
}
