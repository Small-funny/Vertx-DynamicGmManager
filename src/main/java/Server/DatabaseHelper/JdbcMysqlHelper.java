package Server.DatabaseHelper;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcMysqlHelper{

    private Connection initJdbc() {
        Connection conn = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("连接数据库...");
            conn = DriverManager.getConnection("jdbc:mysql://cdb-jpp9dqkf.usw.cdb.myqcloud.com:23055/game_manager_test?useSSL=false", "game", "Pxh130529disc-123");
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

    public List<String> selectAuthority(String username) {
        List<String> result = new ArrayList<>();
        Connection conn = this.initJdbc();
        try {
            System.out.println("实例化PreparedStatement对象...");
            PreparedStatement stmt = conn.prepareStatement("select auth.name from auth, role_auth, user_role, user where" +
                    " auth.id = role_auth.auth_id and user_role.role_id = role_auth.role_id" +
                    " and user_role.user_id = user.id and user.username = '" + username + "'");

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                result.add(rs.getString("auth.name"));
            }
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }
}
