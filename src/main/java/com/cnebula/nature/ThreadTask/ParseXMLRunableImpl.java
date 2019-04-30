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
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

public class ParseXMLRunableImpl implements Runnable {

    private ZipFile zipFile;
    private List<String> fileNameIssue;
    //private String baseDir;
    private Properties properties;
    private SessionFactory sessionFactory;

    public ParseXMLRunableImpl() {
    }

    public ParseXMLRunableImpl(ZipFile zipFile, List<String> fileNameIssue, Properties properties, SessionFactory sessionFactory) {
        this.zipFile = zipFile;
        this.fileNameIssue = fileNameIssue;
        //this.baseDir = baseDir;
        this.properties = properties;
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void run() {
        System.out.println("===========");
        JSONObject articleSetJson = null;
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

                articleSetJson = XML.toJSONObject(new InputStreamReader(ips));
                fileNameIssue.remove(issueFileName);
            }
        }
        if (articleSetJson == null) return;
        //if (articleSetJson == null) throw new Exception("Fail to get XML");

        JSONArray articles = articleSetJson.getJSONObject("ArticleSet").getJSONArray("Article");
        for (int i = 0; i < articles.length(); i++) {
            JSONObject article = articles.getJSONObject(i);
            String pdfDir = article.getString("pdfDir"); //Assume that the directory of PDF is pdfDir;
            for (int j = 0; j < fileNameIssue.size(); j++) {
                if (fileNameIssue.get(j).contains(pdfDir)) {
                    try {
                        InputStream is = zipFile.getInputStream(zipFile.getEntry(fileNameIssue.get(j))); // InputStream of PDF

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }


            Article at = new Article();

            // Remove Duplicate
            Session session = this.sessionFactory.openSession();
                /*CriteriaBuilder cb = session.getCriteriaBuilder();
                CriteriaQuery<Article> query = cb.createQuery(Article.class);

                query.where((Predicate) Restrictions.eq("artid", 1));*/
            //query.where();
            DetachedCriteria dc = DetachedCriteria.forClass(Article.class);
            dc.add(Restrictions.eq("jtl", at.getJournalTitle()));

            Criteria cr = dc.getExecutableCriteria(session);
            List list = cr.list();
            for (int j = 0; j < list.size(); j++) {
                Session s1 = this.sessionFactory.openSession();

            }

        }
/*
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

                Session session = this.sessionFactory.openSession();
                *//*CriteriaBuilder cb = session.getCriteriaBuilder();
                CriteriaQuery<Article> query = cb.createQuery(Article.class);

                query.where((Predicate) Restrictions.eq("artid", 1));*//*
                //query.where();
                DetachedCriteria dc = DetachedCriteria.forClass(Article.class);
                dc.add(Restrictions.eq("artid", 1));

                Criteria cr = dc.getExecutableCriteria(session);
                List list = cr.list();

                System.out.println(list);


            } else {

            }
        }*/
    }
}
