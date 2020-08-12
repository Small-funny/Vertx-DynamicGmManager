package Server.SecretKey;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.security.KeyPair;

import static Server.SecretKey.RSAUtil.generateKeyPair;
import static Server.SecretKey.RSAUtil.getKeyString;

/**
 * 基于RSA非对称加密算法
 * 在resources目录下生成一对秘钥
 */
public class WriteRSAKeys {
    public static void main(String[] args){
       try {
           String publicKey = "";
           String privateKey = "";
           try {
               KeyPair keyPair = generateKeyPair();
               publicKey = getKeyString(keyPair.getPublic());
               privateKey = getKeyString(keyPair.getPrivate());
           } catch (Exception e) {
               e.printStackTrace();
           }
           File file = new File("src/main/java/resources/publicKey");
           if (!file.exists()) {
               file.createNewFile();
           }
           System.out.println("公钥开始写入...");
           FileOutputStream publicFileStream = new FileOutputStream("src/main/java/resources/publicKey");
           BufferedOutputStream publicBuffer =new BufferedOutputStream(publicFileStream);
           publicBuffer.write(publicKey.getBytes(),0,publicKey.getBytes().length);
           publicBuffer.flush();
           publicBuffer.close();
           System.out.println("公钥写入完成...");

           System.out.println("私钥开始写入...");
           FileOutputStream privateFileStream = new FileOutputStream("src/main/java/resources/privateKey");
           BufferedOutputStream privateBuffer =new BufferedOutputStream(privateFileStream);
           privateBuffer.write(privateKey.getBytes(),0,privateKey.getBytes().length);
           privateBuffer.flush();
           privateBuffer.close();
           System.out.println("私钥写入完成...");

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
