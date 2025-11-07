
package admin;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import bean.User;
import dao.IndividualDAO;
import dao.UserDAO;

@WebServlet("/admin/accounts")
public class AdminAccountViewServlet extends HttpServlet {
  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    String idStr = req.getParameter("userId");
    if (idStr == null) { resp.sendRedirect(req.getContextPath()+"/admin/users"); return; }
    UUID userId = UUID.fromString(idStr);

    Optional<User> uOpt = new UserDAO().findById(userId);
    if (!uOpt.isPresent()) {
      req.getSession().setAttribute("error", "アカウントが見つかりません。");
      resp.sendRedirect(req.getContextPath()+"/admin/users");
      return;
    }

    req.setAttribute("user", uOpt.get());
    req.setAttribute("individuals", new IndividualDAO().listByUser(userId));

    // フラッシュ表示
    HttpSession ses = req.getSession(false);
    if (ses != null && ses.getAttribute("flash") != null) {
      req.setAttribute("flash", ses.getAttribute("flash"));
      ses.removeAttribute("flash");
    }

    req.getRequestDispatcher("/admin/account_view.jsp").forward(req, resp);
  }
}
