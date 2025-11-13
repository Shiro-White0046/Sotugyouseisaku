// src/admin/AdminAuthVerifyServlet.java
package admin;

import java.io.IOException;
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

    // ★ GET/POSTとも org.getId() で統一
    Individual person = iDao.findOneByOrgIdAndPersonId(personId);
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

    UUID personId;
    try {
      personId = UUID.fromString(idStr);
    } catch (IllegalArgumentException e) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    Individual person = iDao.findOneByOrgIdAndPersonId(org.getId(), personId);
    if (person == null) {
      resp.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }

    System.out.println("入力PIN=" + pin);
    System.out.println("DBのpinCodeHash=" + person.getPinCodeHash());

    pin = pin.trim();
    if (!pin.matches("\\d{4}")) {              // 4桁数値を要求（JSPのpatternとも一致）
      req.setAttribute("person", person);
      req.setAttribute("error", "パスワードは4桁の数字で入力してください。");
      req.getRequestDispatcher("/admin/auth_verify.jsp").forward(req, resp);
      return;
    }

    boolean ok = verifyPin(pin, person.getPinCodeHash(), org.getId(), person.getUserId());
    if (ok) {
      req.getSession().setAttribute("flashMessage", person.getDisplayName() + " を認証しました。");
      resp.sendRedirect(req.getContextPath() + "/admin/auth");
    } else {
      req.setAttribute("person", person);
      req.setAttribute("error", "パスワードが一致しません。");
      req.getRequestDispatcher("/admin/auth_verify.jsp").forward(req, resp);
    }
  }

  /** 平文 or BCrypt($2a/$2b/$2y) どちらでも判定できるユーティリティ */
  /** 平文 / BCrypt / SHA-256 のいずれでも照合できるユーティリティ */
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

  /** SHA-256 ハッシュ生成 */
  private String sha256(String input) {
    try {
      java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
      byte[] bytes = md.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
      StringBuilder sb = new StringBuilder();
      for (byte b : bytes) sb.append(String.format("%02x", b));
      return sb.toString();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }




}
