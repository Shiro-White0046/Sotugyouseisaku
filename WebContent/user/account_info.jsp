<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="bean.User,bean.Individual,bean.Allergen" %>
<%@ page import="java.util.*" %>

<%
  String ctx = request.getContextPath();
  User user = (User) request.getAttribute("user");
  List<Individual> individuals = (List<Individual>) request.getAttribute("individuals");
  Map<java.util.UUID, List<Allergen>> allergyMap =
    (Map<java.util.UUID, List<Allergen>>) request.getAttribute("allergyMap");
%>

<jsp:include page="/header_user.jsp" />

<main class="content">
<h2>アカウント情報</h2>

<p>利用者：<strong><%= user.getName() %></strong></p>
<p>メール：<%= user.getEmail() %></p>
<hr style="margin:20px 0;">

<% for (Individual ind : individuals) {
     List<Allergen> als = allergyMap.get(ind.getId());
%>

<form method="post" action="<%= ctx %>/user/account-info"
      class="card"
      style="max-width:600px;margin:0 auto 24px;">
  <h3><%= ind.getDisplayName() %></h3>

  <input type="hidden" name="person_id" value="<%= ind.getId() %>">

  <label>生年月日</label>
  <input type="date" name="birthday"
         value="<%= ind.getBirthday() == null ? "" : ind.getBirthday() %>">

  <label>備考</label>
  <textarea name="note"><%= ind.getNote() == null ? "" : ind.getNote() %></textarea>

  <label>アレルギー</label>
  <% if (als == null || als.isEmpty()) { %>
    <p>(なし)</p>
  <% } else { %>
    <ul style="text-align:left;">
      <% for (Allergen a : als) { %>
        <li><%= a.getNameJa() %></li>
      <% } %>
    </ul>
  <% } %>

  <div class="actions">
    <button class="button">保存</button>
  </div>
</form>

<% } %>
</main>
