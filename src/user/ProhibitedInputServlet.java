package user;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import bean.Allergen;
import bean.Individual;
import bean.IndividualAllergy;
import bean.User;
import dao.AllergenDAO;
import dao.IndividualAllergyDAO;
import dao.IndividualDAO;

/**
 * 食べられない食材入力（禁止食材）
 * GET: 画面表示
 * POST: 保存 → ホーム画面へリダイレクト
 */
@WebServlet("/user/avoid")
public class ProhibitedInputServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  private final AllergenDAO allergenDAO = new AllergenDAO();
  private final IndividualDAO individualDAO = new IndividualDAO();
  private final IndividualAllergyDAO iaDAO = new IndividualAllergyDAO();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    HttpSession ses = req.getSession(false);
    User user = (ses == null) ? null : (User) ses.getAttribute("user");
    if (user == null) {
      resp.sendRedirect(req.getContextPath() + "/user/login");
      return;
    }

    // 対象児取得
    List<Individual> persons = individualDAO.listByUser(user.getId());
    if (persons.isEmpty()) {
      req.setAttribute("flash", "まずはお子さま（個人）を登録してください。");
      req.getRequestDispatcher("/user/home.jsp").forward(req, resp);
      return;
    }

    // person選択（?person=）
    String personParam = req.getParameter("person");
    UUID personId = null;
    if (personParam != null && !personParam.isEmpty()) {
      try { personId = UUID.fromString(personParam); } catch (Exception ignore) {}
    }
    if (personId == null) personId = persons.get(0).getId();

    // マスタ: AVOID（食べられない食材）
    List<Allergen> avoidList = allergenDAO.listByCategory("AVOID");

    // 既存登録 → code セット
    List<Allergen> existing = iaDAO.listAllergensOfPerson(personId);
    Set<String> selectedCodes = existing.stream()
        .filter(a -> "AVOID".equalsIgnoreCase(a.getCategory()))
        .map(Allergen::getCode)
        .collect(Collectors.toCollection(LinkedHashSet::new));

    // noteMap（必要なら）
    List<IndividualAllergy> rows = iaDAO.listByPerson(personId);
    Map<Short, String> idToNote = new HashMap<Short, String>();
    for (IndividualAllergy ia : rows) {
      idToNote.put(ia.getAllergenId(), ia.getNote() == null ? "" : ia.getNote());
    }
    Map<String, String> noteMap = new LinkedHashMap<String, String>();
    for (Allergen a : avoidList) {
      String note = idToNote.getOrDefault(a.getId(), "");
      noteMap.put(a.getCode(), note);
    }

    // JSPへ
    req.setAttribute("persons", persons);
    req.setAttribute("personId", personId);
    req.setAttribute("avoidList", avoidList);
    req.setAttribute("selectedCodes", selectedCodes);
    req.setAttribute("noteMap", noteMap);

    req.getRequestDispatcher("/user/prohibited_input.jsp").forward(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    req.setCharacterEncoding("UTF-8");
    HttpSession ses = req.getSession(false);
    User user = (ses == null) ? null : (User) ses.getAttribute("user");
    if (user == null) {
      resp.sendRedirect(req.getContextPath() + "/user/login");
      return;
    }

    UUID personId = UUID.fromString(req.getParameter("person_id"));

    // マスタ取得
    List<Allergen> avoidList = allergenDAO.listByCategory("AVOID");
    Map<String, Short> codeToId = avoidList.stream()
        .collect(Collectors.toMap(Allergen::getCode, Allergen::getId));

    // 新規選択
    String[] codes = req.getParameterValues("avoid");
    Set<Short> newIds = new LinkedHashSet<Short>();
    Map<Short, String> noteById = new LinkedHashMap<Short, String>();

    if (codes != null) {
      for (String code : codes) {
        Short id = codeToId.get(code);
        if (id != null) {
          newIds.add(id);
          if ("OTHER".equalsIgnoreCase(code)) {
            String free = req.getParameter("other_text");
            if (free != null) free = free.trim();
            noteById.put(id, free == null ? "" : free);
          } else {
            noteById.put(id, "");
          }
        }
      }
    }

    // 既存
    Set<Short> existingIds = iaDAO.listAllergensOfPerson(personId).stream()
        .filter(a -> "AVOID".equalsIgnoreCase(a.getCategory()))
        .map(Allergen::getId)
        .collect(Collectors.toCollection(LinkedHashSet::new));

    // 差分削除
    Set<Short> toDelete = new LinkedHashSet<Short>(existingIds);
    toDelete.removeAll(newIds);
    for (Short id : toDelete) iaDAO.delete(personId, id);

    // 追加/更新
    for (Short id : newIds) {
      IndividualAllergy ia = new IndividualAllergy();
      ia.setPersonId(personId);
      ia.setAllergenId(id);
      ia.setNote(noteById.getOrDefault(id, ""));
      ia.setConfirmedAt(null);
      iaDAO.upsert(ia);
    }

    // 完了メッセージ → ホームへ
    ses.setAttribute("flash", "食べられない食材を登録しました");
    resp.sendRedirect(req.getContextPath() + "/user/home");
  }
}
