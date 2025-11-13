<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<title>利用者アレルギー一覧</title>
<meta name="viewport" content="width=device-width, initial-scale=1">
<style>
  body{background:#f7e1ca;margin:0;font-family:sans-serif}
  header{background:#f6e7be;padding:14px 16px;text-align:center;font-weight:800}
  .wrap{max-width:1100px;margin:16px auto;padding:0 16px}
  .search{
    display:grid;grid-template-columns:1fr 1fr auto;gap:10px;
    background:#fff;border:1px solid #e6d3bc;border-radius:10px;padding:12px
  }
  .search input{width:100%;padding:10px;border:1px solid #ddd;border-radius:8px}
  .search button{padding:10px 18px;border:1px solid #c9b391;border-radius:8px;background:#f6e7be;cursor:pointer}

  table{width:100%;border-collapse:collapse;margin-top:14px;background:#fff;border:1px solid #e6d3bc;border-radius:10px;overflow:hidden}
  th,td{padding:10px;border-bottom:1px solid #f0e5d6;vertical-align:top}
  th{background:#faf2e3;text-align:left;white-space:nowrap}
  tbody tr:nth-child(odd){background:#fffdf8}
  .muted{color:#777}
</style>
</head>
<body>

<header>利用者アレルギー一覧</header>
<div class="wrap">

  <!-- 検索：名前 / アレルギー名 -->
  <form class="search" method="get" action="${pageContext.request.contextPath}/admin/allergies">
    <input type="text" name="q"  value="${q}"  placeholder="名前で検索">
    <input type="text" name="aq" value="${aq}" placeholder="アレルギー名で検索（例：卵・乳・そば...）">
    <button type="submit">検索</button>
  </form>

  <table>
    <thead>
      <tr>
        <th>名前</th>
        <th>食物性アレルギー</th>
        <th>食べられないもの</th>
        <th>接触性アレルギー</th>
      </tr>
    </thead>
    <tbody>
      <c:forEach var="r" items="${rows}">
        <tr>
          <td><strong><c:out value="${r.displayName}"/></strong></td>
          <td><c:out value="${empty r.foods ? '—' : r.foods}"/></td>
          <td><c:out value="${empty r.avoids ? '—' : r.avoids}"/></td>
          <td><c:out value="${empty r.contacts ? '—' : r.contacts}"/></td>
        </tr>
      </c:forEach>
      <c:if test="${empty rows}">
        <tr><td colspan="4" class="muted">該当する利用者が見つかりませんでした。</td></tr>
      </c:if>
    </tbody>
  </table>

</div>
</body>
</html>