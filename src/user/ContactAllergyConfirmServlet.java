package user;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import bean.Allergen;
import bean.Individual;
import bean.User;
import dao.AllergenDAO;
import dao.IndividualDAO;

@WebServlet("/user/allergy/contact/confirm")
public class ContactAllergyConfirmServlet extends HttpServlet {
  private final AllergenDAO allergenDAO = new AllergenDAO();
  private final IndividualDAO individualDAO = new IndividualDAO();

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    req.setCharacterEncoding("UTF-8");

    // 認証チェック
    HttpSession ses = req.getSession(false);
    User user = (ses == null) ? null : (User) ses.getAttribute("user");
    if (user == null) { resp.sendRedirect(req.getContextPath() + "/user/login"); return; }

    // 入力取得
    String pid = req.getParameter("person_id");
    if (pid == null || pid.isEmpty()) { resp.sendRedirect(req.getContextPath()+"/user/allergy/contact"); return; }
    java.util.UUID personId;
    try { personId = java.util.UUID.fromString(pid); }
    catch (IllegalArgumentException e) { resp.sendRedirect(req.getContextPath()+"/user/allergy/contact"); return; }

    String[] codes = req.getParameterValues("allergen"); // 例: ["NICKEL","COBALT"]
    if (codes == null || codes.length == 0) {
      // 1件も選んでいない→編集画面に戻す（軽いメッセージ付き）
      req.setAttribute("flash", "少なくとも1つ選択してください。");
      // 編集画面へGETで戻す（person 固定）
      resp.sendRedirect(req.getContextPath()+"/user/allergy/contact?person="+personId.toString());
      return;
    }

    // 表示用に code→name を解決
    List<Allergen> contactList = allergenDAO.listByCategory("CONTACT");
    Map<String,String> codeToName = new HashMap<>();
    for (Allergen a : contactList) codeToName.put(a.getCode(), a.getNameJa());

    // 表示用リスト（選択順は送信順を尊重）
    List<String> selectedCodes = Arrays.asList(codes);
    List<String> selectedNames = selectedCodes.stream()
        .map(c -> codeToName.getOrDefault(c, c))
        .collect(Collectors.toList());

    // 対象児の表示名
    String personName = "";
    for (Individual p : individualDAO.listByUser(user.getId())) {
      if (p.getId().equals(personId)) { personName = p.getDisplayName(); break; }
    }

    // メモも後続保存に引き継ぐ（note_CODE）
    Map<String,String> notes = new LinkedHashMap<>();
    for (String code : selectedCodes) {
      String note = Optional.ofNullable(req.getParameter("note_" + code)).orElse("");
      notes.put(code, note);
    }

    // 確認JSPへ
    req.setAttribute("personId", personId);
    req.setAttribute("personName", personName);
    req.setAttribute("selectedCodes", selectedCodes);
    req.setAttribute("selectedNames", selectedNames);
    req.setAttribute("notes", notes); // 必要なら確認画面に表示

    req.getRequestDispatcher("/user/contact_allergy_confirm.jsp").forward(req, resp);
  }
}
