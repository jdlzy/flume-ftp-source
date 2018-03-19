package com.lzy.poi.test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import org.lzy.poi.xlsx2csv.poi.XLSX2CSV2;

/**
 * Created by taihe on 2018/3/12.
 */
public class ParseExcel {
    public static void main(String[] args) throws EncryptedDocumentException, InvalidFormatException, IOException {

       InputStream inputStream = new FileInputStream("E:/newsssssss.xlsx");
     /*    //InputStream inp = new FileInputStream("C:/Users/H__D/Desktop/workbook.xls");

        Workbook workbook = WorkbookFactory.create(inputStream);

        Sheet sheet = workbook.getSheetAt(0);

        DataFormatter formatter = new DataFormatter();
        for (Row row : sheet) {

                StringBuilder sb = new StringBuilder();

                for (Cell cell : row) {
                    sb.append(formatter.formatCellValue(cell));
                    sb.append(",");
                }
                sb.deleteCharAt(sb.length() - 1);
System.out.println(sb.toString());
            }*/
        try {

            List<String[]> list= XLSX2CSV2.getRecords(inputStream,3);
            List<String> csvList=new ArrayList<String>();
for(String[] records:list){
            StringBuilder sb=new StringBuilder("");

    for (String record:records){


        sb.append(record);
        sb.append(",");
//        System.out.println(record);
    }
    csvList.add(sb.deleteCharAt(sb.length() - 1).toString());
}
for(String record:csvList){
    System.out.println(record);
}
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
