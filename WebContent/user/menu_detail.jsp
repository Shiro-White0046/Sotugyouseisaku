package user;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import bean.Allergen;
import bean.Menu;
import dao.MenuDAO;

@WebServlet("/user/menus_detail")
public class MenuDetailServlet extends HttpServlet {
  private final MenuDAO menuDao = new MenuDAO();

  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    UUID orgId = (UUID) req.getSession().getAttribute("orgId");
    if (orgId == null) { resp.sendError(HttpServletResponse.SC_FORBIDDEN); return; }

    String dateStr = req.getParameter("date");
    if (dateStr == null) { resp.sendError(HttpServletResponse.SC_BAD_REQUEST); return; }

    LocalDate date;
    try { date = LocalDate.parse(dateStr); } catch (Exception e) { resp.sendError(400); return; }

    Optional<Menu> opt = menuDao.findByDate(orgId, date, true);
    if (!opt.isPresent()) {
      req.setAttribute("date", date);
      req.getRequestDispatcher("/WEB-INF/views/user/menu_detail_empty.jsp").forward(req, resp);
      return;
    }

    Menu menu = opt.get();
    List<Allergen> allergens = menuDao.listAllergens(menu.getId());

    req.setAttribute("menu", menu);
    req.setAttribute("allergens", allergens);
    req.getRequestDispatcher("/WEB-INF/views/user/menu_detail.jsp").forward(req, resp);
  }
}