package infra;

import org.mindrot.jbcrypt.BCrypt;

public final class Password {
  private Password() {}
  public static boolean check(String raw, String hash) {
    if (raw == null || hash == null || hash.isEmpty()) return false;
    try { return BCrypt.checkpw(raw, hash); }
    catch (IllegalArgumentException e) { return false; }
  }
  public static String hash(String raw) {
    if (raw == null) throw new IllegalArgumentException("raw is null");
    return BCrypt.hashpw(raw, BCrypt.gensalt());
  }
}
