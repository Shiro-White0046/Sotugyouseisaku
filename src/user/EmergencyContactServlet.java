package user;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import bean.Individual;
import bean.User;
import bean.UserContact;
import dao.IndividualDAO;
import dao.UserContactDAO;
import dao.UserDAO;

@WebServlet("/user/emergency")
public class EmergencyContactServlet extends HttpServlet {

  private final UserContactDAO contactDAO = new UserContactDAO();
  private final UserDAO userDAO = new UserDAO();
  private final IndividualDAO individualDAO = new IndividualDAO();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    HttpSession ses = req.getSession(false);
    User user = (ses == null) ? null : (User) ses.getAttribute("user");

    if (user == null) {
      resp.sendRedirect(req.getContextPath() + "/user/login");
      return;
    }

    // 対象児一覧取得
    List<Individual> persons = individualDAO.listByUser(user.getId());
    if (persons.isEmpty()) {
      req.setAttribute("flashMessage", "まずはお子さま（個人）を登録してください。");
      req.getRequestDispatcher("/user/home.jsp").forward(req, resp);
      return;
    }

    // 対象児の決定
    UUID personId = resolvePersonId(req, ses, persons);
    if (ses != null) {
      ses.setAttribute("currentPersonId", personId);
    }
    req.setAttribute("persons", persons);
    req.setAttribute("personId", personId);

    // フラッシュメッセージの転送
    if (ses.getAttribute("flashMessage") != null) {
      req.setAttribute("flashMessage", ses.getAttribute("flashMessage"));
      ses.removeAttribute("flashMessage");
    }

    // 緊急連絡先取得
    UserContact contact = null;
    UUID mainContactId = user.getMainContactId();
    if (mainContactId != null) {
      Optional<UserContact> opt = contactDAO.findById(mainContactId);
      if (opt.isPresent()) contact = opt.get();
    }

    // ★ユーザー情報（メール表示用）
    req.setAttribute("user", user);
    req.setAttribute("contact", contact);

    req.getRequestDispatcher("/user/emergency.jsp").forward(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    req.setCharacterEncoding("UTF-8");

    HttpSession ses = req.getSession(false);
    User user = (ses == null) ? null : (User) ses.getAttribute("user");
    if (user == null) {
      resp.sendRedirect(req.getContextPath() + "/user/login");
      return;
    }

    // 対象児 ID（任意保持）
    String p = req.getParameter("person_id");
    if (p != null && !p.isEmpty() && ses != null) {
      try {
        UUID pid = UUID.fromString(p);
        ses.setAttribute("currentPersonId", pid);
      } catch (Exception ignore) {}
    }

    // フォーム値取得
    String label = req.getParameter("label");
    String phone = req.getParameter("phone");
    String email = req.getParameter("email");  // ← ★追加

    if (label == null || label.trim().isEmpty() ||
        phone == null || phone.trim().isEmpty()) {

      req.setAttribute("error", "ラベルと電話番号は必須です。");
      req.setAttribute("inputLabel", label);
      req.setAttribute("inputPhone", phone);
      req.setAttribute("user", user);
      req.getRequestDispatcher("/user/emergency.jsp").forward(req, resp);
      return;
    }

    label = label.trim();
    phone = phone.trim();
    if (email != null) email = email.trim();

    try {
      UUID contactId;

      // ★EMAIL 更新
      userDAO.updateEmail(user.getId(), email);
      user.setEmail(email);
      ses.setAttribute("user", user);

      // 緊急連絡先の新規または更新
      if (user.getMainContactId() == null) {
        contactId = contactDAO.addOne(user.getId(), label, phone);
        userDAO.updateMainContactId(user.getId(), contactId);

        user.setMainContactId(contactId);
        ses.setAttribute("user", user);

        ses.setAttribute("flashMessage", "緊急連絡先を登録しました。");
      } else {
        contactId = user.getMainContactId();
        contactDAO.update(contactId, label, phone);
        ses.setAttribute("flashMessage", "緊急連絡先を更新しました。");
      }

      resp.sendRedirect(req.getContextPath() + "/user/emergency");

    } catch (Exception e) {
      e.printStackTrace();
      req.setAttribute("error", "登録時にエラーが発生しました。");
      req.setAttribute("inputLabel", label);
      req.setAttribute("inputPhone", phone);
      req.setAttribute("user", user);
      req.getRequestDispatcher("/user/emergency.jsp").forward(req, resp);
    }
  }

  // ★ 対象児決定ロジック
  private static UUID resolvePersonId(HttpServletRequest req, HttpSession ses,
                                      List<Individual> children) {
    UUID personId = null;

    String personParam = req.getParameter("person");
    if (personParam == null || personParam.isEmpty()) {
      personParam = req.getParameter("personId");
    }
    if (personParam != null && !personParam.isEmpty()) {
      try { personId = UUID.fromString(personParam); } catch (Exception ignore) {}
    }

    if (personId == null && ses != null) {
      Object attr = ses.getAttribute("currentPersonId");
      if (attr instanceof UUID) personId = (UUID) attr;
      else if (attr instanceof String) {
        try { personId = UUID.fromString((String) attr); } catch (Exception ignore) {}
      }
    }

    if (personId == null && !children.isEmpty()) {
      personId = children.get(0).getId();
    }

    boolean ok = false;
    for (Individual c : children) {
      if (c.getId().equals(personId)) { ok = true; break; }
    }
    if (!ok && !children.isEmpty()) personId = children.get(0).getId();

    return personId;
  }
}
