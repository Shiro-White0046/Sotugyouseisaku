<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*, bean.Allergen, bean.Individual" %>
<%
  String ctx = request.getContextPath();
  String pageTitle = "食べられない食材入力";
  request.setAttribute("pageTitle", pageTitle);

  // サーブレットから渡されたデータ
  List<Individual> persons = (List<Individual>) request.getAttribute("persons");
  java.util.UUID personId = (java.util.UUID) request.getAttribute("personId");
  List<Allergen> avoidList = (List<Allergen>) request.getAttribute("avoidList");
  if (avoidList == null) avoidList = java.util.Collections.emptyList();
  Set<String> selectedCodes = (Set<String>) request.getAttribute("selectedCodes");
  if (selectedCodes == null) selectedCodes = new LinkedHashSet<String>();
  Map<Short,String> idToNote = (Map<Short,String>) request.getAttribute("idToNote");
  if (idToNote == null) idToNote = new LinkedHashMap<Short,String>();

  // カテゴリ分類（今回は全部 OTHER 扱い）
  Map<String,List<Allergen>> bySub = new LinkedHashMap<String,List<Allergen>>();
  for (Allergen a : avoidList) {
    String sub = a.getSubcategory();
    if (sub == null || sub.trim().isEmpty()) sub = "OTHER";
    if (!bySub.containsKey(sub)) bySub.put(sub, new ArrayList<Allergen>());
    bySub.get(sub).add(a);
  }
%>

<jsp:include page="/header_user.jsp" />

<main class="content" style="padding:24px 28px 28px;">
  <!--  <h2 class="page-title" style="background:#ffe9c2;border:1px solid #e4c155;border-radius:10px;padding:14px 18px;text-align:center;margin:8px auto 20px;max-width:1040px;font-weight:700;">
    <%= pageTitle %>
  </h2>-->

  <!-- 対象児切替 -->
  <form method="get" action="<%= ctx %>/user/avoid" style="max-width:1040px;margin:0 auto 12px;display:flex;gap:8px;align-items:center;">
    <label>対象：
      <select name="person" onchange="this.form.submit()" style="padding:6px 10px;border-radius:6px;border:1px solid #bbb;">
        <% if (persons != null) {
             for (Individual p : persons) { %>
          <option value="<%= p.getId() %>" <%= p.getId().equals(personId) ? "selected" : "" %>><%= p.getDisplayName() %></option>
        <% } } %>
      </select>
    </label>
    <noscript><button type="submit">切り替え</button></noscript>
  </form>

  <!-- 入力フォーム -->
  <form method="post" action="<%= ctx %>/user/avoid" id="avoidForm" style="max-width:1040px;margin:0 auto;">
    <input type="hidden" name="person_id" value="<%= personId %>">

    <div style="display:grid;grid-template-columns:1fr 340px;gap:18px;align-items:start;">
      <!-- 左：チェック項目 -->
      <div style="background:#fff;border:2px solid #424242;border-radius:8px;overflow:hidden;">
        <div style="padding:14px 16px;border-bottom:2px solid #424242;font-weight:600;">禁止食材（チェックまたは追加）</div>
        <div style="padding:16px;">
          <div style="max-height:380px;overflow:auto;border:1px solid #ddd;border-radius:6px;padding:12px;">
            <div style="display:flex;flex-wrap:wrap;gap:14px 22px;align-items:center;">
              <% for (Allergen a : avoidList) {
                   String code = a.getCode();
                   String name = a.getNameJa();
                   String checked = selectedCodes.contains(code) ? "checked" : "";
              %>
                <label style="display:inline-flex;align-items:center;gap:8px;font-size:1rem;">
                  <input type="checkbox" name="avoid" value="<%= code %>" <%= checked %> data-name="<%= name %>" style="width:18px;height:18px;">
                  <span><%= name %></span>
                </label>
              <% } %>
            </div>
          </div>

          <!-- その他入力 -->
          <div style="margin-top:14px;">
            <label style="display:inline-flex;align-items:center;gap:8px;font-weight:600;">
              <input type="checkbox" id="otherCheck" style="width:18px;height:18px;">その他
            </label>
            <div id="otherBox" style="margin-top:8px;display:none;">
              <input type="text" name="other_free_text" id="otherText" placeholder="例：特定の食材・宗教上の理由など"
                     style="width:100%;padding:8px 10px;border:1px solid #bbb;border-radius:6px;">
            </div>
          </div>

          <!-- 同意チェック -->
          <label style="display:flex;gap:10px;align-items:center;margin-top:16px;">
            <input type="checkbox" id="agree" required style="width:18px;height:18px;">
            <span>入力内容に誤りがないことを確認しました</span>
          </label>
        </div>
      </div>
<!-- 右：選択している項目 -->
<aside style="background:#fffbe8;border:2px solid #e7d68d;border-radius:10px;width:280px;">
  <div style="padding:10px 14px;border-bottom:2px solid #e7d68d;font-weight:700;">選択している項目</div>
  <div style="padding:12px;">
    <ul id="pickedList"
        style="list-style:disc;
               padding-left:16px; /* ← 22px → 16px にして余白を狭く */
               margin:0;
               height:260px;
               overflow-y:auto;
               border:1px solid #cfc4a1;
               border-radius:6px;
               padding:10px; /* 全体の余白も少し調整 */
               background:#fffdf5;">
      <!-- JSで項目を追加 -->
    </ul>
    <div id="pickedCount" style="text-align:center;color:#666;margin-top:6px;">0件選択中</div>
  </div>
</aside>



<div style="display:flex;
            justify-content:space-between;
            align-items:center;
            margin-top:30px; /* ← 18px → 30px にして上下もゆったり */
            max-width:1040px;
            margin-left:auto;
            margin-right:auto;
            gap:200px; /* ← 追加: ボタン間を広げる！ */">
  <a href="<%= ctx %>/user/home"
     class="btn"
     style="display:inline-block;
            width:120px;
            text-align:center;
            padding:10px 0;
            border-radius:22px;
            border:2px solid #e4c155;
            background:#fff7df;
            font-weight:600;
            text-decoration:none;
            color:#000;
            transition:transform .06s;">
     戻る
  </a>

  <button type="submit"
          class="btn"
          style="width:120px;
                 text-align:center;
                 padding:10px 0;
                 border-radius:22px;
                 border:2px solid #e4c155;
                 background:#fff7df;
                 font-weight:600;
                 cursor:pointer;
                 transition:transform .06s;">
    登録
  </button>
</div>

  </form>

  <% if (request.getAttribute("flash") != null) { %>
    <div style="max-width:1040px;margin:16px auto 0;background:#e6ffef;border:1px solid #b6f2c8;padding:.6rem .8rem;border-radius:.5rem;">
      <%= request.getAttribute("flash") %>
    </div>
  <% } %>
</main>



<script>
(function(){
  // その他チェック
  var otherCheck = document.getElementById('otherCheck');
  var otherBox = document.getElementById('otherBox');
  if (otherCheck && otherBox) {
    otherCheck.addEventListener('change', function(){
      otherBox.style.display = this.checked ? 'block' : 'none';
    });
  }

  // 選択している項目リスト更新
  var pickedList = document.getElementById('pickedList');
  var pickedCount = document.getElementById('pickedCount');
  function refreshPicked(){
    var checks = document.querySelectorAll('input[name="avoid"]:checked');
    pickedList.innerHTML = '';
    var n = 0;
    for (var i=0;i<checks.length;i++){
      n++;
      var li = document.createElement('li');
      li.textContent = checks[i].getAttribute('data-name');
      pickedList.appendChild(li);
    }
    if (document.getElementById('otherCheck').checked) {
      n++;
      var li = document.createElement('li');
      li.textContent = 'その他: ' + (document.getElementById('otherText').value || '（未入力）');
      pickedList.appendChild(li);
    }
    if(n===0){
      var li = document.createElement('li');
      li.textContent = '未選択です';
      pickedList.appendChild(li);
    }
    pickedCount.textContent = n + '件選択中';
  }

  document.addEventListener('change', function(e){
    if (e.target && (e.target.name === 'avoid' || e.target.id === 'otherCheck')) refreshPicked();
  });
  document.addEventListener('input', function(e){
    if (e.target && e.target.id === 'otherText') refreshPicked();
  });

  refreshPicked();
})();
</script>
