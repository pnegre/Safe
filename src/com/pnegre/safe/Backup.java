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
import java.util.*;

class Backup {

    private static final String BACKUP_DIR = "/Safe";
    private Database dataBase;

    Backup(Database dataBase) {
        this.dataBase = dataBase;
    }

    // Exporta els secrets i torna el nom del fitxer gravat
    String doExport(String password) throws Exception {

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

        File dir = getSafeDirectory();
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

        return filename;
    }

    void doImport(String filename, String password) throws Exception {
        File dir = getSafeDirectory();
        File file = new File(dir, filename);
        SimpleCrypt simpleCrypt = new SimpleCrypt(password.getBytes());
        InputStream is = simpleCrypt.decryptedInputStream(new FileInputStream(file));

        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = db.newDocument();
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.transform(new StreamSource(is), new DOMResult(doc));

        NodeList nl = doc.getElementsByTagName("secret");
        Set<Secret> secretSet = new TreeSet<Secret>(dataBase.getSecrets());
        for (int i=0; i<nl.getLength(); i++) {
            Element element = (Element) nl.item(i);
            Secret s = new Secret(0,
                    element.getAttribute("sitename"),
                    element.getAttribute("username"),
                    element.getAttribute("password"));
            if (!secretSet.contains(s))
                dataBase.newSecret(s);
        }
    }

    /**
     * Cerca fitxers backup. Retorna una llista
     *
     */
    String[] enumerateFiles() {
        File dir = getSafeDirectory();
        File[] files = dir.listFiles();
        int total = files.length;
        String[] result = new String[total];
        for(int i=0;i<total;i++)
            result[i] = files[i].getName();

        return result;
    }


    private File getSafeDirectory() {
        File sdCard = Environment.getExternalStorageDirectory();
        File dir = new File (sdCard.getAbsolutePath() + BACKUP_DIR);
        dir.mkdirs();
        System.out.println(dir.getAbsolutePath());
        return dir;
    }
}
