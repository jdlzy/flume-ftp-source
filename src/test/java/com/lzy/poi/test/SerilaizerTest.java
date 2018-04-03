package com.lzy.poi.test;


import org.lzy.flume.source.ftp.source.utils.PropertiesUtils;

import java.util.*;

/**
 * Created by taihe on 2018/3/29.
 */
public class SerilaizerTest {
    public static void main(String[] args) {
    Map<String,Integer> map=new HashMap<String,Integer>();
//    map.put("teset",1);
        PropertiesUtils.modif("D:\\test.properties","GSM-CELL");
         List fileTypeList=new ArrayList<String>(Arrays.asList("GSM-SITE","GSM-CELL","WCDMA-SITE","WCDMA-CELL","LTE-SITE","LTE-CELL","PHYSICS","GSM-SCENE","WCDMA-SCENE","LTE-SCENE"));
         System.out.println(fileTypeList.toString());

    }
    }
