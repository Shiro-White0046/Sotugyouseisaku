<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html><html lang="ja"><head>
<meta charset="UTF-8"><title>管理者新規登録 - パスコード確認</title>
<link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head><body>
<div class="container card">
  <h2>管理者 新規登録（3/4）</h2>
  <p>メールに届いた <strong>6桁のパスコード</strong> を入力してください。</p>

  <% if (request.getAttribute("error") != null) { %>
    <div class="alert error"><%= request.getAttribute("error") %></div>
  <% } %>

  <form method="post" action="<%= request.getContextPath() %>/admin/register/verify">
    <label>パスコード（6桁）</label>
    <input type="text" name="code" inputmode="numeric" pattern="[0-9]{6}" maxlength="6" required>
    <div class="actions">
      <a class="button ghost" href="<%= request.getContextPath() %>/admin/register/form">戻る</a>
      <button class="button" type="submit">次へ</button>
    </div>
  </form>
</div>
</body></html>
