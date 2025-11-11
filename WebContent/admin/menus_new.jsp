<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%
  request.setAttribute("pageTitle", "献立作成");
  String ctx = request.getContextPath(); // 例: /sotugyou
%>
<jsp:include page="/header.jsp" />

<main class="content">
  <h2>${year}年${month}月の献立作成</h2>

  <div class="card card-wide">
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px;">
      <!-- 👇 EL式 ${ctx} → スクリプト式 <%= ctx %> に修正 -->
      <a class="button ghost" href="<%= ctx %>/admin/menus_new?ym=${prevYm}">◀ 前の月</a>
      <strong>${year}年${month}月</strong>
      <a class="button ghost" href="<%= ctx %>/admin/menus_new?ym=${nextYm}">次の月 ▶</a>
    </div>

    <table class="simple-table with-border">
      <thead>
        <tr><th>日付</th><th>状態</th><th>操作</th></tr>
      </thead>
      <tbody>
        <%
          java.util.Map<java.time.LocalDate, Boolean> map =
            (java.util.Map<java.time.LocalDate, Boolean>) request.getAttribute("registeredMap");
          java.time.YearMonth ym = java.time.YearMonth.of(
            (Integer)request.getAttribute("year"),
            (Integer)request.getAttribute("month")
          );
          for (int day = 1; day <= ym.lengthOfMonth(); day++) {
            java.time.LocalDate date = ym.atDay(day);
            boolean exists = (map != null && Boolean.TRUE.equals(map.get(date)));
        %>
          <tr>
            <td><%= date %></td>
            <td><%= exists ? "登録済み" : "未登録" %></td>
            <td>
              <% if (exists) { %>
                <a class="button ghost" href="<%= ctx %>/admin/menus_new/select?date=<%= date %>">編集</a>
              <% } else { %>
                <a class="button" href="<%= ctx %>/admin/menus_new/select?date=<%= date %>">追加</a>
              <% } %>
            </td>
          </tr>
        <%
          }
        %>
      </tbody>
    </table>
  </div>
</main>

<jsp:include page="/footer.jsp" />
