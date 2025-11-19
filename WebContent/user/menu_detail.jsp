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
.title{font-size:20px;margin:8px 0 18px;color:#333;text-align:center;font-weight:800;letter-spacing:.03em}
.section-title{font-weight:800;margin:18px 0 10px;color:#5b4a3b;text-align:center}

.grid{display:grid;grid-template-columns: 2.1fr 1fr;gap:18px;align-items:start;margin-bottom:28px}
.left-card,.right-card{
  background:#fff;border:1px solid #e6d3bc;border-radius:12px;
  box-shadow:0 2px 6px rgba(0,0,0,.06);padding:14px 16px;
}
.menu-img{max-width:100%;height:auto;display:block;border:1px solid #eee;border-radius:8px}
.desc{margin-top:10px;white-space:pre-wrap;color:#333}

.label{font-size:12px;color:#7a6a5a;margin-bottom:2px}
.name{font-size:18px;font-weight:800;margin-bottom:10px;text-align:center;letter-spacing:.02em}
.msg{margin:8px 0 8px;color:#333;text-align:center;font-size:14px}

.chips{display:flex;flex-wrap:wrap;gap:10px 10px;justify-content:center}
.chip{
  background:#e53935;color:#fff;padding:6px 12px;border-radius:999px;
  font-weight:700;font-size:13px;line-height:1;
  box-shadow:0 1px 0 rgba(0,0,0,.08)
}

/* 黒リスト（その他）を見やすく） */
.others{
  margin-top:10px;padding-top:10px;border-top:1px dashed #e6d3bc;color:#444
}
.others .others-title{display:block;text-align:center;font-size:12px;color:#888;margin-bottom:6px}
.others ul{
  margin:0;padding-left:1.1em;
  display:grid;grid-template-columns:repeat(auto-fill,minmax(110px,1fr));
  column-gap:12px;row-gap:6px;
}
.others li{font-size:13px;line-height:1.35;list-style:disc}

/* 戻る */
.footer{margin-top:14px;text-align:center}
.btn-back{border:2px solid #333;background:#fff;padding:10px 18px;border-radius:10px;cursor:pointer}
@media (max-width:1000px){ .grid{ grid-template-columns:1fr } }
</style>
</head>
<body>



<%
  request.setAttribute("headerTitle", "献立");
%>
<jsp:include page="/header_user2.jsp" />
<!-- 子ども切替（同日付で personId を切り替える） -->
<form method="get" action="${pageContext.request.contextPath}/user/menu_detail" style="margin:8px 0;">
  <input type="hidden" name="date" value="${menuDate}" />
  <select name="personId" onchange="this.form.submit()">
    <c:forEach var="c" items="${children}">
      <option value="${c.id}" <c:if test="${c.id eq personId}">selected</c:if>>${c.displayName}</option>
    </c:forEach>
  </select>
  <span style="margin-left:8px;color:#555;"></span>
</form>
<div class="wrap">
  <!-- 日付タイトル（1回だけ） -->
  <div class="title">${headTitle}</div>

  <!-- セクション（朝食・昼食・夕食） -->
  <!-- ループはそのまま -->
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

  <!-- ✅ 赤：利用者に一致するものだけ -->
  <div class="chips">
    <c:forEach var="a" items="${sec.allergensUser}">
      <span class="chip">${a.nameJa}</span>
    </c:forEach>
    <c:if test="${empty sec.allergensUser and empty sec.allergensOther}">
      <span style="color:#666">登録なし</span>
    </c:if>
  </div>

  <!-- ✅ 黒：メニューに含まれるが利用者未登録のもの -->
  <c:if test="${not empty sec.allergensOther}">
    <div style="margin-top:8px;color:#222;font-size:14px;">
      <span>（他）</span>
      <c:forEach var="a" items="${sec.allergensOther}" varStatus="st">
        <span>${a.nameJa}</span><c:if test="${!st.last}">・</c:if>
      </c:forEach>
    </div>
  </c:if>
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
