// src/admin/AdminAccountEditServlet.java
package admin;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import bean.User;
import dao.UserDAO;

@WebServlet("/admin/accounts/edit")
public class AdminAccountEditServlet extends HttpServlet {

  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    String idStr = req.getParameter("userId");
    if (idStr == null) { resp.sendRedirect(req.getContextPath()+"/admin/users"); return; }
    UUID userId = UUID.fromString(idStr);

    Optional<User> uOpt = new UserDAO().findById(userId);
    if (!uOpt.isPresent()) {
      req.getSession().setAttribute("error", "アカウントが見つかりません。");
      resp.sendRedirect(req.getContextPath()+"/admin/users");
      return;
    }
    req.setAttribute("user", uOpt.get());
    req.getRequestDispatcher("/admin/account_edit.jsp").forward(req, resp);
  }

  @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    req.setCharacterEncoding("UTF-8");

    String idStr = req.getParameter("userId");
    String name  = req.getParameter("name");
    String type  = req.getParameter("accountType");

    if (idStr == null) { resp.sendRedirect(req.getContextPath()+"/admin/users"); return; }
    UUID userId = UUID.fromString(idStr);

    if (name == null || name.trim().isEmpty() ||
        !( "single".equals(type) || "multi".equals(type) )) {
      req.setAttribute("error", "入力内容を確認してください。");
      doGet(req, resp);
      return;
    }

    new UserDAO().updateNameAndType(userId, name.trim(), type);

    HttpSession ses = req.getSession();
    ses.setAttribute("flash", "アカウントを更新しました。");
    resp.sendRedirect(req.getContextPath()+"/admin/accounts?userId="+userId.toString());
  }
}
