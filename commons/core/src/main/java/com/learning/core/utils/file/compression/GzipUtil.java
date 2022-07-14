package com.learning.core.utils.file.compression;

import com.google.common.base.Charsets;
import com.learning.core.utils.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * GZip工具类
 * zip压缩解压并使用Base64进行编码工具类
 * 调用：
 * 压缩
 *  GZipUtil.compress(str)
 * 解压
 * GZipUtil.uncompressToString(bytes)
 */
public class GzipUtil {
    private static final String TAG = "GzipUtil";
    /**
     * 将字符串进行gzip压缩
     *
     * @param data
     * @return
     */
    public static String compress(String data) {
        if (data == null || data.length() == 0) {
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip;
        try {
            gzip = new GZIPOutputStream(out);
            gzip.write(data.getBytes(Charsets.UTF_8));
            gzip.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Base64.getEncoder().encodeToString(out.toByteArray());
    }

    public static String uncompress(String data) {
        if (StringUtils.isEmpty(data)) {
            return null;
        }
        byte[] decode = Base64.getDecoder().decode(data);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(decode);
        GZIPInputStream gzipStream = null;
        try {
            gzipStream = new GZIPInputStream(in);
            byte[] buffer = new byte[256];
            int n;
            while ((n = gzipStream.read(buffer)) >= 0) {
                out.write(buffer, 0, n);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
                if (gzipStream != null) {
                    gzipStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new String(out.toByteArray(), Charsets.UTF_8);
    }
}
