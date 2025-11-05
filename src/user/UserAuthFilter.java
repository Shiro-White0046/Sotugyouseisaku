package user;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebFilter(urlPatterns = {"/user/*"})
public class UserAuthFilter implements Filter {

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    // 初期化不要なら空でOK
  }

  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
      throws IOException, ServletException {

    HttpServletRequest r = (HttpServletRequest) req;
    HttpServletResponse s = (HttpServletResponse) res;

    // 戻るボタン対策（キャッシュ無効化）
    s.setHeader("Cache-Control","no-cache, no-store, must-revalidate");
    s.setHeader("Pragma","no-cache");
    s.setDateHeader("Expires", 0);

    String ctx = r.getContextPath();
    String uri = r.getRequestURI();

    boolean open =
        uri.equals(ctx + "/user") ||
        uri.equals(ctx + "/user/") ||
        uri.equals(ctx + "/user/index.jsp") ||
        uri.startsWith(ctx + "/user/login") ||
        uri.startsWith(ctx + "/user/login") ||
        uri.startsWith(ctx + "/user/login/cred") ||
        uri.startsWith(ctx + "/user/first-password") ||  // ← 初回PW変更画面を許可
        uri.startsWith(ctx + "/user/logout") ||          // ← 任意で許可
        uri.startsWith(ctx + "/css/") ||
        uri.startsWith(ctx + "/images/");

    if (!open) {
      HttpSession ses = r.getSession(false);
      Object u = (ses == null) ? null : ses.getAttribute("user");
      if (u == null) {
        s.sendRedirect(ctx + "/user/login");
        return;
      }
    }

    chain.doFilter(req, res);
  }

  @Override
  public void destroy() {
    // 後処理不要なら空でOK
  }
}
