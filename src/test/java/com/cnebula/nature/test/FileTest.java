package com.cnebula.nature.test;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class FileTest {

    public static void main(String[] agrs) throws IOException {
        /*File file = new File("/home/jihe/develop/IdeaProjects/nature-data-import", "JOU=41570/VOL=2019.3/ISU=4/ART=85/BodyRef/PDF/41570_2019_Article_85.pdf");
        file.getParentFile().mkdirs();
        file.createNewFile();*/

        /*String pdf = "41570_2019_Article_84.pdf";

        System.out.println(pdf.substring(0, pdf.lastIndexOf(".pdf")));*/

        String dir = "JOU=41570/VOL=2019.3/ISU=4";
        String jtl = dir.substring(0, dir.indexOf(File.separator)).split("=")[1];
        System.out.println(jtl);
    }

    @Test
    public void testNumeric() {
        String code = "4157.0";
        String[] split = code.split("\\.");
        System.out.println(code.split(".")[0]);
    }
}
