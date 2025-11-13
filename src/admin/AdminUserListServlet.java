package admin;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import bean.Administrator;
import dao.IndividualDAO;
import dao.IndividualDAO.IndividualRow;

@WebServlet("/admin/users")
public class AdminUserListServlet extends HttpServlet {
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    HttpSession ses = req.getSession(false);
    Administrator admin = (ses != null) ? (Administrator) ses.getAttribute("admin") : null;
    if (admin == null) {
      resp.sendRedirect(req.getContextPath() + "/admin/login");
      return;
    }

    String keyword = req.getParameter("q");  // 名前検索
    IndividualDAO dao = new IndividualDAO();
    List<IndividualRow> rows = dao.findList(admin.getOrgId(), keyword);

    req.setAttribute("rows", rows);
    req.setAttribute("q", keyword);
    req.getRequestDispatcher("/admin/admin_user_list_allergy.jsp").forward(req, resp);
  }
}
