package admin;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import bean.Administrator;
import bean.Individual;
import bean.MenuDay;
import bean.MenuMeal;
import bean.Organization;
import dao.IndividualAllergyDAO;
import dao.IndividualDAO;
import dao.MenuDayDAO;
import dao.MenuItemAllergenDAO;
import dao.MenuMealDAO;

@WebServlet("/admin/support-meals")
public class AdminSupportMealsServlet extends HttpServlet {

  private final IndividualDAO individualDAO = new IndividualDAO();
  private final MenuMealDAO mealDAO = new MenuMealDAO();
  private final MenuDayDAO dayDAO = new MenuDayDAO();
  private final IndividualAllergyDAO iaDAO = new IndividualAllergyDAO();
  private final MenuItemAllergenDAO miaDAO = new MenuItemAllergenDAO();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    HttpSession ses = req.getSession(false);
    Administrator admin = (Administrator) ses.getAttribute("admin");
    Organization org = (Organization) ses.getAttribute("org");

    if (admin == null || org == null) {
      resp.sendRedirect(req.getContextPath() + "/admin/login");
      return;
    }

    // ------------------------------
    // 1) 今日のメニュー取得
    // ------------------------------
    LocalDate today = LocalDate.now();
    MenuDay day = dayDAO.findByDate(org.getId(), today);
    if (day == null) {
      req.setAttribute("error", "今日の献立が登録されていません");
      req.getRequestDispatcher("/WEB-INF/views/admin/support_meals.jsp").forward(req, resp);
      return;
    }

    // mealType = breakfast / lunch / dinner
    String mealType = Optional.ofNullable(req.getParameter("mealType")).orElse("dinner");

    List<MenuMeal> meals = mealDAO.listByDayAndType(day.getId(), mealType);

    // ------------------------------
    // 2) 組織の子供一覧
    // ------------------------------
    List<Individual> persons = individualDAO.listByOrg(org.getId());

    // ------------------------------
    // 3) 子供ごとのアレルギー一覧をまとめる
    // ------------------------------
    Map<UUID, Set<UUID>> personAllergies = new HashMap<>();
    for (Individual p : persons) {
      personAllergies.put(
          p.getId(),
          iaDAO.findByPersonIdAsSet(p.getId())
      );
    }

    // ------------------------------
    // 4) 食事メニューに含まれるアレルギー一覧
    // ------------------------------
    Map<UUID, Set<UUID>> mealAllergies = new HashMap<>();
    for (MenuMeal m : meals) {
      mealAllergies.put(
          m.getId(),
          miaDAO.findByMealIdAsSet(m.getId())
      );
    }

    req.setAttribute("mealType", mealType);
    req.setAttribute("meals", meals);
    req.setAttribute("persons", persons);
    req.setAttribute("personAllergies", personAllergies);
    req.setAttribute("mealAllergies", mealAllergies);

    req.getRequestDispatcher("/WEB-INF/views/admin/support_meals.jsp")
        .forward(req, resp);
  }
}

