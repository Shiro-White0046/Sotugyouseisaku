<%@ page contentType="text/html; charset=UTF-8" %>
<%
  request.setAttribute("pageTitle", "管理者ホーム");
%>

<jsp:include page="/header.jsp" />

<h2>今日の献立</h2>
<div class="menu-card">
  <% bean.Menu today = (bean.Menu)request.getAttribute("todayMenu"); %>
  <% if (today != null) { %>
    <div class="menu-name"><%= today.getName() %></div>
    <div class="menu-desc"><%= today.getDescription() != null ? today.getDescription() : "" %></div>
    <% if (today.getImagePath() != null && !today.getImagePath().isEmpty()) { %>
      <img class="menu-image" src="<%= request.getContextPath() + today.getImagePath() %>" alt="menu image">
    <% } %>
  <% } else { %>
    <div class="menu-empty">本日の献立は未登録です。</div>
  <% } %>
</div>

<jsp:include page="/footer.jsp" />
