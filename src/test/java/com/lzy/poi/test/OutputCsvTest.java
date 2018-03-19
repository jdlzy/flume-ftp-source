package com.lzy.poi.test;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.lzy.poi.xlsx2csv.poi.XLSX2CSV;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;

/**
 * Created by taihe on 2018/3/19.
 */
public class OutputCsvTest {
    public static void main(String[] args) throws InvalidFormatException {
        XLSX2CSV xlsx2csv=new XLSX2CSV();
        try {
            XLSX2CSV.saveToCSV("E:/工参/湖南3G小区工参.xlsx","E:/output/outputExce2.csv");
        } catch (OpenXML4JException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }


    }


}
