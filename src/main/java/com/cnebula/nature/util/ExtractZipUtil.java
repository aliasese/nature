package com.cnebula.nature.util;

import com.cnebula.nature.ThreadTask.ParseXMLRunableImpl;
import com.cnebula.nature.dto.Article;
import com.cnebula.nature.entity.FileNameEntity;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExtractZipUtil {
    public static final int BUFFER_SIZE = 1024;

    private static int getFromIndex(String fullPath, String str,Integer count) {
        Matcher slashMatcher = Pattern.compile(str).matcher(fullPath);
        int index = 0;
        while(slashMatcher.find()) {
            index++;
            if(index == count){
                break;
            }
        }
        return slashMatcher.start();
    }

    public static void unZip(File zipFile, String baseDir) throws Exception {

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
            String directPath = fullName.substring(0, getFromIndex(fullName, File.separator, 3));

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
        Integer taskNum = 0;
        for (int i = 0; i < fileNames.size(); i++) {
            taskNum += fileNames.get(i).size();
        }
        int cpu = Runtime.getRuntime().availableProcessors();
        LinkedBlockingQueue<Runnable> lbq = new LinkedBlockingQueue<Runnable>(taskNum);
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(cpu, cpu, 200, TimeUnit.SECONDS, lbq);

        // Parse XML then check duplication of Article
        for (List<String> fileNameIssue:fileNames){
            threadPoolExecutor.execute(new ParseXMLRunableImpl(zf, fileNameIssue, baseDir));
        }

        threadPoolExecutor.shutdown();
    }

    /**
     * 解压 zip 文件
     *
     * @param zipfile zip 压缩文件的路径
     * @return 返回 zip 压缩文件里的文件名的 list
     * @throws Exception
     */
    public static void unZip(String zipfile, String baseDir) throws Exception {
        unZip(new File(zipfile), baseDir);
    }

    /*public static void main(String[] args) throws Exception {
        unZip("/home/jihe/developFiles/nature/ftp_PUB_19-04-06_05-50-17.zip", "/home/jihe/developFiles/nature");
        //System.out.println(names);
        //Article article = new Article();
        //article.
    }*/
}