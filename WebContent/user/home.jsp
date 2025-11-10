<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%
  // ページタイトル（header_user.jsp で使用）
  request.setAttribute("pageTitle", "利用者ホーム");
%>

<jsp:include page="/header_user.jsp" />

<h2>今日の献立</h2>
<div class="menu-card">

  <c:if test="${not empty todayMenu}">
    <div class="card">
      <h3>${todayMenu.menuDate} の献立</h3>

      <c:if test="${not empty todayMenu.imagePath}">
        <img src="${pageContext.request.contextPath}${todayMenu.imagePath}"
             alt="今日の献立画像"
             style="max-width:100%">
      </c:if>

      <c:if test="${not empty todayMenu.name}">
        <div class="menu-name">${todayMenu.name}</div>
      </c:if>

      <c:if test="${not empty todayMenu.description}">
        <div class="menu-desc">${todayMenu.description}</div>
      </c:if>
    </div>
  </c:if>

  <c:if test="${empty todayMenu}">
    <div class="card">
      <p>本日の献立は登録されていません。</p>
    </div>
  </c:if>

</div>

<jsp:include page="/footer.jsp" />
