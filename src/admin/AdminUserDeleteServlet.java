// src/admin/AdminUserDeleteServlet.java
package admin;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import bean.Administrator;
import bean.Individual;
import bean.User;
import dao.IndividualDAO;
import dao.UserDAO;
import infra.AuditLogger;   // ★ 追加

@WebServlet("/admin/users/delete")
public class AdminUserDeleteServlet extends HttpServlet {

  // 確認画面表示
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    // ログインチェック（AuthFilterが基本見ますが二重防御）
    HttpSession ses = req.getSession(false);
    Administrator admin = (ses != null) ? (Administrator) ses.getAttribute("admin") : null;
    if (admin == null) { resp.sendRedirect(req.getContextPath()+"/admin/login"); return; }

    String idStr = req.getParameter("id");
    if (idStr == null) { resp.sendRedirect(req.getContextPath()+"/admin/users"); return; }

    UUID individualId;
    try { individualId = UUID.fromString(idStr); }
    catch (IllegalArgumentException ex) {
      resp.sendRedirect(req.getContextPath()+"/admin/users"); return;
    }

    // 個人情報の取得
    Optional<Individual> indOpt = new dao.IndividualDAO().findById(admin.getOrgId(), individualId);
    if (!indOpt.isPresent()) {
      ses.setAttribute("error", "対象の個人が見つかりません。");
      resp.sendRedirect(req.getContextPath()+"/admin/users");
      return;
    }

    Individual ind = indOpt.get();
    // 同一組織ガード
    if (!admin.getOrgId().equals(ind.getOrgId())) {
      ses.setAttribute("error", "他組織のデータにはアクセスできません。");
      resp.sendRedirect(req.getContextPath()+"/admin/users");
      return;
    }

    // 親ユーザー取得（表示用）
    Optional<User> userOpt = new UserDAO().findById(ind.getUserId());
    req.setAttribute("individual", ind);
    req.setAttribute("parentUser", userOpt.orElse(null));

    req.getRequestDispatcher("/admin/user_delete.jsp").forward(req, resp);
  }

  // 実削除
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    HttpSession ses = req.getSession(false);
    Administrator admin = (ses != null) ? (Administrator) ses.getAttribute("admin") : null;
    if (admin == null) { resp.sendRedirect(req.getContextPath()+"/admin/login"); return; }

    req.setCharacterEncoding("UTF-8");
    String idStr = req.getParameter("id");
    if (idStr == null) { resp.sendRedirect(req.getContextPath()+"/admin/users"); return; }

    UUID individualId;
    try { individualId = UUID.fromString(idStr); }
    catch (IllegalArgumentException ex) {
      ses.setAttribute("error", "不正なIDです。");
      resp.sendRedirect(req.getContextPath()+"/admin/users");
      return;
    }

    // 組織チェック（念のため）
    Optional<Individual> indOpt = new dao.IndividualDAO().findById(admin.getOrgId(), individualId);
    if (!indOpt.isPresent() || !admin.getOrgId().equals(indOpt.get().getOrgId())) {
      ses.setAttribute("error", "削除対象が存在しないか、権限がありません。");
      resp.sendRedirect(req.getContextPath()+"/admin/users");
      return;
    }

    // 削除
    new IndividualDAO().delete(individualId);

    // ★ 操作ログ（個人の削除）
    AuditLogger.logAdminFromSession(
        req,
        "delete_individual",
        "individuals",
        individualId.toString()
    );

    ses.setAttribute("flash", "個人「" + indOpt.get().getDisplayName() + "」を削除しました。");
    resp.sendRedirect(req.getContextPath()+"/admin/users");
  }
}
