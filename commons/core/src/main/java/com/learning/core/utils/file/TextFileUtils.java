package com.learning.core.utils.file;

import com.alibaba.fastjson.JSONObject;
import com.learning.core.utils.JsonFormatTool;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

/**
 * txt文件工具类
 * @author wangpenghui
 * @createTime 2021年06月29日 15:00:00
 */
@Slf4j
public class TextFileUtils {

    public static String readString(InputStream inputStream) {
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            StringBuilder content = new StringBuilder();
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                content.append(line);
            }
            return content.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String readString(File file) {
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
            StringBuilder content = new StringBuilder();
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                content.append(line);
            }
            return content.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 写入文件
     */
    public static void writeString(OutputStream outputStream,String content){
        BufferedOutputStream Buff = null;
        try {
            Buff = new BufferedOutputStream(outputStream);
            Buff.write(content.getBytes("UTF-8"));
            Buff.flush();
            Buff.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                Buff.close();
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 写入文件
     */
    public static void writeString(File file,String content){

        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new FileWriter(file));
            bufferedWriter.write(content);
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally{
            try {
                //关闭流释放资源
                bufferedWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 读取json类型的文件
     * @param systemSetting
     * @param c
     * @param <T>
     * @return
     */
    public static  <T> T readJson(File systemSetting,Class<T> c) {
        String content = readString(systemSetting);
        return JSONObject.parseObject(content,c);
    }

    /**
     * 写入json类型的文件
     * @param systemSetting
     * @param o
     */
    public static void writeJson(File systemSetting,Object o) {
        String jsonString = JSONObject.toJSONString(o);
        writeString(systemSetting,jsonString);
    }

    public static void main(String[] args) {
//        String str = "test";
//        List<String> list = new ArrayList<>();
//        list.add(str);
//        list.add(str);
//        list.add(str);
//        list.add(str);
//        writeJson(new File("D:\\test.txt"), list);

        Object o = readJson(new File("D:\\test.txt"), Object.class);
        // 格式化json字符串
        String jsonString = JsonFormatTool.formatJson(o.toString());
        writeString(new File("D:\\test2.txt"), jsonString);
        System.out.println(o.toString());
    }
}
