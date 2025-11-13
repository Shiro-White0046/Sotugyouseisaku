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
import bean.Individual;
import bean.Organization;
import dao.IndividualDAO;

@WebServlet("/admin/supportmeals")
public class AdminSupportMealsServlet extends HttpServlet {
	private final IndividualDAO iDao = new IndividualDAO();


  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    HttpSession ses = req.getSession(false);
    Administrator admin = (ses != null) ? (Administrator) ses.getAttribute("admin") : null;
    Organization org    = (ses != null) ? (Organization)  ses.getAttribute("org")   : null;
    if (admin == null || org == null) {
      resp.sendRedirect(req.getContextPath() + "/admin/login");
      return;
    }

    UUID org_id=admin.getOrgId();
    List<Individual> individual_list=iDao.listByOrg(org_id);
    req.setAttribute("lindividual_list", individual_list);



    req.getRequestDispatcher("/admin/supportMeals.jsp").forward(req, resp);
  }
}
