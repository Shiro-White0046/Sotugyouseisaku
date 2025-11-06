<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<jsp:include page="/header_user.jsp"/>

<c:if test="${not empty sessionScope.flash}">
  <c:set var="flash" value="${sessionScope.flash}"/>
  <c:remove var="flash" scope="session"/>
  <div id="toast"
       role="status" aria-live="polite" aria-atomic="true"
       style="position:fixed; top:20px; right:20px; z-index:9999;
              max-width:420px; box-shadow:0 8px 24px rgba(0,0,0,.2);
              background:#101828; color:#fff; border-radius:10px; overflow:hidden;
              transform:translateY(-10px); opacity:0; transition:.25s ease;">
    <div style="display:flex; align-items:center; gap:12px; padding:14px 16px;">
      <div style="flex:1;">
        <div style="font-weight:600; margin-bottom:4px;">完了</div>
        <div>${fn:escapeXml(flash)}</div>
      </div>
      <button id="toast-ok" type="button"
              style="border:0; background:#3b82f6; color:#fff; padding:8px 12px; border-radius:8px; cursor:pointer;">
        OK
      </button>
      <button id="toast-x" type="button"
              aria-label="閉じる"
              style="border:0; background:transparent; color:#fff; opacity:.8; cursor:pointer; font-size:18px; padding:0 6px;">×</button>
    </div>
    <!-- 進行中に見せたい時はここに薄いバーを出してもOK -->
  </div>
  <script>
    (function(){
      var t = document.getElementById('toast');
      var close = function(){ t.style.opacity='0'; t.style.transform='translateY(-10px)';
                              setTimeout(function(){ t.remove(); }, 250); };
      // フェードイン
      requestAnimationFrame(function(){
        t.style.opacity='1'; t.style.transform='translateY(0)';
      });
      // 自動で消す（3秒）。ホバー中は止める。
      var timer = setTimeout(close, 3000);
      t.addEventListener('mouseenter', function(){ clearTimeout(timer); });
      t.addEventListener('mouseleave', function(){ timer = setTimeout(close, 2000); });
      document.getElementById('toast-ok').addEventListener('click', close);
      document.getElementById('toast-x').addEventListener('click', close);
    })();
  </script>
</c:if>



<main class="content">
  <h2>今日の献立</h2>

  <%
    // サーブレット側でセットしておく想定の属性名
    String menuName = (String) request.getAttribute("todayMenuName");
    String menuDesc = (String) request.getAttribute("todayMenuDesc");
    String menuImg  = (String) request.getAttribute("todayMenuImageUrl"); // 例: /images/menus/2025-11-05.jpg
  %>

  <div class="menu-card">
    <%
      if (menuName == null && menuImg == null) {
    %>
      <div class="menu-empty">本日の献立は未登録です。</div>
      <div style="margin-top:8px;">
        <a href="<%= request.getContextPath() %>/user/menus" class="button ghost">献立一覧へ</a>
      </div>
    <%
      } else {
    %>
      <div class="menu-name"><%= (menuName != null ? menuName : "本日の献立") %></div>
      <% if (menuDesc != null) { %>
        <div class="menu-desc"><%= menuDesc %></div>
      <% } %>
      <% if (menuImg != null) { %>
        <img class="menu-image" src="<%= request.getContextPath() + menuImg %>" alt="今日の献立画像">
      <% } %>
    <%
      }
    %>
  </div>
</main>

</div><!-- /.layout -->
</body>
</html>
