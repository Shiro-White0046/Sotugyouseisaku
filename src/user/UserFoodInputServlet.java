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
  private static final long serialVersionUID = 1L;

  private final IndividualDAO individualDAO = new IndividualDAO();
  private final AllergenDAO allergenDao = new AllergenDAO();
  private final IndividualAllergyDAO iaDao = new IndividualAllergyDAO();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    HttpSession ses = req.getSession(false);
    User user = (ses != null) ? (User) ses.getAttribute("user") : null;

    if (user == null) {
      // 未ログインならログインページへ
      resp.sendRedirect(req.getContextPath() + "/user/login");
      return;
    }

    // ① この保護者に紐づく個体一覧
    List<Individual> persons = individualDAO.listByUser(user.getId());
    if (persons.isEmpty()) {
      req.setAttribute("flash", "まずはお子さま（個人）を登録してください。");
      req.getRequestDispatcher("/user/home.jsp").forward(req, resp);
      return;
    }

    // ② 対象児の決定（?person > セッション LAST_PERSON_ID > 先頭）
    UUID personId = null;

    String personParam = req.getParameter("person");
    if (personParam != null && !personParam.isEmpty()) {
      try { personId = UUID.fromString(personParam); } catch (Exception ignore) {}
    }

    if (personId == null && ses != null) {
      Object last = ses.getAttribute("LAST_PERSON_ID");
      if (last instanceof UUID) {
        personId = (UUID) last;
      } else if (last instanceof String) {
        try { personId = UUID.fromString((String) last); } catch (Exception ignore) {}
      }
    }

    if (personId == null) {
      personId = persons.get(0).getId();
    }

    // 決定した personId をセッションにも保存（他画面用）
    if (ses != null) {
      ses.setAttribute("currentPersonId", personId);
    }

    // ③ 食物アレルギーマスタ（FOOD）
    List<Allergen> list = allergenDao.listByCategory("FOOD");

    // ④ その子の既存 FOOD アレルギーだけ ID セットにする
    Set<Short> selectedIds = iaDao.listAllergensOfPerson(personId).stream()
        .filter(a -> "FOOD".equalsIgnoreCase(a.getCategory()))
        .map(Allergen::getId)
        .collect(Collectors.toCollection(LinkedHashSet::new));

    // JSP に渡す
    req.setAttribute("persons", persons);
    req.setAttribute("personId", personId);
    req.setAttribute("allergenlist", list);
    req.setAttribute("selectedIds", selectedIds);

    // ⑤ JSPへフォワード
    req.getRequestDispatcher("/user/AllergyFood.jsp").forward(req, resp);
  }
}
