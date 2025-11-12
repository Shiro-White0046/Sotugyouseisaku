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

    // ===== 今日の献立（日ヘッダ） =====
    UUID orgId = user.getOrgId();
    LocalDate today = LocalDate.now();
    req.setAttribute("todayDate", today); // 3カラム側の見出しで使用

    MenuDayDAO dayDao   = new MenuDayDAO();
    MenuMealDAO mealDao = new MenuMealDAO();
    MenuItemDAO itemDao = new MenuItemDAO();

    Optional<MenuDay> dayOpt = dayDao.findByDate(orgId, today);
    if (!dayOpt.isPresent() || !dayOpt.get().isPublished()) {
      // 何も登録がない/非公開 → 既存JSPの「未登録です」を出す
      req.setAttribute("hasTodayMenu", false);
      req.getRequestDispatcher("/user/home.jsp").forward(req, resp);
      return;
    }

    MenuDay day = dayOpt.get();
    // 既存JSP（“今日の献立”カード）向け：従来キー名を維持
    req.setAttribute("hasTodayMenu", true);
    req.setAttribute("menuDay", day);
    // 3カラム側の互換：todayMenu という別名も積む
    req.setAttribute("todayMenu", day);

    // ===== 時間帯（朝/昼/夕）をロード：3カラム表示用 =====
    MenuMeal breakfast = mealDao.findByDayAndSlot(day.getId(), "BREAKFAST").orElse(null);
    MenuMeal lunch     = mealDao.findByDayAndSlot(day.getId(), "LUNCH").orElse(null);
    MenuMeal dinner    = mealDao.findByDayAndSlot(day.getId(), "DINNER").orElse(null);
    req.setAttribute("breakfast", breakfast);
    req.setAttribute("lunch",     lunch);
    req.setAttribute("dinner",    dinner);

    // ===== 既存の「自動選択スロット」ロジックは維持（下部リスト表示などで使用） =====
    String chosen = chooseMealSlot(LocalTime.now()); // "breakfast"/"lunch"/"dinner"（下でUPPERに正規化）
    String chosenUpper = chosen.toUpperCase();       // DAOは大文字想定でも小文字でも動くように

    Optional<MenuMeal> mealOpt = mealDao.findByDayAndSlot(day.getId(), chosenUpper);
    if (!mealOpt.isPresent()) {
      // 指定スロットが未登録 → どれか一つでも登録があれば拾う
      Optional<MenuMeal> fallback = mealDao.findAnyByDay(day.getId());
      if (fallback.isPresent()) {
        mealOpt = fallback;
        chosenUpper = fallback.get().getSlot(); // ここはDAOの返却値に合わせる（"BREAKFAST" 等）
      }
    }

    if (!mealOpt.isPresent()) {
      // meals未登録（dayだけ作ってある）
      req.setAttribute("selectedSlot", chosenUpper);
      req.setAttribute("meal", null);
      req.setAttribute("items", java.util.Collections.emptyList());
      req.getRequestDispatcher("/user/home.jsp").forward(req, resp);
      return;
    }

    MenuMeal meal = mealOpt.get();
    List<MenuItem> items = itemDao.listByMeal(meal.getId());

    // ===== 既存キー名でJSPに渡す（保持） =====
    req.setAttribute("selectedSlot", chosenUpper); // "BREAKFAST" | "LUNCH" | "DINNER"
    req.setAttribute("meal", meal);
    req.setAttribute("items", items);

    req.getRequestDispatcher("/user/home.jsp").forward(req, resp);
  }

  // 09:59まで=朝, 10:00–14:59=昼, 15:00以降=夜（必要なら調整）
  private static String chooseMealSlot(LocalTime now) {
    if (now.isBefore(LocalTime.of(10, 0))) return "breakfast";
    if (now.isBefore(LocalTime.of(15, 0))) return "lunch";
    return "dinner";
  }
}
