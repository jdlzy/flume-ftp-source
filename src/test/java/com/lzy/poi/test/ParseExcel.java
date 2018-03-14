package com.lzy.poi.test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellReference;

import com.microsoft.schemas.office.visio.x2012.main.CellType;
import org.keedio.flume.source.ftp.client.poi.XLSX2CSV2;

/**
 * Created by taihe on 2018/3/12.
 */
public class ParseExcel {
    public static void main(String[] args) throws EncryptedDocumentException, InvalidFormatException, IOException {

        InputStream inputStream = new FileInputStream("E:/newnsssssssss.xlsx");
        //InputStream inp = new FileInputStream("C:/Users/H__D/Desktop/workbook.xls");

//        Workbook workbook = WorkbookFactory.create(inputStream);
//        Workbook workbook=new XSSFWorkbook(inputStream);
//
//        Sheet sheet = workbook.getSheetAt(0);
//
//        DataFormatter formatter = new DataFormatter();
//        for (Row row : sheet) {
//            for (Cell cell : row) {
//                CellReference cellRef = new CellReference(row.getRowNum(), cell.getColumnIndex());
//                //单元格名称
//                System.out.print(cellRef.formatAsString());
//                System.out.print(" - ");
//
//                //通过获取单元格值并应用任何数据格式（Date，0.00，1.23e9，$ 1.23等），获取单元格中显示的文本
//                String text = formatter.formatCellValue(cell);
//                System.out.println(text);
//
//            }
//        }
//        try {
//
//            List<String[]> list= XLSX2CSV2.getRecords("E:/newnsssssssss.xlsx",4);
//            List<String> csvList=new ArrayList<String>();
//for(String[] records:list){
//            StringBuilder sb=new StringBuilder("");
//
//    for (String record:records){
//        sb.append(record);
//        sb.append(",");
////        System.out.println(record);
//    }
//    csvList.add(sb.deleteCharAt(sb.length() - 1).toString());
//}
//for(String record:csvList){
//    System.out.println(record);
//}
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}
