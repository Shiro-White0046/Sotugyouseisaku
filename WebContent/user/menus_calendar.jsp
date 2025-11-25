<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%
  request.setAttribute("headerTitle", "献立表示");
%>
<jsp:include page="/header_user.jsp" />

<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>献立表示</title>
<style>
  :root{
    --bg:#f7e1ca; --panel:#fff; --line:#d8c8b5; --th:#f4efe6;
    --sun:#ffe7e7; --sat:#e8f5ff; --chip:#e53935;
  }
  body{ background:var(--bg); margin:0; font-family:sans-serif; }
  .wrap{ max-width:1100px; margin:16px auto 28px; padding:0 16px; }

  /* 上部バー */
  .topbar{ display:flex; align-items:center; gap:12px; margin:6px 0 10px; }
  .child-select{ padding:8px 12px; border:1px solid var(--line); border-radius:8px; }
  .subtle{ color:#666; }

  /* 前月/次月 */
  .calendar-nav{ display:flex; justify-content:space-between; align-items:center; margin:2px 0 10px; }
  .month-title{ font-size:26px; font-weight:800; letter-spacing:.02em; }

  /* カレンダー本体 */
  table.cal{ width:100%; table-layout:fixed; border-collapse:collapse; background:var(--panel); border:1px solid var(--line); }
  .cal thead th{ background:var(--th); border-bottom:1px solid var(--line); padding:10px 0; font-weight:700; }
  .cal td{ border-right:1px solid var(--line); border-bottom:1px solid var(--line); padding:0; vertical-align:top; }
  .cal tr:last-child td{ border-bottom:1px solid var(--line); }
  .cal td:last-child, .cal thead th:last-child{ border-right:0; }

  .dow-sun{ background:var(--sun); }
  .dow-sat{ background:var(--sat); }

  /* 内側ラッパ：ここが“セルの高さ” */
  .cell{ position:relative; height:130px; }
  /* セル全面リンク */
  .cell-link{ display:block; height:100%; width:100%; text-decoration:none; color:inherit; padding:6px; }
  .cell-link.no-badge:hover{ background:rgba(0,0,0,.03); }

  .daynum{ position:absolute; top:6px; right:8px; font-size:12px; color:#333; }

  .badge{ display:inline-block; margin:6px 6px 0 6px; background:var(--chip); color:#fff;
          padding:4px 8px; border-radius:14px; font-size:12px; line-height:1; }

  /* 週の背景（日曜/土曜列だけ薄く） */
  .cal tbody td.dow-0{ background:var(--sun); }
  .cal tbody td.dow-6{ background:var(--sat); }

  /* スマホで高さを少し詰める */
  @media (max-width:560px){
    .cell{ height:110px; }
    .month-title{ font-size:22px; }
  }
</style>
</head>
<body>
<div class="wrap">

  <!-- 子ども切替 -->
  <form method="get" action="${pageContext.request.contextPath}/user/menuscalendar" class="topbar">
    <label>対象：</label>
    <select name="personId" class="child-select" onchange="this.form.submit()">
      <c:forEach var="c" items="${children}">
        <option value="${c.id}" ${c.id == personId ? 'selected' : ''}>${c.displayName}</option>
      </c:forEach>
    </select>
    <input type="hidden" name="ym" value="${param.ym}" />
    <span class="subtle"></span>
  </form>

  <!-- 月移動 + タイトル -->
  <div class="calendar-nav">
    <a href="${pageContext.request.contextPath}/user/menuscalendar?ym=${prevYm}&personId=${personId}">◀ 前の月</a>
    <div class="month-title">${year}年${month}月</div>
    <a href="${pageContext.request.contextPath}/user/menuscalendar?ym=${nextYm}&personId=${personId}">次の月 ▶</a>
  </div>

  <table class="cal" aria-label="${year}年${month}月の献立カレンダー">
    <thead>
      <tr>
        <th class="dow-sun">日</th>
        <th>月</th><th>火</th><th>水</th><th>木</th><th>金</th>
        <th class="dow-sat">土</th>
      </tr>
    </thead>
    <tbody>
    <%
      Integer y  = (Integer)request.getAttribute("year");
      Integer m  = (Integer)request.getAttribute("month");
      Integer fd = (Integer)request.getAttribute("firstDow");
      Integer dim= (Integer)request.getAttribute("daysInMonth");

      java.util.Map<String, Boolean> hasMenuMap =
        (java.util.Map<String, Boolean>)request.getAttribute("hasMenuMap");

      java.util.Map<String, java.util.List<String>> labelsByDate =
        (java.util.Map<String, java.util.List<String>>)request.getAttribute("labelsByDate");

      String ctx  = request.getContextPath();
      String pid  = String.valueOf(request.getAttribute("personId"));

      int day = 1;
      for (int week = 0; week < 6 && day <= dim; week++) {
        out.write("<tr>");
        for (int dow = 0; dow < 7; dow++) {

          if ((week == 0 && dow < fd) || day > dim) {
            out.write("<td class='dow-" + dow + "'><div class=\"cell\"></div></td>");
            continue;
          }

          java.time.LocalDate d = java.time.LocalDate.of(y, m, day);
          String key = d.toString();

          boolean has = false;
          java.util.List<String> labs = null;

          if (labelsByDate != null) {
            labs = labelsByDate.get(key);
            has = (labs != null && !labs.isEmpty());
          } else if (hasMenuMap != null) {
            Boolean b = hasMenuMap.get(key);
            has = (b != null && b.booleanValue());
          }

          out.write("<td class='dow-" + dow + "'>");
          out.write("<div class='cell'>");

          String href = ctx + "/user/menu_detail?date=" + key + "&personId=" + pid;
          if (has) {
            out.write("<a class='cell-link' href='" + href + "' aria-label='" + key + " の献立詳細'>");
            out.write("<span class='daynum'>" + day + "</span>");
            for (String s : (labs == null ? java.util.Collections.<String>emptyList() : labs)) {
              out.write("<span class='badge'>" + s + "</span>");
            }
            out.write("</a>");
          } else {
            out.write("<a class='cell-link no-badge' href='" + href + "' aria-label='" + key + " の献立詳細'>");
            out.write("<span class='daynum'>" + day + "</span>");
            out.write("</a>");
          }

          out.write("</div>");
          out.write("</td>");
          day++;
        }
        out.write("</tr>");
      }
    %>
    </tbody>
  </table>
</div>
</body>
</html>
