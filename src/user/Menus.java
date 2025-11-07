package user;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dao.MenuDAO;

@WebServlet("/user/menus")
public class Menus extends HttpServlet {
  private final MenuDAO menuDao = new MenuDAO();

  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    // セッションから組織ID（UUID）を取得（プロジェクトの属性名に合わせて）
    UUID orgId = (UUID) req.getSession().getAttribute("orgId");
    if (orgId == null) {
      resp.sendError(HttpServletResponse.SC_FORBIDDEN);
      return;
    }

    // ym=yyyy-MM（無ければ今月）
    YearMonth ym;
    try {
      String p = req.getParameter("ym");
      ym = (p == null || p.isEmpty()) ? YearMonth.now() : YearMonth.parse(p);
    } catch (Exception e) { ym = YearMonth.now(); }

    LocalDate first = ym.atDay(1);
    int firstDow = first.getDayOfWeek().getValue() % 7; // 0=日, …, 6=土
    int days = ym.lengthOfMonth();

    Map<LocalDate, List<String>> map = menuDao.findAllergenLabelsForMonth(orgId, ym, true);

    // JSPへ
    req.setAttribute("year", ym.getYear());
    req.setAttribute("month", ym.getMonthValue());
    req.setAttribute("firstDow", firstDow);
    req.setAttribute("daysInMonth", days);

    Map<String, List<String>> labelsByDate = map.entrySet().stream()
      .collect(Collectors.toMap(e -> e.getKey().toString(), Map.Entry::getValue));
    req.setAttribute("labelsByDate", labelsByDate);

    req.setAttribute("prevYm", ym.minusMonths(1).toString());
    req.setAttribute("nextYm", ym.plusMonths(1).toString());

    req.getRequestDispatcher("/WEB-INF/views/user/menu_calendar.jsp").forward(req, resp);
  }
}