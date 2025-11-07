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
import javax.servlet.http.HttpSession;

import bean.User;     // プロジェクトの User ビーン（orgId取得で使用）
import dao.MenuDAO;

@WebServlet("/user/menus")
public class Menus extends HttpServlet {
  private static final long serialVersionUID = 1L;

  // ★あなたのプロジェクトに合わせて必要なら変える（存在するURLに！）
  private static final String FALLBACK_LOGIN_URL = "/auth/login";   // 例: /auth/login
  private static final String FALLBACK_HOME_URL  = "/user/home";     // 例: /user/home

  private final MenuDAO menuDao = new MenuDAO();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    HttpSession session = req.getSession(false);

    // 1) ログインユーザの取得（属性名の揺れに対応：loginUser / user / account）
    Object loginAttr = (session == null) ? null : firstNonNull(
        session.getAttribute("loginUser"),
        session.getAttribute("user"),
        session.getAttribute("account")
    );

    // 未ログインっぽい場合は “存在する” 画面へ誘導（ログイン or ホーム）
    if (loginAttr == null) {
      resp.sendRedirect(req.getContextPath() + FALLBACK_LOGIN_URL);
      return;
    }

    // 2) orgId の取得（sessionに直入れ or Userビーンから）
    UUID orgId = resolveOrgId(session, loginAttr);
    if (orgId == null) {
      // どうしても取れない場合はホームへ
      resp.sendRedirect(req.getContextPath() + FALLBACK_HOME_URL);
      return;
    }

    // 3) ym=yyyy-MM（なければ今月）
    YearMonth ym;
    try {
      String p = req.getParameter("ym");
      ym = (p == null || p.isEmpty()) ? YearMonth.now() : YearMonth.parse(p);
    } catch (Exception e) { ym = YearMonth.now(); }

    LocalDate first = ym.atDay(1);
    int firstDow = first.getDayOfWeek().getValue() % 7; // 0=日, …, 6=土
    int days = ym.lengthOfMonth();

    // 4) DBから当月の主要アレルゲンラベルを取得（公開済のみ）
    Map<LocalDate, List<String>> map = menuDao.findAllergenLabelsForMonth(orgId, ym, true);

    // 5) JSP へ渡す
    req.setAttribute("year", ym.getYear());
    req.setAttribute("month", ym.getMonthValue());
    req.setAttribute("firstDow", firstDow);
    req.setAttribute("daysInMonth", days);

    Map<String, List<String>> labelsByDate = map.entrySet().stream()
        .collect(Collectors.toMap(e -> e.getKey().toString(), Map.Entry::getValue));
    req.setAttribute("labelsByDate", labelsByDate);

    req.setAttribute("prevYm", ym.minusMonths(1).toString());
    req.setAttribute("nextYm", ym.plusMonths(1).toString());

    req.getRequestDispatcher("/user/menu_calendar.jsp").forward(req, resp);
  }

  // ===== helper =====

  private static Object firstNonNull(Object... arr) {
    for (Object o : arr) if (o != null) return o;
    return null;
  }

  /** orgId をセッション or Userビーンから引く。取得できなければ null */
  private static UUID resolveOrgId(HttpSession session, Object loginAttr) {
    // 1) セッションに orgId を直に持っている場合
    Object orgIdObj = (session != null) ? session.getAttribute("orgId") : null;
    if (orgIdObj instanceof UUID) return (UUID) orgIdObj;

    // 2) 文字列で入っている場合
    if (orgIdObj instanceof String) {
      try { return UUID.fromString((String) orgIdObj); } catch (Exception ignore) {}
    }

    // 3) Userビーンに getOrgId() がある場合
    if (loginAttr instanceof User) {
      return ((User) loginAttr).getOrgId();
    }

    // 4) それ以外（別ビーン型）の場合も反射で頑張ってみる
    try {
      java.lang.reflect.Method m = loginAttr.getClass().getMethod("getOrgId");
      Object v = m.invoke(loginAttr);
      if (v instanceof UUID) return (UUID) v;
      if (v instanceof String) return UUID.fromString((String) v);
    } catch (Exception ignore) {}

    return null;
  }
}