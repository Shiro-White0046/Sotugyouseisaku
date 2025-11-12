<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%
  // 画面タイトル（ヘッダー共通の場合）
  if (request.getAttribute("pageTitle") == null) {
    request.setAttribute("pageTitle", "認証パスワード設定");
  }
%>

<jsp:include page="/header_user.jsp" />

<style>
  .pin-page { background:#fde8d7; padding:40px 24px; min-height:70vh; }
  .pin-card {
    max-width: 860px; margin: 0 auto; background:#fde8d7;
    padding: 48px 24px;
  }
  .pin-title { font-size:20px; font-weight:700; margin: 0 0 32px; text-align:center;}
  .grid2 { display:grid; grid-template-columns: 160px 1fr; gap: 16px 24px; align-items:center; }
  .label { font-weight:700; }
  .name-value { font-size:18px; }
  .helper { color:#555; margin-top:4px; font-size:14px; }
  .pin-input {
    width: 260px; height: 44px; font-size:20px; text-align:center;
    border:1px solid #999; border-radius:4px; padding: 4px 8px;
    background:#fff;
  }
  .btn-row { display:flex; justify-content:space-between; margin-top:80px; }
  .btn {
    min-width:160px; height:56px; border:1px solid #333; background:#fff;
    border-radius:4px; font-size:18px; cursor:pointer;
  }
  .btn:active { transform: translateY(1px); }
</style>

<div class="pin-page">
  <div class="pin-card">
    <h2 class="pin-title">認証パスワード設定</h2>

    <form action="${pageContext.request.contextPath}/user/pin" method="post" autocomplete="off">
      <div class="grid2">
        <!-- 名前 -->
        <div class="label">名前</div>
        <div class="name-value">
          <c:choose>
            <c:when test="${not empty individual}">
              <!-- Individualが持っている想定の項目名に合わせて調整してください -->
              <c:out value="${individual.displayName}" />
            </c:when>
            <c:otherwise>
              不明
            </c:otherwise>
          </c:choose>
        </div>

        <!-- PIN -->
        <div class="label">認証パスワード<br><span class="helper">※数字4桁</span></div>
        <div>
          <input
            class="pin-input"
            type="password"
            name="pin"
            inputmode="numeric"
            pattern="\d{4}"
            maxlength="4"
            minlength="4"
            placeholder=""
            required
            aria-label="認証パスワード（数字4桁）"
            autocomplete="one-time-code"
          />
        </div>
      </div>

      <div class="btn-row">
        <button type="button" class="btn" onclick="history.back()">戻る</button>
        <button type="submit" class="btn">確定</button>
      </div>
    </form>
  </div>
</div>