package com.learning.core.utils;

import com.learning.core.exception.SpringBootException;

import java.io.*;

public final class SourceFileUtils {
    private static final int BUFFER_SIZE = 2048;
    private static final String NO_FILE_MESSAGE = "文件不存在！";
    public static final String WINDOWS_SEPARATOR = "\\";
    private static final String LINUX_SEPARATOR = "/";

    public SourceFileUtils() {
    }

    public static String getConfigDirPath() throws IOException {
        return getDir("conf");
    }

    public static String getDownloadDirPath() throws IOException {
        return getDir("download");
    }

    public static String getExportDirPath() throws IOException {
        return getDir("export");
    }

    public static String getUploadDirPath() throws IOException {
        return getDir("upload");
    }

    public static String getAttachmentDirPath() throws IOException {
        return getDir("");
    }

    public static String getTempDirPath() throws IOException {
        return getDir("temp");
    }

    public static String getConfigFilePath(String fileName) throws IOException {
        return getConfigDirPath() + File.separator + fileName;
    }

    public static String getDownloadFilePath(String fileName) throws IOException {
        return getDownloadDirPath() + File.separator + fileName;
    }

    public static String getUploadFilePath(String fileName) throws IOException {
        return getUploadDirPath() + File.separator + fileName;
    }

    public static String getExportFilePath(String fileName) throws IOException {
        return getExportDirPath() + File.separator + fileName;
    }

    public static String getTempFilePath(String fileName) throws IOException {
        return getTempDirPath() + File.separator + fileName;
    }

    public static String getDir(String dirName) throws IOException {
        String path = System.getProperty("user.dir") + File.separator + dirName;
        File file = new File(path);
        if (!file.exists()) {
            boolean ret = file.mkdir();
            if (!ret) {
                throw new IOException("创建" + dirName + "目录失败");
            }
        }

        return path;
    }

    public static boolean createDir(String dirName, String path) throws IOException {
        boolean ret = true;
        path = path + File.separator + dirName;
        File file = new File(path);
        if (!file.exists()) {
            ret = file.mkdir();
            if (!ret) {
                throw new IOException("创建" + dirName + "目录失败");
            }
        }

        return ret;
    }

    public static void createFile(File file) {
        if (file.exists() && file.isFile()) {
            file.delete();

            try {
                file.createNewFile();
            } catch (IOException var3) {
                var3.printStackTrace();
            }

        } else {
            File parentFile = file.getParentFile();
            if (parentFile.exists()) {
                if (parentFile.isFile()) {
                    parentFile.delete();
                    parentFile.mkdirs();
                }
            } else {
                parentFile.mkdirs();
            }

            try {
                file.createNewFile();
            } catch (IOException var4) {
                var4.printStackTrace();
            }

        }
    }

    public static void downloadFile(String path, OutputStream outputStream) throws IOException {
        byte[] buffer = new byte[2048];
        InputStream inputStream = getInputStream(path);
        if (inputStream == null) {
            throw new SpringBootException("文件不存在！");
        } else {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

            for(int i = bufferedInputStream.read(buffer); i != -1; i = bufferedInputStream.read(buffer)) {
                outputStream.write(buffer, 0, i);
            }

            inputStream.close();
            bufferedInputStream.close();
        }
    }

    public static InputStream getInputStream(String path) throws IOException {
        File file = new File(path);
        return !file.exists() ? null : new FileInputStream(file);
    }

    public static OutputStream getOutputStream(String path) throws IOException {
        File file = new File(path);
        if (file.exists()) {
            deleteDirectory(file);
        } else {
            File parentFile = file.getParentFile();
            if (parentFile.exists()) {
                if (parentFile.isFile()) {
                    parentFile.delete();
                    parentFile.mkdirs();
                }
            } else {
                parentFile.mkdirs();
            }
        }

        return new FileOutputStream(file);
    }

    public static void deleteDirectory(File file) throws IOException {
        if (file.exists()) {
            if (file.isFile()) {
                file.delete();
            } else {
                File[] listFiles = file.listFiles();
                if (listFiles != null) {
                    File[] var2 = listFiles;
                    int var3 = listFiles.length;

                    for(int var4 = 0; var4 < var3; ++var4) {
                        File file2 = var2[var4];
                        deleteDirectory(file2);
                    }
                }
            }

            file.delete();
            System.out.println("删除成功");
        } else {
            System.out.println("文件不存在！");
        }

    }

    public static String changeFilePath(String path) {
        if (StringUtils.isEmpty(path)) {
            return "";
        } else {
            String filePath = "";
            boolean linuxSeparator = File.separator.equals("/");
            if (linuxSeparator) {
                filePath = path.replace("/", "\\");
            }

            return filePath;
        }
    }

    public static String getProjectFieldDirPath(Integer projectId) throws IOException {
        String path = "upload/attachment/";
        String replacePath = path.replace("/", File.separator);
        path = replacePath + projectId;
        File file = new File(path);
        if (!file.exists()) {
            boolean ret = file.mkdir();
            if (!ret) {
                throw new IOException("创建" + projectId + "项目目录失败");
            }
        }

        return path;
    }

    public static String getAbsFilePath(Integer projectId, String fileName, String filePath) throws IOException {
        String rootPath = getAttachmentDirPath();
        String projectFieldDirPath = getProjectFieldDirPath(projectId);
        filePath = filePath.replace("\\", File.separator);
        String path = rootPath + projectFieldDirPath + File.separator + filePath.replace("\\", File.separator) + File.separator + fileName;
        return path;
    }
}
