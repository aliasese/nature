package com.cnebula.nature;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.cnebula.nature.entity.Configuration;
import com.cnebula.nature.util.DruidUtil;
import com.cnebula.nature.util.ExtractZipUtil;
import com.cnebula.nature.util.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Properties;

public class AppMain {

    private final static Logger log = LoggerFactory.getLogger(AppMain.class);

    public static void main(String[] agrs) throws IOException {
        log.info("=======================================Begin=====================================");
        log.info("Main thread begin to start for 'Nature data importing tool'");
        String userDir = System.getProperty("user.dir");
        log.info("Begin to read configuration file from file system: " + userDir);
        File file = null;
        try {
            file = new File(userDir + File.separator + "config.properties");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Fail to read config.properties, caused: " + e.getLocalizedMessage(), e);
        }
        FileInputStream fileInputStream = new FileInputStream(file);
        Properties properties = new Properties();
        properties.load(fileInputStream);

        // =========Store configuration to internal inner memory===========
        Configuration.setProperties(properties);

        try {
            ExtractZipUtil.unZip(properties.getProperty("zipFileDir"), properties.getProperty("pdfBaseDir"));
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error to parse zip, caused: " + e.getLocalizedMessage(), e);
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
