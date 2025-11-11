package user;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import bean.Allergen;
import bean.MenuDay;
import bean.MenuItem;
import bean.User; // あなたのUserビーン（getOrgId()がある想定）
import dao.MenuDayDAO;
import dao.MenuItemAllergenDAO;
import dao.MenuItemDAO;
import dao.MenuMealDAO;

@WebServlet("/user/menuscalendar")
public class MenusCalendar extends HttpServlet {
  private static final long serialVersionUID = 1L;

  private static final String FALLBACK_LOGIN_URL = "/auth/login"; // 実URLに合わせて
  private final MenuDayDAO dayDao = new MenuDayDAO();
  private final MenuMealDAO mealDao = new MenuMealDAO();
  private final MenuItemDAO itemDao = new MenuItemDAO();
  private final MenuItemAllergenDAO itemAllergenDao = new MenuItemAllergenDAO();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    // ---- 認証/組織IDの取得 ----
    HttpSession session = req.getSession(false);
    Object loginAttr = (session == null) ? null
        : firstNonNull(session.getAttribute("loginUser"),
                       session.getAttribute("user"),
                       session.getAttribute("account"));
    if (loginAttr == null) {
      resp.sendRedirect(req.getContextPath() + FALLBACK_LOGIN_URL);
      return;
    }
    UUID orgId = resolveOrgId(session, loginAttr);
    if (orgId == null) {
      resp.sendRedirect(req.getContextPath() + FALLBACK_LOGIN_URL);
      return;
    }

    // ---- 月の決定 ----
    YearMonth ym;
    try {
      String p = req.getParameter("ym");
      ym = (p == null || p.isEmpty()) ? YearMonth.now() : YearMonth.parse(p);
    } catch (Exception e) { ym = YearMonth.now(); }

    LocalDate first = ym.atDay(1);
    int firstDow = first.getDayOfWeek().getValue() % 7; // 0=日 … 6=土
    int days = ym.lengthOfMonth();

    // ---- 当月の menu_days を取得（published/非公開の扱いは要件に合わせて。ここでは全件扱い）----
    List<MenuDay> daysList = dayDao.listMonth(orgId, ym);

    // 日付→その日に含まれる主要アレルゲン名リスト（重複排除）を構築
    Map<LocalDate, List<String>> labelsMap = new HashMap<>();

    for (MenuDay day : daysList) {
      // その日の全食事
      List<bean.MenuMeal> meals = mealDao.listByDay(day.getId());

      // その日の全アイテムのアレルゲン名を収集
      Set<String> names = new LinkedHashSet<>();
      for (bean.MenuMeal meal : meals) {
        List<MenuItem> items = itemDao.listByMeal(meal.getId());
        for (MenuItem it : items) {
          List<Allergen> as = itemAllergenDao.listByItem(it.getId());
          for (Allergen a : as) {
            if (a.getNameJa() != null && !a.getNameJa().isEmpty()) {
              names.add(a.getNameJa());
            }
          }
        }
      }
      if (!names.isEmpty()) {
        labelsMap.put(day.getMenuDate(), new ArrayList<>(names));
      }
    }

    // ---- JSPへ ----
    req.setAttribute("year", ym.getYear());
    req.setAttribute("month", ym.getMonthValue());
    req.setAttribute("firstDow", firstDow);
    req.setAttribute("daysInMonth", days);

    Map<String, List<String>> labelsByDate = labelsMap.entrySet().stream()
        .collect(Collectors.toMap(e -> e.getKey().toString(), Map.Entry::getValue));
    req.setAttribute("labelsByDate", labelsByDate); // ④：ある日だけ値がある

    req.setAttribute("prevYm", ym.minusMonths(1).toString());
    req.setAttribute("nextYm", ym.plusMonths(1).toString());

    req.getRequestDispatcher("/user/menus_calendar.jsp").forward(req, resp);
  }

  // ===== helpers =====
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
    if (loginAttr instanceof User) return ((User) loginAttr).getOrgId();
    try {
      java.lang.reflect.Method m = loginAttr.getClass().getMethod("getOrgId");
      Object v = m.invoke(loginAttr);
      if (v instanceof UUID) return (UUID) v;
      if (v instanceof String) return UUID.fromString((String) v);
    } catch (Exception ignore) {}
    return null;
  }
}
