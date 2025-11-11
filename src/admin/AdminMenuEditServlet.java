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
import bean.MenuDay;
import bean.MenuItem;
import bean.MenuMeal;
import bean.Organization;
import dao.AllergenDAO;
import dao.MenuDayDAO;
import dao.MenuItemDAO;
import dao.MenuMealDAO;

/**
 * 献立：時間帯ごとの追加／編集ページ
 * GET  : 表示
 * POST : 保存（※選択された時間帯のみ）
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
    String slot = req.getParameter("slot"); // BREAKFAST/LUNCH/DINNER（画面は1スロット単位）

    if (dayIdStr == null || dayIdStr.trim().isEmpty()) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "dayId は必須です"); return;
    }
    if (slot == null || slot.trim().isEmpty()) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "slot は必須です"); return;
    }

    UUID dayId;
    try { dayId = UUID.fromString(dayIdStr.trim()); }
    catch (Exception e) { resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "dayId が不正です"); return; }

    Optional<MenuDay> dayOpt = dayDao.find(dayId);
    if (!dayOpt.isPresent()) {
      resp.sendError(HttpServletResponse.SC_NOT_FOUND, "指定日の献立が見つかりません"); return;
    }
    MenuDay day = dayOpt.get();

    // 対象スロットの meal と items をだけ取得
    Optional<MenuMeal> mealOpt = mealDao.findByDayAndSlot(day.getId(), slot);
    List<MenuItem> selectedItems = Collections.emptyList();
    if (mealOpt.isPresent()) {
      selectedItems = itemDao.listWithAllergens(mealOpt.get().getId());
    }

    // 画面用データ
    req.setAttribute("menuDay", day);
    req.setAttribute("selectedSlot", slot);
    req.setAttribute("meals", mealDao.findByDayAsMap(day.getId())); // ヘッダ等で使う用（任意）
    req.setAttribute("selectedItems", selectedItems);
    req.setAttribute("allergens", algDao.listByCategory("FOOD"));   // 要件：FOODのみ表示

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
    String slot     = req.getParameter("slot"); // ← この画面は1スロットだけ保存する
    if (dayIdStr == null || dayIdStr.trim().isEmpty()) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "dayId は必須です"); return;
    }
    if (slot == null || slot.trim().isEmpty()) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "slot は必須です"); return;
    }

    final UUID dayId;
    try { dayId = UUID.fromString(dayIdStr.trim()); }
    catch (Exception e) { resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "dayId が不正です"); return; }

    // 画面の命名規則： mealName_{slot}, mealDesc_{slot}, {slot}_rows, itemId_{slot}_{i}, itemName_{slot}_{i}, allergens_{slot}_{i}[]
    String mealName = nvl(req.getParameter("mealName_" + slot));
    String mealDesc = nvl(req.getParameter("mealDesc_" + slot));

    try {
      UUID mealId = null;

      if (!mealName.isEmpty()) {
        // upsert（DAO 側で meal_slot を DB 小文字に合わせて保存する実装）
        mealId = mealDao.upsertMeal(dayId, slot, mealName, mealDesc);
      } else {
        // 名前が空 → そのスロット自体を削除
        mealDao.deleteByDayAndSlot(dayId, slot);
      }

      // 品目保存（meal がある時だけ）
      if (mealId != null) {
        String rowsCsv = req.getParameter(slot + "_rows"); // 例: "0,1,3"
        if (rowsCsv != null && !rowsCsv.trim().isEmpty()) {
          String[] rows = rowsCsv.split(",");
          List<MenuItemDAO.ItemForm> forms = new ArrayList<>();
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

            List<Short> allergenIds = new ArrayList<>();
            if (algIds != null) {
              for (String s : algIds) {
                try { allergenIds.add(Short.parseShort(s)); } catch (Exception ignore) {}
              }
            }

            if (nameParam != null && !nameParam.trim().isEmpty()) {
              forms.add(new MenuItemDAO.ItemForm(itemId, order++, nameParam.trim(), allergenIds));
            }
          }
          itemDao.saveMealItems(mealId, forms); // 既存差分を丸ごと反映
        } else {
          // 品目0件
          itemDao.saveMealItems(mealId, Collections.emptyList());
        }
      }

      ses.setAttribute("flash", "保存しました。");
      // 保存後は同じスロット編集に戻す
      resp.sendRedirect(req.getContextPath() + "/admin/menus_new/edit?dayId=" + dayId + "&slot=" + slot);
      return;

    } catch (RuntimeException e) {
      // ★ 例外の中身を画面に出して原因を可視化（暫定）
      req.setAttribute("error", "保存中にエラーが発生しました。");
      req.setAttribute("errorDetail", e.toString() + (e.getCause() != null ? " / cause: " + e.getCause() : ""));
      // そのまま再描画（選択スロットのみ再ロード）
      try {
        Optional<MenuMeal> mealOpt = mealDao.findByDayAndSlot(dayId, slot);
        List<MenuItem> selectedItems = mealOpt.isPresent()
            ? itemDao.listWithAllergens(mealOpt.get().getId())
            : Collections.emptyList();

        MenuDay day = dayDao.find(dayId).orElse(null);
        req.setAttribute("menuDay", day);
        req.setAttribute("selectedSlot", slot);
        req.setAttribute("meals", mealDao.findByDayAsMap(dayId));
        req.setAttribute("selectedItems", selectedItems);
        req.setAttribute("allergens", algDao.listByCategory("FOOD"));

      } catch (Exception ignore) { /* 再描画用の追加エラーは握りつぶす */ }

      req.getRequestDispatcher("/admin/menus_edit_slot.jsp").forward(req, resp);
    }
  }

  private static String nvl(String s){ return (s == null) ? "" : s.trim(); }
}
