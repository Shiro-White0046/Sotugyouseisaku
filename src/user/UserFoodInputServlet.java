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
      // 未ログインならログインページへ
      resp.sendRedirect(req.getContextPath() + "/user/login");
      return;
    }

    // ---- ① 個体(お子さん) を取得（FoodAllergyRegisterServlet と同じロジックに揃える） ----
    UUID orgId  = user.getOrgId();
    UUID userId = user.getId();

    IndividualDAO individualDAO = new IndividualDAO();
    Individual person = individualDAO.findOneByUserId(orgId, userId);
    if (person == null) {
      req.setAttribute("flash", "まずはお子さま（個人）を登録してください。");
      req.getRequestDispatcher("/user/home.jsp").forward(req, resp);
      return;
    }
    UUID personId = person.getId();

    // ---- ② 食物アレルギーマスタ（FOOD） ----
    AllergenDAO allergenDao = new AllergenDAO();
    List<Allergen> list = allergenDao.listByCategory("FOOD");
    req.setAttribute("allergenlist", list);

    // ---- ③ その子に紐づく既存アレルギーのうち、FOODカテゴリだけを ID セットにする ----
    IndividualAllergyDAO iaDao = new IndividualAllergyDAO();
    Set<Short> selectedIds = iaDao.listAllergensOfPerson(personId).stream()
        .filter(a -> "FOOD".equalsIgnoreCase(a.getCategory()))
        .map(Allergen::getId)
        .collect(Collectors.toCollection(LinkedHashSet::new));

    // JSP で事前チェックに使う
    req.setAttribute("selectedIds", selectedIds);

    // ---- ④ JSPへフォワード ----
    req.getRequestDispatcher("/user/AllergyFood.jsp").forward(req, resp);
  }
}
