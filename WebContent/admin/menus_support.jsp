<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%
  // ★ 前提：サーブレット側でセットしておく属性
  // request.setAttribute("mealType", "breakfast" / "lunch" / "dinner");
  // request.setAttribute("mealLabel", "朝飯" / "昼飯" / "夜飯");
  // request.setAttribute("items", List<MenuItem>);          // 横軸（メニュー）
  // request.setAttribute("individuals", List<Individual>);  // 縦軸（名前）
  // request.setAttribute("supportMap", java.util.Map<String,Boolean>);
  //   キーは "personId-itemId" の文字列
  // request.setAttribute("needCount", java.lang.Long);      // 対応食が必要な人数

  request.setAttribute("pageTitle", "対応食管理");
%>

<jsp:include page="/header.jsp" />

<%
  String ctx = request.getContextPath();
%>

<style>
/* ===== 全体レイアウト ===== */
.support-main {
  background: #fde9c9;
  min-height: calc(100vh - 70px); /* header分をざっくり引く */
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
  box-sizing: border-box;
}

/* タイトル */
.support-title-main {
  text-align: center;
  font-size: 24px;
  font-weight: 800;
  margin-bottom: 24px;
}

/* ===== 上部：食事タブ ===== */
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
  box-shadow: 0 1px 0 rgba(0,0,0,0.05);
}

.meal-tab.is-active {
  background: #fffbeb;
  border-color: #f97316;
  color: #b45309;
}

/* ===== 中央タイトル ===== */
.support-subtitle-row {
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 8px 0 24px;
}

.support-subtitle {
  font-size: 20px;
  font-weight: 700;
}

/* ===== 左：人数表示 / 右：表 ===== */
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

.support-left-label {
  font-size: 16px;
  margin-bottom: 8px;
}

.support-count-box {
  width: 160px;
  height: 160px;
  margin: 0 auto;
  background: #ffffff;
  border-radius: 16px;
  border: 2px solid #000000;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
  font-weight: 800;
}

/* 右側（テーブル） */
.support-right {
  flex: 1;
}

/* スクロールエリア */
.support-table-wrapper {
  max-height: 280px;     /* 縦に増えたときスクロール */
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

/* ヘッダ行 */
.support-table thead th {
  background: #7fb3e8;
  color: #ffffff;
  padding: 6px 8px;
  border-right: 1px solid #ffffff;
  border-bottom: 1px solid #5a8bc0;
  text-align: center;
  font-weight: 700;
  white-space: nowrap;
}

/* 名前列 */
.support-table .col-name {
  width: 120px;
  background: #93c94f;
}

/* 行の背景 */
.support-table tbody tr:nth-child(odd) td {
  background: #c2e08a;
}
.support-table tbody tr:nth-child(even) td {
  background: #b0d676;
}

/* セル */
.support-table td {
  padding: 6px 8px;
  border-right: 1px solid #ffffff;
  border-bottom: 1px solid #ffffff;
  text-align: center;
  vertical-align: middle;
}

/* 名前セル */
.support-table .name-cell {
  text-align: left;
  font-weight: 700;
  padding-left: 10px;
}

/* ○ の見た目（そのまま文字でもOK。必要なら丸枠風に） */
.circle-mark {
  font-size: 18px;
}

/* ===== 下部：戻るボタン ===== */
.support-footer {
  margin-top: 24px;
  text-align: right;
}

.back-button {
  display: inline-block;
  min-width: 140px;
  padding: 10px 20px;
  border-radius: 6px;
  border: 1px solid #d1b37e;
  background: #ffffff;
  text-align: center;
  text-decoration: none;
  color: #333;
  font-weight: 700;
}
.back-button:hover {
  background: #fff7ed;
}
</style>

<main class="support-main">
  <div class="support-wrapper">

    <!-- 上部：朝・昼・夜の切り替えボタン -->
    <div class="meal-tabs">
      <a href="<%= ctx %>/admin/support-meals?mealType=breakfast"
         class="meal-tab ${mealType eq 'breakfast' ? 'is-active' : ''}">
        朝飯
      </a>
      <a href="<%= ctx %>/admin/support-meals?mealType=lunch"
         class="meal-tab ${mealType eq 'lunch' ? 'is-active' : ''}">
        昼飯
      </a>
      <a href="<%= ctx %>/admin/support-meals?mealType=dinner"
         class="meal-tab ${mealType eq 'dinner' ? 'is-active' : ''}">
        夜飯
      </a>
    </div>

    <!-- 中央タイトル -->
    <div class="support-title-main">今日の対応食</div>

    <!-- サブタイトル（選択中の食事） -->
    <div class="support-subtitle-row">
      <div class="support-subtitle">
        <c:out value="${mealLabel}" /> の対応食管理表
      </div>
    </div>

    <div class="support-body">

      <!-- 左：人数表示 -->
      <div class="support-left">
        <div class="support-left-label">対応食が必要な人数</div>
        <div class="support-count-box">
          <c:out value="${needCount}" />人
        </div>
      </div>

      <!-- 右：テーブル -->
      <div class="support-right">

        <c:if test="${empty items}">
          現在、この時間帯の献立が登録されていません。
        </c:if>

        <c:if test="${not empty items}">
          <div class="support-table-wrapper">
            <table class="support-table">
              <thead>
                <tr>
                  <th class="col-name">名前</th>
                  <c:forEach var="it" items="${items}">
                    <!-- MenuItem の名前フィールドに合わせて修正してください -->
                    <th><c:out value="${it.name}" /></th>
                  </c:forEach>
                </tr>
              </thead>

              <tbody>
                <c:forEach var="ind" items="${individuals}">
                  <tr>
                    <td class="name-cell">
                      <c:out value="${ind.displayName}" />
                    </td>

                    <!-- 各セルの ○ 判定 -->
                    <c:forEach var="it" items="${items}">
                      <td>
                        <c:set var="key"
                               value="${fn:concat(ind.id, fn:concat('-', it.id))}" />
                        <c:if test="${supportMap[key]}">
                          <span class="circle-mark">○</span>
                        </c:if>
                      </td>
                    </c:forEach>
                  </tr>
                </c:forEach>
              </tbody>
            </table>
          </div>
        </c:if>

      </div><!-- /support-right -->

    </div><!-- /support-body -->

    <!-- 下部：戻るボタン -->
    <div class="support-footer">
      <a href="<%= ctx %>/admin/home" class="back-button">戻る</a>
    </div>

  </div><!-- /support-wrapper -->
</main>
