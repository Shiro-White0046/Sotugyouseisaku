package admin;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import bean.Administrator;
import bean.Organization;

/**
 * アカウント用PDFを案内するページ表示用（管理者）
 * - ヘッダーメニュー「アカウント用紙」から遷移
 * - JSP でPDFへのリンクと簡単な説明を表示する
 */
@WebServlet("/admin/account-pdf")
public class AdminAccountPdfServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    HttpSession ses = req.getSession(false);
    Administrator admin = (ses != null) ? (Administrator) ses.getAttribute("admin") : null;
    Organization org    = (ses != null) ? (Organization) ses.getAttribute("org")   : null;

    if (admin == null || org == null) {
      // 念のためチェック（AuthFilterもある前提）
      resp.sendRedirect(req.getContextPath() + "/admin/login");
      return;
    }

    req.setAttribute("pageTitle", "アカウント用紙（PDFダウンロード）");
    req.getRequestDispatcher("/admin/account_pdf.jsp").forward(req, resp);
  }
}
