// src/admin/AdminUserListAllergyServlet.java
package admin;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import bean.Administrator;
import bean.User;
import dao.IndividualAllergyDAO;

@WebServlet(urlPatterns = {
    "/admin/allergens"  // ← ここに一本化（JSP側の form もこれに合わせる）
})
public class AdminUserListAllergyServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;
  private final IndividualAllergyDAO iaDao = new IndividualAllergyDAO();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    req.setCharacterEncoding("UTF-8");

    // --- 認証/組織特定（管理者セッション想定） ---
    HttpSession ses = req.getSession(false);
    UUID orgId = null;

    Object adminObj = (ses == null) ? null : firstNonNull(
        ses.getAttribute("loginAdmin"),
        ses.getAttribute("admin"),
        ses.getAttribute("administrator"),
        ses.getAttribute("account")   // 管理者がUserの場合に備える
    );

    if (adminObj instanceof Administrator) {
      orgId = ((Administrator) adminObj).getOrgId();
    } else if (adminObj instanceof User) {
      orgId = ((User) adminObj).getOrgId();
    }

    if (orgId == null) {
      resp.sendRedirect(req.getContextPath() + "/admin/login");
      return;
    }

    // --- 検索条件 ---
    String q  = trimOrEmpty(req.getParameter("q"));   // 名前
    String aq = trimOrEmpty(req.getParameter("aq"));  // アレルギー名

    // --- 集約取得 ---
    List<IndividualAllergyDAO.AllergyView> rows =
        iaDao.aggregateByCategoryWithFilters(orgId, q, aq);

    // --- JSP へ引き渡し ---
    req.setAttribute("q", q);
    req.setAttribute("aq", aq);
    req.setAttribute("rows", rows);

    // あなたの配置に合わせてここは /admin/ の直下を想定
    req.getRequestDispatcher("/admin/admin_user_list_allergy.jsp")
       .forward(req, resp);
  }

  private static String trimOrEmpty(String s) {
    return (s == null) ? "" : s.trim();
  }
  private static Object firstNonNull(Object... arr) {
    for (Object o : arr) if (o != null) return o;
    return null;
  }
}
