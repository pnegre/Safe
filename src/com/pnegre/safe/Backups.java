package com.pnegre.safe;

import android.os.Environment;
import android.util.Log;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.List;

class Exporter {
    private Database dataBase;

    Exporter(Database dataBase) {
        this.dataBase = dataBase;
    }

    void export(String password) {
        try {
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();

            Document doc = db.newDocument();
            Element rootElement = doc.createElement("data");
            doc.appendChild(rootElement);

            List<Secret> secrets = dataBase.getSecrets();
            for (Secret s : secrets) {
                // TODO: enlloc d'atributs, estaria bé que fóssin tot elements XML
                // Exemple: <secret><sitename>www.esliceu.com</sitename> etc...

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

            File sdCard = Environment.getExternalStorageDirectory();
            File dir = new File (sdCard.getAbsolutePath() + "/safe");
            dir.mkdirs();
            String filename = String.format("safe.backup.crypt.%d", System.currentTimeMillis());
            Log.v(SafeApp.LOG_TAG, "Writing " + filename);
            File file = new File(dir, filename);
            SimpleCrypt simpleCrypt = new SimpleCrypt(password.getBytes());
            OutputStream os = simpleCrypt.cryptedOutputStream(new FileOutputStream(file));

            //initialize StreamResult with File object to save to file
            StreamResult result = new StreamResult(os);
            DOMSource source = new DOMSource(doc);
            transformer.transform(source, result);
            os.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}



class Importer {

    Importer() {

    }

    void Import(String fileName, String password) {
        try {
            File file = new File(fileName);
            SimpleCrypt simpleCrypt = new SimpleCrypt(password.getBytes());
            InputStream is = simpleCrypt.decryptedInputStream(new FileInputStream(file));

            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = db.newDocument();
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.transform(new StreamSource(is), new DOMResult(doc));

            NodeList nl = doc.getElementsByTagName("secret");
            int l = nl.getLength();
            for (int i=0; i<l; i++) {
                Element element = (Element) nl.item(i);
                System.out.println(element.getTagName());
                System.out.println(element.getAttribute("sitename"));
                System.out.println(element.getAttribute("username"));
                System.out.println(element.getAttribute("password"));
                System.out.println("---");
                // Acabar...
            }
            System.out.println("Length " + l);

            // TODO: Acabar...

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}