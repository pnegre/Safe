package com.pnegre.safe;

import android.os.Environment;
import android.util.Log;
import com.pnegre.safe.database.Database;
import com.pnegre.safe.database.Secret;
import com.pnegre.simplecrypt.SimpleAESCryptCBC;
import com.pnegre.simplecrypt.SimpleAESCryptECB;
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
import java.util.Set;
import java.util.TreeSet;


class Backup {

    private static final String BACKUP_DIR = "/Safe";
    private Database dataBase;

    // Common initialization vector used for exporting and importing
    // the encrypted database, using AES/CBC
    static final byte[] iv = {
        75,5,-4,3,23,72,4,45,
        56,16,-5,4,-3,-48,122,21
    };


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
            Element secretElement = doc.createElement("secret");
            rootElement.appendChild(secretElement);

            Element e = doc.createElement("sitename");
            e.appendChild(doc.createTextNode(s.name));
            secretElement.appendChild(e);

            e = doc.createElement("username");
            e.appendChild(doc.createTextNode(s.username));
            secretElement.appendChild(e);

            e = doc.createElement("password");
            e.appendChild(doc.createTextNode(s.password));
            secretElement.appendChild(e);
        }

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        File dir = getSafeDirectory();
        String filename = String.format("safe.backup.crypt.%d", System.currentTimeMillis());
        Log.v(SafeApp.LOG_TAG, "Writing " + filename);
        File file = new File(dir, filename);
        SimpleAESCryptCBC simpleCrypt = new SimpleAESCryptCBC(password.getBytes(), iv);
        OutputStream os = simpleCrypt.cryptedOutputStream(new FileOutputStream(file));

        transformer.transform(new DOMSource(doc), new StreamResult(os));
        os.close();
        return filename;
    }

    void doImport(String filename, String password) throws Exception {
        File dir = getSafeDirectory();
        File file = new File(dir, filename);
        SimpleAESCryptCBC simpleCrypt = new SimpleAESCryptCBC(password.getBytes(), iv);
        InputStream is = simpleCrypt.decryptedInputStream(new FileInputStream(file));

        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = db.newDocument();
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.transform(new StreamSource(is), new DOMResult(doc));
        Node root = doc.getFirstChild();
        if (!root.getNodeName().equals("data")) throw new RuntimeException();

        Set<Secret> secretSet = new TreeSet<Secret>(dataBase.getSecrets());
        NodeList nl = root.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (!n.getNodeName().equals("secret")) continue;

            NodeList nl2 = n.getChildNodes();
            String sitename = null, username = null, passwd = null;
            for (int j = 0; j < nl2.getLength(); j++) {
                Node n2 = nl2.item(j);
                if (n2.getNodeName().equals("sitename")) {
                    String sn = ((Text) n2.getFirstChild()).getData();
                    sitename = sn;
                }
                if (n2.getNodeName().equals("username")) {
                    String sn = ((Text) n2.getFirstChild()).getData();
                    username = sn;
                }
                if (n2.getNodeName().equals("password")) {
                    String sn = ((Text) n2.getFirstChild()).getData();
                    passwd = sn;
                }
            }
            if (sitename != null && username != null && passwd != null) {
                Secret s = new Secret(0, sitename, username, passwd);
                if (!secretSet.contains(s))
                    dataBase.newSecret(s);
            }
        }
    }

    /**
     * Cerca fitxers backup. Retorna una llista
     */
    String[] enumerateFiles() {
        File dir = getSafeDirectory();
        File[] files = dir.listFiles();
        int total = files.length;
        String[] result = new String[total];
        for (int i = 0; i < total; i++)
            result[i] = files[i].getName();

        return result;
    }


    private File getSafeDirectory() {
        File sdCard = Environment.getExternalStorageDirectory();
        File dir = new File(sdCard.getAbsolutePath() + BACKUP_DIR);
        dir.mkdirs();
        System.out.println(dir.getAbsolutePath());
        return dir;
    }
}
