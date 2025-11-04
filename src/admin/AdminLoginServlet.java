package admin;

import java.io.IOException;
import java.text.Normalizer;
import java.util.Optional;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import bean.Organization;
import dao.OrganizationDAO;

@WebServlet("/admin/login")
public class AdminLoginServlet extends HttpServlet {

  private static final String VIEW_ORG = "/admin/login_org.jsp";
  private static final String NEXT_CRED = "/admin/login/cred";

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    forward(req, resp, VIEW_ORG);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    req.setCharacterEncoding("UTF-8");

    // 1) 入力取得＆正規化（全角→半角、英字は大文字、空白除去）
    String orgCodeRaw = req.getParameter("orgCode");
    String orgCode = normalizeOrgCode(orgCodeRaw);

    // 2) バリデーション
    if (orgCode == null || orgCode.isEmpty()) {
      req.setAttribute("error", "組織コードを入力してください。");
      req.setAttribute("orgCode", safe(orgCodeRaw)); // 入力値復元用
      forward(req, resp, VIEW_ORG);
      return;
    }
    // 例：英数ハイフンのみ（必要に応じて調整）
    if (!orgCode.matches("^[A-Z0-9\\-]+$")) {
      req.setAttribute("error", "組織コードの形式が正しくありません。");
      req.setAttribute("orgCode", safe(orgCodeRaw));
      forward(req, resp, VIEW_ORG);
      return;
    }

    // 3) 検索
    OrganizationDAO orgDao = new OrganizationDAO();
    Optional<Organization> orgOpt = orgDao.findByCode(orgCode);
    if (!orgOpt.isPresent()) {
      req.setAttribute("error", "組織コードが見つかりません。");
      req.setAttribute("orgCode", safe(orgCodeRaw));
      forward(req, resp, VIEW_ORG);
      return;
    }

    // 4) セッション再生成（セッション固定化対策の小対策）
    HttpSession old = req.getSession(false);
    if (old != null) old.invalidate();
    HttpSession session = req.getSession(true);
    session.setAttribute("org", orgOpt.get());

    // 5) 次のステップへ（POST/Redirect/GET）
    resp.sendRedirect(req.getContextPath() + NEXT_CRED);
  }

  private void forward(HttpServletRequest req, HttpServletResponse resp, String path)
      throws ServletException, IOException {
    RequestDispatcher rd = req.getRequestDispatcher(path);
    rd.forward(req, resp);
  }

  private String normalizeOrgCode(String s) {
    if (s == null) return null;
    // NFKC 正規化 → 全角英数・ハイフン等を半角化、全角スペースも半角へ
    String t = Normalizer.normalize(s, Normalizer.Form.NFKC);
    // 中間に紛れた空白を除去（必要に応じて1つに圧縮でもOK）
    t = t.replaceAll("\\s+", "");
    // 大文字化
    t = t.toUpperCase();
    return t.trim();
  }

  private String safe(String s) {
    // JSPのvalue復元に使うだけなので、null→""だけで十分
    return (s == null) ? "" : s;
  }
}
