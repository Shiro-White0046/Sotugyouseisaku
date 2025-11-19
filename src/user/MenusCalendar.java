// src/user/MenusCalendar.java
package user;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import bean.Allergen;
import bean.Individual;
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

  private final IndividualDAO individualDao = new IndividualDAO();
  private final IndividualAllergyDAO individualAllergyDao = new IndividualAllergyDAO();
  private final MenuDayDAO dayDao = new MenuDayDAO();
  private final MenuMealDAO mealDao = new MenuMealDAO();
  private final MenuItemDAO itemDao = new MenuItemDAO();
  private final MenuItemAllergenDAO itemAlgDao = new MenuItemAllergenDAO();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    req.setCharacterEncoding("UTF-8");
    HttpSession ses = req.getSession(false);
    User loginUser = (ses == null) ? null : (User) firstNonNull(
        ses.getAttribute("loginUser"),
        ses.getAttribute("user"),
        ses.getAttribute("account")
    );

    if (loginUser == null) {
      resp.sendRedirect(req.getContextPath() + "/auth/login");
      return;
    }

    UUID orgId = loginUser.getOrgId();

    // 子ども一覧
    List<Individual> children = individualDao.listByUser(loginUser.getId());
    if (children == null || children.isEmpty()) {
      resp.sendRedirect(req.getContextPath() + "/user/home");
      return;
    }
    req.setAttribute("children", children);

    // ===== ★ 対象児の決定（共通ロジック） =====
    UUID personId = resolvePersonId(req, ses, children);

    // ★ 決定した対象児をセッションに保存（他画面と共有）
    if (ses != null) {
      ses.setAttribute("currentPersonId", personId);
    }

    req.setAttribute("personId", personId);

    // 選択された子ども
    Individual selectedChild = children.stream()
        .filter(c -> c.getId().equals(personId))
        .findFirst()
        .orElse(children.get(0));
    req.setAttribute("selectedChild", selectedChild);

    // 子どものアレルゲンID一覧
    Set<Short> userAllergenIds = individualAllergyDao.findAllergenIds(personId);

    // ---- カレンダー年月処理 ----
    String ymParam = req.getParameter("ym");
    YearMonth ym;
    try {
      ym = (ymParam == null || ymParam.isEmpty())
          ? YearMonth.now()
          : YearMonth.parse(ymParam);
    } catch (Exception e) {
      ym = YearMonth.now();
    }

    int year = ym.getYear();
    int month = ym.getMonthValue();
    int firstDow = ym.atDay(1).getDayOfWeek().getValue(); // Mon=1..Sun=7
    firstDow = (firstDow == 7) ? 0 : firstDow; // Sun=0
    int daysInMonth = ym.lengthOfMonth();

    req.setAttribute("year", year);
    req.setAttribute("month", month);
    req.setAttribute("firstDow", firstDow);
    req.setAttribute("daysInMonth", daysInMonth);
    req.setAttribute("prevYm", ym.minusMonths(1).toString());
    req.setAttribute("nextYm", ym.plusMonths(1).toString());

    // ---- 献立とアレルゲン情報 ----
    Map<String, Boolean> hasMenuMap = new LinkedHashMap<>();
    Map<String, List<String>> labelsByDate = new LinkedHashMap<>();

    for (int day = 1; day <= daysInMonth; day++) {
      LocalDate date = ym.atDay(day);
      String key = date.toString();

      Optional<MenuDay> optDay = dayDao.findByDate(orgId, date);
      if (!optDay.isPresent()) {
        hasMenuMap.put(key, Boolean.FALSE);
        continue;
      }

      // 当日の全アレルゲン収集
      Set<Short> allAlgIds = new LinkedHashSet<>();
      for (MenuMeal meal : mealDao.listByDay(optDay.get().getId())) {
        for (MenuItem it : itemDao.listByMeal(meal.getId())) {
          for (Allergen a : itemAlgDao.listByItem(it.getId())) {
            allAlgIds.add(a.getId());
          }
        }
      }

      // 子どものアレルゲンと一致するものだけ
      List<Short> matched = new ArrayList<>();
      for (Short id : allAlgIds) {
        if (userAllergenIds.contains(id)) matched.add(id);
      }

      if (!matched.isEmpty()) {
        List<Allergen> list = itemAlgDao.findAllergensByIds(matched);
        List<String> names = new ArrayList<>();
        for (Allergen a : list) names.add(a.getNameJa());
        labelsByDate.put(key, names);
      }

      hasMenuMap.put(key, Boolean.TRUE);
    }

    req.setAttribute("hasMenuMap", hasMenuMap);
    req.setAttribute("labelsByDate", labelsByDate);

    // JSPへ
    req.getRequestDispatcher("/user/menus_calendar.jsp").forward(req, resp);
  }

  // UUID パース（null safe）
  private UUID parseUUID(String s) {
    if (s == null || s.isEmpty()) return null;
    try { return UUID.fromString(s); }
    catch (Exception e) { return null; }
  }

  private Object firstNonNull(Object... arr) {
    for (Object o : arr) if (o != null) return o;
    return null;
  }

  // ===== ★ 共通「対象児 personId 決定」ロジック =====
  private UUID resolvePersonId(HttpServletRequest req, HttpSession ses, List<Individual> children) {
    UUID personId = null;

    // ① クエリ ?person= または ?personId= があれば最優先
    String personParam = req.getParameter("person");
    if (personParam == null || personParam.isEmpty()) {
      personParam = req.getParameter("personId");
    }
    if (personParam != null && !personParam.isEmpty()) {
      personId = parseUUID(personParam);
    }

    // ② セッション currentPersonId があればそれを使う
    if (personId == null && ses != null) {
      Object attr = ses.getAttribute("currentPersonId");
      if (attr instanceof UUID) {
        personId = (UUID) attr;
      } else if (attr instanceof String) {
        personId = parseUUID((String) attr);
      }
    }

    // ③ まだ null なら一覧の先頭の子
    if (personId == null) {
      personId = children.get(0).getId();
    }

    // 念のため「このユーザーの子どもか」をチェック
    boolean belongs = false;
    for (Individual c : children) {
      if (c.getId().equals(personId)) { belongs = true; break; }
    }
    if (!belongs) {
      personId = children.get(0).getId();
    }

    return personId;
  }
}
