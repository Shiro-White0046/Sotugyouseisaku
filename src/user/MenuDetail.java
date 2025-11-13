package user;

import java.io.IOException;
import java.lang.reflect.Method;                 // ★ 追加
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
import bean.MenuItem;                            // ★ 追加
import bean.MenuMeal;
import bean.User;
import dao.IndividualAllergyDAO;
import dao.IndividualDAO;
import dao.MenuDayDAO;
import dao.MenuItemAllergenDAO;
import dao.MenuItemDAO;
import dao.MenuMealDAO;

@WebServlet(urlPatterns = {
    "/user/menudetail",
    "/user/menu_detail",
    "/user/menus/detail"
})
public class MenuDetail extends HttpServlet {
  private static final long serialVersionUID = 1L;

  private static final String FALLBACK_LOGIN_URL = "/auth/login";
  private static final String FALLBACK_CALENDAR_URL = "/user/menuscalendar"; // ★ 統一

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
      req.setAttribute("menuImagePath", null);
      req.setAttribute("menuDate", date);
      req.setAttribute("sections", buildEmptySections());     // ★ キー整合版に変更済み
      setHeadAndYm(req, date);
      req.getRequestDispatcher("/user/menu_detail.jsp").forward(req, resp);
      return;
    }
    MenuDay day = optDay.get();

    // ---- 朝→昼→晩 の順に 3セクション分を作る ----
    Map<String, MenuMeal> mealMap = mealDao.findByDayAsMap(day.getId());
    String[] slots  = {"BREAKFAST", "LUNCH", "DINNER"};
    String[] labels = {"朝食", "昼食", "夕食"};

    List<Map<String,Object>> sections = new ArrayList<>();
    for (int i = 0; i < slots.length; i++) {
      String slot  = slots[i];
      String label = labels[i];

      Map<String,Object> sec = new LinkedHashMap<>();
      sec.put("slot", slot);
      sec.put("label", label);

      MenuMeal meal = mealMap.get(slot);
      if (meal == null) {
        sec.put("name", "(メニュー未設定)");
        sec.put("description", "");
        sec.put("imagePath", null);
        sec.put("allergensUser",  Collections.emptyList());
        sec.put("allergensOther", Collections.emptyList());
      } else {
        sec.put("name", meal.getName());
        sec.put("description", meal.getDescription());

        // 画像（Java8互換の反射）
        String mealImage = null;
        try {
          Method m = meal.getClass().getMethod("getImagePath");
          Object v = m.invoke(meal);
          if (v != null) mealImage = String.valueOf(v);
        } catch (Exception ignore) {}
        sec.put("imagePath", mealImage);

        // その食事に含まれる全アレルゲン（重複除去）
        Map<Short, Allergen> allMap = new LinkedHashMap<>();
        for (MenuItem it : itemDao.listByMeal(meal.getId())) {
          for (Allergen a : itemAllergenDao.listByItem(it.getId())) {
            if (!allMap.containsKey(a.getId())) allMap.put(a.getId(), a);
          }
        }

        // 利用者一致（赤）／その他（黒）
        List<Allergen> userList  = new ArrayList<>();
        List<Allergen> otherList = new ArrayList<>();
        for (Map.Entry<Short,Allergen> e : allMap.entrySet()) {
          if (userAllergenIds.contains(e.getKey())) userList.add(e.getValue());
          else otherList.add(e.getValue());
        }
        sec.put("allergensUser",  userList);
        sec.put("allergensOther", otherList);
      }
      sections.add(sec);
    }

    // JSPへ
    req.setAttribute("menuImagePath", day.getImagePath());
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

  // ★ 空セクションでも JSP のキーと揃える
  private static List<Map<String,Object>> buildEmptySections() {
    String[] labels = {"朝食", "昼食", "夕食"};
    List<Map<String,Object>> list = new ArrayList<>();
    for (String label : labels) {
      Map<String,Object> m = new LinkedHashMap<>();
      m.put("label", label);
      m.put("name", "(メニュー未設定)");
      m.put("description", "");
      m.put("imagePath", null);
      m.put("allergensUser",  Collections.emptyList());
      m.put("allergensOther", Collections.emptyList());
      list.add(m);
    }
    return list;
  }

  private static Object firstNonNull(Object... arr) { for (Object o: arr) if (o!=null) return o; return null; }
}