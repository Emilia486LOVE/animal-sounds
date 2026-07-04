import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TestDBConnection {
    public static void main(String[] args) {
        String url = "jdbc:mysql://127.0.0.1:3306/animal_voiceprint?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=utf8";
        String user = "animal_app";
        String password = "password";
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("SUCCESS: Database connection established!");
            conn.close();
        } catch (ClassNotFoundException e) {
            System.out.println("ERROR: Driver not found - " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("ERROR: Connection failed - " + e.getMessage());
            System.out.println("SQL State: " + e.getSQLState());
            System.out.println("Error Code: " + e.getErrorCode());
        }
    }
}