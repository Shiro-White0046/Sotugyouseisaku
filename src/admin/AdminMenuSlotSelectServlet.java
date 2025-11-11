package admin;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import bean.Administrator;
import bean.MenuDay;
import bean.MenuMeal;
import bean.Organization;
import dao.MenuDayDAO;
import dao.MenuMealDAO;

@WebServlet("/admin/menus_new/select")
public class AdminMenuSlotSelectServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  private final MenuDayDAO dayDao  = new MenuDayDAO();
  private final MenuMealDAO mealDao = new MenuMealDAO();

  private static String trim(String s){ if (s==null) return null; String t=s.trim(); return t.isEmpty()?null:t; }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    HttpSession ses = req.getSession(false);
    Administrator admin = ses==null? null: (Administrator) ses.getAttribute("admin");
    Organization  org   = ses==null? null: (Organization)  ses.getAttribute("org");
    if (admin==null || org==null) { resp.sendRedirect(req.getContextPath()+"/admin/login"); return; }

    String dayIdStr = trim(req.getParameter("dayId"));
    String dateStr  = trim(req.getParameter("date"));

    UUID dayId;
    try {
      if (dayIdStr != null) {
        dayId = UUID.fromString(dayIdStr);
      } else if (dateStr != null) {
        java.time.LocalDate date = java.time.LocalDate.parse(dateStr);
        dayId = dayDao.ensureDay(org.getId(), date); // 無ければ作成
      } else {
        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "dayId または date は必須です");
        return;
      }
    } catch (Exception e) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "dayId/date の形式が不正です");
      return;
    }

    Optional<MenuDay> dayOpt = dayDao.find(dayId);
    if (!dayOpt.isPresent()) { resp.sendError(HttpServletResponse.SC_NOT_FOUND, "指定日の献立が見つかりません"); return; }

    MenuDay day = dayOpt.get();
    Map<String, MenuMeal> mealBySlot = mealDao.findByDayAsMap(day.getId());

    req.setAttribute("menuDay", day);
    req.setAttribute("meals", mealBySlot);
    req.getRequestDispatcher("/admin/menus_slot_select.jsp").forward(req, resp);
  }
}
