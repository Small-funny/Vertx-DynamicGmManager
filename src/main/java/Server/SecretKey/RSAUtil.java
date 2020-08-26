package Server.SecretKey;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * RSA加解密（前后端支持版本）
 *      支持2048位RSA密钥对
 *
 *      前端需要使用RSA公钥（Base64编码）进行加密
 *      后端使用RSA私钥（这里可以是PrivateKey或者Base64编码格式）进行解密
 *
 */
public class RSAUtil {

    private static Cipher cipher;

    private static final String KEY_TYPE = "RSA";

    private static final int KEY_SIZE = 2048;

    static {
        try {
            cipher = Cipher.getInstance(KEY_TYPE);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
        }
    }

    /**
     * 生成密钥对
     *
     * @return
     */
    public static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(KEY_TYPE);
            // 密钥位数
            keyPairGen.initialize(KEY_SIZE);
            // 密钥对
            return keyPairGen.generateKeyPair();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 产生密钥对，并且导出密钥对
     *
     * @param filePath
     * @return
     */
    public static KeyPair generateKeyPair(String filePath) {
        try {
            // 密钥对
            KeyPair keyPair = generateKeyPair();
            // 公钥
            PublicKey publicKey = keyPair.getPublic();
            // 私钥
            PrivateKey privateKey = keyPair.getPrivate();
            //得到公钥字符串
            String publicKeyString = getKeyString(publicKey);
            //得到私钥字符串
            String privateKeyString = getKeyString(privateKey);
            //将密钥对写入到文件
            FileWriter pubfw = new FileWriter(filePath + "/publicKey.keystore");
            FileWriter prifw = new FileWriter(filePath + "/privateKey.keystore");
            BufferedWriter pubbw = new BufferedWriter(pubfw);
            BufferedWriter pribw = new BufferedWriter(prifw);
            pubbw.write(publicKeyString);
            pribw.write(privateKeyString);
            pubbw.flush();
            pubbw.close();
            pubfw.close();
            pribw.flush();
            pribw.close();
            prifw.close();
            //将生成的密钥对返回
            return keyPair;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 得到密钥字符串
     *
     * @param key
     * @return 密钥字符串（Base64编码后）
     * @throws Exception
     */
    public static String getKeyString(Key key) {
        byte[] keyBytes = key.getEncoded();
        return Base64.getEncoder().encodeToString(keyBytes);
    }

    /**
     * 得到公钥
     * Base64编码公钥和PublicKey的转换
     *
     * @param key Base64编码后的公钥字符串
     * @return
     * @throws Exception
     */
    public static PublicKey getPublicKey(String key) throws Exception {
        byte[] keyBytes;
        keyBytes = Base64.getDecoder().decode(key);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_TYPE);
        return keyFactory.generatePublic(keySpec);
    }

    /**
     * 得到私钥
     * Base64编码私钥和PrivateKey的转换
     *
     * @param key Base64编码后的私钥字符串
     * @return
     * @throws Exception
     */
    public static PrivateKey getPrivateKey(String key) throws Exception {
        byte[] keyBytes;
        keyBytes = Base64.getDecoder().decode(key);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_TYPE);
        return keyFactory.generatePrivate(keySpec);
    }

    /**
     * 公钥加密
     *
     * @param publicKey 公钥（Base64编码）
     * @param plainText 明文
     * @return 密文
     */
    public static String encrypt(String publicKey, String plainText) {
        try {
            return encrypt(getPublicKey(publicKey), plainText);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 公钥加密
     *
     * @param publicKey 公钥
     * @param plainText 明文
     * @return 密文
     */
    public static String encrypt(PublicKey publicKey, String plainText) {
        try {
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] enBytes = cipher.doFinal(plainText.getBytes());
            return Base64.getEncoder().encodeToString(enBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 私钥解密
     *
     * @param privateKey 私钥（Base64编码）
     * @param enStr      密文
     * @return 明文
     */
    public static String decrypt(String privateKey, String enStr) {
        try {
            return decrypt(getPrivateKey(privateKey), enStr);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 私钥解密
     *
     * @param privateKey 私钥
     * @param enStr      密文
     * @return 明文
     */
    public static String decrypt(PrivateKey privateKey, String enStr) {
        try {
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] deBytes = cipher.doFinal(Base64.getDecoder().decode(enStr));
            return new String(deBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}