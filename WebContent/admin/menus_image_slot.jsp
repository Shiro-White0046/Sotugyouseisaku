<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
  request.setAttribute("pageTitle", "献立画像の追加");
  String ctx = request.getContextPath();
%>
<jsp:include page="/header.jsp" />

<main class="content">
  <h2>${slot} の画像追加</h2>

  <c:if test="${not empty error}">
    <div class="alert danger">${error}</div>
  </c:if>
  <c:if test="${not empty sessionScope.flash}">
    <div class="alert success">${sessionScope.flash}</div>
    <c:remove var="flash" scope="session"/>
  </c:if>

  <div style="display:flex;gap:40px;align-items:flex-start;flex-wrap:wrap;margin-top:24px;">
    <!-- 左：画像追加フォーム -->
    <div class="card" style="flex:1;min-width:260px;">
      <h3>画像を追加</h3>
      <form method="post" action="<%=ctx%>/admin/menus_new/image" enctype="multipart/form-data">
        <input type="hidden" name="dayId" value="${dayId}" />
        <input type="hidden" name="slot"  value="${slot}" />
        <input type="file" name="imageFile" accept="image/*" required style="margin-bottom:12px;" />
        <button type="submit" class="button">追加</button>
      </form>
    </div>

    <!-- 右：現在登録中の画像 -->
    <div class="card" style="flex:2;min-width:300px;text-align:center;">
      <h3>現在の画像</h3>
      <c:choose>
        <c:when test="${meal != null && not empty meal.imagePath}">
          <img src="<%=ctx%>/${meal.imagePath}" alt="登録済み画像"
               style="max-width:100%;height:auto;border:1px solid #ccc;padding:4px;" />
        </c:when>
        <c:otherwise>
          <p>まだ画像が登録されていません。</p>
        </c:otherwise>
      </c:choose>
    </div>
  </div>

  <div style="margin-top:24px;">
    <a class="button ghost" href="<%=ctx%>/admin/menus_new/edit?dayId=${dayId}&slot=${slot}">← 献立作成に戻る</a>
  </div>
</main>

<jsp:include page="/footer.jsp" />
