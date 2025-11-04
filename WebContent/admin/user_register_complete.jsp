<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html><html lang="ja"><head>
<meta charset="UTF-8"><title>利用者 登録完了</title>
<link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head><body><div class="container card" style="text-align:center;">
  <h2>利用者の登録が完了しました</h2>
  <div class="actions" style="margin-top:16px; display:flex; gap:8px; justify-content:center;">
    <a class="button" href="<%= request.getContextPath() %>/admin/home">ホーム画面</a>
    <a class="button" href="<%= request.getContextPath() %>/admin/users/register">続けてアカウント登録</a>
    <a class="button ghost" href="<%= request.getContextPath() %>/admin/users">アカウント一覧</a>
  </div>
</div></body></html>
