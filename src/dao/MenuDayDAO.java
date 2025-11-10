package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import bean.MenuDay;
import infra.ConnectionFactory;

/**
 * menu_days テーブル用 DAO
 * 1日単位の献立（日付・画像・公開状態）
 */
public class MenuDayDAO {

  /** 指定日を取得 */
  public Optional<MenuDay> find(UUID orgId, LocalDate date) {
    final String sql =
        "SELECT id, org_id, menu_date, image_path, published, created_at " +
        "FROM menu_days WHERE org_id = ? AND menu_date = ?";

    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

      ps.setObject(1, orgId);
      ps.setObject(2, date);
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) return Optional.empty();
        return Optional.of(map(rs));
      }
    } catch (SQLException e) {
      throw new RuntimeException("menu_days の検索に失敗しました", e);
    }
  }

  /** 存在しなければ作成して返す（1日1レコード） */
  public MenuDay findOrCreate(UUID orgId, LocalDate date) {
    Optional<MenuDay> existing = find(orgId, date);
    if (existing.isPresent()) return existing.get();

    final String sql =
        "INSERT INTO menu_days (org_id, menu_date, published) VALUES (?, ?, TRUE) " +
        "RETURNING id, org_id, menu_date, image_path, published, created_at";

    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

      ps.setObject(1, orgId);
      ps.setObject(2, date);
      try (ResultSet rs = ps.executeQuery()) {
        rs.next();
        return map(rs);
      }
    } catch (SQLException e) {
      throw new RuntimeException("menu_days の新規作成に失敗しました", e);
    }
  }

  /** 指定月の一覧を取得 */
  public List<MenuDay> listMonth(UUID orgId, YearMonth ym) {
    final String sql =
        "SELECT id, org_id, menu_date, image_path, published, created_at " +
        "FROM menu_days " +
        "WHERE org_id = ? AND menu_date BETWEEN ? AND ? " +
        "ORDER BY menu_date ASC";

    LocalDate first = ym.atDay(1);
    LocalDate last  = ym.atEndOfMonth();
    List<MenuDay> list = new ArrayList<>();

    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

      ps.setObject(1, orgId);
      ps.setObject(2, first);
      ps.setObject(3, last);

      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) list.add(map(rs));
      }
      return list;
    } catch (SQLException e) {
      throw new RuntimeException("menu_days の月次一覧取得に失敗しました", e);
    }
  }

  /** 画像パスの更新 */
  public void updateImage(UUID id, String imagePath) {
    final String sql = "UPDATE menu_days SET image_path = ? WHERE id = ?";
    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

      if (imagePath == null || imagePath.trim().isEmpty())
        ps.setNull(1, Types.VARCHAR);
      else
        ps.setString(1, imagePath);

      ps.setObject(2, id);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("menu_days の画像パス更新に失敗しました", e);
    }
  }

  /** 公開フラグ切り替え */
  public void setPublished(UUID id, boolean published) {
    final String sql = "UPDATE menu_days SET published = ? WHERE id = ?";
    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setBoolean(1, published);
      ps.setObject(2, id);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("menu_days の公開フラグ更新に失敗しました", e);
    }
  }

  /** 削除（1日分の献立を丸ごと削除） */
  public void delete(UUID id) {
    final String sql = "DELETE FROM menu_days WHERE id = ?";
    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setObject(1, id);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("menu_days の削除に失敗しました", e);
    }
  }

  // --- 共通マッピング ---
  private MenuDay map(ResultSet rs) throws SQLException {
    MenuDay m = new MenuDay();
    m.setId((UUID) rs.getObject("id"));
    m.setOrgId((UUID) rs.getObject("org_id"));
    m.setMenuDate(rs.getObject("menu_date", LocalDate.class));
    m.setImagePath(rs.getString("image_path"));
    m.setPublished(rs.getBoolean("published"));
    m.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
    return m;
  }
	//MenuDayDAO.java
	public Optional<MenuDay> findByDate(UUID orgId, LocalDate date) {
	 final String sql = "SELECT * FROM menu_days WHERE org_id = ? AND menu_date = ? LIMIT 1";
	 try (Connection con = ConnectionFactory.getConnection();
	      PreparedStatement ps = con.prepareStatement(sql)) {
	   ps.setObject(1, orgId);
	   ps.setObject(2, date);
	   try (ResultSet rs = ps.executeQuery()) {
	     if (rs.next()) return Optional.of(map(rs));
	     else return Optional.empty();
	   }
	 } catch (SQLException e) {
	   throw new RuntimeException("menu_days 取得に失敗しました", e);
	 }
	}

}
