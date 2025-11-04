package admin;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import bean.Administrator;
import bean.User;
import dao.IndividualDAO;

@WebServlet(urlPatterns = {"/admin/individuals/register"})
public class AdminIndividualsRegisterServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    HttpSession ses = req.getSession(false);
    if (ses == null || ses.getAttribute("createdUser") == null) {
      resp.sendRedirect(req.getContextPath()+"/admin/users/register"); return;
    }
    req.getRequestDispatcher("/admin/individuals_register.jsp").forward(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    req.setCharacterEncoding("UTF-8");

    HttpSession ses = req.getSession(false);
    Administrator admin = (ses != null) ? (Administrator) ses.getAttribute("admin") : null;
    User created = (ses != null) ? (User) ses.getAttribute("createdUser") : null;
    if (admin == null || created == null) {
      resp.sendRedirect(req.getContextPath()+"/admin/users/register"); return;
    }

    String[] names = req.getParameterValues("childName");
    List<String> list = (names == null) ? Collections.emptyList() :
        Arrays.stream(names)
              .map(s -> s == null ? "" : s.trim())
              .filter(s -> !s.isEmpty())
              .limit(50)
              .collect(Collectors.toList());

    if (list.isEmpty()) {
      req.setAttribute("error","少なくとも1名の名前を入力してください。");
      doGet(req, resp); return;
    }

    new IndividualDAO().bulkInsert(admin.getOrgId(), created.getId(), list);

    // 後続用に整頓
    ses.removeAttribute("tempPwPlain"); // 仮PWはここで破棄
    ses.setAttribute("registeredUserLoginId", created.getLoginId());

    resp.sendRedirect(req.getContextPath()+"/admin/users/register/complete");
  }
}
