<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<jsp:include page="/header.jsp" />

<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<title>アレルギー情報管理</title>
<meta name="viewport" content="width=device-width, initial-scale=1">
<style>
  body { background:#f7e1ca; margin:0; font-family:"Noto Sans JP", sans-serif; }

  h2 {
    text-align:center; margin:16px 0;
    font-size:26px; font-weight:700;
  }

  .container {
    max-width:1000px;
    margin:0 auto;
    background:#f6d9c0;
    padding:20px;
    border-radius:8px;
  }

  .search-box {
    text-align:center;
    margin-bottom:20px;
  }
  .search-box input {
    width:300px;
    padding:8px 10px;
    border-radius:8px;
    border:1px solid #aaa;
  }

  table {
    width:100%;
    border-collapse:collapse;
    background:#c6e6f7;
    border-radius:10px;
    overflow:hidden;
  }
  th, td {
    border-bottom:1px solid #b1d3e0;
    padding:10px 12px;
    text-align:left;
  }
  th {
    background:#b1d3e0;
    font-size:15px;
    font-weight:700;
  }

  .add-area {
    width:100%;
    display:flex;
    justify-content:center;
    margin-top:30px;
  }
  .add-area input {
    padding:10px;
    font-size:14px;
    border:1px solid #999;
    border-radius:8px;
    width: 220px;
    margin-right:5px;
  }
  .btn-add {
    padding:12px 24px;
    border:none;
    background:#fff;
    border:2px solid #888;
    border-radius:8px;
    cursor:pointer;
  }
</style>
</head>

<body>

<h2>アレルギー情報管理</h2>

<div class="container">

  <!-- 検索欄 -->
  <div class="search-box">
    <form method="get" action="${pageContext.request.contextPath}/admin/allergens-master">
      <input type="text" name="q" value="${q}" placeholder="検索したいアレルギーを入力してください">
      <button type="submit">検索</button>
    </form>
  </div>

  <!-- アレルギー一覧 -->
  <table>
    <thead>
      <tr>
        <th>アレルギー名</th>
        <th>カテゴリ</th>
        <th>サブカテゴリ</th>
      </tr>
    </thead>
    <tbody>
  <c:forEach var="a" items="${allergens}">
    <tr>
      <td>${a.nameJa}</td>
      <td>${a.category}</td>
      <td>${a.subcategory}</td>
    </tr>
  </c:forEach>
  <c:if test="${empty allergens}">
    <tr><td colspan="3" style="text-align:center;">データがありません</td></tr>
  </c:if>
</tbody>
  </table>

  <!-- 追加フォーム -->
  <form method="post" action="${pageContext.request.contextPath}/admin/allergens-master">
    <div class="add-area">
      <input type="text" name="name" placeholder="アレルギー名" required>
      <input type="text" name="category" placeholder="カテゴリ" required>
      <input type="text" name="subCategory" placeholder="サブカテゴリ" required>
      <button class="btn-add">追加</button>
    </div>
  </form>

</div>
</body>
</html>
