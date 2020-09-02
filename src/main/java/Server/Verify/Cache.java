package Server.Verify;

import java.util.HashMap;
import Server.DatabaseHelper.VerifyDatabaseHelper;

public class Cache {

    private static HashMap<String, HashMap<String, String>> formData = new HashMap<>();

    public static void setArgs(String token, HashMap<String, String> args) {
        try {
            String username = VerifyDatabaseHelper.tokenToUsername(token);
            formData.put(username, args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static HashMap<String, String> getArgs(String token) {
        HashMap<String, String> args = null;
        try {
            String username = VerifyDatabaseHelper.tokenToUsername(token);
            args = formData.get(username);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return args;
    }

    public static void removeArgs(String token) {
        try {
            String username = VerifyDatabaseHelper.tokenToUsername(token);
            formData.remove(username);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
