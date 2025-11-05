<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <title>利用者ログイン - 組織コード</title>
  <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<div class="container card">
  <h2>利用者ログイン</h2>
  <p>所属する組織の「組織コード」を入力してください。</p>

  <% if (request.getAttribute("error") != null) { %>
    <div class="alert error"><%= request.getAttribute("error") %></div>
  <% } %>

  <form method="post" action="<%= request.getContextPath() %>/user/login">
    <label>組織コード</label>
    <input type="text" name="orgCode" maxlength="50" required>

    <div class="actions">
      <a href="<%= request.getContextPath() %>/" class="button ghost">トップへ戻る</a>
      <button type="submit" class="button">次へ</button>
    </div>
  </form>
</div>
</body>
</html>
