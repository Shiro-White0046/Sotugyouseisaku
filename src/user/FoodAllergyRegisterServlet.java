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


@WebServlet("/user/allergy/register")
public class FoodAllergyRegisterServlet extends HttpServlet {
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    req.setCharacterEncoding("UTF-8");

    // 1) 同意チェック
    if (!"agree".equals(req.getParameter("consent"))) {
      req.setAttribute("error", "同意が必須です。");
      req.getRequestDispatcher("/WEB-INF/views/user/allergy_confirm.jsp").forward(req, resp);
      return;
    }

    // 2) パラメータ取得
    String[] ids = req.getParameterValues("allergenIds");
    String otherFlag = req.getParameter("allergenOtherFlag");
    String otherName = req.getParameter("allergenOtherName");

    // 3) ログインユーザー
    HttpSession session = req.getSession(false);
    if (session == null) { resp.sendError(HttpServletResponse.SC_UNAUTHORIZED); return; }
    User user = (User) session.getAttribute("user");
    if (user == null || user.getId() == null) { resp.sendError(HttpServletResponse.SC_UNAUTHORIZED); return; }

    // 4) ID配列の正規化
    Set<Short> uniqueIds = new LinkedHashSet<>();
    if (ids != null) {
      for (String s : ids) {
        try { uniqueIds.add(Short.parseShort(s)); } catch (NumberFormatException ignore) {}
      }
    }
    boolean hasOther = "1".equals(otherFlag) || "true".equalsIgnoreCase(otherFlag);
    if (uniqueIds.isEmpty() && !(hasOther && otherName != null && !otherName.trim().isEmpty())) {
      req.setAttribute("error", "登録対象がありません。");
      req.getRequestDispatcher("/WEB-INF/views/user/allergy_confirm.jsp").forward(req, resp);
      return;
    }

    try {
      // 5) 個体(Individual)の特定
    	UUID personId = individual.getId(); // ← individuals.id を person_id として使う
      IndividualDAO individualDAO = new IndividualDAO();
      Individual individual = individualDAO.findById(orgId, individualId) // null返す実装ならnullチェック
      if (individual == null) {
        req.setAttribute("error", "個人情報が未登録です。");
        req.getRequestDispatcher("/WEB-INF/views/user/allergy_confirm.jsp").forward(req, resp);
        return;
      }
      UUID personId = individual.getId(); // ← individuals.id を person_id として使う

      // 6) 登録処理（追加/更新）
      IndividualAllergyDAO iaDao = new IndividualAllergyDAO();
      iaDao.upsertMultiple(personId, uniqueIds, null); // note不要なら null。confirmed_at は now()

      // 7) （必要なら「その他」処理）
      // if (hasOther && otherName != null && !otherName.trim().isEmpty()) {
      //   iaDao.insertOtherName(personId, otherName.trim(), user.getId()); // 任意の拡張
      // }

      // 8) 完了
      session.setAttribute("flash", "アレルギーを登録しました");
      resp.sendRedirect(req.getContextPath() + "/user/home");

    } catch (Exception e) {
      e.printStackTrace();
      req.setAttribute("error", "登録時にエラーが発生しました。もう一度お試しください。");
      req.setAttribute("originalIds", ids == null ? new String[0] : ids);
      req.setAttribute("otherFlag", otherFlag);
      req.setAttribute("otherName", otherName);
      req.getRequestDispatcher("/WEB-INF/views/user/allergy_confirm.jsp").forward(req, resp);
    }
  }
}
