package admin;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet(urlPatterns = {"/admin/users/register/done"})
public class AdminUserRegisterDoneServlet extends HttpServlet {
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    HttpSession ses = req.getSession(false);
    if (ses == null || ses.getAttribute("createdUser") == null) {
      resp.sendRedirect(req.getContextPath()+"/admin/users/register");
      return;
    }
    req.getRequestDispatcher("/admin/user_register_done.jsp").forward(req, resp);
  }
}
