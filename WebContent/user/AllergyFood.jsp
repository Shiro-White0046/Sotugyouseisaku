<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*, bean.Allergen, bean.Individual" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%
  String ctx = request.getContextPath();
  String pageTitle = "食物性アレルギー入力";
  request.setAttribute("pageTitle", pageTitle);

  List<Individual> persons =
      (List<Individual>) request.getAttribute("persons");
  java.util.UUID personId =
      (java.util.UUID) request.getAttribute("personId");
  List<Allergen> allergenlist =
      (List<Allergen>) request.getAttribute("allergenlist");
  Set<Short> selectedIds =
      (Set<Short>) request.getAttribute("selectedIds");
  if (selectedIds == null) {
    selectedIds = new java.util.LinkedHashSet<Short>();
  }
%>

<jsp:include page="/header_user.jsp" />

<style>
  body{background:#f7e1ca;margin:0;font-family:"Hiragino Sans","Noto Sans JP",sans-serif;}
  main.content{padding:24px 28px 48px;}
  .wrap{max-width:980px;margin:0 auto;padding:0 16px;}

  .person-switch{max-width:980px;margin:0 auto 14px;display:flex;gap:8px;align-items:center;}
  .person-switch select{padding:6px 10px;border-radius:6px;border:1px solid #bbb;}

  .row{display:grid;grid-template-columns:1fr 260px;gap:16px;align-items:start;}
  .panel{background:#fff;border:2px solid #333;height:300px;overflow-y:auto;
         padding:16px;box-shadow:inset 0 0 0 2px #000;}
  .grid{display:flex;flex-wrap:wrap;gap:28px 36px;}
  .item{display:flex;align-items:center;gap:8px;min-width:120px;}
  .other{display:flex;align-items:center;gap:8px;margin-top:16px;}

  .side{background:#fff;border:2px solid #333;height:300px;overflow:auto;padding:12px;}
  .side h4{margin:0 0 8px 0;font-weight:600;}
  .sel-list{margin:0;padding-left:18px;}
  .sel-empty{color:#777;}

  .actions{display:flex;justify-content:space-between;margin-top:18px;max-width:980px;margin-left:auto;margin-right:auto;}
  .btn{border:2px solid #d8c68f;background:#faefcf;padding:14px 28px;border-radius:22px;cursor:pointer;text-decoration:none;color:#333;font-weight:600;}
  .btn-primary{background:#f6e7be;border-color:#e6d595;}
  .btn[disabled]{opacity:.5;cursor:not-allowed;}

  @media (max-width:720px){
    .row{grid-template-columns:1fr;}
    .actions{flex-direction:column;gap:10px;}
    .actions .btn{width:100%;text-align:center;}
  }
</style>
</head>
<body>
<main class="content">

  <!-- 対象児切り替え -->
  <form method="get" action="<%= ctx %>/user/allergy/food" class="person-switch">
    <label>対象：
      <select name="person" onchange="this.form.submit()">
        <% for (Individual p : persons) { %>
          <option value="<%= p.getId() %>"
            <%= p.getId().equals(personId) ? "selected" : "" %>>
            <%= p.getDisplayName() %>
          </option>
        <% } %>
      </select>
    </label>
    <noscript><button type="submit" class="btn">切り替え</button></noscript>
  </form>
  <div class="wrap">
    <form action="${pageContext.request.contextPath}/user/allergy/confirm" method="post" id="form">
  <input type="hidden" name="person_id" value="${personId}"/>

      <!-- ★ 確認・登録まで person_id を引き回す -->
      <input type="hidden" name="person_id" value="<%= personId %>"/>

      <div class="row">
        <!-- 左：チェック一覧 -->
        <div class="panel" role="group" aria-labelledby="allergenGroupLabel">
          <span id="allergenGroupLabel" class="sr-only">アレルゲン選択</span>

          <div class="grid" id="akGrid">
            <% if (allergenlist != null) {
                 for (Allergen a : allergenlist) {
                   short id = a.getId();
                   String name = a.getNameJa();
            %>
              <label class="item">
                <input type="checkbox"
                       name="allergenIds"
                       value="<%= id %>"
                       class="ak-check"
                       <%= selectedIds.contains(id) ? "checked" : "" %> >
                <span class="ak-name"><%= name %></span>
              </label>
            <%   }
               } %>
          </div>

          <div class="other">
            <label class="item" style="margin:0">
              <input id="otherCheck" type="checkbox" name="allergenOtherFlag"
                     value="1" class="ak-other">
              <span>その他</span>
            </label>
            <input id="otherName" type="text" name="allergenOtherName"
                   maxlength="50" disabled placeholder="その他のアレルゲン名">
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
        <a href="<%= ctx %>/user/home" class="btn">戻る</a>
        <button type="submit" class="btn btn-primary" id="nextBtn" disabled>次へ</button>
      </div>
    </form>
  </div>

</main>

<script>
  // 右側「選択している項目」を更新
  function updateSelected(){
    var list  = document.getElementById('selList');
    var count = document.getElementById('selCount');
    while (list.firstChild) list.removeChild(list.firstChild);

    var checked = [];
    var boxes = document.querySelectorAll('.ak-check:checked');
    for (var i=0;i<boxes.length;i++){
      var label = boxes[i].closest('label');
      var nameEl = label ? label.querySelector('.ak-name') : null;
      var name = nameEl ? nameEl.textContent.replace(/^\s+|\s+$/g,'') : '';
      if (name) checked.push(name);
    }

    var oc = document.getElementById('otherCheck');
    var on = document.getElementById('otherName');
    if (oc.checked){
      on.disabled = false;
      var t = (on.value || '').replace(/^\s+|\s+$/g,'');
      if (t){ checked.push('その他: ' + t); }
    }else{
      on.disabled = true;
    }

    if (checked.length === 0){
      var li = document.createElement('li');
      li.className = 'sel-empty';
      li.textContent = '未選択です';
      list.appendChild(li);
    }else{
      for (var j=0;j<checked.length;j++){
        var li2 = document.createElement('li');
        li2.textContent = checked[j];
        list.appendChild(li2);
      }
    }
    count.textContent = checked.length ? '(' + checked.length + '件)' : '';
    document.getElementById('nextBtn').disabled = (checked.length === 0);
  }

  document.addEventListener('change', function(e){
    var t = e.target || e.srcElement;
    if (t && (t.classList.contains('ak-check') || t.id === 'otherCheck')){
      updateSelected();
    }
  });
  document.addEventListener('input', function(e){
    var t = e.target || e.srcElement;
    if (t && t.id === 'otherName'){
      updateSelected();
    }
  });

  // 初期表示（既選択ぶん反映）
  updateSelected();
</script>
</body>
</html>
