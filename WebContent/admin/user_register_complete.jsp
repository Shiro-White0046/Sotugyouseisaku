<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html><html lang="ja"><head>
<meta charset="UTF-8"><title>利用者 登録完了</title>
<link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head><body><div class="container card" style="text-align:center;">
  <h2>利用者の登録が完了しました</h2>
	<div class="page-actions">
	  <div class="left">
	    <a class="button ghost" href="<%= request.getContextPath() %>/admin/home">ホームへ</a>
	  </div>
	  <div class="right">
	    <a class="button new-btn" href="<%= request.getContextPath() %>/admin/users/register">続けて登録</a>
	    <a class="button" href="<%= request.getContextPath() %>/admin/users">一覧へ</a>
	  </div>
	</div>
</div></body></html>
