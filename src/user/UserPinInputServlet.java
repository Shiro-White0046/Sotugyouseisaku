package user;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;      // ★追加
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import bean.Individual;
import bean.User;
import dao.IndividualDAO;
import infra.AuditLogger;   // ★ 追加

@WebServlet("/user/pin")
public class UserPinInputServlet extends HttpServlet {
  private final IndividualDAO individualDAO = new IndividualDAO();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    HttpSession ses = req.getSession(false);
    User user = (ses != null) ? (User) ses.getAttribute("user") : null;
    if (user == null) {
      resp.sendRedirect(req.getContextPath() + "/user/login");
      return;
    }

    // ★ここから：複数子ども対応 ＋ currentPersonId 共通ロジック
    List<Individual> persons = individualDAO.listByUser(user.getId());
    if (persons.isEmpty()) {
      req.setAttribute("pageTitle", "認証パスワード設定");
      req.setAttribute("error", "まずはお子さま（個人）を登録してください。");
      req.getRequestDispatcher("/user/Pin.jsp").forward(req, resp);
      return;
    }

    UUID personId = resolvePersonId(req, ses, persons);
    if (ses != null) {
      ses.setAttribute("currentPersonId", personId);
    }
    req.setAttribute("persons", persons);
    req.setAttribute("personId", personId);

    // 選択中の子ども
    Individual ind = null;
    for (Individual p : persons) {
      if (p.getId().equals(personId)) { ind = p; break; }
    }
    req.setAttribute("individual", ind);
    // ★ここまで

    req.setAttribute("pageTitle", "認証パスワード設定");

    // JSP のパスはすでにここに合わせているのでそのまま
    req.getRequestDispatcher("/user/Pin.jsp").forward(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    req.setCharacterEncoding("UTF-8");
    HttpSession ses = req.getSession(false);
    User user = (ses != null) ? (User) ses.getAttribute("user") : null;
    if (user == null) {
      resp.sendRedirect(req.getContextPath() + "/user/login");
      return;
    }

    String pin = req.getParameter("pin");
    if (pin == null || !pin.matches("\\d{4}")) { // Java側は \\d でOK
      req.setAttribute("error", "認証パスワードは数字4桁で入力してください。");
      doGet(req, resp);
      return;
    }

    // ★どの子のPINか（hiddenから取得）
    String p = req.getParameter("person_id");
    UUID personId = null;
    try {
      if (p != null && !p.isEmpty()) {
        personId = UUID.fromString(p);
      }
    } catch (Exception ignore) {}

    if (personId == null) {
      req.setAttribute("error", "対象の子どもが指定されていません。");
      doGet(req, resp);
      return;
    }

    // 念のため「このユーザーの子どもか」確認
    Individual ind = individualDAO.findById(user.getOrgId(), personId)
                                  .orElse(null);
    if (ind == null || !user.getId().equals(ind.getUserId())) {
      req.setAttribute("error", "対象の子どもを確認できません。");
      doGet(req, resp);
      return;
    }

    // ★ currentPersonId を更新しておく
    if (ses != null) {
      ses.setAttribute("currentPersonId", personId);
    }

    // --- ここでハッシュ化して保存（従来どおり） ---
    String hash = hashPin(user.getOrgId(), user.getId(), pin);
    ind.setPinCodeHash(hash);              // Individual に対応する setter を用意
    individualDAO.updatePinHash(ind);      // DAO 側も pin_code_hash を更新するメソッド

    // ★ 操作ログ（認証パスワード更新）
    AuditLogger.logGuardianFromSession(
        req,
        "update_pin",
        "individuals",
        personId.toString()
    );

    ses.setAttribute("flashMessage", "認証パスワードを設定しました。");
    resp.sendRedirect(req.getContextPath() + "/user/home");
  }

  /** 簡易SHA-256（orgId:userId:pin を結合してハッシュ） */
  private static String hashPin(UUID orgId, UUID userId, String pin) {
    try {
      String src = orgId.toString() + ":" + userId.toString() + ":" + pin;
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] digest = md.digest(src.getBytes(StandardCharsets.UTF_8));
      StringBuilder sb = new StringBuilder(digest.length * 2);
      for (byte b : digest) sb.append(String.format("%02x", b));
      return sb.toString();
    } catch (Exception e) {
      throw new RuntimeException("PINハッシュ化に失敗", e);
    }
  }

  // ★共通：対象児決定ロジック
  private static UUID resolvePersonId(HttpServletRequest req, HttpSession ses,
                                      java.util.List<Individual> children) {
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
      if (attr instanceof UUID) {
        personId = (UUID) attr;
      } else if (attr instanceof String) {
        try { personId = UUID.fromString((String) attr); } catch (Exception ignore) {}
      }
    }

    if (personId == null && !children.isEmpty()) {
      personId = children.get(0).getId();
    }

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
