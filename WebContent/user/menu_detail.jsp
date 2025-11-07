<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html><html lang="ja"><head>
<meta charset="UTF-8"><title>献立詳細</title><meta name="viewport" content="width=device-width, initial-scale=1">
<style>
  body{background:#f7e1ca;margin:0;font-family:sans-serif}
  header{background:#f6e7be;padding:16px 20px;text-align:center}
  .wrap{max-width:860px;margin:20px auto;padding:0 16px}
  .card{background:#fff;border:2px solid #333;padding:16px}
  .img{max-width:100%;height:auto;border:1px solid #ccc;margin-top:8px}
  .chips{display:flex;flex-wrap:wrap;gap:8px;margin-top:8px}
  .chip{background:#e53935;color:#fff;padding:4px 10px;border-radius:16px}
</style>
</head><body>
<header><h2 style="margin:0">献立詳細</h2></header>
<div class="wrap">
  <div class="card">
    <h3 style="margin:0 0 6px 0">${menu.name}</h3>
    <div>${menu.menuDate}</div>
    <c:if test="${not empty menu.description}">
      <p style="white-space:pre-wrap">${menu.description}</p>
    </c:if>
    <c:if test="${not empty menu.imagePath}">
      <img class="img" src="${pageContext.request.contextPath}${menu.imagePath}" alt="献立画像">
    </c:if>

    <h4>主要アレルゲン</h4>
    <div class="chips">
      <c:forEach var="a" items="${allergens}">
        <span class="chip">${a.nameJa}</span>
      </c:forEach>
      <c:if test="${empty allergens}">登録なし</c:if>
    </div>

    <div style="margin-top:16px">
      <a href="${pageContext.request.contextPath}/user/menus?ym=${menu.menuDate.toString().substring(0,7)}">月表示に戻る</a>
    </div>
  </div>
</div>
</body></html>