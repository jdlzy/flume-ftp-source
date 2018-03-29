package org.lzy.flume.source.ftp.source.utils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

/**
 * Created by taihe on 2018/3/29.
 */
public class PropertiesUtils {
    public static void modif(String path,String flag){
        Properties props=new Properties();
//            String path="/software/flume-ng/conf/input_batch.properties";
        Date date=new Date();
          SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
String year2month=sdf.format(date);
        InputStream in=null;
        FileOutputStream file=null;
        try {
              in=new BufferedInputStream(new FileInputStream(path));
//            BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
            props.load(in);
            String month=props.getProperty("month");
            if(month.equals(year2month)){
                int index=Integer.valueOf(props.getProperty(flag))+1;
                props.put(flag,String.valueOf(index));
            }
            else{
                props.put("month",year2month);
                props.put("GC1","1");
                props.put("GC2","1");
                props.put("GC3","1");
                props.put("GC4","1");
                props.put("GC5","1");
                props.put("GC6","1");
                props.put("GC7","1");
                props.put("CJ1","1");
            }
             file = new FileOutputStream(path);
            props.store(file,"系统配置修改");

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(in!=null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(file!=null) {
                try {
                    file.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
