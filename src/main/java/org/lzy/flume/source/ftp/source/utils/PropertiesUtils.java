package org.lzy.flume.source.ftp.source.utils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

/**
 * Created by taihe on 2018/3/29.
 */
public class PropertiesUtils {
    public static void modif(String path, String flag) {
        Properties props = new Properties();
//            String path="/software/flume-ng/conf/input_batch.properties";
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
        String year2month = sdf.format(date);
        InputStream in = null;
        FileOutputStream file = null;
        try {
            in = new BufferedInputStream(new FileInputStream(path));
//            BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
            props.load(in);
            String month = props.getProperty("MONTH");
            if(month==null||month.equals("")){
                props.put("MONTH", year2month);
            }else{
            if (month.equals(year2month)) {
                String value=props.getProperty(flag);
                if(value==null||value.equals("")){
                    props.put(flag, "1");
                }else {
                    int index = Integer.valueOf(props.getProperty(flag)) + 1;
                    props.put(flag, String.valueOf(index));
                }
            } else {
                props.put("MONTH", year2month);
                props.put("GSM-SITE", "0");
                props.put("GSM-CELL", "0");
                props.put("WCDMA-SITE", "0");
                props.put("WCDMA-CELL", "0");
                props.put("LTE-SITE", "0");
                props.put("LTE-CELL", "0");
                props.put("PHYSICS", "0");
                props.put("GSM-SCENE", "0");
                props.put("WCDMA-SCENE", "0");
                props.put("LTE-SCENE", "0");
                props.put(flag, "1");
            }}
            file = new FileOutputStream(path);
            props.store(file, "系统配置修改");

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (file != null) {
                try {
                    file.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
