<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<jsp:include page="/header.jsp"/>

<style>
  /* ==== このページ専用のレイアウト調整 ==== */
  #user-list-page {
    margin-top: 8px;
  }

  /* 検索フォームと一覧テーブルの横幅を揃える */
  #user-list-page .search-card,
  #user-list-page .user-table-card {
    max-width: 920px;
    margin: 0 auto 16px;
  }

  /* 検索フォームまわり */
  #user-list-page .search-card {
    padding: 12px 20px;
  }
  #user-list-page .search-card .form-row {
    display: flex;
    align-items: center;
    gap: 12px;
  }
  #user-list-page .search-card label {
    margin: 0;
    white-space: nowrap;
    font-weight: 600;
  }
  #user-list-page .search-card input[type="text"] {
    flex: 1;
  }
  #user-list-page .search-card .actions {
    margin-top: 8px;
    text-align: right;
  }

  /* 一覧テーブル（style.css の simple-table をこのページだけ上書き） */
  #user-list-page .user-list-table {
    width: 100%;
    border-collapse: collapse;
    border: 1px solid #e5e7eb;
    border-radius: 8px;
    overflow: hidden;
    background: #fff;
    table-layout: auto; /* もし style.css で fixed 指定されていても上書き */
  }
  #user-list-page .user-list-table th,
  #user-list-page .user-list-table td {
    padding: 6px 10px; /* 行の余白を少し詰める */
    border-bottom: 1px solid #e5e7eb;
    font-size: 14px;
  }
  #user-list-page .user-list-table th {
    background: #f9fafb;
    font-weight: 600;
  }
  #user-list-page .user-list-table tr:last-child td {
    border-bottom: none;
  }

  /* 各列のざっくり幅・配置 */
  #user-list-page .user-list-table .col-name    { width: 18%; }
  #user-list-page .user-list-table .col-login   { width: 18%; }
  #user-list-page .user-list-table .col-contact { width: 44%; }
  #user-list-page .user-list-table .col-actions { width: 20%; text-align: center; }

  #user-list-page .cell-contact {
    font-size: 13px;
    /* 番号が欠けないように wrap は許可しておく（left 側が切れる原因を消す） */
    white-space: normal;
  }

  /* 下の「アレルギー管理」ボタン位置 */
  #user-list-page .user-table-card .actions {
    text-align: right;
    margin-top: 12px;
  }
/* 行 hover → 削除セル以外だけ色を変える */
.user-list-table tr.clickable-row:hover td:not(.delete-cell) {
  background: #f3f4f6;
}

/* 削除セルは常に白 */
.user-list-table td.delete-cell {
  background: #fff;
}

/* 削除セル hover → 何も変えない（白のまま） */
.user-list-table td.delete-cell:hover {
  background: #fff !important;
}

</style>

<div id="user-list-page">

<%
  String flash = (String) session.getAttribute("flash");
  if (flash != null) {
%>
  <div class="alert" style="background:#ecfeff;border:1px solid #67e8f9;color:#155e75;border-radius:6px;padding:10px 12px;">
    <%= flash %>
  </div>
<%
    session.removeAttribute("flash");
  }
  String err = (String) session.getAttribute("error");
  if (err != null) {
%>
  <div class="alert error"><%= err %></div>
<%
    session.removeAttribute("error");
  }
%>

  <h2>利用者一覧</h2>

  <!-- 検索フォーム -->
  <form method="get"
        action="${pageContext.request.contextPath}/admin/users"
        class="card search-card">
    <div class="form-row">
      <label>名前で検索</label>
      <input type="text" name="q" value="<c:out value='${q}'/>">
    </div>
    <div class="actions">
      <button class="button">検索</button>
    </div>
  </form>

  <!-- 一覧テーブル -->
  <div class="card user-table-card">
    <table class="user-list-table">
      <thead>
        <tr>
          <th class="col-name">名前</th>
          <th class="col-login">アカウントID</th>
          <th class="col-contact">緊急連絡先</th>
          <th class="col-actions">操作</th>
        </tr>
      </thead>
      <tbody>
        <c:choose>
          <c:when test="${empty rows}">
            <tr>
              <td colspan="4" style="text-align:center;color:#777;">
                該当する利用者はいません。
              </td>
            </tr>
          </c:when>
          <c:otherwise>
            <c:forEach var="r" items="${rows}">
             <tr class="clickable-row"
    onclick="location.href='${pageContext.request.contextPath}/admin/accounts?userId=${r.userId}'">

                <!-- 名前 -->
                <td><c:out value="${r.displayName}"/></td>

                <!-- アカウントID（リンク） -->
                <td>
                  <a href="${pageContext.request.contextPath}/admin/accounts?userId=${r.userId}">
                    <c:out value="${r.loginId}"/>
                  </a>
                </td>

                <!-- 緊急連絡先：1ユーザー分を1行で表示
                     DAO 側で
                     STRING_AGG(uc.label || '：' || uc.phone, ' / ')
                     のようにまとめてあれば
                     例）「メイン：090-1111-2222 / 自宅：048-xxx-xxxx」
                -->
                <td class="cell-contact">
                  <c:choose>
                    <c:when test="${empty r.contacts}">
                      ー
                    </c:when>
                    <c:otherwise>
                      <c:out value="${r.contacts}"/>
                    </c:otherwise>
                  </c:choose>
                </td>

                <!-- 操作 -->
                <td class="delete-cell" onclick="event.stopPropagation();" style="text-align:center;">
  <a class="button ghost"
     href="${pageContext.request.contextPath}/admin/users/delete?id=${r.id}"
     onclick="event.stopPropagation();">
    削除
  </a>
</td>
              </tr>
            </c:forEach>
          </c:otherwise>
        </c:choose>
      </tbody>
    </table>

    <div class="actions">
      <a class="button" href="${pageContext.request.contextPath}/admin/allergens">
        アレルギー管理
      </a>
    </div>
  </div>

</div>

<jsp:include page="/footer.jsp"/>
