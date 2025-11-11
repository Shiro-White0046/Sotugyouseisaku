<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%
  request.setAttribute("pageTitle", "利用者ホーム");
%>

<jsp:include page="/header_user.jsp" />

<!-- 🔔 Flash Message（成功→緑／失敗→赤） -->
<c:set var="__flash"
       value="${not empty requestScope.flashMessage ? requestScope.flashMessage : sessionScope.flashMessage}" />
<c:if test="${not empty __flash}">
  <%-- メッセージ内容から色を推定 --%>
  <c:set var="__isError"
         value="${fn:contains(__flash, '失敗') or fn:contains(__flash, 'エラー') or fn:contains(__flash, 'できません')}" />

  <div id="flash-message" class="flash-message no-js ${__isError ? 'error' : 'success'}">
    <c:out value="${__flash}" />
  </div>

  <c:if test="${not empty sessionScope.flashMessage}">
    <c:remove var="flashMessage" scope="session" />
  </c:if>

  <script>
    (function(){
      const el = document.getElementById('flash-message');
      if (!el) return;
      el.classList.remove('no-js');
      setTimeout(() => {
        el.style.transition = 'opacity 0.8s ease, top 0.8s ease';
        el.style.opacity = '0';
        el.style.top = '0px';
        setTimeout(() => el.remove(), 850);
      }, 3500);
    })();
  </script>

  <style>
    .flash-message {
      position: fixed;
      top: 25px;
      left: 50%;
      transform: translateX(-50%);
      color: #fff;
      padding: 20px 40px;
      border-radius: 12px;
      font-size: 20px;
      font-weight: bold;
      text-align: center;
      min-width: 300px;
      max-width: 90%;
      box-shadow: 0 4px 15px rgba(0, 0, 0, 0.4);
      z-index: 2147483647;
      opacity: 1;
      letter-spacing: 0.05em;
      line-height: 1.4;
    }
    .flash-message.success {
      background: rgba(0, 150, 0, 0.9); /* 💚緑：成功 */
    }
    .flash-message.error {
      background: rgba(200, 0, 0, 0.9); /* ❤️赤：エラー */
    }
  </style>
</c:if>


<div class="menu-card">
  <h2 class="menu-title">今日の献立</h2>

  <c:if test="${not empty todayMenu}">
    <div class="card">
      <h3>${todayMenu.menuDate} の献立</h3>
      <c:if test="${not empty todayMenu.imagePath}">
        <img src="${pageContext.request.contextPath}${todayMenu.imagePath}"
             alt="今日の献立画像" style="max-width:100%">
      </c:if>
      <c:if test="${not empty todayMenu.name}">
        <div class="menu-name">${todayMenu.name}</div>
      </c:if>
      <c:if test="${not empty todayMenu.description}">
        <div class="menu-desc">${todayMenu.description}</div>
      </c:if>
    </div>
  </c:if>

  <c:if test="${empty todayMenu}">
    <div class="card"><p>本日の献立は登録されていません。</p></div>
  </c:if>
</div>

<jsp:include page="/footer.jsp" />
