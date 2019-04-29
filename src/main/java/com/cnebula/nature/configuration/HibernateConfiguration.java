package com.cnebula.nature.configuration;

import com.cnebula.nature.dto.Article;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class HibernateConfiguration {

    //定义变量
    //private static Configuration config;
    //private static SessionFactory sessionFactory;

    /*static {
        Properties properties = new Properties();
        InputStream systemResourceAsStream = ClassLoader.getSystemResourceAsStream("hibernate.properties");
        try {
            properties.load(systemResourceAsStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //properties.load(new FileInputStream("/hibernate.properties"));
        //1.加载hibernate.cfg.xml配置
        config=new Configuration().setProperties(properties)
                //.addPackage("com.houshenglory.hibernate.dto")
                .addAnnotatedClass(Article.class)
                .configure();
        //2.获取SessionFactory
        sessionFactory=config.buildSessionFactory();
    }*/

    public static SessionFactory getSessionFactory(Properties p) throws IOException {
        Properties properties = new Properties();
        InputStream systemResourceAsStream = ClassLoader.getSystemResourceAsStream("hibernate.properties");
        properties.load(systemResourceAsStream);
        //properties.load(new FileInputStream("/hibernate.properties"));
        //1.加载hibernate.cfg.xml配置
        Configuration config=new Configuration().setProperties(properties)
                //.addPackage("com.houshenglory.hibernate.dto")
                .addAnnotatedClass(Article.class);
        //2.获取SessionFactory
        return config.buildSessionFactory();
    }

    public static void main(String [] args) throws IOException {

        SessionFactory sessionFactory = HibernateConfiguration.getSessionFactory(null);

        Session session = sessionFactory.openSession();

        Article article = session.get(Article.class, 1);

        System.out.println(article);
    }

}
