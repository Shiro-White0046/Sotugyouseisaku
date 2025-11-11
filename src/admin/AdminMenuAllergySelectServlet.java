package admin;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import bean.Administrator;
import bean.Organization;
import dao.AllergenDAO;

@WebServlet("/admin/menus_new/allergy")
public class AdminMenuAllergySelectServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;
  private final AllergenDAO algDao = new AllergenDAO();

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

    // 必須パラメータ
    String dayId = req.getParameter("dayId");
    String slot  = req.getParameter("slot");   // BREAKFAST / LUNCH / DINNER
    String idx   = req.getParameter("idx");    // 品目の行インデックス
    String name  = req.getParameter("name");   // 品名（タイトル表示用）

    if (dayId == null || slot == null || idx == null) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "dayId/slot/idx は必須です");
      return;
    }

    // FOODのみ
    req.setAttribute("allergens", algDao.listByCategory("FOOD"));
    req.setAttribute("dayId", dayId);
    req.setAttribute("slot",  slot);
    req.setAttribute("idx",   idx);
    req.setAttribute("itemName", (name == null) ? "" : name);

    req.getRequestDispatcher("/admin/menus_allergy_pick.jsp").forward(req, resp);
  }
}
