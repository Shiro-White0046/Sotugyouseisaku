<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*, bean.Allergen, bean.Individual, bean.User" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
  String ctx = request.getContextPath();
  String pageTitle = "接触性アレルギー入力";
  request.setAttribute("pageTitle", pageTitle);

  List<Individual> persons = (List<Individual>) request.getAttribute("persons");
  java.util.UUID personId = (java.util.UUID) request.getAttribute("personId");
  List<Allergen> contactList = (List<Allergen>) request.getAttribute("contactList");
  Set<String> selectedCodes = (Set<String>) request.getAttribute("selectedCodes");
  if (selectedCodes == null) selectedCodes = new LinkedHashSet<String>();

  Map<String,String> labelMap = new LinkedHashMap<String,String>();
  labelMap.put("METAL","金属"); labelMap.put("PLANT","植物"); labelMap.put("ANIMAL","動物");
  labelMap.put("CHEMICAL","化粧品・香料／薬品"); labelMap.put("RUBBER","ゴム・樹脂"); labelMap.put("OTHER","その他");

  Map<String,List<Allergen>> bySub = new LinkedHashMap<String,List<Allergen>>();
  for (Allergen a : contactList) {
    String sub = a.getSubcategory();
    if (sub == null || sub.trim().isEmpty()) sub = "OTHER";
    if (!bySub.containsKey(sub)) bySub.put(sub, new ArrayList<Allergen>());
    bySub.get(sub).add(a);
  }
  List<String> order = new ArrayList<String>();
  for (Map.Entry<String,String> ent : labelMap.entrySet()) if (bySub.containsKey(ent.getKey())) order.add(ent.getKey());
  String firstCat = order.isEmpty() ? "OTHER" : order.get(0);
%>

<jsp:include page="/header_user.jsp" />

<style>
:root{--bg:#fde6c9;--panel:#ffe9c2;--card:#fff;--line:#424242;--accent:#e4c155;--accent-bg:#fff7df;--text:#333;}
body{background:var(--bg);color:var(--text);margin:0;font-family:"Hiragino Sans","Noto Sans JP",sans-serif;}
main.content{padding:24px 28px 48px;}
.page-title{background:var(--panel);border:1px solid var(--accent);border-radius:10px;padding:14px 18px;text-align:center;margin:8px auto 20px;max-width:980px;font-weight:600;}
.person-switch{max-width:980px;margin:0 auto 14px;display:flex;gap:8px;align-items:center;}
.person-switch select{padding:6px 10px;border-radius:6px;border:1px solid #bbb;}
/* 2カラム：左=入力、右=確認 */
.ca-layout{max-width:980px;margin:0 auto;display:grid;grid-template-columns:1fr 260px;gap:16px;align-items:start;}
.ca-panel{background:var(--card);border:2px solid var(--line);border-radius:6px;overflow:hidden;}
.ca-tabs{display:flex;border-bottom:2px solid var(--line);background:var(--card);}
.ca-tab{appearance:none;background:var(--card);border:2px solid var(--line);border-bottom:none;padding:10px 18px;font-size:.95rem;cursor:pointer;margin-right:-2px;border-top-left-radius:6px;border-top-right-radius:6px;}
.ca-tab[aria-selected="true"]{background:var(--accent-bg);border-color:var(--accent);position:relative;top:2px;}
.ca-body{padding:18px 20px 22px;min-height:260px;background:var(--card);}
.ca-checklist{display:flex;flex-wrap:wrap;gap:14px 28px;align-items:center;}
.ca-item{display:inline-flex;align-items:center;gap:8px;font-size:1rem;}
.ca-item input[type="checkbox"]{width:18px;height:18px;}
/* 右プレビュー */
.ca-preview{background:#fff7df;border:1px solid #e4c155;border-radius:8px;padding:10px;height:100%;}
.ca-preview-title{font-weight:700;text-align:center;margin-bottom:8px;}
.ca-preview-box{background:#fff;border:2px solid #424242;border-radius:6px;min-height:260px;padding:10px;overflow:auto;}
#selList{margin:0;padding-left:1.1em;line-height:1.9;}
.ca-preview-note{text-align:center;margin-top:6px;font-size:.92rem;color:#555;}
/* フッターボタン */
.form-actions{max-width:980px;margin:22px auto 0;display:grid;grid-template-columns:1fr 1fr;gap:16px;}
.btn{display:inline-block;padding:12px 26px;border-radius:16px/22px;border:2px solid var(--accent);background:var(--accent-bg);color:var(--text);text-align:center;font-weight:600;cursor:pointer;}
.form-actions .left{justify-self:start}.form-actions .right{justify-self:end}
@media (max-width:720px){.ca-layout{grid-template-columns:1fr}.form-actions{grid-template-columns:1fr}}
</style>
</head>
<body>
<main class="content">
  <!--  <h2 class="page-title"><%= pageTitle %></h2>-->

  <!-- 子ども切替 -->
  <form method="get" action="<%= ctx %>/user/allergy/contact" class="person-switch">
    <label>対象：
      <select name="person" onchange="this.form.submit()">
        <% for (Individual p : persons) { %>
          <option value="<%= p.getId() %>" <%= p.getId().equals(personId)?"selected":"" %>><%= p.getDisplayName() %></option>
        <% } %>
      </select>
    </label>
    <noscript><button class="btn" type="submit">切り替え</button></noscript>
  </form>

  <!-- 入力+右プレビュー／保存先は直接ContactAllergyServlet -->
  <form id="contactForm" method="post" action="<%= ctx %>/user/allergy/contact">
    <input type="hidden" name="person_id" value="<%= personId %>">

    <div class="ca-layout">
      <!-- 左：カテゴリ＋チェック -->
      <div class="ca-panel" id="contact-allergy">
        <div class="ca-tabs" role="tablist" aria-label="カテゴリ">
          <% for (String cat : order) { String label = labelMap.get(cat); boolean sel = cat.equals(firstCat); %>
            <button type="button" class="ca-tab" role="tab" aria-selected="<%= sel ? "true" : "false" %>" data-target="<%= cat %>"><%= label %></button>
          <% } %>
        </div>
        <div class="ca-body">
          <% for (String cat : order) { boolean hidden = !cat.equals(firstCat); %>
            <div class="ca-pane" data-cat="<%= cat %>" <%= hidden ? "hidden" : "" %>>
              <div class="ca-checklist">
                <% List<Allergen> list = bySub.get(cat);
                   for (Allergen a : list) { String code = a.getCode(); String name = a.getNameJa(); %>
                  <label class="ca-item">
                    <!-- data-name を付けて右側に表示名を渡す -->
                    <input type="checkbox" name="allergen" value="<%= code %>" data-name="<%= name %>"
                           <%= selectedCodes.contains(code) ? "checked" : "" %> >
                    <%= name %>
                  </label>
                <% } %>
              </div>
            </div>
          <% } %>
        </div>
      </div>

      <!-- 右：選択プレビュー -->
      <aside class="ca-preview">
        <div class="ca-preview-title">選択している項目</div>
        <div class="ca-preview-box"><ul id="selList"></ul></div>
        <div class="ca-preview-note" id="selNote">0件選択中</div>
      </aside>
    </div>

    <div class="form-actions">
      <a href="<%= ctx %>/user/home" class="btn left">戻る</a>
      <!-- ✅ action先を /confirm に -->
      <button id="submitBtn" type="submit" class="btn right">次へ</button>
	</div>

  </form>

  <% if (request.getAttribute("flash") != null) { %>
    <div style="max-width:980px;margin:16px auto 0;background:#e6ffef;border:1px solid #b6f2c8;padding:.6rem .8rem;border-radius:.5rem;">
      <%= request.getAttribute("flash") %>
    </div>
  <% } %>
</main>

<script>
(function(){
  var root = document.getElementById('contact-allergy');
  var tabs = root.querySelectorAll('.ca-tab');
  var panes = root.querySelectorAll('.ca-pane');
  function show(cat){
    for (var i=0;i<panes.length;i++){
      panes[i].hidden = (panes[i].getAttribute('data-cat') !== cat);
    }
    for (var j=0;j<tabs.length;j++){
      var t = tabs[j];
      t.setAttribute('aria-selected', t.getAttribute('data-target') === cat ? 'true' : 'false');
    }
  }
  for (var k=0;k<tabs.length;k++){
    (function(btn){ btn.addEventListener('click', function(){ show(btn.getAttribute('data-target')); }); })(tabs[k]);
  }

  // ===== 右側プレビュー（ES5） =====
  var selList = document.getElementById('selList');
  var selNote = document.getElementById('selNote');

  function getChecked(){
    var boxes = root.querySelectorAll('input[name="allergen"]');
    var arr = [];
    for (var i=0;i<boxes.length;i++){ if (boxes[i].checked) arr.push(boxes[i]); }
    return arr;
  }
  function render(){
    var checked = getChecked();
    while (selList.firstChild) selList.removeChild(selList.firstChild);
    for (var i=0;i<checked.length;i++){
      var li = document.createElement('li');
      var name = checked[i].getAttribute('data-name') || checked[i].value;
      li.appendChild(document.createTextNode(name));
      selList.appendChild(li);
    }
    selNote.innerHTML = checked.length + "件選択中";
  }
  root.addEventListener('change', function(e){
    var t = e.target || e.srcElement;
    if (t && t.name === 'allergen') render();
  });
  // 初期表示（既選択の反映）
  render();
})();
</script>
</body>
</html>
