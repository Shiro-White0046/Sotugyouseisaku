<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <title>管理者ホーム</title>
    <!-- ✅ 共通CSSを読み込み -->
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
    <h1>管理者メニュー</h1>
    <p>以下から操作を選択してください。</p>

    <!-- ログイン画面へ -->
    <a href="<%= request.getContextPath() %>/admin/login" class="button">ログイン</a>

    <!-- 新規登録画面へ -->
    <a href="<%= request.getContextPath() %>/admin/register" class="button new-btn">新規登録</a>
</body>
</html>
