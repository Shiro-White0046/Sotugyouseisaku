package infra;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.sql.DataSource;

/**
 * 起動時に allergens_seed.sql を1回だけ実行するリスナー。
 * - seed_registry テーブルで実行済みを管理。
 * - PCマーカー（ローカルファイル）は使用しない。
 * - エラーが発生してもアプリは起動継続。
 */
@javax.servlet.annotation.WebListener
public class AllergensSeedOnceDualGuard implements ServletContextListener {

  private static final String SQL_PATH = "/WEB-INF/sql/allergens_seed.sql";
  private static final String SEED_KEY = "seed_allergens_sql_v2";
  private static final long   LOCK_KEY = 2025110704L;

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    ServletContext ctx = sce.getServletContext();
    try {
      runSeeding(ctx);
    } catch (Exception e) {
      ctx.log("[Seed] ERROR (app will continue): " + e.getMessage(), e);
    }
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    // no-op
  }

  /** メイン処理 */
  private void runSeeding(ServletContext ctx) throws Exception {
    DataSource ds = lookupDataSource(ctx);
    try (Connection con = ds.getConnection()) {
      con.setAutoCommit(false);

      advisoryLock(con);
      ensureRegistry(con);

      if (alreadyRan(con, SEED_KEY)) {
        ctx.log("[Seed] already executed -> skip");
        con.commit();
        return;
      }

      List<String> stmts = loadSqlStatements(ctx, SQL_PATH);
      ctx.log("[Seed] sql loaded: " + stmts.size() + " statements");

      for (String s : stmts) {
        if (s.trim().isEmpty()) continue;
        try (Statement st = con.createStatement()) {
          st.execute(s);
        }
      }

      markRan(con, SEED_KEY, "executed on " + LocalDateTime.now());
      con.commit();
      ctx.log("[Seed] seeding completed successfully");
    }
  }

  private DataSource lookupDataSource(ServletContext ctx) {
    try {
      javax.naming.Context init = new javax.naming.InitialContext();
      return (DataSource) init.lookup("java:comp/env/jdbc/book");
    } catch (Exception e) {
      throw new RuntimeException("DataSource lookup failed", e);
    }
  }

  /** seed_registryテーブルを作成（なければ） */
  private void ensureRegistry(Connection con) throws SQLException {
    try (Statement st = con.createStatement()) {
      st.execute(
        "CREATE TABLE IF NOT EXISTS seed_registry (" +
        "  key TEXT PRIMARY KEY," +
        "  ran_at TIMESTAMP NOT NULL DEFAULT now()," +
        "  detail TEXT" +
        ")"
      );
    }
  }

  private boolean alreadyRan(Connection con, String key) throws SQLException {
    try (PreparedStatement ps = con.prepareStatement("SELECT 1 FROM seed_registry WHERE key = ?")) {
      ps.setString(1, key);
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next();
      }
    }
  }

  private void markRan(Connection con, String key, String detail) throws SQLException {
    try (PreparedStatement ps = con.prepareStatement(
        "INSERT INTO seed_registry(key, detail) VALUES (?, ?)")) {
      ps.setString(1, key);
      ps.setString(2, detail);
      ps.executeUpdate();
    }
  }

  /** PostgreSQL advisory lock (optional) */
  private void advisoryLock(Connection con) {
    try (PreparedStatement ps = con.prepareStatement("SELECT pg_advisory_lock(?)")) {
      ps.setLong(1, LOCK_KEY);
      ps.execute();
    } catch (SQLException e) {
      // PostgreSQL以外では無視
    }
  }

  /** SQLファイルを ; 区切りで読み込む */
  private List<String> loadSqlStatements(ServletContext ctx, String path) throws IOException {
    InputStream in = ctx.getResourceAsStream(path);
    if (in == null) throw new FileNotFoundException("SQL file not found: " + path);

    BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
    StringBuilder sb = new StringBuilder();
    String line;
    while ((line = br.readLine()) != null) {
      if (line.trim().startsWith("--")) continue; // コメント除外
      sb.append(line).append('\n');
    }
    br.close();

    String[] parts = sb.toString().split(";");
    List<String> list = new ArrayList<>();
    for (String p : parts) list.add(p.trim());
    return list;
  }
}
