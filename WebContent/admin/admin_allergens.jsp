<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<jsp:include page="/header.jsp" />

<!-- 🔔 Flash Message（成功→緑／失敗→赤） -->
<c:set var="__flash"
       value="${not empty requestScope.flashMessage ? requestScope.flashMessage : sessionScope.flashMessage}" />
<c:if test="${not empty __flash}">
  <c:set var="__isError"
         value="${fn:contains(__flash, '失敗') or fn:contains(__flash, 'エラー') or fn:contains(__flash, 'できません')}" />

  <div id="flash-message" class="flash-message no-js ${__isError ? 'error' : 'success'}">
    <c:out value="${__flash}" />
  </div>

  <!-- セッションから flashMessage を削除（1回だけ表示） -->
  <c:if test="${not empty sessionScope.flashMessage}">
    <c:remove var="flashMessage" scope="session" />
  </c:if>

  <script>
    (function(){
      const el = document.getElementById('flash-message');
      if (!el) return;
      el.classList.remove('no-js');
      setTimeout(() => {
        el.style.transition = 'opacity 0.8s ease, top 0.8s ease';
        el.style.opacity = '0';
        el.style.top = '0px';
        setTimeout(() => el.remove(), 850);
      }, 3200);
    })();
  </script>
</c:if>

<style>
  body { background:#f7e1ca; margin:0; font-family:"Noto Sans JP", sans-serif; }

  h2 {
    text-align:center; margin:16px 0;
    font-size:26px; font-weight:700;
  }

  .container {
    max-width:1000px;
    margin:0 auto;
    background:#f6d9c0;
    padding:20px;
    border-radius:8px;
  }

  .search-box {
    text-align:center;
    margin-bottom:20px;
  }
  .search-box input {
    width:300px;
    padding:8px 10px;
    border-radius:8px;
    border:1px solid #aaa;
  }

  table {
    width:100%;
    border-collapse:collapse;
    background:#c6e6f7;
    border-radius:10px;
    overflow:hidden;
  }
  th, td {
    border-bottom:1px solid #b1d3e0;
    padding:10px 12px;
    text-align:left;
  }
  th {
    background:#b1d3e0;
    font-size:15px;
    font-weight:700;
  }

  .add-area {
    width:100%;
    display:flex;
    justify-content:center;
    margin-top:30px;
  }
  .add-area input {
    padding:10px;
    font-size:14px;
    border:1px solid #999;
    border-radius:8px;
    width: 220px;
    margin-right:5px;
  }
  .btn-add {
    padding:12px 24px;
    border:none;
    background:#fff;
    border:2px solid #888;
    border-radius:8px;
    cursor:pointer;
  }

  .table-scroll {
    max-height: 400px;      /* ←高さここで調整 */
    overflow-y: auto;
    border: 2px solid #b1d3e0;
    border-radius: 10px;
  }

  /* 上中央に表示される FlashMessage */
  .flash-message {
    position: fixed;
    top: 20px;
    left: 50%;
    transform: translateX(-50%);
    color: #fff;
    padding: 14px 26px;
    border-radius: 12px;
    font-size: 18px;
    font-weight: bold;
    text-align: center;
    min-width: 280px;
    max-width: 90%;
    box-shadow: 0 4px 15px rgba(0,0,0,0.35);
    z-index: 99999;
    opacity: 1;
    letter-spacing: 0.05em;
    line-height: 1.4;
  }

  .flash-message.success {
    background: rgba(0,150,0,.9);   /* 成功：緑 */
  }
  .flash-message.error {
    background: rgba(200,0,0,.9);   /* 失敗：赤 */
  }

  .flash-message.no-js {
    display: none; /* JS 無効環境で非表示 */
  }
</style>

</head>
<body>

<h2>アレルギー情報管理</h2>

<div class="container">

  <!-- 検索欄 -->
  <div class="search-box">
    <form method="get" action="${pageContext.request.contextPath}/admin/allergens-master">
      <input type="text" name="q" value="${q}" placeholder="検索したいアレルギーを入力してください">
      <button type="submit">検索</button>
    </form>
  </div>

  <c:if test="${not empty allergens}">
    <div style="text-align:center; margin-bottom:10px; font-weight:700;">
      検索結果：${count} 件
    </div>
  </c:if>

  <c:if test="${not empty error}">
    <div style="text-align:center; color:#c00; margin-bottom:8px;">
      ${error}
    </div>
  </c:if>

  <c:if test="${count == 0}">
    <div style="text-align:center; padding:10px; color:#555; font-size:15px;">
      該当するアレルギー項目はありません。
    </div>
  </c:if>

  <!-- アレルギー一覧 -->
  <div class="table-scroll">
    <table>
      <thead>
        <tr>
          <th>アレルギー名</th>
          <th>カテゴリ</th>
          <th>サブカテゴリ</th>
        </tr>
      </thead>
      <tbody>
        <c:forEach var="a" items="${allergens}">
          <tr>
            <td>${a.nameJa}</td>
            <td>${a.category}</td>
            <td>${a.subcategory}</td>
          </tr>
        </c:forEach>
      </tbody>
    </table>
  </div>

  <!-- 追加フォーム -->
  <form method="post" action="${pageContext.request.contextPath}/admin/allergens-master">
    <div class="add-area">

      <!-- アレルギー名 -->
      <input type="text" name="name" placeholder="アレルギー名" required>

      <!-- カテゴリ選択 -->
      <select id="category" name="category" required
              style="padding:10px;border-radius:8px;border:1px solid #999;">
        <option value="">カテゴリ選択</option>
        <option value="FOOD">FOOD（食物アレルギー）</option>
        <option value="CONTACT">CONTACT（接触アレルギー）</option>
        <option value="AVOID">AVOID（食べられないもの）</option>
      </select>

      <!-- サブカテゴリ選択（動的に変更） -->
      <select id="subCategory" name="subCategory" required
              style="padding:10px;border-radius:8px;border:1px solid #999; display:none;">
        <!-- JS で中身を入れ替える -->
      </select>

      <button class="btn-add">追加</button>
    </div>
  </form>

  <script>
    const category = document.getElementById('category');
    const subCategory = document.getElementById('subCategory');

    // サブカテゴリ一覧
    const SUB_OPTIONS = {
      "CONTACT": [
        { value: "METAL", text: "METAL（金属）" },
        { value: "CHEMICAL", text: "CHEMICAL（化学物質）" },
        { value: "PLANT", text: "PLANT（植物）" },
        { value: "ANIMAL", text: "ANIMAL（動物）" },
        { value: "OTHER", text: "OTHER（その他）" },
      ],
      "AVOID": [
        { value: "OTHER", text: "OTHER（その他）" },
      ]
    };

    // カテゴリ変更時の動作
    category.addEventListener('change', () => {
      const cat = category.value;

      // FOOD → サブカテゴリ非表示
      if (cat === "FOOD" || cat === "") {
        subCategory.style.display = "none";
        subCategory.innerHTML = "";
        subCategory.required = false;  // 必須解除
        return;
      }

      // CONTACT / AVOID の場合 → サブカテゴリ生成
      subCategory.style.display = "inline-block";
      subCategory.required = true; // 必須化
      subCategory.innerHTML = "";  // 初期化

      const opts = SUB_OPTIONS[cat] || [];

      // 初期の選択肢
      const defaultOpt = document.createElement("option");
      defaultOpt.value = "";
      defaultOpt.textContent = "サブカテゴリ選択";
      subCategory.appendChild(defaultOpt);

      // サブカテゴリ追加
      opts.forEach(o => {
        const opt = document.createElement("option");
        opt.value = o.value;
        opt.textContent = o.text;
        subCategory.appendChild(opt);
      });
    });
  </script>

</div>
</body>
</html>
