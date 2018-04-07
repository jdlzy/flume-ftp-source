package com.lzy.poi.test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2018/4/7.
 */
public class ParseTest {
    private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    public static void main(String[] args) throws Exception {
test();
    }
    public static void test(){
        String value="2017-11-12";
        List<String> list=new ArrayList<String>();
        List<Date> list2=new ArrayList<>();

        list.add(value);
        list.add("cuowu");
        for(String str:list){
            try {
                Date resultdate=format.parse(str);
//                System.out.println(resultdate);
                list2.add(resultdate);
            } catch (ParseException e) {
                System.out.println("有错误:"+str);
            }
//                Systemz.out.println("继续执行");
        }
            System.out.println(list2);

    }
    }
