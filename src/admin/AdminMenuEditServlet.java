package admin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
 * 献立の追加/編集（時間帯ごと：朝食/昼食/夕食）
 * GET  : 指定日の指定スロット画面表示
 * POST : 指定スロットの保存
 *
 * 画面⇔サーバ間のパラメータ命名規約：
 *   - mealName_{SLOT}, mealDesc_{SLOT}
 *   - itemId_{SLOT}_{i}, itemName_{SLOT}_{i}
 *   - allergens_{SLOT}_{i}   (checkbox, multiple)
 *   - {SLOT}_rows            (例 "0,1,2")
 */
@WebServlet("/admin/menus_new/edit")
public class AdminMenuEditServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  private final MenuDayDAO dayDao   = new MenuDayDAO();
  private final MenuMealDAO mealDao = new MenuMealDAO();
  private final MenuItemDAO itemDao = new MenuItemDAO();
  private final AllergenDAO algDao  = new AllergenDAO();

  // 表示・保存対象となる時間帯
  private static final String[] SLOTS = { "BREAKFAST", "LUNCH", "DINNER" };

  /* ======================= helpers ======================= */
  private static String trim(String s) { if (s == null) return null; String t = s.trim(); return t.isEmpty()? null : t; }
  private static String nvl(String s)  { return (s == null) ? "" : s.trim(); }
  private static boolean isValidSlot(String s) {
    if (s == null) return false;
    for (String x : SLOTS) if (x.equals(s)) return true;
    return false;
  }

  /* ========================= GET ========================= */
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    HttpSession ses = req.getSession(false);
    Administrator admin = (ses != null) ? (Administrator) ses.getAttribute("admin") : null;
    Organization  org   = (ses != null) ? (Organization)  ses.getAttribute("org")   : null;
    if (admin == null || org == null) {
      resp.sendRedirect(req.getContextPath() + "/admin/login");
      return;
    }

    String dayIdStr = trim(req.getParameter("dayId"));
    String dateStr  = trim(req.getParameter("date"));
    String slot     = trim(req.getParameter("slot"));

    // slot 未指定で来た場合は時間帯選択に誘導（旧リンク対策）
    if (slot == null) {
      String qs = (dayIdStr != null) ? ("dayId=" + dayIdStr)
                 : (dateStr  != null) ? ("date="  + dateStr) : null;
      if (qs == null) { resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "dayId または date は必須です"); return; }
      resp.sendRedirect(req.getContextPath() + "/admin/menus_new/select?" + qs);
      return;
    }

    UUID dayId;
    try {
      if (dayIdStr != null) {
        dayId = UUID.fromString(dayIdStr);
      } else { // date 指定なら存在しなければ確保
        java.time.LocalDate date = java.time.LocalDate.parse(dateStr);
        dayId = dayDao.ensureDay(org.getId(), date);
      }
    } catch (Exception e) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "dayId/date の形式が不正です");
      return;
    }

    Optional<MenuDay> dayOpt = dayDao.find(dayId);
    if (!dayOpt.isPresent()) {
      resp.sendError(HttpServletResponse.SC_NOT_FOUND, "指定日の献立が見つかりません");
      return;
    }
    MenuDay day = dayOpt.get();

    // 全スロットの Meal を取得
    Map<String, MenuMeal> mealBySlot = mealDao.findByDayAsMap(day.getId());

    // 選択スロットの品目一覧（アレルゲン付き）
    List<MenuItem> selectedItems = Collections.emptyList();
    MenuMeal current = mealBySlot.get(slot);
    if (current != null) {
      selectedItems = itemDao.listWithAllergens(current.getId());
    }

    // 画面用データ
    req.setAttribute("menuDay", day);
    req.setAttribute("meals", mealBySlot);
    req.setAttribute("selectedSlot", slot);
    req.setAttribute("selectedItems", selectedItems);

    // ✅ アレルゲンは FOOD のみを表示
    req.setAttribute("allergens", algDao.listByCategory("FOOD"));

    req.getRequestDispatcher("/admin/menus_edit_slot.jsp").forward(req, resp);
  }

  /* ======================== POST ========================= */
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    req.setCharacterEncoding("UTF-8");

    HttpSession ses = req.getSession(false);
    Administrator admin = (ses != null) ? (Administrator) ses.getAttribute("admin") : null;
    Organization  org   = (ses != null) ? (Organization)  ses.getAttribute("org")   : null;
    if (admin == null || org == null) {
      resp.sendRedirect(req.getContextPath() + "/admin/login");
      return;
    }

    String dayIdStr = trim(req.getParameter("dayId"));
    String slot     = trim(req.getParameter("slot"));
    if (dayIdStr == null || !isValidSlot(slot)) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "dayId/slot は必須です");
      return;
    }

    UUID dayId;
    try {
      dayId = UUID.fromString(dayIdStr);
    } catch (Exception e) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "dayId が不正です");
      return;
    }

    try {
      // メニュー（スロット単位）
      String mealName = nvl(req.getParameter("mealName_" + slot));
      String mealDesc = nvl(req.getParameter("mealDesc_" + slot));

      UUID mealId = null;
      if (!mealName.isEmpty()) {
        // 既存があれば更新、無ければ作成
        mealId = mealDao.upsertMeal(dayId, slot, mealName, mealDesc);
      } else {
        // 名前が空ならそのスロットを未登録扱いにするため削除
        mealDao.deleteByDayAndSlot(dayId, slot);
      }

      // 品目の保存（meal がある場合のみ）
      if (mealId != null) {
        String rowsCsv = req.getParameter(slot + "_rows"); // 例 "0,1,2"
        if (rowsCsv != null && !rowsCsv.trim().isEmpty()) {
          List<MenuItemDAO.ItemForm> forms = new ArrayList<>();
          int order = 1;
          for (String r : rowsCsv.split(",")) {
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
          itemDao.saveMealItems(mealId, forms); // 差分を反映（既存置換）
        } else {
          // 行指定なし → 既存があれば0件に更新
          itemDao.saveMealItems(mealId, Collections.<MenuItemDAO.ItemForm>emptyList());
        }
      }

      ses.setAttribute("flash", "保存しました。");
      resp.sendRedirect(req.getContextPath() + "/admin/menus_new/edit?dayId=" + dayId + "&slot=" + slot);

    } catch (RuntimeException e) {
      // 画面にエラーメッセージを出して再表示
      req.setAttribute("error", "保存中にエラーが発生しました。");
      // GET と同じ準備をして返す
      // （selectedItems 等は doGet 内で用意しているので、そのまま委譲）
      doGet(req, resp);
    }
  }
}
