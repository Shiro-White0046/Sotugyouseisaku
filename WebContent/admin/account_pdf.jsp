<%@ page contentType="text/html; charset=UTF-8" %>
<%
  // タイトルとコンテキストパス
  request.setAttribute("pageTitle", "アカウント用紙（PDFダウンロード）");
  String ctx = request.getContextPath();
%>

<jsp:include page="/header.jsp" />

<style>
  .pdf-page-wrap {
    max-width: 1200px;
    margin: 16px auto 24px;
    padding: 0 16px 24px;
  }
  .pdf-layout {
    display: flex;
    gap: 16px;
    align-items: flex-start;
  }
  .pdf-info {
    width: 40%;
    text-align: left;
  }
  .pdf-info h2 {
    margin-top: 0;
    margin-bottom: 12px;
  }
  .pdf-info p {
    margin: 6px 0;
  }
  .pdf-preview {
    flex: 1;
    min-width: 0;
  }
  .pdf-preview object {
    width: 100%;
    height: 420px; /* 小さめに固定してスクロールなしで見やすく */
    border: 1px solid #e5e7eb;
    background: #fff;
  }
  .pdf-download-btn {
    display: inline-block;
    margin-top: 12px;
  }
</style>

<main class="content">
  <div class="pdf-page-wrap">
    <div class="pdf-layout">

      <!-- 左側：説明 + ダウンロードボタン -->
      <section class="pdf-info">
        <h2>アカウント用紙（配布用PDF）</h2>

        <p>
          利用者に配布するためのアカウント情報用紙です。<br>
          ダウンロードして印刷し、アカウント名・ID・利用者名を手書きで記入して渡してください。
        </p>

        <p>
          下のボタンから PDF を開くことができます。<br>
          ブラウザで開いた状態から「印刷」や「名前を付けて保存」が可能です。
        </p>

        <a class="button pdf-download-btn"
           href="<%= ctx %>/pdf/account_template.pdf"
           target="_blank">
          PDFを開く
        </a>
      </section>

      <!-- 右側：縮小プレビュー -->
      <section class="pdf-preview">
        <object
          data="<%= ctx %>/pdf/account_template.pdf"
          type="application/pdf">
          <p>
            PDFプレビューを表示できません。<br>
            下のリンクから直接ダウンロードしてください。<br>
            <a href="<%= ctx %>/pdf/account_template.pdf" target="_blank">
              PDFを開く・ダウンロード
            </a>
          </p>
        </object>
      </section>

    </div>
  </div>
</main>
