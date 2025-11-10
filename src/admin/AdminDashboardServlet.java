package admin;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import bean.Administrator;
import bean.MenuDay;
import bean.Organization;
import dao.MenuDayDAO;

/**
 * 管理者ホーム画面（トップダッシュボード）
 * - 今日の日付の献立（公開中のみ）を表示
 */
@WebServlet("/admin/home")
public class AdminDashboardServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    // セッション確認（未ログインならログイン画面へ）
    HttpSession session = req.getSession(false);
    Administrator admin = (session != null) ? (Administrator) session.getAttribute("admin") : null;
    Organization org = (session != null) ? (Organization) session.getAttribute("org") : null;
    if (admin == null || org == null) {
      resp.sendRedirect(req.getContextPath() + "/admin/login");
      return;
    }

    // 今日の日付
    LocalDate today = LocalDate.now();

    // 今日の献立（menu_days）を取得
    try {
      MenuDayDAO dayDao = new MenuDayDAO();
      Optional<MenuDay> opt = dayDao.findByDate(org.getId(), today);

      if (opt.isPresent()) {
        MenuDay menuDay = opt.get();
        // 公開中のみ表示
        if (menuDay.isPublished()) {
          req.setAttribute("todayMenu", menuDay);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    RequestDispatcher rd = req.getRequestDispatcher("/admin/home.jsp");
    rd.forward(req, resp);
  }
}
