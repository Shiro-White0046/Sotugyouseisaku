package admin;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import bean.Administrator;
import bean.Individual;
import bean.Organization;
import dao.IndividualDAO;

@WebServlet("/admin/auth/verify")
public class AdminAuthVerifyServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  private final IndividualDAO iDao = new IndividualDAO();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    HttpSession ses = req.getSession(false);
    Administrator admin = (ses != null) ? (Administrator) ses.getAttribute("admin") : null;
    Organization org    = (ses != null) ? (Organization)  ses.getAttribute("org")   : null;
    if (admin == null || org == null) {
      resp.sendRedirect(req.getContextPath() + "/admin/login");
      return;
    }

    String idStr = req.getParameter("id");
    if (idStr == null || idStr.isEmpty()) {
      resp.sendRedirect(req.getContextPath() + "/admin/auth");
      return;
    }

    UUID personId;
    try {
      personId = UUID.fromString(idStr);
    } catch (IllegalArgumentException e) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    // org.getId() で統一
    Individual person = iDao.findOneByOrgIdAndPersonId(org.getId(), personId);
    if (person == null) {
      resp.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }

    req.setAttribute("person", person);
    req.getRequestDispatcher("/admin/auth_verify.jsp").forward(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    req.setCharacterEncoding("UTF-8");
    HttpSession ses = req.getSession(false);
    Administrator admin = (ses != null) ? (Administrator) ses.getAttribute("admin") : null;
    Organization org    = (ses != null) ? (Organization)  ses.getAttribute("org")   : null;
    if (admin == null || org == null) {
      resp.sendRedirect(req.getContextPath() + "/admin/login");
      return;
    }

    String idStr = req.getParameter("id");
    String pin   = req.getParameter("pin");
    if (idStr == null || pin == null) {
      resp.sendRedirect(req.getContextPath() + "/admin/auth");
      return;
    }

    UUID personId = UUID.fromString(idStr);
    Individual person = iDao.findOneByOrgIdAndPersonId(org.getId(), personId);
    if (person == null) {
      resp.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }

    // ==== 1日3回まで（朝・昼・夜それぞれ1回）の制限 ====
    // 日本時間(JST, +09:00)で「今日」「現在時刻」を判定
    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.ofHours(9));
    LocalDate today = now.toLocalDate();
    LocalTime nowTime = now.toLocalTime();

    // 現在時刻がどの時間帯か
    String nowSlot = judgeSlot(nowTime);
    if (nowSlot == null) {
      // 時間帯外のときはエラー扱いにする
      req.setAttribute("person", person);
      req.setAttribute("error",
          "認証できるのは 朝(6～11時未満)・昼(11～17時未満)・夜(17～21時未満) の時間帯のみです。");
      req.getRequestDispatcher("/admin/auth_verify.jsp").forward(req, resp);
      return;
    }

    // 直近の認証が同じ日・同じ時間帯なら NG
    OffsetDateTime last = person.getLastVerifiedAt(); // Individual に getter がある前提
    if (last != null) {
      // 保存されている時刻を日本時間に変換
      OffsetDateTime lastJst = last.withOffsetSameInstant(ZoneOffset.ofHours(9));
      if (lastJst.toLocalDate().isEqual(today)) {
        String lastSlot = judgeSlot(lastJst.toLocalTime());
        if (nowSlot.equals(lastSlot)) {
          req.setAttribute("person", person);
          req.setAttribute("error",
              "本日の「" +
              ("MORNING".equals(nowSlot) ? "朝" :
               "NOON".equals(nowSlot)    ? "昼" : "夜") +
              "」の認証はすでに完了しています。");
          req.getRequestDispatcher("/admin/auth_verify.jsp").forward(req, resp);
          return;
        }
      }
    }
    // ==== ここまで制限ロジック ====

    // PIN形式チェック
    pin = pin.trim();
    if (!pin.matches("\\d{4}")) {
      req.setAttribute("person", person);
      req.setAttribute("error", "パスワードは4桁の数字で入力してください。");
      req.getRequestDispatcher("/admin/auth_verify.jsp").forward(req, resp);
      return;
    }

    // PIN照合（orgId:userId:pin の SHA-256）
    boolean ok = verifyPin(pin, person.getPinCodeHash(), org.getId(), person.getUserId());
    if (ok) {
      // 認証時刻を記録（last_verified_at を更新） ※ now は JST
      iDao.updateLastVerifiedAt(person.getId(), now);

      ses.setAttribute("flashMessage", person.getDisplayName() + " を認証しました。");
      resp.sendRedirect(req.getContextPath() + "/admin/auth");
    } else {
      req.setAttribute("person", person);
      req.setAttribute("error", "パスワードが一致しません。");
      req.getRequestDispatcher("/admin/auth_verify.jsp").forward(req, resp);
    }
  }

  /** 利用者側と同じロジック（orgId:userId:pin）で照合 */
  private boolean verifyPin(String pin, String hash, UUID orgId, UUID userId) {
    if (hash == null || hash.isEmpty()) return false;
    String h = hash.trim();
    try {
      if (!h.matches("[0-9a-fA-F]{64}")) return false;

      String src = orgId.toString() + ":" + userId.toString() + ":" + pin;
      java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
      byte[] digest = md.digest(src.getBytes(java.nio.charset.StandardCharsets.UTF_8));
      StringBuilder sb = new StringBuilder(digest.length * 2);
      for (byte b : digest) sb.append(String.format("%02x", b));
      String calc = sb.toString();

      System.out.println("比較対象 src=" + src);
      System.out.println("計算結果=" + calc);
      System.out.println("DBハッシュ=" + h);

      return h.equalsIgnoreCase(calc);
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * 時間帯判定
   * 朝 : 6:00–10:59
   * 昼 : 11:00–16:59
   * 夜 : 17:00–20:59
   * それ以外は null
   */
  private static String judgeSlot(LocalTime t) {
    LocalTime mStart = LocalTime.of(6, 0);
    LocalTime mEnd   = LocalTime.of(11, 0);  // 11:00未満 → 朝

    LocalTime nStart = LocalTime.of(11, 0);
    LocalTime nEnd   = LocalTime.of(17, 0);  // 17:00未満 → 昼

    LocalTime eStart = LocalTime.of(17, 0);
    LocalTime eEnd   = LocalTime.of(21, 0);  // 21:00未満 → 夜

    if (!t.isBefore(mStart) && t.isBefore(mEnd)) {
      return "MORNING";   // 06:00〜10:59
    } else if (!t.isBefore(nStart) && t.isBefore(nEnd)) {
      return "NOON";      // 11:00〜16:59
    } else if (!t.isBefore(eStart) && t.isBefore(eEnd)) {
      return "NIGHT";     // 17:00〜20:59
    } else {
      return null;
    }
  }
}