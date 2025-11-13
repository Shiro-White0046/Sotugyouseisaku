<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<jsp:include page="/header_user.jsp" />
<meta name="viewport" content="width=device-width, initial-scale=1">
<link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">

<style>
  body{background:#f7e1ca;margin:0;font-family:sans-serif}
  header{background:#f6e7be;padding:16px 20px;position:sticky;top:0;z-index:1;text-align:center}
  .wrap{max-width:980px;margin:20px auto;padding:0 16px}

  /* レイアウト：左（チェック一覧）＋右（選択中） */
  .row{display:grid; grid-template-columns: 1fr 300px; gap:16px; align-items:start}

  /* 左のスクロールボックス */
  .panel{background:#fff;border:2px solid #333;height:300px;overflow-y:auto;padding:16px;box-shadow:inset 0 0 0 2px #000}
  .grid{display:flex;flex-wrap:wrap;gap:28px 36px}
  .item{display:flex;align-items:center;gap:8px;min-width:120px}
  .other{display:flex;align-items:center;gap:8px;margin-top:16px}

  /* 右側：選択中リスト */
  .side{background:#fff;border:2px solid #333;height:300px;overflow:auto;padding:12px}
  .side h4{margin:0 0 8px 0;font-weight:600}
  .sel-list{margin:0;padding-left:18px}
  .sel-empty{color:#777}

  /* 下部ボタン */
  .actions{display:flex;justify-content:space-between;margin-top:18px}
  .btn{border:2px solid #d8c68f;background:#faefcf;padding:14px 28px;border-radius:22px;cursor:pointer}
  .btn-primary{background:#f6e7be;border-color:#e6d595}
  .btn[disabled]{opacity:.5;cursor:not-allowed}
</style>
</head>
<body>


<div class="wrap">
  <form action="${pageContext.request.contextPath}/user/allergy/confirm" method="post" id="form">
    <input type="hidden" name="_csrf" value="${csrfToken}"/>

    <div class="row">
      <!-- 左：チェック一覧（スクロール） -->
      <div class="panel" role="group" aria-labelledby="allergenGroupLabel">
        <span id="allergenGroupLabel" class="sr-only">アレルゲン選択</span>

<div class="grid" id="akGrid">
  <c:forEach var="a" items="${allergenlist}">
    <label class="item">
      <input type="checkbox"
             name="allergenIds"
             value="${a.id}"
             class="ak-check"
             <c:if test="${selectedIds != null and selectedIds.contains(a.id)}">checked</c:if>>
      <span class="ak-name">${a.nameJa}</span>
    </label>
  </c:forEach>
</div>


        <div class="other">
          <label class="item" style="margin:0">
            <input id="otherCheck" type="checkbox" name="allergenOtherFlag" value="1" class="ak-other">
            <span>その他</span>
          </label>
          <input id="otherName" type="text" name="allergenOtherName" maxlength="50" disabled
                 placeholder="その他のアレルゲン名">
        </div>
      </div>

      <!-- 右：選択中リスト -->
      <aside class="side">
        <h4>選択している項目 <span id="selCount" style="font-weight:normal;color:#666"></span></h4>
        <ul class="sel-list" id="selList">
          <li class="sel-empty">未選択です</li>
        </ul>
      </aside>
    </div>

    <!-- 下：戻る／次へ -->
    <div class="actions">
      <button type="button" class="btn" onclick="history.back()">戻る</button>
      <button type="submit" class="btn btn-primary" id="nextBtn" disabled>次へ</button>
    </div>
  </form>
</div>

<script>
  // 右側「選択している項目」を更新
  function updateSelected(){
    const list = document.getElementById('selList');
    const count = document.getElementById('selCount');
    list.innerHTML = '';

    // チェック済みの名前を収集
    const checked = Array.from(document.querySelectorAll('.ak-check:checked'))
      .map(cb => cb.closest('label').querySelector('.ak-name').textContent.trim());

    // その他
    const oc = document.getElementById('otherCheck');
    const on = document.getElementById('otherName');
    if (oc.checked) {
      on.disabled = false;
      const t = (on.value || '').trim();
      if (t) checked.push('その他: ' + t);
    } else {
      on.disabled = true;
    }

    if (checked.length === 0) {
      const li = document.createElement('li');
      li.className = 'sel-empty';
      li.textContent = '未選択です';
      list.appendChild(li);
    } else {
      checked.forEach(txt=>{
        const li = document.createElement('li');
        li.textContent = txt;
        list.appendChild(li);
      });
    }
    count.textContent = checked.length ? '(' + checked.length + '件)' : '';
    document.getElementById('nextBtn').disabled = (checked.length === 0);
  }

  // 監視
  document.addEventListener('change', (e)=>{
    if (e.target.matches('.ak-check') || e.target.matches('#otherCheck')) updateSelected();
  });
  document.addEventListener('input', (e)=>{
    if (e.target.matches('#otherName')) updateSelected();
  });

  // 初期表示
  updateSelected();
</script>
</body>
</html>
