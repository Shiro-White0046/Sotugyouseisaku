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
import bean.MenuItem;
import bean.Organization;
import dao.IndividualAllergyDAO;
import dao.IndividualDAO;
import dao.MenuItemAllergenDAO;
import dao.MenuItemDAO;

@WebServlet("/admin/support-meals")
public class AdminSupportMealsServlet extends HttpServlet {

  private final IndividualDAO individualDAO = new IndividualDAO();
  private final IndividualAllergyDAO iaDAO = new IndividualAllergyDAO();
  private final MenuItemDAO menuItemDAO = new MenuItemDAO();
  private final MenuItemAllergenDAO miaDAO = new MenuItemAllergenDAO();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    HttpSession ses = req.getSession(false);
    Administrator admin = (ses != null) ? (Administrator) ses.getAttribute("admin") : null;
    Organization org    = (ses != null) ? (Organization) ses.getAttribute("org")   : null;

    if (admin == null || org == null) {
      resp.sendRedirect(req.getContextPath() + "/admin/login");
      return;
    }

    // ==== 1) 朝/昼/夜の判定 ====
    String mealType = req.getParameter("mealType");
    if (mealType == null || mealType.isEmpty()) {
      mealType = "dinner"; // デフォルト夜
    }

    String mealLabel;
    switch (mealType) {
      case "breakfast":

        mealLabel = "朝飯";
        break;
      case "lunch":

        mealLabel = "昼飯";
        break;
      default:

        mealLabel = "夜飯";
        break;
    }

    LocalDate today = LocalDate.now();

 // ==== 2) この時間帯のメニュー品目一覧（横軸） ====
 // orgId / date は一旦見ないで、meal_slot だけで絞る
 List<MenuItem> items = menuItemDAO.listByMealSlot(mealType);

    // ==== 3) 組織の個人一覧（縦軸） ====
    List<Individual> individuals = individualDAO.listByOrg(org.getId());

    // ==== 4) 子どもごとのアレルギーセット（Integer 型） ====
    Map<UUID, Set<Integer>> personAllergies = new HashMap<>();
    for (Individual ind : individuals) {
      Set<Integer> allergenIds = iaDAO.findByPersonIdAsSet(ind.getId());
      personAllergies.put(ind.getId(), allergenIds);
    }

    // ==== 5) メニューごとのアレルギーセット（Integer 型） ====
    Map<UUID, Set<Integer>> itemAllergies = new HashMap<>();
    for (MenuItem item : items) {
      Set<Integer> allergenIds = miaDAO.findByItemIdAsSet(item.getId());
      itemAllergies.put(item.getId(), allergenIds);
    }

    // ==== 6) ○ を出すかどうかのマップ (personId-itemId → true/false) ====
    Map<String, Boolean> supportMap = new HashMap<>();

    long needCount = 0; // 対応食が必要な人数

    for (Individual ind : individuals) {
      UUID personId = ind.getId();
      Set<Integer> pAll = personAllergies.get(personId);
      if (pAll == null || pAll.isEmpty()) {
        // そもそもアレルギー登録なし
        for (MenuItem item : items) {
          supportMap.put(personId.toString() + "-" + item.getId().toString(), Boolean.FALSE);
        }
        continue;
      }

      boolean thisPersonNeed = false;

      for (MenuItem item : items) {
        UUID itemId = item.getId();
        Set<Integer> iAll = itemAllergies.get(itemId);
        boolean showCircle = false;

        if (iAll != null && !iAll.isEmpty()) {
          // 積集合があれば対応食必要
          for (Integer a : pAll) {
            if (iAll.contains(a)) {
              showCircle = true;
              break;
            }
          }
        }

        if (showCircle) {
          thisPersonNeed = true;
        }
        supportMap.put(personId.toString() + "-" + itemId.toString(), showCircle);
      }

      if (thisPersonNeed) {
        needCount++;
      }
    }

    // ==== 7) JSP へ渡す ====
    req.setAttribute("mealType", mealType);
    req.setAttribute("mealLabel", mealLabel);
    req.setAttribute("items", items);
    req.setAttribute("individuals", individuals);
    req.setAttribute("supportMap", supportMap);
    req.setAttribute("needCount", needCount);

    req.getRequestDispatcher("/admin/menus_support.jsp")
       .forward(req, resp);
  }
}
