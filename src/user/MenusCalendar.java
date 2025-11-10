package user;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import bean.MenuDay;
import bean.User;
import dao.MenuDayDAO;
import dao.MenuItemAllergenDAO;
import dao.MenuItemDAO;
import dao.MenuMealDAO;

/**
 * 利用者用：カレンダー形式で献立を月単位表示する
 * /user/menuscalendar?ym=YYYY-MM
 */
@WebServlet("/user/menuscalendar")
public class MenusCalendar extends HttpServlet {
  private static final long serialVersionUID = 1L;

  private static final String FALLBACK_LOGIN_URL = "/user/login";
  private static final String FALLBACK_HOME_URL  = "/user/home";

  private final MenuDayDAO dayDao = new MenuDayDAO();
  private final MenuMealDAO mealDao = new MenuMealDAO();
  private final MenuItemDAO itemDao = new MenuItemDAO();
  private final MenuItemAllergenDAO itemAllergenDao = new MenuItemAllergenDAO();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    HttpSession session = req.getSession(false);
    Object loginAttr = (session == null) ? null : firstNonNull(
        session.getAttribute("loginUser"),
        session.getAttribute("user"),
        session.getAttribute("account")
    );

    if (loginAttr == null) {
      resp.sendRedirect(req.getContextPath() + FALLBACK_LOGIN_URL);
      return;
    }

    UUID orgId = resolveOrgId(session, loginAttr);
    if (orgId == null) {
      resp.sendRedirect(req.getContextPath() + FALLBACK_HOME_URL);
      return;
    }

    // 対象年月を取得（例：2025-11）
    YearMonth ym;
    try {
      String p = req.getParameter("ym");
      ym = (p == null || p.isEmpty()) ? YearMonth.now() : YearMonth.parse(p);
    } catch (Exception e) { ym = YearMonth.now(); }

    LocalDate first = ym.atDay(1);
    int firstDow = first.getDayOfWeek().getValue() % 7; // 0=日
    int days = ym.lengthOfMonth();

    // --- 月内の全日献立を取得 ---
    List<MenuDay> daysList = dayDao.listMonth(orgId, ym);

    // --- 各日付に登録があるかマーク用 Map を作る ---
    Map<String, Boolean> hasMenuMap = new HashMap<>();
    for (MenuDay d : daysList) {
      hasMenuMap.put(d.getMenuDate().toString(), true);
    }

    // --- JSP へ渡す ---
    req.setAttribute("year", ym.getYear());
    req.setAttribute("month", ym.getMonthValue());
    req.setAttribute("firstDow", firstDow);
    req.setAttribute("daysInMonth", days);
    req.setAttribute("hasMenuMap", hasMenuMap);
    req.setAttribute("prevYm", ym.minusMonths(1).toString());
    req.setAttribute("nextYm", ym.plusMonths(1).toString());

    // カレンダー画面へフォワード
    req.getRequestDispatcher("/user/menu_calendar.jsp").forward(req, resp);
  }

  // ===== helper =====

  private static Object firstNonNull(Object... arr) {
    for (Object o : arr) if (o != null) return o;
    return null;
  }

  private static UUID resolveOrgId(HttpSession session, Object loginAttr) {
    Object orgIdObj = (session != null) ? session.getAttribute("orgId") : null;
    if (orgIdObj instanceof UUID) return (UUID) orgIdObj;
    if (orgIdObj instanceof String) {
      try { return UUID.fromString((String) orgIdObj); } catch (Exception ignore) {}
    }
    if (loginAttr instanceof User) {
      return ((User) loginAttr).getOrgId();
    }
    try {
      java.lang.reflect.Method m = loginAttr.getClass().getMethod("getOrgId");
      Object v = m.invoke(loginAttr);
      if (v instanceof UUID) return (UUID) v;
      if (v instanceof String) return UUID.fromString((String) v);
    } catch (Exception ignore) {}
    return null;
  }
}
