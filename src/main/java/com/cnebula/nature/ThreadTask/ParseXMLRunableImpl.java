package com.cnebula.nature.ThreadTask;

import com.cnebula.nature.configuration.DefaultConfiguration;
import com.cnebula.nature.configuration.HibernateConfiguration;
import com.cnebula.nature.dto.Affiliation;
import com.cnebula.nature.dto.Article;
import com.cnebula.nature.dto.AuthAff;
import com.cnebula.nature.dto.Author;
import com.cnebula.nature.util.ExtractZipUtil;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.hibernate.*;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.hibernate.jdbc.ReturningWork;
import org.hibernate.query.NativeQuery;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import java.io.*;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

public class ParseXMLRunableImpl implements Runnable {

    private ZipFile zipFile;
    private List<String> fileNameIssue;
    //private String baseDir;
    private static Properties properties;
    //private SessionFactory sessionFactory;

    public ParseXMLRunableImpl() {
    }

    public ParseXMLRunableImpl(ZipFile zipFile, List<String> fileNameIssue, Properties properties) {
        this.zipFile = zipFile;
        this.fileNameIssue = fileNameIssue;
        //this.baseDir = baseDir;
        this.properties = properties;
        //this.sessionFactory = sessionFactory;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void run() {
        try {
            String jtlCode = null;
            JSONObject articleSetJson = null;
            List<Element> es = null;
            Iterator<String> issueIt = fileNameIssue.iterator();
            while (issueIt.hasNext()) {
                String issueFileName = issueIt.next();
                jtlCode = issueFileName.substring(0, issueFileName.indexOf("/")).split("=")[1];
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
                    break;
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
                    Session session = HibernateConfiguration.sessionFactory.openSession();
                    //Transaction transaction1 = session.beginTransaction();

                    try {
                        Object o = session.doReturningWork(new ReturningWork<Object>() {
                            @Override
                            public Object execute(Connection connection) throws SQLException {
                                CallableStatement cs = connection.prepareCall("{Call pc_remove_duplication(?,?,?,?,?,?,?)}");
                                cs.setString(1, at.getJabt());
                                cs.setString(2, at.getIssn8());
                                cs.setInt(3, Integer.valueOf(at.getVolume()));
                                cs.setInt(4, Integer.valueOf(at.getIssue()));
                                cs.setString(5, at.getAtl());
                                cs.setInt(6, Integer.valueOf(at.getPpf()));
                                cs.setInt(7, Integer.valueOf(at.getPpl()));
                                return cs.executeUpdate();
                                //return null;
                            }
                        });
                    } finally {
                        session.close();
                    }

                    // Delete PDF of specific article
                    /*String pdfBaseDir = properties.getProperty(DefaultConfiguration.NAME_PDFBASEDIR);
                    String pdfDirChild = att.getJabt() + File.separator + att.getVolume() + File.separator + att.getIssue() + File.separator + att.getPips() + ".pdf";

                    File file = new File(pdfBaseDir, pdfDirChild);
                    if (file.exists()) {
                        file.delete();
                    }*/



                    Session session1 = HibernateConfiguration.sessionFactory.openSession();
                    Transaction transaction = session1.beginTransaction();
                    try {
                        session1.save(at);

                        // Store PDF to specific directory of file system
                    /*String pdfBaseDir = properties.getProperty(DefaultConfiguration.NAME_PDFBASEDIR);
                    String pdfDirChild = at.getJabt() + File.separator + at.getVolume() + File.separator + at.getIssue();
                    File file = new File(pdfBaseDir, pdfDirChild);
                    if (!file.exists()) {
                        file.mkdirs();
                    }

                    BufferedOutputStream bos = null;
                    try {
                        bos = new BufferedOutputStream(new FileOutputStream(at.getPpf().concat(".pdf"), false));
                        byte[] bt = new byte[1024];
                        int length = 0;
                        while ((length = is.read(bt)) != -1) {
                            bos.write(bt);
                            bos.flush();
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            bos.close();
                            is.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }*/

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
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                        transaction.rollback();
                    } finally {
                        session1.close();
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
            return;
        }
    }
}
