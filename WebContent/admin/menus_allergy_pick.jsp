<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
  request.setAttribute("pageTitle", "アレルギー項目選択");
  String ctx = request.getContextPath();
%>
<jsp:include page="/header.jsp" />

<main class="content" style="max-width:960px;margin:0 auto;">
  <h2 style="text-align:center;margin-bottom:12px;">
    <input type="text" value="${itemName}" readonly
           style="text-align:center;font-size:20px;padding:6px 10px;border:1px solid #ccc;border-radius:6px;min-width:280px;">
    <span style="margin-left:18px;">アレルギー項目</span>
  </h2>

  <div class="grid">
    <c:forEach var="a" items="${allergens}">
      <label class="tile">
        <input type="checkbox" class="alg" value="${a.id}">
        <div class="pic">
          <!-- 画像があれば表示。なければ名前のみ -->
          <c:choose>
            <c:when test="${not empty a.imagePath}">
              <img src="<%=ctx%>/${a.imagePath}" alt="${a.nameJa}">
            </c:when>
            <c:otherwise>
              <div class="ph">${a.nameJa}</div>
            </c:otherwise>
          </c:choose>
        </div>
        <div class="name">${a.nameJa}</div>
      </label>
    </c:forEach>
  </div>

  <div style="text-align:right;margin-top:12px;">
    <button id="saveBtn" class="button">項目を追加</button>
  </div>

  <div style="margin-top:10px;">
    <a class="button ghost" href="<%=ctx%>/admin/menus_new/edit?dayId=${dayId}&slot=${slot}">戻る</a>
  </div>
</main>

<jsp:include page="/footer.jsp" />

<style>
  .grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(140px,1fr));gap:12px;border:1px solid #333;padding:12px;}
  .tile{display:flex;flex-direction:column;border:1px solid #ccc;border-radius:10px;padding:8px;gap:6px;align-items:center;}
  .tile input{align-self:flex-start;}
  .pic{width:100%;aspect-ratio:4/3;display:flex;align-items:center;justify-content:center;border-radius:8px;border:1px solid #eee;overflow:hidden;background:#fff;}
  .pic img{max-width:100%;height:auto;display:block;}
  .ph{padding:8px;text-align:center;}
  .name{font-size:14px;}
</style>

<script>
(function(){
  var dayId = "${dayId}";
  var slot  = "${slot}";
  var idx   = "${idx}";
  var key   = "alg-"+dayId+"-"+slot+"-"+idx;

  // 既存選択の復元
  document.addEventListener('DOMContentLoaded', function(){
    var prev = localStorage.getItem(key);
    if(!prev) return;
    var set = new Set(prev.split(',').filter(Boolean));
    document.querySelectorAll('.alg').forEach(function(cb){
      cb.checked = set.has(cb.value);
    });
  });

  // 保存 → localStorage に入れて edit 画面へ戻る
  document.getElementById('saveBtn').addEventListener('click', function(){
    var ids=[];
    document.querySelectorAll('.alg:checked').forEach(function(cb){ ids.push(cb.value); });
    localStorage.setItem(key, ids.join(','));
    location.href = "<%=ctx%>/admin/menus_new/edit?dayId="+encodeURIComponent(dayId)+"&slot="+encodeURIComponent(slot);
  });
})();
</script>
