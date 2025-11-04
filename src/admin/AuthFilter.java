package admin;

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

@WebFilter(urlPatterns = {"/admin/*"})
public class AuthFilter implements Filter {

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    // 初期化が不要なら空でOK
  }

  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
      throws IOException, ServletException {

    HttpServletRequest request  = (HttpServletRequest) req;
    HttpServletResponse response = (HttpServletResponse) res;

    // 戻るボタン対策（キャッシュ無効）
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    response.setHeader("Pragma", "no-cache");
    response.setDateHeader("Expires", 0);

    String ctx = request.getContextPath();
    String uri = request.getRequestURI();

    boolean open =
         uri.equals(ctx + "/admin")
      || uri.equals(ctx + "/admin/")
      || uri.equals(ctx + "/admin/index.jsp")
      || uri.startsWith(ctx + "/admin/login")
      || uri.startsWith(ctx + "/css/")
      || uri.startsWith(ctx + "/images/");

    if (!open) {
      HttpSession session = request.getSession(false);
      Object admin = (session == null) ? null : session.getAttribute("admin");
      if (admin == null) {
        response.sendRedirect(ctx + "/admin/index.jsp");
        return;
      }
    }
    chain.doFilter(req, res);
  }

  @Override
  public void destroy() {
    // 後片付けが不要なら空でOK
  }
}
