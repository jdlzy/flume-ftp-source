package com.lzy.poi.test;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import static org.apache.poi.xssf.usermodel.XSSFWorkbookType.XLSX;

/**
 * Created by taihe on 2018/3/12.
 */
public class ParseExcelOntescomm {
    public static void main(String[] args) throws EncryptedDocumentException, InvalidFormatException, IOException {

        InputStream inputStream = new FileInputStream("E:/2g.xlsx");
        //InputStream inp = new FileInputStream("C:/Users/H__D/Desktop/workbook.xls");

        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);

        DataFormatter formatter = new DataFormatter();
        for (Row row : sheet) {
            if(row.getRowNum()==0){continue;}
                StringBuilder sb=new StringBuilder();

            for (Cell cell : row) {
                sb.append(cell.getStringCellValue());
                sb.append(",");
            }
            sb.deleteCharAt(sb.length()-1);
            //输出每一行，使用逗号分隔
            System.out.println(sb);
        }

    }
}
