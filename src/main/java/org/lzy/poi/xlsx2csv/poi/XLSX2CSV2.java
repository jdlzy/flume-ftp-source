package org.lzy.poi.xlsx2csv.poi;


import org.apache.poi.openxml4j.exceptions.OpenXML4JException;


import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;






import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.util.SAXHelper;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler.SheetContentsHandler;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
public class XLSX2CSV2 {
    /**
     * Uses the XSSF Event SAX helpers to do most of the work
     * of parsing the Sheet XML, and outputs the contents
     * as a (basic) CSV.
     */
    private List<String[]> rows = new ArrayList<String[]>();



    private final OPCPackage xlsxPackage;

    /**
     * Number of columns to read starting with leftmost
     */
    private int minColumns;

    /**
     * Destination for data
     */
    private class SheetToCSV implements SheetContentsHandler {
        private String[] record;
        private  int minColumns;
        private int thisColumn = 0;
        public SheetToCSV(int minColumns) {
            super();
            this.minColumns = minColumns;
        }

        @Override
        public void startRow(int rowNum) {
            record=new String[this.minColumns];
            // System.out.println("################################:"+rowNum);
        }

        @Override
        public void endRow(int rowNum) {
            thisColumn=0;
            rows.add(this.record);
            //System.out.println("**********************************");

        }

        @Override
        public void cell(String cellReference, String formattedValue, XSSFComment comment) {
            if(thisColumn<this.minColumns)
                record[thisColumn]=formattedValue;
            thisColumn++;
            //System.out.print(formattedValue);
            //System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&");


        }

        @Override
        public void headerFooter(String text, boolean isHeader, String tagName) {
            // Skip, no headers or footers in CSV
        }


    }


    /**
     * Creates a new XLSX -> CSV converter
     *
     * @param pkg        The XLSX package to process
     * @param minColumns The minimum number of columns to output, or -1 for no minimum
     */
    public XLSX2CSV2(OPCPackage pkg, int minColumns) {
        this.xlsxPackage = pkg;
        this.minColumns = minColumns;
    }

    /**
     * Parses and shows the content of one sheet
     * using the specified styles and shared-strings tables.
     *
     * @param styles
     * @param strings
     * @param sheetInputStream
     */
    public void processSheet(StylesTable styles,  ReadOnlySharedStringsTable strings, SheetContentsHandler sheetHandler,InputStream sheetInputStream)
            throws IOException, ParserConfigurationException, SAXException {
        DataFormatter formatter = new DataFormatter();
        InputSource sheetSource = new InputSource(sheetInputStream);
        try {
            XMLReader sheetParser = SAXHelper.newXMLReader();
            ContentHandler handler = new XSSFSheetXMLHandler(styles, null, strings, sheetHandler, formatter, false);
            sheetParser.setContentHandler(handler);
            sheetParser.parse(sheetSource);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException("SAX parser appears to be broken - " + e.getMessage());
        }
    }

    /**
     * Initiates the processing of the XLS workbook file to CSV.
     *
     * @throws IOException
     * @throws OpenXML4JException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    public  List<String[]> process() throws IOException, OpenXML4JException, ParserConfigurationException, SAXException {
        ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(this.xlsxPackage);
        XSSFReader xssfReader = new XSSFReader(this.xlsxPackage);
        StylesTable styles = xssfReader.getStylesTable();
        XSSFReader.SheetIterator iter = (XSSFReader.SheetIterator) xssfReader.getSheetsData();
        int index = 0;
        while (iter.hasNext()) {
            InputStream stream = iter.next();
            String sheetName = iter.getSheetName();
            //this.output.println();
            //this.output.println(sheetName + " [index=" + index + "]:");
            processSheet(styles, strings, new SheetToCSV(this.minColumns), stream);
            stream.close();
            ++index;
        }
        return this.rows;
    }

    /**
     * 得到excel的记录
     * @param inputStream
     * @param minColumns 输出多少列
     * @return
     * @throws Exception
     */
    public static List<String[]> getRecords(InputStream inputStream,int minColumns) throws Exception{
//        File xlsxFile = new File(excelPath);
//        if (!xlsxFile.exists()) {
//            System.err.println("Not found or not a file: " + xlsxFile.getPath());
//            return null;
//        }
        // The package open is instantaneous, as it should be.
        OPCPackage p = OPCPackage.open(inputStream);
        XLSX2CSV2 xlsx2csv = new XLSX2CSV2(p,  minColumns);
        List<String[]>list=xlsx2csv.process();
        p.close();
        return list;
    }

    public static void main(String[] args) throws Exception {

/*        File xlsxFile = new File("/home/lhy/QA数据20170516210605.xlsx");
        if (!xlsxFile.exists()) {
            System.err.println("Not found or not a file: " + xlsxFile.getPath());
            return;
        }
        // The package open is instantaneous, as it should be.
        OPCPackage p = OPCPackage.open(xlsxFile.getPath(), PackageAccess.READ);
        XLSX2CSV xlsx2csv = new XLSX2CSV(p,  4);*/
//        List<String[]>list=getRecords("/home/lhy/d3a65e38583640eaa6e81343311f6d38.xls",4);
        // p.close();

//        for(int i=0;i<list.size();i++)
//        {
//            System.out.println("******************:"+i);
//            for(String a:list.get(i))
//            {
//                System.out.println(a);
//                System.out.println("------------------------");
//            }
//            System.out.println("#####################");
//        }
    }
}