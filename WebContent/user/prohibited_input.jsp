<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*, bean.Allergen, bean.Individual" %>
<%
  String ctx = request.getContextPath();
  String pageTitle = "食べられない食材入力";
  request.setAttribute("pageTitle", pageTitle);

  List<Individual> persons = (List<Individual>) request.getAttribute("persons");
  java.util.UUID personId = (java.util.UUID) request.getAttribute("personId");
  List<Allergen> avoidList = (List<Allergen>) request.getAttribute("avoidList");
  Set<String> selectedCodes = (Set<String>) request.getAttribute("selectedCodes");
  if (selectedCodes == null) selectedCodes = new LinkedHashSet<String>();

  boolean hasOther = false;
  for (Allergen a : avoidList) if ("OTHER".equalsIgnoreCase(a.getCode())) { hasOther = true; break; }
%>

<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<title><%= pageTitle %></title>
<style>
/* ContactAllergy風のスタイルでOK */
:root{--bg:#fde6c9;--panel:#ffe9c2;--card:#fff;--line:#424242;--accent:#e4c155;--accent-bg:#fff7df;--text:#333;--warn:#e74c3c;}
body{background:var(--bg);color:var(--text);margin:0;font-family:"Hiragino Sans","Noto Sans JP",sans-serif;}
main.content{padding:24px 28px 48px;}
.page-title{background:var(--panel);border:1px solid var(--accent);border-radius:10px;padding:14px 18px;text-align:center;margin:8px auto 20px;max-width:980px;font-weight:600;}
.person-switch{max-width:980px;margin:0 auto 14px;display:flex;gap:8px;align-items:center;}
.person-switch select{padding:6px 10px;border-radius:6px;border:1px solid #bbb;}
.box{max-width:980px;margin:0 auto;background:#fff;border:2px solid #424242;border-radius:6px;padding:18px;}
.grid{display:grid;grid-template-columns:140px 1fr 140px 1fr;gap:14px 22px;align-items:center;}
.chk{display:inline-flex;align-items:center;gap:8px;}
.chk input{width:18px;height:18px;}
.help{grid-column:3 / 5;color:#555;font-size:.9rem;}
.input-text{grid-column:3 / 5;}
.input-text input{width:100%;padding:8px;border:1px solid #444;border-radius:4px;}
.note-warn{max-width:980px;margin:12px auto 0;color:var(--warn);font-weight:700;text-align:center;}
.actions{max-width:980px;margin:22px auto 0;display:flex;justify-content:space-between;}
.btn{padding:12px 26px;border-radius:16px/22px;border:2px solid var(--accent);background:var(--accent-bg);font-weight:600;cursor:pointer;text-decoration:none;color:#333;}
.btn[disabled]{opacity:.6;pointer-events:none;}
</style>
</head>
<body>
<main class="content">
  <h2 class="page-title"><%= pageTitle %></h2>

  <!-- 子ども切替 -->
  <form method="get" action="<%= ctx %>/user/prohibited" class="person-switch">
    <label>対象：
      <select name="person" onchange="this.form.submit()">
        <% for (Individual p : persons) { %>
          <option value="<%= p.getId() %>" <%= p.getId().equals(personId) ? "selected" : "" %>><%= p.getDisplayName() %></option>
        <% } %>
      </select>
    </label>
  </form>

  <!-- 入力 -->
  <form id="avoidForm" method="post" action="<%= ctx %>/user/prohibited">
    <input type="hidden" name="person_id" value="<%= personId %>">

    <div class="box" id="avoid-box">
      <div style="margin-bottom:10px;font-weight:700;">禁止食材（チェックまたは追加）</div>

      <div class="grid">
        <% for (Allergen a : avoidList) { String code=a.getCode(); String name=a.getNameJa(); %>
          <span class="chk">
            <input type="checkbox" name="avoid" value="<%= code %>"
                   data-code="<%= code %>" data-name="<%= name %>"
                   <%= selectedCodes.contains(code) ? "checked" : "" %> >
            <label><%= name %></label>
          </span>
          <span></span>
        <% } %>

        <% if (hasOther) { %>
          <div class="help">複数ある場合は「,」刻みで入力してください</div>
          <div class="input-text">
            <input id="otherText" type="text" name="other_text" placeholder="例）ゼラチン、えびだし">
          </div>
        <% } %>
      </div>
    </div>

    <div id="warnArea" class="note-warn" style="display:none;"></div>

    <div class="actions">
      <a class="btn" href="<%= ctx %>/user/home">戻る</a>
      <button id="submitBtn" type="submit" class="btn">登録</button>
    </div>
  </form>
</main>

<script>
(function(){
  var box = document.getElementById('avoid-box');
  var form = document.getElementById('avoidForm');
  var btn  = document.getElementById('submitBtn');
  var warn = document.getElementById('warnArea');
  var otherInput = document.getElementById('otherText');

  function anyChecked(){
    var cbs = box.querySelectorAll('input[name="avoid"]');
    for (var i=0;i<cbs.length;i++){ if (cbs[i].checked) return true; }
    return false;
  }
  function isOtherChecked(){
    var cb = box.querySelector('input[name="avoid"][data-code="OTHER"]');
    return cb ? cb.checked : false;
  }
  function validate(showMsg){
    var ok = true, msg="";
    if (!anyChecked()){ ok=false; msg="一つ以上選択してください"; }
    else if (isOtherChecked() && otherInput && !otherInput.value.trim()){
      ok=false; msg="その他の欄を記入してください";
    }
    if (showMsg){
      if(ok){warn.style.display="none";warn.textContent="";}
      else{warn.style.display="block";warn.textContent=msg;}
    }
    btn.disabled=!ok;
    return ok;
  }

  box.addEventListener('change',function(){validate(true);});
  if(otherInput) otherInput.addEventListener('input',function(){validate(false);});
  form.addEventListener('submit',function(e){
    if(!validate(true)){e.preventDefault();return false;}
  });
  validate(false);
})();
</script>
</body>
</html>
