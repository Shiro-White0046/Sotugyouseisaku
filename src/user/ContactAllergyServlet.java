package user;

import java.io.IOException;
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

@WebServlet("/user/allergy/contact")
public class ContactAllergyServlet extends HttpServlet {

  private final AllergenDAO allergenDAO = new AllergenDAO();
  private final IndividualDAO individualDAO = new IndividualDAO();
  private final IndividualAllergyDAO iaDAO = new IndividualAllergyDAO();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    HttpSession ses = req.getSession(false);
    User user = (ses == null) ? null : (User) ses.getAttribute("user");
    if (user == null) { resp.sendRedirect(req.getContextPath() + "/user/login"); return; }

    // 対象児一覧（singleでも1件返る）
    List<Individual> persons = individualDAO.listByUser(user.getId());
    if (persons.isEmpty()) {
      req.setAttribute("flash", "まずはお子さま（個人）を登録してください。");
      req.getRequestDispatcher("/user/home.jsp").forward(req, resp);
      return;
    }

    // クエリ?person= 指定があればそれを優先、なければ先頭
    String personParam = req.getParameter("person");
    UUID personId = null;
    if (personParam != null && !personParam.isEmpty()) {
      try { personId = java.util.UUID.fromString(personParam); } catch (Exception ignore) {}
    }
    if (personId == null) personId = persons.get(0).getId();

    // CONTACTの候補（マスタ）
    List<Allergen> contactList = allergenDAO.listByCategory("CONTACT");

    // 登録済み（全部）→ CONTACTだけ抽出
    List<Allergen> existingAllergens = iaDAO.listAllergensOfPerson(personId);
    Set<String> selectedCodes = existingAllergens.stream()
        .filter(a -> "CONTACT".equalsIgnoreCase(a.getCategory()))
        .map(Allergen::getCode)
        .collect(Collectors.toCollection(LinkedHashSet::new));

    // コード→メモのMap（CONTACTだけ）
    List<IndividualAllergy> existingRows = iaDAO.listByPerson(personId);
    Map<String,String> noteMap = new LinkedHashMap<>();
    // allergen_id -> code を解決するためにCONTACTの id->code マップを作る
    Map<Short,String> idToCode = contactList.stream()
        .collect(Collectors.toMap(Allergen::getId, Allergen::getCode));
    for (IndividualAllergy ia : existingRows) {
    	  String code = idToCode.get(ia.getAllergenId());
    	  if (code != null) noteMap.put(code, ia.getNote() == null ? "" : ia.getNote());
    }


    // ビュー渡し
    req.setAttribute("persons", persons);
    req.setAttribute("personId", personId);
    req.setAttribute("contactList", contactList);
    req.setAttribute("selectedCodes", selectedCodes);
    req.setAttribute("noteMap", noteMap);

    req.getRequestDispatcher("/user/contact_allergy.jsp").forward(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    req.setCharacterEncoding("UTF-8");
    HttpSession ses = req.getSession(false);
    User user = (ses == null) ? null : (User) ses.getAttribute("user");
    if (user == null) { resp.sendRedirect(req.getContextPath() + "/user/login"); return; }

    UUID personId = java.util.UUID.fromString(req.getParameter("person_id"));
    String[] codes = req.getParameterValues("allergen"); // 例: ["NICKEL","LATEX"]

    // CONTACTマスタ（code→id 解決用）
    List<Allergen> contactList = allergenDAO.listByCategory("CONTACT");
    Map<String,Short> codeToId = contactList.stream()
        .collect(Collectors.toMap(Allergen::getCode, Allergen::getId));

    // 既存のCONTACTセット（id）
    Set<Short> existingContactIds = iaDAO.listAllergensOfPerson(personId).stream()
        .filter(a -> "CONTACT".equalsIgnoreCase(a.getCategory()))
        .map(Allergen::getId)
        .collect(Collectors.toCollection(LinkedHashSet::new));

    // 新規選択のCONTACTセット（id）
    Set<Short> newContactIds = new LinkedHashSet<>();
    Map<Short,String> noteById = new LinkedHashMap<>();
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

    // 差分：削除対象（既存 − 新規）
    Set<Short> toDelete = new LinkedHashSet<>(existingContactIds);
    toDelete.removeAll(newContactIds);

    // 差分：追加/更新対象（新規集合）
    Set<Short> toUpsert = newContactIds;

    // DELETE
    for (Short id : toDelete) {
      iaDAO.delete(personId, id);
    }

    // UPSERT（noteを反映、confirmed_atはUIで扱わないのでNULL）
    for (Short id : toUpsert) {
      IndividualAllergy ia = new IndividualAllergy();
      ia.setPersonId(personId);
      ia.setAllergenId(id);
      ia.setNote(noteById.getOrDefault(id, ""));
      ia.setConfirmedAt(null);
      iaDAO.upsert(ia);
    }

    req.setAttribute("flash", "接触性アレルギーを保存しました。");
    // PRGにしても良いが、そのまま再表示要求に合わせる
    doGet(req, resp);
  }
}
