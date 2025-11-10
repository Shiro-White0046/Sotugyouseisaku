<%@ page contentType="text/html; charset=UTF-8" %>
<%
  request.setAttribute("pageTitle", "管理者ホーム");
%>

<jsp:include page="/header.jsp" />

<h2>今日の献立</h2>
<div class="menu-card">

<c:if test="${not empty todayMenu}">
  <div class="card">
    <h3>${todayMenu.menuDate} の献立</h3>
    <c:if test="${not empty todayMenu.imagePath}">
      <img src="${pageContext.request.contextPath}${todayMenu.imagePath}" alt="今日の献立画像" style="max-width:100%">
    </c:if>
  </div>
</c:if>
<c:if test="${empty todayMenu}">
  <p>本日の献立は登録されていません。</p>
</c:if>

<jsp:include page="/footer.jsp" />
