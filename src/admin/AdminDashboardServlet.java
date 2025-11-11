package admin;

import java.io.IOException;
import java.time.LocalDate;
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

import bean.Administrator;
import bean.MenuDay;
import bean.MenuItem;
import bean.MenuMeal;
import bean.Organization;
import dao.MenuDayDAO;
import dao.MenuItemDAO;
import dao.MenuMealDAO;

@WebServlet("/admin/home")
public class AdminDashboardServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  private final MenuDayDAO dayDao = new MenuDayDAO();
  private final MenuMealDAO mealDao = new MenuMealDAO();
  private final MenuItemDAO itemDao = new MenuItemDAO();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    HttpSession ses = req.getSession(false);
    Administrator admin = (ses != null) ? (Administrator) ses.getAttribute("admin") : null;
    Organization org    = (ses != null) ? (Organization)  ses.getAttribute("org")   : null;

    if (admin == null || org == null) {
      resp.sendRedirect(req.getContextPath() + "/admin/login");
      return;
    }

    // 今日の日付を使用（別日表示したい場合はパラメータで指定してもOK）
    LocalDate today = LocalDate.now();

    // menu_days を取得（なければ新規作成）
    MenuDay menuDay = dayDao.findByDate(org.getId(), today)
        .orElseGet(() -> {
          UUID id = dayDao.ensureDay(org.getId(), today);
          return dayDao.find(id).orElse(null);
        });

    if (menuDay == null) {
      req.setAttribute("error", "本日の献立情報を取得できませんでした。");
      req.getRequestDispatcher("/admin/home.jsp").forward(req, resp);
      return;
    }

    // スロットごとの Meal 一覧
    Map<String, MenuMeal> meals = mealDao.findByDayAsMap(menuDay.getId());

    // 各 Meal に紐づく品目
    Map<String, List<MenuItem>> itemsBySlot = new HashMap<>();
    for (String slot : new String[]{"BREAKFAST", "LUNCH", "DINNER"}) {
      MenuMeal meal = meals.get(slot);
      if (meal != null) {
        itemsBySlot.put(slot, itemDao.listWithAllergens(meal.getId()));
      } else {
        itemsBySlot.put(slot, java.util.Collections.emptyList());
      }
    }

    // JSP用データ
    req.setAttribute("menuDay", menuDay);
    req.setAttribute("meals", meals);
    req.setAttribute("itemsBySlot", itemsBySlot);

    req.getRequestDispatcher("/admin/home.jsp").forward(req, resp);
  }
}
