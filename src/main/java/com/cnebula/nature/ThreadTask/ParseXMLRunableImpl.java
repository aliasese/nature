package com.cnebula.nature.ThreadTask;

import com.cnebula.nature.configuration.HibernateConfiguration;
import com.cnebula.nature.dto.Article;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.hibernate.Session;
import org.json.JSONObject;
import org.json.XML;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

public class ParseXMLRunableImpl implements Runnable {

    private ZipFile zipFile;
    private List<String> fileNameIssue;
    //private String baseDir;
    private Properties properties;

    public ParseXMLRunableImpl() {
    }

    public ParseXMLRunableImpl(ZipFile zipFile, List<String> fileNameIssue, Properties properties) {
        this.zipFile = zipFile;
        this.fileNameIssue = fileNameIssue;
        //this.baseDir = baseDir;
        this.properties = properties;
    }

    @Override
    public void run() {
        System.out.println("===========");
        Iterator<String> issueIt = fileNameIssue.iterator();
        while (issueIt.hasNext()) {
            String issueFileName = issueIt.next();
            if (issueFileName.endsWith("xml")) {
                InputStream ips = null;
                try {
                    ips = zipFile.getInputStream(zipFile.getEntry(issueFileName));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                JSONObject articleSetJson = XML.toJSONObject(new InputStreamReader(ips));
                System.out.println(articleSetJson);

                try {
                    Session session = HibernateConfiguration.getSessionFactory(this.properties).openSession();
                    CriteriaBuilder cb = session.getCriteriaBuilder();
                    CriteriaQuery<Article> query = cb.createQuery(Article.class);
                    query.where();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else {

            }
        }
    }
}
