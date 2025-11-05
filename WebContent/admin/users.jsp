<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<jsp:include page="/header.jsp"/>

<h2>利用者一覧</h2>

<!-- 検索フォーム -->
<form method="get" action="${pageContext.request.contextPath}/admin/users"
      class="card card-narrow">
  <div class="form-row">
    <label>名前で検索</label>
    <input type="text" name="q" value="<c:out value='${q}'/>">
  </div>
  <div class="actions">
    <button class="button">検索</button>
  </div>
</form>

<!-- 一覧テーブル -->
<div class="card card-wide">
  <table class="simple-table with-border">
    <thead>
      <tr>
        <th>名前</th>
        <th>アカウントID</th>
        <th>操作</th>
      </tr>
    </thead>
    <tbody>
      <c:choose>
        <c:when test="${empty rows}">
          <tr>
            <td colspan="3" style="text-align:center;color:#777;">
              該当する利用者はいません。
            </td>
          </tr>
        </c:when>
        <c:otherwise>
          <c:forEach var="r" items="${rows}">
            <tr>
              <td><c:out value="${r.displayName}"/></td>
              <td>
                <a href="${pageContext.request.contextPath}/admin/accounts?userId=${r.userId}">
                  <c:out value="${r.loginId}"/>
                </a>
              </td>
              <td>
                <a class="button ghost"
                   href="${pageContext.request.contextPath}/admin/users/delete?id=${r.id}">
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
    <a class="button" href="${pageContext.request.contextPath}/admin/users/allergies">
      アレルギー管理
    </a>
  </div>
</div>

<jsp:include page="/footer.jsp"/>
