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
import bean.AuditLog;
import bean.Organization;
import dao.AuditLogDAO;

@WebServlet("/admin/logs")
public class AdminAuditLogsServlet extends HttpServlet {

  private final AuditLogDAO auditLogDAO = new AuditLogDAO();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    HttpSession ses = req.getSession(false);
    Administrator admin = (ses != null) ? (Administrator) ses.getAttribute("admin") : null;
    Organization org    = (ses != null) ? (Organization)  ses.getAttribute("org")   : null;

    if (admin == null || org == null) {
      resp.sendRedirect(req.getContextPath() + "/admin/login");
      return;
    }

    UUID orgId = org.getId();

    // ひとまず直近 200 件を表示
    List<AuditLog> logs = auditLogDAO.listRecentByOrg(orgId, 200);
    req.setAttribute("logs", logs);

    req.getRequestDispatcher("/admin/audit_logs.jsp")
       .forward(req, resp);
  }
}
