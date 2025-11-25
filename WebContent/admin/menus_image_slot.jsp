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

      <form method="post" action="<%=ctx%>/admin/menus_new/image"
            enctype="multipart/form-data">

        <input type="hidden" name="dayId" value="${dayId}" />
        <input type="hidden" name="slot"  value="${slot}" />

        <!-- ★変更：id付与（プレビュー用） -->
        <input type="file" name="imageFile" id="imageFile"
               accept="image/*" required style="margin-bottom:12px;" />

        <button type="submit" class="button">追加</button>
      </form>
    </div>

    <!-- 右：現在登録中の画像（プレビュー対象） -->
    <div class="card" style="flex:2;min-width:300px;text-align:center;">
      <h3>現在の選択している画像</h3>

      <c:choose>
        <c:when test="${meal != null && not empty meal.imagePath}">
          <!-- ★変更：id を付与（プレビュー更新対象） -->
          <img id="previewImage"
               src="<%=ctx%>/${meal.imagePath}"
               alt="登録済み画像"
               style="max-width:100%;height:auto;border:1px solid #ccc;padding:4px;" />
          <p id="noImageText" style="display:none;"></p>
        </c:when>

        <c:otherwise>
          <!-- ★画像がない場合でも img を置く（非表示に） -->
          <img id="previewImage"
               src=""
               style="max-width:100%;height:auto;border:1px solid #ccc;padding:4px;display:none;" />
          <p id="noImageText">まだ画像が登録されていません。</p>
        </c:otherwise>
      </c:choose>
    </div>
  </div>

  <div style="margin-top:24px;">
    <a class="button ghost"
       href="<%=ctx%>/admin/menus_new/edit?dayId=${dayId}&slot=${slot}">
      ← 献立作成に戻る
    </a>
  </div>
</main>

<jsp:include page="/footer.jsp" />

<!-- ★★★ 追加：画像プレビュー JavaScript ★★★ -->
<script>
(function() {
  const input = document.getElementById('imageFile');
  const preview = document.getElementById('previewImage');
  const noText = document.getElementById('noImageText');

  if (!input || !preview) return;

  input.addEventListener('change', function() {
    if (!input.files || input.files.length === 0) return;

    const file = input.files[0];

    // 画像以外のファイルは拒否
    if (!file.type.match(/^image\//)) {
      alert("画像ファイルを選択してください");
      input.value = "";
      return;
    }

    const reader = new FileReader();

    reader.onload = function(e) {
      preview.src = e.target.result;
      preview.style.display = "block";     // 表示
      if (noText) noText.style.display = "none"; // "まだ画像が…" を消す
    };

    reader.readAsDataURL(file);
  });
})();
</script>
