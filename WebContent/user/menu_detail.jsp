<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <title>献立詳細</title>
  <meta name="viewport" content="width=device-width, initial-scale=1">

  <!-- 本来は style.css に寄せる。動作確認用に最小限だけ同梱 -->
  <style>
    body{background:#f7e1ca;margin:0;font-family:sans-serif}
    header{background:#f6e7be;padding:16px 20px;text-align:center}
    .wrap{max-width:960px;margin:20px auto;padding:0 16px}
    .card{background:#fff;border:2px solid #333;padding:16px}
    .img{max-width:100%;height:auto;border:1px solid #ccc;margin-top:8px}
    .meal{border-top:1px dashed #aaa;margin-top:16px;padding-top:12px}
    .items{margin-top:8px}
    .item{padding:6px 0;border-bottom:1px solid #eee}
    .chips{display:flex;flex-wrap:wrap;gap:8px;margin-top:6px}
    .chip{background:#e53935;color:#fff;padding:2px 10px;border-radius:14px;font-size:12px}
    .muted{color:#666}
    .back{margin-top:16px}
    table { width:100%; border-collapse:collapse }
    th,td { padding:6px 8px; text-align:left; vertical-align:top }
    th { width:120px }
  </style>
</head>
<body>
<header><h2 style="margin:0">献立詳細</h2></header>

<div class="wrap">
  <div class="card">

    <!-- 日付・画像（day は MenuDay） -->
    <table>
      <tr>
        <th>日付</th>
        <td><c:out value="${date}" /></td>
      </tr>
      <c:if test="${not empty day.imagePath}">
        <tr>
          <th>画像</th>
          <td>
            <img class="img"
                 src="${pageContext.request.contextPath}${day.imagePath}"
                 alt="献立画像">
          </td>
        </tr>
      </c:if>
    </table>

    <!-- 朝・昼・夜（meals は List<MenuMeal>） -->
    <c:if test="${empty meals}">
      <p class="muted" style="margin-top:12px">この日はまだ献立が登録されていません。</p>
    </c:if>

    <c:forEach var="m" items="${meals}">
      <div class="meal">
        <h3 style="margin:0 0 4px 0">
          <c:choose>
            <c:when test="${m.mealSlot == 'breakfast'}">朝</c:when>
            <c:when test="${m.mealSlot == 'lunch'}">昼</c:when>
            <c:when test="${m.mealSlot == 'dinner'}">夜</c:when>
            <c:otherwise>${m.mealSlot}</c:otherwise>
          </c:choose>
          ：<c:out value="${m.name}" />
        </h3>
        <c:if test="${not empty m.description}">
          <div class="muted" style="white-space:pre-wrap"><c:out value="${m.description}" /></div>
        </c:if>

        <!-- 品目一覧（itemsMap は mealId→List<MenuItem> の Map） -->
        <div class="items">
          <c:set var="items" value="${itemsMap[m.id]}" />
          <c:if test="${empty items}">
            <div class="muted">品目は未登録です。</div>
          </c:if>

          <c:forEach var="it" items="${items}">
            <div class="item">
              <strong><c:out value="${it.itemOrder}" />. <c:out value="${it.name}" /></strong>
              <c:if test="${not empty it.note}">
                <div class="muted" style="white-space:pre-wrap"><c:out value="${it.note}" /></div>
              </c:if>

              <!-- 品目×アレルゲン（allergensMap は itemId→List<Allergen> の Map） -->
              <c:set var="als" value="${allergensMap[it.id]}" />
              <div class="chips">
                <c:forEach var="a" items="${als}">
                  <span class="chip"><c:out value="${a.nameJa}" /></span>
                </c:forEach>
                <c:if test="${empty als}">
                  <span class="muted">アレルゲン登録なし</span>
                </c:if>
              </div>
            </div>
          </c:forEach>
        </div>
      </div>
    </c:forEach>

    <!-- 戻るリンク（月表示へ） -->
    <div class="back">
      <a href="${pageContext.request.contextPath}/user/menus?ym=${fn:substring(date,0,7)}">月表示に戻る</a>
    </div>
  </div>
</div>

</body>
</html>
