<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="/header_user.jsp" />
<meta name="viewport" content="width=device-width, initial-scale=1">
<link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">

<style>
  body { background:#f7e1ca; margin:0; font-family:sans-serif; }
  header { background:#f6e7be; padding:16px 20px; text-align:center; }
  .wrap { max-width:720px; margin:20px auto; padding:0 16px; }

  .card {
    background:#fff;
    border:1px solid #d7b88f;
    border-radius:8px;
    padding:20px 24px;
  }
  h2 {
    margin-top:0;
    text-align:center;
  }
  .field {
    margin-bottom:16px;
  }
  .field label {
    display:block;
    font-weight:700;
    margin-bottom:6px;
  }
  .field input[type="text"] {
    width:100%;
    padding:8px 10px;
    border-radius:4px;
    border:1px solid #ccc;
    box-sizing:border-box;
  }
  .note {
    font-size:12px;
    color:#555;
    margin-top:4px;
  }

  .actions {
    margin-top:20px;
    display:flex;
    justify-content:flex-end;
    gap:12px;
  }
  .btn {
    border:2px solid #d8c68f;
    background:#faefcf;
    padding:10px 22px;
    border-radius:22px;
    cursor:pointer;
  }
  .btn-primary {
    background:#f6e7be;
    border-color:#e6d595;
  }
  .alert {
    margin-bottom:16px;
    padding:10px 12px;
    border-radius:6px;
    font-size:14px;
  }
  .alert-error {
    background:#fee2e2;
    border:1px solid #fca5a5;
    color:#991b1b;
  }
  .alert-success {
    background:#dcfce7;
    border:1px solid #86efac;
    color:#166534;
  }
</style>
</head>
<body>

<div class="wrap">
  <h2>緊急連絡先の設定</h2>

  <!-- フラッシュメッセージ -->
  <c:if test="${not empty flashMessage}">
    <div class="alert alert-success">
      ${flashMessage}
    </div>
  </c:if>

  <!-- エラーメッセージ -->
  <c:if test="${not empty error}">
    <div class="alert alert-error">
      ${error}
    </div>
  </c:if>

  <div class="card">
    <form action="${pageContext.request.contextPath}/user/emergency" method="post">

      <c:set var="c" value="${contact}" />

      <div class="field">
        <label for="label">ラベル</label>
        <input id="label" name="label" type="text"
               value="<c:out value='${empty inputLabel ? (c != null ? c.label : "メイン") : inputLabel}'/>"
               maxlength="20" required>
        <div class="note">例：メイン、自宅、勤務先 等</div>
      </div>

      <div class="field">
        <label for="phone">電話番号</label>
        <input id="phone" name="phone" type="text"
               value="<c:out value='${empty inputPhone ? (c != null ? c.phone : "") : inputPhone}'/>"
               maxlength="20"
               pattern="\d{2,4}-\d{2,4}-\d{3,4}"
               placeholder="090-1234-5678 など"
               required>
        <div class="note">半角数字とハイフンで入力してください。（例: 090-1234-5678）</div>
      </div>

      <div class="actions">
        <button type="button" class="btn" onclick="history.back()">戻る</button>
        <button type="submit" class="btn btn-primary">
          <c:choose>
            <c:when test="${c != null}">更新する</c:when>
            <c:otherwise>登録する</c:otherwise>
          </c:choose>
        </button>
      </div>

    </form>
  </div>
</div>

</body>
</html>
