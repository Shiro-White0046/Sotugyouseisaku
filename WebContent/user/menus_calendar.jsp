<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>


<%
      // <title>タグ用
  request.setAttribute("headerTitle", "献立表示"); // ヘッダー表示用
%>
<jsp:include page="/header_user2.jsp" />


<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <style>
  body {
    background: #f7e1ca;
    margin: 0;
    font-family: sans-serif;
  }

  header {
    background: #f6e7be;
    padding: 16px 20px;
    text-align: center;
  }

  .wrap {
    max-width: 980px;
    margin: 20px auto;
    padding: 0 16px;
  }

  /* ← クラス名を calendar-nav に変更して衝突回避 */
  .calendar-nav {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;
  }

  .cal {
    width: 100%;
    border-collapse: collapse;
    table-layout: fixed;
    background: #fff;
    margin-top: 12px;
  }

  .cal th,
  .cal td {
    border: 1px solid #bbb;
    vertical-align: top;
  }

  .cal th {
    height: 36px;
    background: #f2f2f2;
  }

  .cal td {
    height: 96px;
    position: relative;
    padding: 4px;
  }

  .dow-sun {
    background: #ffe7e7;
  }

  .dow-sat {
    background: #e3f7ff;
  }

  .daynum {
    position: absolute;
    top: 4px;
    right: 6px;
    font-size: 12px;
    color: #333;
  }

  .menu-badge {
    display: inline-block;
    margin-top: 8px;
    background: #e53935;
    color: #fff;
    padding: 4px 8px;
    border-radius: 4px;
    min-width: 36px;
    text-align: center;
  }

  .cell-link {
    display: block;
    width: 100%;
    height: 100%;
    text-decoration: none;
    color: inherit;
  }

  .cell-disabled {
    display: block;
    width: 100%;
    height: 100%;
    color: #999;
    cursor: default;
  }
</style>

</head>
<body>


<div class="wrap">
 <div class="calendar-nav">
  <a href="${pageContext.request.contextPath}/user/menuscalendar?ym=${prevYm}">◀ 前の月</a>
  <strong style="font-size:24px">${year}年${month}月</strong>
  <a href="${pageContext.request.contextPath}/user/menuscalendar?ym=${nextYm}">次の月 ▶</a>
</div>


  <table class="cal" aria-label="${year}年${month}月の献立カレンダー">
    <thead>
      <tr>
        <th class="dow-sun">日</th><th>月</th><th>火</th><th>水</th><th>木</th><th>金</th><th class="dow-sat">土</th>
      </tr>
    </thead>
    <tbody>
    <%
      Integer y = (Integer)request.getAttribute("year");
      Integer m = (Integer)request.getAttribute("month");
      Integer firstDow = (Integer)request.getAttribute("firstDow");
      Integer daysInMonth = (Integer)request.getAttribute("daysInMonth");

      java.util.Map<String, Boolean> hasMenuMap =
        (java.util.Map<String, Boolean>)request.getAttribute("hasMenuMap");

      java.util.Map<String, java.util.List<String>> labelsByDate =
        (java.util.Map<String, java.util.List<String>>)request.getAttribute("labelsByDate");

      String ctx = request.getContextPath();

      int day = 1;
      for (int week = 0; week < 6 && day <= daysInMonth; week++) {
        out.write("<tr>");
        for (int dow = 0; dow < 7; dow++) {
          if ((week == 0 && dow < firstDow) || day > daysInMonth) { out.write("<td></td>"); continue; }

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

          String tdClass = (dow == 0 ? " class='dow-sun'" : (dow == 6 ? " class='dow-sat'" : ""));
          out.write("<td" + tdClass + ">");

          if (has) {
            out.write("<a class='cell-link' href='" + ctx + "/user/menu_detail?date=" + key + "'>");
            out.write("<span class='daynum'>" + day + "</span>");
            if (labs != null && !labs.isEmpty()) {
              for (String s : labs) out.write("<div class='menu-badge'>" + s + "</div>");
            } else {
              out.write("<div class='menu-badge'>献立あり</div>");
            }
            out.write("</a>");
          } else {
            out.write("<div class='cell-disabled'>");
            out.write("<span class='daynum'>" + day + "</span>");
            out.write("</div>");
          }

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