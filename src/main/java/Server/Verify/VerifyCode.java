package Server.Verify;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class VerifyCode {
    // 图片的宽度
    private int width = 100;
    // 图片的高度
    private int height = 40;
    // 验证码字符个数
    private int codeCount = 4;
    // 验证码干扰线数
    private int lineCount = 50;
    // 验证码
    private String code = null;
    // 验证码图片Buffer
    private BufferedImage buffImg = null;

    private char[] codeSequence = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'M', 'N', 'P', 'Q', 'R', 'S',
            'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '2', '3', '4', '5', '6', '7', '8' };

    // 生成随机数
    private Random random = new Random();

    public VerifyCode() {
        this.createCode();
    }

    /**
     *
     * @param width  图片宽
     * @param height 图片高
     */
    public VerifyCode(int width, int height) {
        this.width = width;
        this.height = height;
        this.createCode();
    }

    /**
     *
     * @param width     图片宽
     * @param height    图片高
     * @param codeCount 字符个数
     * @param lineCount 干扰线条数
     */
    public VerifyCode(int width, int height, int codeCount, int lineCount) {
        this.width = width;
        this.height = height;
        this.codeCount = codeCount;
        this.lineCount = lineCount;
        this.createCode();
    }

    public void createCode() {
        int codeX = 0;
        int fontHeight = 0;
        fontHeight = height - 5;// 字体的高度
        codeX = width / (codeCount + 3);// 每个字符的宽度

        // 图像buffer
        buffImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = buffImg.createGraphics();

        // 将图像填充为白色
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        // 创建字体
        ImgFontByte imgFont = new ImgFontByte();
        Font font = imgFont.getFont(fontHeight);
        g.setFont(font);

        // 绘制干扰线
        for (int i = 0; i < lineCount; i++) {
            int xs = getRandomNumber(width);
            int ys = getRandomNumber(height);
            int xe = xs + getRandomNumber(width / 8);
            int ye = ys + getRandomNumber(height / 8);
            g.setColor(getRandomColor());
            g.drawLine(xs, ys, xe, ye);
        }

        StringBuffer randomCode = new StringBuffer();
        // 随机产生验证码字符
        for (int i = 0; i < codeCount; i++) {
            String strRand = String.valueOf(codeSequence[random.nextInt(codeSequence.length)]);
            // 设置字体颜色
            g.setColor(getRandomColor());
            // 设置字体位置
            g.drawString(strRand, (i + 1) * codeX, getRandomNumber(height / 2) + 25);
            randomCode.append(strRand);
        }
        code = randomCode.toString();
    }

    /** 获取随机颜色 */
    private Color getRandomColor() {
        int r = getRandomNumber(200);
        int g = getRandomNumber(200);
        int b = getRandomNumber(200);
        // System.out.println(r);
        // System.out.println(g);
        // System.out.println(b);
        return new Color(r, g, b);
    }

    /** 获取随机数 */
    private int getRandomNumber(int number) {
        return random.nextInt(number);
    }

    public void write(String path) throws IOException {
        OutputStream sos = new FileOutputStream(path);
        this.write(sos);
    }

    public void write(OutputStream sos) throws IOException {
        ImageIO.write(buffImg, "png", sos);
        sos.close();
    }

    public BufferedImage getBuffImg() {
        return buffImg;
    }

    public String getCode() {
        return code;
    }

    /** 字体样式类 */
    class ImgFontByte {
        public Font getFont(int fontHeight) {
            try {
                Font baseFont = Font.createFont(Font.TRUETYPE_FONT,
                        new ByteArrayInputStream(hex2byte(getFontByteStr())));
                return baseFont.deriveFont(Font.PLAIN, fontHeight);
            } catch (Exception e) {
                return new Font("Arial", Font.PLAIN, fontHeight);
            }
        }

        private byte[] hex2byte(String str) {
            if (str == null)
                return null;
            str = str.trim();
            int len = str.length();
            if (len == 0 || len % 2 == 1)
                return null;

            byte[] b = new byte[len / 2];
            try {
                for (int i = 0; i < str.length(); i += 2) {
                    b[i / 2] = (byte) Integer.decode("0x" + str.substring(i, i + 2)).intValue();
                }
                return b;
            } catch (Exception e) {
                return null;
            }
        }

        // 字体文件的十六进制字符串
        private String getFontByteStr() {

            try {

                String fontByteStr = "";
                File file = new File("src/main/java/resources/VerifyCodeFontByte");
                FileInputStream inputStream = new FileInputStream(file);
                int length = inputStream.available();
                byte bytes[] = new byte[length];
                inputStream.read(bytes);
                inputStream.close();
                fontByteStr = new String(bytes, StandardCharsets.UTF_8);
                return fontByteStr;

            } catch (Exception e) {
                e.printStackTrace();
            }

            return "";
        }
    }

    // public static void main(String[] args) throws IOException{
    // File dir = new File("verifies");
    // if (!dir.exists()) {
    // System.out.println("创建目录");
    // dir.mkdir();
    // }
    // VerifyCode instance = new VerifyCode();
    // String verifyCode = instance.getCode();
    // File file = new File(dir, verifyCode + ".jpg");
    // ImageIO.write(instance.getBuffImg(), "jpg", file);
    // System.out.println(instance.getBuffImg().toString());
    // }
}
