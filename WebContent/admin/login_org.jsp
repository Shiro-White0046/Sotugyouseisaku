<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%
  boolean hasError = request.getAttribute("error") != null;
  String prev = (String) request.getAttribute("orgCode");
  if (prev == null) prev = request.getParameter("orgCode");
  if (prev == null) prev = "";
%>
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <title>管理者ログイン - 組織コード</title>
  <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
  <script>
    document.addEventListener('DOMContentLoaded', function(){
      <% if (hasError) { %>
        var el = document.getElementById('orgCode');
        if (el) el.focus();
      <% } %>
    });
  </script>
</head>
<body>
  <div class="container">
    <h1>管理者ログイン</h1>
    <p>所属する組織の「組織コード」を入力してください。</p>

    <% if (hasError) { %>
      <div class="alert error"><%= request.getAttribute("error") %></div>
    <% } %>

    <form method="post" action="<%= request.getContextPath() %>/admin/login" class="card">
      <label for="orgCode">組織コード</label>
      <input type="text"
             id="orgCode"
             name="orgCode"
             maxlength="50"
             inputmode="text"
             class="<%= hasError ? "input-error" : "" %>"
             value="<%= prev %>"
             aria-invalid="<%= hasError ? "true" : "false" %>"
             required>
      <div class="actions">
        <button type="submit" class="button">次へ</button>
        <a href="<%= request.getContextPath() %>/admin/index.jsp" class="button ghost">戻る</a>

      </div>
    </form>
  </div>
</body>
</html>
