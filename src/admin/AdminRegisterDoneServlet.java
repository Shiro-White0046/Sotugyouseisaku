package admin;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet(urlPatterns = {"/admin/register/done"})
public class AdminRegisterDoneServlet extends HttpServlet {
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    HttpSession ses = req.getSession(false);
    // 登録完了情報が無ければ最初から
    if (ses == null || ses.getAttribute("createdAdmin") == null) {
      resp.sendRedirect(req.getContextPath() + "/admin/register");
      return;
    }

    // そのまま完了JSPへ
    req.getRequestDispatcher("/admin/register_done.jsp").forward(req, resp);
  }
}
