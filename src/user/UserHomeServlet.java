package user;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import bean.MenuDay;
import bean.MenuItem;
import bean.MenuMeal;
import bean.User;
import dao.MenuDayDAO;
import dao.MenuItemDAO;
import dao.MenuMealDAO;

@WebServlet("/user/home")
public class UserHomeServlet extends HttpServlet {
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    HttpSession ses = req.getSession(false);
    User user = (ses != null) ? (User) ses.getAttribute("user") : null;
    if (user == null) {
      resp.sendRedirect(req.getContextPath() + "/user/login");
      return;
    }

    // -------- 今日の献立（公開分のみ）を取得 --------
    UUID orgId = user.getOrgId();
    LocalDate today = LocalDate.now(); // サーバ時刻。必要ならTZをアプリで統一

    MenuDayDAO dayDao   = new MenuDayDAO();
    MenuMealDAO mealDao = new MenuMealDAO();
    MenuItemDAO itemDao = new MenuItemDAO();

    Optional<MenuDay> dayOpt = dayDao.findByDate(orgId, today);
    if (!dayOpt.isPresent() || !dayOpt.get().isPublished()) {
      // 何も登録がない/非公開
      req.setAttribute("hasTodayMenu", false);
      req.getRequestDispatcher("/user/home.jsp").forward(req, resp);
      return;
    }

    MenuDay day = dayOpt.get();

    // -------- 時刻から朝/昼/夜を自動選択 --------
    // 09:59まで=朝, 10:00–14:59=昼, 15:00以降=夜（必要なら調整）
    String slot = chooseMealSlot(LocalTime.now());

    Optional<MenuMeal> mealOpt = mealDao.findByDayAndSlot(day.getId(), slot);
    if (!mealOpt.isPresent()) {
      // 指定スロットが未登録なら、登録がある最初のスロットを使う
      Optional<MenuMeal> fallback = mealDao.findAnyByDay(day.getId());
      if (fallback.isPresent()) {
        mealOpt = fallback;
        slot = fallback.get().getMealSlot(); // 表示用に更新
      }
    }

    if (!mealOpt.isPresent()) {
      // 1件もない（dayだけ作ってmeals未登録）
      req.setAttribute("hasTodayMenu", true);
      req.setAttribute("menuDay", day);
      req.setAttribute("selectedSlot", slot);
      req.setAttribute("meal", null);
      req.setAttribute("items", java.util.Collections.emptyList());
      req.getRequestDispatcher("/user/home.jsp").forward(req, resp);
      return;
    }

    MenuMeal meal = mealOpt.get();
    List<MenuItem> items = itemDao.listByMeal(meal.getId());

    // -------- JSPへ渡す --------
    req.setAttribute("hasTodayMenu", true);
    req.setAttribute("menuDay", day);          // date, image_path など
    req.setAttribute("selectedSlot", slot);    // "breakfast" | "lunch" | "dinner"
    req.setAttribute("meal", meal);            // name, description
    req.setAttribute("items", items);          // 各品目（item_order, name, note）

    req.getRequestDispatcher("/user/home.jsp").forward(req, resp);
  }

  private static String chooseMealSlot(LocalTime now) {
    if (now.isBefore(LocalTime.of(10, 0))) return "breakfast";
    if (now.isBefore(LocalTime.of(15, 0))) return "lunch";
    return "dinner";
  }
}
