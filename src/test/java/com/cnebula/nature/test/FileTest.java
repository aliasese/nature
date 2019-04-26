package com.cnebula.nature.test;

import java.io.File;
import java.io.IOException;

public class FileTest {

    public static void main(String[] agrs) throws IOException {
        File file = new File("/home/jihe/develop/IdeaProjects/nature-data-import", "JOU=41570/VOL=2019.3/ISU=4/ART=85/BodyRef/PDF/41570_2019_Article_85.pdf");
        file.getParentFile().mkdirs();
        file.createNewFile();
    }
}
