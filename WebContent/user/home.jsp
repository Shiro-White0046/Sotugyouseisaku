<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%
  request.setAttribute("pageTitle", "利用者ホーム");
%>

<jsp:include page="/header_user.jsp" />

<!-- 🔔 Flash Message（成功→緑／失敗→赤） -->
<c:set var="__flash"
       value="${not empty requestScope.flashMessage ? requestScope.flashMessage : sessionScope.flashMessage}" />
<c:if test="${not empty __flash}">
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
    .flash-message.success { background: rgba(0,150,0,.9); }
    .flash-message.error   { background: rgba(200,0,0,.9); }
  </style>
</c:if>

<!-- ===== ▼▼ 追加：常に3カラム表示（未登録でもプレースホルダ） ▼▼ ===== -->
<div class="content--peach">
  <h2 class="page-title">
    <c:choose>
      <c:when test="${not empty requestScope.todayDate}">
        ${requestScope.todayDate} の献立（ホーム）
      </c:when>
      <c:when test="${not empty menuDay}">
        ${menuDay.menuDate} の献立（ホーム）
      </c:when>
      <c:otherwise>本日の献立（ホーム）</c:otherwise>
    </c:choose>
  </h2>

  <div class="meal-grid">
    <!-- 朝食カード -->
    <article class="meal-card">
      <div class="meal-card__head">
        <span class="pill pill--breakfast">朝食</span>
        <a class="tiny-link" href="${pageContext.request.contextPath}/user/menudetail?slot=BREAKFAST&date=${requestScope.todayDate}">詳細</a>
      </div>
      <div class="image-box">
        <c:choose>
          <c:when test="${not empty breakfast && not empty breakfast.imagePath}">
            <img src="<c:url value='/${breakfast.imagePath}'/>" alt="朝食画像">
          </c:when>
          <c:otherwise><span class="image-box__ph">画像未登録</span></c:otherwise>
        </c:choose>
      </div>
      <div class="meal-card__foot">
        <c:choose>
          <c:when test="${not empty breakfast && not empty breakfast.name}">
            <div class="meal-name"><c:out value="${breakfast.name}"/></div>
          </c:when>
          <c:otherwise><div class="meal-empty">登録されていません</div></c:otherwise>
        </c:choose>
        <c:if test="${not empty breakfastItems}">
          <ul class="items">
            <c:forEach var="it" items="${breakfastItems}">
              <li><c:out value="${it.name}"/></li>
            </c:forEach>
          </ul>
        </c:if>
      </div>
    </article>

    <!-- 昼食カード -->
    <article class="meal-card">
      <div class="meal-card__head">
        <span class="pill pill--lunch">昼食</span>
        <a class="tiny-link" href="${pageContext.request.contextPath}/user/menudetail?slot=LUNCH&date=${requestScope.todayDate}">詳細</a>
      </div>
      <div class="image-box">
        <c:choose>
          <c:when test="${not empty lunch && not empty lunch.imagePath}">
            <img src="<c:url value='/${lunch.imagePath}'/>" alt="昼食画像">
          </c:when>
          <c:otherwise><span class="image-box__ph">画像未登録</span></c:otherwise>
        </c:choose>
      </div>
      <div class="meal-card__foot">
        <c:choose>
          <c:when test="${not empty lunch && not empty lunch.name}">
            <div class="meal-name"><c:out value="${lunch.name}"/></div>
          </c:when>
          <c:otherwise><div class="meal-empty">登録されていません</div></c:otherwise>
        </c:choose>
        <c:if test="${not empty lunchItems}">
          <ul class="items">
            <c:forEach var="it" items="${lunchItems}">
              <li><c:out value="${it.name}"/></li>
            </c:forEach>
          </ul>
        </c:if>
      </div>
    </article>

    <!-- 夕食カード -->
    <article class="meal-card">
      <div class="meal-card__head">
        <span class="pill pill--dinner">夕食</span>
        <a class="tiny-link" href="${pageContext.request.contextPath}/user/menudetail?slot=DINNER&date=${requestScope.todayDate}">詳細</a>
      </div>
      <div class="image-box">
        <c:choose>
          <c:when test="${not empty dinner && not empty dinner.imagePath}">
            <img src="<c:url value='/${dinner.imagePath}'/>" alt="夕食画像">
          </c:when>
          <c:otherwise><span class="image-box__ph">画像未登録</span></c:otherwise>
        </c:choose>
      </div>
      <div class="meal-card__foot">
        <c:choose>
          <c:when test="${not empty dinner && not empty dinner.name}">
            <div class="meal-name"><c:out value="${dinner.name}"/></div>
          </c:when>
          <c:otherwise><div class="meal-empty">登録されていません</div></c:otherwise>
        </c:choose>
        <c:if test="${not empty dinnerItems}">
          <ul class="items">
            <c:forEach var="it" items="${dinnerItems}">
              <li><c:out value="${it.name}"/></li>
            </c:forEach>
          </ul>
        </c:if>
      </div>
    </article>
  </div>

  <div class="center-block">
    <a class="btn-month" href="${pageContext.request.contextPath}/user/menuscalendar">献立カレンダー</a>
  </div>
</div>

<jsp:include page="/footer.jsp" />

<style>
  /* 背景色と上下の余白 */
  .content--peach {
    background:#f9dbbf;
    padding: 18px 0 28px;
  }

  .page-title {
    text-align:center;
    font-size:22px;
    margin:2px 0 18px;
    font-weight:800;
  }

  /* ▼ main.content を横いっぱいに広げる（header_user.jsp の max-width を打ち消す） */
  .content {
    max-width: none !important;
    margin: 0 !important;
  }

  /* ▼ 献立カードの並び：中央寄せ + 折り返し */
  .meal-grid {
    width: min(1100px, 100%);
    margin: 0 auto;           /* 中央寄せ */
    display: flex;
    flex-wrap: wrap;
    gap: 18px;
    justify-content: center;  /* 左寄せ → 中央寄せに変更 */
  }

  /* カード本体 */
  .meal-card {
    background:#fff;
    border:1px solid #e6d3bc;
    border-radius:12px;
    box-shadow:0 1px 2px rgba(0,0,0,.04);
    overflow:hidden;
    flex: 0 0 260px;          /* 1枚あたり 260px 固定 */
    max-width:260px;
  }

  .meal-card__head{
    display:flex;
    justify-content:space-between;
    align-items:center;
    padding:10px 12px 0 12px;
  }

  .tiny-link{
    font-size:12px;
    color:#1a73e8;
    text-decoration:underline;
  }

  .pill{
    display:inline-block;
    border-radius:999px;
    padding:4px 10px;
    font-size:12px;
    font-weight:700;
    color:#333;
    background:#ffd451;
  }
  .pill--breakfast{ background:#ffd451; }
  .pill--lunch{      background:#ffcd75; }
  .pill--dinner{     background:#ffc38a; }

  .image-box{
    margin:8px 12px;
    border:1px solid #eee;
    border-radius:10px;
    background:#fafafa;
    position:relative;
    overflow:hidden;
    aspect-ratio:16/9;
    display:flex;
    align-items:center;
    justify-content:center;
  }
  .image-box img{
    width:100%;
    height:100%;
    object-fit:cover;
    display:block;
  }
  .image-box__ph{
    color:#b0b0b0;
    font-size:14px;
  }

  .meal-card__foot{
    padding:8px 12px 14px;
    text-align:center;
  }
  .meal-name{
    font-weight:800;
    margin-bottom:6px;
  }
  .meal-empty{
    color:#777;
    font-weight:700;
    margin-bottom:6px;
  }

  .items{
    list-style:none;
    padding:0;
    margin:0;
    display:flex;
    flex-wrap:wrap;
    gap:6px 10px;
    justify-content:center;
  }
  .items li{
    font-size:13px;
    color:#444;
    background:#fff7ec;
    border:1px solid #f2d7b8;
    border-radius:999px;
    padding:4px 10px;
  }

  .center-block{
    text-align:center;
    margin:22px 0 6px;
  }
  .btn-month{
    display:inline-block;
    background:#fff;
    border:1px solid #e0c8a8;
    border-radius:10px;
    padding:10px 22px;
    font-weight:700;
    text-decoration:none;
    color:#333;
    box-shadow:0 1px 2px rgba(0,0,0,.04);
  }
  .btn-month:hover{
    background:#fff7ec;
  }

  /* 管理者用に使っていた .menu-card を無効化（念のため） */
  .menu-card {
    max-width: none !important;
    margin: 0 !important;
    padding: 0 !important;
    border: none !important;
    background: none !important;
    text-align: initial !important;
  }
  /* ★ 利用者ホーム(page-user-home)のときだけ layout の固定幅を解除 */
  body.page-user-home .layout {
    max-width: 100% !important;
    width: 100% !important;
    margin: 0 !important;
    padding: 0 !important;
    overflow-x: hidden;   /* 念のため */
  }

  /* ★ 利用者ホームのメイン領域を全幅に */
  body.page-user-home .content,
  body.page-user-home .content--peach {
    max-width: 100% !important;
    width: 100% !important;
    margin: 0 !important;
  }

</style>