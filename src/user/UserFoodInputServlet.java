// src/user/UserFoodInputServlet.java
package user;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
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
import bean.Individual;
import bean.User;
import dao.AllergenDAO;
import dao.IndividualAllergyDAO;
import dao.IndividualDAO;

@WebServlet("/user/allergy/food")
public class UserFoodInputServlet extends HttpServlet {
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    HttpSession ses = req.getSession(false);
    User user = (ses != null) ? (User) ses.getAttribute("user") : null;

    if (user == null) {
      resp.sendRedirect(req.getContextPath() + "/user/login");
      return;
    }

    IndividualDAO individualDAO = new IndividualDAO();

    // ★ 対象児一覧を取得（single でも1件だけ返る想定）
    List<Individual> persons = individualDAO.listByUser(user.getId());
    if (persons.isEmpty()) {
      req.setAttribute("flash", "まずはお子さま（個人）を登録してください。");
      req.getRequestDispatcher("/user/home.jsp").forward(req, resp);
      return;
    }

    // ★★★ 対象児の決定ロジック（接触性と同じパターン）★★★
    UUID personId = null;

    // ① クエリ ?person= or ?personId= があれば最優先
    String personParam = req.getParameter("person");
    if (personParam == null || personParam.isEmpty()) {
      personParam = req.getParameter("personId");
    }
    if (personParam != null && !personParam.isEmpty()) {
      try {
        personId = UUID.fromString(personParam);
      } catch (IllegalArgumentException ignore) {
        // 無効な UUID は無視
      }
    }

    // ② セッションの currentPersonId があればそれを使う
    if (personId == null && ses != null) {
      Object attr = ses.getAttribute("currentPersonId");
      if (attr instanceof UUID) {
        personId = (UUID) attr;
      } else if (attr instanceof String) {
        try { personId = UUID.fromString((String) attr); } catch (Exception ignore) {}
      }
    }

    // ③ それでも無ければ一覧の先頭を採用
    if (personId == null) {
      personId = persons.get(0).getId();
    }

    // ④ 決まった対象児をセッションにも保存（接触性と共有）
    if (ses != null) {
      ses.setAttribute("currentPersonId", personId);
    }

    // ★ 食物アレルギーマスタ（FOOD）
    AllergenDAO allergenDao = new AllergenDAO();
    List<Allergen> list = allergenDao.listByCategory("FOOD");

    // ★ その子に紐づく既存アレルギーのうち FOOD のみ ID セット化
    IndividualAllergyDAO iaDao = new IndividualAllergyDAO();
    Set<Short> selectedIds = iaDao.listAllergensOfPerson(personId).stream()
        .filter(a -> "FOOD".equalsIgnoreCase(a.getCategory()))
        .map(Allergen::getId)
        .collect(Collectors.toCollection(LinkedHashSet::new));

    // JSP 用にセット
    req.setAttribute("persons", persons);    // ★ 対象児一覧
    req.setAttribute("personId", personId);  // ★ 現在の対象児
    req.setAttribute("allergenlist", list);
    req.setAttribute("selectedIds", selectedIds);

    req.getRequestDispatcher("/user/AllergyFood.jsp").forward(req, resp);
  }
}
