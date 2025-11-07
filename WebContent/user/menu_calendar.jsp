<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html><html lang="ja"><head>
<meta charset="UTF-8"><title>献立</title><meta name="viewport" content="width=device-width, initial-scale=1">
<style>
  body{background:#f7e1ca;margin:0;font-family:sans-serif}
  header{background:#f6e7be;padding:16px 20px;text-align:center}
  .wrap{max-width:980px;margin:20px auto;padding:0 16px}
  .cal{width:100%;border-collapse:collapse;table-layout:fixed;background:#fff;margin-top:12px}
  .cal th,.cal td{border:1px solid #bbb;vertical-align:top}
  .cal th{height:36px;background:#f2f2f2}
  .cal td{height:96px;position:relative;padding:4px}
  .dow-sun{background:#ffe7e7}.dow-sat{background:#e3f7ff}
  .daynum{position:absolute;top:4px;right:6px;font-size:12px;color:#333}
  .menu-badge{display:inline-block;margin-top:8px;background:#e53935;color:#fff;
              padding:4px 8px;border-radius:4px;min-width:36px;text-align:center}
  .cell-link{display:block;width:100%;height:100%;text-decoration:none;color:inherit}
  .nav{display:flex;gap:10px;justify-content:center;align-items:center;margin-top:8px}
  .nav a{padding:6px 10px;border:1px solid #bbb;border-radius:6px;background:#fff;text-decoration:none}
</style>
</head><body>
<header><h2 style="margin:0">献立</h2></header>
<div class="wrap">
  <div class="nav">
    <a href="${pageContext.request.contextPath}/user/menus?ym=${prevYm}">◀ 前の月</a>
    <strong style="font-size:24px">${year}年${month}月</strong>
    <a href="${pageContext.request.contextPath}/user/menus?ym=${nextYm}">次の月 ▶</a>
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
      Integer days = (Integer)request.getAttribute("daysInMonth");
      java.util.Map<String, java.util.List<String>> labels =
        (java.util.Map<String, java.util.List<String>>)request.getAttribute("labelsByDate");
      String ctx = request.getContextPath();

      int day=1;
      for(int week=0; week<6 && day<=days; week++){
        out.write("<tr>");
        for(int dow=0; dow<7; dow++){
          if((week==0 && dow<firstDow) || day>days){ out.write("<td></td>"); continue; }
          java.time.LocalDate d = java.time.LocalDate.of(y, m, day);
          String key = d.toString();
          java.util.List<String> labs = (labels==null)?null:labels.get(key);
          boolean has = labs!=null && !labs.isEmpty();
          String tdClass = (dow==0?" class='dow-sun'":(dow==6?" class='dow-sat'":""));
          out.write("<td"+tdClass+">");
          out.write("<a class='cell-link' href='"+ctx+"/user/menus/detail?date="+key+"'>");
          out.write("<span class='daynum'>"+day+"</span>");
          if(has){
            for(String s: labs){
              out.write("<div class='menu-badge'>" + s + "</div>");  // ← commons 未使用
            }
          }
          out.write("</a></td>");
          day++;
        }
        out.write("</tr>");
      }
    %>
    </tbody>
  </table>
</div>
</body></html>