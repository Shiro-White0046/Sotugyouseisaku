<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*, bean.Allergen, bean.Individual, bean.User" %>
<%
  String ctx = request.getContextPath();
  String pageTitle = "接触性アレルギー入力";
  request.setAttribute("pageTitle", pageTitle);

  // サーブレットで詰めている前提
  List<Individual> persons = (List<Individual>) request.getAttribute("persons");
  java.util.UUID personId = (java.util.UUID) request.getAttribute("personId");
  List<Allergen> contactList = (List<Allergen>) request.getAttribute("contactList");   // category='CONTACT'
  Set<String> selectedCodes = (Set<String>) request.getAttribute("selectedCodes");     // 既選択コード
  if (selectedCodes == null) selectedCodes = new LinkedHashSet<String>();

  // subcategory→和名ラベル
  Map<String,String> labelMap = new LinkedHashMap<String,String>();
  labelMap.put("METAL",   "金属");
  labelMap.put("PLANT",   "植物");
  labelMap.put("ANIMAL",  "動物");
  labelMap.put("CHEMICAL","化粧品・香料／薬品");
  labelMap.put("RUBBER",  "ゴム・樹脂");
  labelMap.put("OTHER",   "その他");

  // subcategory ごとにグルーピング（JSP内でJava8のやり方）
  Map<String,List<Allergen>> bySub = new LinkedHashMap<String,List<Allergen>>();
  for (Allergen a : contactList) {
    String sub = a.getSubcategory();
    if (sub == null || sub.trim().isEmpty()) sub = "OTHER";
    if (!bySub.containsKey(sub)) bySub.put(sub, new ArrayList<Allergen>());
    bySub.get(sub).add(a);
  }

  // 表示順：labelMapの順で存在するものだけ並べる
  List<String> order = new ArrayList<String>();
  for (Map.Entry<String,String> ent : labelMap.entrySet()) {
    if (bySub.containsKey(ent.getKey())) order.add(ent.getKey());
  }
  // 初期タブ
  String firstCat = order.isEmpty() ? "OTHER" : order.get(0);
%>

<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<title><%= pageTitle %></title>

<style>
:root{
  --bg:#fde6c9; --panel:#ffe9c2; --card:#fff; --line:#424242;
  --accent:#e4c155; --accent-bg:#fff7df; --text:#333;
}
body{ background:var(--bg); color:var(--text); margin:0; font-family:"Hiragino Sans","Noto Sans JP",sans-serif; }
main.content{ padding:24px 28px 48px; }

.page-title{
  background:var(--panel); border:1px solid var(--accent);
  border-radius:10px; padding:14px 18px; text-align:center;
  margin:8px auto 20px; max-width:860px; font-weight:600;
}

/* 対象児選択（上部） */
.person-switch{
  max-width:860px; margin:0 auto 14px; display:flex; gap:8px; align-items:center;
}
.person-switch select{ padding:6px 10px; border-radius:6px; border:1px solid #bbb; }

.ca-panel{ max-width:860px; margin:0 auto; background:var(--card); border:2px solid var(--line);
  border-radius:6px; overflow:hidden; }
.ca-tabs{ display:flex; border-bottom:2px solid var(--line); background:var(--card); }
.ca-tab{
  appearance:none; background:var(--card); border:2px solid var(--line); border-bottom:none;
  padding:10px 18px; font-size:.95rem; cursor:pointer; margin-right:-2px;
  border-top-left-radius:6px; border-top-right-radius:6px;
}
.ca-tab[aria-selected="true"]{ background:var(--accent-bg); border-color:var(--accent); position:relative; top:2px; }

.ca-body{ padding:18px 20px 22px; min-height:260px; background:var(--card); }
.ca-checklist{ display:flex; flex-wrap:wrap; gap:14px 28px; align-items:center; }
.ca-item{ display:inline-flex; align-items:center; gap:8px; font-size:1rem; }
.ca-item input[type="checkbox"]{ width:18px; height:18px; accent-color:#4a4a4a; }

.form-actions{
  max-width:860px; margin:22px auto 0; display:grid; grid-template-columns:1fr 1fr; gap:16px;
}
.btn{
  display:inline-block; padding:12px 26px; border-radius:16px/22px;
  border:2px solid var(--accent); background:var(--accent-bg); color:var(--text);
  text-align:center; font-weight:600; cursor:pointer; text-decoration:none;
  transition:transform .06s, box-shadow .06s;
}
.btn:hover{ transform:translateY(-1px); }
.btn-ghost{ background:transparent; border-color:#d6d6d6; }
.form-actions .left{ justify-self:start; } .form-actions .right{ justify-self:end; }

@media (max-width:640px){
  .ca-tabs{ flex-wrap:wrap; }
  .ca-tab{ margin-bottom:6px; }
  .form-actions{ grid-template-columns:1fr; }
  .form-actions .left,.form-actions .right{ justify-self:stretch; }
}
</style>
</head>

<body>
<main class="content">
  <h2 class="page-title"><%= pageTitle %></h2>

  <!-- 対象児選択（multi対応） -->
  <form method="get" action="<%= ctx %>/user/allergy/contact" class="person-switch">
    <label>対象：
      <select name="person" onchange="this.form.submit()">
        <% for (Individual p : persons) { %>
          <option value="<%= p.getId() %>" <%= p.getId().equals(personId) ? "selected" : "" %>><%= p.getDisplayName() %></option>
        <% } %>
      </select>
    </label>
    <noscript><button type="submit" class="btn">切り替え</button></noscript>
  </form>

  <!-- 入力フォーム（POST） -->
 <form method="post" action="<%= ctx %>/user/allergy/contact/confirm">
  <input type="hidden" name="person_id" value="<%= personId %>">

    <div class="ca-panel" id="contact-allergy">
      <!-- タブ -->
      <div class="ca-tabs" role="tablist" aria-label="カテゴリ">
        <% for (String cat : order) { String label = labelMap.get(cat); boolean sel = cat.equals(firstCat); %>
          <button type="button" class="ca-tab" role="tab"
                  aria-selected="<%= sel ? "true" : "false" %>" data-target="<%= cat %>"><%= label %></button>
        <% } %>
      </div>

      <!-- タブ中身 -->
      <div class="ca-body">
        <% for (String cat : order) { boolean hidden = !cat.equals(firstCat); %>
          <div class="ca-pane" data-cat="<%= cat %>" <%= hidden ? "hidden" : "" %>>
            <div class="ca-checklist">
              <% List<Allergen> list = bySub.get(cat);
                 for (Allergen a : list) { String code = a.getCode(); String name = a.getNameJa(); %>
                <label class="ca-item">
                  <input type="checkbox" name="allergen" value="<%= code %>" <%= selectedCodes.contains(code) ? "checked" : "" %>>
                  <%= name %>
                </label>
              <% } %>
            </div>
          </div>
        <% } %>
      </div>
    </div>

     <div class="form-actions">
    <a href="<%= ctx %>/user/home" class="btn btn-ghost left">戻る</a>
    <button type="submit" class="btn right">次へ</button>
   </div>
  </form>

  <% if (request.getAttribute("flash") != null) { %>
    <div style="max-width:860px;margin:16px auto 0;background:#e6ffef;border:1px solid #b6f2c8;padding:.6rem .8rem;border-radius:.5rem;">
      <%= request.getAttribute("flash") %>
    </div>
  <% } %>
</main>

<script>
  (function(){
    var root = document.getElementById('contact-allergy');
    if(!root) return;
    var tabs  = root.querySelectorAll('.ca-tab');
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
      (function(btn){
        btn.addEventListener('click', function(){ show(btn.getAttribute('data-target')); });
      })(tabs[k]);
    }
  })();
</script>

</body>
</html>
