<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
  request.setAttribute("pageTitle", "献立作成ページ");
  String ctx = request.getContextPath();
%>
<jsp:include page="/header.jsp" />

<main class="content">
  <h2 style="text-align:center;">${menuDay.menuDate} の献立（${selectedSlot}）</h2>

  <c:if test="${not empty error}">
    <div class="alert danger">${error}</div>
  </c:if>
  <c:if test="${not empty sessionScope.flash}">
    <div class="alert success">${sessionScope.flash}</div>
    <c:remove var="flash" scope="session"/>
  </c:if>

  <form id="slotForm" method="post" action="<%=ctx%>/admin/menus_new/edit">
    <input type="hidden" name="dayId" value="${menuDay.id}" />
    <input type="hidden" name="slot"  value="${selectedSlot}" />

    <div class="card" style="padding:16px;max-width:880px;margin:0 auto;">
      <!-- 上段：献立名と画像リンク -->
      <div style="display:flex;align-items:center;gap:12px;flex-wrap:wrap;margin-bottom:14px;">
        <div style="width:92px;">献立名入力</div>
        <input type="text" name="mealName_${selectedSlot}" value="${meals[selectedSlot].name}"
               style="flex:1;min-width:260px;" placeholder="例：シーフードカレー" />
        <a class="link" href="<%=ctx%>/admin/menus_new/image?dayId=${menuDay.id}">献立の画像を追加</a>
        <span style="color:#666;">${menuDay.imagePath}</span>
      </div>

      <!-- 右上：枠追加 -->
      <div style="text-align:right;margin-bottom:8px;">
        <button type="button" class="button" onclick="addItem()">献立枠追加</button>
        <div id="addedHint" style="color:#c44;font-size:12px;display:none;">※枠を追加しました</div>
      </div>

      <!-- 既存アイテム -->
      <div id="itemsContainer">
        <c:forEach var="it" items="${selectedItems}" varStatus="st">
          <c:set var="idx" value="${st.index}" />
          <div class="row" data-idx="${idx}">
            <input type="hidden" name="itemId_${selectedSlot}_${idx}" value="${it.id}" />
            <input type="text" class="dish" name="itemName_${selectedSlot}_${idx}" value="${it.name}" placeholder="品名" />
            <button type="button" class="button outline" onclick="toggleAlg(${idx})">アレルギー項目追加</button>
          </div>

          <!-- ✅ 初期は非表示。ボタンで開閉 -->
          <div class="alg-box" id="alg_${idx}">
            <div class="alg-header">
              <strong>アレルギー（FOOD）</strong>
              <button type="button" class="button ghost sm" onclick="toggleAlg(${idx})">閉じる</button>
            </div>
            <div class="chips">
              <c:forEach var="a" items="${allergens}">
                <label class="chip">
                  <input type="checkbox"
                         name="allergens_${selectedSlot}_${idx}"
                         value="${a.id}"
                         <c:if test="${not empty it.allergenIds and it.allergenIds.contains(a.id)}">checked</c:if> />
                  ${a.nameJa}
                </label>
              </c:forEach>
            </div>
          </div>
        </c:forEach>
      </div>

      <!-- rows をJSで維持 -->
      <input id="rowsField" type="hidden" name="${selectedSlot}_rows"
             value="<c:forEach var='x' items='${selectedItems}' varStatus='s'>${s.index}<c:if test='${!s.last}'>,</c:if></c:forEach>" />

      <div style="margin-top:16px;text-align:right;">
        <a class="button ghost" href="<%=ctx%>/admin/menus_new/select?dayId=${menuDay.id}">戻る</a>
        <button type="submit" class="button">確定</button>
      </div>
    </div>

    <label style="display:block;margin-top:12px;">説明</label>
    <textarea name="mealDesc_${selectedSlot}" rows="3" style="width:100%">${meals[selectedSlot].description}</textarea>
  </form>
</main>

<jsp:include page="/footer.jsp" />

<style>
  .row{display:flex;align-items:center;gap:16px;margin:10px 0;}
  .dish{flex:1;min-width:260px;}
  .link{color:#1772d0;text-decoration:underline;}
  .button.outline{
    background:#fff;
    border:1px solid #999;
    color:#333;
  }
  .button.outline:hover{
    background:#f5f5f5;
  }
  .button.sm{padding:4px 8px;font-size:12px;}
  .alg-box{display:none;border:1px solid #ddd;border-radius:10px;padding:10px;margin:8px 0 14px 0;background:#fff;}
  .alg-header{display:flex;justify-content:space-between;align-items:center;margin-bottom:8px;}
  .chips{display:flex;flex-wrap:wrap;gap:8px;}
  .chip{border:1px solid #ddd;padding:6px 10px;border-radius:10px;display:inline-flex;gap:6px;align-items:center;}
</style>

<!-- 追加テンプレート -->
<template id="tpl">
  <div class="row" data-idx="__IDX__">
    <input type="hidden" name="itemId_${selectedSlot}___IDX__" value=""/>
    <input type="text" class="dish" name="itemName_${selectedSlot}___IDX__" placeholder="品名"/>
    <button type="button" class="button outline" onclick="toggleAlg(__IDX__)">アレルギー項目追加</button>
  </div>
  <div class="alg-box" id="alg___IDX__">
    <div class="alg-header">
      <strong>アレルギー（FOOD）</strong>
      <button type="button" class="button ghost sm" onclick="toggleAlg(__IDX__)">閉じる</button>
    </div>
    <div class="chips">
      <c:forEach var="a" items="${allergens}">
        <label class="chip">
          <input type="checkbox" name="allergens_${selectedSlot}___IDX__" value="${a.id}"/>
          ${a.nameJa}
        </label>
      </c:forEach>
    </div>
  </div>
</template>

<script>
(function(){
  var nextIdx = (function(){
    var nodes = document.querySelectorAll('#itemsContainer .row[data-idx]');
    var max=-1; for(var i=0;i<nodes.length;i++){var n=parseInt(nodes[i].getAttribute('data-idx'),10); if(!isNaN(n)&&n>max)max=n;}
    return max+1;
  })();

  function updateRows(){
    var idxs=[];
    document.querySelectorAll('#itemsContainer .row[data-idx]').forEach(function(r){
      idxs.push(r.getAttribute('data-idx'));
    });
    document.getElementById('rowsField').value = idxs.join(',');
  }

  window.addItem = function(){
    var h = document.getElementById('tpl').innerHTML
      .replaceAll('__IDX__', String(nextIdx))
      .replaceAll('___IDX__', String(nextIdx));
    var box = document.createElement('div'); box.innerHTML = h;
    var frag = document.createDocumentFragment();
    while(box.firstChild){ frag.appendChild(box.firstChild); }
    document.getElementById('itemsContainer').appendChild(frag);
    nextIdx++;
    updateRows();
    var hint=document.getElementById('addedHint');
    if(hint) {hint.style.display='block'; setTimeout(()=>hint.style.display='none',2000);}
  };

  window.toggleAlg = function(idx){
    var el = document.getElementById('alg_'+idx);
    if(!el) return;
    el.style.display = (el.style.display==='none' || el.style.display==='') ? 'block' : 'none';
  };

  document.getElementById('slotForm').addEventListener('submit', updateRows);
})();
</script>
