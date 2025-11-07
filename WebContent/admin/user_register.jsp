<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <title>利用者 新規登録</title>
  <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<div class="register-page">
  <h2>新規登録（アカウント）</h2>

  <%
    // セッションに保存されたエラーメッセージを取得
    String errMsg = (String) session.getAttribute("error");
    if (errMsg != null) {
  %>
    <div class="alert error"><%= errMsg %></div>
  <%
      // 再読み込み時に残らないよう削除
      session.removeAttribute("error");
    }
  %>

  <form class="register-form" method="post" action="<%= request.getContextPath() %>/admin/users/register">

    <label>利用者名</label>
    <input type="text" name="userName" maxlength="50" placeholder="山田花子" required>

	<label>仮パスワード</label>
	<input type="password" name="tempPassword"
	       pattern="^(?=\S+$)(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[A-Za-z\d]{8,16}$"
	       autocomplete="new-password"
	       placeholder="例：Abc12345"
	       title="8〜16文字、英大文字・小文字・数字をそれぞれ1文字以上含む。記号・空白は使用できません。"
	       required>
	<small style="color:#666;">※ 英大文字・小文字・数字をそれぞれ含む8〜16文字。記号・空白は不可。</small>



    <label>対象</label>
    <div class="inline-options" style="margin-top:8px;">
      <label><input type="radio" name="accountType" value="single" required> 個人</label>
      <label><input type="radio" name="accountType" value="multi"> 複数</label>
    </div>

    <div class="actions" style="margin-top:24px; text-align:center;">
    <a class="button ghost" href="<%= request.getContextPath() %>/admin/home">ホームへ</a>
      <a class="button ghost" href="<%= request.getContextPath() %>/admin/users">一覧へ戻る</a>
      <button class="button new-btn" type="submit">次へ</button>
    </div>
  </form>
</div>
</body>
</html>
