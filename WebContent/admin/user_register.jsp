<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <title>利用者 新規登録</title>
  <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<div class="container card">
  <h2>新規登録（アカウント）</h2>

  <% if (request.getAttribute("error") != null) { %>
    <div class="alert error"><%= request.getAttribute("error") %></div>
  <% } %>

  <form method="post" action="<%= request.getContextPath() %>/admin/users/register">
    <!-- ✅ 名前入力欄を追加 -->
    <label>利用者名</label>
    <input type="text" name="userName" maxlength="50" placeholder="山田花子" required>

    <label>仮パスワード</label>
    <input type="password" name="tempPassword"
      pattern="^(?=\\S+$)(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d]{8,16}$"
      title="8〜16文字／英大文字・小文字・数字をすべて含む／記号・空白は不可" required>

    <label>対象</label>
    <div>
      <label><input type="radio" name="accountType" value="single" required> 個人</label>
      <label style="margin-left:12px;">
        <input type="radio" name="accountType" value="multi"> 複数
      </label>
    </div>

    <div class="actions">
      <a class="button ghost" href="<%= request.getContextPath() %>/admin/users">一覧へ戻る</a>
      <button class="button" type="submit">次へ</button>
    </div>
  </form>
</div>
</body>
</html>
