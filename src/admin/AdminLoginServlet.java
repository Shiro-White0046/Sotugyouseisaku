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

import bean.Organization;
import dao.OrganizationDAO;

@WebServlet("/admin/login")
public class AdminLoginServlet extends HttpServlet {
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    RequestDispatcher rd = req.getRequestDispatcher("/admin/login_org.jsp");
    rd.forward(req, resp);
  }



  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    req.setCharacterEncoding("UTF-8");
    String orgCode = req.getParameter("orgCode");
    if (orgCode == null || orgCode.trim().isEmpty()) {
      req.setAttribute("error", "組織コードを入力してください。");
      doGet(req, resp);
      return;
    }
    OrganizationDAO orgDao = new OrganizationDAO();
    Optional<Organization> orgOpt = orgDao.findByCode(orgCode.trim());
    if (!orgOpt.isPresent()) {
      req.setAttribute("error", "組織コードが見つかりません。");
      doGet(req, resp);
      return;
    }
    // セッションに組織を保存
    HttpSession session = req.getSession(true);
    session.setAttribute("org", orgOpt.get());
    // 次のステップへ
    resp.sendRedirect(req.getContextPath() + "/admin/login/cred");
  }
}
