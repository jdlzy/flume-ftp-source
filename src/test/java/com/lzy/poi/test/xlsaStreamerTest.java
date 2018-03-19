package com.lzy.poi.test;

import com.monitorjbl.xlsx.StreamingReader;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by taihe on 2018/3/16.
 */
public class xlsaStreamerTest {
    public static void main(String[] args) {

        try {
            InputStream inputStream = new FileInputStream("E:/newsssssss.xlsx");
            Workbook wk = StreamingReader.builder().
                    rowCacheSize(100)
                    .bufferSize(4096).open(inputStream);
            Sheet sheet = wk.getSheetAt(0);
            sheet.getFirstRowNum();
            for (Row row : sheet) {
                System.out.println("开始遍历第" + row.getRowNum() + "行数据：");
                //遍历所有的列
                StringBuilder sb=new StringBuilder("");
                for (Cell cell : row) {
                    sb.append(cell.getStringCellValue());
                    sb.append(",");
                }
                sb.deleteCharAt(sb.length()-1);
                System.out.println(sb.toString());
            }
            } catch(FileNotFoundException e){
                e.printStackTrace();
            }
        }
    }
