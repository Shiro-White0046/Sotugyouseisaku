<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html><html lang="ja"><head>
<meta charset="UTF-8"><title>管理者新規登録 - 組織コード</title>
<link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head><body>
<div class="container card">
  <h2>管理者 新規登録（1/4）</h2>
  <p>所属する組織の「組織コード」を入力してください。</p>

  <% if (request.getAttribute("error") != null) { %>
    <div class="alert error"><%= request.getAttribute("error") %></div>
  <% } %>

  <form method="post" action="<%= request.getContextPath() %>/admin/register">
    <label for="orgCode">組織コード</label>
    <input type="text" id="orgCode" name="orgCode" maxlength="50"
           class="<%= request.getAttribute("error")!=null ? "input-error" : "" %>" required>
    <div class="actions">
      <a class="button ghost" href="<%= request.getContextPath() %>/admin/index.jsp">戻る</a>
      <button class="button" type="submit">次へ</button>
    </div>
  </form>
</div>
</body></html>
