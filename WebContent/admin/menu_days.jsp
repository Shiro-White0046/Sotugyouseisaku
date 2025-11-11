<%@ page contentType="text/html; charset=UTF-8" %>
<%
  request.setAttribute("pageTitle", "献立（日付一覧）");
%>
<jsp:include page="/header.jsp" />

<%
  int year = (request.getAttribute("year") != null) ? (Integer)request.getAttribute("year") : java.time.YearMonth.now().getYear();
  int month = (request.getAttribute("month") != null) ? (Integer)request.getAttribute("month") : java.time.YearMonth.now().getMonthValue();
  String prevYm = (String) request.getAttribute("prevYm");
  String nextYm = (String) request.getAttribute("nextYm");
  java.util.Map<java.time.LocalDate, Boolean> reg =
      (java.util.Map<java.time.LocalDate, Boolean>) request.getAttribute("registeredMap");

  java.time.YearMonth ym = java.time.YearMonth.of(year, month);
  java.time.LocalDate first = ym.atDay(1);
  int days = ym.lengthOfMonth();
%>

<main class="content">
  <h2>献立（日付一覧）</h2>

  <div class="card card-narrow" style="text-align:center;">
    <a class="button ghost" href="<%=request.getContextPath()%>/admin/menus?ym=<%= prevYm %>">◀ 前の月</a>
    <strong style="margin:0 12px;font-size:18px;"><%= year %>年<%= month %>月</strong>
    <a class="button ghost" href="<%=request.getContextPath()%>/admin/menus?ym=<%= nextYm %>">次の月 ▶</a>
  </div>

  <div class="card card-wide">
    <table class="simple-table with-border" style="width:100%;">
      <thead>
        <tr>
          <th style="width:140px;">日付</th>
          <th>状態</th>
          <th style="width:220px;">操作</th>
        </tr>
      </thead>
      <tbody>
      <%
        for (int d=1; d<=days; d++) {
          java.time.LocalDate date = ym.atDay(d);
          boolean exists = reg != null && Boolean.TRUE.equals(reg.get(date));
      %>
        <tr>
          <td><%= date.toString() %></td>
          <td><%= exists ? "登録済み" : "未登録" %></td>
          <td>
            <% if (exists) { %>
              <a class="button" href="<%=request.getContextPath()%>/admin/menus/edit?date=<%= date %>">編集</a>
              <a class="button ghost" href="<%=request.getContextPath()%>/admin/menus/image?date=<%= date %>">画像</a>
            <% } else { %>
              <a class="button new-btn" href="<%=request.getContextPath()%>/admin/menus/new?date=<%= date %>">追加</a>
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
