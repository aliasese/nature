package com.cnebula.nature.ThreadTask;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

public class ParseXMLRunableImpl implements Runnable {

    private ZipFile zipFile;
    private String fileName;

    public ParseXMLRunableImpl() {
    }

    public ParseXMLRunableImpl(ZipFile zipFile, String fileName) {
        this.zipFile = zipFile;
        this.fileName = fileName;
    }

    @Override
    public void run() {
        System.out.println("===========");
        if (fileName.endsWith("xml")) {
            InputStream ips = null;
            try {
                ips = zipFile.getInputStream(zipFile.getEntry(fileName));
            } catch (IOException e) {
                e.printStackTrace();
            }

            SAXReader saxReader = new SAXReader();
            Document doc = null;
            try {
                doc = saxReader.read(ips);
            } catch (DocumentException e) {
                e.printStackTrace();
            }
            Element rootElement = doc.getRootElement();
            List<Element> list = rootElement.elements();
            for (Element element : list) {
                String name = element.getName();

                Iterator<Element> ei = element.elementIterator();
                while (ei.hasNext()) {
                    ei.forEachRemaining(element1 -> {
                        Iterator<Element> ei1 = element1.elementIterator();

                    });
                }


                Attribute attribute = element.attribute(name);
                String text = element.getText();
                Iterator<Element> elementIterator = element.elementIterator();
                while (elementIterator.hasNext()) {
                    Element next = elementIterator.next();
                    List<Element> elements = next.elements();
                }
                Iterator<Attribute> attributeIterator = element.attributeIterator();
                while (attributeIterator.hasNext()) {
                    Attribute next = attributeIterator.next();
                    String name1 = next.getName();
                    String value = next.getValue();
                }
                List<Attribute> attributes = element.attributes();
                for (Attribute a : attributes) {
                    String name2 = a.getName();
                    a.getValue();
                }
            }
        } else {

        }
    }
}
