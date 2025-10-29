// src/infra/ConnectionFactory.java
package infra;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory {
  private static final String URL  = "jdbc:postgresql://localhost:5432/book";
  private static final String USER = "postgres";
  private static final String PASS = "pass";

  static {
    try { Class.forName("org.postgresql.Driver"); }
    catch (ClassNotFoundException e) { throw new RuntimeException(e); }
  }

  public static Connection getConnection() throws SQLException {
    return DriverManager.getConnection(URL, USER, PASS);
  }
}
