package user;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import bean.Allergen;
import dao.AllergenDAO;

@WebServlet("/user/allergy/confirm")
public class FoodAllergyConfirmServlet extends HttpServlet {
  private AllergenDAO allergenDAO = new AllergenDAO();

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    req.setCharacterEncoding("UTF-8");

    // 入力画面で送られてきた値
    String[] ids = req.getParameterValues("allergenIds"); // 複数
    String otherFlag = req.getParameter("allergenOtherFlag");
    String otherName = req.getParameter("allergenOtherName");

    if (ids == null || ids.length == 0) {
      // 最低1件必須（入力画面に戻すでもOK）
      req.setAttribute("error", "アレルゲンを1件以上選択してください。");
      req.getRequestDispatcher("/user/allergy_input.jsp").forward(req, resp);
      return;
    }

    // ID→名称を解決
    List<Short> idList = new ArrayList<>();
    for (String s : ids) idList.add(Short.parseShort(s));
    List<Allergen> selected = allergenDAO.findByIdsPreserveOrder(idList);

 // 「その他」がチェックされ名前があれば表示対象に加える
    if ("1".equals(otherFlag) && otherName != null && !otherName.trim().isEmpty()) {
      Allergen oth = new Allergen();
      oth.setId((short) -1);          // 仮ID（保存時の分岐用）
      oth.setNameJa(otherName.trim());
      selected.add(oth);
    }

    // 確認JSP用に渡す
    req.setAttribute("selectedAllergens", selected);
    req.setAttribute("originalIds", ids);            // 次のPOSTに引き継ぐため
    req.setAttribute("otherFlag", otherFlag);
    req.setAttribute("otherName", otherName);

    // CSRFトークンを引き継ぐならここで再発行/渡し
    req.getRequestDispatcher("/user/allergy_confirm.jsp").forward(req, resp);
  }
}