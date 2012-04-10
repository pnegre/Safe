package com.pnegre.safe;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.util.List;

class Exporter {
    private Database dataBase;

    Exporter(Database dataBase) {
        this.dataBase = dataBase;
    }

    void export() {
        try {
            DocumentBuilderFactory dfb = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dfb.newDocumentBuilder();

            Document doc = db.newDocument();
            Element rootElement = doc.createElement("data");
            doc.appendChild(rootElement);

            List<Secret> secrets = dataBase.getSecrets();
            for (Secret s : secrets) {
                Element secretElement = doc.createElement("secret");
                Attr attSiteName = doc.createAttribute("sitename");
                attSiteName.setValue(s.name);
                Attr attUserName = doc.createAttribute("username");
                attUserName.setValue(s.username);
                Attr attPassword = doc.createAttribute("password");
                attPassword.setValue(s.password);
                secretElement.setAttributeNode(attSiteName);
                secretElement.setAttributeNode(attUserName);
                secretElement.setAttributeNode(attPassword);
                rootElement.appendChild(secretElement);
            }

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            //initialize StreamResult with File object to save to file
            StreamResult result = new StreamResult(new StringWriter());
            DOMSource source = new DOMSource(doc);
            transformer.transform(source, result);

            String xmlString = result.getWriter().toString();
            System.out.println(xmlString);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}



class Importer {

}