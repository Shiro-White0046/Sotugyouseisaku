<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%
  request.setAttribute("pageTitle", "対応食管理");
  String ctx = request.getContextPath();
%>

<jsp:include page="/header.jsp" />



<style>
/* ===== 全体レイアウト ===== */
.support-main {
  background: #fde9c9;
  min-height: calc(100vh - 70px);
  padding: 24px 32px 40px;
  box-sizing: border-box;
}

.support-wrapper {
  max-width: 980px;
  margin: 0 auto;
  padding: 24px 32px 32px;
  background: #f9e1c1;
  border-radius: 8px;
  border: 1px solid #d7b88f;
}

/* タイトル */
.support-title-main {
  text-align: center;
  font-size: 24px;
  font-weight: 800;
  margin-bottom: 24px;
}

/* ===== 食事タブ ===== */
.meal-tabs {
  text-align: left;
  margin-bottom: 16px;
}
.meal-tab {
  display: inline-block;
  min-width: 110px;
  padding: 8px 16px;
  margin-right: 8px;
  border-radius: 6px;
  border: 1px solid #d1b37e;
  background: #ffffff;
  text-align: center;
  text-decoration: none;
  color: #333;
  font-weight: 700;
}
.meal-tab.is-active {
  background: #fffbeb;
  border-color: #f97316;
  color: #b45309;
}

/* ===== 中央タイトル ===== */
.support-subtitle-row {
  display: flex;
  justify-content: center;
  margin: 8px 0 24px;
}
.support-subtitle {
  font-size: 20px;
  font-weight: 700;
}

/* ===== 左：人数 + 右：表 ===== */
.support-body {
  display: flex;
  align-items: flex-start;
  gap: 32px;
}

/* 左側 */
.support-left {
  width: 220px;
  text-align: center;
}
.support-count-box {
  width: 160px;
  height: 160px;
  background: #fff;
  border-radius: 16px;
  border: 2px solid #000;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
  font-weight: 800;
}

/* 右側 */
.support-right {
  flex: 1;
}

/* テーブルスクロール枠 */
.support-table-wrapper {
  max-height: 280px;
  overflow-y: auto;
  border-radius: 8px;
  border: 1px solid #c1c1c1;
}

/* テーブル本体 */
.support-table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
  font-size: 14px;
}

/* ヘッダ */
.support-table thead th {
  background: #7fb3e8;
  color: white;
  padding: 6px 8px;
  border-right: 1px solid #fff;
  border-bottom: 1px solid #5a8bc0;
  white-space: nowrap;
}

/* 名前列（潰れ対策） */
.support-table .col-name {
  width: 150px !important;
  min-width: 150px;
  background: #93c94f;
}

/* 名前セル（潰れ対策） */
.support-table .name-cell {
  min-width: 150px;
  text-align: left;
  font-weight: 700;
  padding-left: 10px;
}

/* 行の背景 */
.support-table tbody tr:nth-child(odd) td { background: #c2e08a; }
.support-table tbody tr:nth-child(even) td { background: #b0d676; }

/* セル */
.support-table td {
  padding: 6px 8px;
  border-right: 1px solid #fff;
  border-bottom: 1px solid #fff;
  text-align: center;
}

/* ===== 戻るボタン ===== */
.support-footer {
  margin-top: 24px;
  text-align: right;
}
.back-button {
  display: inline-block;
  padding: 10px 20px;
  border-radius: 6px;
  border: 1px solid #d1b37e;
  background: #fff;
  font-weight: 700;
}
</style>

<main class="support-main">
  <div class="support-wrapper">

    <!-- 食事タブ -->
    <div class="meal-tabs">
      <a href="<%= ctx %>/admin/support-meals?mealType=breakfast"
         class="meal-tab ${mealType eq 'breakfast' ? 'is-active' : ''}">朝飯</a>
      <a href="<%= ctx %>/admin/support-meals?mealType=lunch"
         class="meal-tab ${mealType eq 'lunch' ? 'is-active' : ''}">昼飯</a>
      <a href="<%= ctx %>/admin/support-meals?mealType=dinner"
         class="meal-tab ${mealType eq 'dinner' ? 'is-active' : ''}">夜飯</a>
    </div>

    <!-- 見出し -->
    <div class="support-title-main">今日の対応食</div>

    <div class="support-subtitle-row">
      <div class="support-subtitle">
        <c:out value="${mealLabel}" /> の対応食管理表
      </div>
    </div>

    <div class="support-body">

      <!-- 左：人数 -->
      <div class="support-left">
        <div class="support-left-label">対応食が必要な人数</div>
        <div class="support-count-box">
          <c:out value="${needCount}" />人
        </div>
      </div>

      <!-- 右：テーブル（items があるときだけ表示） -->
      <div class="support-right">

        <!-- 献立がある場合のみ表示 -->
        <c:if test="${not empty items}">
          <div class="support-table-wrapper">
            <table class="support-table">
              <thead>
                <tr>
                  <th class="col-name">名前</th>
                  <c:forEach var="item" items="${items}">
                    <th>${item.name}</th>
                  </c:forEach>
                </tr>
              </thead>

              <tbody>
                <c:forEach var="ind" items="${individuals}">
  <tr>
    <td class="name-cell">
      <c:out value="${ind.displayName}" />
    </td>

    <c:forEach var="it" items="${items}">
      <td>

        <!-- ★ 安全にキーを生成（EL の + を使わない） -->
        <c:set var="key" value="${ind.id}-${it.id}" />

        <!-- ★ supportMap 参照 -->
        <c:if test="${supportMap[key]}">○</c:if>

      </td>
    </c:forEach>
  </tr>
</c:forEach>
              </tbody>
            </table>
          </div>
        </c:if>

      </div>
    </div>

    <div class="support-footer">
      <a href="<%= ctx %>/admin/home" class="back-button">戻る</a>
    </div>

  </div>
</main>
