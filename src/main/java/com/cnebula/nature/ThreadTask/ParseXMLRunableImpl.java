package com.cnebula.nature.ThreadTask;

import com.cnebula.nature.configuration.HibernateConfiguration;
import com.cnebula.nature.dto.Affiliation;
import com.cnebula.nature.dto.Article;
import com.cnebula.nature.dto.AuthAff;
import com.cnebula.nature.dto.Author;
import com.cnebula.nature.util.ExtractZipUtil;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
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
    @SuppressWarnings("unchecked")
    public void run() {
        System.out.println("===========");
        String jtlCode = null;
        JSONObject articleSetJson = null;
        List<Element> es = null;
        Iterator<String> issueIt = fileNameIssue.iterator();
        while (issueIt.hasNext()) {
            String issueFileName = issueIt.next();
            jtlCode = issueFileName.substring(0, issueFileName.indexOf(File.separator)).split("=")[1];
            if (issueFileName.endsWith("xml")) {
                InputStream ips = null;
                try {
                    ips = zipFile.getInputStream(zipFile.getEntry(issueFileName));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                SAXReader sr = new SAXReader();
                Document doc = null;
                try {
                    doc = sr.read(ips);
                } catch (DocumentException e) {
                    e.printStackTrace();
                }
                Element rootElement = doc.getRootElement();
                es = rootElement.elements();
                /*for (Element element : list) {
                    element
                }*/
                InputStream ipss = null;
                try {
                    ipss = zipFile.getInputStream(zipFile.getEntry(issueFileName));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                articleSetJson = XML.toJSONObject(new InputStreamReader(ipss));
                //fileNameIssue.remove(issueFileName);
            }
        }
        if (articleSetJson == null) return;
        //if (articleSetJson == null) throw new Exception("Fail to get XML");

        JSONArray articles = articleSetJson.getJSONObject("ArticleSet").getJSONArray("Article");
        for (int i = 0; i < articles.length(); i++) {
            JSONObject article = articles.getJSONObject(i);
            String pdfDir = null; //Assume that the directory of PDF is pdfDir;
            try {
                pdfDir = article.getString("pdfDir");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            InputStream is = null;
            int byteCount = 0;
            /*for (int j = 0; j < fileNameIssue.size(); j++) {
                if (fileNameIssue.get(j).contains(pdfDir)) {
                    try {
                        is = zipFile.getInputStream(zipFile.getEntry(fileNameIssue.get(j))); // InputStream of PDF
                        byteCount = is.available();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }*/

            Article at = new Article();
            String pips = pdfDir != null ? pdfDir.substring(0, pdfDir.lastIndexOf(".pdf")) : null;
            at.setPips(pips);
            JSONArray atIds = article.getJSONObject("ArticleIdList").getJSONArray("ArticleId");
            for (int j = 0; j < atIds.length(); j++) {
                String idType = atIds.getJSONObject(j).getString("IdType");
                if (idType.contentEquals("doi")) {
                    at.setDoi(atIds.getJSONObject(j).getString("content"));
                } else
                    if (idType.contentEquals("pii")) {
                        at.setPii(atIds.getJSONObject(j).getString("content"));
                }
            }
            JSONObject journal = article.getJSONObject("Journal");
            at.setPublisherName(journal.getString("PublisherName"));
            ExtractZipUtil.getJtlFullName(at, jtlCode);
            at.setIssn9(journal.getString("Issn"));
            at.setIssn8(journal.getString("Issn").replace("-",""));
            at.setVolume(String.valueOf(journal.get("Volume")));
            at.setIssue(String.valueOf(journal.get("Issue")));
            Object day = null;
            try {
                day = journal.getJSONObject("PubDate").get("Day");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String dayy = day == null ? null : String.valueOf(day);

            at.setPubDate(String.valueOf(journal.getJSONObject("PubDate").get("Year")), String.valueOf(journal.getJSONObject("PubDate").get("Month")), dayy);
            at.setPubStatus(journal.getJSONObject("PubDate").getString("PubStatus"));
            at.setCateg(null);
            at.setLanguage(article.getString("Language"));
            at.setPpct(article.getInt("LastPage") - article.getInt("FirstPage") + 1);
            at.setPpf(String.valueOf(article.get("FirstPage")));
            at.setPpl(String.valueOf(article.get("LastPage")));
            System.out.println("&&&&&&&&&&&&&&&&&&&&&&&" + article.getString("ArticleTitle"));
            try {
                at.setAtl(new String(article.getString("ArticleTitle").getBytes(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            at.setAbst(article.getString("Abstract"));
            at.setFileSize(String.valueOf(byteCount));
            at.setAbsFlag(StringUtils.isEmpty(at.getAbst()) ? 0 : 1);
            at.setOriContent(es.get(i).asXML());

            //Author author = new Author();
            JSONArray authors = new JSONArray();
            JSONArray affiliations = new JSONArray();
            //List<Affiliation> affiliations = new ArrayList<>();
            JSONArray authorList = new JSONArray();
            if (article.getJSONObject("AuthorList").get("Author") instanceof JSONObject) {
                authorList.put(article.getJSONObject("AuthorList").getJSONObject("Author"));
            } else {
                authorList = article.getJSONObject("AuthorList").getJSONArray("Author");
            }
            //JSONArray authorList = article.getJSONObject("AuthorList").getJSONArray("Author");
            for (int j = 0; j < authorList.length(); j++) {
                Author author = new Author();
                authors.put(author);
                String firstName = authorList.getJSONObject(j).getString("FirstName");
                String middleName = "";
                try {
                    middleName = authorList.getJSONObject(j).getString("MiddleName");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String lastName = authorList.getJSONObject(j).getString("LastName");
                author.setAufnms(firstName + " " + middleName);
                author.setAufnmsindex(firstName + " " + middleName);
                author.setAusnm(lastName);
                author.setAusnmindex(lastName);
                author.setAbbindex(firstName.substring(0, 1) + "." + middleName);
                author.setAusort(String.valueOf(j + 1));

                JSONArray affs = new JSONArray();
                if (authorList.getJSONObject(j).get("AffiliationInfo") instanceof JSONObject) {
                    affs.put(authorList.getJSONObject(j).getJSONObject("AffiliationInfo"));
                } else {
                    affs = authorList.getJSONObject(j).getJSONArray("AffiliationInfo");
                }
                for (int k = 0; k < affs.length(); k++) {
                    Affiliation affiliation = new Affiliation();
                    affiliations.put(affiliation);
                    affiliation.setAff(affs.getJSONObject(k).getString("Affiliation"));
                    affiliation.setAffsort(k + 1);
                }
            }

            // Remove Duplicate
            synchronized (ParseXMLRunableImpl.class) {
                Session session = this.sessionFactory.openSession();
                /*CriteriaBuilder cb = session.getCriteriaBuilder();
                CriteriaQuery<Article> query = cb.createQuery(Article.class);

                query.where((Predicate) Restrictions.eq("artid", 1));*/
                //query.where();
                DetachedCriteria dc = DetachedCriteria.forClass(Article.class);
                //dc.add(Restrictions.eq("jtl", at.getJournalTitle()));
                dc.add(Restrictions.eq("jabt", at.getJabt()));
                dc.add(Restrictions.eq("issn8", at.getIssn8()));
                dc.add(Restrictions.eq("volume", at.getVolume()));
                dc.add(Restrictions.eq("issue", at.getIssue()));
                dc.add(Restrictions.eq("atl", at.getAtl()));
                dc.add(Restrictions.eq("ppf", at.getPpf()));
                dc.add(Restrictions.eq("ppl", at.getPpl()));

                Criteria cr = dc.getExecutableCriteria(session);
                List<Article> list = cr.list();
                System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@" + list.size() + dc.toString());
                for (int j = 0; j < list.size(); j++) {
                    //Transaction transaction = session.beginTransaction();
                    Article att = list.get(j);

                    @SuppressWarnings("unchecked")
                    String hqlSelAff = "FROM Affiliation WHERE artid = :artid";
                    List<Affiliation> affs = session.createQuery(hqlSelAff)
                            .setParameter("artid", att.getArtid())
                            .getResultList();

                    @SuppressWarnings("unchecked")
                    String hqlSelAu = "FROM Author WHERE artid = :artid";
                    List<Author> auths = session.createQuery(hqlSelAu)
                            .setParameter("artid", att.getArtid())
                            .getResultList();

                    synchronized (ParseXMLRunableImpl.class) {
                        Transaction transaction = session.beginTransaction();
                        for (int k = 0; k < auths.size(); k++) {
                            Author author = auths.get(k);
                            //Session session1 = this.sessionFactory.openSession();
                            //Transaction transaction = session1.beginTransaction();
                            for (int l = 0; l < affs.size(); l++) {
                                Affiliation aff = affs.get(l);
                                Session session2 = this.sessionFactory.getCurrentSession();
                                Transaction transaction2 = session2.beginTransaction();
                                String hqlSelAuthAff = "FROM AuthAff WHERE aid = :aid AND affid = :affid";
                                List<AuthAff> authAffs = session2.createQuery(hqlSelAuthAff)
                                        .setParameter("aid", author.getAid())
                                        .setParameter("affid", aff.getAffid())
                                        .getResultList();
                                transaction2.commit();
                                session2.close();
                                for (int m = 0; m < authAffs.size(); m++) {
                                    AuthAff authAff = authAffs.get(m);
                                    session.delete(authAff);
                                }
                                session.delete(aff);
                            }
                            session.delete(author);
                            //transaction.commit();
                        }
                        session.delete(att);
                        transaction.commit();
                    }
                }
                session.close();

                Session session1 = this.sessionFactory.openSession();
                Transaction transaction = session1.beginTransaction();
                session1.save(at);
                for (int j = 0; j < authors.length(); j++) {
                    Author author = (Author) authors.get(j);
                    author.setArtid(at.getArtid());
                    session1.save(author);
                    for (int k = 0; k < affiliations.length(); k++) {
                        Affiliation affiliation = (Affiliation) affiliations.get(k);
                        affiliation.setArtid(at.getArtid());
                        session1.save(affiliation);
                        AuthAff authAff = new AuthAff();
                        authAff.setAid(author.getAid());
                        authAff.setAffid(affiliation.getAffid());
                        session1.save(authAff);
                    }
                }
                transaction.commit();

                //session.flush();
                session1.close();
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
