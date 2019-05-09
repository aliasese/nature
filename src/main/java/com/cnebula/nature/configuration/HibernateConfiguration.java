package com.cnebula.nature.configuration;

import com.cnebula.nature.dto.Affiliation;
import com.cnebula.nature.dto.Article;
import com.cnebula.nature.dto.AuthAff;
import com.cnebula.nature.dto.Author;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class HibernateConfiguration {

    private final static Properties PROPERTIES = com.cnebula.nature.entity.Configuration.getProperties();
    public static SessionFactory sessionFactory;

    static {
        Properties properties = new Properties();
        InputStream systemResourceAsStream = ClassLoader.getSystemResourceAsStream("hibernate.properties");
        try {
            properties.load(systemResourceAsStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String dbHost = !StringUtils.isEmpty(PROPERTIES.getProperty("dbHost")) ? PROPERTIES.getProperty("dbHost") : DefaultConfiguration.DEFDBHOST;
        String dbName = !StringUtils.isEmpty(PROPERTIES.getProperty("dbName")) ? PROPERTIES.getProperty("dbName") : DefaultConfiguration.DEFDBNAME;
        String username = !StringUtils.isEmpty(PROPERTIES.getProperty("username")) ? PROPERTIES.getProperty("username") : DefaultConfiguration.DEFUSERNAME;
        String password = !StringUtils.isEmpty(PROPERTIES.getProperty("password")) ? PROPERTIES.getProperty("password") : DefaultConfiguration.DEFPASSWORD;
        properties.setProperty("url", "jdbc:sqlserver://" + dbHost + ";DatabaseName=" + dbName);
        properties.setProperty("username", username);
        properties.setProperty("password", password);
        System.out.println("The real properties configurations will be injected to hibernate are: " + properties);
        //1.加载hibernate.cfg.xml配置
        Configuration config=new Configuration().setProperties(properties)
                //.addPackage("com.houshenglory.hibernate.dto")
                .addAnnotatedClass(Author.class)
                .addAnnotatedClass(AuthAff.class)
                .addAnnotatedClass(Affiliation.class)
                .addAnnotatedClass(Article.class);
        //2.获取SessionFactory
        sessionFactory = config.buildSessionFactory();
    }
}
