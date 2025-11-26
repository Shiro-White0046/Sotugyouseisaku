// src/admin/AdminUserListAllergyServlet.java
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
import bean.Allergen;
import dao.AllergenDAO;


@WebServlet("/admin/allergens-master")
public class AdminAllergenServlet extends HttpServlet {

  private final AllergenDAO allergenDAO = new AllergenDAO();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    HttpSession ses = req.getSession(false);
    Administrator admin = (ses != null) ? (Administrator) ses.getAttribute("admin") : null;

    if (admin == null) {
      resp.sendRedirect(req.getContextPath() + "/admin/login");
      return;
    }

    String q = trim(req.getParameter("q"));

    List<Allergen> list = allergenDAO.search(admin.getOrgId(), q);

    req.setAttribute("q", q);
    req.setAttribute("allergens", list);

    req.getRequestDispatcher("/admin/admin_allergens.jsp").forward(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    HttpSession ses = req.getSession(false);
    Administrator admin = (ses != null) ? (Administrator) ses.getAttribute("admin") : null;

    if (admin == null) {
      resp.sendRedirect(req.getContextPath() + "/admin/login");
      return;
    }

    String name = trim(req.getParameter("name"));
    String cat  = trim(req.getParameter("category"));
    String sub  = trim(req.getParameter("subCategory"));

    allergenDAO.insert(admin.getOrgId(), name, cat, sub);

    resp.sendRedirect(req.getContextPath() + "/admin/allergens-master");
  }

  private static String trim(String s) {
    return (s == null) ? "" : s.trim();
  }
}
