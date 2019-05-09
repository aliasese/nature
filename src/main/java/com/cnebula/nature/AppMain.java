package com.cnebula.nature;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.cnebula.nature.configuration.DefaultConfiguration;
import com.cnebula.nature.entity.Configuration;
import com.cnebula.nature.util.DruidUtil;
import com.cnebula.nature.util.ExtractZipUtil;
import com.cnebula.nature.util.PropertiesUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Properties;

public class AppMain {

    private final static Logger log = LoggerFactory.getLogger(AppMain.class);

    public static void main(String[] agrs) throws IOException {
        log.debug("Processing trade with id: {} and symbol : {} ", 100000000, "pku");
        log.debug("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
        log.warn("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        log.error("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
        log.info("=======================================Begin=====================================");
        log.info("Main thread begin to start for 'Nature data importing tool'");
        String userDir = System.getProperty("user.dir");
        log.info("Begin to read configuration file from file system: " + userDir);
        File file = null;
        try {
            file = new File(DefaultConfiguration.CONFIG);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Fail to read config.properties, caused: " + e.getLocalizedMessage(), e);
        }
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Properties properties = new Properties();
        properties.load(fileInputStream);

        // =========Store configuration to internal inner memory===========
        Configuration.setProperties(properties);

        try {
            String zipFileDir = !StringUtils.isEmpty(properties.getProperty("zipFileDir")) ? properties.getProperty("zipFileDir") : DefaultConfiguration.ZIP;
            String pdfBaseDir = !StringUtils.isEmpty(properties.getProperty("pdfBaseDir")) ? properties.getProperty("pdfBaseDir") : DefaultConfiguration.PDFBASE;
            ExtractZipUtil.unZip(zipFileDir, pdfBaseDir);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error to parse zip, caused: " + e.getLocalizedMessage(), e);
        } finally {
            System.exit(1);
        }




       /* DruidDataSource ds = DruidUtil.createDruidConnectionPool(PropertiesUtil.getProperties());
        try {
            DruidPooledConnection dc = ds.getConnection();

            CallableStatement c = dc.prepareCall("SELECT * FROM nature.dbo.content");
            ResultSet rs = c.executeQuery();
            ResultSetMetaData md = rs.getMetaData();
            int columnCount = md.getColumnCount();
            while (rs.next()) {
                for (int i = 1; i < columnCount; i++) {
                    Object object = rs.getObject(i);
                    System.out.println(object);
                }
            }
            System.out.println(columnCount);
        } catch (SQLException e) {
            e.printStackTrace();
        }
*/
        log.info("=======================================Success=======================================");
        log.info("=======================================End=======================================");
    }
}
