<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="bean.Menu" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<title>献立（詳細）</title>
<meta name="viewport" content="width=device-width, initial-scale=1">

<style>
  body{background:#f7e1ca;margin:0;font-family:sans-serif}
  /* ヘッダーは include 側に任せる */

  .wrap{max-width:1100px;margin:16px auto;padding:0 16px}
  .title{font-size:18px;margin:8px 0 12px 0;color:#333}

  .grid{display:grid;grid-template-columns: 2.1fr 1fr;gap:16px;align-items:start}
  /* 左：大きい写真/説明枠 */
  .left-card{background:#fff;border:2px solid #333;min-height:340px;padding:16px}
  .menu-img{max-width:100%;height:auto;display:block;border:1px solid #ccc}
  .desc{margin-top:8px;white-space:pre-wrap;color:#333}

  /* 右：情報欄 */
  .right-card{background:#fff;border:2px solid #333;min-height:340px;padding:14px}
  .right-card h4{margin:0 0 8px 0}
  .label{font-size:12px;color:#666;margin-bottom:4px}
  .name{font-size:18px;font-weight:600;margin-bottom:12px}
  .msg{margin:10px 0 6px 0;color:#333}

  .chips{display:flex;flex-wrap:wrap;gap:8px}
  .chip{background:#e53935;color:#fff;padding:4px 10px;border-radius:16px}

  /* 下部：戻る */
  .footer{margin-top:18px}
  .btn-back{border:2px solid #333;background:#fff;padding:10px 18px;border-radius:8px;cursor:pointer}
</style>
</head>
<body>

<%
  // ヘッダー（指定の include を使用）
  request.setAttribute("headerTitle", "献立詳細"); // 表示名は必要に応じて変えてOK
%>
<jsp:include page="/header_user2.jsp" />

<%
  // ---- 表示用のタイトル（日付の「10月10日のメニュー」など）と戻り先 ym を作成 ----
  Menu menu = (Menu) request.getAttribute("menu");
  String headTitle = "この日のメニュー";
  String ym = "";
  if (menu != null && menu.getMenuDate() != null) {
    java.time.LocalDate d = menu.getMenuDate();
    headTitle = d.getMonthValue() + "月" + d.getDayOfMonth() + "日のメニュー";
    ym = String.format("%d-%02d", d.getYear(), d.getMonthValue()); // 例: 2025-10
  }
%>

<div class="wrap">
  <!-- ① タイトル -->
  <div class="title"><%= headTitle %></div>

  <div class="grid">
    <!-- ② 左：写真＋説明 -->
    <section class="left-card">
      <c:choose>
        <c:when test="${not empty menu.imagePath}">
          <img class="menu-img" src="${pageContext.request.contextPath}${menu.imagePath}" alt="献立画像">
        </c:when>
        <c:otherwise>
          <!-- 画像なしプレースホルダ -->
          <div style="height:260px;display:flex;align-items:center;justify-content:center;color:#666;">
            写真
          </div>
        </c:otherwise>
      </c:choose>

      <c:if test="${not empty menu.description}">
        <div class="desc">${menu.description}</div>
      </c:if>
    </section>

    <!-- ③④⑤⑥ 右：メニュー名＋含有アレルゲン -->
    <aside class="right-card">
      <div class="label">メニュー名</div>       <!-- ③ -->
      <div class="name">${menu.name}</div>       <!-- ④ -->
      <div class="msg">このメニューには以下のものが含まれています！</div> <!-- ⑤ -->

      <div class="chips">
        <c:forEach var="a" items="${allergens}">
          <span class="chip">${a.nameJa}</span>  <!-- ⑥ -->
        </c:forEach>
        <c:if test="${empty allergens}">
          <span style="color:#666">登録なし</span>
        </c:if>
      </div>
    </aside>
  </div>

  <!-- ⑦ 戻る（カレンダーへ） -->
  <div class="footer">
    <button type="button" class="btn-back"
            onclick="location.href='${pageContext.request.contextPath}/user/menus?ym=<%= ym %>'">戻る</button>
  </div>
</div>

</body>
</html>