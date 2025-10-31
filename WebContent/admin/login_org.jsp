<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <title>管理者ログイン - 組織コード</title>
  <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
  <div class="container">
    <h1>管理者ログイン</h1>
    <p>所属する組織の「組織コード」を入力してください。</p>

    <c:if test="${not empty error}">
      <div class="alert error">${error}</div>
    </c:if>

    <form method="post" action="<%= request.getContextPath() %>/admin/login" class="card">
      <label>組織コード</label>
      <input type="text" name="orgCode" maxlength="50" required>
      <div class="actions">
        <button type="submit" class="button">次へ</button>
        <a href="<%= request.getContextPath() %>/admin" class="button ghost">戻る</a>
      </div>
    </form>
  </div>
</body>
</html>
