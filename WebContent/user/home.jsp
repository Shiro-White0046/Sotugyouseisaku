<%@ page contentType="text/html; charset=UTF-8" %>
<jsp:include page="/header_user.jsp"/>

<main class="content">
  <h2>今日の献立</h2>

  <%
    // サーブレット側でセットしておく想定の属性名
    String menuName = (String) request.getAttribute("todayMenuName");
    String menuDesc = (String) request.getAttribute("todayMenuDesc");
    String menuImg  = (String) request.getAttribute("todayMenuImageUrl"); // 例: /images/menus/2025-11-05.jpg
  %>

  <div class="menu-card">
    <%
      if (menuName == null && menuImg == null) {
    %>
      <div class="menu-empty">本日の献立は未登録です。</div>
      <div style="margin-top:8px;">
        <a href="<%= request.getContextPath() %>/user/menus" class="button ghost">献立一覧へ</a>
      </div>
    <%
      } else {
    %>
      <div class="menu-name"><%= (menuName != null ? menuName : "本日の献立") %></div>
      <% if (menuDesc != null) { %>
        <div class="menu-desc"><%= menuDesc %></div>
      <% } %>
      <% if (menuImg != null) { %>
        <img class="menu-image" src="<%= request.getContextPath() + menuImg %>" alt="今日の献立画像">
      <% } %>
    <%
      }
    %>
  </div>
</main>

</div><!-- /.layout -->
</body>
</html>
