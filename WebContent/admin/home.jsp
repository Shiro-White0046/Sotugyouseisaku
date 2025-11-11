<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%
  request.setAttribute("pageTitle", "管理者ホーム");
  String ctx = request.getContextPath();
%>

<jsp:include page="/header.jsp" />

<main class="content">
  <h2 style="margin-bottom:12px;">${menuDay.menuDate} の献立（ホーム）</h2>

  <c:if test="${not empty error}">
    <div class="alert danger">${error}</div>
  </c:if>
  <c:if test="${not empty sessionScope.flash}">
    <div class="alert success">${sessionScope.flash}</div>
    <c:remove var="flash" scope="session"/>
  </c:if>

  <div class="meal-grid">
    <!-- 朝食 -->
    <div class="meal-card">
      <div class="meal-header">
        <span class="badge">朝食</span>
        <span class="links">
          <a class="small-link" href="<%=ctx%>/admin/menus_new/image?dayId=${menuDay.id}&slot=BREAKFAST">画像</a>
          <a class="small-link" href="<%=ctx%>/admin/menus_new/edit?dayId=${menuDay.id}&slot=BREAKFAST">編集</a>
        </span>
      </div>
      <div class="meal-photo">
        <c:choose>
          <c:when test="${meals['BREAKFAST'] != null && not empty meals['BREAKFAST'].imagePath}">
            <img src="<%=ctx%>/${meals['BREAKFAST'].imagePath}" alt="朝食の画像">
          </c:when>
          <c:otherwise><div class="photo-ph">画像未登録</div></c:otherwise>
        </c:choose>
      </div>
      <div class="meal-name">
        <c:choose>
          <c:when test="${meals['BREAKFAST'] != null && not empty meals['BREAKFAST'].name}">
            ${meals['BREAKFAST'].name}
          </c:when>
          <c:otherwise>登録されていません</c:otherwise>
        </c:choose>
      </div>
    </div>

    <!-- 昼食 -->
    <div class="meal-card">
      <div class="meal-header">
        <span class="badge">昼食</span>
        <span class="links">
          <a class="small-link" href="<%=ctx%>/admin/menus_new/image?dayId=${menuDay.id}&slot=LUNCH">画像</a>
          <a class="small-link" href="<%=ctx%>/admin/menus_new/edit?dayId=${menuDay.id}&slot=LUNCH">編集</a>
        </span>
      </div>
      <div class="meal-photo">
        <c:choose>
          <c:when test="${meals['LUNCH'] != null && not empty meals['LUNCH'].imagePath}">
            <img src="<%=ctx%>/${meals['LUNCH'].imagePath}" alt="昼食の画像">
          </c:when>
          <c:otherwise><div class="photo-ph">画像未登録</div></c:otherwise>
        </c:choose>
      </div>
      <div class="meal-name">
        <c:choose>
          <c:when test="${meals['LUNCH'] != null && not empty meals['LUNCH'].name}">
            ${meals['LUNCH'].name}
          </c:when>
          <c:otherwise>登録されていません</c:otherwise>
        </c:choose>
      </div>
    </div>

    <!-- 夕食 -->
    <div class="meal-card">
      <div class="meal-header">
        <span class="badge">夕食</span>
        <span class="links">
          <a class="small-link" href="<%=ctx%>/admin/menus_new/image?dayId=${menuDay.id}&slot=DINNER">画像</a>
          <a class="small-link" href="<%=ctx%>/admin/menus_new/edit?dayId=${menuDay.id}&slot=DINNER">編集</a>
        </span>
      </div>
      <div class="meal-photo">
        <c:choose>
          <c:when test="${meals['DINNER'] != null && not empty meals['DINNER'].imagePath}">
            <img src="<%=ctx%>/${meals['DINNER'].imagePath}" alt="夕食の画像">
          </c:when>
          <c:otherwise><div class="photo-ph">画像未登録</div></c:otherwise>
        </c:choose>
      </div>
      <div class="meal-name">
        <c:choose>
          <c:when test="${meals['DINNER'] != null && not empty meals['DINNER'].name}">
            ${meals['DINNER'].name}
          </c:when>
          <c:otherwise>登録されていません</c:otherwise>
        </c:choose>
      </div>
    </div>
  </div>

  <div style="text-align:center;margin-top:18px;">
    <a href="<%=ctx%>/admin/menus_new" class="button ghost">月別一覧へ</a>
  </div>
</main>

<jsp:include page="/footer.jsp" />

<style>
  .meal-grid{display:grid;grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));gap:16px;}
  .meal-card{border:1px solid #e1e1e1;border-radius:12px;background:#fff;padding:12px;box-shadow:0 1px 2px rgba(0,0,0,0.04);}
  .meal-header{display:flex;justify-content:space-between;align-items:center;margin-bottom:8px;}
  .links a{ margin-left:8px; }
  .badge{display:inline-block;background:#ffbf00;color:#333;padding:2px 8px;border-radius:999px;font-size:12px;}
  .small-link{font-size:12px;color:#1772d0;text-decoration:underline;}
  .meal-photo{width:100%;aspect-ratio:4/3;border:1px solid #eee;border-radius:10px;overflow:hidden;background:#fafafa;display:flex;align-items:center;justify-content:center;margin-bottom:8px;}
  .meal-photo img{width:100%;height:100%;object-fit:cover;display:block;}
  .photo-ph{color:#999;font-size:13px;}
  .meal-name{font-size:16px;font-weight:600;color:#333;min-height:1.8em;}
</style>
