package user;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import bean.User;
import bean.UserContact;
import dao.UserContactDAO;
import dao.UserDAO;

@WebServlet("/user/emergency")
public class EmergencyContactServlet extends HttpServlet {

  private final UserContactDAO contactDAO = new UserContactDAO();
  private final UserDAO userDAO = new UserDAO();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    HttpSession ses = req.getSession(false);
    User user = (ses == null) ? null : (User) ses.getAttribute("user");
    if (user == null) {
      resp.sendRedirect(req.getContextPath() + "/user/login");
      return;
    }

    // フラッシュメッセージ（あれば表示してから消す）
    if (ses.getAttribute("flashMessage") != null) {
      req.setAttribute("flashMessage", ses.getAttribute("flashMessage"));
      ses.removeAttribute("flashMessage");
    }

    // すでに main_contact があれば取得
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
}
