package com.learning.core.utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.security.SecureRandom;

/**
 * 验证码相关工具类
 */
public class VerificationCode {
    private final int weight = 110;
    private final int height = 38;
    /**
     * 验证码文本
     */
    private String text;
    /**
     * 基础随机数生成器
     */
    private SecureRandom r = new SecureRandom();
    /**
     * 字体样式
     */
    private final String[] fontNames = new String[]{"宋体", "华文楷体", "黑体", "微软雅黑", "楷体_GB2312"};
    private String codes = "23456789acdefghjkmnopqrstuvwxyzACDEFGHJKMNPQRSTUVWXYZ";
    /**
     * 验证码中字符数量
     */
    private int codeNum = 4;
    private static int TWO_FIVE_FIVE = 255;

    public VerificationCode() {
    }

    /**
     * 生成随机字体颜色
     * @return
     */
    private Color randomColor() {
        int r = this.r.nextInt(150);
        int g = this.r.nextInt(150);
        int b = this.r.nextInt(150);
        return new Color(r, g, b);
    }

    /**
     * 生成随机字体样式
     * @return
     */
    private Font randomFont() {
        int index = this.r.nextInt(this.fontNames.length);
        String fontName = this.fontNames[index];
        int style = this.r.nextInt(4);
        int size = this.r.nextInt(5) + 24;
        return new Font(fontName, style, size);
    }

    /**
     * 获取codes中的随机字符
     * @return
     */
    private char randomChar() {
        int index = this.r.nextInt(this.codes.length());
        return this.codes.charAt(index);
    }

    /**
     * 绘制干扰线
     * @param image
     */
    private void drawLine(BufferedImage image) {
        //设置干扰线数量
        int num = 155;
        Graphics2D g = (Graphics2D)image.getGraphics();

        //绘制多条干扰线
        for(int i = 0; i < num; ++i) {
            int x = this.r.nextInt(this.weight);
            int y = this.r.nextInt(this.height);
            int xl = this.r.nextInt(this.weight);
            int yl = this.r.nextInt(this.height);
            g.setColor(this.getRandColor(160, 200));
            g.drawLine(x, y, x + xl, y + yl);
        }

    }

    /**
     * 创建长为110，高为38的底图
     * @return
     */
    private BufferedImage createImage() {
        //初始化图片长高
        BufferedImage image = new BufferedImage(this.weight, this.height, 1);
        Graphics2D g = (Graphics2D)image.getGraphics();
        //设置填充色
        g.setColor(this.getRandColor(200, 250));
        //将颜色填充到背景
        g.fillRect(0, 0, this.weight, this.height);

        return image;
    }

    /**
     * 获取图片验证码
     * @return
     */
    public BufferedImage getImage() {
        BufferedImage image = this.createImage();
        Graphics2D g = (Graphics2D)image.getGraphics();
        StringBuilder sb = new StringBuilder();
        //设置干扰线
        this.drawLine(image);

        //设置图片验证码
        for(int i = 0; i < this.codeNum; ++i) {
            String s = this.randomChar() + "";

            sb.append(s);
            //设置字符x坐标
            float x = (float)i * 1.0F * (float)this.weight / 4.0F;
            //指定字符字体样式及颜色
            g.setFont(this.randomFont());
            g.setColor(this.randomColor());
            //将字符标注在图片中
            g.drawString(s, x, (float)(this.height - 5));
        }
        //将验证码字符串赋予text
        this.text = sb.toString();

        return image;
    }

    Color getRandColor(int fc, int bc) {
        SecureRandom random = new SecureRandom();
        if (fc > TWO_FIVE_FIVE) {
            fc = TWO_FIVE_FIVE;
        }

        if (bc > TWO_FIVE_FIVE) {
            bc = TWO_FIVE_FIVE;
        }

        int r = fc + random.nextInt(bc - fc);
        int g = fc + random.nextInt(bc - fc);
        int b = fc + random.nextInt(bc - fc);
        return new Color(r, g, b);
    }

    public String getText() {
        return this.text;
    }
}
