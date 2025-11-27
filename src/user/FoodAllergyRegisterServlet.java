package user;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import bean.Individual;
import bean.User;
import dao.IndividualAllergyDAO;
import dao.IndividualDAO;
import infra.AuditLogger;   // ★ 追加

@WebServlet("/user/allergy/register")
public class FoodAllergyRegisterServlet extends HttpServlet {

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    req.setCharacterEncoding("UTF-8");

    // 1) 同意チェック
    if (!"agree".equals(req.getParameter("consent"))) {
      req.setAttribute("error", "同意が必須です。");
      req.getRequestDispatcher("/user/allergy_confirm.jsp")
         .forward(req, resp);
      return;
    }

    // 2) パラメータ取得
    String[] ids      = req.getParameterValues("allergenIds");
    String otherFlag  = req.getParameter("allergenOtherFlag");
    String otherName  = req.getParameter("allergenOtherName");
    String personStr  = req.getParameter("person_id");   // ★ 追加：どの子か

    // 3) ログインユーザー
    HttpSession session = req.getSession(false);
    if (session == null) {
      resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }
    User user = (User) session.getAttribute("user");
    if (user == null || user.getId() == null) {
      resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    // 4) 対象児(person_id) の決定＆妥当性チェック
    UUID personId = null;
    try {
      if (personStr != null && !personStr.isEmpty()) {
        personId = UUID.fromString(personStr);
      }
    } catch (IllegalArgumentException e) {
      personId = null;
    }

    if (personId == null) {
      req.setAttribute("error", "対象の子どもが指定されていません。");
      req.getRequestDispatcher("/user/allergy_confirm.jsp")
         .forward(req, resp);
      return;
    }

    // 念のため「このユーザーの子どもか」チェック
    IndividualDAO iDao = new IndividualDAO();
    Individual ind = iDao.findById(user.getOrgId(), personId)
                         .orElse(null);
    if (ind == null || !user.getId().equals(ind.getUserId())) {
      req.setAttribute("error", "対象の子どもを確認できません。");
      req.getRequestDispatcher("/user/allergy_confirm.jsp")
         .forward(req, resp);
      return;
    }

    // 5) ID配列の正規化
    Set<Short> uniqueIds = new LinkedHashSet<Short>();
    if (ids != null) {
      for (String s : ids) {
        try { uniqueIds.add(Short.parseShort(s)); }
        catch (NumberFormatException ignore) {}
      }
    }
    boolean hasOther = "1".equals(otherFlag)
        || "true".equalsIgnoreCase(otherFlag);

    if (uniqueIds.isEmpty()
        && !(hasOther && otherName != null && !otherName.trim().isEmpty())) {

      req.setAttribute("error", "登録対象がありません。");
      req.getRequestDispatcher("/user/allergy_confirm.jsp")
         .forward(req, resp);
      return;
    }

    try {
      IndividualAllergyDAO iaDao = new IndividualAllergyDAO();

      // ★ FOODカテゴリだけ消して入れ直し
      boolean existed = iaDao.existsByCategory(personId, "FOOD");
      iaDao.deleteByCategory(personId, "FOOD");
      iaDao.upsertMultiple(personId, uniqueIds, null);

      if (existed) {
        session.setAttribute("flashMessage", "食物性アレルギー登録を更新しました");
      } else {
        session.setAttribute("flashMessage", "食物性アレルギーを登録しました");
      }

      // ★ 操作ログ（食物アレルギー更新）
      AuditLogger.logGuardianFromSession(
          req,
          "update_allergy",
          "individual_allergies",
          personId.toString()
      );

      resp.sendRedirect(req.getContextPath() + "/user/home");

    } catch (Exception e) {
      e.printStackTrace();
      req.setAttribute("error", "登録時にエラーが発生しました。もう一度お試しください。");
      req.setAttribute("originalIds", ids == null ? new String[0] : ids);
      req.setAttribute("otherFlag", otherFlag);
      req.setAttribute("otherName", otherName);
      req.getRequestDispatcher("/user/allergy_confirm.jsp")
         .forward(req, resp);
    }
  }
}
