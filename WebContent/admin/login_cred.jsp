<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ page import="bean.Organization" %>
<%
    // セッションから選択済みの組織を取得
    Organization org = (Organization) session.getAttribute("org");
%>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <title>管理者ログイン - 認証</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<div class="container">
    <h1>管理者ログイン</h1>
    <p>所属組織：<strong><%= (org != null ? org.getName() + "（" + org.getCode() + "）" : "未選択") %></strong></p>

    <% if (request.getAttribute("error") != null) { %>
        <div class="alert error"><%= request.getAttribute("error") %></div>
    <% } %>

    <form method="post" action="<%= request.getContextPath() %>/admin/login/cred" class="card">
        <label for="adminNo">個人番号（8桁）</label>
        <input type="text" id="adminNo" name="adminNo"
       inputmode="numeric" maxlength="8"
       pattern="[0-9]{8}" required>

        <label for="password">パスワード</label>
        <input type="password" id="password" name="password" required>

        <div class="actions">
            <a href="<%= request.getContextPath() %>/admin/login" class="button ghost">戻る</a>
            <button type="submit" class="button">ログイン</button>
        </div>
    </form>
</div>
</body>
</html>
