import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBUtil {

    private static String url;
    private static String user;
    private static String password;

    static {
        try {
            Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream("app.properties")) {
                props.load(fis);
            }

            url = props.getProperty("db.url");
            user = props.getProperty("db.user");
            password = props.getProperty("db.password");

            Class.forName("com.mysql.cj.jdbc.Driver");

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to load DB configuration or driver", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}
