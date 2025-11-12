package dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import bean.Individual;
import bean.User;
import infra.ConnectionFactory;

/**
 * individuals テーブル用 DAO
 * - user_id: users への FK（ON DELETE CASCADE）
 * - pin_code_hash は必要な時だけ扱う（本DAOでは基本項目を中心に）
 */
public class IndividualDAO {

  /** 1件追加して ID を返す（単体追加用） */
  public UUID addOne(UUID orgId, UUID userId, String displayName) {
    final String sql =
        "INSERT INTO individuals (org_id, user_id, display_name) " +
        "VALUES (?, ?, ?) RETURNING id";

    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

      ps.setObject(1, orgId);
      ps.setObject(2, userId);
      ps.setString(3, displayName);

      try (ResultSet rs = ps.executeQuery()) {
        rs.next();
        return (UUID) rs.getObject("id");
      }
    } catch (SQLException e) {
      throw new RuntimeException("individuals 追加に失敗しました", e);
    }
  }

  /** 複数名を一括登録（空文字は事前に除去して渡してください） */
  public void bulkInsert(UUID orgId, UUID userId, List<String> names) {
    if (names == null || names.isEmpty()) return;

    final String sql =
        "INSERT INTO individuals (org_id, user_id, display_name) VALUES (?, ?, ?)";

    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

      for (String name : names) {
        ps.setObject(1, orgId);
        ps.setObject(2, userId);
        ps.setString(3, name);
        ps.addBatch();
      }
      ps.executeBatch();

    } catch (SQLException e) {
      throw new RuntimeException("individuals の一括登録に失敗しました", e);
    }
  }

  /** ユーザー配下の個人一覧 */
	//dao/IndividualDAO.java に追記
	public java.util.List<bean.Individual> listByUser(java.util.UUID userId) {
	 final String sql = "SELECT id, org_id, user_id, display_name, birthday, note, created_at, pin_code_hash " +
	                    "FROM individuals WHERE user_id = ? ORDER BY created_at ASC";
	 java.util.List<bean.Individual> list = new java.util.ArrayList<>();
	 try (Connection con = ConnectionFactory.getConnection();
	      PreparedStatement ps = con.prepareStatement(sql)) {
	   ps.setObject(1, userId);
	   try (ResultSet rs = ps.executeQuery()) {
	     while (rs.next()) {
	       bean.Individual i = new bean.Individual();
	       i.setId((java.util.UUID) rs.getObject("id"));
	       i.setOrgId((java.util.UUID) rs.getObject("org_id"));
	       i.setUserId((java.util.UUID) rs.getObject("user_id"));
	       i.setDisplayName(rs.getString("display_name"));
	       java.sql.Date d = rs.getDate("birthday");
	       if (d != null) i.setBirthday(d.toLocalDate());
	       i.setNote(rs.getString("note"));
	       list.add(i);
	     }
	   }
	 } catch (SQLException e) {
	   throw new RuntimeException("個人一覧の取得に失敗しました", e);
	 }
	 return list;
	}

	/** 組織コードの個人一覧 */
	//dao/IndividualDAO.java に追記
	public java.util.List<bean.Individual> listByOrg(java.util.UUID orgId) {
	 final String sql = "SELECT * " +
	                    "FROM individuals WHERE org_id = ? ORDER BY created_at ASC";
	 java.util.List<bean.Individual> list = new java.util.ArrayList<>();
	 try (Connection con = ConnectionFactory.getConnection();
	      PreparedStatement ps = con.prepareStatement(sql)) {
	   ps.setObject(1, orgId);
	   try (ResultSet rs = ps.executeQuery()) {
	     while (rs.next()) {
	       bean.Individual i = new bean.Individual();
	       i.setId((java.util.UUID) rs.getObject("id"));
	       i.setOrgId((java.util.UUID) rs.getObject("org_id"));
	       i.setUserId((java.util.UUID) rs.getObject("user_id"));
	       i.setDisplayName(rs.getString("display_name"));
	       java.sql.Date d = rs.getDate("birthday");
	       if (d != null) i.setBirthday(d.toLocalDate());
	       i.setNote(rs.getString("note"));
	       list.add(i);
	     }
	   }
	 } catch (SQLException e) {
	   throw new RuntimeException("個人一覧の取得に失敗しました", e);
	 }
	 return list;
	}






  /** 件数（任意） */
  public int countByUser(UUID userId) {
    final String sql = "SELECT COUNT(*) FROM individuals WHERE user_id = ?";

    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

      ps.setObject(1, userId);
      try (ResultSet rs = ps.executeQuery()) {
        rs.next();
        return rs.getInt(1);
      }

    } catch (SQLException e) {
      throw new RuntimeException("個人数の取得に失敗しました", e);
    }
  }

  /** 表示名の変更 */
  public void rename(UUID individualId, String newDisplayName) {
    final String sql = "UPDATE individuals SET display_name = ? WHERE id = ?";

    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

      ps.setString(1, newDisplayName);
      ps.setObject(2, individualId);
      ps.executeUpdate();

    } catch (SQLException e) {
      throw new RuntimeException("個人名の更新に失敗しました", e);
    }
  }

  /** 誕生日・備考の更新（任意で利用） */
  public void updateProfile(UUID individualId, LocalDate birthday, String note) {
    final String sql = "UPDATE individuals SET birthday = ?, note = ? WHERE id = ?";

    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

      if (birthday == null) ps.setNull(1, Types.DATE);
      else ps.setObject(1, Date.valueOf(birthday));
      if (note == null || note.trim().isEmpty()) ps.setNull(2, Types.VARCHAR);
      else ps.setString(2, note.trim());
      ps.setObject(3, individualId);
      ps.executeUpdate();

    } catch (SQLException e) {
      throw new RuntimeException("個人プロフィール更新に失敗しました", e);
    }
  }

  /** 1件削除 */
	//src/dao/IndividualDAO.java に追記
	public void delete(java.util.UUID individualId) {
	 final String sql = "DELETE FROM individuals WHERE id = ?";
	 try (java.sql.Connection con = infra.ConnectionFactory.getConnection();
	      java.sql.PreparedStatement ps = con.prepareStatement(sql)) {
	   ps.setObject(1, individualId);
	   ps.executeUpdate();
	 } catch (java.sql.SQLException e) {
	   throw new RuntimeException("個人レコードの削除に失敗しました", e);
	 }
	}


  /** ユーザー配下をまとめて削除（アカウント削除時など） */
  public void deleteByUser(UUID userId) {
    final String sql = "DELETE FROM individuals WHERE user_id = ?";

    try (Connection con = ConnectionFactory.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

      ps.setObject(1, userId);
      ps.executeUpdate();

    } catch (SQLException e) {
      throw new RuntimeException("個人一括削除に失敗しました", e);
    }
  }

  // ===== 内部共通マッピング =====
//  private Individual mapIndividual(ResultSet rs) throws SQLException {
//    Individual i = new Individual();
//    i.setId((UUID) rs.getObject("id"));
//    i.setOrgId((UUID) rs.getObject("org_id"));
//    i.setUserId((UUID) rs.getObject("user_id"));
//    i.setDisplayName(rs.getString("display_name"));
//    Date bd = rs.getDate("birthday");
//    if (bd != null) i.setBirthday(bd.toLocalDate());
//    i.setNote(rs.getString("note"));
//    i.setPinCodeHash(rs.getString("pin_code_hash"));
//    OffsetDateTime odt = rs.getObject("created_at", OffsetDateTime.class);
//    if (odt != null) i.setCreatedAt(odt);
//    return i;
//  }

//返却DTO（置き場所: src/bean または src/dto）
	public class IndividualRow {
	 private final java.util.UUID id;
	 private final String displayName;
	 private final java.util.UUID userId;
	 private final String loginId;      // users.login_id（6桁）
	 private final String accountName;  // users.name

	 public IndividualRow(java.util.UUID id, String displayName,
	                      java.util.UUID userId, String loginId, String accountName) {
	   this.id = id; this.displayName = displayName; this.userId = userId;
	   this.loginId = loginId; this.accountName = accountName;
	 }
	 public java.util.UUID getId(){ return id; }
	 public String getDisplayName(){ return displayName; }
	 public java.util.UUID getUserId(){ return userId; }
	 public String getLoginId(){ return loginId; }
	 public String getAccountName(){ return accountName; }
	}
	// 追加：一覧（名前検索のみ。keywordが空/nullなら全件）
	public List<IndividualRow> findList(java.util.UUID orgId, String keyword) {
	  String sql =
	    "SELECT i.id, i.display_name, i.user_id, u.login_id, u.name AS account_name " +
	    "FROM individuals i " +
	    "JOIN users u ON u.id = i.user_id " +
	    "WHERE i.org_id = ? AND (COALESCE(?, '') = '' OR i.display_name ILIKE '%' || ? || '%') " +
	    "ORDER BY i.created_at DESC, i.id";

	  List<IndividualRow> list = new ArrayList<>();
	  try (Connection con = ConnectionFactory.getConnection();
	       PreparedStatement ps = con.prepareStatement(sql)) {
	    ps.setObject(1, orgId);
	    String q = (keyword == null) ? "" : keyword.trim();
	    ps.setString(2, q);
	    ps.setString(3, q);
	    try (ResultSet rs = ps.executeQuery()) {
	      while (rs.next()) {
	        list.add(new IndividualRow(
	          (java.util.UUID) rs.getObject("id"),
	          rs.getString("display_name"),
	          (java.util.UUID) rs.getObject("user_id"),
	          rs.getString("login_id"),
	          rs.getString("account_name")
	        ));
	      }
	    }
	  } catch (SQLException e) {
	    throw new RuntimeException("individuals一覧の取得に失敗しました", e);
	  }
	  return list;
	}
	// 追加：個別取得（削除確認画面用）
	public Optional<bean.Individual> findById(UUID orgId, UUID individualId) {
		  final String sql = "SELECT * FROM individuals WHERE org_id = ? AND id = ?";

		  try (Connection con = ConnectionFactory.getConnection();
		       PreparedStatement ps = con.prepareStatement(sql)) {

		    ps.setObject(1, orgId);
		    ps.setObject(2, individualId);

		    try (ResultSet rs = ps.executeQuery()) {
		      if (!rs.next()) return Optional.empty();

		      bean.Individual i = new bean.Individual();
		      i.setId((UUID) rs.getObject("id"));
		      i.setOrgId((UUID) rs.getObject("org_id"));
		      i.setUserId((UUID) rs.getObject("user_id"));
		      i.setDisplayName(rs.getString("display_name"));
		      i.setNote(rs.getString("note"));

		      // ✅ birthday: java.sql.Date → LocalDate へ変換してからセット
		      java.sql.Date bd = rs.getDate("birthday");
		      if (bd != null) {
		        i.setBirthday(bd.toLocalDate());   // ← ここがポイント
		      }

		      // created_at の型はあなたの bean に合わせて選択
		      Timestamp ts = rs.getTimestamp("created_at");
		      if (ts != null) {
		        // ① bean が Instant の場合
		        // i.setCreatedAt(ts.toInstant());

		        // ② bean が OffsetDateTime の場合（UTC想定）
		        // i.setCreatedAt(ts.toInstant().atOffset(java.time.ZoneOffset.UTC));
		      }

		      i.setPinCodeHash(rs.getString("pin_code_hash"));

		      return Optional.of(i);
		    }
		  } catch (Exception e) {
		    throw new RuntimeException("individualの取得に失敗しました", e);
		  }
		}

	// 追加：削除（individual_allergiesはON DELETE CASCADE想定）
	public int delete(java.util.UUID orgId, java.util.UUID individualId) {
	  String sql = "DELETE FROM individuals WHERE org_id = ? AND id = ?";
	  try (Connection con = ConnectionFactory.getConnection();
	       PreparedStatement ps = con.prepareStatement(sql)) {
	    ps.setObject(1, orgId);
	    ps.setObject(2, individualId);
	    return ps.executeUpdate();
	  } catch (SQLException e) {
	    throw new RuntimeException("individualの削除に失敗しました", e);
	  }
	}

	//1件取得（orgID,personId）
	  public Individual findOneByOrgIdAndPersonId(UUID org_id,UUID personId) {
		  final String sql =
		      "SELECT id, org_id, user_id, display_name, birthday, note, created_at, pin_code_hash "
		    + "FROM individuals "
		    + "WHERE org_id = ? and person_id=? "
		    + "ORDER BY id";

		  try (Connection con = ConnectionFactory.getConnection();
		       PreparedStatement ps = con.prepareStatement(sql)) {

		    ps.setObject(1, org_id);
		    ps.setObject(2, personId);

		    try (ResultSet rs = ps.executeQuery()) {
		      if (rs.next()) {
		        return mapIndividual(rs);
		      } else {
		        return null; // ← 見つからなかった場合
		      }
		    }

		  } catch (SQLException e) {
		    throw new RuntimeException("individual取得（user_id）に失敗しました", e);
		  }
		}
	//1件取得（UUID）
	  public Individual findOneByUserId(UUID org_id,UUID userId) {
		  final String sql =
		      "SELECT id, org_id, user_id, display_name, birthday, note, created_at, pin_code_hash "
		    + "FROM individuals "
		    + "WHERE user_id = ? and org_id=? "
		    + "ORDER BY id";

		  try (Connection con = ConnectionFactory.getConnection();
		       PreparedStatement ps = con.prepareStatement(sql)) {

		    ps.setObject(1, userId);
		    ps.setObject(2, org_id);

		    try (ResultSet rs = ps.executeQuery()) {
		      if (rs.next()) {
		        return mapIndividual(rs);
		      } else {
		        return null; // ← 見つからなかった場合
		      }
		    }

		  } catch (SQLException e) {
		    throw new RuntimeException("individual取得（user_id）に失敗しました", e);
		  }
		}

	 // 1件取得（Userオブジェクト）
	  public Individual findOneByUser(User user) {
		  if (user == null || user.getId() == null) return null;
		  return findOneByUserId(user.getOrgId(),user.getId());
		}

	 private Individual mapIndividual(ResultSet rs) throws SQLException {
		  Individual i = new Individual();
		  i.setId(rs.getObject("id", UUID.class));
		  i.setOrgId(rs.getObject("org_id", UUID.class));
		  i.setUserId(rs.getObject("user_id", UUID.class));
		  i.setDisplayName(rs.getString("display_name"));
		  i.setBirthday(rs.getObject("birthday", java.time.LocalDate.class));
		  i.setNote(rs.getString("note"));
		  i.setCreatedAt(rs.getObject("created_at", java.time.OffsetDateTime.class));
		  i.setPinCodeHash(rs.getString("pin_code_hash"));
		  return i;
		}
	 /** 個人ごとの認証パスワードを更新 */
	 public void updatePin(Individual ind) {
	   String sql = "UPDATE individuals SET pin_code_hash = ? WHERE id = ?";
	   try (Connection con = ConnectionFactory.getConnection();
	        PreparedStatement ps = con.prepareStatement(sql)) {
	     ps.setString(1, ind.getPinCodeHash());
	     ps.setObject(2, ind.getId());
	     ps.executeUpdate();
	   } catch (SQLException e) {
	     throw new RuntimeException("認証パスワード更新に失敗しました", e);
	   }
	 }

	// IndividualDAO に追記
	 public void updatePinHash(Individual ind) {
	   final String sql = "UPDATE individuals SET pin_code_hash = ? WHERE id = ?";
	   try (Connection con = ConnectionFactory.getConnection();
	        PreparedStatement ps = con.prepareStatement(sql)) {
	     ps.setString(1, ind.getPinCodeHash());
	     ps.setObject(2, ind.getId());
	     ps.executeUpdate();
	   } catch (SQLException e) {
	     throw new RuntimeException("PIN更新に失敗", e);
	   }
	 }


}

