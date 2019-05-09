package com.cnebula.nature.configuration;

import java.io.File;

public class DefaultConfiguration {

    public static final String CONFIG = System.getProperty("user.dir") + File.separator + "config.properties";
    public static final String LOCALSITEXML = System.getProperty("user.dir") + File.separator + "local_site.xlsx";
    public static final String ZIP = System.getProperty("user.dir") + File.separator + "ftp_PUB_19-04-06_05-50-17.zip";
    //public static final String PDFBASE = System.getProperty("user.dir") + File.separator + "nature";
    public static final String PDFBASE = "Z:\\qikan-fulltext\\nature\\";
    public static final String DEFDBHOST = "localhost:1433";
    public static final String DEFDBNAME = "nature";
    public static final String DEFUSERNAME = "sa";
    public static final String DEFPASSWORD = "sqlserver";

}
