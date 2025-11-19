package user;

import java.io.IOException;
import java.util.List;          // ★追加
import java.util.Optional;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import bean.Individual;        // ★追加
import bean.User;
import bean.UserContact;
import dao.IndividualDAO;      // ★追加
import dao.UserContactDAO;
import dao.UserDAO;

@WebServlet("/user/emergency")
public class EmergencyContactServlet extends HttpServlet {

  private final UserContactDAO contactDAO = new UserContactDAO();
  private final UserDAO userDAO = new UserDAO();
  private final IndividualDAO individualDAO = new IndividualDAO();   // ★追加

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    HttpSession ses = req.getSession(false);
    User user = (ses == null) ? null : (User) ses.getAttribute("user");
    if (user == null) {
      resp.sendRedirect(req.getContextPath() + "/user/login");
      return;
    }

    // ★ここから：対象児一覧 ＋ currentPersonId 共通ロジック
    List<Individual> persons = individualDAO.listByUser(user.getId());
    if (persons.isEmpty()) {
      req.setAttribute("flashMessage", "まずはお子さま（個人）を登録してください。");
      req.getRequestDispatcher("/user/home.jsp").forward(req, resp);
      return;
    }

    UUID personId = resolvePersonId(req, ses, persons);
    if (ses != null) {
      ses.setAttribute("currentPersonId", personId);
    }
    req.setAttribute("persons", persons);
    req.setAttribute("personId", personId);
    // ★ここまで

    // フラッシュメッセージ（あれば表示してから消す）
    if (ses.getAttribute("flashMessage") != null) {
      req.setAttribute("flashMessage", ses.getAttribute("flashMessage"));
      ses.removeAttribute("flashMessage");
    }

    // すでに main_contact があれば取得（従来どおり）
    UserContact contact = null;
    UUID mainContactId = user.getMainContactId();
    if (mainContactId != null) {
      Optional<UserContact> opt = contactDAO.findById(mainContactId);
      if (opt.isPresent()) {
        contact = opt.get();
      }
    }

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

    // ★任意：POST 時に hidden の person_id が来ていたら currentPersonId を更新しておく
    String p = req.getParameter("person_id");
    if (p != null && !p.isEmpty() && ses != null) {
      try {
        UUID pid = UUID.fromString(p);
        ses.setAttribute("currentPersonId", pid);
      } catch (Exception ignore) {}
    }
    // ★ここまで任意（なくても動くけど入れておくとより安全）

    String label = req.getParameter("label");
    String phone = req.getParameter("phone");

    // 簡易バリデーション
    if (label == null || label.trim().isEmpty() ||
        phone == null || phone.trim().isEmpty()) {

      req.setAttribute("error", "ラベルと電話番号は必須です。");
      // 再表示用に入力値を渡す
      req.setAttribute("contact", null);
      req.setAttribute("inputLabel", label);
      req.setAttribute("inputPhone", phone);
      req.getRequestDispatcher("/user/emergency.jsp").forward(req, resp);
      return;
    }

    label = label.trim();
    phone = phone.trim();

    try {
      UUID contactId;

      if (user.getMainContactId() == null) {
        // まだ緊急連絡先が無い → 新規作成
        contactId = contactDAO.addOne(user.getId(), label, phone);
        userDAO.updateMainContactId(user.getId(), contactId);

        // セッション上の User も更新
        user.setMainContactId(contactId);
        ses.setAttribute("user", user);

        ses.setAttribute("flashMessage", "緊急連絡先を登録しました。");
      } else {
        // 既に main_contact がある → 更新
        contactId = user.getMainContactId();
        contactDAO.update(contactId, label, phone);
        ses.setAttribute("flashMessage", "緊急連絡先を更新しました。");
      }

      resp.sendRedirect(req.getContextPath() + "/user/emergency");

    } catch (Exception e) {
      e.printStackTrace();
      req.setAttribute("error", "登録時にエラーが発生しました。もう一度お試しください。");
      req.setAttribute("inputLabel", label);
      req.setAttribute("inputPhone", phone);
      req.getRequestDispatcher("/user/emergency.jsp").forward(req, resp);
    }
  }

  // ★共通：対象児決定ロジック
  private static UUID resolvePersonId(HttpServletRequest req, HttpSession ses,
                                      java.util.List<Individual> children) {
    UUID personId = null;

    // ① ?person / ?personId パラメータ最優先
    String personParam = req.getParameter("person");
    if (personParam == null || personParam.isEmpty()) {
      personParam = req.getParameter("personId");
    }
    if (personParam != null && !personParam.isEmpty()) {
      try { personId = UUID.fromString(personParam); } catch (Exception ignore) {}
    }

    // ② セッションの currentPersonId
    if (personId == null && ses != null) {
      Object attr = ses.getAttribute("currentPersonId");
      if (attr instanceof UUID) {
        personId = (UUID) attr;
      } else if (attr instanceof String) {
        try { personId = UUID.fromString((String) attr); } catch (Exception ignore) {}
      }
    }

    // ③ まだ null なら先頭の子
    if (personId == null && !children.isEmpty()) {
      personId = children.get(0).getId();
    }

    // 念のため、一覧に存在するかチェック
    boolean belongs = false;
    for (Individual c : children) {
      if (c.getId().equals(personId)) { belongs = true; break; }
    }
    if (!belongs && !children.isEmpty()) {
      personId = children.get(0).getId();
    }
    return personId;
  }
}
