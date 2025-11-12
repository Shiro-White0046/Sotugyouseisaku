package user;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
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

    Individual ind = individualDAO.findOneByUserId(user.getOrgId(), user.getId());
    req.setAttribute("individual", ind);
    req.setAttribute("pageTitle", "認証パスワード設定");

    // ★JSPの実在場所に合わせて片方だけ使う
    // 推奨: /WEB-INF 配下
   // req.getRequestDispatcher("/WEB-INF/views/user/Pin.jsp").forward(req, resp);
    // もし /user/Pin.jsp に置いているなら上をコメントアウトして↓を有効化
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
    if (pin == null || !pin.matches("\\d{4}")) { // Java側は \\d
      req.setAttribute("error", "認証パスワードは数字4桁で入力してください。");
      doGet(req, resp);
      return;
    }

    Individual ind = individualDAO.findOneByUserId(user.getOrgId(), user.getId());
    if (ind == null) {
      req.setAttribute("error", "利用者情報が見つかりません。");
      doGet(req, resp);
      return;
    }

    // --- ここでハッシュ化して保存 ---
    String hash = hashPin(user.getOrgId(), user.getId(), pin);
    ind.setPinCodeHash(hash);              // Individual に対応する setter を用意
    individualDAO.updatePinHash(ind);      // DAO 側も pin_code_hash を更新するメソッド名に

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
}
