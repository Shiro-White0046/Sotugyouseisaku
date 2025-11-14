<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%
  request.setAttribute("headerTitle", "利用者アレルギー一覧");
%>
<jsp:include page="/header2.jsp" />

<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<title>利用者アレルギー一覧</title>
<meta name="viewport" content="width=device-width, initial-scale=1">
<style>
  body { background:#f7e1ca; margin:0; font-family:"Hiragino Sans","Noto Sans JP",sans-serif; }
  h2 { text-align:center; margin:16px 0; font-size:22px; font-weight:700; }
  table { width:90%; max-width:1100px; margin:0 auto 30px; border-collapse:collapse; background:#fff; border-radius:10px; overflow:hidden; }
  th, td { border-bottom:1px solid #eee; padding:10px 12px; text-align:left; vertical-align:top; }
  th { background:#f8ebd8; font-weight:700; font-size:15px; }
  td { font-size:14px; line-height:1.8; white-space:normal; word-break:break-all; }
  .no-data { text-align:center; padding:20px; color:#555; }
</style>
</head>
<body>

<h2>利用者アレルギー一覧</h2>

<div class="searchbar">
  <!-- ★ Servlet のパスに合わせる -->
  <form method="get" action="${pageContext.request.contextPath}/admin/allergens">
    <input type="text" name="q"  value="${fn:escapeXml(param.q)}"  placeholder="名前で検索">
    <input type="text" name="aq" value="${fn:escapeXml(param.aq)}" placeholder="アレルギー名で検索（例：卵・乳・そば）">
    <button type="submit" class="btn">検索</button>
  </form>
</div>

<table>
  <thead>
    <tr>
      <th>名前</th>
      <th>食物性アレルギー</th>
      <th>食べられないもの</th>
      <th>接触性アレルギー</th>
    </tr>
  </thead>
<tbody>
  <c:forEach var="r" items="${rows}">
    <tr>
      <td>${r.displayName}</td>
      <td>
        <c:choose>
          <c:when test="${not empty r.foods}">
            <c:out value="${fn:replace(r.foods, '・', '<br>')}" escapeXml="false"/>
          </c:when>
          <c:otherwise>―</c:otherwise>
        </c:choose>
      </td>
      <td>
        <c:choose>
          <c:when test="${not empty r.avoids}">
            <c:out value="${fn:replace(r.avoids, '・', '<br>')}" escapeXml="false"/>
          </c:when>
          <c:otherwise>―</c:otherwise>
        </c:choose>
      </td>
      <td>
        <c:choose>
          <c:when test="${not empty r.contacts}">
            <c:out value="${fn:replace(r.contacts, '・', '<br>')}" escapeXml="false"/>
          </c:when>
          <c:otherwise>―</c:otherwise>
        </c:choose>
      </td>
    </tr>
  </c:forEach>
  <c:if test="${empty rows}">
    <tr><td colspan="4" class="no-data">該当するデータがありません。</td></tr>
  </c:if>
</tbody>
</table>

</body>
</html>
