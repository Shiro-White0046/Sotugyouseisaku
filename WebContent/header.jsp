<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="bean.Administrator" %>
<%
  // 共通でよく使う値
  String ctx = request.getContextPath();
  Object pt = request.getAttribute("pageTitle");
  String pageTitle = (pt != null) ? String.valueOf(pt) : "アレルギー対策アプリ（管理者）";

  // ログイン中の管理者名を安全に取り出す
  String adminName = "管理者";
  Object adminObj = session.getAttribute("admin");
  if (adminObj instanceof Administrator) {
    adminName = ((Administrator) adminObj).getName();
  }

  // 現在パス（ナビのアクティブ判定に利用）
  String uri = request.getRequestURI();
%>
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <title><%= pageTitle %></title>
  <link rel="stylesheet" href="<%= ctx %>/css/style.css">
  <script>
    // サイドバー開閉（<html>に付ける方式）
    function toggleSidebar(){ document.documentElement.classList.toggle('sidebar-open'); saveSidebarState(); }

    // 初期状態：PC(>=960px)なら開、スマホなら閉。ユーザーの前回状態を優先。
    function applyInitialSidebarState(){
      try{
        var saved = localStorage.getItem('sidebar-open');
        if (saved === 'true')      document.documentElement.classList.add('sidebar-open');
        else if (saved === 'false')document.documentElement.classList.remove('sidebar-open');
        else                        // 未保存なら画面幅で決定
          (window.innerWidth >= 960)
            ? document.documentElement.classList.add('sidebar-open')
            : document.documentElement.classList.remove('sidebar-open');
      }catch(e){
        // localStorageが使えない場合は幅で決定
        (window.innerWidth >= 960)
          ? document.documentElement.classList.add('sidebar-open')
          : document.documentElement.classList.remove('sidebar-open');
      }
    }
    function saveSidebarState(){
      try{ localStorage.setItem('sidebar-open', document.documentElement.classList.contains('sidebar-open')); }catch(e){}
    }

    // 現在ページをサイドバーで強調
    function highlightActiveNav(){
      var here = location.pathname;
      var links = document.querySelectorAll('.sidebar .nav a');
      for (var i=0;i<links.length;i++){
        var a = links[i];
        if (a.getAttribute('href') && here.indexOf(a.pathname) === 0){
          a.classList.add('active');
        }
      }
    }

    window.addEventListener('DOMContentLoaded', function(){
      applyInitialSidebarState();
      highlightActiveNav();
    });
    window.addEventListener('resize', function(){
      // 幅が大きく変わった時、未保存ユーザーなら再判定したいが
      // 保存を尊重するため、ここでは何もしない（必要なら判定ロジックを追加）
    });
  </script>
</head>
<body>
  <!-- 黄色いヘッダー -->
  <header class="app-header" role="banner">
    <button class="hamburger" aria-label="メニュー" aria-controls="sidebar" aria-expanded="false" onclick="toggleSidebar()">≡</button>
    <h1>アレルギー対策アプリ（管理者）</h1>
    <nav class="header-actions" aria-label="アカウント操作">
      <% if (adminObj != null) { %>
        <!-- シンプル版：GETログアウト（最短） -->
        <a class="btn-logout" href="<%= ctx %>/admin/logout">ログアウト</a>
        <!-- 厳格運用ならPOST+CSRFトークン方式に切替（Servlet側対応が必要）
        <form method="post" action="<%= ctx %>/admin/logout" style="display:inline;">
          <button type="submit" class="btn-logout">ログアウト</button>
        </form>
        -->
      <% } %>
    </nav>
  </header>

  <div class="layout">
    <!-- サイドバー -->
    <nav id="sidebar" class="sidebar" aria-label="サイドバー">
      <div class="sidebar-user"><%= adminName %></div>
      <ul class="nav">
        <li><a href="<%= ctx %>/admin/home">ホーム</a></li>
        <li><a href="<%= ctx %>/admin/users">利用者一覧</a></li>
        <li><a href="<%= ctx %>/admin/users/register">利用者アカウント作成</a></li>
        <li><a href="<%= ctx %>/admin/allergens">アレルギー情報管理</a></li>
		<li><a href="<%= ctx %>/admin/menus_new">献立作成</a></li>
        <li><a href="<%= ctx %>/admin/support-meals">対応食管理</a></li>
        <li><a href="<%= ctx %>/admin/auth">認証機能</a></li>
        <li><a href="<%= ctx %>/admin/emergency_guide.jsp">緊急対応ガイド</a></li>
        <li><a href="${pageContext.request.contextPath}/admin/logs">操作ログ</a></li>
        <li><a href="<%= ctx %>/admin/logout">ログアウト</a></li>
      </ul>
    </nav>

    <!-- ページ本文開始 -->
    <main class="content" role="main">
