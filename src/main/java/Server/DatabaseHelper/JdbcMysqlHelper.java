package Server.DatabaseHelper;

import java.sql.*;

public class JdbcMysqlHelper{

    private Connection initJdbc() {
        Connection conn = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("连接数据库...");
            conn = DriverManager.getConnection("xxxxx", "xxxxx", "xxxx");
            return conn;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isExisted(String username, String password) {
        boolean result = false;
        Connection conn = this.initJdbc();
        try {
            System.out.println("实例化PreparedStatement对象...");
            PreparedStatement stmt = conn.prepareStatement("select username from user where password = '" + password
                    + "' and username = '" + username +"'");
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                result = true;
            }
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }
}
