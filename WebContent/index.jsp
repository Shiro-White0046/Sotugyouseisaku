<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<title>アレルギー対策システム</title>
<link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<div class="container" style="text-align:center;margin-top:80px;">
<h1>アレルギー対策システム</h1>
<p>ログインする区分を選択してください。</p>

    <div style="margin-top:40px;">
<!-- 管理者用 -->
<a class="button new-btn" href="<%= request.getContextPath() %>/admin/index.jsp">管理者ログイン</a>

      <!-- 利用者用 -->
<a class="button" href="<%= request.getContextPath() %>/user/index.jsp">利用者ログイン</a>
</div>
</div>

  <footer class="app-footer" style="margin-top:60px;">
<p>© 2025 アレルギー対策アプリ</p>
</footer>
</body>
</html>
