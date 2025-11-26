package dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import bean.Individual;
import bean.User;
import infra.ConnectionFactory;

public class IndividualDAO {

    /* =====================
     *  管理者一覧 DTO
     * ===================== */
    public static class IndividualRow {
        private final UUID id;
        private final String displayName;
        private final UUID userId;
        private final String loginId;
        private final String accountName;
        private final String contacts;   // ★連絡先（複数あれば改行でまとめる）

        public IndividualRow(UUID id, String displayName,
                             UUID userId, String loginId,
                             String accountName, String contacts) {
            this.id = id;
            this.displayName = displayName;
            this.userId = userId;
            this.loginId = loginId;
            this.accountName = accountName;
            this.contacts = contacts;
        }

        public UUID getId() { return id; }
        public String getDisplayName() { return displayName; }
        public UUID getUserId() { return userId; }
        public String getLoginId() { return loginId; }
        public String getAccountName() { return accountName; }
        public String getContacts() { return contacts; }
    }

    /* =============================
     *   ★ findList（連絡先つき）
     * ============================= */
    public List<IndividualRow> findList(UUID orgId, String keyword) {

        String sql =
            "WITH base AS ( " +
            "  SELECT i.id AS ind_id, i.display_name, i.user_id, u.login_id, u.name AS account_name " +
            "  FROM individuals i " +
            "  JOIN users u ON u.id = i.user_id " +
            "  WHERE i.org_id = ? " +
            "    AND (COALESCE(?, '') = '' OR i.display_name ILIKE '%' || ? || '%') " +
            ") " +
            "SELECT b.ind_id, b.display_name, b.user_id, b.login_id, b.account_name, " +
            "       COALESCE(STRING_AGG(uc.label || '：' || uc.phone, E'\\n' ORDER BY uc.created_at), '') AS contacts " +
            "FROM base b " +
            "LEFT JOIN user_contacts uc ON uc.user_id = b.user_id " +
            "GROUP BY b.ind_id, b.display_name, b.user_id, b.login_id, b.account_name " +
            "ORDER BY b.ind_id DESC";

        List<IndividualRow> list = new ArrayList<>();

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            String q = (keyword == null) ? "" : keyword.trim();

            ps.setObject(1, orgId);
            ps.setString(2, q);
            ps.setString(3, q);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {

                    String contacts = rs.getString("contacts");
                    if (contacts == null) contacts = "";

                    list.add(new IndividualRow(
                        rs.getObject("ind_id", UUID.class),
                        rs.getString("display_name"),
                        rs.getObject("user_id", UUID.class),
                        rs.getString("login_id"),
                        rs.getString("account_name"),
                        contacts
                    ));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("individuals一覧（連絡先付き）取得に失敗", e);
        }

        return list;
    }


    /* =============================
     * 以下、あなたの元のコードそのまま残す
     * ============================= */

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
                return rs.getObject("id", UUID.class);
            }

        } catch (SQLException e) {
            throw new RuntimeException("individuals 追加失敗", e);
        }
    }

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
            throw new RuntimeException("個人一括登録失敗", e);
        }
    }

    public List<Individual> listByUser(UUID userId) {
        final String sql =
            "SELECT id, org_id, user_id, display_name, birthday, note, created_at, pin_code_hash " +
            "FROM individuals WHERE user_id = ? ORDER BY created_at ASC";

        List<Individual> list = new ArrayList<>();

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setObject(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Individual i = new Individual();
                    i.setId(rs.getObject("id", UUID.class));
                    i.setOrgId(rs.getObject("org_id", UUID.class));
                    i.setUserId(rs.getObject("user_id", UUID.class));
                    i.setDisplayName(rs.getString("display_name"));

                    Date bd = rs.getDate("birthday");
                    if (bd != null) i.setBirthday(bd.toLocalDate());

                    i.setNote(rs.getString("note"));
                    list.add(i);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("個人一覧取得失敗", e);
        }
        return list;
    }

    public List<Individual> listByOrg(UUID orgId) {
        final String sql =
            "SELECT id, org_id, user_id, display_name, birthday, note, created_at, pin_code_hash, last_verified_at " +
            "FROM individuals WHERE org_id = ? ORDER BY created_at ASC";

        List<Individual> list = new ArrayList<>();

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setObject(1, orgId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Individual i = new Individual();
                    i.setId(rs.getObject("id", UUID.class));
                    i.setOrgId(rs.getObject("org_id", UUID.class));
                    i.setUserId(rs.getObject("user_id", UUID.class));
                    i.setDisplayName(rs.getString("display_name"));

                    Date bd = rs.getDate("birthday");
                    if (bd != null) i.setBirthday(bd.toLocalDate());

                    i.setNote(rs.getString("note"));
                    i.setPinCodeHash(rs.getString("pin_code_hash"));

                    Timestamp ts = rs.getTimestamp("last_verified_at");
                    if (ts != null) {
                        i.setLastVerifiedAt(ts.toInstant().atOffset(java.time.ZoneOffset.UTC));
                    }

                    list.add(i);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("個人一覧取得失敗", e);
        }
        return list;
    }

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
            throw new RuntimeException("個人数取得失敗", e);
        }
    }

    public void rename(UUID individualId, String newDisplayName) {
        final String sql = "UPDATE individuals SET display_name = ? WHERE id = ?";

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, newDisplayName);
            ps.setObject(2, individualId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("個人名更新失敗", e);
        }
    }

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
            throw new RuntimeException("プロフィール更新失敗", e);
        }
    }

    public void delete(UUID individualId) {
        final String sql = "DELETE FROM individuals WHERE id = ?";

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setObject(1, individualId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("個人削除失敗", e);
        }
    }

    public void deleteByUser(UUID userId) {
        final String sql = "DELETE FROM individuals WHERE user_id = ?";

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setObject(1, userId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("個人一括削除失敗", e);
        }
    }

    /* ===== 個別取得系 ===== */

    public Optional<Individual> findById(UUID orgId, UUID individualId) {
        final String sql = "SELECT * FROM individuals WHERE org_id = ? AND id = ?";

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setObject(1, orgId);
            ps.setObject(2, individualId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();

                Individual i = new Individual();

                i.setId(rs.getObject("id", UUID.class));
                i.setOrgId(rs.getObject("org_id", UUID.class));
                i.setUserId(rs.getObject("user_id", UUID.class));
                i.setDisplayName(rs.getString("display_name"));
                i.setNote(rs.getString("note"));

                Date bd = rs.getDate("birthday");
                if (bd != null) i.setBirthday(bd.toLocalDate());

                Timestamp ts = rs.getTimestamp("created_at");
                if (ts != null) {
                    i.setCreatedAt(ts.toInstant().atOffset(java.time.ZoneOffset.UTC));
                }

                i.setPinCodeHash(rs.getString("pin_code_hash"));

                return Optional.of(i);
            }

        } catch (Exception e) {
            throw new RuntimeException("findById 失敗", e);
        }
    }

    /* ===== findOneByOrgIdAndPersonId 系 ===== */

    public Individual findOneByOrgIdAndPersonId(UUID personId) {
        final String sql =
            "SELECT id, org_id, user_id, display_name, birthday, note, created_at, pin_code_hash, last_verified_at " +
            "FROM individuals WHERE id = ? ORDER BY id";

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setObject(1, personId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapIndividual(rs);
                return null;
            }

        } catch (SQLException e) {
            throw new RuntimeException("individual取得（id）失敗", e);
        }
    }

    public Individual findOneByOrgIdAndPersonId(UUID orgId, UUID personId) {
        final String sql =
            "SELECT id, org_id, user_id, display_name, birthday, note, created_at, pin_code_hash, last_verified_at " +
            "FROM individuals WHERE org_id = ? AND id = ? ORDER BY id";

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setObject(1, orgId);
            ps.setObject(2, personId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapIndividual(rs);
                return null;
            }

        } catch (SQLException e) {
            throw new RuntimeException("individual取得（org,id）失敗", e);
        }
    }

    public Individual findOneByUserId(UUID orgId, UUID userId) {
        final String sql =
            "SELECT id, org_id, user_id, display_name, birthday, note, created_at, pin_code_hash, last_verified_at " +
            "FROM individuals WHERE user_id = ? AND org_id = ? ORDER BY id";

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setObject(1, userId);
            ps.setObject(2, orgId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapIndividual(rs);
                return null;
            }

        } catch (SQLException e) {
            throw new RuntimeException("individual取得（user_id）失敗", e);
        }
    }

    public Individual findOneByUser(User user) {
        if (user == null || user.getId() == null) return null;
        return findOneByUserId(user.getOrgId(), user.getId());
    }

    private Individual mapIndividual(ResultSet rs) throws SQLException {
        Individual i = new Individual();

        i.setId(rs.getObject("id", UUID.class));
        i.setOrgId(rs.getObject("org_id", UUID.class));
        i.setUserId(rs.getObject("user_id", UUID.class));
        i.setDisplayName(rs.getString("display_name"));
        i.setBirthday(rs.getObject("birthday", LocalDate.class));
        i.setNote(rs.getString("note"));
        i.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
        i.setPinCodeHash(rs.getString("pin_code_hash"));
        i.setLastVerifiedAt(rs.getObject("last_verified_at", OffsetDateTime.class));

        return i;
    }

    public void updatePin(Individual ind) {
        final String sql = "UPDATE individuals SET pin_code_hash = ? WHERE id = ?";

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, ind.getPinCodeHash());
            ps.setObject(2, ind.getId());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("PIN更新失敗", e);
        }
    }

    public void updatePinHash(Individual ind) {
        final String sql = "UPDATE individuals SET pin_code_hash = ? WHERE id = ?";

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, ind.getPinCodeHash());
            ps.setObject(2, ind.getId());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("PIN更新失敗", e);
        }
    }

    public void updateLastVerifiedAt(UUID individualId, OffsetDateTime when) {
        final String sql = "UPDATE individuals SET last_verified_at = ? WHERE id = ?";

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setObject(1, when);
            ps.setObject(2, individualId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("last_verified_at 更新失敗", e);
        }
    }

    public List<Individual> searchByName(UUID orgId, String keyword) {
        final String sql =
            "SELECT id, org_id, user_id, display_name, birthday, note, created_at, pin_code_hash, last_verified_at " +
            "FROM individuals " +
            "WHERE org_id = ? AND display_name ILIKE ? " +
            "ORDER BY display_name ASC";

        List<Individual> list = new ArrayList<>();

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setObject(1, orgId);
            ps.setString(2, "%" + keyword + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapIndividual(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("名前検索失敗", e);
        }
        return list;
    }

    public List<Individual> searchById(UUID orgId, String keyword) {
        final String sql =
            "SELECT id, org_id, user_id, displayName, birthday, note, created_at, pin_code_hash, last_verified_at " +
            "FROM individuals " +
            "WHERE org_id = ? AND CAST(id AS TEXT) ILIKE ? " +
            "ORDER BY created_at ASC";

        List<Individual> list = new ArrayList<>();

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setObject(1, orgId);
            ps.setString(2, "%" + keyword + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapIndividual(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("ID検索失敗", e);
        }

        return list;
    }

    public Set<Integer> findByPersonIdAsSet(UUID personId) {
        final String sql =
            "SELECT allergen_id FROM individual_allergies WHERE person_id = ?";

        Set<Integer> set = new HashSet<>();

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setObject(1, personId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int allergenId = rs.getInt("allergen_id");
                    if (!rs.wasNull()) set.add(allergenId);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("allergies取得失敗", e);
        }

        return set;
    }
    public void updateBirthdayAndNote(UUID id, LocalDate birthday, String note) {
    	  String sql = "UPDATE individuals SET birthday=?, note=? WHERE id=?";
    	  try (Connection con = ConnectionFactory.getConnection();
    	       PreparedStatement ps = con.prepareStatement(sql)) {

    	    if (birthday != null) {
    	      ps.setObject(1, birthday);
    	    } else {
    	      ps.setNull(1, Types.DATE);
    	    }
    	    ps.setString(2, note);
    	    ps.setObject(3, id);

    	    ps.executeUpdate();
    	  } catch (SQLException e) {
    	    throw new RuntimeException("birthday/note 更新失敗", e);
    	  }
    	}

    public void updateBasicInfo(UUID id, Date birthday, String note) {
    	  String sql = "UPDATE individuals SET birthday = ?, note = ? WHERE id = ?";

    	  try (Connection con = ConnectionFactory.getConnection();
    	       PreparedStatement ps = con.prepareStatement(sql)) {
    	    ps.setObject(1, birthday);
    	    ps.setString(2, note);
    	    ps.setObject(3, id);
    	    ps.executeUpdate();
    	  } catch (SQLException e) {
    	    throw new RuntimeException("individual 更新に失敗しました", e);
    	  }
    	}

}
