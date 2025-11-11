package admin;

import java.io.IOException;
import java.time.YearMonth;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import bean.Administrator;
import bean.Organization;
import dao.MenuDayDAO;

@WebServlet("/admin/menus_new")
public class AdminMenuNewServlet extends HttpServlet {
  private final MenuDayDAO dayDao = new MenuDayDAO();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    HttpSession ses = req.getSession(false);
    Administrator admin = (ses != null) ? (Administrator) ses.getAttribute("admin") : null;
    Organization org    = (ses != null) ? (Organization)  ses.getAttribute("org")   : null;

    // ログインチェック
    if (admin == null || org == null) {
      resp.sendRedirect(req.getContextPath() + "/admin/login");
      return;
    }

    // ym=YYYY-MM（未指定なら今月）
    YearMonth ym;
    try {
      String p = req.getParameter("ym");
      ym = (p == null || p.isEmpty()) ? YearMonth.now() : YearMonth.parse(p);
    } catch (Exception e) {
      ym = YearMonth.now();
    }

    // 当月の「登録済み日付」を取得
    // Map<LocalDate, Boolean> registeredMap = その日が登録済みか
    Map<java.time.LocalDate, Boolean> registeredMap =
        dayDao.existsByMonth(org.getId(), ym);

    req.setAttribute("year", ym.getYear());
    req.setAttribute("month", ym.getMonthValue());
    req.setAttribute("prevYm", ym.minusMonths(1).toString());
    req.setAttribute("nextYm", ym.plusMonths(1).toString());
    req.setAttribute("registeredMap", registeredMap);

    // JSP へフォワード
    req.getRequestDispatcher("/admin/menus_new.jsp").forward(req, resp);
  }
}
