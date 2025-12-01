import java.sql.*;
import java.util.Scanner;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OnlineStoreApp {

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        new OnlineStoreApp().run();
    }

    private void run() {
        boolean running = true;
        while (running) {
            printMenu();
            int choice = readInt("Choose an option: ");
            switch (choice) {
                case 1 -> viewCustomers();          // SELECT on Customer
                case 2 -> insertCustomer();         // INSERT on Customer
                case 3 -> updateCustomerEmail();    // UPDATE on Customer
                case 4 -> viewProducts();           // SELECT on Product (+ Category)
                case 5 -> updateProductPrice();     // UPDATE on Product
                case 6 -> viewOrders();             // SELECT on Orders (3rd table)
                case 7 -> placeOrderTransactional();
                case 8 -> viewOrderSummary();       // VIEW for reporting
                case 9 -> addOrderItem();           // STORED PROCEDURE for task automation
                case 0 -> running = false;
                default -> System.out.println("Invalid choice. Try again.");
            }
        }
        System.out.println("Goodbye!");
    }

    private void printMenu() {
        System.out.println("\n=== Online Store Menu ===");
        System.out.println("1. View Customers (SELECT)");
        System.out.println("2. Insert Customer (INSERT)");
        System.out.println("3. Update Customer Email (UPDATE)");
        System.out.println("4. View Products (SELECT)");
        System.out.println("5. Update Product Price (UPDATE)");
        System.out.println("6. View Orders (SELECT)");
        System.out.println("7. Place Order (Transaction: COMMIT/ROLLBACK)");
        System.out.println("8. View Order Items Report (VIEW)");
        System.out.println("9. Add Order Item (STORED PROCEDURE)");
        System.out.println("0. Exit");
    }


    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine();
            try {
                return Integer.parseInt(line.trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid integer.");
            }
        }
    }

    private BigDecimal readBigDecimal(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine();
            try {
                return new BigDecimal(line.trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid decimal number (e.g., 19.99).");
            }
        }
    }

    private String readNonEmpty(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            if (!line.isEmpty()) {
                return line;
            }
            System.out.println("Input cannot be empty.");
        }
    }

    private void handleSqlError(String msg, SQLException e) {
        System.err.println(msg);
        System.err.println("SQLState=" + e.getSQLState()
                + " ErrorCode=" + e.getErrorCode());
        System.err.println("Message=" + e.getMessage());
    }
    private void viewCustomers() {
        String sql = "SELECT CustomerID, Name, Email, Address FROM Customer";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\n-- Customers --");
            while (rs.next()) {
                int id = rs.getInt("CustomerID");
                String name = rs.getString("Name");
                String email = rs.getString("Email");
                String addr = rs.getString("Address");
                System.out.printf("%d | %s | %s | %s%n", id, name, email, addr);
            }

        } catch (SQLException e) {
            handleSqlError("Error viewing customers", e);
        }
    }

    private void insertCustomer() {
        String name = readNonEmpty("Customer name: ");
        String email = readNonEmpty("Email: ");
        String address = readNonEmpty("Address: ");

        String sql = "INSERT INTO Customer (Name, Email, Address) VALUES (?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, address);

            int rows = ps.executeUpdate();
            System.out.println("Inserted " + rows + " customer(s).");

        } catch (SQLException e) {
            handleSqlError("Error inserting customer (maybe duplicate email?)", e);
        }
    }

    private void updateCustomerEmail() {
        int id = readInt("Customer ID to update: ");
        String newEmail = readNonEmpty("New email: ");

        String sql = "UPDATE Customer SET Email = ? WHERE CustomerID = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newEmail);
            ps.setInt(2, id);

            int rows = ps.executeUpdate();
            if (rows == 0) {
                System.out.println("No customer found with ID " + id);
            } else {
                System.out.println("Updated email for customer " + id);
            }

        } catch (SQLException e) {
            handleSqlError("Error updating customer email", e);
        }
    }

    private void viewProducts() {
        String sql =
                "SELECT p.ProductID, p.Name, p.Price, p.StockQty, c.Name AS CategoryName " +
                "FROM Product p " +
                "JOIN Category c ON p.CategoryID = c.CategoryID";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\n-- Products --");
            while (rs.next()) {
                System.out.printf("%d | %s | $%s | stock=%d | category=%s%n",
                        rs.getInt("ProductID"),
                        rs.getString("Name"),
                        rs.getBigDecimal("Price"),
                        rs.getInt("StockQty"),
                        rs.getString("CategoryName"));
            }

        } catch (SQLException e) {
            handleSqlError("Error viewing products", e);
        }
    }

    private void updateProductPrice() {
        int productId = readInt("Product ID to update: ");
        BigDecimal newPrice = readBigDecimal("New price: ");

        String sql = "UPDATE Product SET Price = ? WHERE ProductID = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBigDecimal(1, newPrice);
            ps.setInt(2, productId);

            int rows = ps.executeUpdate();
            if (rows == 0) {
                System.out.println("No product found with ID " + productId);
            } else {
                System.out.println("Updated price for product " + productId);
            }

        } catch (SQLException e) {
            handleSqlError("Error updating product price", e);
        }
    }

    private void viewOrders() {
        String sql =
                "SELECT o.OrderID, o.`Date` AS OrderDate, o.TotalPrice, o.Status, " +
                "       c.Name AS CustomerName " +
                "FROM Orders o " +
                "JOIN Customer c ON o.CustomerID = c.CustomerID";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\n-- Orders --");
            while (rs.next()) {
                System.out.printf("%d | %s | customer=%s | $%s | status=%s%n",
                        rs.getInt("OrderID"),
                        rs.getTimestamp("OrderDate"),
                        rs.getString("CustomerName"),
                        rs.getBigDecimal("TotalPrice"),
                        rs.getString("Status"));
            }

        } catch (SQLException e) {
            handleSqlError("Error viewing orders", e);
        }
    }


    private void placeOrderTransactional() {
        System.out.println("\n-- Place Order (Transaction Demo) --");

        int customerId = readInt("Customer ID: ");
        int productId  = readInt("Product ID: ");
        int quantity   = readInt("Quantity: ");

        String checkCustomerSql = "SELECT CustomerID FROM Customer WHERE CustomerID = ?";

        String selectProductSql =
                "SELECT Price, StockQty FROM Product WHERE ProductID = ?";

        String insertOrderSql =
                "INSERT INTO Orders (`Date`, TotalPrice, Status, CustomerID) " +
                "VALUES (?, ?, ?, ?)";

        String insertItemSql =
                "INSERT INTO OrderItem (ItemID, OrderID, Quantity, UnitPrice, ProductID) " +
                "VALUES (?, ?, ?, ?, ?)";

        String updateStockSql =
                "UPDATE Product SET StockQty = StockQty - ? WHERE ProductID = ?";

        String insertTransactionSql =
                "INSERT INTO `Transaction` (`Date`, Amount, OrderID) " +
                "VALUES (?, ?, ?)";

        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false); 

            try (PreparedStatement ps = conn.prepareStatement(checkCustomerSql)) {
                ps.setInt(1, customerId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new RuntimeException("Customer not found: " + customerId);
                    }
                }
            }

            BigDecimal price;
            int stock;

            try (PreparedStatement ps = conn.prepareStatement(selectProductSql)) {
                ps.setInt(1, productId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new RuntimeException("Product not found: " + productId);
                    }
                    price = rs.getBigDecimal("Price");
                    stock = rs.getInt("StockQty");
                }
            }

            if (quantity <= 0) {
                throw new RuntimeException("Quantity must be > 0.");
            }
            if (quantity > stock) {
                throw new RuntimeException("Not enough stock. Available: " + stock);
            }

            BigDecimal total = price.multiply(BigDecimal.valueOf(quantity));

            int newOrderId;
            try (PreparedStatement ps = conn.prepareStatement(
                    insertOrderSql, Statement.RETURN_GENERATED_KEYS)) {

                ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                ps.setBigDecimal(2, total);
                ps.setString(3, "Pending");
                ps.setInt(4, customerId);

                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new RuntimeException("Failed to get new OrderID");
                    }
                    newOrderId = keys.getInt(1);
                }
            }

            int itemId = 1;
            try (PreparedStatement ps = conn.prepareStatement(insertItemSql)) {
                ps.setInt(1, itemId);
                ps.setInt(2, newOrderId);
                ps.setInt(3, quantity);
                ps.setBigDecimal(4, price);
                ps.setInt(5, productId);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(updateStockSql)) {
                ps.setInt(1, quantity);
                ps.setInt(2, productId);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(insertTransactionSql)) {
                ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                ps.setBigDecimal(2, total);
                ps.setInt(3, newOrderId);
                ps.executeUpdate();
            }

            conn.commit();
            System.out.println("Order " + newOrderId + " created successfully.");
            System.out.println("COMMIT executed (Orders + OrderItem + Transaction).");

        } catch (Exception e) {
            if (conn != null) {
                try {
                    System.out.println("Error occurred. Rolling back transaction...");
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Error during rollback: " + ex.getMessage());
                }
            }
            System.err.println("Transaction failed: " + e.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Error closing connection: " + e.getMessage());
                }
            }
        }
    }

    private void viewOrderSummary() {
        String sql = "SELECT OrderID, CustomerName, TotalItems, TotalAmount FROM order_summary";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\n-- Order Items Report --");
            while (rs.next()) {
                System.out.printf("order %d | customer=%s | items=%d | total=$%s%n",
                        rs.getInt("OrderID"),
                        rs.getString("CustomerName"),
                        rs.getInt("TotalItems"),
                        rs.getBigDecimal("TotalAmount"));
            }

        } catch (SQLException e) {
            handleSqlError("Error selecting from view order_summary", e);
        }
    }

    private void addOrderItem() {
        int orderId = readInt("Order ID: ");
        int productId = readInt("Product ID: ");
        int qty = readInt("Quantity: ");

        String sql = "CALL add_order_item(?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            ps.setInt(2, productId);
            ps.setInt(3, qty);

            ps.execute();
            System.out.println("Stored procedure executed successfully.");

        } catch (SQLException e) {
            handleSqlError("Error calling stored procedure add_order_item", e);
        }
    }

}
