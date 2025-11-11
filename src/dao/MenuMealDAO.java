package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import bean.MenuMeal;
import infra.ConnectionFactory;

/**
 * menu_meals 用 DAO
 *
 * 期待スキーマ:
 *   - id UUID PK
 *   - day_id UUID (FK -> menu_days.id)
 *   - slot TEXT  (BREAKFAST / LUNCH / DINNER)
 *   - name TEXT
 *   - description TEXT
 *   - created_at TIMESTAMPTZ (nullable 可)
 *   - updated_at TIMESTAMPTZ (nullable 可)
 *
 * 想定ユニーク制約:
 *   - UNIQUE (day_id, slot)  … upsert前提（無い場合は update→insert の2段構えで処理）
 */
public class MenuMealDAO {

  /** 指定日の全スロットを Map(slot -> MenuMeal) で返す。存在しないスロットは Map に入らない。 */
  public Map<String, MenuMeal> findByDayAsMap(UUID dayId) {
    final String sql =
        "SELECT id, day_id, slot, name, description, created_at, updated_at " +
        "FROM menu_meals WHERE day_id=? " +
        "ORDER BY CASE slot WHEN 'BREAKFAST' THEN 1 WHEN 'LUNCH' THEN 2 WHEN 'DINNER' THEN 3 ELSE 9 END, created_at";

    Map<String, MenuMeal> result = new HashMap<>();
    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

      ps.setObject(1, dayId);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          MenuMeal m = map(rs);
          result.put(m.getSlot(), m);
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("menu_meals の取得に失敗しました(findByDayAsMap)", e);
    }
    return result;
  }

  /** 指定日の指定スロット（BREAKFAST/LUNCH/DINNER）を返す。無ければ empty。 */
  public Optional<MenuMeal> findByDayAndSlot(UUID dayId, String slot) {
    final String sql =
        "SELECT id, day_id, slot, name, description, created_at, updated_at " +
        "FROM menu_meals WHERE day_id=? AND slot=? LIMIT 1";
    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setObject(1, dayId);
      ps.setString(2, slot);
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) return Optional.empty();
        return Optional.of(map(rs));
      }
    } catch (SQLException e) {
      throw new RuntimeException("menu_meals の取得に失敗しました(findByDayAndSlot)", e);
    }
  }

  /**
   * 指定日に1件でも meal があれば 1件返す（優先順：BREAKFAST→LUNCH→DINNER）。無ければ empty。
   * 時間帯未指定アクセス時のフォールバックに使用。
   */
  public Optional<MenuMeal> findAnyByDay(UUID dayId) {
    final String sql =
        "SELECT id, day_id, slot, name, description, created_at, updated_at " +
        "FROM menu_meals WHERE day_id=? " +
        "ORDER BY CASE slot WHEN 'BREAKFAST' THEN 1 WHEN 'LUNCH' THEN 2 WHEN 'DINNER' THEN 3 ELSE 9 END, created_at " +
        "LIMIT 1";
    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setObject(1, dayId);
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) return Optional.empty();
        return Optional.of(map(rs));
      }
    } catch (SQLException e) {
      throw new RuntimeException("menu_meals の取得に失敗しました(findAnyByDay)", e);
    }
  }

  /**
   * 指定 dayId/slot の Meal を upsert して id を返す。
   * name/description をセットし、既存なら更新・無ければ作成する。
   */
  public UUID upsertMeal(UUID dayId, String slot, String name, String description) {
    // まず UPDATE を試みて、更新件数 0 の場合に INSERT する（UNIQUE(day_id,slot) が無くても安全）
    final String update =
        "UPDATE menu_meals SET name=?, description=?, updated_at=NOW() WHERE day_id=? AND slot=?";
    final String insert =
        "INSERT INTO menu_meals (id, day_id, slot, name, description, created_at, updated_at) " +
        "VALUES (?, ?, ?, ?, ?, NOW(), NOW())";

    try (Connection con = ConnectionFactory.getConnection()) {
      con.setAutoCommit(false);
      try {
        UUID id;

        // 既存IDの有無を確認
        Optional<MenuMeal> exists = findByDayAndSlot(dayId, slot);
        if (exists.isPresent()) {
          // UPDATE
          try (PreparedStatement ps = con.prepareStatement(update)) {
            ps.setString(1, name);
            ps.setString(2, description);
            ps.setObject(3, dayId);
            ps.setString(4, slot);
            ps.executeUpdate();
          }
          id = exists.get().getId();
        } else {
          // INSERT
          id = UUID.randomUUID();
          try (PreparedStatement ps = con.prepareStatement(insert)) {
            ps.setObject(1, id);
            ps.setObject(2, dayId);
            ps.setString(3, slot);
            ps.setString(4, name);
            ps.setString(5, description);
            ps.executeUpdate();
          }
        }

        con.commit();
        return id;

      } catch (SQLException e) {
        try { con.rollback(); } catch (SQLException ignore) {}
        throw e;
      } finally {
        try { con.setAutoCommit(true); } catch (SQLException ignore) {}
      }
    } catch (SQLException e) {
      throw new RuntimeException("menu_meals の upsert に失敗しました(upsertMeal)", e);
    }
  }

  /** 指定 dayId/slot の Meal を物理削除（存在しなくてもOK）。 */
  public void deleteByDayAndSlot(UUID dayId, String slot) {
    final String sql = "DELETE FROM menu_meals WHERE day_id=? AND slot=?";
    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setObject(1, dayId);
      ps.setString(2, slot);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("menu_meals の削除に失敗しました(deleteByDayAndSlot)", e);
    }
  }

  // -------------------------------------------------------
  // 共通マッピング
  // -------------------------------------------------------
  private static MenuMeal map(ResultSet rs) throws SQLException {
    MenuMeal m = new MenuMeal();
    m.setId((UUID) rs.getObject("id"));
    m.setDayId((UUID) rs.getObject("day_id"));
    m.setSlot(rs.getString("slot"));
    m.setName(rs.getString("name"));
    m.setDescription(rs.getString("description"));

    // TIMESTAMPTZ -> OffsetDateTime で受ける（null 許容）
    OffsetDateTime cAt = null, uAt = null;
    try { cAt = rs.getObject("created_at", OffsetDateTime.class); } catch (Throwable ignore) {}
    try { uAt = rs.getObject("updated_at", OffsetDateTime.class); } catch (Throwable ignore) {}
    m.setCreatedAt(cAt);
    m.setUpdatedAt(uAt);

    return m;
    }
}
