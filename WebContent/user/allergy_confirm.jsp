<%
  response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
  response.setHeader("Pragma", "no-cache");
  response.setDateHeader("Expires", 0);
%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<title>食物アレルギー入力（確認）</title>
<meta name="viewport" content="width=device-width, initial-scale=1">
<style>
  body{background:#f7e1ca;margin:0;font-family:sans-serif}
  header{background:#f6e7be;padding:16px 20px;text-align:center}
  .wrap{max-width:960px;margin:20px auto;padding:0 16px}
  /* ① スクロール枠 */
  .panel{background:#fff;border:2px solid #333;height:220px;overflow-y:auto;padding:16px}
  .list{margin:0;padding-left:20px}
  .note{margin:12px 0}
  /* ④～⑤ チェックと文言 */
  .agree{display:flex;align-items:center;gap:16px;margin:18px 0}
  .agree input[type="checkbox"]{width:28px;height:28px}
  /* ⑥～⑦ ボタン */
  .actions{display:flex;justify-content:space-between;margin-top:24px}
  .btn{border:2px solid #d8c68f;background:#faefcf;padding:14px 28px;border-radius:22px;cursor:pointer}
  .btn-primary{background:#f6e7be;border-color:#e6d595}
</style>
</head>
<body>
<header><h2 style="margin:0">食物アレルギー入力（確認）</h2></header>

<div class="wrap">
  <!-- POSTで登録サーブレットへ -->
  <form action="${pageContext.request.contextPath}/user/allergy/register" method="post">
    <input type="hidden" name="_csrf" value="${csrfToken}"/>

    <!-- 次のPOSTに引き継ぐ hidden -->
    <c:forEach var="id" items="${originalIds}">
      <input type="hidden" name="allergenIds" value="${id}">
    </c:forEach>
    <input type="hidden" name="allergenOtherFlag" value="${otherFlag}">
    <input type="hidden" name="allergenOtherName" value="${otherName}">

    <!-- ①② 選択一覧（スクロールボックス） -->
    <div class="panel">
      <ul class="list">
        <c:forEach var="a" items="${selectedAllergens}">
          <li>${a.nameJa}</li>
        </c:forEach>
      </ul>
    </div>

    <!-- ③ 確認メッセージ -->
    <p class="note">以上で登録します。よろしいでしょうか。</p>

    <!-- ④⑤ 同意チェック -->
    <div class="agree">
      <input id="consent" name="consent" type="checkbox" value="agree" required>
      <label for="consent">アレルギー情報の提供に同意しますか？</label>
    </div>

    <!-- ⑥⑦ 戻る／登録ボタン -->
    <div class="actions">
      <button type="button" class="btn" onclick="history.back()">戻る</button>
      <button type="submit" class="btn btn-primary">登録</button>
    </div>
  </form>
</div>
</body>
</html>
