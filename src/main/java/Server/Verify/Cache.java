package Server.Verify;

import java.util.HashMap;

import Server.DatabaseHelper.DatabaseHelper;

public class Cache {

    private static HashMap<String, String> formData = new HashMap<>();

    public static void setArgs(String token, String arg) {
        try {
            String username = DatabaseHelper.tokenToUsername(token);
            formData.put(username, arg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getArgs(String token) {
        String args = null;
        try {
            String username = DatabaseHelper.tokenToUsername(token);
            args = formData.get(username);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return args;
    }
    
}
 