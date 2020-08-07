/**
 * RSA 加密算法封装，
 * 对外提供密钥的生成、加密、解密
 * 本模块测试时使用的是：node-rsa@1.0.1
 */

let NodeRSA = require("node-rsa");

export var RSAUtil = {
    /**
     * 生成 RSA 密钥对
     * @returns 以 PEM 格式返回 公钥与私钥 的组合对象
     */
    getRSAKeyPair: function () {
        // 生成空对象
        let keyPair = new NodeRSA();
        keyPair.setOptions({
            encryptionScheme:"pkcs1"
        })
        // keyPairObj, 保存经 BASE64 编码处理之后 PEM 格式的 RSA 密钥对
        let keyPairObj = {
            publicKey: '',
            privateKey: ''
        };
        // keysize: 2048; 公指数为：65537
        keyPair.generateKeyPair(1024, 65537);
        /**
         * 导出密钥，对输出的密钥做一些格式化处理，以便 Java 端能直接使用，算然经过处理但是并不影响 JS 端的密钥导入，及正确性。
         * 1. 公钥
         * 2. 私钥
         */
        keyPairObj.publicKey = keyPair.exportKey("pkcs8-public-pem").replace(/-----BEGIN PUBLIC KEY-----/, '').replace(/-----END PUBLIC KEY-----/, '').replace(/\n/g, '');
        keyPairObj.privateKey = keyPair.exportKey("pkcs8-private-pem").replace(/-----BEGIN PRIVATE KEY-----/, '').replace(/-----END PRIVATE KEY-----/, '').replace(/\n/g, '');

        return keyPairObj;
    },

    /**
     * 公钥加密，加密之后以 BASE64 形式编码
     * @param buffer : 待加密内容 编码格式：utf-8
     * @param publicKey: 加密使用的公钥, 格式是：pkcs8 pem
     * @param encoding: 加密之后的输出编码类型 默认输出编码格式是：base64
     * @param source_encoding: 指定代加密内容的编码方式，默认是：utf-8
     * @returns: 返回以 BASE64 处理之后的加密内容
     */
    publicKeyEncrypt: function(buffer, pubicKey, encoding = "base64", source_encoding = "utf8"){

        let key = new NodeRSA();
        key.setOptions({
            encryptionScheme:"pkcs1", // 默认是：pkcs1_oaep，Java 端默认是 pkcs1, 这里做个修改
        })
        key.importKey(pubicKey, "pkcs8-public-pem");
        // 加密并返回加密结果
        return key.encrypt(buffer, encoding, source_encoding);
    },

    /**
     * 私钥解密，解密之后 返回 utf8编码的字符串
     * @param buffer: Buffer object or base64 encoded string
     * @param privateKey: 解密用的私钥，格式是：pkcs8 pem
     * @param encoding: 加密之后的类型 buffer OR json, 默认是 buffer
     * @returns：默认返回值类型就是 encoding 的默认值，即 buffer
     */
    privateKeyDecrypt: function(buffer, privateKey, encoding = "buffer"){
        // 导入 privatekey
        let key = new NodeRSA();
        key.setOptions({
            encryptionScheme: "pkcs1", // 默认是：pkcs1_oaep，Java 端默认是 pkcs1, 这里做个修改
        })
        key.importKey(privateKey, "pkcs8-private-pem");
        // 解密
        return key.decrypt(buffer, encoding);
    }
}