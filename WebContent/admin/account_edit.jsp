<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="bean.User" %>
<jsp:include page="/header.jsp"/>

<%
  String ctx = request.getContextPath();
  User u = (User) request.getAttribute("user");
%>

<main class="content">
  <h2>アカウント編集</h2>

  <% if (request.getAttribute("error") != null) { %>
    <div class="alert error"><%= request.getAttribute("error") %></div>
  <% } %>

  <form class="card" style="max-width:640px;margin:0 auto;" method="post" action="<%= ctx %>/admin/accounts/edit">
    <input type="hidden" name="userId" value="<%= u.getId() %>">

    <label>ログインID</label>
    <input type="text" value="<%= u.getLoginId() %>" disabled>

    <label>現在の名前</label>
    <input type="text" name="name" maxlength="50" value="<%= u.getName() %>" required>

    <label>アカウントタイプ</label>
    <div class="inline-options" style="margin-top:8px;">
      <label><input type="radio" name="accountType" value="single" <%= "single".equals(u.getAccountType())?"checked":"" %> > 個人</label>
      <label style="margin-left:16px;"><input type="radio" name="accountType" value="multi"
      	<%= "multi".equals(u.getAccountType())?"checked":"" %> > 複数</label>
    </div>

    <div class="actions" style="display:flex;justify-content:space-between;gap:12px;margin-top:16px;">
      <a class="button ghost" href="<%= ctx %>/admin/accounts?userId=<%= u.getId() %>">キャンセル</a>
      <button class="button" type="submit">保存</button>
    </div>
  </form>
</main>

</div><!-- /.layout -->
</body>
</html>
