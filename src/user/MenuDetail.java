package user;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
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
import bean.MenuItem;
import bean.MenuMeal;
import bean.User;
import dao.IndividualAllergyDAO;
import dao.IndividualDAO;
import dao.MenuDayDAO;
import dao.MenuItemAllergenDAO;
import dao.MenuItemDAO;
import dao.MenuMealDAO;

@WebServlet("/user/menudetail")
public class MenuDetail extends HttpServlet {
  private static final long serialVersionUID = 1L;

  private static final String FALLBACK_LOGIN_URL = "/auth/login";
  private static final String FALLBACK_CALENDAR_URL = "/user/menuscalendar";

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

    // パラメータ
    String dateStr = req.getParameter("date");
    if (dateStr == null || dateStr.isEmpty()) { resp.sendRedirect(req.getContextPath() + FALLBACK_CALENDAR_URL); return; }
    LocalDate date;
    try { date = LocalDate.parse(dateStr); }
    catch (Exception e) { resp.sendRedirect(req.getContextPath() + FALLBACK_CALENDAR_URL); return; }

    // 指定日
    Optional<MenuDay> optDay = dayDao.findByDate(orgId, date);
    if (!optDay.isPresent()) {
      req.setAttribute("date", date);
      req.getRequestDispatcher("/user/menu_detail_empty.jsp").forward(req, resp);
      return;
    }
    MenuDay day = optDay.get();

    // 朝→昼→夜の優先で1食取得（無ければ画像のみ表示）
    Optional<MenuMeal> optMeal = mealDao.findAnyByDay(day.getId());

    Map<String,Object> menuMap = new HashMap<>();
    List<Allergen> filtered = new ArrayList<>();

    if (optMeal.isPresent()) {
      MenuMeal meal = optMeal.get();
      menuMap.put("name", meal.getName());
      menuMap.put("description", meal.getDescription());
      for (MenuItem it : itemDao.listByMeal(meal.getId())) {
        for (Allergen a : itemAllergenDao.listByItem(it.getId())) {
          if (userAllergenIds.contains(a.getId())) filtered.add(a); // ★利用者のみ
        }
      }
    } else {
      menuMap.put("name", "(メニュー未設定)");
      menuMap.put("description", "");
    }
    menuMap.put("imagePath", day.getImagePath());
    menuMap.put("menuDate", day.getMenuDate());

    req.setAttribute("menu", menuMap);
    req.setAttribute("allergens", filtered);
    req.getRequestDispatcher("/user/menu_detail.jsp").forward(req, resp);
  }

  private static Object firstNonNull(Object... arr) { for (Object o: arr) if (o!=null) return o; return null; }
}