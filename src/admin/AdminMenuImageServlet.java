package admin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import bean.Administrator;
import bean.MenuMeal;
import bean.Organization;
import dao.MenuMealDAO;

/**
 * 画像を「時間帯（slot）ごと」に登録
 * GET : 画像追加画面（該当slotの画像を表示）
 * POST: アップロード＆ menu_meals.image_path を更新
 */
@WebServlet("/admin/menus_new/image")
@MultipartConfig(maxFileSize = 10 * 1024 * 1024)
public class AdminMenuImageServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;
  private final MenuMealDAO mealDao = new MenuMealDAO();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    HttpSession ses = req.getSession(false);
    Administrator admin = (ses != null) ? (Administrator) ses.getAttribute("admin") : null;
    Organization org = (ses != null) ? (Organization) ses.getAttribute("org") : null;
    if (admin == null || org == null) {
      resp.sendRedirect(req.getContextPath() + "/admin/login");
      return;
    }

    String dayIdStr = req.getParameter("dayId");
    String slot = req.getParameter("slot");

    if (dayIdStr == null || dayIdStr.isEmpty() || slot == null || slot.isEmpty()) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "dayId と slot は必須です");
      return;
    }

    UUID dayId;
    try {
      dayId = UUID.fromString(dayIdStr);
    } catch (Exception e) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "dayId が不正です");
      return;
    }

    // 該当スロットの献立を取得（存在しない場合は新規扱い）
    MenuMeal meal = mealDao.findByDayAndSlot(dayId, slot).orElse(null);
    req.setAttribute("dayId", dayId);
    req.setAttribute("slot", slot);
    req.setAttribute("meal", meal);

    req.getRequestDispatcher("/admin/menus_image_slot.jsp").forward(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    req.setCharacterEncoding("UTF-8");

    HttpSession ses = req.getSession(false);
    Administrator admin = (ses != null) ? (Administrator) ses.getAttribute("admin") : null;
    Organization org = (ses != null) ? (Organization) ses.getAttribute("org") : null;
    if (admin == null || org == null) {
      resp.sendRedirect(req.getContextPath() + "/admin/login");
      return;
    }

    String dayIdStr = req.getParameter("dayId");
    String slot = req.getParameter("slot");

    if (dayIdStr == null || slot == null || dayIdStr.isEmpty() || slot.isEmpty()) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "dayId と slot は必須です");
      return;
    }

    UUID dayId;
    try {
      dayId = UUID.fromString(dayIdStr);
    } catch (Exception e) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "dayId が不正です");
      return;
    }

    Part part = req.getPart("imageFile");
    if (part == null || part.getSize() == 0) {
      req.setAttribute("error", "画像ファイルを選択してください。");
      doGet(req, resp);
      return;
    }

    // 保存先: /uploads/menus/{dayId}/{slot}.ext
    final String baseRel = "/uploads/menus/" + dayId;
    final String baseAbs = req.getServletContext().getRealPath(baseRel);
    File dir = new File(baseAbs);
    if (!dir.exists()) dir.mkdirs();

    String submitted = part.getSubmittedFileName();
    String ext = (submitted != null && submitted.contains(".")) ?
        submitted.substring(submitted.lastIndexOf('.') + 1).toLowerCase() : "jpg";
    if (!Arrays.asList("jpg", "jpeg", "png", "gif").contains(ext)) ext = "jpg";

    for (String e : new String[] {"jpg", "jpeg", "png", "gif"}) {
      File old = new File(dir, slot + "." + e);
      if (old.exists()) old.delete();
    }

    File dest = new File(dir, slot + "." + ext);
    try (InputStream in = part.getInputStream()) {
      Files.copy(in, dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }

    // DB に保存
    String relPath = (baseRel + "/" + slot + "." + ext).substring(1); // 先頭スラ除去
    mealDao.updateImagePath(dayId, slot, relPath);

    ses.setAttribute("flash", "画像を保存しました。");
    resp.sendRedirect(req.getContextPath() + "/admin/menus_new/edit?dayId=" + dayId + "&slot=" + slot);
  }
}
