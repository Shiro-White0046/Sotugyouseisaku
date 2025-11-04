<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="bean.User" %>
<%
  User u = (User) session.getAttribute("createdUser");
  String tmp = (String) session.getAttribute("tempPwPlain");
%>
<!DOCTYPE html><html lang="ja"><head>
<meta charset="UTF-8"><title>アカウントIDの発行</title>
<link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
<script>
function copyText(id){
  var t = document.getElementById(id);
  t.select(); t.setSelectionRange(0, 99999);
  document.execCommand('copy');
  alert('コピーしました');
}
</script>
</head><body><div class="container card">
  <h2>アカウントIDを発行しました</h2>

  <label>アカウントID</label>
  <input type="text" value="<%= u.getLoginId() %>" readonly id="loginId">
  <button class="button ghost" type="button" onclick="copyText('loginId')">コピー</button>

  <label style="margin-top:12px;">仮パスワード（控え）</label>
  <input type="text" value="<%= tmp %>" readonly id="tempPw">
  <button class="button ghost" type="button" onclick="copyText('tempPw')">コピー</button>

  <div class="actions" style="margin-top:16px;">
    <a class="button" href="<%= request.getContextPath() %>/admin/individuals/register">利用者作成へ</a>
  </div>
</div></body></html>
