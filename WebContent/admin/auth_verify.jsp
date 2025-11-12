<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%
  request.setAttribute("pageTitle", "認証ページ");
%>

<jsp:include page="/header.jsp" />

<style>
:root{
  --bg:#fde9c9; --panel:#f3dcb1; --row:#e3a07e; --border:#d7b88f; --btn:#c9d8f0;
}
body{ background:var(--bg); }
.verify-wrap{
  max-width: 720px;
  margin: 24px auto 48px;
  background: var(--panel);
  border: 1px solid var(--border);
  border-radius: 8px;
  padding: 24px 32px 32px;
}
.verify-title{ text-align:center; font-weight:700; margin-bottom:16px; }
.lead{ text-align:center; margin: 8px 0 24px; }

.form-row{ display:grid; grid-template-columns: 120px 1fr; gap: 16px; align-items:center; margin-bottom:18px; }
input[type="text"], input[type="password"]{
  width: 100%; box-sizing: border-box; padding: 10px 12px; border:1px solid #999; border-radius:8px; background:#fff;
  font-size:16px;
}
.readonly{ background:#fff; }
.actions{ text-align: right; margin-top: 8px; }
button{
  padding: 10px 22px; border:1px solid #9eb2d2; border-radius:10px; background: var(--btn); cursor:pointer; font-size:15px;
}
.error{ color:#b00020; margin-bottom:12px; text-align:center; font-weight:700; }
</style>

<div class="verify-wrap">
  <h2 class="verify-title">認証ページ</h2>
  <p class="lead">アレルギー物質が含まれていないか確認しましたか？</p>

  <c:if test="${not empty error}">
    <div class="error"><c:out value="${error}" /></div>
  </c:if>

  <form method="post" action="${pageContext.request.contextPath}/admin/auth/verify">
    <!-- 個体IDを保持 -->
    <input type="hidden" name="id" value="${person.id}" />

    <div class="form-row">
      <label>名前</label>
      <input type="text" class="readonly" value="${person.displayName}" readonly />
    </div>

    <div class="form-row">
      <label>パスワード</label>
      <input type="password" name="pin" placeholder="4桁" required minlength="4" maxlength="4" pattern="\d{4}" />
    </div>

    <div class="actions">
      <button type="submit">認証</button>
    </div>
  </form>
</div>
