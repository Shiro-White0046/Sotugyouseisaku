package user;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import dao.MenuDayDAO;
import dao.MenuItemAllergenDAO;
import dao.MenuItemDAO;
import dao.MenuMealDAO;

/**
 * 利用者向け：献立の詳細（指定日の「朝・昼・夜」と各品目・アレルゲンをまとめて表示）
 * URL例: /user/menus/detail?date=2025-11-10
 */
@WebServlet("/user/menus/detail")
public class MenuDetail extends HttpServlet {

  private final MenuDayDAO dayDao = new MenuDayDAO();
  private final MenuMealDAO mealDao = new MenuMealDAO();
  private final MenuItemDAO itemDao = new MenuItemDAO();
  private final MenuItemAllergenDAO itemAllergenDao = new MenuItemAllergenDAO();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    // 認証チェック（利用者は session 属性 "user" を使用）
    HttpSession session = req.getSession(false);
    if (session == null || session.getAttribute("user") == null) {
      resp.sendRedirect(req.getContextPath() + "/user/login");
      return;
    }
    User loginUser = (User) session.getAttribute("user");
    UUID orgId = loginUser.getOrgId();

    // 必須パラメータ：date=YYYY-MM-DD
    String dateStr = req.getParameter("date");
    if (dateStr == null) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "date パラメータが必要です");
      return;
    }

    final LocalDate date;
    try {
      date = LocalDate.parse(dateStr);
    } catch (Exception e) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "date の形式が不正です");
      return;
    }

    // 日単位の献立を取得（存在しなければ「未登録画面」へ）
    Optional<MenuDay> dayOpt = dayDao.find(orgId, date);
    if (!dayOpt.isPresent()) {
      req.setAttribute("date", date);
      // プロジェクトの配置方針に合わせてパスは /user/ 配下に
      req.getRequestDispatcher("/user/menu_detail_empty.jsp").forward(req, resp);
      return;
    }

    MenuDay day = dayOpt.get();

    // 指定日の朝・昼・夜を取得
    List<MenuMeal> meals = mealDao.listByDay(day.getId());

    // 各 meal ごとの品目一覧をまとめる（mealId → items）
    Map<UUID, List<MenuItem>> itemsMap = new HashMap<UUID, List<MenuItem>>();

    // 各 item ごとのアレルゲン一覧をまとめる（itemId → allergens）
    Map<UUID, List<Allergen>> allergensMap = new HashMap<UUID, List<Allergen>>();

    for (MenuMeal meal : meals) {
      List<MenuItem> items = itemDao.listByMeal(meal.getId());
      itemsMap.put(meal.getId(), items);

      // 品目ごとのアレルゲン
      for (MenuItem it : items) {
        List<Allergen> als = itemAllergenDao.listByItem(it.getId());
        allergensMap.put(it.getId(), als);
      }
    }

    // 画面に渡す
    req.setAttribute("date", date);
    req.setAttribute("day", day);
    req.setAttribute("meals", meals);
    req.setAttribute("itemsMap", itemsMap);
    req.setAttribute("allergensMap", allergensMap);

    // 表示 JSP（/WebContent/user/menu_detail.jsp を想定）
    req.getRequestDispatcher("/user/menu_detail.jsp").forward(req, resp);
  }
}
