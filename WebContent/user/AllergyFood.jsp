<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html><html lang="ja"><head>
<meta charset="UTF-8"><title>食物アレルギー</title>
<meta name="viewport" content="width=device-width, initial-scale=1">

<link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">

<style>
  body{background:#f7e1ca;margin:0;font-family:sans-serif}
  header{background:#f6e7be;padding:16px 20px;position:sticky;top:0;z-index:1;text-align:center}
  .wrap{max-width:960px;margin:20px auto;padding:0 16px}
  .panel{background:#fff; border:2px solid #333; height:300px; overflow-y:auto; padding:16px}
  .grid{display:flex; flex-wrap:wrap; gap:28px 36px}
  .item{display:flex; align-items:center; gap:8px; min-width:120px}
  .other{display:flex; align-items:center; gap:8px; margin-top:16px}
  .actions{display:flex; justify-content:space-between; margin-top:28px}
  .btn{border:2px solid #d8c68f; background:#faefcf; padding:14px 28px; border-radius:22px; cursor:pointer}
  .btn-primary{background:#f6e7be; border-color:#e6d595}
  /* ①～⑤の見た目イメージに近づけるための軽い調整 */
  .panel{box-shadow:inset 0 0 0 2px #000}
</style>



</head><body>
<header><h2 style="margin:0">食物アレルギー入力</h2></header>

  <div class="wrap">
    <form action="${pageContext.request.contextPath}/user/allergy/confirm" method="post">
      <input type="hidden" name="_csrf" value="${csrfToken}"/>

      <!-- ① スクロールボックス -->
      <div class="panel" role="group" aria-labelledby="allergenGroupLabel">
        <span id="allergenGroupLabel" class="sr-only">アレルゲン選択</span>

        <!-- ② DBからチェックボックスを並べる -->
        <div class="grid">
          <c:forEach var="a" items="${allergenlist}">
            <label class="item">
              <input type="checkbox" name="allergenIds" value="${a.id}"
                      />
              <span>${a.nameJa}</span>
            </label>
          </c:forEach>
        </div>

        <!-- ③ 「その他」+ テキスト -->
        <div class="other">
          <label class="item" style="margin:0">
            <input id="otherCheck" type="checkbox" name="allergenOtherFlag" value="1"
                   onclick="document.getElementById('otherName').disabled = !this.checked">
            <span>その他</span>
          </label>
          <input id="otherName" type="text" name="allergenOtherName" maxlength="50" disabled
                 placeholder="その他のアレルゲン名">
        </div>
      </div>

      <!-- ④ 戻る ／ ⑤ 次へ -->
      <div class="actions">
        <button type="button" class="btn" onclick="history.back()">戻る</button>
        <button type="submit" class="btn btn-primary" >次へ</button>
      </div>
    </form>
  </div>

</body></html>
