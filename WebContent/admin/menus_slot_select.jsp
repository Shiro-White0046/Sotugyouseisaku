<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
  request.setAttribute("pageTitle", "時間帯の選択");
  String ctx = request.getContextPath();
%>
<jsp:include page="/header.jsp" />

<main class="content">
  <h2>${menuDay.menuDate} の時間帯を選択</h2>

  <div style="display:grid;grid-template-columns:repeat(3,1fr);gap:16px;">
    <div class="card">
      <h3>朝食</h3>
      <p><c:choose><c:when test="${not empty meals.BREAKFAST}">登録済み</c:when><c:otherwise>未登録</c:otherwise></c:choose></p>
      <a class="button" href="<%=ctx%>/admin/menus_new/edit?dayId=${menuDay.id}&slot=BREAKFAST">この時間帯を作成/編集</a>
    </div>

    <div class="card">
      <h3>昼食</h3>
      <p><c:choose><c:when test="${not empty meals.LUNCH}">登録済み</c:when><c:otherwise>未登録</c:otherwise></c:choose></p>
      <a class="button" href="<%=ctx%>/admin/menus_new/edit?dayId=${menuDay.id}&slot=LUNCH">この時間帯を作成/編集</a>
    </div>

    <div class="card">
      <h3>夕食</h3>
      <p><c:choose><c:when test="${not empty meals.DINNER}">登録済み</c:when><c:otherwise>未登録</c:otherwise></c:choose></p>
      <a class="button" href="<%=ctx%>/admin/menus_new/edit?dayId=${menuDay.id}&slot=DINNER">この時間帯を作成/編集</a>
    </div>
  </div>

  <div style="margin-top:24px;">
    <a class="button ghost" href="<%=ctx%>/admin/menus_new">← 月一覧に戻る</a>
  </div>
</main>

<jsp:include page="/footer.jsp" />
