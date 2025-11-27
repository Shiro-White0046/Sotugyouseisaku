<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<jsp:include page="/header.jsp" />

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

  /* エピペン手順のレイアウト */
  .epipen-grid{
    display:grid;
    grid-template-columns: 1.3fr 1fr;
    gap:16px;
    margin-top:8px;
  }
  .epipen-steps{
    list-style:none;
    margin:0;
    padding:0;
    font-size:13px;
  }
  .epipen-step{
    display:flex;
    align-items:flex-start;
    gap:8px;
    margin-bottom:8px;
  }
  .epipen-step-num{
    flex:none;
    width:22px;
    height:22px;
    border-radius:999px;
    background:#fff;
    border:1px solid var(--accent);
    display:flex;
    align-items:center;
    justify-content:center;
    font-size:12px;
    font-weight:700;
    color:var(--accent);
  }
  .epipen-step-body strong{
    font-weight:700;
  }

  .epipen-figure{
    /* 画像の大きさにほぼフィットさせる */
    display:flex;
    flex-direction:column;
    align-items:center;
    justify-content:flex-start;

    /* 目立つ外枠を消す */
    background:transparent;
    border:none;
    padding:4px 0;

    font-size:12px;
    line-height:1.6;
    text-align:center;
  }

  .epipen-figure img{
    display:block;
    max-width:100%;
    height:auto;
    /* 画像のまわりの余白だけ少しだけ */
    margin:0 auto 4px;
    border-radius:8px;
  }

  .epipen-figure-caption{
    font-size:11px;
    color:#555;
    margin-top:2px;
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
    .epipen-grid{
      grid-template-columns: 1fr;
    }
  }
</style>

<main class="guide-wrap">
  <div class="guide-header">
    <h2>緊急対応ガイド（アレルギー誤飲・誤食時）</h2>
  </div>

  <div class="guide-body">
    <p class="guide-lead">
      このページは、<strong>園・学校などの職員向け</strong>に、アレルギーのあるお子さまが
      <strong>誤って原因となる食べ物を口にしてしまったとき</strong>の対応の流れをまとめたものです。<br>
      実際の対応では、<strong>保護者から預かっている「アレルギー対応指示書」「エピペン指示書」</strong>と、
      医師・救急隊の指示を必ず優先してください。
    </p>

    <div class="guide-alert">
      【重要な注意】<br>
      ・このガイドは<strong>一般的な目安</strong>であり、すべてのケースをカバーするものではありません。<br>
      ・<strong>診断・治療・薬剤使用の最終判断は医師・救急隊の指示に従ってください。</strong><br>
      ・迷ったときは「様子を見る」のではなく、<strong>早めに相談・受診する</strong>ことを基本とします。
    </div>

    <!-- 1. 最初の3ステップ -->
    <section class="guide-section">
      <h3>1．何かあったかもしれない時の「最初の3ステップ」 <span class="badge">最優先</span></h3>
      <ul class="guide-list">
        <li><strong>① その場で食事・活動を中断する</strong><br>
          ・口にしている食べ物を止め、本人を安全な場所に移動させます。<br>
          ・無理に吐かせたり、走らせたりしないようにします。
        </li>
        <li><strong>② 様子を観察しながら、情報を整理する</strong><br>
          ・<strong>「いつ」「何を」「どのくらい」</strong>口にしたかを確認する。<br>
          ・過去の反応歴（重い症状だったかどうか）を、保護者の申告情報や園の記録から確認する。
        </li>
        <li><strong>③ すぐに責任者へ報告する</strong><br>
          ・クラス担任だけで抱え込まず、<strong>園長・主任・看護師など責任者</strong>にすぐ共有する。<br>
          ・「現在の症状」と「これからどうするか」を一緒に判断します。
        </li>
      </ul>
      <div class="guide-important">
        この段階で、すでに<strong>咳き込み・顔色悪化・ぐったり</strong>などが見られる場合は、<br>
        「様子を見てから」ではなく、<strong>先に救急要請（119）や救急相談（#7119 等）につなぐ</strong>ことを検討してください。
      </div>
    </section>

    <!-- 2. 症状の目安（軽症〜重症） -->
    <section class="guide-section">
      <h3>2．症状の目安と観察のポイント</h3>
      <p>アレルギー症状は、<strong>数分以内〜2時間程度</strong>で現れることが多いとされています。</p>
      <ul class="guide-list">
        <li><strong>皮ふ：</strong>じんましん、赤み、かゆみ、まぶたや唇・耳たぶのはれ など</li>
        <li><strong>消化器：</strong>腹痛、吐き気、嘔吐、下痢 など</li>
        <li><strong>呼吸器：</strong>咳き込み、ゼーゼー・ヒューヒューする息、息苦しさ、声が出しにくい など</li>
        <li><strong>全身：</strong>顔色が悪い、ぐったりする、ぼーっとして反応が悪い など</li>
      </ul>
      <div class="guide-important">
        <strong>「皮ふだけだから軽い」と決めつけない</strong>ことが重要です。<br>
        ・<strong>短時間で症状が増えていないか／範囲が広がっていないか</strong><br>
        ・呼吸や意識の変化が出ていないか<br>
        を、数分おきに確認し、少しでも悪化傾向があれば早めに受診・相談してください。
      </div>
    </section>

    <!-- 3. エピペン等の一般的な使い方 -->
    <section class="guide-section">
      <h3>3．エピネフリン自己注射薬（エピペン等）の一般的な使い方</h3>
      <p>
        保護者から<strong>エピペン（エピネフリン自己注射薬）を預かっている場合</strong>は、
        必ず事前に <strong>医師の指示書・園としてのマニュアル</strong> を確認し、
        実際の使用はその指示に従ってください。<br>
        ここでは、あくまで<strong>一般的な流れのイメージ</strong>を示します。
      </p>

      <div class="epipen-grid">
        <ul class="epipen-steps">
          <li class="epipen-step">
            <div class="epipen-step-num">1</div>
            <div class="epipen-step-body">
              <strong>症状と指示書を確認する</strong><br>
              ・呼吸が苦しそう、声が出しにくい、ぐったりしている、意識がもうろうとしている等、
              <strong>重い症状が出ているか</strong>を確認。<br>
              ・保護者からの <strong>「このような症状のときはエピペン使用」</strong> の指示があるか確認する。
            </div>
          </li>
          <li class="epipen-step">
            <div class="epipen-step-num">2</div>
            <div class="epipen-step-body">
              <strong>エピペン本体を確認する</strong><br>
              ・本人の名前、有効期限、用量（体重に応じて処方）が正しいか確認する。<br>
              ・ケースから本体を取り出し、<strong>針の出る側（先端）を必ず確認</strong>する。
            </div>
          </li>
          <li class="epipen-step">
            <div class="epipen-step-num">3</div>
            <div class="epipen-step-body">
              <strong>安全キャップを外す</strong><br>
              ・指定されている色の安全キャップ（多くは「青」など）をまっすぐ引き抜く。<br>
              ・この時点で<strong>先端側を手で握り込まない</strong>よう注意する（誤注射防止）。
            </div>
          </li>
          <li class="epipen-step">
            <div class="epipen-step-num">4</div>
            <div class="epipen-step-body">
              <strong>太ももの外側に垂直にあてて押し込む</strong><br>
              ・本人をできるだけ寝かせ、必要に応じて大人が支える。<br>
              ・太ももの外側に<strong>衣服の上からでも良い</strong>ので、先端をしっかりあてる。<br>
              ・垂直に強く押し当てて注射が始まるまで押し込む。
            </div>
          </li>
          <li class="epipen-step">
            <div class="epipen-step-num">5</div>
            <div class="epipen-step-body">
              <strong>数秒間そのまま保持する</strong><br>
              ・製品ごとの指示に従い、<strong>おおよそ数秒間</strong>太ももに押し当てたままにする。<br>
              ・終わったらまっすぐ引き抜き、太ももを軽くもむようにさする。
            </div>
          </li>
          <li class="epipen-step">
            <div class="epipen-step-num">6</div>
            <div class="epipen-step-body">
              <strong>すぐに救急要請し、経過を観察する</strong><br>
              ・<strong>エピペン使用後は必ず医療機関の受診が必要</strong>です。119番通報を行い、<br>
              「エピペンを使用したこと」「使用した時間」「症状の経過」を伝える。<br>
              ・その後も呼吸・意識の状態を継続して見守る。
            </div>
          </li>
        </ul>

        <div class="epipen-figure">
          <!-- ここに施設で作成した図解画像を設置するとわかりやすい -->
          <!-- 例：<img src="${pageContext.request.contextPath}/images/guide.png" alt="エピペン使用のイメージ図"> -->
         <img src="${pageContext.request.contextPath}/image/guide.png" alt="エピペン使用の手順図">
          <div class="epipen-figure-caption">
            図：太ももの外側に垂直にあてて数秒間押し続けるイメージ。<br>
            実際の使い方は、必ず製品付属の説明書と医師の指示を優先してください。
          </div>
        </div>
      </div>

      <div class="guide-important" style="margin-top:10px;">
        ・ここでの説明はあくまで<strong>一般的な流れ</strong>です。実際の使用方法・保持時間などは製品によって異なります。<br>
        ・園・学校としては、<strong>事前にダミー練習用キットなどで職員研修を行う</strong>ことが望ましいです。
      </div>
    </section>

    <!-- 4. 救急車を呼ぶ目安 -->
    <section class="guide-section">
      <h3>4．救急車を呼ぶ目安</h3>
      <p>次のような様子がひとつでも見られたら、<strong>すぐに119番通報</strong>し、救急車を呼びます。</p>
      <ul class="guide-list">
        <li>声が出しにくい、かすれる、犬が吠えるようなせきが出る</li>
        <li>ゼーゼー・ヒューヒューと苦しそうな呼吸をしている</li>
        <li>唇や爪の色が紫っぽい、顔色が明らかに悪い</li>
        <li>立ち上がれない・歩けない・ぐったりしている</li>
        <li>意識がもうろうとしている、呼びかけに反応しにくい</li>
      </ul>
      <div class="guide-important">
        通報時には、次の点を簡潔に伝えるとスムーズです。<br>
        ・お子さまの年齢・体重（わかる範囲で）<br>
        ・もともとのアレルギー（卵・牛乳・小麦 など）<br>
        ・誤って口にした食品の名前・量・食べてからの時間<br>
        ・エピペンを持っているか／使用したかどうか・使用した時間
      </div>
    </section>

    <!-- 5. 園・学校としての事前準備 -->
    <section class="guide-section">
      <h3>5．園・学校としての事前準備</h3>
      <ul class="guide-list">
        <li>保護者・医師と相談し、<strong>個別のアレルギー対応指示書・エピペン指示書</strong>を整備する。</li>
        <li>指示書に基づき、<strong>職員向けの園内マニュアル</strong>を作成・更新する。</li>
        <li>エピペンを預かる場合は、<strong>保管場所・持ち出し方法・有効期限</strong>を一元管理する。</li>
        <li>新年度や職員異動のタイミングで、<strong>全職員への研修とシミュレーション訓練</strong>を行う。</li>
        <li>誤飲・誤食を防ぐため、<strong>食材の管理・表示・持ち込みルール</strong>を園全体で共有する。</li>
      </ul>
    </section>

    <div class="guide-footer">
      <strong>【最終確認】</strong><br>
      このページは、緊急時の行動の「めやす」を示したものであり、<br>
      実際の判断・処置・薬剤の使用は、<strong>必ず医師・救急隊・かかりつけ医の指示に従ってください。</strong><br>
      システム上で管理しているアレルギー情報も、<strong>日々の保護者との連絡・診断書の内容</strong>と合わせて確認することが重要です。
    </div>

    <div class="guide-back">
      <a href="${pageContext.request.contextPath}/admin/home">ホームに戻る</a>
    </div>
  </div>
</main>
