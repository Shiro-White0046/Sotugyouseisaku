<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<jsp:include page="/header_user.jsp" />

<style>
  :root{
    --bg:#ffe9c7;
    --panel:#ffe0c2;
    --header:#fff2bf;
    --section:#fff7e5;
    --border:#e2c59a;
    --accent:#d97757;
    --accent-soft:#fed7aa;
    --text-main:#333;
  }

  body{
    background:var(--bg);
    font-family:"Hiragino Kaku Gothic ProN", system-ui, sans-serif;
    margin:0;
  }

  .guide-wrap{
    max-width:980px;
    margin:24px auto 40px;
    padding:0 16px 32px;
    box-sizing:border-box;
  }

  .guide-header{
    background:var(--header);
    border:1px solid var(--border);
    border-bottom:none;
    padding:16px 20px;
    border-radius:10px 10px 0 0;
    text-align:center;
  }

  .guide-header h2{
    margin:0;
    font-size:24px;
    letter-spacing:.08em;
  }

  .guide-body{
    background:var(--panel);
    border:1px solid var(--border);
    border-top:none;
    border-radius:0 0 10px 10px;
    padding:24px 20px 28px;
  }

  .guide-lead{
    font-size:15px;
    line-height:1.8;
    margin-bottom:18px;
  }

  .guide-alert{
    background:#fef2f2;
    border:1px solid #fecaca;
    color:#b91c1c;
    padding:10px 12px;
    border-radius:6px;
    font-size:13px;
    margin-bottom:18px;
  }

  .guide-section{
    background:var(--section);
    border-radius:10px;
    padding:18px 18px 16px;
    margin-top:16px;
    border:1px solid rgba(0,0,0,0.04);
  }

  .guide-section h3{
    margin:0 0 8px;
    font-size:18px;
    display:flex;
    align-items:center;
    gap:6px;
  }

  .guide-section h3 span.badge{
    display:inline-block;
    font-size:11px;
    padding:2px 6px;
    border-radius:999px;
    background:var(--accent-soft);
    color:#7c2d12;
  }

  .guide-section p{
    margin:4px 0 8px;
    font-size:14px;
    line-height:1.8;
  }

  .guide-list{
    margin:4px 0 8px 1.3em;
    padding:0;
    font-size:14px;
  }
  .guide-list li{
    margin:2px 0 2px;
    line-height:1.7;
  }

  .guide-important{
    background:var(--accent-soft);
    border-left:4px solid var(--accent);
    padding:8px 10px;
    margin-top:6px;
    border-radius:6px;
    font-size:13px;
  }

  .guide-footer{
    margin-top:22px;
    font-size:12px;
    line-height:1.7;
    color:#555;
  }

  .guide-footer strong{
    color:#b91c1c;
  }

  /* 戻るボタン */
  .guide-back{
    margin-top:24px;
    text-align:center;
  }
  .guide-back a{
    display:inline-flex;
    align-items:center;
    justify-content:center;
    min-width:160px;
    padding:10px 18px;
    border-radius:20px;
    border:1px solid var(--border);
    background:#fff8eb;
    color:#333;
    font-size:14px;
    text-decoration:none;
  }
  .guide-back a:hover{
    background:#fff0d7;
  }

  @media (max-width:640px){
    .guide-body{
      padding:18px 12px 22px;
    }
  }
</style>

<main class="guide-wrap">
  <div class="guide-header">
    <h2>緊急対応ガイド（アレルギー誤飲・誤食時）</h2>
  </div>

  <div class="guide-body">
    <p class="guide-lead">
      このページは、アレルギーのあるお子さまが<span style="font-weight:600;">誤って原因となる食べ物を口にしてしまったとき</span>の
      基本的な対応の流れをまとめたものです。<br>
      少しでも様子がおかしいと感じたら、迷わず医療機関や救急相談に連絡してください。
    </p>

    <div class="guide-alert">
      ※このガイドはあくまで目安です。<br>
      <strong>実際の診断・治療・判断は必ず医師・救急隊の指示を優先してください。</strong>
    </div>

    <!-- 1. 今すぐ確認・対応すること -->
    <section class="guide-section">
      <h3>1．今すぐ行うこと <span class="badge">最優先</span></h3>
      <ul class="guide-list">
        <li>口に残っている食べ物があれば、<strong>無理をさせない範囲で取り除く</strong>（吐かせるのは医師の指示がある場合のみ）。</li>
        <li>息苦しさ・顔色・声の出し方・ぐったりしていないかなど、<strong>いつもと違う様子がないか確認</strong>する。</li>
        <li>エピペン（アドレナリン自己注射薬）を処方されている場合は、<strong>使用するタイミングについて事前に医師から聞いておいた指示</strong>に従う。</li>
        <li>迷ったとき・不安なときは、<strong>ためらわずに医療機関・救急（119番）・#7119などの救急相談窓口</strong>に連絡する。</li>
      </ul>
      <div class="guide-important">
        特に、<strong>呼吸が苦しそう・声がかすれる・ぐったりしている・意識がぼんやりする</strong>などの症状があれば、
        すぐに119番通報し、アレルギーがあることと、口にしたものの名前を伝えてください。
      </div>
    </section>

    <!-- 2. 症状の目安 -->
    <section class="guide-section">
      <h3>2．よくみられる症状の例</h3>
      <p>アレルギー症状は、<strong>数分以内～2時間程度</strong>で現れることが多いとされています。</p>
      <ul class="guide-list">
        <li><strong>皮ふ：</strong>じんましん、赤み、かゆみ、まぶたや唇・耳たぶのはれ など</li>
        <li><strong>消化器：</strong>腹痛、吐き気、嘔吐、下痢 など</li>
        <li><strong>呼吸器：</strong>せきこみ、ゼーゼー・ヒューヒューする息、息苦しさ、声が出しにくい など</li>
        <li><strong>全身：</strong>顔色が悪い、ぐったりする、意識がもうろうとする など</li>
      </ul>
      <div class="guide-important">
        皮ふだけの軽い症状であっても、<strong>短時間で症状が変化・悪化する場合があります。</strong><br>
        「さっきよりも悪くなっていないか」をこまめに観察し、必要に応じて医療機関へ連絡してください。
      </div>
    </section>

    <!-- 3. 救急車を呼ぶ目安 -->
    <section class="guide-section">
      <h3>3．救急車を呼ぶ目安</h3>
      <p>次のような様子がひとつでも見られたら、<strong>すぐに119番通報</strong>し、救急車を呼びます。</p>
      <ul class="guide-list">
        <li>声が出しにくい、かすれる、犬が吠えるようなせきが出る</li>
        <li>ゼーゼー・ヒューヒューと苦しそうな呼吸をしている</li>
        <li>唇や爪の色が紫っぽい、顔色が明らかに悪い</li>
        <li>立ち上がれない・歩けない・ぐったりしている</li>
        <li>意識がもうろうとしている、呼びかけに反応しない</li>
      </ul>
      <div class="guide-important">
        救急隊への連絡時には、<br>
        ・お子さまの年齢・体重<br>
        ・もともとのアレルギー（卵・牛乳・小麦 など）<br>
        ・誤って口にした食品の名前と量、食べてからの時間<br>
        ・エピペンを使用したかどうか<br>
        を伝えると、よりスムーズです。
      </div>
    </section>

    <!-- 4. やってはいけないこと -->
    <section class="guide-section">
      <h3>4．やってはいけないこと</h3>
      <ul class="guide-list">
        <li><strong>自己判断で吐かせること</strong>（誤嚥や状態悪化のおそれがあります）。</li>
        <li><strong>市販薬だけで様子を見ること</strong>（症状の変化を見逃すおそれがあります）。</li>
        <li><strong>「少しだから大丈夫」と決めつけて、受診や相談を先延ばしにすること。</strong></li>
        <li>エピペンの使い方に自信がないまま、<strong>説明書を読まずに自己流で使用すること。</strong></li>
      </ul>
    </section>

    <!-- 5. 普段からできる準備 -->
    <section class="guide-section">
      <h3>5．普段からの準備</h3>
      <ul class="guide-list">
        <li>かかりつけ医と相談し、<strong>「どのような症状が出たらエピペンを使うか」「どのタイミングで受診・救急要請するか」</strong>を書面などで確認しておく。</li>
        <li>園・学校・家族・預け先などと情報を共有し、<strong>緊急連絡先・持参薬の保管場所</strong>を分かりやすくしておく。</li>
        <li>エピペンをお持ちの場合は、<strong>有効期限・保管温度・持ち歩き方法</strong>を定期的に確認する。</li>
        <li>誤飲・誤食を防ぐため、<strong>原材料表示の確認・食材の管理・アレルゲンの持ち込みルール</strong>を家族で話し合っておく。</li>
      </ul>
    </section>

    <div class="guide-footer">
      <strong>【重要】</strong><br>
      このページは、緊急時の行動の「めやす」を示したものであり、すべての状況をカバーするものではありません。<br>
      少しでも迷ったとき、不安を感じたときは、<strong>ためらわずに医療機関や救急相談窓口に連絡し、専門家の指示に従ってください。</strong>
    </div>

    <div class="guide-back">
      <a href="${pageContext.request.contextPath}/user/home">ホームに戻る</a>
    </div>
  </div>
