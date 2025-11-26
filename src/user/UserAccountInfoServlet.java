package user;

import java.io.IOException;
import java.sql.Date;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import bean.IndividualAllergy;
import bean.User;
import dao.AllergenDAO;
import dao.IndividualAllergyDAO;
import dao.IndividualDAO;
import dao.UserDAO;

@WebServlet("/user/account-info")
public class UserAccountInfoServlet extends HttpServlet {

  private final UserDAO userDao = new UserDAO();
  private final IndividualDAO individualDao = new IndividualDAO();
  private final IndividualAllergyDAO iaDao = new IndividualAllergyDAO();
  private final AllergenDAO allergenDao = new AllergenDAO();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    HttpSession ses = req.getSession(false);
    User user = (ses != null) ? (User) ses.getAttribute("user") : null;
    if (user == null) {
      resp.sendRedirect(req.getContextPath() + "/user/login");
      return;
    }

    req.setAttribute("user", user);

    UUID userId = user.getId();
    List<Individual> persons = individualDao.listByUser(userId);
    req.setAttribute("individuals", persons);

    Map<UUID, List<Allergen>> allergyMap = new HashMap<>();
    for (Individual ind : persons) {
      List<Short> ids = iaDao.listByPerson(ind.getId())
          .stream().map(IndividualAllergy::getAllergenId)
          .collect(Collectors.toList());

      List<Allergen> als = ids.isEmpty()
          ? Collections.emptyList()
          : allergenDao.findAllByIds(ids);

      allergyMap.put(ind.getId(), als);
    }

    req.setAttribute("allergyMap", allergyMap);
    req.setAttribute("pageTitle", "アカウント情報");

    req.getRequestDispatcher("/user/account_info.jsp").forward(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    HttpSession ses = req.getSession(false);
    User user = (ses != null) ? (User) ses.getAttribute("user") : null;
    if (user == null) {
      resp.sendRedirect(req.getContextPath() + "/user/login");
      return;
    }

    req.setCharacterEncoding("UTF-8");

    UUID personId = UUID.fromString(req.getParameter("person_id"));
    String birthdayStr = req.getParameter("birthday");
    String note = req.getParameter("note");

    Date birthday = null;
    if (birthdayStr != null && !birthdayStr.isEmpty()) {
      birthday = Date.valueOf(birthdayStr);
    }

    individualDao.updateBasicInfo(personId, birthday, note);

    // 完了メッセージ
    req.getSession().setAttribute("flash", "保存しました。");

    resp.sendRedirect(req.getContextPath() + "/user/account-info");
  }
}
