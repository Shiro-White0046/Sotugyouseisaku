package user;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
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

@WebServlet(urlPatterns = {"/user/menu_detail", "/user/menudetail"})
public class MenuDetail extends HttpServlet {
  private static final long serialVersionUID = 1L;

  private static final String FALLBACK_LOGIN_URL    = "/auth/login";
  private static final String FALLBACK_CALENDAR_URL = "/user/menuscalendar";

  private final MenuDayDAO            dayDao  = new MenuDayDAO();
  private final MenuMealDAO           mealDao = new MenuMealDAO();
  private final MenuItemDAO           itemDao = new MenuItemDAO();
  private final MenuItemAllergenDAO   itemAllergenDao = new MenuItemAllergenDAO();
  private final IndividualDAO         individualDao   = new IndividualDAO();
  private final IndividualAllergyDAO  individualAllergyDao = new IndividualAllergyDAO();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    // 認証
    HttpSession ses = req.getSession(false);
    User loginUser = (ses == null) ? null : (User) firstNonNull(
        ses.getAttribute("loginUser"),
        ses.getAttribute("user"),
        ses.getAttribute("account"));
    if (loginUser == null) {
      resp.sendRedirect(req.getContextPath() + FALLBACK_LOGIN_URL);
      return;
    }

    UUID orgId = loginUser.getOrgId();

    // 子ども一覧（保護者配下）
    List<bean.Individual> children = individualDao.listByUser(loginUser.getId());
    if (children == null || children.isEmpty()) {
      resp.sendRedirect(req.getContextPath() + FALLBACK_CALENDAR_URL);
      return;
    }
    req.setAttribute("children", children);

    // ★ 対象児 personId を共通ロジックで決定
    UUID personId = resolvePersonId(req, ses, children);

    // ★ 決定した対象児をセッションに保存（他画面と共有）
    if (ses != null) {
      ses.setAttribute("currentPersonId", personId);
    }

    req.setAttribute("personId", personId);

    // 表示中の child を入れておく（JSP表示用）
    bean.Individual selectedChild = children.get(0);
    for (bean.Individual c : children) {
      if (c.getId().equals(personId)) { selectedChild = c; break; }
    }
    req.setAttribute("selectedChild", selectedChild);

    // 当人のアレルゲンIDセット
    Set<Short> userAllergenIds = new HashSet<Short>();
    for (bean.IndividualAllergy ia : individualAllergyDao.listByPerson(personId)) {
      userAllergenIds.add(ia.getAllergenId());
    }

    // パラメータ：date
    String dateStr = req.getParameter("date");
    if (dateStr == null || dateStr.isEmpty()) {
      resp.sendRedirect(req.getContextPath() + FALLBACK_CALENDAR_URL);
      return;
    }
    LocalDate date;
    try { date = LocalDate.parse(dateStr); }
    catch (Exception e) {
      resp.sendRedirect(req.getContextPath() + FALLBACK_CALENDAR_URL);
      return;
    }

    // 日のメニューヘッダ
    Optional<MenuDay> optDay = dayDao.findByDate(orgId, date);
    if (!optDay.isPresent()) {
      // 空表示
      req.setAttribute("menuImagePath", null);
      req.setAttribute("menuDate", date);
      req.setAttribute("sections", Collections.emptyList());
      setHeadAndYm(req, date);
      req.getRequestDispatcher("/user/menu_detail.jsp").forward(req, resp);
      return;
    }
    MenuDay day = optDay.get();

    // 朝・昼・夕でセクション作成
    Map<String, MenuMeal> mealMap = mealDao.findByDayAsMap(day.getId()); // slot -> meal
    String[] slots  = {"BREAKFAST","LUNCH","DINNER"};
    String[] labels = {"朝食","昼食","夕食"};

    List<Map<String,Object>> sections = new ArrayList<Map<String,Object>>();

    for (int i = 0; i < slots.length; i++) {
      String slot = slots[i];
      String label = labels[i];

      Map<String,Object> sec = new LinkedHashMap<String,Object>();
      sec.put("slot",  slot);
      sec.put("label", label);

      MenuMeal meal = mealMap.get(slot);
      if (meal == null) {
        sec.put("name","(メニュー未設定)");
        sec.put("description","");
        sec.put("imagePath", null);
        sec.put("allergensUser",  Collections.emptyList());
        sec.put("allergensOther", Collections.emptyList());
      } else {
        sec.put("name", meal.getName());
        sec.put("description", meal.getDescription());

        // 画像（任意プロパティ getImagePath があれば使う）
        String mealImage = null;
        try {
          Object v = meal.getClass().getMethod("getImagePath").invoke(meal);
          if (v != null) mealImage = String.valueOf(v);
        } catch (Exception ignore) {}
        sec.put("imagePath", mealImage);

        // その食事に含まれるアレルゲン（重複排除）
        Map<Short, Allergen> allMap = new LinkedHashMap<Short, Allergen>();
        for (MenuItem it : itemDao.listByMeal(meal.getId())) {
          for (Allergen a : itemAllergenDao.listByItem(it.getId())) {
            if (!allMap.containsKey(a.getId())) allMap.put(a.getId(), a);
          }
        }

        // 利用者一致（赤）／その他（黒）
        List<Allergen> userList  = new ArrayList<Allergen>();
        List<Allergen> otherList = new ArrayList<Allergen>();
        for (Map.Entry<Short, Allergen> e : allMap.entrySet()) {
          if (userAllergenIds.contains(e.getKey())) userList.add(e.getValue());
          else otherList.add(e.getValue());
        }

        sec.put("allergensUser",  userList);
        sec.put("allergensOther", otherList);
      }

      sections.add(sec);
    }

    req.setAttribute("menuImagePath", day.getImagePath());
    req.setAttribute("menuDate", day.getMenuDate());
    req.setAttribute("sections", sections);
    setHeadAndYm(req, day.getMenuDate());

    req.getRequestDispatcher("/user/menu_detail.jsp").forward(req, resp);
  }

  // ============ helpers ============
  private static Object firstNonNull(Object... arr) {
    for (Object o : arr) if (o != null) return o;
    return null;
  }

  private static UUID parseUUID(String s) {
    if (s == null || s.trim().isEmpty()) return null;
    try { return java.util.UUID.fromString(s.trim()); }
    catch (Exception ignore) { return null; }
  }

  private static void setHeadAndYm(HttpServletRequest req, LocalDate d) {
    req.setAttribute("headTitle", (d == null) ? "この日のメニュー"
        : (d.getMonthValue() + "月" + d.getDayOfMonth() + "日のメニュー"));
    req.setAttribute("ym", (d == null) ? "" : String.format("%d-%02d", d.getYear(), d.getMonthValue()));
  }

  // ★ 追加：対象児 personId を決める共通ロジック
  private static UUID resolvePersonId(HttpServletRequest req, HttpSession ses, List<bean.Individual> children) {
    UUID personId = null;

    // ① クエリ ?person= or ?personId= を最優先
    String personParam = req.getParameter("person");
    if (personParam == null || personParam.isEmpty()) {
      personParam = req.getParameter("personId");
    }
    if (personParam != null && !personParam.isEmpty()) {
      personId = parseUUID(personParam);
    }

    // ② セッション currentPersonId
    if (personId == null && ses != null) {
      Object attr = ses.getAttribute("currentPersonId");
      if (attr instanceof UUID) {
        personId = (UUID) attr;
      } else if (attr instanceof String) {
        personId = parseUUID((String) attr);
      }
    }

    // ③ まだ null なら先頭の子
    if (personId == null) {
      personId = children.get(0).getId();
    }

    // 念のため「このユーザーの子どもか」をチェック
    boolean belongs = false;
    for (bean.Individual c : children) {
      if (c.getId().equals(personId)) {
        belongs = true;
        break;
      }
    }
    if (!belongs) {
      personId = children.get(0).getId();
    }

    return personId;
  }
}
