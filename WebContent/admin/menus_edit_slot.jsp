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
      <!-- ★変更：名前入力＋画像アップロード＋プレビューを1つの行ブロックに -->
      <div class="top-row">
        <!-- 左：献立名入力 -->
        <div class="top-row__left">
          <div style="display:flex;align-items:center;gap:12px;flex-wrap:wrap;margin-bottom:14px;">
            <div style="width:92px;">献立名入力</div>
            <input type="text"
                   name="mealName_${selectedSlot}"
                   value="${meals[selectedSlot].name}"
                   style="flex:1;min-width:260px;"
                   placeholder="例：シーフードカレーセット" />
          </div>

          <!-- ★追加：画像選択＆アップロードボタン（同じ画面で完結） -->
          <div class="image-upload-box">
            <label style="font-weight:600;display:block;margin-bottom:4px;">献立画像</label>
            <input type="file"
                   id="mealImageInput"
                   accept="image/*"
                   style="margin-bottom:8px;" />
            <div style="display:flex;align-items:center;gap:8px;flex-wrap:wrap;">
              <button type="button"
                      class="button outline"
                      id="mealImageUploadBtn">
                画像をアップロード
              </button>
              <span id="mealImageStatus"
                    style="font-size:12px;color:#666;">
                ※ 画像を選択して「画像をアップロード」を押すと即保存されます
              </span>
            </div>
          </div>
        </div>

        <!-- 右：現在の画像プレビュー -->
        <div class="top-row__right">
          <div class="current-image-card">
            <div style="font-weight:600;margin-bottom:6px;">現在の画像</div>
            <c:choose>
              <c:when test="${meals[selectedSlot] != null && not empty meals[selectedSlot].imagePath}">
                <img id="mealImagePreview"
                     src="<%=ctx%>/${meals[selectedSlot].imagePath}"
                     alt="登録済み画像"
                     style="max-width:100%;height:auto;border:1px solid #ccc;padding:4px;border-radius:4px;">
              </c:when>
              <c:otherwise>
                <img id="mealImagePreview"
                     src=""
                     alt="プレビュー"
                     style="display:none;max-width:100%;height:auto;border:1px solid #ccc;padding:4px;border-radius:4px;">
                <p id="mealImageEmptyMsg"
                   style="margin:0;color:#777;font-size:13px;">
                  まだ画像が登録されていません。
                </p>
              </c:otherwise>
            </c:choose>
          </div>
        </div>
      </div>
      <!-- ★ここまで：トップ行 -->

      <div style="text-align:right;margin:16px 0 8px;">
        <button type="button" class="button" onclick="addItem()">献立枠追加</button>
        <div id="addedHint" style="color:#c44;font-size:12px;display:none;">※枠を追加しました</div>
      </div>

     <div id="itemsContainer">
  <c:forEach var="it" items="${selectedItems}" varStatus="st">
    <c:set var="idx" value="${st.index}" />
    <div class="row" data-idx="${idx}">
      <input type="hidden" name="itemId_${selectedSlot}_${idx}" value="${it.id}" />
      <input type="text" class="dish" name="itemName_${selectedSlot}_${idx}" value="${it.name}" placeholder="品名" />
      <button type="button" class="button outline" onclick="toggleAlg(${idx})">アレルギー項目追加</button>

      <!-- ★追加：赤い削除ボタン -->
      <button type="button"
              class="button button-delete"
              onclick="deleteRow(${idx})">
        削除
      </button>
    </div>

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
  .button.outline{background:#fff;border:1px solid #999; color:#333;}
  .button.sm{padding:4px 8px;font-size:12px;}
  .alg-box{display:none;border:1px solid #ddd;border-radius:10px;padding:10px;margin:8px 0 14px 0;background:#fff;}
  .alg-header{display:flex;justify-content:space-between;align-items:center;margin-bottom:8px;}
  .chips{display:flex;flex-wrap:wrap;gap:8px;}
  .chip{border:1px solid #ddd;padding:6px 10px;border-radius:10px;display:inline-flex;gap:6px;align-items:center;}

  /* ★追加：上部の2カラムレイアウト（名前＋画像エリア） */
  .top-row{
    display:flex;
    gap:24px;
    align-items:flex-start;
    flex-wrap:wrap;
  }
  .top-row__left{
    flex: 2 1 320px;
  }
  .top-row__right{
    flex: 1 1 240px;
  }
  .current-image-card{
    border:1px solid #ddd;
    border-radius:8px;
    padding:8px;
    background:#fff;
    text-align:center;
  }
  .image-upload-box{
    margin-top:4px;
    padding:8px 0;
  }
  .delete-btn {
  color: #c00;
  border-color: #c88;
}
.delete-btn:hover {
  background: #fee;
}
  .row{
    display:flex;
    align-items:center;
    gap:16px;
    margin:10px 0;

    /* ★追加：削除アニメーション用トランジション */
    transition:
      opacity .22s ease,
      transform .22s ease,
      max-height .22s ease,
      margin .22s ease,
      padding .22s ease;
  }

  .dish{flex:1;min-width:260px;}
  .link{color:#1772d0;text-decoration:underline;}
  .button.outline{background:#fff;border:1px solid #999; color:#333;}
  .button.sm{padding:4px 8px;font-size:12px;}
  .alg-box{
    display:none;
    border:1px solid #ddd;
    border-radius:10px;
    padding:10px;
    margin:8px 0 14px 0;
    background:#fff;

    /* ★追加：アニメーションのためのトランジション */
    transition:
      opacity .22s ease,
      transform .22s ease,
      max-height .22s ease,
      margin .22s ease,
      padding .22s ease;
  }
  .alg-header{display:flex;justify-content:space-between;align-items:center;margin-bottom:8px;}
  .chips{display:flex;flex-wrap:wrap;gap:8px;}
  .chip{border:1px solid #ddd;padding:6px 10px;border-radius:10px;display:inline-flex;gap:6px;align-items:center;}

  /* ★追加：赤い削除ボタン（少し大きめ） */
  .button.button-delete{
    background:#e53935;
    border:1px solid #c62828;
    color:#fff;
    font-weight:700;
    padding:8px 18px;
    font-size:14px;
  }
  .button.button-delete:hover{
    background:#d32f2f;
  }
  .button.button-delete:active{
    transform: translateY(1px);
  }

  /* ★追加：スライドしながら消える用のクラス */
  .row-removing{
    opacity:0;
    transform: translateX(-16px);
    max-height:0;
    margin-top:0;
    margin-bottom:0;
    padding-top:0;
    padding-bottom:0;
  }
  .alg-removing{
    opacity:0;
    transform: translateX(-16px);
    max-height:0;
    margin-top:0;
    margin-bottom:0;
    padding-top:0;
    padding-bottom:0;
  }

</style>

<template id="tpl">
  <div class="row" data-idx="__IDX__">
    <input type="hidden" name="itemId_${selectedSlot}___IDX__" value=""/>
    <input type="text" class="dish" name="itemName_${selectedSlot}___IDX__" placeholder="品名"/>
    <button type="button" class="button outline" onclick="toggleAlg(__IDX__)">アレルギー項目追加</button>

       <!-- ★追加：テンプレ用の削除ボタン -->
    <button type="button"
            class="button button-delete"
            onclick="deleteRow(__IDX__)">
      削除
    </button>
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
      .replace(/__IDX__/g, String(nextIdx))
      .replace(/___IDX__/g, String(nextIdx));
    var box = document.createElement('div'); box.innerHTML = h;
    var frag = document.createDocumentFragment();
    while(box.firstChild){ frag.appendChild(box.firstChild); }
    document.getElementById('itemsContainer').appendChild(frag);
    nextIdx++;
    updateRows();
    var hint=document.getElementById('addedHint'); if(hint) {hint.style.display='block'; setTimeout(function(){hint.style.display='none';},2000);}
  };

  window.toggleAlg = function(idx){
    var el = document.getElementById('alg_'+idx);
    if(!el) return;
    el.style.display = (el.style.display==='none' || el.style.display==='') ? 'block' : 'none';
  };
  window.deleteRow = function(idx){
	  // row 本体
	  var row = document.querySelector('.row[data-idx="'+idx+'"]');
	  if (row) row.remove();

	  // アレルギー枠
	  var alg = document.getElementById('alg_' + idx);
	  if (alg) alg.remove();

	  // rowsField を更新
	  updateRows();
	};
	  window.deleteRow = function(idx){
		    // row 本体
		    var row = document.querySelector('.row[data-idx="'+idx+'"]');
		    // アレルギー枠
		    var alg = document.getElementById('alg_' + idx);

		    if (!row && !alg) return;

		    // ★アニメーション用クラス付与
		    if (row)  row.classList.add('row-removing');
		    if (alg)  alg.classList.add('alg-removing');

		    // トランジションが終わった頃に DOM から削除
		    setTimeout(function(){
		      if (row && row.parentNode) row.parentNode.removeChild(row);
		      if (alg && alg.parentNode) alg.parentNode.removeChild(alg);
		      updateRows();   // rowsField 更新
		    }, 230); // CSSの .22s に合わせた時間
		  };



  document.getElementById('slotForm').addEventListener('submit', updateRows);

  // ★追加：画像アップロード処理（画面遷移なしで /image サーブレットへ投げる）
  (function(){
    var btn   = document.getElementById('mealImageUploadBtn');
    var input = document.getElementById('mealImageInput');
    var status = document.getElementById('mealImageStatus');
    var preview = document.getElementById('mealImagePreview');
    var emptyMsg = document.getElementById('mealImageEmptyMsg');

    if (!btn || !input) return;

    btn.addEventListener('click', function(e){
      e.preventDefault();

      if (!input.files || !input.files[0]) {
        alert('アップロードする画像を選択してください');
        return;
      }

      var file = input.files[0];

      var formData = new FormData();
      formData.append('dayId', '${menuDay.id}');
      formData.append('slot',  '${selectedSlot}');
      formData.append('imageFile', file);

      status.textContent = 'アップロード中...';
      status.style.color = '#555';

      fetch('<%=ctx%>/admin/menus_new/image', {
        method: 'POST',
        body: formData
      }).then(function(res){
        if (!res.ok) {
          throw new Error('アップロードに失敗しました');
        }
        // サーバー側のレスポンス内容には依存しない（成功したとみなす）
        // ローカルプレビューを即更新
        var url = URL.createObjectURL(file);
        if (preview) {
          preview.src = url;
          preview.style.display = 'inline-block';
        }
        if (emptyMsg) {
          emptyMsg.style.display = 'none';
        }
        status.textContent = '画像を保存しました（画面の入力内容は維持されています）';
        status.style.color = '#0a7';
      }).Identifier(function(err){
        console.error(err);
        status.textContent = '画像のアップロードに失敗しました。時間をおいて再度お試しください。';
        status.style.color = '#c00';
      });
    });

    // ファイル選択した瞬間にプレビューだけ先に出す（任意）
    input.addEventListener('change', function(){
      if (!input.files || !input.files[0]) return;
      var file = input.files[0];
      var url = URL.createObjectURL(file);
      if (preview) {
        preview.src = url;
        preview.style.display = 'inline-block';
      }
      if (emptyMsg) {
        emptyMsg.style.display = 'none';
      }
      status.textContent = '選択中の画像をまだ保存していません。「画像をアップロード」を押してください。';
      status.style.color = '#b58900';
    });
  })();

})();
</script>
