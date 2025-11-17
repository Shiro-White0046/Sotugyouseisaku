package user;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import bean.Individual;
import bean.User;
import dao.IndividualDAO;

@WebServlet("/user/menuscalendar")
public class MenusCalendar extends HttpServlet {
  private static final long serialVersionUID = 1L;

  private final IndividualDAO individualDao = new IndividualDAO();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    // 認証
    HttpSession ses = req.getSession(false);
    User loginUser = (ses == null) ? null : (User) firstNonNull(
        ses.getAttribute("loginUser"),
        ses.getAttribute("user"),
        ses.getAttribute("account"));
    if (loginUser == null) {
      resp.sendRedirect(req.getContextPath() + "/auth/login");
      return;
    }

    // 子ども一覧
    List<Individual> children = individualDao.listByUser(loginUser.getId());
    req.setAttribute("children", children);

    // personId 決定（パラメータ優先、無ければ先頭）
    UUID personId = parseUUID(req.getParameter("personId"));
    boolean belongs = false;
    if (personId != null && children != null) {
      for (Individual c : children) {
        if (c.getId().equals(personId)) { belongs = true; break; }
      }
    }
    if (!belongs) {
      personId = (children != null && !children.isEmpty()) ? children.get(0).getId() : null;
    }
    req.setAttribute("personId", personId);

    // ===== ここから先は、元のカレンダーロジックをそのまま使ってOK =====
    // ym=YYYY-MM
    YearMonth ym;
    try {
      String ymStr = req.getParameter("ym");
      ym = (ymStr == null || ymStr.isEmpty())
          ? YearMonth.now()
          : YearMonth.parse(ymStr);
    } catch (Exception e) {
      ym = YearMonth.now();
    }

    LocalDate first = ym.atDay(1);
    int firstDow = first.getDayOfWeek().getValue() % 7; // 日(0)〜土(6)
    int daysInMonth = ym.lengthOfMonth();

    req.setAttribute("year", ym.getYear());
    req.setAttribute("month", ym.getMonthValue());
    req.setAttribute("firstDow", firstDow);
    req.setAttribute("daysInMonth", daysInMonth);

    // 前月・次月
    YearMonth prev = ym.minusMonths(1);
    YearMonth next = ym.plusMonths(1);
    req.setAttribute("prevYm", prev.toString());
    req.setAttribute("nextYm", next.toString());

    // 既存の hasMenuMap / labelsByDate を計算している処理が別にあれば、その結果を req に積む
    // req.setAttribute("hasMenuMap", ...);
    // req.setAttribute("labelsByDate", ...);

    req.getRequestDispatcher("/user/menus_calendar.jsp").forward(req, resp);
  }

  // ============ helpers ============
  private static Object firstNonNull(Object... arr) {
    for (Object o : arr) if (o != null) return o;
    return null;
  }

  private static UUID parseUUID(String s) {
    if (s == null || s.trim().isEmpty()) return null;
    try { return UUID.fromString(s.trim()); } catch (Exception ignore) { return null; }
  }
}
