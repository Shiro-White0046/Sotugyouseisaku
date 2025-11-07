<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="bean.Individual, bean.User" %>
<jsp:include page="/header.jsp"/>

<%
  String ctx = request.getContextPath();
  Individual ind = (Individual) request.getAttribute("individual");
  User parent = (User) request.getAttribute("parentUser");
%>

<main class="content">
  <h2>利用者の削除確認</h2>

  <div class="card" style="max-width:720px;margin:0 auto;">
    <p>この個人データを削除します。よろしいですか？<br>
       <strong style="color:#b91c1c;">※ この操作は取り消せません。</strong></p>

    <table class="simple-table" style="width:100%;border-collapse:collapse;margin-top:8px;">
      <tr><th style="text-align:left;width:220px;">氏名</th><td><%= ind.getDisplayName() %></td></tr>
      <tr><th style="text-align:left;">生年月日</th><td><%= (ind.getBirthday()!=null? ind.getBirthday() : "（未設定）") %></td></tr>
      <tr><th style="text-align:left;">備考</th><td><%= (ind.getNote()!=null? ind.getNote() : "") %></td></tr>
      <tr><th style="text-align:left;">親アカウント</th>
          <td><%= (parent!=null? (parent.getName()+"（ID: "+parent.getLoginId()+"）") : "（不明）") %></td></tr>
    </table>

    <form method="post" action="<%= ctx %>/admin/users/delete" class="actions" style="display:flex;justify-content:space-between;margin-top:16px;">
      <input type="hidden" name="id" value="<%= ind.getId() %>">
      <a class="button ghost" href="<%= ctx %>/admin/users">キャンセル</a>
      <button type="submit" class="button" style="background:#dc2626;">削除する</button>
    </form>
  </div>
</main>

</div><!-- /.layout -->
</body>
</html>
