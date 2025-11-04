<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="bean.Organization" %>
<%
  Organization org = (Organization) session.getAttribute("org");
%>
<!DOCTYPE html><html lang="ja"><head>
<meta charset="UTF-8"><title>管理者新規登録 - 入力</title>
<link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head><body>
<div class="container card">
  <h2>管理者 新規登録（2/4）</h2>
  <p>組織：<strong><%= org!=null ? (org.getName()+"（"+org.getCode()+"）") : "未選択" %></strong></p>

  <% if (request.getAttribute("error") != null) { %>
    <div class="alert error"><%= request.getAttribute("error") %></div>
  <% } %>

  <form method="post" action="<%= request.getContextPath() %>/admin/register/form">
    <label>氏名</label>
    <input type="text" name="name" required>

    <label>メールアドレス（任意：パスコード送信用）</label>
    <input type="email" name="email" placeholder="例）you@example.com">

	<label>パスワード</label>
	<input type="password" name="password"
	       pattern="^(?=\S+$)(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[A-Za-z\d]{8,16}$"
	       title="8〜16文字／英大文字・小文字・数字をすべて含む／記号・空白は使用不可"
	       required>


    <label>パスワード（確認）</label>
    <input type="password" name="passwordConfirm" required>

    <div class="actions">
      <a class="button ghost" href="<%= request.getContextPath() %>/admin/register">戻る</a>
      <button class="button" type="submit">次へ</button>
    </div>
  </form>
</div>
</body></html>
