package com.lzy.poi.test;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.lzy.poi.xlsx2csv.poi.XLSX2CSV;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by taihe on 2018/3/12.
 */
public class ParseExcelOntescomm {
    public static void main(String[] args) throws EncryptedDocumentException, InvalidFormatException, IOException {
String path ="E:/2gTests.xlsx";
//OutputStream os=new OutputStream("E:/csv/test");
        InputStream inputStream = new FileInputStream(path);
        //InputStream inp = new FileInputStream("C:/Users/H__D/Desktop/workbook.xls");

//        Workbook workbook = new XSSFWorkbook(inputStream);
//        Sheet sheet = workbook.getSheetAt(0);
//
//        DataFormatter formatter = new DataFormatter();
//        for (Row row : sheet) {
//            if(row.getRowNum()==0){continue;}
//                StringBuilder sb=new StringBuilder();
//
//            for (Cell cell : row) {
//                sb.append(cell.getStringCellValue());
//                sb.append(",");
//            }
//        }
//            sb.deleteCharAt(sb.length()-1);
//            //输出每一行，使用逗号分隔
//            System.out.println(sb);
            OPCPackage p = OPCPackage.open(path, PackageAccess.READ);
            XLSX2CSV xlsx2csv=new XLSX2CSV(p,System.out,-1);
        try {
            xlsx2csv.process();
        } catch (OpenXML4JException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }


    }
}
