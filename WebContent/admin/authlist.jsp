<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%
  request.setAttribute("pageTitle", "認証ページ");
%>

<jsp:include page="/header.jsp" flush="true" />

<style>
:root{
  --bg:#fde9c9;
  --panel:#f3dcb1;
  --panel-dark:#e8cda1;
  --row:#e3a07e;
  --row-border:#c78866;
  --header:#f7e6bf;
  --sticky:#f2dfaf;
}
body{ background:var(--bg); }

/* 全体カード */
.auth-container{
  max-width:980px;
  margin:24px auto 48px;
  padding:12px 16px 24px;
  background:var(--panel);
  border:1px solid #d7b88f;
  border-radius:8px;
}
.auth-title{
  text-align:center;
  font-weight:700;
  margin:4px 0 12px;
}

/* 検索ボックス */
.search-row{
  display:grid;
  grid-template-columns:1fr auto;
  gap:12px;
  align-items:center;
  margin:8px 0 12px;
}
.search-field{ position:relative; }
.search-field input[type="text"]{
  width:100%; box-sizing:border-box;
  padding:10px 40px 10px 12px;
  border:1px solid #aaa;
  border-radius:8px;
  font-size:14px;
  background:#fff;
}
.search-field button{
  position:absolute;
  right:6px;
  top:50%;
  transform:translateY(-50%);
  border:none;
  background:transparent;
  cursor:pointer;
  padding:4px 6px;
  font-size:18px;
}

/* 一覧テーブル */
.table-wrap{
  margin-top:8px;
  border:1px solid var(--row-border);
  border-radius:6px;
  overflow:hidden;
  background:#fff;
}
.scroll-body{ max-height:400px; overflow-y:auto; }

.list-table{
  width:100%;
  border-collapse:separate;
  border-spacing:0;
  table-layout:fixed;
  font-size:15px;
}

/* ヘッダ */
.list-table thead th{
  position:sticky;
  top:0;
  background:var(--header);
  z-index:2;
  text-align:left;
  padding:8px 10px;
  border-bottom:1px solid var(--row-border);
  font-weight:700;
}

/* 左固定（ID列） */
.list-table th.sticky-left,
.list-table td.sticky-left{
  position:sticky;
  left:0;
  z-index:1;
  background:var(--panel-dark);
  width:72px;
  text-align:left;
}

/* 右固定（認証時間列） */
.list-table th.sticky-right,
.list-table td.sticky-right{
  position:sticky;
  right:0;
  z-index:1;
  background:var(--sticky);
  width:90px;
  text-align:center;
  padding:0 4px;
}

/* 本文セル */
.list-table tbody td{
  padding:6px 12px;
  background:var(--row);
  border-bottom:1px solid var(--row-border);
  color:#1f1f1f;
  word-break:break-all;
  vertical-align:middle;
}
.list-table tbody tr:hover td{
  filter:brightness(0.98);
}

/* 列別スタイル */
.col-name{
  width:100%;
}
.col-slot{
  width:45px;
  text-align:center;
  padding:0;
}

/* 認証時間（折り返さず横1行） */
.time-cell{
  white-space:nowrap;
  word-break:normal;
  text-align:center;
}

/* 〇マーク（セル中央に小さく） */
.mark-circle{
  display:inline-flex;
  align-items:center;
  justify-content:center;
  width:16px;
  height:16px;
  border-radius:50%;
  border:2px solid #5b3b2a;
  box-sizing:border-box;
}

/* 認証済みの名前を少し太字などにしたい場合はここにクラス追加も可 */
</style>

<div class="auth-container">
  <h2 class="auth-title">認証ページ</h2>

  <!-- 検索（GET） -->
  <form method="get"
        action="${pageContext.request.contextPath}/admin/auth"
        class="search-row" autocomplete="off">
    <div class="search-field">
      <input type="text" name="q" value="${fn:escapeXml(param.q)}"
             placeholder="検索したい名前を入力してください"
             maxlength="50" pattern=".{0,50}" />
      <button type="submit" aria-label="検索">🔍</button>
    </div>
  </form>

  <!-- 一覧 -->
  <div class="table-wrap">
    <div class="scroll-body">
      <table class="list-table">
        <thead>
          <tr>
            <th class="sticky-left">ID</th>
            <th>名前</th>
            <th class="col-slot">朝</th>
            <th class="col-slot">昼</th>
            <th class="col-slot">夜</th>
            <th class="sticky-right">認証時間</th>
          </tr>
        </thead>
        <tbody>
          <c:choose>
            <c:when test="${not empty list}">
              <c:forEach var="ind" items="${list}" varStatus="st">
                <tr>
                  <!-- ID -->
                  <td class="sticky-left">${st.index + 1}</td>

                  <!-- 名前（常にリンクで認証画面へ遷移） -->
                  <td class="col-name">
                    <a href="<c:url value='/admin/auth/verify'>
                               <c:param name='id' value='${ind.id}'/>
                             </c:url>">
                      <c:out value="${ind.displayName}" />
                    </a>
                  </td>

                  <!-- 朝 / 昼 / 夜 -->
                  <td class="col-slot">
                    <c:if test="${slotMap[ind.id] == 'MORNING'}">
                      <span class="mark-circle"></span>
                    </c:if>
                  </td>
                  <td class="col-slot">
                    <c:if test="${slotMap[ind.id] == 'NOON'}">
                      <span class="mark-circle"></span>
                    </c:if>
                  </td>
                  <td class="col-slot">
                    <c:if test="${slotMap[ind.id] == 'NIGHT'}">
                      <span class="mark-circle"></span>
                    </c:if>
                  </td>

                  <!-- 認証時間 -->
                  <td class="sticky-right time-cell">
                    <c:out value="${timeMap[ind.id]}" />
                  </td>
                </tr>
              </c:forEach>
            </c:when>
            <c:otherwise>
              <tr>
                <td class="sticky-left">-</td>
                <td class="col-name">該当する利用者がいません。</td>
                <td class="col-slot"></td>
                <td class="col-slot"></td>
                <td class="col-slot"></td>
                <td class="sticky-right time-cell"></td>
              </tr>
            </c:otherwise>
          </c:choose>
        </tbody>
      </table>
    </div>
  </div>
</div>
