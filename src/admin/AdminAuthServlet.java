package admin;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import bean.Administrator;
import bean.Individual;
import bean.Organization;
import dao.IndividualDAO;

@WebServlet("/admin/auth")
public class AdminAuthServlet extends HttpServlet {
  private final IndividualDAO iDao = new IndividualDAO();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    HttpSession ses = req.getSession(false);
    Administrator admin = (ses != null) ? (Administrator) ses.getAttribute("admin") : null;
    Organization org    = (ses != null) ? (Organization) ses.getAttribute("org")   : null;

    if (admin == null || org == null) {
      resp.sendRedirect(req.getContextPath() + "/admin/login");
      return;
    }

    String q   = req.getParameter("q");       // 検索文字
    UUID orgId = admin.getOrgId();

    List<Individual> list;
    if (q != null && !q.isEmpty()) {
      list = iDao.searchByName(orgId, q);
    } else {
      list = iDao.listByOrg(orgId);
    }

    // 「今日」は日本時間で判定
    LocalDate today = OffsetDateTime.now(ZoneOffset.ofHours(9)).toLocalDate();

    // ID → スロット(MORNING/NOON/NIGHT), 表示用時刻(HH:mm)
    Map<UUID,String> slotMap = new LinkedHashMap<>();
    Map<UUID,String> timeMap = new LinkedHashMap<>();

    if (list != null) {
      java.time.format.DateTimeFormatter timeFmt =
          java.time.format.DateTimeFormatter.ofPattern("HH:mm");

      for (Individual ind : list) {
        OffsetDateTime vAt = ind.getLastVerifiedAt();
        if (vAt == null) continue;

        // DBに入っている時刻を「日本時間(+09:00)の瞬間」に変換
        OffsetDateTime jst = vAt.withOffsetSameInstant(ZoneOffset.ofHours(9));

        if (!jst.toLocalDate().isEqual(today)) continue;

        LocalTime t = jst.toLocalTime().truncatedTo(ChronoUnit.MINUTES);
        String slot = judgeSlot(t);
        if (slot == null) continue;

        UUID key = ind.getId();
        slotMap.put(key, slot);
        timeMap.put(key, t.format(timeFmt));
      }
    }

    req.setAttribute("list", list);
    req.setAttribute("today", today);
    req.setAttribute("slotMap", slotMap);
    req.setAttribute("timeMap", timeMap);

    req.getRequestDispatcher("/admin/authlist.jsp").forward(req, resp);
  }

  // 朝:6:00〜10:59, 昼:11:00〜16:59, 夜:17:00〜20:59
  private static String judgeSlot(LocalTime t) {
    LocalTime mStart = LocalTime.of(6, 0);
    LocalTime mEnd   = LocalTime.of(11, 0);  // 11:00未満 → 朝

    LocalTime nStart = LocalTime.of(11, 0);
    LocalTime nEnd   = LocalTime.of(17, 0);  // 17:00未満 → 昼

    LocalTime eStart = LocalTime.of(17, 0);
    LocalTime eEnd   = LocalTime.of(21, 0);  // 21:00未満 → 夜

    if (!t.isBefore(mStart) && t.isBefore(mEnd)) {
      return "MORNING";
    } else if (!t.isBefore(nStart) && t.isBefore(nEnd)) {
      return "NOON";
    } else if (!t.isBefore(eStart) && t.isBefore(eEnd)) {
      return "NIGHT";
    } else {
      return null;
    }
  }
}