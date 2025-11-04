<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ page import="bean.Organization" %>
<%
    // セッションから選択済みの組織を取得
    Organization org = (Organization) session.getAttribute("org");

    // エラー有無
    boolean hasError = (request.getAttribute("error") != null);

    // 直前の入力値（管理者番号は復元、PWはセキュリティのため復元しない）
    String prevAdminNo = request.getParameter("adminNo");
    if (prevAdminNo == null) prevAdminNo = "";
%>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <title>管理者ログイン - 認証</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
    <script>
      // エラー時は最初の入力にフォーカス
      document.addEventListener('DOMContentLoaded', function(){
        <% if (hasError) { %>
          var el = document.getElementById('adminNo');
          if (el) el.focus();
        <% } %>
      });
    </script>
</head>
<body>
<div class="container">
    <h1>管理者ログイン</h1>
    <p>所属組織：<strong><%= (org != null ? org.getName() + "（" + org.getCode() + "）" : "未選択") %></strong></p>

    <% if (hasError) { %>
        <div class="alert error"><%= request.getAttribute("error") %></div>
    <% } %>

    <form method="post" action="<%= request.getContextPath() %>/admin/login/cred" class="card">
        <label for="adminNo">個人番号（8桁）</label>
        <input type="text"
               id="adminNo"
               name="adminNo"
               inputmode="numeric"
               maxlength="8"
               pattern="[0-9]{8}"
               value="<%= prevAdminNo %>"
               class="<%= hasError ? "input-error" : "" %>"
               aria-invalid="<%= hasError ? "true" : "false" %>"
               required>

        <label for="password">パスワード</label>
        <input type="password"
               id="password"
               name="password"
               class="<%= hasError ? "input-error" : "" %>"
               aria-invalid="<%= hasError ? "true" : "false" %>"
               required>

        <div class="actions">
            <a href="<%= request.getContextPath() %>/admin/login" class="button ghost">戻る</a>
            <button type="submit" class="button">ログイン</button>
        </div>
    </form>
</div>
</body>
</html>
