<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="bean.User, java.util.List, bean.Individual" %>
<jsp:include page="/header.jsp"/>

<%
  String ctx = request.getContextPath();
  User u = (User) request.getAttribute("user");
  List<Individual> inds = (List<Individual>) request.getAttribute("individuals");
%>

<main class="content">
  <h2>アカウント詳細</h2>

  <% if (request.getAttribute("flash") != null) { %>
    <div class="alert" style="background:#ecfeff;border:1px solid #67e8f9;color:#155e75;border-radius:6px;padding:10px 12px;">
      <%= request.getAttribute("flash") %>
    </div>
  <% } %>

  <div class="card" style="max-width:760px;margin:0 auto 16px;">
    <table class="simple-table" style="width:100%;border-collapse:collapse;">
      <tr><th style="text-align:left;width:220px;">ログインID</th><td><%= u.getLoginId() %></td></tr>
      <tr><th style="text-align:left;">名前</th><td><%= u.getName() %></td></tr>
      <tr><th style="text-align:left;">メールアドレス</th><td><%= (u.getEmail()!=null?u.getEmail():"（未設定）") %></td></tr>
      <tr><th style="text-align:left;">携帯番号</th><td><%= (u.getPhone()!=null?u.getPhone():"（未設定）") %></td></tr>
      <tr><th style="text-align:left;">アカウントタイプ</th><td><%= u.getAccountType() %></td></tr>
      <tr><th style="text-align:left;">状態</th><td><%= (u.isActive()?"有効":"無効") %></td></tr>
      <tr><th style="text-align:left;">作成日時</th><td><%= (u.getCreatedAt()!=null?u.getCreatedAt():"") %></td></tr>
    </table>
    <div class="actions" style="display:flex;justify-content:space-between;gap:12px;margin-top:12px;">
      <a class="button ghost" href="<%= ctx %>/admin/users">一覧へ戻る</a>
      <a class="button" href="<%= ctx %>/admin/accounts/edit?userId=<%= u.getId() %>">編集</a>
    </div>
  </div>

  <div class="card" style="max-width:760px;margin:0 auto;">
    <h3 style="margin-top:0;">紐づく個人（individuals）</h3>
    <table class="simple-table" style="width:100%;border-collapse:collapse;">
      <thead>
        <tr><th>氏名</th><th>生年月日</th><th>備考</th></tr>
      </thead>
      <tbody>
      <%
        if (inds == null || inds.isEmpty()) {
      %>
        <tr><td colspan="3" style="text-align:center;color:#777;">紐づく個人はまだ登録されていません。</td></tr>
      <%
        } else {
          for (Individual i : inds) {
      %>
        <tr>
          <td><%= i.getDisplayName() %></td>
          <td><%= (i.getBirthday()!=null? i.getBirthday() : "") %></td>
          <td><%= (i.getNote()!=null? i.getNote() : "") %></td>
        </tr>
      <%
          }
        }
      %>
      </tbody>
    </table>
  </div>
</main>

</div><!-- /.layout -->
</body>
</html>
