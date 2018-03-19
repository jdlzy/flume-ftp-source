package org.lzy.poi.xlsx2csv.poi.monitor;

import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;

import java.util.concurrent.TimeUnit;

/**
 * Created by taihe on 2018/3/19.
 */
public class FileMonitor {
    public static void moveFile(String listenerFir,String outputPath){

        String dir="E:/input";
        //轮询时间,3秒
        long interval= TimeUnit.SECONDS.toMillis(3);
        FileAlterationObserver observer=new FileAlterationObserver(listenerFir);
        observer.addListener(new FileListener(outputPath));
        //创建文件变化监听器
        FileAlterationMonitor monitor=new FileAlterationMonitor(interval,observer);
        //开始监控
        try {
            monitor.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    }
