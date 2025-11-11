package dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import bean.MenuDay;               // id, orgId, menuDate, imagePath, published, createdAt
import infra.ConnectionFactory;

public class MenuDayDAO {

  /** 指定日の menu_days レコードを返す（なければ empty） */
  public Optional<MenuDay> findByDate(UUID orgId, LocalDate date) {
    final String sql =
        "SELECT id, org_id, menu_date, image_path, published, created_at " +
        "FROM menu_days WHERE org_id=? AND menu_date=?";
    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

      ps.setObject(1, orgId);
      ps.setDate(2, Date.valueOf(date));
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) return Optional.empty();
        return Optional.of(map(rs));
      }
    } catch (SQLException e) {
      throw new RuntimeException("menu_days の取得に失敗しました(findByDate)", e);
    }
  }

  /** 主キーで取得（なければ empty） */
  public Optional<MenuDay> find(UUID dayId) {
    final String sql =
        "SELECT id, org_id, menu_date, image_path, published, created_at " +
        "FROM menu_days WHERE id=?";
    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

      ps.setObject(1, dayId);
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) return Optional.empty();
        return Optional.of(map(rs));
      }
    } catch (SQLException e) {
      throw new RuntimeException("menu_days の取得に失敗しました(find)", e);
    }
  }

  /** 指定月のレコード一覧（menu_date 昇順） */
  public List<MenuDay> listMonth(UUID orgId, YearMonth ym) {
    final String sql =
        "SELECT id, org_id, menu_date, image_path, published, created_at " +
        "FROM menu_days WHERE org_id=? AND menu_date BETWEEN ? AND ? " +
        "ORDER BY menu_date";
    LocalDate start = ym.atDay(1);
    LocalDate end = ym.atEndOfMonth();

    List<MenuDay> list = new ArrayList<>();
    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

      ps.setObject(1, orgId);
      ps.setDate(2, Date.valueOf(start));
      ps.setDate(3, Date.valueOf(end));

      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) list.add(map(rs));
      }
    } catch (SQLException e) {
      throw new RuntimeException("menu_days の月次一覧取得に失敗しました(listMonth)", e);
    }
    return list;
  }

  /**
   * 指定月の「その日に朝/昼/夜いずれかの meal が1件でもあるか」を返す。
   * ＝ menu_meals の存在で true。menu_days だけある日は false。
   */
  public Map<LocalDate, Boolean> existsByMonth(UUID orgId, YearMonth ym) {
    Map<LocalDate, Boolean> result = new LinkedHashMap<>();
    LocalDate start = ym.atDay(1);
    LocalDate end = ym.atEndOfMonth();

    // 全日 false で初期化
    for (int d = 1; d <= ym.lengthOfMonth(); d++) {
      result.put(ym.atDay(d), false);
    }

    // menu_days と menu_meals を結合し、meal が1件でもある日だけ true にする
    final String sql =
        "SELECT md.menu_date, COUNT(mm.id) AS cnt " +
        "FROM menu_days md " +
        "LEFT JOIN menu_meals mm ON mm.day_id = md.id " +
        "WHERE md.org_id=? AND md.menu_date BETWEEN ? AND ? " +
        "GROUP BY md.menu_date";

    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

      ps.setObject(1, orgId);
      ps.setDate(2, Date.valueOf(start));
      ps.setDate(3, Date.valueOf(end));

      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          LocalDate date = rs.getDate("menu_date").toLocalDate();
          long cnt = rs.getLong("cnt");
          // meal が1件でもあれば登録済み
          if (cnt > 0) {
            result.put(date, true);
          } else {
            result.put(date, false);
          }
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("menu_meals 存在判定の取得に失敗しました(existsByMonth)", e);
    }
    return result;
  }

  /** 指定日を確保（存在すればその id、なければ作成して id） */
  public UUID ensureDay(UUID orgId, LocalDate date) {
    final String sel = "SELECT id FROM menu_days WHERE org_id=? AND menu_date=?";
    try (Connection con = ConnectionFactory.getConnection()) {
      try (PreparedStatement ps = con.prepareStatement(sel)) {
        ps.setObject(1, orgId);
        ps.setDate(2, Date.valueOf(date));
        try (ResultSet rs = ps.executeQuery()) {
          if (rs.next()) return (UUID) rs.getObject("id");
        }
      }
      final String ins = "INSERT INTO menu_days (org_id, menu_date) VALUES (?, ?) RETURNING id";
      try (PreparedStatement ps = con.prepareStatement(ins)) {
        ps.setObject(1, orgId);
        ps.setDate(2, Date.valueOf(date));
        try (ResultSet rs = ps.executeQuery()) {
          rs.next();
          return (UUID) rs.getObject("id");
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("menu_days の確保に失敗しました(ensureDay)", e);
    }
  }

  /** 指定日の削除（menu_meals 等は FK ON DELETE CASCADE） */
  public void deleteByDate(UUID orgId, LocalDate date) {
    final String sql = "DELETE FROM menu_days WHERE org_id=? AND menu_date=?";
    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setObject(1, orgId);
      ps.setDate(2, Date.valueOf(date));
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("menu_days の削除に失敗しました(deleteByDate)", e);
    }
  }

  /** 指定日の存在チェック（menu_days の行の有無）※必要なら使用 */
  public boolean exists(UUID orgId, LocalDate date) {
    final String sql = "SELECT 1 FROM menu_days WHERE org_id=? AND menu_date=?";
    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setObject(1, orgId);
      ps.setDate(2, Date.valueOf(date));
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next();
      }
    } catch (SQLException e) {
      throw new RuntimeException("menu_days の存在確認に失敗しました(exists)", e);
    }
  }

  /** menu_days.image_path を更新（1日単位の画像を使う場合に使用） */
  public void updateImagePath(UUID dayId, String relPath) {
    final String sql = "UPDATE menu_days SET image_path=? WHERE id=?";
    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setString(1, relPath);
      ps.setObject(2, dayId);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("menu_days の画像パス更新に失敗しました(updateImagePath)", e);
    }
  }

  // ---- 共通マッピング ----
  private static MenuDay map(ResultSet rs) throws SQLException {
    MenuDay d = new MenuDay();
    d.setId((UUID) rs.getObject("id"));
    d.setOrgId((UUID) rs.getObject("org_id"));
    d.setMenuDate(rs.getDate("menu_date").toLocalDate());
    d.setImagePath(rs.getString("image_path"));
    d.setPublished(rs.getBoolean("published"));
    OffsetDateTime odt;
    try {
      odt = rs.getObject("created_at", OffsetDateTime.class);
    } catch (Throwable t) {
      odt = null;
    }
    if (odt != null) d.setCreatedAt(odt);
    return d;
  }
}
