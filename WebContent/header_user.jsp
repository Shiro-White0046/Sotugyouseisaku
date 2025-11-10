<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="bean.User" %>
<%
  String ctx = request.getContextPath();
  String pageTitle = (request.getAttribute("pageTitle") != null)
      ? String.valueOf(request.getAttribute("pageTitle"))
      : "アレルギー対策アプリ（利用者）";

  // セッションの利用者名
  String userName = "利用者";
  Object userObj = session.getAttribute("user");
  if (userObj instanceof User) {
    userName = ((User) userObj).getName();
  }

  // このページが /user/home のときだけ body に page-user-home を付与
  String uri = request.getRequestURI();
  boolean isUserHome = (uri != null) && (uri.endsWith("/user/home") || uri.endsWith("/user/home.jsp"));
%>
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <title><%= pageTitle %></title>
  <link rel="stylesheet" href="<%= ctx %>/css/style.css">
  <script>
    // サイドバー開閉
    function toggleSidebar(){
      document.documentElement.classList.toggle('sidebar-open');
      saveSidebarState();
    }

    function applyInitialSidebarState(){
      try {
        var saved = localStorage.getItem('sidebar-open');
        if (saved === 'true') {
          document.documentElement.classList.add('sidebar-open');
        } else if (saved === 'false') {
          document.documentElement.classList.remove('sidebar-open');
        } else {
          if (window.innerWidth >= 960) {
            document.documentElement.classList.add('sidebar-open');
          } else {
            document.documentElement.classList.remove('sidebar-open');
          }
        }
      } catch (e) {
        if (window.innerWidth >= 960) {
          document.documentElement.classList.add('sidebar-open');
        } else {
          document.documentElement.classList.remove('sidebar-open');
        }
      }
    }

    function saveSidebarState(){
      try {
        localStorage.setItem('sidebar-open',
          document.documentElement.classList.contains('sidebar-open')
        );
      } catch(e) {}
    }

    function highlightActiveNav(){
      var here = location.pathname;
      var links = document.querySelectorAll('.sidebar .nav a');
      for (var i=0; i<links.length; i++){
        var a = links[i];
        // startsWith 相当（サブパスでも当たるよう indexOf===0）
        if (here.indexOf(a.pathname) === 0) {
          a.classList.add('active');
          a.setAttribute('aria-current', 'page');
        }
      }
    }

    window.addEventListener('DOMContentLoaded', function(){
      applyInitialSidebarState();
      highlightActiveNav();
    });

    // 画面幅変更時、未保存状態ならデフォルト判定を再適用（任意）
    window.addEventListener('resize', function(){
      var saved = null;
      try { saved = localStorage.getItem('sidebar-open'); } catch(e){}
      if (saved === null) { // ユーザー操作未保存時のみ自動追従
        if (window.innerWidth >= 960) {
          document.documentElement.classList.add('sidebar-open');
        } else {
          document.documentElement.classList.remove('sidebar-open');
        }
      }
    });
  </script>
</head>
<body class="<%= isUserHome ? "page-user-home" : "" %>">
  <!-- ヘッダー -->
  <header class="app-header">
    <button class="hamburger" aria-label="menu" onclick="toggleSidebar()">≡</button>
    <h1>アレルギー対策アプリ</h1>
    <nav class="header-actions">
      <% if (userObj != null) { %>
        <a class="btn-logout" href="<%= ctx %>/user/logout">ログアウト</a>
      <% } %>
    </nav>
  </header>

  <div class="layout">
    <!-- サイドバー（利用者メニュー） -->
    <nav class="sidebar" aria-label="サイドバー">
      <div class="sidebar-user"><%= userName %></div>
      <ul class="nav">
        <li><a href="<%= ctx %>/user/home">ホーム</a></li>
        <li><a href="<%= ctx %>/user/allergy/food">食物性アレルギー入力</a></li>
        <li><a href="<%= ctx %>/user/allergy/contact">接触性アレルギー入力</a></li>
        <li><a href="<%= ctx %>/user/avoid">食べられない食材入力</a></li>
        <li><a href="<%= ctx %>/user/menuscalendar">献立表示</a></li>
        <li><a href="<%= ctx %>/user/emergency">緊急連絡先設定</a></li>
        <li><a href="<%= ctx %>/user/pin">認証パスワード設定</a></li>
        <li><a href="<%= ctx %>/user/withdraw">退会</a></li>
        <li><a href="<%= ctx %>/user/logout" style="color:#c00;">ログアウト</a></li>
      </ul>
    </nav>

    <!-- 以降のページ本文は各JSPで <main class="content"> ... を書く -->
