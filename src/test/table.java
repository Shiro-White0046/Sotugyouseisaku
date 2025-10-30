// /src/test/table.java
package test;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

@WebServlet(urlPatterns = {"/test/table"})
public class table extends HttpServlet {

  // 1ページあたりの表示上限
  private static final int MAX_ROWS = 200;

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    request.setCharacterEncoding("UTF-8");
    response.setContentType("text/html; charset=UTF-8");

    try (PrintWriter out = response.getWriter()) {
      out.println("<!DOCTYPE html><html><head><meta charset='UTF-8'>");
      out.println("<title>DB Table Viewer</title>");
      out.println("<style>");
      out.println("body{font-family:system-ui,Segoe UI,Arial; margin:20px;}");
      out.println("h1{font-size:20px; margin:0 0 16px;}");
      out.println("h2{font-size:18px; margin:32px 0 8px;}");
      out.println(".meta{color:#666; font-size:12px; margin-bottom:12px;}");
      out.println("table{border-collapse:collapse; width:100%; margin:8px 0 24px; table-layout:auto;}");
      out.println("th,td{border:1px solid #ccc; padding:6px 8px; vertical-align:top;}");
      out.println("th{background:#f6f7f9;}");
      out.println("tbody tr:nth-child(odd){background:#fafafa;}");
      out.println(".schema{display:inline-block; font-size:12px; color:#555; margin-left:6px;}");
      out.println(".cap{font-size:12px; color:#888;}");
      out.println("</style></head><body>");

      out.println("<h1>DB Table Viewer <span class='schema'>(schema: public, limit: " + MAX_ROWS + ")</span></h1>");

      // ====== 接続 ======
      DataSource ds = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/book");
      try (Connection con = ds.getConnection()) {

        // 1) publicスキーマのベーステーブル一覧
        List<String> tables = loadTables(con);
        if (tables.isEmpty()) {
          out.println("<p class='meta'>publicスキーマにテーブルが見つかりません。</p>");
        }

        // 2) それぞれのテーブルをレンダリング
        for (String table : tables) {
          renderTable(out, con, "public", table);
        }
      } catch (Exception e) {
        out.println("<pre style='color:#b00020'>");
        e.printStackTrace(out);
        out.println("</pre>");
      }

      out.println("</body></html>");
    } catch (NamingException e1) {
		// TODO 自動生成された catch ブロック
		e1.printStackTrace();
	}
  }

  /** publicスキーマのベーステーブル一覧を取得 */
  private List<String> loadTables(Connection con) throws SQLException {
    String sql =
        "SELECT table_name " +
        "FROM information_schema.tables " +
        "WHERE table_schema = 'public' AND table_type = 'BASE TABLE' " +
        "ORDER BY table_name";
    List<String> list = new ArrayList<>();
    try (PreparedStatement ps = con.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
      while (rs.next()) {
        list.add(rs.getString(1));
      }
    }
    return list;
  }

  /** テーブルの内容をHTML出力 */
  private void renderTable(PrintWriter out, Connection con, String schema, String table) {
    out.println("<h2>" + esc(table) + "</h2>");

    // カラム一覧を取得
    List<String> columns = new ArrayList<>();
    String colSql =
        "SELECT column_name " +
        "FROM information_schema.columns " +
        "WHERE table_schema = ? AND table_name = ? " +
        "ORDER BY ordinal_position";
    try (PreparedStatement ps = con.prepareStatement(colSql)) {
      ps.setString(1, schema);
      ps.setString(2, table);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) columns.add(rs.getString(1));
      }
    } catch (SQLException e) {
      out.println("<pre style='color:#b00020'>カラム取得失敗: " + esc(e.getMessage()) + "</pre>");
      return;
    }

    // データを取得（MAX_ROWSまで）
    String dataSql =
        "SELECT * FROM " + quoteIdent(schema) + "." + quoteIdent(table) + " LIMIT " + MAX_ROWS;
    try (PreparedStatement ps = con.prepareStatement(dataSql);
         ResultSet rs = ps.executeQuery()) {

      // ヘッダ
      out.println("<table><thead><tr>");
      for (String c : columns) {
        out.println("<th>" + esc(c) + "</th>");
      }
      out.println("</tr></thead><tbody>");

      // 行
      int rowCount = 0;
      final int colCount = columns.size();
      while (rs.next()) {
        rowCount++;
        out.println("<tr>");
        for (int i = 1; i <= colCount; i++) {
          Object val = rs.getObject(i);
          out.println("<td>" + esc(stringify(val)) + "</td>");
        }
        out.println("</tr>");
      }
      out.println("</tbody></table>");
      out.println("<div class='cap'>rows shown: " + rowCount + (rowCount >= MAX_ROWS ? " (truncated)" : "") + "</div>");

    } catch (SQLException e) {
      out.println("<pre style='color:#b00020'>SELECT失敗: " + esc(e.getMessage()) + "</pre>");
    }
  }

  /** 値を表示用に文字列化（UUID/日付/配列/バイナリ等をそれっぽく） */
  private String stringify(Object val) {
    if (val == null) return "NULL";
    if (val instanceof byte[]) {
      byte[] b = (byte[]) val;
      int len = Math.min(b.length, 24);
      StringBuilder sb = new StringBuilder("bytea[");
      for (int i = 0; i < len; i++) {
        sb.append(String.format("%02x", b[i]));
      }
      if (b.length > len) sb.append("…");
      sb.append("]");
      return sb.toString();
    }
    if (val.getClass().isArray()) {
      // JDBCの配列型（例：text[]）
      try {
        Object[] arr = (Object[]) val;
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < arr.length; i++) {
          if (i > 0) sb.append(", ");
          sb.append(arr[i]);
        }
        sb.append("}");
        return sb.toString();
      } catch (Exception ignore) {}
    }
    return String.valueOf(val);
  }

  /** HTMLエスケープ */
  private String esc(String s) {
    if (s == null) return "";
    return s.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;");
  }

  /** 識別子の引用（シンプル版） */
  private String quoteIdent(String ident) {
    // ダブルクォートで囲み、中のダブルクォートは二重化
    return "\"" + ident.replace("\"", "\"\"") + "\"";
  }
}
