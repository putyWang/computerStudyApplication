package com.learning.core.utils.file;

import com.learning.core.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 文件帮助类
 */
@Slf4j
public class FileHelper {
    /**
     * IO流缓冲区大小
     */
    private static final int BUFFER_SIZE = 2048;

    private static final String NO_FILE_MESSAGE = "文件不存在！";

    /**
     * 表单填写数据excel导入模板文件夹路径
     */
    public static String getExcelTemplateDir() {
        return System.getProperty("user.dir") + File.separator + "export" + File.separator + "template" + File.separator;
    }

    /**
     * 访视提醒导出文件夹路径
     */
    public static String getVisitExportDir() {
        return System.getProperty("user.dir") + File.separator + "export" + File.separator + "visit" + File.separator;
    }

    public static String getWxQrPath() {
        return System.getProperty("user.dir") + File.separator + "wx" + File.separator + "qr" + File.separator;
    }
    public static String getWxQrUrl() {
        return "wx" + File.separator + "qr" + File.separator;
    }

    /**
     * 附件下载文件夹临时目录
     *
     * @param dirName 文件夹名称
     * @return
     */
    public static String getAttachmentTemporaryDir(String dirName) throws IOException {
        String path = "";

        if (! StringUtils.isBlank(dirName)) {
            path = System.getProperty("user.dir") +
                    File.separator + "attachment" +
                    File.separator + "temporary" +
                    File.separator + dirName +
                    File.separator;
        } else {
            path = System.getProperty("user.dir") +
                    File.separator + "attachment" +
                    File.separator + "temporary" +
                    File.separator;
        }
        FileHelper.createDir(path);
        return path;
    }

    public static String getFormTempDir(String uuid) throws IOException {
        String path = "";

        path = System.getProperty("user.dir") +
                File.separator + "form" +
                File.separator + "temporary" +
                File.separator +
                uuid +
                File.separator;

        FileHelper.createDir(path);
        return path;
    }

    public static String getFormZipDir() throws IOException {
        String path = "";

        path = System.getProperty("user.dir") +
                File.separator + "form" +
                File.separator;

        FileHelper.createDir(path);
        return path;
    }


    /**
     * 病例导入错误信息路径
     * @return
     */
    public static String  getImportErrorInfoPath() throws IOException {
        String path = "";

        path = System.getProperty("user.dir") +
                File.separator + "patImport" +
                File.separator ;

        FileHelper.createDir(path);
        return path;
    }

    /**
     * 访视提醒导出文件路径
     *
     * @param uuid
     * @return
     */
    public static String getVisitFilePath(String uuid) {
        return System.getProperty("user.dir")
                + File.separator + "export" + File.separator + "visit" + File.separator + "visit_" + uuid + ".xlsx";
    }

    /**
     * 表单填写数据导出文件夹路径
     */
    public static String getCrfFillDataExportDir() {
        return System.getProperty("user.dir") + File.separator + "export" + File.separator + "fillData" + File.separator;
    }

    /**
     * 表单填写数据导出文件压缩前路径
     */
    public static String getCrfFillDataExportZipDir(Long exportId) {
        return System.getProperty("user.dir") + File.separator + "export" + File.separator + "fillData" + File.separator + "export_" + exportId + File.separator;
    }

    /**
     * 表单填写数据导出文件路径
     *
     * @param exportId
     * @return
     */
    public static String getCrfFillDataFilePath(Long exportId) {
        return System.getProperty("user.dir")
                + File.separator + "export" + File.separator + "fillData" + File.separator + "export_" + exportId + ".zip";
    }

    /**
     * 质疑记录数据导出文件路径
     *
     * @param visitFormFillId
     * @return
     */
    public static String getDoubtFilePath(Long visitFormFillId) {

        return System.getProperty("user.dir")
                + File.separator + "export" + File.separator + "doubtRecord" + File.separator + "doubt_" + visitFormFillId + ".xlsx";

    }

    /**
     * 文件下载,文件内容输出到流
     *
     * @param path
     * @param outputStream
     * @return
     * @throws IOException
     */
    public static void downloadFile(String path, OutputStream outputStream) throws IOException {

        byte[] buffer = new byte[BUFFER_SIZE];
        InputStream inputStream = getInputStream(path);

        if (inputStream == null) {
            throw new FileNotFoundException(NO_FILE_MESSAGE);
        }
        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
        int i = bufferedInputStream.read(buffer);
        while (i != -1) {
            outputStream.write(buffer, 0, i);
            i = bufferedInputStream.read(buffer);
        }
        inputStream.close();
        bufferedInputStream.close();

    }

    /**
     * 压缩单个文件
     * 将hdfs目录压缩成zip文件
     *
     * @param inputPath
     * @param outputPath
     * @throws IOException
     */
    public static void compressFile(String inputPath, String outputPath) throws IOException {
        File file = new File(inputPath);
        if (!file.exists()) {
            return;
        }
        byte[] buf = new byte[BUFFER_SIZE];
        OutputStream outputStream = getOutputStream(outputPath);
        ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
        zipOutputStream.putNextEntry(new ZipEntry(file.getName()));
        InputStream inputStream = new FileInputStream(file);
        int len;
        while ((len = inputStream.read(buf)) != -1) {
            zipOutputStream.write(buf, 0, len);
        }
        inputStream.close();
        zipOutputStream.closeEntry();
        zipOutputStream.close();
        outputStream.close();

    }

    /**
     * 将存放在sourceFilePath目录下的源文件，打包成fileName名称的zip文件，并存放到zipFilePath路径下
     *
     * @param sourceFilePath :待压缩的文件路径
     * @param zipFilePath    :压缩后文件路径
     * @return
     */
    public static boolean fileToZip(String sourceFilePath, String zipFilePath) {
        boolean flag = false;
        File sourceFile = new File(sourceFilePath);
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        FileOutputStream fos = null;
        ZipOutputStream zos = null;

        if (sourceFile.exists() == false) {
            System.out.println("待压缩的文件目录：" + sourceFilePath + "不存在.");
        } else {
            try {
                File zipFile = new File(zipFilePath);
                if (zipFile.exists()) {
                    deleteDirectory(zipFile);
                } else {
                    File[] sourceFiles = sourceFile.listFiles();
                    if (null == sourceFiles || sourceFiles.length < 1) {
                        System.out.println("待压缩的文件目录：" + sourceFilePath + "里面不存在文件，无需压缩.");
                    } else {
                        fos = new FileOutputStream(zipFile);
                        zos = new ZipOutputStream(new BufferedOutputStream(fos));
                        byte[] bufs = new byte[1024 * 10];
                        for (int i = 0; i < sourceFiles.length; i++) {
                            //创建ZIP实体，并添加进压缩包
                            ZipEntry zipEntry = new ZipEntry(sourceFiles[i].getName());
                            zos.putNextEntry(zipEntry);
                            //读取待压缩的文件并写进压缩包里
                            fis = new FileInputStream(sourceFiles[i]);
                            bis = new BufferedInputStream(fis, 1024 * 10);
                            int read = 0;
                            while ((read = bis.read(bufs, 0, 1024 * 10)) != -1) {
                                zos.write(bufs, 0, read);
                            }
                        }
                        flag = true;
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            } finally {
                //关闭流
                try {
                    if (null != bis) {
                        bis.close();
                    }
                    if (null != zos) {
                        zos.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
        }
        return flag;
    }

    /**
     * 重建目录
     *
     * @param dir
     * @return
     * @throws IOException
     */
    public static boolean rebuildDir(String dir) throws IOException {
        File file = new File(dir);
        // 如果存在文件夹则先删除再重新创建
        if (file.exists()) {
            deleteDirectory(file);
        }
        boolean ret = file.mkdirs();
        if (!ret) {
            throw new IOException("创建" + dir + "目录失败");
        }

        return ret;
    }

    public static boolean createDir(String dir) throws IOException {
        boolean ret = true;
        File file = new File(dir);
        if (!file.exists()) {
            ret = file.mkdirs();
            if (!ret) {
                throw new IOException("创建" + dir + "目录失败");
            }
        }
        return ret;
    }

    /**
     * 获取hdfs输出流
     *
     * @param path
     * @return
     * @throws IOException
     */
    public static OutputStream getOutputStream(String path) throws IOException {
        File file = new File(path);
        if (file.exists()) {
            deleteDirectory(file);
        } else {
            File parent = file.getParentFile();
            if (!parent.exists()) {
                parent.mkdirs();
            }
        }
        return new FileOutputStream(file);
    }

    /**
     * 获取hdfs输入流
     *
     * @param path
     * @return
     * @throws IOException
     */
    public static InputStream getInputStream(String path) throws IOException {
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }
        return new FileInputStream(file);
    }

    /**
     * 删除目录或文件
     *
     * @param file
     * @throws IOException
     */
    public static void deleteDirectory(File file) throws IOException {
        if (file == null) {
            return;
        }

        if (file.exists()) {
            if (file.isFile()) {
                file.delete();
            } else {
                File[] listFiles = file.listFiles();
                for (File file2 : listFiles) {
                    deleteDirectory(file2);
                }
            }
            file.delete();
            System.out.println("删除成功");
        } else {
            System.out.println(NO_FILE_MESSAGE);
        }

    }

    /**
     * 下载excel
     *
     * @param response   HttpServletResponse
     * @param inFileName 类路径下的excel 文件全名
     * @param outFileNam 需要输出的文件名
     */
    public static void downloadExcel(HttpServletResponse response, String inFileName, String outFileNam) {
        InputStream inputStream = null;
        try {
            response.reset();
            //设置输出文件格式
            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment;filename=" + new String(outFileNam.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));
            ServletOutputStream outputStream = response.getOutputStream();
            inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("excleTemplate/" + inFileName);
            byte[] buff = new byte[1024];
            int length;
            while ((length = inputStream.read(buff)) != -1) {
                outputStream.write(buff, 0, length);
            }
            if (outputStream != null) {
                outputStream.flush();
                outputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error("关闭资源出错" + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 将单个文件压缩成zip文件
     *
     * @param inFile
     * @param out
     * @param dir
     * @throws IOException
     */
    public static void doZip(File inFile, ZipOutputStream out, String dir) throws IOException {
        String entryName = null;
        if (!"".equals(dir)) {
            entryName = dir + "/" + inFile.getName();
        } else {
            entryName = inFile.getName();
        }
        ZipEntry entry = new ZipEntry(entryName);
        out.putNextEntry(entry);

        int len = 0;
        byte[] buffer = new byte[1024];
        FileInputStream fis = new FileInputStream(inFile);
        while ((len = fis.read(buffer)) > 0) {
            out.write(buffer, 0, len);
            out.flush();
        }
        out.closeEntry();
        fis.close();
    }

    /**
     * 递归压缩多层文件结构
     *
     * @param inFile
     * @param out
     * @param dir
     * @throws IOException
     */
    public static void doCompress(File inFile, ZipOutputStream out, String dir) throws IOException {
        if (inFile.isDirectory()) {
            File[] files = inFile.listFiles();
            if (files != null && files.length > 0) {
                for (File file : files) {
                    String name = inFile.getName();
                    if (!"".equals(dir)) {
                        name = dir + "/" + name;
                    }
                    doCompress(file, out, name);
                }
            }
        } else {
            doZip(inFile, out, dir);
        }
    }

    /**
     * 批量文件压缩
     *
     * @param srcFile 目录或者单个文件
     * @param zipFile 压缩后的ZIP文件
     */
    public static void doCompress(String srcFile, String zipFile) throws IOException {
        ZipOutputStream out = null;
        try {
            out = new ZipOutputStream(new FileOutputStream(zipFile));
            doCompress(new File(srcFile), out, "");
        } catch (Exception e) {
            throw e;
        } finally {
            //记得关闭资源
            out.close();
        }
    }

    public static void main(String[] args) {
        String sourcePath = "D:\\idea-workplace\\edc_java\\edc_java-master\\edc_java\\boot\\export\\fillData\\export_31";
        String targetPath = "D:\\idea-workplace\\edc_java\\edc_java-master\\edc_java\\boot\\export\\fillData\\export_31.zip";
        try {
            doCompress(sourcePath, targetPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 获取某个文件夹下的所有文件
     *
     * @param file
     * @param vector
     * @return
     */
    public static Vector<File> getPathAllFiles(File file, Vector<File> vector) {
        if (file.isFile()) {
            //如果是文件，直接装载
            System.out.println("在迭代函数中文件" + file.getName() + "大小为：" + file.length());
            vector.add(file);
        } else {
            //文件夹
            File[] files = file.listFiles();
            for (File f : files) {
                //递归
                if (f.isDirectory()) {
                    getPathAllFiles(f, vector);
                } else {
                    System.out.println("在迭代函数中文件" + f.getName() + "大小为：" + f.length());
                    vector.add(f);
                }
            }
        }
        return vector;
    }

    /**
     * 把某个文件路径下面的文件包含文件夹压缩到一个文件下
     *
     * @param file
     * @param rootPath        相对地址
     * @param zipoutputStream
     */
    public static void zipFileFun(File file, String rootPath, ZipOutputStream zipoutputStream) {
        if (file.exists()) {//文件存在才合法
            if (file.isFile()) {
                //定义相关操作流
                FileInputStream fis = null;
                BufferedInputStream bis = null;
                try {
                    //设置文件夹
                    String relativeFilePath = file.getPath().replace(rootPath + File.separator, "");
                    //创建输入流读取文件
                    fis = new FileInputStream(file);
                    bis = new BufferedInputStream(fis, 10 * 1024);
                    //将文件装入zip中，开始打包
                    ZipEntry zipEntry;
                    if (!relativeFilePath.contains("\\")) {
                        zipEntry = new ZipEntry(file.getName());//此处值不能重复，要唯一，否则同名文件会报错
                    } else {
                        zipEntry = new ZipEntry(relativeFilePath);//此处值不能重复，要唯一，否则同名文件会报错
                    }
                    zipoutputStream.putNextEntry(zipEntry);
                    //开始写文件
                    byte[] b = new byte[10 * 1024];
                    int size = 0;
                    while ((size = bis.read(b, 0, 10 * 1024)) != -1) {//没有读完
                        zipoutputStream.write(b, 0, size);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        //读完以后关闭相关流操作
                        if (bis != null) {
                            bis.close();
                        }
                        if (fis != null) {
                            fis.close();
                        }
                    } catch (Exception e2) {
                        System.out.println("流关闭错误！");
                    }
                }
            }
//            else{//如果是文件夹
//                try {
//                    File [] files = file.listFiles();//获取文件夹下的所有文件
//                    for(File f : files){
//                        zipFileFun(f,rootPath, zipoutputStream);
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//            }
        }
    }

    /**
     * InputStream -> File
     *
     * @param inputStream
     * @param file
     * @throws IOException
     */
    public static void copyInputStreamToFile(InputStream inputStream, File file)
            throws IOException {

        try (FileOutputStream outputStream = new FileOutputStream(file)) {

            int read;
            byte[] bytes = new byte[1024];

            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
        }

    }


}