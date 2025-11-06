package user;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/user/allergy/register")
public class FoodAllergyRegisterServlet extends HttpServlet {
//	private final AllergenDAO iaDao = new AllergenDAO();

	  @Override
	  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
	      throws ServletException, IOException {

	    req.setCharacterEncoding("UTF-8");

	    // 1) 同意チェック（必須）
	    if (!"agree".equals(req.getParameter("consent"))) {
	      req.setAttribute("error", "同意が必須です。");
	      req.getRequestDispatcher("/WEB-INF/views/user/allergy_confirm.jsp").forward(req, resp);
	      return;
	    }

	    // 2) パラメータ取得
	    String[] ids = req.getParameterValues("allergenIds"); // hidden で引き継ぎ
	    String otherFlag = req.getParameter("allergenOtherFlag");
	    String otherName = req.getParameter("allergenOtherName");

	    // 3) 対象児童IDの決定（単一/複数アカウントでの取得方法はプロジェクト規約に合わせて）
	    //   ここではセッション等から拾う想定にしています。
	    Integer individualId = (Integer) req.getSession().getAttribute("targetIndividualId");
	    if (individualId == null) {
	      // 必要に応じて、入力画面に戻す or 例外
	      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "対象児童が特定できません。");
	      return;
	    }

	    // ログインユーザID（監査用）
	    Integer userId = (Integer) req.getSession().getAttribute("loginUserId");
	    if (userId == null) userId = 0; // 監査不要なら0でも可

	    // 4) ID配列の正規化（null/重複/変換エラー除去）
	    Set<Short> uniqueIds = new LinkedHashSet<>();
	    if (ids != null) {
	      for (String s : ids) {
	        try {
	          uniqueIds.add(Short.parseShort(s));
	        } catch (NumberFormatException ignore) {
	          // 不正値はスキップ
	        }
	      }
	    }

	    if (uniqueIds.isEmpty() && !("1".equals(otherFlag) && otherName != null && !otherName.trim().isEmpty())) {
	      // 何も登録するものがない（通常はここに来ない想定）
	      req.setAttribute("error", "登録対象がありません。");
	      req.getRequestDispatcher("/WEB-INF/views/user/allergy_confirm.jsp").forward(req, resp);
	      return;
	    }

	    // 5) 登録処理
	    try {
	       //既知アレルゲン（マスタIDあり）は upsert。重症度は当面1（必要なら確認画面で持たせてください）
	      for (Short allergenId : uniqueIds) {
	    	  System.out.println(allergenId);

	        //iaDao.upsert(individualId, allergenId, 1 /* default severity */, userId);
	      }

	      // 「その他」があれば別テーブルへ
	      //if ("1".equals(otherFlag) && otherName != null) {
	        //String trimmed = otherName.trim();
	        //if (!trimmed.isEmpty()) {
	          //iaDao.insertOtherName(individualId, trimmed, userId);
	       // }
	     // }

	      // 6) 完了（重複送信防止のためリダイレクト）
	      req.getRequestDispatcher("/user/home.jsp").forward(req, resp);
	    } catch (Exception e) {
	      e.printStackTrace();
	      req.setAttribute("error", "登録時にエラーが発生しました。もう一度お試しください。");
	      // 失敗時は確認画面に戻す（hiddenを再セットする場合は必要に応じて詰め直す）
	      req.setAttribute("originalIds", ids == null ? new String[0] : ids);
	      req.setAttribute("otherFlag", otherFlag);
	      req.setAttribute("otherName", otherName);
	      req.getRequestDispatcher("/WEB-INF/views/user/allergy_confirm.jsp").forward(req, resp);
	    }
	  }
}