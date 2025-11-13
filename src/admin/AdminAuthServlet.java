package admin;

import java.io.IOException;
import java.util.List;
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

        String q    = req.getParameter("q");     // 検索文字

        UUID orgId  = admin.getOrgId();

        List<Individual> list;

        // ★ 検索条件あり
        if (q != null && !q.isEmpty()) {

            list = iDao.searchByName(orgId, q);

        } else {
            // ★ 検索していない or 空欄 → 全件
            list = iDao.listByOrg(orgId);
        }

        req.setAttribute("list", list);

        // 今日の日付（認証済み判定用）
        java.time.LocalDate today = java.time.LocalDate.now();
        req.setAttribute("today", today);

        req.getRequestDispatcher("/admin/authlist.jsp").forward(req, resp);
    }
}
