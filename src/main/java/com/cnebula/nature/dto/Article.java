package com.cnebula.nature.dto;

import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import java.text.ParseException;

@Entity
@Table(name = "nature.dbo.content")
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "artid", nullable = false)
    private Integer artid;


    @Column(name = "pips", nullable = true)
    private String pips; //全文文件名称

    @Column(name = "pii", nullable = true, insertable = false, updatable = false)
    private String pii;

    @Column(name = "pii", nullable = true)
    private String doi;

    @Column(name = "pnm", nullable = true)
    private String publisherName; //出版社

    @Column(name = "jtl", nullable = false)
    private String journalTitle; //期刊名称

    @Column(name = "jtl_sort", nullable = false)
    private String jtlSort; //期刊排序名称

    @Column(name = "jtl_index", nullable = false)
    private String jtlIndex; //期刊排序名称

    @Column(name = "jabt", nullable = false)
    private String jabt; //期刊排序名称

    @Column(name = "issn9", nullable = true)
    private String issn9;

    @Column(name = "issn8", nullable = true)
    private String issn8;

    @Column(name = "vid", nullable = false)
    private String volume; //卷号

    @Column(name = "iid", nullable = true)
    private String issue; //期号

    @Column(name = "cd", nullable = true)
    private String pubDate; //出版日期

    @Column(name = "cd_pub", nullable = true)
    private String cdPub;

    @Column(name = "pubstatus", nullable = true)
    private String pubStatus;

    @Column(name = "categ", nullable = true)
    private String categ; //文章所属类目

    @Column(name = "lang", nullable = true)
    private String language;

    @Column(name = "ppct", nullable = true)
    private Integer ppct; //页码长度

    @Column(name = "ppf", nullable = true)
    private String ppf; //起始页码

    @Column(name = "ppl", nullable = true)
    private String ppl; //终止页码

    @Column(name = "atl", nullable = false)
    private String atl; //文章名称

    @Column(name = "atl_sort", nullable = false)
    private String atlSort; //文章排序名称

    @Column(name = "atl_index", nullable = false)
    private String atlIndex; //文章索引名称

    @Column(name = "abst", nullable = true)
    private String abst; //文章摘要

    @Column(name = "filesize", nullable = true)
    private String fileSize; //文章PDF文件大小

    @Column(name = "paperflag", nullable = true)
    private Integer paperFlag = 0; //未知, 数据库字段全0，所以此处默认 0

    @Column(name = "absflag", nullable = true)
    private Integer absFlag; //文章是否有摘要,0:无 1:有

    @Column(name = "oricontent", nullable = true)
    private String oriContent; //文章原始XML


    public Article() {

    }

    public String getAtl() {
        return atl;
    }

    public void setAtl(String atl) {
        this.atl = atl;
        this.atlSort = atl;
        this.atlIndex = atl;
    }

    public String getAtlSort() {
        return atlSort;
    }

    public String getAtlIndex() {
        return atlIndex;
    }

    public String getJournalTitle() {
        return journalTitle;
    }

    public void setJournalTitle(String journalTitle) {
        this.journalTitle = journalTitle;
        this.jtlSort = journalTitle;
        this.jtlIndex = journalTitle;
    }

    public String getJtlSort() {
        return jtlSort;
    }

    public String getJtlIndex() {
        return jtlIndex;
    }

    public void setPubDate(String pubDate) {
        this.pubDate = pubDate;
        this.cdPub = pubDate;
    }

    public String getPubDate() {
        return pubDate;
    }

    public String getCdPub() {
        return cdPub;
    }

    public void setPubDate(String year, String month, String day) {
        if (!StringUtils.isEmpty(month)) {
            if (month.length() == 1) month = "0" + month;
        }
        this.pubDate = year + month;
    }

    public static void main(String [] args) throws ParseException {
        Article article = new Article();
        article.setPubDate("2019", "12", "28");
        System.out.println(article.pubDate);

        /*SimpleDateFormat fo = new SimpleDateFormat("yyyyMMdd");
        String format = fo.format(fo.parse("00000000"));*/

    }
}

class PubDate {

    private String pubStatus;
    private String Year;
}