<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%
  request.setAttribute("pageTitle", "管理者ホーム");
  String ctx = request.getContextPath(); // 例: /sotugyou
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

  <!-- 3カラムカード：朝／昼／夜 -->
  <div class="meal-grid">
    <!-- ===== 朝食 ===== -->
    <div class="meal-card">
      <div class="meal-header">
        <span class="badge">朝食</span>
        <a class="small-link" href="<%=ctx%>/admin/menus_new/edit?dayId=${menuDay.id}&slot=BREAKFAST">編集</a>
      </div>

      <!-- 画像（日単位。将来スロット画像にするならここを差し替え） -->
      <div class="meal-photo">
        <c:choose>
          <c:when test="${not empty menuDay.imagePath}">
            <img src="<%=ctx%>/${menuDay.imagePath}" alt="朝食の画像">
          </c:when>
          <c:otherwise>
            <div class="photo-ph">画像未登録</div>
          </c:otherwise>
        </c:choose>
      </div>

      <!-- メニュー名 -->
      <div class="meal-name">
        <c:choose>
          <c:when test="${meals['BREAKFAST'] != null && not empty meals['BREAKFAST'].name}">
            ${meals['BREAKFAST'].name}
          </c:when>
          <c:otherwise>登録されていません</c:otherwise>
        </c:choose>
      </div>

      <!-- 品目一覧 -->
      <div class="items">
        <c:choose>
          <c:when test="${not empty itemsBySlot['BREAKFAST']}">
            <ul class="item-list">
              <c:forEach var="it" items="${itemsBySlot['BREAKFAST']}">
                <li>${it.name}</li>
              </c:forEach>
            </ul>
          </c:when>
          <c:otherwise><div class="item-empty">品目未登録</div></c:otherwise>
        </c:choose>
      </div>
    </div>

    <!-- ===== 昼食 ===== -->
    <div class="meal-card">
      <div class="meal-header">
        <span class="badge">昼食</span>
        <a class="small-link" href="<%=ctx%>/admin/menus_new/edit?dayId=${menuDay.id}&slot=LUNCH">編集</a>
      </div>

      <div class="meal-photo">
        <c:choose>
          <c:when test="${not empty menuDay.imagePath}">
            <img src="<%=ctx%>/${menuDay.imagePath}" alt="昼食の画像">
          </c:when>
          <c:otherwise>
            <div class="photo-ph">画像未登録</div>
          </c:otherwise>
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

      <div class="items">
        <c:choose>
          <c:when test="${not empty itemsBySlot['LUNCH']}">
            <ul class="item-list">
              <c:forEach var="it" items="${itemsBySlot['LUNCH']}">
                <li>${it.name}</li>
              </c:forEach>
            </ul>
          </c:when>
          <c:otherwise><div class="item-empty">品目未登録</div></c:otherwise>
        </c:choose>
      </div>
    </div>

    <!-- ===== 夕食 ===== -->
    <div class="meal-card">
      <div class="meal-header">
        <span class="badge">夕食</span>
        <a class="small-link" href="<%=ctx%>/admin/menus_new/edit?dayId=${menuDay.id}&slot=DINNER">編集</a>
      </div>

      <div class="meal-photo">
        <c:choose>
          <c:when test="${not empty menuDay.imagePath}">
            <img src="<%=ctx%>/${menuDay.imagePath}" alt="夕食の画像">
          </c:when>
          <c:otherwise>
            <div class="photo-ph">画像未登録</div>
          </c:otherwise>
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

      <div class="items">
        <c:choose>
          <c:when test="${not empty itemsBySlot['DINNER']}">
            <ul class="item-list">
              <c:forEach var="it" items="${itemsBySlot['DINNER']}">
                <li>${it.name}</li>
              </c:forEach>
            </ul>
          </c:when>
          <c:otherwise><div class="item-empty">品目未登録</div></c:otherwise>
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
  .meal-grid{
    display:grid;
    grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
    gap:16px;
  }
  .meal-card{
    border:1px solid #e1e1e1;
    border-radius:12px;
    background:#fff;
    padding:12px;
    box-shadow:0 1px 2px rgba(0,0,0,0.04);
  }
  .meal-header{
    display:flex;justify-content:space-between;align-items:center;margin-bottom:8px;
  }
  .badge{
    display:inline-block;background:#ffbf00;color:#333;padding:2px 8px;border-radius:999px;font-size:12px;
  }
  .small-link{font-size:12px;color:#1772d0;text-decoration:underline;}
  .meal-photo{
    width:100%;aspect-ratio:4/3;border:1px solid #eee;border-radius:10px;overflow:hidden;background:#fafafa;
    display:flex;align-items:center;justify-content:center;margin-bottom:8px;
  }
  .meal-photo img{width:100%;height:100%;object-fit:cover;display:block;}
  .photo-ph{color:#999;font-size:13px;}
  .meal-name{
    font-size:16px;font-weight:600;color:#333;min-height:1.8em;margin-bottom:6px;
  }
  .items{min-height:1.8em;}
  .item-list{margin:0;padding-left:18px;}
  .item-list li{margin:2px 0;}
  .item-empty{color:#888;font-size:13px;}
</style>
