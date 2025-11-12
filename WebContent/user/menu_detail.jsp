<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<title>献立（詳細）</title>
<meta name="viewport" content="width=device-width, initial-scale=1">
<style>
  body{background:#f7e1ca;margin:0;font-family:sans-serif}
  .wrap{max-width:1100px;margin:16px auto;padding:0 16px}
  .title{font-size:18px;margin:8px 0 16px 0;color:#333;text-align:center}
  .section-title{font-weight:700;margin:16px 0 8px 0;color:#555}
  .grid{display:grid;grid-template-columns: 2.1fr 1fr;gap:16px;align-items:start;margin-bottom:24px}
  .left-card{background:#fff;border:2px solid #333;min-height:340px;padding:16px}
  .menu-img{max-width:100%;height:auto;display:block;border:1px solid #ccc}
  .desc{margin-top:8px;white-space:pre-wrap;color:#333}
  .right-card{background:#fff;border:2px solid #333;min-height:340px;padding:14px}
  .label{font-size:12px;color:#666;margin-bottom:4px}
  .name{font-size:18px;font-weight:600;margin-bottom:12px}
  .msg{margin:10px 0 6px 0;color:#333}
  .chips{display:flex;flex-wrap:wrap;gap:8px}
  .chip{background:#e53935;color:#fff;padding:4px 10px;border-radius:16px}
  .footer{margin-top:10px;text-align:center}
  .btn-back{border:2px solid #333;background:#fff;padding:10px 18px;border-radius:8px;cursor:pointer}
</style>
</head>
<body>

<%
  request.setAttribute("headerTitle", "献立");
%>
<jsp:include page="/header_user2.jsp" />

<div class="wrap">
  <!-- 日付タイトル（1回だけ） -->
  <div class="title">${headTitle}</div>

  <!-- セクション（朝食・昼食・夕食） -->
  <!-- ループはそのまま -->
<c:forEach var="sec" items="${sections}">
  <div class="section-title">${sec.label}</div>

  <div class="grid">
    <section class="left-card">
      <%-- ① 食事画像があれば最優先 --%>
      <c:set var="imgMeal" value="${sec.imagePath}" />
      <c:set var="imgDay"  value="${menuImagePath}" />
      <c:choose>
        <c:when test="${not empty imgMeal}">
          <c:choose>
            <c:when test="${fn:startsWith(imgMeal, '/')}">
              <img class="menu-img" src="${pageContext.request.contextPath}${imgMeal}" alt="献立画像">
            </c:when>
            <c:otherwise>
              <img class="menu-img" src="${pageContext.request.contextPath}/${imgMeal}" alt="献立画像">
            </c:otherwise>
          </c:choose>
        </c:when>

        <%-- ② 無ければ日単位の画像にフォールバック --%>
        <c:when test="${not empty imgDay}">
          <c:choose>
            <c:when test="${fn:startsWith(imgDay, '/')}">
              <img class="menu-img" src="${pageContext.request.contextPath}${imgDay}" alt="献立画像">
            </c:when>
            <c:otherwise>
              <img class="menu-img" src="${pageContext.request.contextPath}/${imgDay}" alt="献立画像">
            </c:otherwise>
          </c:choose>
        </c:when>

        <%-- ③ どちらも無ければプレースホルダ --%>
        <c:otherwise>
          <div style="height:260px;display:flex;align-items:center;justify-content:center;color:#666;">
            写真
          </div>
        </c:otherwise>
      </c:choose>

      <c:if test="${not empty sec.description}">
        <div class="desc">${sec.description}</div>
      </c:if>
    </section>

    <!-- 右側（メニュー名・アレルゲン）は従来どおり -->
    <aside class="right-card">
      <div class="label">メニュー名</div>
      <div class="name">${sec.name}</div>
      <div class="msg">このメニューには以下のものが含まれています！</div>
      <div class="chips">
        <c:forEach var="a" items="${sec.allergens}">
          <span class="chip">${a.nameJa}</span>
        </c:forEach>
        <c:if test="${empty sec.allergens}">
          <span style="color:#666">登録なし</span>
        </c:if>
      </div>
    </aside>
  </div>
</c:forEach>

  <!-- 戻る（カレンダーへ） -->
  <div class="footer">
    <button type="button" class="btn-back"
            onclick="location.href='${pageContext.request.contextPath}/user/menuscalendar?ym=${ym}'">戻る</button>
  </div>
</div>

</body>
</html>
