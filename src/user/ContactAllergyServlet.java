// src/user/ContactAllergyServlet.java
package user;

import java.io.IOException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
 * 接触性アレルギーの入力・保存
 * GET  : 入力画面表示
 * POST : 保存してホームへリダイレクト（セッション flash 付き）
 */
@WebServlet("/user/allergy/contact")
public class ContactAllergyServlet extends HttpServlet {
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

    // 対象児一覧（single でも 1 件返る想定）
    List<Individual> persons = individualDAO.listByUser(user.getId());
    if (persons.isEmpty()) {
      req.setAttribute("flash", "まずはお子さま（個人）を登録してください。");
      req.getRequestDispatcher("/user/home.jsp").forward(req, resp);
      return;
    }

    // クエリ ?person= 指定があれば優先、なければ先頭
    String personParam = req.getParameter("person");
    UUID personId = null;
    if (personParam != null && !personParam.isEmpty()) {
      try { personId = UUID.fromString(personParam); } catch (Exception ignore) {}
    }
    if (personId == null) personId = persons.get(0).getId();

    // CONTACT の候補（マスタ）
    List<Allergen> contactList = allergenDAO.listByCategory("CONTACT");

    // 既存登録（全部）→ CONTACT だけ抽出して code セット化
    List<Allergen> existingAllergens = iaDAO.listAllergensOfPerson(personId);
    Set<String> selectedCodes = existingAllergens.stream()
        .filter(a -> "CONTACT".equalsIgnoreCase(a.getCategory()))
        .map(Allergen::getCode)
        .collect(Collectors.toCollection(LinkedHashSet::new));

    // コード → メモ（CONTACT だけ）
    List<IndividualAllergy> existingRows = iaDAO.listByPerson(personId);
    Map<String, String> noteMap = new LinkedHashMap<String, String>();
    // id -> code 解決用マップ
    Map<Short, String> idToCode = contactList.stream()
        .collect(Collectors.toMap(Allergen::getId, Allergen::getCode));
    for (IndividualAllergy ia : existingRows) {
      String code = idToCode.get(ia.getAllergenId());
      if (code != null) noteMap.put(code, ia.getNote() == null ? "" : ia.getNote());
    }

    // ビューへ
    req.setAttribute("persons", persons);
    req.setAttribute("personId", personId);
    req.setAttribute("contactList", contactList);
    req.setAttribute("selectedCodes", selectedCodes);
    req.setAttribute("noteMap", noteMap);

    // 入力 JSP を表示（パスはあなたの配置に合わせて）
    req.getRequestDispatcher("/user/contact_allergy.jsp").forward(req, resp);
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
    String[] codes = req.getParameterValues("allergen"); // 例: ["NICKEL","LATEX"]

    // CONTACT マスタ（code -> id）
    List<Allergen> contactList = allergenDAO.listByCategory("CONTACT");
    Map<String, Short> codeToId = contactList.stream()
        .collect(Collectors.toMap(Allergen::getCode, Allergen::getId));

    // 既存 CONTACT（id セット）
    Set<Short> existingContactIds = iaDAO.listAllergensOfPerson(personId).stream()
        .filter(a -> "CONTACT".equalsIgnoreCase(a.getCategory()))
        .map(Allergen::getId)
        .collect(Collectors.toCollection(LinkedHashSet::new));

    // 新規選択 CONTACT（id セット）＋メモ
    Set<Short> newContactIds = new LinkedHashSet<Short>();
    Map<Short, String> noteById = new LinkedHashMap<Short, String>();
    if (codes != null) {
      for (String code : codes) {
        Short id = codeToId.get(code);
        if (id != null) {
          newContactIds.add(id);
          String note = Optional.ofNullable(req.getParameter("note_" + code)).orElse("");
          noteById.put(id, note);
        }
      }
    }

    // 差分：削除（既存 − 新規）
    Set<Short> toDelete = new LinkedHashSet<Short>(existingContactIds);
    toDelete.removeAll(newContactIds);

    iaDAO.deleteByCategory(personId, "CONTACT");


    // 追加/更新（新規集合）
    for (Short id : newContactIds) {
      IndividualAllergy ia = new IndividualAllergy();
      ia.setPersonId(personId);
      ia.setAllergenId(id);
      String note = noteById.get(id);
      if (note != null && note.trim().isEmpty()) note = null; // 空文字 → NULL
      ia.setNote(note);
      ia.setConfirmedAt(LocalDate.now()); // ここで today をセットしてもOK（DAO側でも可）
      iaDAO.upsert(ia);
    }

    // フラッシュをセッションに格納してホームへ（PRG）
    ses.setAttribute("flashMessage", "アレルギーを登録しました");
    resp.sendRedirect(req.getContextPath() + "/user/home");
  }
}
