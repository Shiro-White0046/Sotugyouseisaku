package user;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import bean.User;

@WebServlet("/user/avoid")
public class ProhibitedInputServlet extends HttpServlet {
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

    // 禁止食材を取得
    //subtitle



    // JSPへフォワード
    req.getRequestDispatcher("/user/AllergyFood.jsp").forward(req, resp);
  }
}
