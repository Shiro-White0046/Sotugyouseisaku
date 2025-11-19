<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%
  request.setAttribute("pageTitle", "操作ログ");
%>

<jsp:include page="/header.jsp" />

<main class="content">
  <h2>操作ログ</h2>

  <p style="margin-bottom:8px;font-size:13px;color:#555;">
    直近200件の操作ログを表示しています。
  </p>

  <div class="card" style="overflow-x:auto;">
    <table class="simple-table" style="min-width:900px;">
      <thead>
        <tr>
          <th>日時</th>
          <th>種別</th>
          <th>actor_id</th>
          <th>action</th>
          <th>entity</th>
          <th>entity_id</th>
          <th>IP</th>
        </tr>
      </thead>
      <tbody>
        <c:forEach var="log" items="${logs}">
          <tr>
            <td><c:out value="${log.createdAt}"/></td>
            <td><c:out value="${log.actorType}"/></td>
            <td><c:out value="${log.actorId}"/></td>
            <td><c:out value="${log.action}"/></td>
            <td><c:out value="${log.entity}"/></td>
            <td><c:out value="${log.entityId}"/></td>
            <td><c:out value="${log.ip}"/></td>
          </tr>
        </c:forEach>
        <c:if test="${empty logs}">
          <tr>
            <td colspan="7" style="text-align:center;color:#666;">
              ログはまだありません。
            </td>
          </tr>
        </c:if>
      </tbody>
    </table>
  </div>
</main>
