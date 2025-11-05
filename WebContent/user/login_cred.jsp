<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="bean.Organization" %>
<%
  // セッションから組織情報を取得
  Organization org = (Organization) session.getAttribute("org");
%>
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <title>利用者ログイン - 認証</title>
  <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<div class="container card">
  <h2>利用者ログイン</h2>

  <p>
    所属組織：
    <strong>
      <%= (org != null ? org.getName() + "（" + org.getCode() + "）" : "未選択") %>
    </strong>
  </p>

  <%-- エラーメッセージがある場合 --%>
  <% if (request.getAttribute("error") != null) { %>
    <div class="alert error"><%= request.getAttribute("error") %></div>
  <% } %>

  <form method="post" action="<%= request.getContextPath() %>/user/login/cred">
    <label for="loginId">アカウントID（6桁）</label>
    <input type="text" id="loginId" name="loginId"
           pattern="[0-9]{6}" maxlength="6"
           placeholder="例：000123"
           required>

    <label for="password">パスワード</label>
    <input type="password" id="password" name="password" required>

    <div class="actions">
      <a class="button ghost" href="<%= request.getContextPath() %>/user/login">戻る</a>
      <button type="submit" class="button">ログイン</button>
    </div>
  </form>
</div>
</body>
</html>
