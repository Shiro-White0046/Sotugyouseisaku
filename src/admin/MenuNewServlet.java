// src/admin/AdminMenuNewServlet.java
package admin;

import java.io.IOException;
import java.time.LocalDate;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import bean.Administrator;
import bean.Organization;
import dao.MenuDayDAO;
import dao.MenuMealDAO;

@WebServlet("/admin/menus/new")
public class MenuNewServlet extends HttpServlet {
  private final MenuDayDAO dayDao = new MenuDayDAO();
  private final MenuMealDAO mealDao = new MenuMealDAO();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    HttpSession ses = req.getSession(false);
    Administrator admin = (ses != null) ? (Administrator) ses.getAttribute("admin") : null;
    Organization org    = (ses != null) ? (Organization)  ses.getAttribute("org")   : null;
    if (admin == null || org == null) { resp.sendRedirect(req.getContextPath()+"/admin/login"); return; }

    String dateStr = req.getParameter("date");
    if (dateStr == null || dateStr.isEmpty()) { resp.sendError(400, "date は必須です"); return; }

    LocalDate date;
    try { date = LocalDate.parse(dateStr); } catch (Exception e) { resp.sendError(400, "日付形式が不正です"); return; }

    // 既に登録があってもこの画面は「新規」扱いで上書き可能にする（初期版）
    req.setAttribute("date", date);
    req.getRequestDispatcher("/admin/menu_new.jsp").forward(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    req.setCharacterEncoding("UTF-8");

    HttpSession ses = req.getSession(false);
    Organization org = (ses != null) ? (Organization) ses.getAttribute("org") : null;
    if (org == null) { resp.sendRedirect(req.getContextPath()+"/admin/login"); return; }

    LocalDate date;
    try { date = LocalDate.parse(req.getParameter("date")); }
    catch (Exception e) { req.setAttribute("error", "日付が不正です"); doGet(req, resp); return; }

    String bName = n(req.getParameter("breakfastName"));
    String bDesc = n(req.getParameter("breakfastDesc"));
    String lName = n(req.getParameter("lunchName"));
    String lDesc = n(req.getParameter("lunchDesc"));
    String dName = n(req.getParameter("dinnerName"));
    String dDesc = n(req.getParameter("dinnerDesc"));

    // 必須は“いずれか1食は名前あり”ぐらいから
    if (isEmptyAll(bName, lName, dName)) {
      req.setAttribute("error", "少なくとも1つの食事に名前を入力してください。");
      req.setAttribute("date", date);
      req.getRequestDispatcher("/admin/menu_new.jsp").forward(req, resp);
      return;
    }

    // 保存
    UUID dayId = new MenuDayDAO().ensureDay(org.getId(), date);
    if (!isBlank(bName)) mealDao.upsert(dayId, "breakfast", bName, bDesc);
    if (!isBlank(lName)) mealDao.upsert(dayId, "lunch",     lName, lDesc);
    if (!isBlank(dName)) mealDao.upsert(dayId, "dinner",    dName, dDesc);

    // 完了へ（後で専用doneにしてもOK）
    resp.sendRedirect(req.getContextPath() + "/admin/menus?ym=" + date.toString().substring(0,7));
  }

  private static String n(String s){ return (s==null)? "": s.trim(); }
  private static boolean isBlank(String s){ return s==null || s.trim().isEmpty(); }
  private static boolean isEmptyAll(String... arr){
    for (String s: arr) if (!isBlank(s)) return false;
    return true;
  }
}
