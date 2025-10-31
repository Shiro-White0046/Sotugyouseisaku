package admin;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import bean.Administrator;
import bean.Menu;
import bean.Organization;
import dao.MenuDAO;

@WebServlet("/admin/home")
public class AdminDashboardServlet extends HttpServlet {
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    // 未ログインならログインへ
    HttpSession session = req.getSession(false);
    Administrator admin = (session != null) ? (Administrator) session.getAttribute("admin") : null;
    Organization org = (session != null) ? (Organization) session.getAttribute("org") : null;
    if (admin == null || org == null) {
      resp.sendRedirect(req.getContextPath() + "/admin/login");
      return;
    }

    // 今日の献立（公開分のみ）を取得してJSPへ渡す（任意）
    try {
      MenuDAO menuDao = new MenuDAO();
      Optional<Menu> today = menuDao.findToday((UUID) org.getId(), true);
      today.ifPresent(m -> req.setAttribute("todayMenu", m));
    } catch (Exception ignore) {}

    RequestDispatcher rd = req.getRequestDispatcher("/admin/home.jsp");
    rd.forward(req, resp);
  }
}
