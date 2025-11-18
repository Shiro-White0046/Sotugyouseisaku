<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%
  request.setAttribute("headerTitle", "献立表示");
%>
<jsp:include page="/header_user2.jsp"/>

<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<title>献立カレンダー</title>
<meta name="viewport" content="width=device-width, initial-scale=1">
<style>
  body{ background:#f7e1ca; margin:0; font-family:sans-serif; }
  .wrap{ max-width:980px; margin:16px auto 28px; padding:0 16px; }

  .switcher{ display:flex; gap:8px; align-items:center; margin:8px 0 6px; }
  .switcher select{ padding:6px 8px; }

  .calendar-nav{ display:flex; justify-content:space-between; align-items:center; margin-top:6px; }
  .calendar-nav a{ text-decoration:underline; color:#1a73e8; }

  .cal{ width:100%; border-collapse:collapse; table-layout:fixed; background:#fff; margin-top:10px; }
  .cal th,.cal td{ border:1px solid #cfcfcf; vertical-align:top; }
  .cal th{ height:36px; background:#f2f2f2; }
  .cal td{ height:96px; position:relative; padding:0; }
  .dow-sun{ background:#ffe7e7; } .dow-sat{ background:#e7f3ff; }

  .cell-link{ display:block; width:100%; height:100%; padding:4px; text-decoration:none; color:inherit; position:relative; }
  .cell-link:hover{ background:rgba(0,0,0,.03); }
  .daynum{ position:absolute; top:4px; right:6px; font-size:12px; color:#333; }

  .menu-badge{ display:inline-block; margin:8px 8px 0 8px; background:#e53935; color:#fff;
               padding:4px 8px; border-radius:999px; font-size:12px; font-weight:700; }
  .no-badge .menu-badge{ display:none; } /* 念のため */

  .month-title{ font-weight:800; font-size:22px; }
</style>
</head>
<body>
<div class="wrap">

  <!-- 子ども切替 -->
  <form method="get" action="${pageContext.request.contextPath}/user/menuscalendar" class="switcher">
    <label>子ども：</label>
    <select name="personId" onchange="this.form.submit()">
      <c:forEach var="c" items="${children}">
        <option value="${c.id}" ${c.id == personId ? 'selected' : ''}>${c.displayName}</option>
      </c:forEach>
    </select>
    <input type="hidden" name="ym" value="${param.ym}"/>
    <span style="color:#555;">（表示中：<c:out value="${selectedChild != null ? selectedChild.displayName : '—'}"/>）</span>
  </form>

  <div class="calendar-nav">
    <a href="${pageContext.request.contextPath}/user/menuscalendar?ym=${prevYm}&personId=${personId}">◀ 前の月</a>
    <div class="month-title">${year}年${month}月</div>
    <a href="${pageContext.request.contextPath}/user/menuscalendar?ym=${nextYm}&personId=${personId}">次の月 ▶</a>
  </div>

  <table class="cal" aria-label="${year}年${month}月の献立カレンダー">
    <thead>
      <tr>
        <th class="dow-sun">日</th><th>月</th><th>火</th><th>水</th>
        <th>木</th><th>金</th><th class="dow-sat">土</th>
      </tr>
    </thead>
    <tbody>
    <%
      Integer y  = (Integer)request.getAttribute("year");
      Integer m  = (Integer)request.getAttribute("month");
      Integer fd = (Integer)request.getAttribute("firstDow");
      Integer dim= (Integer)request.getAttribute("daysInMonth");

      Map<String,Boolean> hasMenuMap =
        (Map<String,Boolean>)request.getAttribute("hasMenuMap");
      Map<String,java.util.List<String>> labelsByDate =
        (Map<String,java.util.List<String>>)request.getAttribute("labelsByDate");

      String ctx = request.getContextPath();
      String pid = String.valueOf(request.getAttribute("personId"));

      int day = 1;
      for (int week=0; week<6 && day<=dim; week++) {
        out.write("<tr>");
        for (int dow=0; dow<7; dow++) {
          if ((week==0 && dow<fd) || day>dim) { out.write("<td></td>"); continue; }

          java.time.LocalDate d = java.time.LocalDate.of(y, m, day);
          String key = d.toString();

          java.util.List<String> labs = (labelsByDate!=null) ? labelsByDate.get(key) : null;
          boolean has = (hasMenuMap!=null && Boolean.TRUE.equals(hasMenuMap.get(key)));

          String tdClass = (dow==0 ? " class='dow-sun'" : (dow==6 ? " class='dow-sat'" : ""));
          out.write("<td"+tdClass+">");

          // セル全面リンク（バッジ無しでも常に遷移可能）
          out.write("<a class='cell-link"+((labs==null||labs.isEmpty())?" no-badge":"")+"' href='"
              + ctx + "/user/menu_detail?date=" + key + "&personId=" + pid + "'>");

          out.write("<span class='daynum'>" + day + "</span>");
          if (labs != null && !labs.isEmpty()) {
            for (String s : labs) {
              out.write("<div class='menu-badge'>" + s + "</div>");
            }
          } else if (has) {
            // 献立はあるが一致アレルゲンが無い日 → 小さく“献立あり”目印
            out.write("<div class='menu-badge' style='opacity:.75'>献立あり</div>");
          }
          out.write("</a>");

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
