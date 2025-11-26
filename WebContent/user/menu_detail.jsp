<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%
  // タブタイトル
  request.setAttribute("pageTitle", "献立（詳細）");
%>

<jsp:include page="/header_user.jsp" />

<style>
/* ===== このページ専用スタイル ===== */

/* body が text-align:center なので本文だけ左寄せに戻す */
main.menu-detail-content {
  text-align: left;
}

/* 「対象：～」の行（位置は今のまま。軽く余白だけ） */
.menu-detail-person-row {
  margin: 8px 0 12px;
}
.menu-detail-person-row label {
  font-weight: 600;
  color: #555;
}
.menu-detail-person-row select {
  padding: 6px 10px;
  border-radius: 6px;
  border: 1px solid #cbd5e1;
  margin-left: 4px;
}

/* ▼ ここがポイント：サイドメニュー分だけ右に寄せるラッパ */
.menu-detail-inner {
  position: relative;
  left: -120px;          /* サイドバー 240px の半分だけ右にシフト */
}

/* スマホ幅ではシフトを無効化 */
@media (max-width: 959px) {
  .menu-detail-inner {
    left: 0;
  }
}

/* タイトル・見出しを中央寄せする用ラッパ */
.menu-detail-full-center {
  text-align: center;
}

/* ◎月〇日のメニュー */
.menu-detail-title {
  font-size: 20px;
  font-weight: 800;
  letter-spacing: .08em;
  color: #333;
  margin: 8px 0 14px;
}

/* 朝食 / 昼食 / 夕食 見出し */
.menu-detail-section-title {
  font-weight: 800;
  color: #5b4a3b;
  font-size: 16px;
  letter-spacing: .08em;
  margin: 12px 0 8px;
}

/* コンテンツ全体の横幅 */
.menu-detail-wrap {
  max-width: 1100px;
  margin: 0 auto 24px;
}

/* 左：写真カード + 右：メニュー名カード */
.menu-detail-grid {
  display: grid;
  grid-template-columns: 2.1fr 1fr;
  gap: 18px;
  align-items: flex-start;
  margin: 0 auto 28px;

  /* ★グリッド全体を少し右に寄せる → 左の「写真」カードの中心が画面中央に来るイメージ */
  transform: translateX(14%);
}
/* スマホ幅では1カラム＆シフト解除 */
@media (max-width: 1000px) {
  .menu-detail-grid {
    grid-template-columns: 1fr;
    transform: none;   /* スマホでは普通に中央寄せ */
  }
}


.menu-detail-left,
.menu-detail-right {
  background: #fff;
  border: 1px solid #e6d3bc;
  border-radius: 12px;
  box-shadow: 0 2px 6px rgba(0,0,0,.06);
  padding: 14px 16px;
}

/* 画像：カード内で中央 */
.menu-detail-img {
  max-width: 100%;
  height: auto;
  display: block;
  border: 1px solid #eee;
  border-radius: 8px;
  margin: 0 auto;
}

/* 「写真」プレースホルダ（カード中央） */
.menu-detail-img-placeholder {
  height: 260px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #666;
  border-radius: 8px;
  border: 1px dashed #ddd;
  background: #faf5ee;
}

/* 説明文 */
.menu-detail-desc {
  margin-top: 10px;
  white-space: pre-wrap;
  color: #333;
  font-size: 14px;
}

/* 右側：メニュー名・アレルゲン */
.menu-detail-right-label {
  font-size: 12px;
  color: #7a6a5a;
  margin-bottom: 2px;
}

.menu-detail-right-name {
  font-size: 18px;
  font-weight: 800;
  margin-bottom: 10px;
  text-align: center;
  letter-spacing: .02em;
}

.menu-detail-right-msg {
  margin: 8px 0;
  color: #333;
  text-align: center;
  font-size: 14px;
}

.menu-detail-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 10px 10px;
  justify-content: center;
}

.menu-detail-chip {
  background:#e53935;
  color:#fff;
  padding:6px 12px;
  border-radius:999px;
  font-weight:700;
  font-size:13px;
  line-height:1;
  box-shadow:0 1px 0 rgba(0,0,0,.08);
}

.menu-detail-others {
  margin-top:8px;
  color:#222;
  font-size:14px;
}

/* 戻るボタン */
.menu-detail-footer {
  margin-top: 8px;
  text-align: center;
}
.menu-detail-back-btn {
  border:2px solid #333;
  background:#fff;
  padding:10px 18px;
  border-radius:10px;
  cursor:pointer;
}

/* スマホ時は1カラムに */
@media (max-width: 1000px) {
  .menu-detail-grid {
    grid-template-columns: 1fr;
  }
}
/* 対象：〜 の位置を写真枠の左と揃える */
.menu-detail-person-row {
  margin: 8px 0 12px;
  position: relative;
  left: 120px;   /* ★ ここを追加：写真の左と揃う位置に移動 */
}

/* スマホ幅では位置補正を無効化する */
@media (max-width: 959px) {
  .menu-detail-person-row {
    left: 0;
  }
}
</style>

<main class="content menu-detail-content">

  <!-- ▼ 対象：名前（この位置はそのまま） -->
  <form method="get" action="${pageContext.request.contextPath}/user/menu_detail"
        class="menu-detail-person-row">
    <input type="hidden" name="date" value="${menuDate}" />
    <label>
      対象：
      <select name="personId" onchange="this.form.submit()">
        <c:forEach var="c" items="${children}">
          <option value="${c.id}" <c:if test="${c.id eq personId}">selected</c:if>>
            ${c.displayName}
          </option>
        </c:forEach>
      </select>
    </label>
  </form>

  <!-- ▼ ここからを 120px 右に寄せて「画面の真ん中寄り」にする -->
  <div class="menu-detail-inner">
    <div class="menu-detail-wrap">

      <!-- ◎月〇日のメニュー（中央） -->
      <div class="menu-detail-full-center">
        <div class="menu-detail-title">${headTitle}</div>
      </div>

      <!-- 朝食 / 昼食 / 夕食 セクション -->
      <c:forEach var="sec" items="${sections}">

        <!-- 見出し（中央） -->
        <div class="menu-detail-full-center">
          <div class="menu-detail-section-title">${sec.label}</div>
        </div>

        <!-- 左：写真 / 右：アレルゲン -->
        <div class="menu-detail-grid">

          <!-- 左：画像 + 説明 -->
          <section class="menu-detail-left">
            <c:set var="imgMeal" value="${sec.imagePath}" />
            <c:set var="imgDay"  value="${menuImagePath}" />

            <c:choose>
              <c:when test="${not empty imgMeal}">
                <c:choose>
                  <c:when test="${fn:startsWith(imgMeal, '/')}">
                    <img class="menu-detail-img"
                         src="${pageContext.request.contextPath}${imgMeal}" alt="献立画像">
                  </c:when>
                  <c:otherwise>
                    <img class="menu-detail-img"
                         src="${pageContext.request.contextPath}/${imgMeal}" alt="献立画像">
                  </c:otherwise>
                </c:choose>
              </c:when>

              <c:when test="${not empty imgDay}">
                <c:choose>
                  <c:when test="${fn:startsWith(imgDay, '/')}">
                    <img class="menu-detail-img"
                         src="${pageContext.request.contextPath}${imgDay}" alt="献立画像">
                  </c:when>
                  <c:otherwise>
                    <img class="menu-detail-img"
                         src="${pageContext.request.contextPath}/${imgDay}" alt="献立画像">
                  </c:otherwise>
                </c:choose>
              </c:when>

              <c:otherwise>
                <div class="menu-detail-img-placeholder">
                  写真
                </div>
              </c:otherwise>
            </c:choose>

            <c:if test="${not empty sec.description}">
              <div class="menu-detail-desc">${sec.description}</div>
            </c:if>
          </section>

          <!-- 右：メニュー名 + アレルゲン -->
          <aside class="menu-detail-right">
            <div class="menu-detail-right-label">メニュー名</div>
            <div class="menu-detail-right-name">${sec.name}</div>
            <div class="menu-detail-right-msg">
              このメニューには以下のものが含まれています！
            </div>

            <div class="menu-detail-chips">
              <c:forEach var="a" items="${sec.allergensUser}">
                <span class="menu-detail-chip">${a.nameJa}</span>
              </c:forEach>
              <c:if test="${empty sec.allergensUser and empty sec.allergensOther}">
                <span style="color:#666">登録なし</span>
              </c:if>
            </div>

            <c:if test="${not empty sec.allergensOther}">
              <div class="menu-detail-others">
                <span>（他）</span>
                <c:forEach var="a" items="${sec.allergensOther}" varStatus="st">
                  <span>${a.nameJa}</span><c:if test="${!st.last}">・</c:if>
                </c:forEach>
              </div>
            </c:if>
          </aside>

        </div><!-- /.menu-detail-grid -->

      </c:forEach>

      <!-- 戻る -->
      <div class="menu-detail-footer">
        <button type="button" class="menu-detail-back-btn"
                onclick="location.href='${pageContext.request.contextPath}/user/menuscalendar?ym=${ym}'">
          戻る
        </button>
      </div>

    </div><!-- /.menu-detail-wrap -->
  </div><!-- /.menu-detail-inner -->
</main>

</div><!-- header_user.jsp 内の .layout をクローズ -->
</body>
</html>
