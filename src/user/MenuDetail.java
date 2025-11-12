package user;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import bean.MenuMeal;
import bean.User;
import dao.IndividualAllergyDAO;
import dao.IndividualDAO;
import dao.MenuDayDAO;
import dao.MenuItemAllergenDAO;
import dao.MenuItemDAO;
import dao.MenuMealDAO;

@WebServlet(urlPatterns = {
    "/user/menudetail",     // 旧リンク
    "/user/menu_detail",    // 現在のリンク
    "/user/menus/detail"    // 互換
})
public class MenuDetail extends HttpServlet {
  private static final long serialVersionUID = 1L;

  private static final String FALLBACK_LOGIN_URL = "/auth/login";
  private static final String FALLBACK_CALENDAR_URL = "/user/menus_calendar";

  private final MenuDayDAO dayDao = new MenuDayDAO();
  private final MenuMealDAO mealDao = new MenuMealDAO();
  private final MenuItemDAO itemDao = new MenuItemDAO();
  private final MenuItemAllergenDAO itemAllergenDao = new MenuItemAllergenDAO();
  private final IndividualDAO individualDao = new IndividualDAO();
  private final IndividualAllergyDAO individualAllergyDao = new IndividualAllergyDAO();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    // 認証
    HttpSession session = req.getSession(false);
    User loginUser = (session == null) ? null : (User) firstNonNull(
        session.getAttribute("loginUser"),
        session.getAttribute("user"),
        session.getAttribute("account"));
    if (loginUser == null) { resp.sendRedirect(req.getContextPath() + FALLBACK_LOGIN_URL); return; }
    UUID orgId = loginUser.getOrgId();

    // 個人特定
    bean.Individual person = individualDao.findOneByUser(loginUser);
    if (person == null) { resp.sendRedirect(req.getContextPath() + FALLBACK_CALENDAR_URL); return; }
    UUID individualId = person.getId();
    Set<Short> userAllergenIds = individualAllergyDao.listByPerson(individualId)
        .stream().map(bean.IndividualAllergy::getAllergenId).collect(Collectors.toSet());

    // パラメータ：日付
    String dateStr = req.getParameter("date");
    if (dateStr == null || dateStr.isEmpty()) { resp.sendRedirect(req.getContextPath() + FALLBACK_CALENDAR_URL); return; }
    LocalDate date;
    try { date = LocalDate.parse(dateStr); }
    catch (Exception e) { resp.sendRedirect(req.getContextPath() + FALLBACK_CALENDAR_URL); return; }

    // 指定日取得
    Optional<MenuDay> optDay = dayDao.findByDate(orgId, date);
    if (!optDay.isPresent()) {
      // 1日分の枠は出すが、全部 未設定 にする
      req.setAttribute("menuImagePath", null);
      req.setAttribute("menuDate", date);
      req.setAttribute("sections", buildEmptySections());
      setHeadAndYm(req, date);
      req.getRequestDispatcher("/user/menu_detail.jsp").forward(req, resp);
      return;
    }
    MenuDay day = optDay.get();

    // ---- 朝→昼→晩 の順に 3セクション分を作る ----
    Map<String, MenuMeal> mealMap = mealDao.findByDayAsMap(day.getId()); // slot -> MenuMeal
    // スロット定義（DB値に合わせて大文字）
    String[] slots = {"BREAKFAST", "LUNCH", "DINNER"};
    String[] labels = {"朝食", "昼食", "夕食"};

    List<Map<String,Object>> sections = new ArrayList<>();
    for (int i = 0; i < slots.length; i++) {
      String slot = slots[i];
      String label = labels[i];

      Map<String,Object> sec = new LinkedHashMap<>();
      sec.put("slot", slot);
      sec.put("label", label);

      MenuMeal meal = mealMap.get(slot);
      if (meal == null) {
        sec.put("name", "(メニュー未設定)");
        sec.put("description", "");
        sec.put("allergens", Collections.emptyList());
      } else {
        sec.put("name", meal.getName());
        sec.put("description", meal.getDescription());

        // 品目→アレルゲン→利用者に一致するものだけ
        List<Allergen> filtered = new ArrayList<>();
        for (bean.MenuItem it : itemDao.listByMeal(meal.getId())) {
          for (Allergen a : itemAllergenDao.listByItem(it.getId())) {
            if (userAllergenIds.contains(a.getId())) filtered.add(a);
          }
        }
        sec.put("allergens", filtered);
      }
      sections.add(sec);
    }

    // JSPへ
    req.setAttribute("menuImagePath", day.getImagePath()); // 1日の画像（全セクション共通）
    req.setAttribute("menuDate", day.getMenuDate());
    req.setAttribute("sections", sections);
    setHeadAndYm(req, day.getMenuDate());

    req.getRequestDispatcher("/user/menu_detail.jsp").forward(req, resp);
  }

  private static void setHeadAndYm(HttpServletRequest req, LocalDate d) {
    req.setAttribute("headTitle", (d == null) ? "この日のメニュー"
        : (d.getMonthValue()+"月"+d.getDayOfMonth()+"日のメニュー"));
    req.setAttribute("ym", (d == null) ? "" : String.format("%d-%02d", d.getYear(), d.getMonthValue()));
  }

  private static List<Map<String,Object>> buildEmptySections() {
    String[] labels = {"朝食", "昼食", "夕食"};
    List<Map<String,Object>> list = new ArrayList<>();
    for (String label : labels) {
      Map<String,Object> m = new LinkedHashMap<>();
      m.put("label", label);
      m.put("name", "(メニュー未設定)");
      m.put("description", "");
      m.put("allergens", Collections.emptyList());
      list.add(m);
    }
    return list;
  }

  private static Object firstNonNull(Object... arr) { for (Object o: arr) if (o!=null) return o; return null; }
}
