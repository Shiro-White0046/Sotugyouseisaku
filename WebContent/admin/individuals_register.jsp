<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="bean.User" %>
<%
  User u = (User) session.getAttribute("createdUser");
  boolean isSingle = "single".equals(u.getAccountType());
%>
<!DOCTYPE html><html lang="ja"><head>
<meta charset="UTF-8"><title>利用者（個人）登録</title>
<link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
<script>
function addRow(){
  const wrap = document.getElementById('rows');
  const div = document.createElement('div');
  div.innerHTML = '<input type="text" name="childName" placeholder="氏名" required> '+
                  '<button type="button" class="button ghost" onclick="this.parentNode.remove()">削除</button>';
  wrap.appendChild(div);
}
</script>
</head><body><div class="container card">
  <h2>新規登録（利用者）</h2>
  <p>アカウントID：<strong><%= u.getLoginId() %></strong></p>

  <% if (request.getAttribute("error") != null) { %>
    <div class="alert error"><%= request.getAttribute("error") %></div>
  <% } %>

  <form method="post" action="<%= request.getContextPath() %>/admin/individuals/register">
    <div id="rows">
      <label>氏名</label>
      <input type="text" name="childName" placeholder="氏名" required>
      <% if (!isSingle) { %>
        <!-- 複数なら最初から2行目を出しておいてもOK。ここは1行だけにしておく -->
      <% } %>
    </div>

    <% if (!isSingle) { %>
      <button class="button ghost" type="button" onclick="addRow()">利用者枠追加</button>
    <% } %>

    <div class="actions" style="margin-top:16px;">
      <a class="button ghost" href="<%= request.getContextPath() %>/admin/users/register/done">戻る</a>
      <button class="button" type="submit">確定</button>
    </div>
  </form>
</div></body></html>
