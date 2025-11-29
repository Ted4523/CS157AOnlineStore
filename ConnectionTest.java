import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionTest {
    public static void main(String[] args) {
        try (Connection conn = DBUtil.getConnection()) {
            System.out.println("Connected");
        } catch (SQLException e) {
            System.err.println("Connection FAILED: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

