<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<jsp:include page="/header_user.jsp" />

<style>
  :root{
    --bg:#ffe9c7;
    --panel:#ffe0c2;
    --header:#fff2bf;
    --btn:#ffe9c7;
    --btn-border:#e3bf84;
    --btn-hover:#ffefcf;
    --error:#fee2e2;
    --error-border:#fca5a5;
    --error-text:#b91c1c;
  }

  body{
    background:var(--bg);
    font-family: "Hiragino Kaku Gothic ProN", system-ui, sans-serif;
  }

  .withdraw-page{
    max-width:960px;
    margin:24px auto 40px;
    border:1px solid #f0d6a6;
    background:var(--panel);
    box-sizing:border-box;
  }

  .withdraw-header{
    background:var(--header);
    padding:18px 20px;
    text-align:center;
    font-size:20px;
    font-weight:700;
  }

  .withdraw-inner{
    padding:60px 40px 48px;
    text-align:center;
  }

  .withdraw-title{
    font-size:28px;
    font-weight:700;
    letter-spacing:0.06em;
    margin-bottom:40px;
  }

  .withdraw-sub{
    font-size:16px;
    margin-bottom:12px;
  }

  .withdraw-input-wrap{
    margin:16px auto 40px;
    max-width:420px;
  }

  .withdraw-input{
    width:100%;
    font-size:18px;
    padding:10px 12px;
    border-radius:4px;
    border:1px solid #c9a77a;
    box-sizing:border-box;
    background:#fff;
  }

  .withdraw-buttons{
    display:flex;
    justify-content:center;
    gap:80px;
    margin-top:34px;
  }

  .withdraw-btn{
    display:inline-flex;
    align-items:center;
    justify-content:center;
    width:180px;
    height:90px;
    border-radius:16px;
    border:2px solid var(--btn-border);
    background:var(--btn);
    font-size:18px;
    font-weight:600;
    color:#333;
    text-decoration:none;
    cursor:pointer;
  }

  .withdraw-btn:hover{
    background:var(--btn-hover);
  }

  .withdraw-btn.back{
    /* 必要なら色を変えてもOK */
  }

  .withdraw-btn.ok{
    /* 必要なら色を変えてもOK */
  }

  .withdraw-error{
    max-width:480px;
    margin:0 auto 24px;
    padding:10px 12px;
    border-radius:6px;
    background:var(--error);
    border:1px solid var(--error-border);
    color:var(--error-text);
    text-align:center;
    font-size:14px;
  }
</style>

<div class="withdraw-page">
  <div class="withdraw-header">退会</div>

  <div class="withdraw-inner">
    <p class="withdraw-title">退会します。よろしいですか？</p>

    <c:if test="${not empty error}">
      <div class="withdraw-error">
        ${error}
      </div>
    </c:if>

    <form method="post"
          action="${pageContext.request.contextPath}/user/withdraw">

      <p class="withdraw-sub">
        退会する場合はパスワードを入力してください
      </p>

      <div class="withdraw-input-wrap">
        <input type="password"
               name="password"
               class="withdraw-input"
               autocomplete="current-password"
               required>
      </div>

      <div class="withdraw-buttons">
        <a href="${pageContext.request.contextPath}/user/home"
           class="withdraw-btn back">戻る</a>

        <button type="submit" class="withdraw-btn ok">
          はい
        </button>
      </div>
    </form>
  </div>
</div>
