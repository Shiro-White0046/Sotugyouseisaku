package admin;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ThreadLocalRandom;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import bean.Administrator;
import bean.Organization;
import dao.AccountTokenDAO;
import dao.AdminDAO;
import infra.DummyEmailService;
import infra.EmailService;
import infra.Password;


@WebServlet(urlPatterns = {"/admin/register/form"})
public class AdminRegisterFormServlet extends HttpServlet {
  private final EmailService email = new DummyEmailService(); // 本番は実装差替

  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    req.getRequestDispatcher("/admin/register_form.jsp").forward(req, resp);
  }

  @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    req.setCharacterEncoding("UTF-8");
    HttpSession ses = req.getSession(false);
    Organization org = (ses==null) ? null : (Organization) ses.getAttribute("org");
    if (org==null) { resp.sendRedirect(req.getContextPath()+"/admin/register"); return; }

    String name = req.getParameter("name");
    String emailAddr = emptyToNull(req.getParameter("email"));
    String pw = req.getParameter("password");
    String pw2 = req.getParameter("passwordConfirm");

    if (isBlank(name) || isBlank(pw) || isBlank(pw2)) {
      req.setAttribute("error","未入力の項目があります。"); doGet(req, resp); return;
    }
    if (!pw.equals(pw2)) {
      req.setAttribute("error","確認用パスワードが一致しません。"); doGet(req, resp); return;
    }
    if (!pw.matches("^(?=\\S+$)(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,16}$")) {
      req.setAttribute("error","パスワード条件を満たしていません。"); doGet(req, resp); return;
    }

    try {
      AdminDAO adminDao = new AdminDAO();
      String hash = Password.hash(pw);
      // is_active=false で「仮登録」。トリガで admin_no は採番される
      Administrator created = adminDao.create(org.getId(), emailAddr, hash, name, /*active*/ false);

      // 6桁パスコードを生成（000000〜999999→ゼロ埋め6桁）
      String code = String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1_000_000));
      Instant expires = Instant.now().plus(30, ChronoUnit.MINUTES);

      AccountTokenDAO tokenDao = new AccountTokenDAO();
      tokenDao.issue(org.getId(), "admin", created.getId(), code, expires);

      // メール送信（ダミー：コンソール出力 / 本番はSMTP）
      if (emailAddr != null) {
        email.send(emailAddr, "【アレルギー対策】登録用パスコード",
          "パスコードは「" + code + "」です（30分間有効）。\n誤送信の場合は破棄してください。");
      }

      // 検証画面へ（セッションにadminId保存）
      ses.setAttribute("registerAdminId", created.getId());
      resp.sendRedirect(req.getContextPath()+"/admin/register/verify");
    } catch (Exception e) {
      req.setAttribute("error", "登録に失敗しました：" + e.getMessage());
      doGet(req, resp);
    }
  }

  private boolean isBlank(String s){ return s==null || s.trim().isEmpty(); }
  private String emptyToNull(String s){ return (s==null || s.trim().isEmpty()) ? null : s.trim(); }
}
