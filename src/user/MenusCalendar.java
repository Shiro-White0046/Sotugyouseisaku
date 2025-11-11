package user;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collection;
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
import bean.MenuMeal;
import bean.User;
import dao.IndividualAllergyDAO;
import dao.IndividualDAO;
import dao.MenuDayDAO;
import dao.MenuItemAllergenDAO;
import dao.MenuItemDAO;
import dao.MenuMealDAO;

@WebServlet("/user/menuscalendar")
public class MenusCalendar extends HttpServlet {
  private static final long serialVersionUID = 1L;

  private static final String FALLBACK_LOGIN_URL = "/auth/login";

  private final MenuDayDAO dayDao = new MenuDayDAO();
  private final MenuMealDAO mealDao = new MenuMealDAO();
  private final MenuItemDAO itemDao = new MenuItemDAO();
  private final MenuItemAllergenDAO itemAllergenDao = new MenuItemAllergenDAO();
  private final IndividualDAO individualDao = new IndividualDAO();
  private final IndividualAllergyDAO individualAllergyDao = new IndividualAllergyDAO();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    // ---- 認証 & ユーザ/組織取得 ----
    HttpSession session = req.getSession(false);
    User loginUser = (session == null) ? null : (User) firstNonNull(
        session.getAttribute("loginUser"),
        session.getAttribute("user"),
        session.getAttribute("account"));
    if (loginUser == null) { resp.sendRedirect(req.getContextPath() + FALLBACK_LOGIN_URL); return; }
    UUID orgId = loginUser.getOrgId();

    // ---- 個人（児童）特定：User → IndividualDAO.findOneByUser ----
    bean.Individual person = individualDao.findOneByUser(loginUser);
    if (person == null) {
      // 個人未登録なら何も表示しない月画面へ
      setEmptyMonth(req);
      req.getRequestDispatcher("/user/menus_calendar.jsp").forward(req, resp);
      return;
    }
    UUID individualId = person.getId();

    // 利用者アレルゲンID集合（1回だけ取得）
    Set<Short> userAllergenIds = individualAllergyDao.listByPerson(individualId)
        .stream().map(bean.IndividualAllergy::getAllergenId)
        .collect(Collectors.toSet());

    // ---- 月決定 ----
    YearMonth ym;
    try {
      String p = req.getParameter("ym");
      ym = (p == null || p.isEmpty()) ? YearMonth.now() : YearMonth.parse(p);
    } catch (Exception e) { ym = YearMonth.now(); }

    LocalDate first = ym.atDay(1);
    int firstDow = first.getDayOfWeek().getValue() % 7; // 0=日 … 6=土
    int days = ym.lengthOfMonth();

    // ---- 当月の menu_days ----
    List<MenuDay> daysList = dayDao.listMonth(orgId, ym);

    // 日付 -> 利用者に該当するアレルゲン名だけ（交差）
    Map<LocalDate, List<String>> labelsMap = new HashMap<>();

    for (MenuDay day : daysList) {
    	Collection<MenuMeal> meals = mealDao.findByDayAsMap(day.getId()).values();

      Set<String> names = new LinkedHashSet<>();
      for (MenuMeal meal : meals) {
        List<MenuItem> items = itemDao.listByMeal(meal.getId());
        for (MenuItem it : items) {
          for (Allergen a : itemAllergenDao.listByItem(it.getId())) {
            if (userAllergenIds.contains(a.getId())) {
              String name = a.getNameJa();
              if (name != null && !name.isEmpty()) names.add(name);
            }
          }
        }
      }
      if (!names.isEmpty()) {
        labelsMap.put(day.getMenuDate(), new ArrayList<>(names));
      }
    }

    // ---- JSP へ ----
    req.setAttribute("year", ym.getYear());
    req.setAttribute("month", ym.getMonthValue());
    req.setAttribute("firstDow", firstDow);
    req.setAttribute("daysInMonth", days);
    req.setAttribute("labelsByDate",
        labelsMap.entrySet().stream()
            .collect(Collectors.toMap(e -> e.getKey().toString(), Map.Entry::getValue)));
    req.setAttribute("prevYm", ym.minusMonths(1).toString());
    req.setAttribute("nextYm", ym.plusMonths(1).toString());

    req.getRequestDispatcher("/user/menus_calendar.jsp").forward(req, resp);
  }

  private static Object firstNonNull(Object... arr) {
    for (Object o : arr) if (o != null) return o;
    return null;
  }

  private static void setEmptyMonth(HttpServletRequest req) {
    java.time.YearMonth now = java.time.YearMonth.now();
    req.setAttribute("year", now.getYear());
    req.setAttribute("month", now.getMonthValue());
    req.setAttribute("firstDow", now.atDay(1).getDayOfWeek().getValue() % 7);
    req.setAttribute("daysInMonth", now.lengthOfMonth());
    req.setAttribute("labelsByDate", java.util.Collections.emptyMap());
    req.setAttribute("prevYm", now.minusMonths(1).toString());
    req.setAttribute("nextYm", now.plusMonths(1).toString());
  }
}
