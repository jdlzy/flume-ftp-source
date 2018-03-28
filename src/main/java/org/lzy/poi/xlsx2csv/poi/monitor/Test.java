package org.lzy.poi.xlsx2csv.poi.monitor;
import org.lzy.poi.xlsx2csv.poi.monitor.FileMonitor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.lzy.poi.xlsx2csv.poi.monitor.FileListener;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by taihe on 2018/3/19.
 */
public class Test {
    public static void main(String[] args) {
//        String in="/home/lzy1/";
//        String out="/home/lzy2/";
//        FileMonitor.moveFile(in, out);
//        System.out.println(Integer.valueOf("12.8"));
        Date date=new Date();
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM");
        System.out.println(sdf.format(date));
    }
}
