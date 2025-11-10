<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*, bean.Allergen, bean.Individual" %>
<%
  String ctx = request.getContextPath();
  String pageTitle = "食べられない食材入力";

  List<Individual> persons = (List<Individual>) request.getAttribute("persons");
  java.util.UUID personId = (java.util.UUID) request.getAttribute("personId");
  List<Allergen> avoidList = (List<Allergen>) request.getAttribute("avoidList");
  Set<String> selectedCodes = (Set<String>) request.getAttribute("selectedCodes");
  if (selectedCodes == null) selectedCodes = new LinkedHashSet<String>();
%>

<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<title><%= pageTitle %></title>
<style>
:root {
  --bg:#fde6c9; --panel:#ffe9c2; --card:#fff; --line:#3f3f3f;
  --accent:#e4c155; --accent-bg:#fff7df; --text:#333; --warn:#e74c3c;
}
body {
  background:var(--bg); color:var(--text);
  font-family:"Hiragino Sans","Noto Sans JP",sans-serif;
  margin:0;
}
main.content { padding:24px 28px 56px; }

.page-title {
  background:var(--panel); border:1px solid var(--accent);
  border-radius:10px; padding:14px 18px; text-align:center;
  margin:8px auto 20px; max-width:1080px; font-weight:700; font-size:22px;
}

/* 対象児選択 */
.person-switch { max-width:1080px; margin:0 auto 14px; display:flex; gap:8px; align-items:center; }
.person-switch select { padding:6px 10px; border-radius:6px; border:1px solid #bbb; }

/* 全体2ペイン */
.frame { max-width:1080px; margin:0 auto; display:grid; grid-template-columns:1fr 1fr; gap:18px; }
.pane {
  background:var(--card); border:2px solid var(--line); border-radius:6px; overflow:hidden;
}
.pane h3 {
  margin:0; padding:10px 14px; border-bottom:2px solid var(--line);
  font-size:18px; text-align:center;
}
.pane-body { padding:16px; }

/* 左リスト */
.scroll {
  height:360px; overflow:auto; border:1px solid #ccc;
  border-radius:6px; padding:12px;
}
.chk-grid {
  display:grid; grid-template-columns:repeat(2, minmax(220px,1fr)); gap:14px 28px;
}
.item { display:flex; align-items:center; gap:10px; font-size:18px; }
.item input[type="checkbox"] { width:20px; height:20px; accent-color:#555; }

/* その他入力欄 */
.other-wrap { margin-top:14px; display:none; }
.other-wrap input {
  width:100%; padding:8px 10px; border:1px solid #777; border-radius:6px;
}

/* 右プレビュー */
.sel-list { list-style:disc; padding-left:22px; margin:8px 0 0; min-height:320px; }
.sel-list li { margin:6px 0; font-size:18px; }
.muted { color:#777; }

/* 警告・同意・ボタン */
.warn { max-width:1080px; margin:14px auto 0; color:var(--warn); font-weight:700; text-align:center; display:none; }
.actions { max-width:1080px; margin:22px auto 0; display:flex; justify-content:space-between; align-items:center; }
.agree { display:flex; align-items:center; gap:10px; font-size:16px; }
.agree input[type="checkbox"] { width:20px; height:20px; }

.btn {
  padding:12px 26px; border-radius:16px/22px; border:2px solid var(--accent);
  background:var(--accent-bg); color:var(--text); font-weight:700;
  text-decoration:none; cursor:pointer; transition:.1s;
}
.btn:hover { transform:translateY(-1px); }
.btn[disabled] { opacity:.6; pointer-events:none; }
.btn-ghost { background:#fff; border-color:#d6d6d6; }
</style>
</head>
<body>
<main class="content">
  <h2 class="page-title"><%= pageTitle %></h2>

  <!-- 対象児選択 -->
  <form method="get" action="<%= ctx %>/user/prohibited" class="person-switch">
    <label>対象：
      <select name="person" onchange="this.form.submit()">
        <% for (Individual p : persons) { %>
          <option value="<%= p.getId() %>" <%= p.getId().equals(personId) ? "selected" : "" %>>
            <%= p.getDisplayName() %>
          </option>
        <% } %>
      </select>
    </label>
  </form>

  <!-- 本体 -->
  <form id="form" method="post" action="<%= ctx %>/user/prohibited">
    <input type="hidden" name="person_id" value="<%= personId %>">

    <div class="frame">
      <!-- 左側：食材チェック -->
      <section class="pane">
        <h3>禁止食材（チェックまたは追加）</h3>
        <div class="pane-body">
          <div class="scroll">
            <div class="chk-grid" id="chkGrid">
              <% for (Allergen a : avoidList) {
                   String code=a.getCode(); String name=a.getNameJa();
              %>
                <label class="item">
                  <input type="checkbox" name="avoid" value="<%= code %>" data-code="<%= code %>"
                         <%= selectedCodes.contains(code) ? "checked" : "" %>>
                  <%= name %>
                </label>
              <% } %>
              <!-- 「その他」追加 -->
              <label class="item">
                <input type="checkbox" name="avoid" value="OTHER" data-code="OTHER" id="otherChk">
                その他
              </label>
            </div>

            <!-- その他入力欄 -->
            <div class="other-wrap" id="otherWrap">
              <label>複数ある場合は「、」区切りで入力してください
                <input type="text" name="other_text" id="otherText" placeholder="例）えびだし、ゼラチンなど">
              </label>
            </div>
          </div>
        </div>
      </section>

      <!-- 右側：選択項目プレビュー -->
      <section class="pane">
        <h3>選択している項目</h3>
        <div class="pane-body">
          <ul id="selectedList" class="sel-list">
            <li class="muted">未選択です</li>
          </ul>
        </div>
      </section>
    </div>

    <div id="warn" class="warn"></div>

    <div class="actions">
      <a href="<%= ctx %>/user/home" class="btn btn-ghost">戻る</a>

      <label class="agree">
        <input type="checkbox" id="agree">
        以上の情報の提供に同意します
      </label>

      <button id="submitBtn" type="submit" class="btn" disabled>登録</button>
    </div>
  </form>
</main>

<script>
(function(){
  var grid = document.getElementById('chkGrid');
  var list = document.getElementById('selectedList');
  var otherChk = document.getElementById('otherChk');
  var otherWrap = document.getElementById('otherWrap');
  var otherText = document.getElementById('otherText');
  var warn = document.getElementById('warn');
  var agree = document.getElementById('agree');
  var submitBtn = document.getElementById('submitBtn');

  function toggleOther(){
    if (otherWrap) otherWrap.style.display = otherChk.checked ? 'block' : 'none';
    if (!otherChk.checked && otherText) otherText.value='';
  }

  function buildPreview(){
    while(list.firstChild){ list.removeChild(list.firstChild); }
    var cbs = grid.querySelectorAll('input[name="avoid"]');
    var any=false;
    for (var i=0;i<cbs.length;i++){
      if(!cbs[i].checked) continue;
      any=true;
      var label = cbs[i].parentNode.textContent.trim();
      if(cbs[i].getAttribute('data-code')==='OTHER' && otherText.value.trim()){
        label += "：" + otherText.value.trim();
      }
      var li = document.createElement('li');
      li.textContent = label;
      list.appendChild(li);
    }
    if(!any){
      var li=document.createElement('li');
      li.className='muted';
      li.textContent='未選択です';
      list.appendChild(li);
    }
  }

  function validate(showMsg){
    var cbs=grid.querySelectorAll('input[name="avoid"]');
    var any=false;
    for(var i=0;i<cbs.length;i++){ if(cbs[i].checked){ any=true; break; } }

    var ok=true,msg="";
    if(!any){ ok=false; msg="一つ以上選択してください"; }
    else if(otherChk.checked && !otherText.value.trim()){
      ok=false; msg="「その他」の内容を入力してください";
    }
    if(!agree.checked){ ok=false; msg = msg || "同意チェックを付けてください"; }

    if(showMsg){
      if(ok){ warn.style.display='none'; }
      else{ warn.style.display='block'; warn.textContent=msg; }
    }
    submitBtn.disabled=!ok;
    return ok;
  }

  grid.addEventListener('change', function(e){
    var t=e.target;
    if(t && t.id==='otherChk') toggleOther();
    buildPreview();
    validate(false);
  });
  if(otherText) otherText.addEventListener('input', function(){ buildPreview(); validate(false); });
  agree.addEventListener('change', function(){ validate(false); });

  toggleOther();
  buildPreview();
  validate(false);

  document.getElementById('form').addEventListener('submit', function(ev){
    if(!validate(true)){ ev.preventDefault(); return false; }
  });
})();
</script>
</body>
</html>
