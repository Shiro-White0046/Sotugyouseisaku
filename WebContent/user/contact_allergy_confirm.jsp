<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*" %>
<%
  String ctx = request.getContextPath();
  String pageTitle = "接触性アレルギー入力";
  request.setAttribute("pageTitle", pageTitle);

  java.util.UUID personId = (java.util.UUID) request.getAttribute("personId");
  String personName = (String) request.getAttribute("personName");
  List<String> selectedCodes = (List<String>) request.getAttribute("selectedCodes");
  List<String> selectedNames = (List<String>) request.getAttribute("selectedNames");
  Map<String,String> notes = (Map<String,String>) request.getAttribute("notes");
%>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<title><%= pageTitle %> - 確認</title>
<style>
:root{ --bg:#fde6c9; --panel:#ffe9c2; --card:#fff; --line:#424242; --accent:#e4c155; --text:#333; }
body{ background:var(--bg); color:var(--text); margin:0; font-family:"Hiragino Sans","Noto Sans JP",sans-serif; }
main.content{ padding:24px 28px 48px; }
.page-title{ background:var(--panel); border:1px solid var(--accent); border-radius:10px;
  padding:14px 18px; text-align:center; margin:8px auto 20px; max-width:860px; font-weight:600; }

.confirm-panel{ max-width:860px; margin:0 auto; }
.box{
  background:#fff; border:2px solid var(--line); border-radius:6px;
  min-height:220px; padding:18px 20px; margin:0 auto 14px; width:80%;
}
.list{ line-height:2.2; font-size:1.05rem; }
.notice{ margin:10px 0 18px; }

.chkline{ display:flex; align-items:center; gap:12px; margin:10px 0 20px; }
.big-check{ width:36px; height:36px; }
.big-check input{ width:100%; height:100%; }

.actions{ max-width:860px; margin:16px auto 0; display:grid; grid-template-columns:1fr 1fr; }
.btn{
  display:inline-block; padding:12px 26px; border-radius:16px/22px;
  border:2px solid #e4c155; background:#fff7df; color:#333; text-align:center; font-weight:600;
  text-decoration:none; cursor:pointer;
}
.left{ justify-self:start; background:transparent; border-color:#d6d6d6; }
.right{ justify-self:end; }
.small{ font-size:.92rem; color:#666; margin-left:6px; }
</style>
</head>
<body>
<main class="content">
  <h2 class="page-title">接触性アレルギー入力</h2>

  <div class="confirm-panel">
    <!-- ①② 選択内容のボックス -->
    <div class="box">
      <div class="list">
        <% for (int i=0; i<selectedNames.size(); i++) { %>
          <div>・<%= selectedNames.get(i) %></div>
        <% } %>
      </div>
    </div>

    <!-- ③ 文言 -->
    <div class="notice">
      <%= personName == null ? "" : personName %> について、以上で登録します。よろしいでしょうか。
    </div>

    <!-- ⑤⑥ 同意チェック -->
    <div class="chkline">
      <label class="big-check">
        <input type="checkbox" id="agree" required>
      </label>
      <label for="agree">アレルギー情報の提供に同意しますか？</label>
    </div>

    <!-- ④戻る & ⑦登録 -->
    <div class="actions">
      <!-- 戻る：編集画面に戻るだけ（選択はブラウザ戻るで残ります） -->
      <a class="btn left" href="<%= ctx %>/user/allergy/contact?person=<%= personId %>">戻る</a>

      <!-- 登録：実保存は既存の ContactAllergyServlet に投げる -->
      <form method="post" action="<%= ctx %>/user/allergy/contact" style="justify-self:end;">
        <input type="hidden" name="person_id" value="<%= personId %>">
        <%-- 選択コードとメモをそのまま引き継ぐ --%>
        <% for (String code : selectedCodes) { %>
          <input type="hidden" name="allergen" value="<%= code %>">
          <% String note = (notes!=null)? notes.getOrDefault(code,"") : ""; %>
          <input type="hidden" name="note_<%= code %>" value="<%= note %>">
        <% } %>

        <button type="submit" class="btn right" onclick="return document.getElementById('agree').checked;">登録</button>
        <div class="small">※同意チェックが必要です</div>
      </form>
    </div>
  </div>
</main>
</body>
</html>
