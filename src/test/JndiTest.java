package test;

import java.sql.Connection;

import javax.naming.InitialContext;
import javax.sql.DataSource;

public class JndiTest {
  public static void main(String[] args) {
    try {
      InitialContext ic = new InitialContext();
      javax.naming.Context env = (javax.naming.Context) ic.lookup("java:comp/env");
      DataSource ds = (DataSource) env.lookup("jdbc/book");
      try (Connection con = ds.getConnection()) {
        System.out.println("✅ JNDI DataSource OK: " + con.getMetaData().getURL());
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
