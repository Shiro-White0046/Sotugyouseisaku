// src/admin/AdminUserListAllergyServlet.java
package admin;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import bean.Administrator;
import bean.Allergen;
import dao.AllergenDAO;
import infra.AuditLogger;   // ★ 追加


@WebServlet("/admin/allergens-master")
public class AdminAllergenServlet extends HttpServlet {

  private final AllergenDAO allergenDAO = new AllergenDAO();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    HttpSession ses = req.getSession(false);
    Administrator admin = (ses != null) ? (Administrator) ses.getAttribute("admin") : null;

    if (admin == null) {
      resp.sendRedirect(req.getContextPath() + "/admin/login");
      return;
    }
    // ★ フラッシュメッセージの受け渡し
    if (ses != null) {
      String flash = (String) ses.getAttribute("flashMessage");
      if (flash != null) {
        req.setAttribute("flashMessage", flash);
        ses.removeAttribute("flashMessage");
      }
    }

    String q = trim(req.getParameter("q"));

    List<Allergen> list = allergenDAO.searchForAdmin(q);
    req.setAttribute("allergens", list);
    req.setAttribute("count", list.size());  // 件数をセット

    req.setAttribute("q", q);

    req.getRequestDispatcher("/admin/admin_allergens.jsp").forward(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    HttpSession ses = req.getSession(false);
    Administrator admin = (ses != null) ? (Administrator) ses.getAttribute("admin") : null;

    if (admin == null) {
      resp.sendRedirect(req.getContextPath() + "/admin/login");
      return;
    }

    req.setCharacterEncoding("UTF-8");
    String name = trim(req.getParameter("name"));
    String cat  = trim(req.getParameter("category"));
    String sub  = trim(req.getParameter("subCategory"));

    // ① 追加試行（戻り値で重複判定）
    boolean inserted = allergenDAO.insertForAdmin(name, cat, sub);

    if (!inserted) {
      // ② 同名があった場合 → エラーメッセージを出して再表示
      req.setAttribute("error", "同じ名前のアレルギーが既に登録されています。");

      // 再描画用に一覧も再取得
      List<Allergen> list = allergenDAO.searchForAdmin("");
      req.setAttribute("allergens", list);

      req.getRequestDispatcher("/admin/admin_allergens.jsp").forward(req, resp);
      return;
    }

    // ★ 操作ログ（アレルギー項目の追加）
    // ID はここでは取れないので、ひとまず name を entityId に入れておく
    AuditLogger.logAdminFromSession(
        req,
        "create_allergen",
        "allergens",
        name
    );

    // ★ 登録成功時にフラッシュメッセージをセット
    HttpSession ses2 = req.getSession();
    ses2.setAttribute("flashMessage", "アレルギー項目を登録しました。");

    // ③ 正常登録 → 一覧へリダイレクト
    resp.sendRedirect(req.getContextPath() + "/admin/allergens-master");
  }

  private static String trim(String s) {
    return (s == null) ? "" : s.trim();
  }
}
