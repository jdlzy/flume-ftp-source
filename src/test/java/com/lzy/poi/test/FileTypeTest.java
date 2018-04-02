package com.lzy.poi.test;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by taihe on 2018/3/12.
 */
public class FileTypeTest {
    public static void main(String[] args)  {
        String fileName="abcd.csv";
//        String fileName="absc.xlsx"
        String[] fileSuffixs=fileName.split("\\.",-1);
        String fileType=fileSuffixs[fileSuffixs.length-1];
        System.out.println(fileType);
         List fileTypeList=new ArrayList<String>(Arrays.asList("GSM-SITE","GSM-CELL","WCDMA-SITE","WCDMA-CELL","LTE-SITE","LTE-CELL","GSM-SCENE","WCDMA-SCENE","LTE-SCENE"));
        System.out.println(fileTypeList.contains("GSM-site".toUpperCase()));
    }}
