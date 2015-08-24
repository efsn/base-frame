package com.codeyn.base.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadOfficeFileUtil {
    private static Logger logger = LoggerFactory.getLogger(ReadOfficeFileUtil.class);

    private ReadOfficeFileUtil() {
    }

    public static ReadOfficeFileUtil instance = null;

    public static ReadOfficeFileUtil getInstance() {
        if (null == instance) {
            instance = new ReadOfficeFileUtil();
        }
        return instance;
    }

    /**
     * 解析execl文件,此方法兼容2003和2007版本的execl Map为一行记录 Map的key就是头行的值
     * 
     * @param execlFilePath
     *            文件路径
     * @return list<Map<String,String>>
     */
    public static List<Map<String, String>> analyticalExeclFile(String execlFilePath) {
        List<Map<String, String>> list = null;
        /** 验证文件是否存在和路径是否为空 */
        boolean bool = ReadOfficeFileUtil.validateFile(execlFilePath);
        if (bool) {
            /** 验证文件格式 */
            Map<String, String> map = ReadOfficeFileUtil.isExcelFormat(execlFilePath);
            if (null != map) {
                String excelVersion = map.get("excelVersion");
                Workbook wb = null;
                InputStream inputStream = null;
                try {
                    inputStream = new FileInputStream(execlFilePath);
                    if ("2003".equals(excelVersion)) {
                        /** Excel版本2003 */
                        wb = new HSSFWorkbook(inputStream);
                    } else {
                        /** Excel版本2007 */
                        wb = new XSSFWorkbook(inputStream);
                    }
                    list = readExcel(wb);
                } catch (FileNotFoundException e) {
                    logger.debug("读取Excel文件错误!!!");
                    e.printStackTrace();
                } catch (IOException e) {
                    logger.debug("创建HSSFWorkbook对象失败!!!");
                    e.printStackTrace();
                } finally {
                    if (null != inputStream) {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        } else {
            logger.debug("文件扩展名错误!!!");
        }
        return list;
    }

    public static List<Map<String, String>> readExcel(Workbook wb, String sheetName) {
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        /** 得到第一个shell */
        Sheet sheet = wb.getSheet(sheetName);

        if (sheet == null) {
            return list;
        }

        /** 得到Excel的行数 */
        int totalRows = sheet.getPhysicalNumberOfRows();
        /** 得到Excel的列数 */
        int totalCells = 0;
        if (totalRows >= 1 && sheet.getRow(0) != null) {
            totalCells = sheet.getRow(0).getPhysicalNumberOfCells();
        }
        /** 循环Excel的行 */
        Row row = null;
        /** 获得头行数据 */
        Row totalRow = sheet.getRow(0);
        Cell totalCell = null;
        for (int r = 1; r < totalRows; r++) {
            row = sheet.getRow(r);
            /** 判断行数据是否为空,如果为空跳过此行 */
            if (row == null) {
                continue;
            }
            Map<String, String> map = new HashMap<String, String>();
            /** 循环Excel的列 */
            for (int c = 0; c < totalCells; c++) {
                /** key */
                totalCell = totalRow.getCell(c);
                /** value */
                Cell cell = row.getCell(c);
                map.put(getCellString(totalCell), getCellString(cell));
            }
            list.add(map);
        }
        return list;
    }

    /**
     * 读取execl文件
     * 
     * @param wb
     */
    public static List<Map<String, String>> readExcel(Workbook wb) {
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        /** 得到第一个shell */
        Sheet sheet = wb.getSheetAt(0);
        /** 得到Excel的行数 */
        int totalRows = sheet.getPhysicalNumberOfRows();
        /** 得到Excel的列数 */
        int totalCells = 0;
        if (totalRows >= 1 && sheet.getRow(0) != null) {
            totalCells = sheet.getRow(0).getPhysicalNumberOfCells();
        }
        /** 循环Excel的行 */
        Row row = null;
        /** 获得头行数据 */
        Row totalRow = sheet.getRow(0);
        Cell totalCell = null;
        for (int r = 1; r < totalRows; r++) {
            row = sheet.getRow(r);
            /** 判断行数据是否为空,如果为空跳过此行 */
            if (row == null) {
                continue;
            }
            Map<String, String> map = new HashMap<String, String>();
            /** 循环Excel的列 */
            for (int c = 0; c < totalCells; c++) {
                /** key */
                totalCell = totalRow.getCell(c);
                /** value */
                Cell cell = row.getCell(c);
                map.put(getCellString(totalCell), getCellString(cell));
            }
            list.add(map);
        }
        return list;
    }

    /**
     * 把cell的值转换成String
     * 
     * @param cell
     * @return
     */
    public static String getCellString(Cell cell) {
        String value = "";

        if (cell == null) {
            return value;
        }

        int cellType = cell.getCellType();

        switch (cellType) {
            case Cell.CELL_TYPE_BLANK:
                break;

            case Cell.CELL_TYPE_BOOLEAN:
                boolean cellValue = cell.getBooleanCellValue();
                value = String.valueOf(cellValue);
                break;
            case Cell.CELL_TYPE_ERROR:
                byte errorCellValue = cell.getErrorCellValue();
                value = String.valueOf(errorCellValue);
                break;
            case Cell.CELL_TYPE_FORMULA:
                String cellFormula = cell.getCellFormula();
                value = cellFormula;
                break;
            case Cell.CELL_TYPE_NUMERIC:
                double numericCellValue = cell.getNumericCellValue();
                value = String.valueOf(numericCellValue);
                break;
            case Cell.CELL_TYPE_STRING:
                value = cell.getStringCellValue();
                break;

            default:
                break;
        }

        return value;
    }

    /**
     * 判断execl文件的版本
     * 
     * @param filePath
     *            文件完成路径
     * @return Map<String,String>
     */
    public static Map<String, String> isExcelFormat(String filePath) {
        Map<String, String> map = new HashMap<String, String>();

        // boolean fileFormatResult = false;
        /** 检查文件格式2007 */
        if (filePath.matches("^.+\\.(?i)(xlsx)$")) {
            // fileFormatResult = true;
            map.put("excelVersion", "2007");
        }
        /** 检查文件格式2003 */
        if (filePath.matches("^.+\\.(?i)(xls)$")) {
            // fileFormatResult = true;
            map.put("excelVersion", "2003");
        }

        return map;
    }

    /**
     * 验证文件路径是否为空 验证文件是否存在
     * 
     * @param filePath
     *            文件路径
     * @return bool
     */
    public static boolean validateFile(String filePath) {
        boolean bool = false;
        if (StringUtils.isNotBlank(filePath)) {
            File file = new File(filePath);
            if (file.exists()) {
                bool = true;
            } else {
                logger.debug("文件不存在!!!");
            }
        } else {
            logger.debug("文件路径为空!!!");
        }
        return bool;
    }

    public static void main(String[] args) {
        List<Map<String, String>> list = ReadOfficeFileUtil.analyticalExeclFile("C:\\Users\\dell\\Desktop\\商品类目.xlsx");
        System.out.println("总共记录数:" + list.size());
        System.out.println("一级目录||二级目录||三级目录");
        for (int i = 0; i < list.size(); i++) {
            Map<String, String> map = list.get(i);
            System.out.println(map.get("一级类目") + "||" + map.get("二级类目") + "||" + map.get("三级类目"));
        }

    }
}
