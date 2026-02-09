// src/user/ProhibitedInputServlet.java
package user;

import java.io.IOException;
import java.time.LocalDate;
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
import infra.AuditLogger;   // ★ 追加

/**
 * 食べられない食材入力（AVOID）画面
 * ※ header_user.jsp のリンクは /user/avoid 固定なので、ここで受ける
 */
@WebServlet(urlPatterns = {"/user/avoid"}) // ← header_user.jsp のURLに合わせる
public class ProhibitedInputServlet extends HttpServlet {

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

    // ユーザー配下の個人
    List<Individual> persons = individualDAO.listByUser(user.getId());
    if (persons.isEmpty()) {
      req.setAttribute("pageTitle", "食べられない食材入力");
      req.setAttribute("flash", "まずはお子さま（個人）を登録してください。");
      req.getRequestDispatcher("user/prohibited_input.jsp")
         .forward(req, resp);
      return;
    }

    // ===== ★ 対象児の決定（共通ロジック） =====
    UUID personId = resolvePersonId(req, ses, persons);

    // ★ 決定した対象児をセッションに保存（他画面と共有）
    if (ses != null) {
      ses.setAttribute("currentPersonId", personId);
    }

    // AVOID マスタ
    List<Allergen> avoidList = allergenDAO.listByCategory("AVOID");

    // 既存登録（その人の全アレルギー→AVOIDだけ抽出）
    List<Allergen> existing = iaDAO.listAllergensOfPerson(personId);
    Set<String> selectedCodes = existing.stream()
        .filter(a -> "AVOID".equalsIgnoreCase(a.getCategory()))
        .map(Allergen::getCode)
        .collect(Collectors.toCollection(LinkedHashSet::new));

    // 既存メモ（avoid に紐づく note）
    List<IndividualAllergy> rows = iaDAO.listByPerson(personId);
    Map<Short,String> idToNote = new LinkedHashMap<Short,String>();
    for (IndividualAllergy ia : rows) {
      idToNote.put(ia.getAllergenId(), ia.getNote() == null ? "" : ia.getNote());
    }

    // 画面へ
    req.setAttribute("pageTitle", "食べられない食材入力");
    req.setAttribute("persons", persons);
    req.setAttribute("personId", personId);
    req.setAttribute("avoidList", avoidList);
    req.setAttribute("selectedCodes", selectedCodes);
    req.setAttribute("idToNote", idToNote);

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

    String otherText = req.getParameter("other_free_text");
    System.out.println("otherText = " + otherText);




    // ★ POST 時にも currentPersonId を更新しておくと、保存後も対象児が維持される
    if (ses != null) {
      ses.setAttribute("currentPersonId", personId);
    }

    String[] codes = req.getParameterValues("avoid"); // 例: ["AVOID_PORK", "AVOID_BEEF"]

    // AVOID マスタ（code -> id）
    List<Allergen> avoidList = allergenDAO.listByCategory("AVOID");
    Map<String,Short> codeToId = new LinkedHashMap<String,Short>();
    for (Allergen a : avoidList) codeToId.put(a.getCode(), a.getId());

    // 既存（idセット）
    Set<Short> existingIds = new LinkedHashSet<Short>();
    for (Allergen a : iaDAO.listAllergensOfPerson(personId)) {
      if ("AVOID".equalsIgnoreCase(a.getCategory())) existingIds.add(a.getId());
    }

    // 新規（選択分の id セット + note）
    Set<Short> newIds = new LinkedHashSet<Short>();
    Map<Short,String> noteById = new LinkedHashMap<Short,String>();
    if (codes != null) {
      for (String code : codes) {
        Short id = codeToId.get(code);
        if (id != null) {
          newIds.add(id);
          String note = req.getParameter("note_" + code);
          if (note == null) note = "";
          noteById.put(id, note);
        }
      }
    }

    // 削除(既存 − 新規)
    Set<Short> toDelete = new LinkedHashSet<Short>(existingIds);
    toDelete.removeAll(newIds);

    iaDAO.deleteByCategory(personId, "AVOID");

    // 追加/更新（新規側）
    for (Short id : newIds) {
      IndividualAllergy ia = new IndividualAllergy();
      ia.setPersonId(personId);
      ia.setAllergenId(id);
      String note = noteById.get(id);
      if (note != null && note.trim().isEmpty()) note = null; // 空文字 → NULL
      ia.setNote(note);
      ia.setConfirmedAt(LocalDate.now()); // ここで today をセットしてもOK（DAO側でも可）
      iaDAO.upsert(ia);
    }

    // ★ 操作ログ（食べられない食材の更新）
    AuditLogger.logGuardianFromSession(
        req,
        "update_allergy",
        "individual_allergies",
        personId.toString()
    );

    // 完了→ホームへ（alert用フラッシュ）
    ses.setAttribute("flashMessage", "食べられない食材を登録しました");
    resp.sendRedirect(req.getContextPath() + "/user/home");
  }

  // ===== ★ MenusCalendar と同じ対象児決定ロジック =====
  private UUID resolvePersonId(HttpServletRequest req, HttpSession ses, List<Individual> persons) {
    UUID personId = null;

    // ① クエリ ?person= or ?personId=
    String personParam = req.getParameter("person");
    if (personParam == null || personParam.isEmpty()) {
      personParam = req.getParameter("personId");
    }
    if (personParam != null && !personParam.isEmpty()) {
      try {
        personId = UUID.fromString(personParam);
      } catch (IllegalArgumentException ignore) {
        personId = null;
      }
    }

    // ② セッション currentPersonId
    if (personId == null && ses != null) {
      Object attr = ses.getAttribute("currentPersonId");
      if (attr instanceof UUID) {
        personId = (UUID) attr;
      } else if (attr instanceof String) {
        try { personId = UUID.fromString((String) attr); } catch (Exception ignore) {}
      }
    }

    // ③ まだ null なら先頭の子
    if (personId == null) {
      personId = persons.get(0).getId();
    }

    // 念のため「このユーザーの子どもか」をチェック
    boolean belongs = false;
    for (Individual p : persons) {
      if (p.getId().equals(personId)) { belongs = true; break; }
    }
    if (!belongs) {
      personId = persons.get(0).getId();
    }

    return personId;
  }
}
