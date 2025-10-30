package test;

import java.sql.Connection;
import java.sql.DriverManager;

public class DirectConnectTest {
  public static void main(String[] args) {
    try {
      Class.forName("org.postgresql.Driver");
      try (Connection con = DriverManager.getConnection(
          "jdbc:postgresql://localhost:5432/book", "postgres", "pass")) {
        System.out.println("✅ Direct DB Connect OK");
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
