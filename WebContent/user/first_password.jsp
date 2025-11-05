<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html><html lang="ja"><head>
<meta charset="UTF-8"><title>パスワード変更（初回）</title>
<link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head><body>
<div class="container card">
  <h2>初回ログインのためパスワードを変更してください</h2>

  <% if (request.getAttribute("error") != null) { %>
    <div class="alert error"><%= request.getAttribute("error") %></div>
  <% } %>

  <form method="post" action="<%= request.getContextPath() %>/user/first-password">
	<label>新しいパスワード</label>
	<input type="password" name="newPassword"
	       pattern="^(?=\S+$)(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[A-Za-z\d]{8,16}$"
	       title="8〜16文字、英大文字・小文字・数字をそれぞれ1文字以上含む（記号・空白は不可）"
	       required>
	<small style="color:#666;">※ 英大文字・小文字・数字をそれぞれ含む8〜16文字。記号・空白は使用できません。</small>


    <label>確認用パスワード</label>
    <input type="password" name="confirmPassword" required>

    <div class="actions">
      <button class="button" type="submit">更新して進む</button>
    </div>
  </form>
</div>
</body></html>
