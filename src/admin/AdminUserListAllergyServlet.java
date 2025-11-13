package admin;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import bean.Administrator;
import dao.IndividualAllergyDAO;

@WebServlet("/admin/adminallergies")
public class AdminUserListAllergyServlet extends HttpServlet {
  private final IndividualAllergyDAO iaDao = new IndividualAllergyDAO();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    // 管理者ログイン想定（orgId 取得）
    HttpSession ses = req.getSession(false);
    Administrator admin = (ses != null) ? (Administrator) ses.getAttribute("admin") : null;
    if (admin == null) {
      resp.sendRedirect(req.getContextPath() + "/admin/login");
      return;
    }
    UUID orgId = admin.getOrgId();

    // クエリ取得：q=名前, aq=アレルギー名
    String q  = req.getParameter("q");
    String aq = req.getParameter("aq");

    List<IndividualAllergyDAO.AllergyView> rows =
        iaDao.aggregateByCategoryWithFilters(orgId, q, aq);

    req.setAttribute("rows", rows);
    req.setAttribute("q",  q  == null ? "" : q);
    req.setAttribute("aq", aq == null ? "" : aq);

    req.getRequestDispatcher("/admin/admin_user_list_allergy.jsp")
       .forward(req, resp);
  }
}