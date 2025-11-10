<%@ page contentType="text/html; charset=UTF-8" %>
<%
  request.setAttribute("pageTitle", "献立作成（新規）");
  java.time.LocalDate date = (java.time.LocalDate) request.getAttribute("date");
  String dateStr = (date != null) ? date.toString() : "";
%>
<jsp:include page="/header.jsp" />

<main class="content">
  <h2>献立作成（新規）</h2>

  <div class="card card-narrow">
    <% if (request.getAttribute("error") != null) { %>
      <div class="alert error"><%= request.getAttribute("error") %></div>
    <% } %>

    <form method="post" action="<%=request.getContextPath()%>/admin/menus/new">
      <div class="form-row">
        <label>対象日</label>
        <input type="text" name="date" value="<%= dateStr %>" readonly>
      </div>

      <hr style="margin:16px 0;">

      <h3>朝食</h3>
      <label>メニュー名</label>
      <input type="text" name="breakfastName" maxlength="100">
      <label>説明（任意）</label>
      <input type="text" name="breakfastDesc" maxlength="200">

      <h3>昼食</h3>
      <label>メニュー名</label>
      <input type="text" name="lunchName" maxlength="100">
      <label>説明（任意）</label>
      <input type="text" name="lunchDesc" maxlength="200">

      <h3>夕食</h3>
      <label>メニュー名</label>
      <input type="text" name="dinnerName" maxlength="100">
      <label>説明（任意）</label>
      <input type="text" name="dinnerDesc" maxlength="200">

      <div class="actions">
        <a class="button ghost" href="<%=request.getContextPath()%>/admin/menus?ym=<%= dateStr.length()>=7? dateStr.substring(0,7):"" %>">一覧へ戻る</a>
        <button class="button" type="submit">保存して一覧へ</button>
      </div>
    </form>
  </div>
</main>

<jsp:include page="/footer.jsp" />
