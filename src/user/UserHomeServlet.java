package user;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import bean.Menu;
import bean.User;
import dao.MenuDAO;

@WebServlet("/user/home")
public class UserHomeServlet extends HttpServlet {
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    HttpSession ses = req.getSession(false);
    User user = (ses != null) ? (User) ses.getAttribute("user") : null;

    if (user == null) {
      // 未ログインならログインページへ
      resp.sendRedirect(req.getContextPath() + "/user/login");
      return;
    }

    // 今日の日付の献立を取得
    MenuDAO menuDao = new MenuDAO();
    Optional<Menu> menuOpt = menuDao.findByDate(user.getOrgId(), LocalDate.now(), true);

    if (menuOpt.isPresent()) {
      Menu m = menuOpt.get();
      req.setAttribute("todayMenuName", m.getName());
      req.setAttribute("todayMenuDesc", m.getDescription());
      req.setAttribute("todayMenuImageUrl", m.getImagePath());
    } else {
      req.setAttribute("todayMenuName", null);
      req.setAttribute("todayMenuDesc", null);
      req.setAttribute("todayMenuImageUrl", null);
    }

    // JSPへフォワード
    req.getRequestDispatcher("/user/home.jsp").forward(req, resp);
  }
}
