// src/user/MenusCalendar.java
package user;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import bean.MenuDay;
import bean.MenuItem;
import bean.MenuMeal;
import bean.User;
import bean.Allergen;
import bean.Individual;
import dao.MenuDayDAO;
import dao.MenuMealDAO;
import dao.MenuItemDAO;
import dao.MenuItemAllergenDAO;
import dao.IndividualDAO;
import dao.IndividualAllergyDAO;

@WebServlet("/user/menuscalendar")
public class MenusCalendar extends HttpServlet {
  private static final long serialVersionUID = 1L;

  private static final String FALLBACK_LOGIN_URL    = "/auth/login";
  private static final String FALLBACK_CALENDAR_URL = "/user/menuscalendar";

  private final MenuDayDAO dayDao                 = new MenuDayDAO();
  private final MenuMealDAO mealDao               = new MenuMealDAO();
  private final MenuItemDAO itemDao               = new MenuItemDAO();
  private final MenuItemAllergenDAO itemAlgDao    = new MenuItemAllergenDAO();
  private final IndividualDAO individualDao       = new IndividualDAO();
  private final IndividualAllergyDAO indAlgDao    = new IndividualAllergyDAO();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    // ---- 認証 ----
    HttpSession ses = req.getSession(false);
    User loginUser = (ses == null) ? null :
        (User) firstNonNull(ses.getAttribute("loginUser"),
                            ses.getAttribute("user"),
                            ses.getAttribute("account"));
    if (loginUser == null) {
      resp.sendRedirect(req.getContextPath() + FALLBACK_LOGIN_URL);
      return;
    }
    UUID orgId = loginUser.getOrgId();

    // ---- 保護者配下の子ども一覧 ----
    List<Individual> children = individualDao.listByUser(loginUser.getId());
    if (children == null || children.isEmpty()) {
      // 子ども未登録 → そのままカレンダーだけ表示（アレルギーは出ない）
      children = Collections.emptyList();
    }
    req.setAttribute("children", children);

    // ---- personId 決定（パラメータが自分の配下に属しているか検証）----
    UUID personId = parseUUID(req.getParameter("personId"));
    Individual selectedChild = null;
    if (personId != null) {
      for (Individual c : children) {
        if (c.getId().equals(personId)) { selectedChild = c; break; }
      }
    }
    if (selectedChild == null && !children.isEmpty()) {
      selectedChild = children.get(0);
      personId = selectedChild.getId();
    }
    req.setAttribute("personId", personId);
    req.setAttribute("selectedChild", selectedChild);

    // ---- 年月（ym=YYYY-MM） ----
    YearMonth ym = parseYm(defaultIfBlank(req.getParameter("ym"),
                          YearMonth.now().toString()));
    int year  = ym.getYear();
    int month = ym.getMonthValue();
    LocalDate first = ym.atDay(1);
    int firstDow = first.getDayOfWeek().getValue() % 7; // 日=0 … 土=6
    int daysInMonth = ym.lengthOfMonth();

    req.setAttribute("year", year);
    req.setAttribute("month", month);
    req.setAttribute("firstDow", firstDow);
    req.setAttribute("daysInMonth", daysInMonth);
    req.setAttribute("prevYm", ym.minusMonths(1).toString());
    req.setAttribute("nextYm", ym.plusMonths(1).toString());

    // ---- その子どものアレルゲンIDセット ----
    Set<Short> userAllergenIds = Collections.emptySet();
    if (selectedChild != null) {
      userAllergenIds = indAlgDao.findAllergenIds(selectedChild.getId());
    }

    // ---- 1日ずつ調べて、当日の“子ども一致アレルゲン名”リストを作る ----
    Map<String, List<String>> labelsByDate = new HashMap<>();
    Map<String, Boolean> hasMenuMap = new HashMap<>();

    for (int day = 1; day <= daysInMonth; day++) {
      LocalDate d = ym.atDay(day);
      String key = d.toString();

      Optional<MenuDay> optDay = dayDao.findByDate(orgId, d);
      if (!optDay.isPresent()) {
        hasMenuMap.put(key, Boolean.FALSE);
        continue;
      }
      hasMenuMap.put(key, Boolean.TRUE);

      // 当日の全アレルゲンを集約
      Set<Short> allAlgIds = new LinkedHashSet<>();
      for (MenuMeal meal : mealDao.listByDay(optDay.get().getId())) {
        for (MenuItem it : itemDao.listByMeal(meal.getId())) {
          for (Allergen a : itemAlgDao.listByItem(it.getId())) {
            allAlgIds.add(a.getId());
          }
        }
      }

      // 子どもに一致するアレルゲン名だけ抽出
      if (!allAlgIds.isEmpty() && !userAllergenIds.isEmpty()) {
        List<Short> matched = new ArrayList<>();
        for (Short id : allAlgIds) {
          if (userAllergenIds.contains(id)) matched.add(id);
        }
        if (!matched.isEmpty()) {
          // 表示順はID順でOK（必要なら AllergenDAO.findByIdsPreserveOrder を使ってもよい）
          List<Allergen> list = itemAlgDao.findAllergensByIds(matched); // なければ AllergenDAO を使う実装でもOK
          List<String> names = new ArrayList<>();
          for (Allergen a : list) names.add(a.getNameJa());
          labelsByDate.put(key, names);
        }
      }
    }

    req.setAttribute("labelsByDate", labelsByDate);
    req.setAttribute("hasMenuMap", hasMenuMap);

    // 画面へ
    req.getRequestDispatcher("/user/menus_calendar.jsp").forward(req, resp);
  }

  // ===== helper =====
  private static Object firstNonNull(Object... arr) {
    for (Object o : arr) if (o != null) return o;
    return null;
  }
  private static String defaultIfBlank(String s, String def) {
    return (s == null || s.trim().isEmpty()) ? def : s.trim();
  }
  private static UUID parseUUID(String s) {
    if (s == null || s.isEmpty()) return null;
    try { return UUID.fromString(s); } catch (Exception e) { return null; }
  }
  private static YearMonth parseYm(String s) {
    try { return YearMonth.parse(s); } catch (Exception e) { return YearMonth.now(); }
  }
}
