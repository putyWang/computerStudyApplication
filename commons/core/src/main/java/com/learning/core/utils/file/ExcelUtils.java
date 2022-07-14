package com.learning.core.utils.file;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.fastjson.JSONArray;
import com.learning.core.bean.excel.ReadExcelListener;
import com.learning.core.exception.SpringBootException;
import com.learning.core.utils.CollectionUtils;
import com.learning.core.utils.file.FileHelper;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.util.*;

/**
 * Excel 工具类
 *
 * @author felix
 */
public class ExcelUtils {

    private final static String EXCEL_SUFFX_XLSX = "xlsx";

    private final static String EXCEL_SUFFX_XLS = "xls";

    /**
     * 读Excel操作
     *
     * @param file
     * @param cls
     * @param listener
     * @param <T>
     * @return
     */
    public static <T> List<T> readExcel(MultipartFile file, Class<T> cls, ReadExcelListener<T> listener) {
        List<T> data = new ArrayList<T>();
        try {
            EasyExcel.read(file.getInputStream(), cls, listener).sheet().doRead();
            //根据实际业务需求来选择，是否有返回值
            data = listener.getDatas();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    /**
     * 写Excel操作
     *
     * @param response
     * @param fileName
     * @param heads
     * @param dataList
     * @param <T>
     */
    public static <T> void wirteExcel(HttpServletResponse response, String fileName, List<List<String>> heads, List<T> dataList) {
        try {
            EasyExcel.write(getOutputStream(fileName, response)).head(heads).sheet("sheet1").doWrite(dataList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 写Excel操作
     *
     * @param response
     * @param fileName
     * @param cls
     * @param dataList
     * @param <T>
     */
    public static <T> void wirteExcel(HttpServletResponse response, String fileName, Class<T> cls, List<T> dataList) {
        try {
            EasyExcel.write(getOutputStream(fileName, response), cls).sheet("sheet1").doWrite(dataList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 导出文件时为Writer生成OutputStream
     *
     * @param fileName
     * @param response
     * @return
     * @throws Exception
     */
    private static OutputStream getOutputStream(String fileName, HttpServletResponse response)
            throws Exception {
        try {
            fileName = URLEncoder.encode(fileName, "utf-8");
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            //此处指定了文件类型为xls，如果是xlsx的，请自行替换修改
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName + ".xlsx");
            response.setHeader("Pragma", "public");
            response.setHeader("Cache-Control", "no-store");
            response.addHeader("Cache-Control", "max-age=0");
            return response.getOutputStream();
        } catch (IOException e) {
            throw new Exception("导出文件失败！");
        }
    }

    /**
     * 生成Excel 并放到指定位置
     *
     * @param filepath 文件路径(要绝对路径)
     * @param filename 文件名称 (如: demo.xls  记得加.xls)
     * @param heads    表头
     * @param datalist 数据list (这里也可以改成List<Map<String,String>>  格式的数据)
     * @return 是否正常生成
     * @throws IOException
     */
    public static boolean createExcel(String filepath, String filename, List<List<String>> heads, JSONArray datalist) throws IOException {
        boolean success = false;
        try {
            //创建HSSFWorkbook对象(excel的文档对象)
            HSSFWorkbook wb = new HSSFWorkbook();
            // 建立新的sheet对象（excel的表单）
            HSSFSheet sheet = wb.createSheet("sheet1");
            // 在sheet里创建第一行，参数为行索引(excel的行)，可以是0～65535之间的任何一个
            HSSFRow row0 = sheet.createRow(0);
            // 添加表头
            for (int i = 0; i < heads.size(); i++) {
                row0.createCell(i).setCellValue(heads.get(i).get(0));
            }
            //添加表中内容
            for (int row = 0; row < datalist.size(); row++) {
                //创建新行 数据从第二行开始
                HSSFRow newrow = sheet.createRow(row + 1);
                //获取该行的数据
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) datalist.get(row);

                for (int col = 0; col < heads.size(); col++) {
                    //数据从第一列开始
                    //创建单元格并放入数据
                    newrow.createCell(col).setCellValue(data != null && data.get(heads.get(col).get(0)) != null ? String.valueOf(data.get(heads.get(col).get(0))) : "");
                }
            }

            //判断是否存在目录. 不存在则创建
            FileHelper.createDir(filepath);
            //输出Excel文件1
            FileOutputStream output = new FileOutputStream(filepath + filename);
            wb.write(output);
            output.close();
            success = true;
        } catch (Exception e) {
            success = false;
            e.printStackTrace();
        }
        return success;
    }

    /**
     * 获取excel表头
     *
     * @param clazz 实体类
     * @param <T>
     * @return
     * @throws NoSuchFieldException
     */
    public static <T> List<List<String>> getHeads(Class<T> clazz) throws NoSuchFieldException {
        List<List<String>> heads = new ArrayList<>();
        Field[] fields = clazz.getDeclaredFields();
        Field field;
        for (int i = 0; i < fields.length; i++) {
            List<String> column = new ArrayList<>();
            field = clazz.getDeclaredField(fields[i].getName());
            field.setAccessible(true);
            //在实体类属性字段上加上@ExcelProperty注解
            ExcelProperty excelProperty = field.getAnnotation(ExcelProperty.class);
            if (excelProperty != null) {
                String[] values = excelProperty.value();
                StringBuilder value = new StringBuilder();
                for (String v : values) {
                    value.append(v);
                }
                column.add(value.toString());
                heads.add(column);
            }
        }
        return heads;
    }

    public static Map<String, List<Map<String, String>>> readExcel(MultipartFile file) {
        if (file != null) {
            Map<String, List<Map<String, String>>> result = new HashMap<>();
            String fileName = file.getOriginalFilename();
            String fileStyle = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
            List<List<String>> results = new LinkedList<>();
            if (!EXCEL_SUFFX_XLSX.equals(fileStyle)) {
                throw new SpringBootException("请选择Excel类型文件");
            }

            //获取输入流
            InputStream in = null;
            Workbook workbook = null;
            try {
                in = file.getInputStream();
                workbook = new XSSFWorkbook(in);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // 遍历工作表
            int numberOfSheets = workbook.getNumberOfSheets();
            List<Map<String, String>> dataList;
            for (int i = 0; i < numberOfSheets; i++) {
                Sheet sheet = workbook.getSheetAt(i);
                int numberOfRows = sheet.getPhysicalNumberOfRows();
                String sheetName = sheet.getSheetName();
                // 表头信息在excel第二行
                List<String> heads = new ArrayList<>();
                Map<String, String> dataMap;
                dataList = new ArrayList<>();
                int numberOfCells = 0;
                //按行遍历工作表内容
                for (int row = 1; row < numberOfRows; row++) {
                    dataMap = new HashMap<>();
                    //循环获取工作表的每一行
                    Row sheetRow = sheet.getRow(row);
                    if (sheetRow == null) {
                        continue;
                    }

                    if (row == 1) {
                        numberOfCells = sheetRow.getPhysicalNumberOfCells();
                    }
                    //获取每行每一列单元格的值
                    for (int col = 0; col < numberOfCells; col++) {
                        Cell cell = sheetRow.getCell(col);
                        if (cell == null) {
                            continue;
                        }
                        CellType cellTypeEnum = cell.getCellTypeEnum();
                        switch (cellTypeEnum) {
                            case NUMERIC:
                                cell.setCellType(CellType.STRING);
                                break;
                            case STRING:
                                break;
                            default:
//                                throw new BusinessException("单元格格式要求为数值或文本类型");
                        }

                        String value = cell.getStringCellValue();
                        if (row == 1) {
                            heads.add(value);
                        } else {
                            if (col < heads.size()) {
                                dataMap.put(heads.get(col), value);
                            }
                        }
                    }

                    // 获取每一行中所有列的数据
                    if (!CollectionUtils.isEmpty(dataMap)) {
                        dataList.add(dataMap);
                    }
                }

                // 获取一个工作簿中所有行数据
                if (!CollectionUtils.isEmpty(dataList)) {
                    result.put(sheetName, dataList);
                }
            }
            //关闭资源
            try {
                workbook.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // 关闭流
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return result;
        }

        return null;
    }

    public static Map<String, List<Map<String, String>>> readExcel(MultipartFile file, InputStream inputStream) throws IOException {
        if (inputStream != null) {
            Map<String, List<Map<String, String>>> result = new HashMap<>();

            String fileName = file.getOriginalFilename();
            String fileStyle = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
            List<List<String>> results = new LinkedList<>();
            if (!EXCEL_SUFFX_XLSX.equals(fileStyle)) {
                throw new SpringBootException("请选择Excel类型文件");
            }

            //获取输入流
            InputStream in = null;
            Workbook workbook = null;
            try {
//                in = file.getInputStream();
                workbook = new XSSFWorkbook(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // 遍历工作表
            int numberOfSheets = workbook.getNumberOfSheets();
            List<Map<String, String>> dataList;
            for (int i = 0; i < numberOfSheets; i++) {
                Sheet sheet = workbook.getSheetAt(i);
                int numberOfRows = sheet.getPhysicalNumberOfRows();
                String sheetName = sheet.getSheetName();
                // 表头信息在excel第二行
                List<String> heads = new ArrayList<>();
                Map<String, String> dataMap;
                dataList = new ArrayList<>();
                int numberOfCells = 0;
                //按行遍历工作表内容
                for (int row = 1; row < numberOfRows; row++) {
                    dataMap = new HashMap<>();
                    //循环获取工作表的每一行
                    Row sheetRow = sheet.getRow(row);
                    if (sheetRow == null) {
                        continue;
                    }

                    if (row == 1) {
                        numberOfCells = sheetRow.getPhysicalNumberOfCells();
                    }
                    //获取每行每一列单元格的值
                    for (int col = 0; col < numberOfCells; col++) {
                        Cell cell = sheetRow.getCell(col);
                        if (cell == null) {
                            continue;
                        }
                        CellType cellTypeEnum = cell.getCellTypeEnum();
                        switch (cellTypeEnum) {
                            case NUMERIC:
                                cell.setCellType(CellType.STRING);
                                break;
                            case STRING:
                                break;
                            default:
//                                throw new BusinessException("单元格格式要求为数值或文本类型");
                        }

                        String value = cell.getStringCellValue();
                        if (row == 1) {
                            heads.add(value);
                        } else {
                            if (col < heads.size()) {
                                dataMap.put(heads.get(col), value);
                            }
                        }
                    }

                    // 获取每一行中所有列的数据
                    if (!CollectionUtils.isEmpty(dataMap)) {
                        dataList.add(dataMap);
                    }
                }

                // 获取一个工作簿中所有行数据
                if (!CollectionUtils.isEmpty(dataList)) {
                    result.put(sheetName, dataList);
                }
            }
            //关闭资源
            try {
                workbook.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // 关闭流
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return result;
        }

        return null;


    }
}
