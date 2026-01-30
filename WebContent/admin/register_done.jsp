<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="bean.Administrator" %>
<%
  Administrator a = (Administrator) session.getAttribute("createdAdmin");
%>
<!DOCTYPE html><html lang="ja"><head>
<meta charset="UTF-8"><title>管理者新規登録 - 完了</title>
<link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head><body>
<div class="container card">
  <h2>管理者 新規登録（4/4）</h2>
  <% if (a != null) { %>
    <p>登録が完了しました。あなたの <strong>個人番号</strong> は：</p>
    <p style="font-size:24px;font-weight:bold;"><%= a.getAdminNo() %></p>
  <% } else { %>
    <p>完了情報が見つかりませんでした。</p>
  <% } %>
  <div class="actions">
    <a class="button" href="<%= request.getContextPath() %>/admin/home">トップへ</a>
  </div>
</div>
</body></html>
