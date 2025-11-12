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

<!-- ==== 追加：管理者ホーム寄りの3カラム（任意表示） ====== -->
<c:if test="${not empty breakfast or not empty lunch or not empty dinner}">
  <div role="main" class="content content--peach">
    <h2 class="page-title">
      ${requestScope.todayDate} の献立（ホーム）
    </h2>

    <div class="meal-grid">
      <!-- 朝食 -->
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
            <c:when test="${not empty breakfast}">
                            <div class="meal-name" style="font-weight:800;"><c:out value="${breakfast.name}"/></div>
              <c:if test="${not empty breakfastItems}">
                <ul class="menu-items" style="margin:6px 0 0; padding-left:1.1em; font-size:13px; line-height:1.5; text-align:left;">
                  <c:forEach var="it" items="${breakfastItems}">
                    <li style="list-style:disc;"><c:out value="${it.name}"/></li>
                  </c:forEach>
                </ul>
              </c:if>
            </c:when>
            <c:otherwise><div class="meal-empty">登録されていません</div></c:otherwise>
          </c:choose>
        </div>
      </article>

      <!-- 昼食 -->
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
            <c:when test="${not empty lunch}">
              <div class="meal-name" style="font-weight:800;"><c:out value="${lunch.name}"/></div>
              <c:if test="${not empty lunchItems}">
                <ul class="menu-items" style="margin:6px 0 0; padding-left:1.1em; font-size:13px; line-height:1.5; text-align:left;">
                  <c:forEach var="it" items="${lunchItems}">
                    <li style="list-style:disc;"><c:out value="${it.name}"/></li>
                  </c:forEach>
                </ul>
              </c:if>
            </c:when>
            <c:otherwise><div class="meal-empty">登録されていません</div></c:otherwise>
          </c:choose>
        </div>
      </article>

      <!-- 夕食 -->
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
            <c:when test="${not empty dinner}">
              <div class="meal-name" style="font-weight:800;"><c:out value="${dinner.name}"/></div>
              <c:if test="${not empty dinnerItems}">
                <ul class="menu-items" style="margin:6px 0 0; padding-left:1.1em; font-size:13px; line-height:1.5; text-align:left;">
                  <c:forEach var="it" items="${dinnerItems}">
                    <li style="list-style:disc;"><c:out value="${it.name}"/></li>
                  </c:forEach>
                </ul>
              </c:if>
            </c:when>
            <c:otherwise><div class="meal-empty">登録されていません</div></c:otherwise>
          </c:choose>
        </div>
      </article>
    </div>

    <div class="center-block">
      <a class="btn-month" href="${pageContext.request.contextPath}/user/menuscalendar">献立カレンダー</a>
    </div>
  </div>
</c:if>

<jsp:include page="/footer.jsp" />

<!-- ====== 追記CSS（管理者ホーム風に寄せる） ====== -->
<style>
  .content--peach { background:#f9dbbf; padding-bottom:28px; }
  .page-title { text-align:center; font-size:22px; margin:18px 0 16px; font-weight:800; }
  .meal-grid { width:min(1100px,92%); margin:0 auto; display:grid; grid-template-columns:repeat(3,1fr); gap:18px; }
  @media (max-width:1000px){ .meal-grid{ grid-template-columns:1fr; } }
  .meal-card { background:#fff; border:1px solid #e6d3bc; border-radius:12px; box-shadow:0 1px 2px rgba(0,0,0,.04); overflow:hidden; }
  .meal-card__head{ display:flex; justify-content:space-between; align-items:center; padding:10px 12px 0 12px; }
  .tiny-link{ font-size:12px; color:#1a73e8; text-decoration:underline; }
  .pill{ display:inline-block; border-radius:999px; padding:4px 10px; font-size:12px; font-weight:700; color:#333; background:#ffd451; }
  .pill--breakfast{ background:#ffd451; } .pill--lunch{ background:#ffcd75; } .pill--dinner{ background:#ffc38a; }
  .image-box{ margin:8px 12px; border:1px solid #eee; border-radius:10px; background:#fafafa; position:relative; overflow:hidden; aspect-ratio:16/9; display:flex; align-items:center; justify-content:center; }
  .image-box img{ width:100%; height:100%; object-fit:cover; display:block; }
  .image-box__ph{ color:#b0b0b0; font-size:14px; }
  .meal-card__foot{ padding:8px 12px 14px; text-align:center; }
  .meal-name{ font-weight:800; }
  .meal-empty{ color:#777; font-weight:700; }
  .center-block{ text-align:center; margin:22px 0 6px; }
  .btn-month{ display:inline-block; background:#fff; border:1px solid #e0c8a8; border-radius:10px; padding:10px 22px; font-weight:700; text-decoration:none; color:#333; box-shadow:0 1px 2px rgba(0,0,0,.04); }
  .btn-month:hover{ background:#fff7ec; }

</style>
