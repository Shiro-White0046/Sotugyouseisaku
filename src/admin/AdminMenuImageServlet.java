package admin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import bean.Administrator;
import bean.MenuDay;
import bean.Organization;
import dao.MenuDayDAO;

@WebServlet("/admin/menus_new/image")
@MultipartConfig(maxFileSize = 10_000_000) // 10MB
public class AdminMenuImageServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;
  private final MenuDayDAO dayDao = new MenuDayDAO();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    HttpSession ses = req.getSession(false);
    Administrator admin = ses==null? null: (Administrator) ses.getAttribute("admin");
    Organization  org   = ses==null? null: (Organization)  ses.getAttribute("org");
    if (admin==null || org==null) { resp.sendRedirect(req.getContextPath()+"/admin/login"); return; }

    String dayIdStr = req.getParameter("dayId");
    if (dayIdStr == null) { resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "dayId は必須です"); return; }

    UUID dayId = UUID.fromString(dayIdStr);
    Optional<MenuDay> dayOpt = dayDao.find(dayId);
    if (!dayOpt.isPresent()) { resp.sendError(HttpServletResponse.SC_NOT_FOUND, "指定日の献立が見つかりません"); return; }

    req.setAttribute("menuDay", dayOpt.get());
    req.getRequestDispatcher("/admin/menus_image_attach.jsp").forward(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    req.setCharacterEncoding("UTF-8");

    String dayIdStr = req.getParameter("dayId");
    if (dayIdStr == null) { resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "dayId は必須です"); return; }
    UUID dayId = UUID.fromString(dayIdStr);

    Part part = req.getPart("imageFile");
    if (part == null || part.getSize() == 0) {
      req.setAttribute("error", "ファイルを選択してください。");
      doGet(req, resp);
      return;
    }

    ServletContext context = getServletContext();
    String uploadDir = context.getRealPath("/uploads/menu_images");
    Files.createDirectories(Paths.get(uploadDir));

    String fileName = java.util.UUID.randomUUID().toString() + "_" +
                      Paths.get(part.getSubmittedFileName()).getFileName().toString();
    Path filePath = Paths.get(uploadDir, fileName);

    try {
      Files.copy(part.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
      dayDao.updateImagePath(dayId, "uploads/menu_images/" + fileName);
      req.setAttribute("success", "画像を追加しました。");
    } catch (IOException e) {
      req.setAttribute("error", "画像の保存に失敗しました。");
    }

    doGet(req, resp);
  }
}
