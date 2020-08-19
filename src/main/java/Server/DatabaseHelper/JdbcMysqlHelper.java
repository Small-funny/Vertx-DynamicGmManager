package Server.DatabaseHelper;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 基于JDBC的数据库帮助类
 */
public class JdbcMysqlHelper{

    //数据库连接初始化方法
//    private static Connection initJdbc() {
//        Connection conn = null;
//        try {
//            long starttime = System.currentTimeMillis();
//            System.out.println("数据库连接计时开始");
//            Class.forName("com.mysql.cj.jdbc.Driver");
//            System.out.println("连接数据库...");
//            conn = DriverManager.getConnection("", "", "");
//            long endtime = System.currentTimeMillis();
//            System.out.println("数据库连接运行时间：" + (endtime - starttime) + "ms");
//            return conn;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

    //登录查询，是否允许用户登录
    public static boolean isExisted(String username, String password) {
        boolean result = false;
        Connection conn = MysqlDBPool.getConnection();
        try {

            System.out.println("实例化PreparedStatement对象...");
            PreparedStatement stmt = conn.prepareStatement("select username from user where password = '" + password
                    + "' and username = '" + username +"'");
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                result = true;
            }
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    //查询用户对应的权限
    public static List<String> selectAuthority(String token) {
        List<String> result = new ArrayList<>();
        Connection conn = MysqlDBPool.getConnection();
        try {
            System.out.println("实例化PreparedStatement对象...");


            PreparedStatement stmt = conn.prepareStatement("select auth.name from auth, role_auth, user_role, user where" +
                    " auth.id = role_auth.auth_id and user_role.role_id = role_auth.role_id" +
                    " and user_role.user_id = user.id and user.token = '" + token + "'");

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                result.add(rs.getString("auth.name"));
            }
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static boolean tokenIsExisted(String token) {
        boolean result = false;
        Connection conn = MysqlDBPool.getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement("select token from user where token = '" + token
                    + "'");
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                result = true;
            }
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    // 适合增删改三种操作
    public static void execute(String sql) {
        Connection conn = MysqlDBPool.getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.execute();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
