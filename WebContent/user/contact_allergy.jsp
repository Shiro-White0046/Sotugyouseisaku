<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*, bean.Allergen, bean.Individual, bean.User" %>
<%
  String ctx = request.getContextPath();
  String pageTitle = "接触性アレルギー入力";
  request.setAttribute("pageTitle", pageTitle);

  User loginUser = (User) session.getAttribute("user");
  List<Individual> persons = (List<Individual>) request.getAttribute("persons");      // 個人一覧
  UUID personId = (UUID) request.getAttribute("personId");                             // 表示対象
  List<Allergen> contactList = (List<Allergen>) request.getAttribute("contactList");   // CONTACT候補
  Set<String> selectedCodes = (Set<String>) request.getAttribute("selectedCodes");     // 既選択(コード)
  Map<String,String> noteMap = (Map<String,String>) request.getAttribute("noteMap");   // コード→メモ
%>

<main class="content">
  <div class="page-header">
    <h2><%= pageTitle %></h2>
    <p class="muted">皮膚に触れて症状が出る可能性がある項目を選択し、必要ならメモを残してください。</p>
  </div>

  <!-- 対象児選択（multi用）。singleでも1件のみでそのまま表示 -->
  <form method="get" action="<%= ctx %>/user/allergy/contact" class="card" style="margin-bottom:1rem;">
    <label>対象：
      <select name="person" onchange="this.form.submit()">
        <% for (Individual p : persons) { %>
          <option value="<%= p.getId() %>" <%= p.getId().equals(personId) ? "selected" : "" %>>
            <%= p.getDisplayName() %>
          </option>
        <% } %>
      </select>
    </label>
    <noscript><button type="submit" class="btn">切り替え</button></noscript>
  </form>

  <form method="post" action="<%= ctx %>/user/allergy/contact" class="card">
    <input type="hidden" name="person_id" value="<%= personId %>">

    <ul class="check-list">
      <% for (Allergen a : contactList) {
           String code = a.getCode();
           String name = a.getNameJa();
           boolean checked = selectedCodes.contains(code);
           String note = noteMap.getOrDefault(code, "");
      %>
        <li>
          <label>
            <input type="checkbox" name="allergen" value="<%= code %>" <%= checked ? "checked" : "" %>>
            <%= name %>
          </label>
          <input type="text" name="note_<%= code %>" placeholder="メモ（任意）" value="<%= note %>" maxlength="100">
        </li>
      <% } %>
    </ul>

    <div class="form-actions">
      <button type="submit" class="btn primary">保存</button>
      <a class="btn ghost" href="<%= ctx %>/user/home">戻る</a>
    </div>
  </form>

  <% if (request.getAttribute("flash") != null) { %>
    <div class="alert success"><%= request.getAttribute("flash") %></div>
  <% } %>
</main>

<style>
  .check-list{list-style:none;padding:0;margin:0}
  .check-list li{margin:8px 0}
  .check-list input[type="text"]{margin-left:1em;min-width:16em}
  .alert.success{background:#e6ffef;border:1px solid #b6f2c8;padding:.6rem .8rem;margin-top:1rem;border-radius:.5rem}
</style>
