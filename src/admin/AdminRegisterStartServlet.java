package admin;

import java.io.IOException;
import java.text.Normalizer;
import java.util.Optional;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import bean.Organization;
import dao.OrganizationDAO;

@WebServlet(urlPatterns = {"/admin/register"})
public class AdminRegisterStartServlet extends HttpServlet {
  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    req.getRequestDispatcher("/admin/register.jsp").forward(req, resp);
  }

  @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    req.setCharacterEncoding("UTF-8");
    String raw = req.getParameter("orgCode");
    String code = normalize(raw);
    if (code==null || code.isEmpty()) {
      req.setAttribute("error", "組織コードを入力してください。");
      doGet(req, resp); return;
    }
    OrganizationDAO dao = new OrganizationDAO();
    Optional<Organization> orgOpt = dao.findByCode(code);
    if (!orgOpt.isPresent()) {
      req.setAttribute("error", "組織コードが見つかりません。");
      doGet(req, resp); return;
    }
    // 既存セッション破棄→新規（固定化対策の小ワンポイント）
    HttpSession old = req.getSession(false); if (old!=null) old.invalidate();
    HttpSession ses = req.getSession(true);
    ses.setAttribute("org", orgOpt.get());
    resp.sendRedirect(req.getContextPath() + "/admin/register/form");
  }

  private String normalize(String s) {
    if (s==null) return null;
    String t = Normalizer.normalize(s, Normalizer.Form.NFKC);
    t = t.replaceAll("\\s+","").toUpperCase();
    return t.trim();
  }
}
