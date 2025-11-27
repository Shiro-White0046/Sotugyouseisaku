package admin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import bean.Administrator;
import bean.Allergen;
import bean.MenuDay;
import bean.MenuItem;
import bean.MenuMeal;
import bean.Organization;
import dao.AllergenDAO;
import dao.MenuDayDAO;
import dao.MenuItemDAO;
import dao.MenuMealDAO;
import infra.AuditLogger;  // ★ 追加

/**
 * 献立：時間帯ごとの追加／編集ページ
 * GET  : 表示
 * POST : 保存（選択された時間帯のみ）
 */
@WebServlet("/admin/menus_new/edit")
public class AdminMenuEditServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  private final MenuDayDAO dayDao   = new MenuDayDAO();
  private final MenuMealDAO mealDao = new MenuMealDAO();
  private final MenuItemDAO itemDao = new MenuItemDAO();
  private final AllergenDAO algDao  = new AllergenDAO();

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

    String dayIdStr = req.getParameter("dayId");
    String slot     = req.getParameter("slot"); // BREAKFAST/LUNCH/DINNER 必須
    if (dayIdStr == null || dayIdStr.trim().isEmpty()) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "dayId は必須です"); return;
    }
    if (slot == null || slot.trim().isEmpty()) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "slot は必須です"); return;
    }

    final UUID dayId;
    try { dayId = UUID.fromString(dayIdStr.trim()); }
    catch (Exception e) { resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "dayId が不正です"); return; }

    MenuDay day = dayDao.find(dayId).orElse(null);
    if (day == null) {
      resp.sendError(HttpServletResponse.SC_NOT_FOUND, "指定日の献立が見つかりません"); return;
    }

    // 対象スロットの meal と items
    Optional<MenuMeal> mealOpt = mealDao.findByDayAndSlot(day.getId(), slot);
    List<MenuItem> selectedItems = Collections.emptyList();
    if (mealOpt.isPresent()) {
      selectedItems = itemDao.listWithAllergens(mealOpt.get().getId());
    }

    // --- アレルゲン一覧（FOOD + AVOID）を結合表示 ---
    List<Allergen> allergens = new ArrayList<Allergen>();
    allergens.addAll(algDao.listByCategory("FOOD"));
    allergens.addAll(algDao.listByCategory("AVOID"));

    // 画面用データ
    req.setAttribute("menuDay", day);
    req.setAttribute("selectedSlot", slot);
    req.setAttribute("meals", mealDao.findByDayAsMap(day.getId()));
    req.setAttribute("selectedItems", selectedItems);
    req.setAttribute("allergens", allergens);   // ← FOOD＋AVOID 両方を渡す

    req.getRequestDispatcher("/admin/menus_edit_slot.jsp").forward(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    req.setCharacterEncoding("UTF-8");

    HttpSession ses = req.getSession(false);
    Administrator admin = (ses != null) ? (Administrator) ses.getAttribute("admin") : null;
    Organization org    = (ses != null) ? (Organization)  ses.getAttribute("org")   : null;
    if (admin == null || org == null) {
      resp.sendRedirect(req.getContextPath() + "/admin/login");
      return;
    }

    String dayIdStr = req.getParameter("dayId");
    String slot     = req.getParameter("slot"); // この画面は1スロットのみ保存
    if (dayIdStr == null || dayIdStr.trim().isEmpty()) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "dayId は必須です"); return;
    }
    if (slot == null || slot.trim().isEmpty()) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "slot は必須です"); return;
    }

    final UUID dayId;
    try { dayId = UUID.fromString(dayIdStr.trim()); }
    catch (Exception e) { resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "dayId が不正です"); return; }

    String mealName = nvl(req.getParameter("mealName_" + slot));
    String mealDesc = nvl(req.getParameter("mealDesc_" + slot));

    try {
      UUID mealId = null;

      if (!mealName.isEmpty()) {
        // 既存の画像パスは保持して upsert（画像は別画面で更新）
        Optional<MenuMeal> ex = mealDao.findByDayAndSlot(dayId, slot);
        String keepImage = ex.isPresent() ? ex.get().getImagePath() : null;

        mealId = mealDao.upsertMeal(dayId, slot, mealName, mealDesc);

      } else {
        // 名前が空 → そのスロットを未登録に戻す（meal自体を削除）
        mealDao.deleteByDayAndSlot(dayId, slot);
      }

      // 品目保存（meal がある時のみ）
      if (mealId != null) {
        String rowsCsv = req.getParameter(slot + "_rows"); // "0,1,2" など
        if (rowsCsv != null && !rowsCsv.trim().isEmpty()) {
          String[] rows = rowsCsv.split(",");
          List<MenuItemDAO.ItemForm> forms = new ArrayList<MenuItemDAO.ItemForm>();
          int order = 1;
          for (String r : rows) {
            String idx = r.trim();
            if (idx.isEmpty()) continue;

            String idParam   = req.getParameter("itemId_"   + slot + "_" + idx);
            String nameParam = req.getParameter("itemName_" + slot + "_" + idx);
            String[] algIds  = req.getParameterValues("allergens_" + slot + "_" + idx);

            UUID itemId = null;
            if (idParam != null && !idParam.trim().isEmpty()) {
              try { itemId = UUID.fromString(idParam.trim()); } catch (Exception ignore) {}
            }

            List<Short> allergenIds = new ArrayList<Short>();
            if (algIds != null) {
              for (String s : algIds) {
                try { allergenIds.add(Short.parseShort(s)); } catch (Exception ignore) {}
              }
            }

            if (nameParam != null && !nameParam.trim().isEmpty()) {
              forms.add(new MenuItemDAO.ItemForm(itemId, order++, nameParam.trim(), allergenIds));
            }
          }
          itemDao.saveMealItems(mealId, forms);
        } else {
          // 品目0件
          itemDao.saveMealItems(mealId, Collections.<MenuItemDAO.ItemForm>emptyList());
        }
      }

      // ★ 操作ログ（献立の更新／作成）
      AuditLogger.logAdminFromSession(
          req,
          "update_menu",
          "menu_days",
          dayId.toString()
      );

      ses.setAttribute("flash", "保存しました。");
      resp.sendRedirect(req.getContextPath() + "/admin/menus_new/edit?dayId=" + dayId + "&slot=" + slot);
      return;

    } catch (RuntimeException e) {
      req.setAttribute("error", "保存中にエラーが発生しました。");

      // 再描画用の最低限データを再ロード
      try {
        Optional<MenuMeal> mealOpt = mealDao.findByDayAndSlot(dayId, slot);
        List<MenuItem> selectedItems = mealOpt.isPresent()
            ? itemDao.listWithAllergens(mealOpt.get().getId())
            : Collections.<MenuItem>emptyList();

        MenuDay day = dayDao.find(dayId).orElse(null);

        // --- FOOD + AVOID を再設定 ---
        List<Allergen> allergens = new ArrayList<Allergen>();
        allergens.addAll(algDao.listByCategory("FOOD"));
        allergens.addAll(algDao.listByCategory("AVOID"));

        req.setAttribute("menuDay", day);
        req.setAttribute("selectedSlot", slot);
        req.setAttribute("meals", mealDao.findByDayAsMap(dayId));
        req.setAttribute("selectedItems", selectedItems);
        req.setAttribute("allergens", allergens);
      } catch (Exception ignore) {}

      req.getRequestDispatcher("/admin/menus_edit_slot.jsp").forward(req, resp);
    }
  }

  private static String nvl(String s){ return (s == null) ? "" : s.trim(); }
}
