package com.lzy.poi.test;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;

import java.io.IOException;

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

    }}
