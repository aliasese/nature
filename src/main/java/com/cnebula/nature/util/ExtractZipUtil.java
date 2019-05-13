package com.cnebula.nature.util;

import com.cnebula.nature.ThreadTask.ParseXMLRunableImpl;
import com.cnebula.nature.configuration.DefaultConfiguration;
import com.cnebula.nature.configuration.HibernateConfiguration;
import com.cnebula.nature.dto.Article;
import com.cnebula.nature.entity.Configuration;
import com.cnebula.nature.entity.FileNameEntity;
import com.microsoft.sqlserver.jdbc.SQLServerException;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExtractZipUtil {

    private static XSSFSheet sheet = null;

    static {

        String localSiteInfoXML = Configuration.getProperties().getProperty(DefaultConfiguration.NAME_LOCALSITEINFOXML);
        FileInputStream inp = null;
        try {
            inp = new FileInputStream(localSiteInfoXML);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        XSSFWorkbook sheets = null;
        try {
            sheets = new XSSFWorkbook(inp);
            inp.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        sheet = sheets.getSheetAt(0);
    }

    /*private static int getFromIndex(String fullPath, String str,Integer count) {
        System.out.println(str);
        str = str.contains("\\") ? str += "\\" : str;
        System.out.println(str);
        Matcher slashMatcher = Pattern.compile(str).matcher(fullPath);
        int index = 0;
        while(slashMatcher.find()) {
            index++;
            if(index == count){
                break;
            }
        }
        return index;
    }*/

    public static void unZip(File zipFile) throws Exception, RuntimeException {

        ZipFile zf = new ZipFile(zipFile);
        Enumeration<? extends ZipArchiveEntry> entries = zf.getEntries();

        //Save files of the same issue to a entity.
        List<List<String>> fileNames = new ArrayList<List<String>>();
        FileNameEntity fileNameEntity = new FileNameEntity(fileNames);
        //List<String> fileNameOfSameIssue = new ArrayList<>();
        String firstDirectPath = null;
        String secondDirectPath = null;
        while (entries.hasMoreElements()) {
            ZipArchiveEntry entry = entries.nextElement();
            String fullName = entry.getName();
            //String directPath = fullName.substring(0, getFromIndex(fullName, File.separator, 3));
            String directPath = fullName.substring(0, StringUtils.ordinalIndexOf(fullName, File.separator, 3));

            if (firstDirectPath == null) {
                firstDirectPath = directPath;
            } else {
                secondDirectPath = directPath;
                //List<String> fileNameOfSameIssue = new ArrayList<>();
            }

            if (secondDirectPath == null || firstDirectPath.contentEquals(secondDirectPath)) {
                if (secondDirectPath == null) {
                    List<String> fileNameOfSameIssue = new ArrayList<>();
                    fileNames.add(fileNameOfSameIssue);
                    fileNameOfSameIssue.add(fullName);
                } else {
                    fileNames.get(fileNames.size() - 1).add(fullName);
                }
            } else {
                firstDirectPath = secondDirectPath;
                List<String> fileNameOfSameIssue = new ArrayList<>();
                fileNames.add(fileNameOfSameIssue);
                fileNameOfSameIssue.add(fullName);
            }
        }

        // Start multiple threads to process single file inner zip
        /*Integer taskNum = 0;
        for (int i = 0; i < fileNames.size(); i++) {
            taskNum += fileNames.get(i).size();
        }*/
        int cpu = Runtime.getRuntime().availableProcessors();
        LinkedBlockingQueue<Runnable> lbq = new LinkedBlockingQueue<Runnable>(fileNames.size());
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(cpu, cpu, 200, TimeUnit.SECONDS, lbq);

        try {
            Class.forName(HibernateConfiguration.class.getName(), true, HibernateConfiguration.class.getClassLoader());
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        // Parse XML then check duplication of Article
        for (List<String> fileNameIssue:fileNames){
            threadPoolExecutor.execute(new ParseXMLRunableImpl(zf, fileNameIssue, Configuration.getProperties()));
        }

        threadPoolExecutor.shutdown();
        threadPoolExecutor.awaitTermination(1000L, TimeUnit.HOURS);
        /*while (threadPoolExecutor.awaitTermination(1000L, TimeUnit.HOURS)) {
            System.out.println("================Completed===============");
        }*/
    }

    /**
     * 解压 zip 文件
     *
     * @return 返回 zip 压缩文件里的文件名的 list
     * @throws Exception
     */
    public static void unZip() throws Exception, RuntimeException {
        unZip(new File(Configuration.getProperties().getProperty(DefaultConfiguration.NAME_ZIPFILEDIR)));
    }

    public static void getJtlFullName(Article article, String code) {
        sheet.rowIterator().forEachRemaining((Row row) -> {
            if (row.getRowNum() == 0) {
                return;
            }
            row.cellIterator().forEachRemaining(cell -> {
                int j = cell.getColumnIndex();
                /*if (j == 7) {
                    if (cell != null && cell.toString() != null && cell.toString() != "") {
                        String code_ = cell.toString();
                        String code__ = code_.split("\\.").length > 0 ? code_.split("\\.")[0] : code_;
                        System.out.println(code_ + "  " + code_.split(".")[0]);
                        if (code__.contentEquals(code)) {
                            article.setJournalTitle(row.getCell(0).toString());
                            article.setJabt(row.getCell(5).toString());
                            return;
                        }
                    }
                }*/
                if (j == 7 && (cell != null || cell.toString() != null || cell.toString() != "") && cell.toString().split("\\.")[0].contentEquals(code)) {
                    article.setJournalTitle(row.getCell(0).toString());
                    article.setJabt(row.getCell(5).toString());
                    return;
                }
            });
        });
    }
}