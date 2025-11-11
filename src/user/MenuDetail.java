package user;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import bean.MenuDay;          // ← menu_days 用のビーン（id, orgId, menuDate, imagePath, published, createdAt など）
import bean.User;            // ← ログインユーザー（orgId 取得で使用）
import dao.MenuDayDAO;       // ← ★ 旧 MenuDAO ではなく MenuDayDAO を使用

@WebServlet("/user/menus/detail")
public class MenuDetail extends HttpServlet {

  private static final long serialVersionUID = 1L;

  // ★ 旧: private final MenuDAO menuDao = new MenuDAO();
  private final MenuDayDAO menuDao = new MenuDayDAO();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    // --- 認可チェック（未ログインならログインへ） ---
    HttpSession session = req.getSession(false);
    if (session == null || session.getAttribute("user") == null) {
      resp.sendRedirect(req.getContextPath() + "/user/login");
      return;
    }
    User loginUser = (User) session.getAttribute("user");
    UUID orgId = loginUser.getOrgId();

    // --- パラメータ date=yyyy-MM-dd を取得/検証 ---
    String dateStr = req.getParameter("date");
    if (dateStr == null || dateStr.trim().isEmpty()) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "date パラメータが必要です");
      return;
    }

    LocalDate date;
    try {
      date = LocalDate.parse(dateStr.trim());
    } catch (Exception e) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "日付形式が不正です (yyyy-MM-dd)");
      return;
    }

    // --- 指定日の menu_days を取得（公開/非公開の判定は必要に応じてJSP側またはここで実施） ---
    Optional<MenuDay> opt = menuDao.findByDate(orgId, date);

    if (!opt.isPresent()) {
      // レコードが無い場合：空画面へ
      req.setAttribute("date", date);
      // 既存の JSP 構成に合わせてパスを維持（必要なら /user/... に変更）
      req.getRequestDispatcher("/WEB-INF/views/user/menu_detail_empty.jsp").forward(req, resp);
      return;
    }

    // レコードがある場合：詳細へ
    MenuDay day = opt.get();
    req.setAttribute("menuDay", day);

    // ※ 旧スキーマ互換のために "menu" という属性名に同じオブジェクトを載せておくと
    //    既存JSPが ${menu.*} を参照していても移行が少し楽です（必要ならアンコメント）
    // req.setAttribute("menu", day);

    // 旧コードではアレルゲン一覧を menuDao.listAllergens(menu.getId()) で渡していたが、
    // 新スキーマ（menu_meals / menu_items / menu_item_allergens）では取得方法が変わる。
    // 必要になったら MenuMealDAO / MenuItemDAO で品目ごとのアレルゲンを組み立てて渡す。

    req.getRequestDispatcher("/WEB-INF/views/user/menu_detail.jsp").forward(req, resp);
  }
}