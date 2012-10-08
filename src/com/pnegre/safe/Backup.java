package com.pnegre.safe;

import android.os.Environment;
import android.util.Log;
import com.pnegre.safe.database.Database;
import com.pnegre.safe.database.Secret;
import com.pnegre.simplecrypt.SimpleAESCryptCBC;
import net.iharder.base64.Base64;
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

    // Common initialization vector used for exporting and importing
    // the encrypted database, using AES/CBC
    static final byte[] iv = {
        75,5,-4,3,23,72,4,45,
        56,16,-5,4,-3,-48,122,21
    };

    private static byte[] randomIV() {
        byte[] result = new byte[16];
        Random r = new Random();
        for (int i=0;i<16;i++) {
            result[i] = (byte) (r.nextInt(256) - 127);
        }
        return result;
    }

    private static void copyStream(InputStream input, OutputStream output)
            throws IOException
    {
        byte[] buffer = new byte[1024]; // Adjust if you want
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1)
        {
            output.write(buffer, 0, bytesRead);
        }
    }


    Backup(Database dataBase) {
        this.dataBase = dataBase;
    }

    // Exporta els secrets i torna el nom del fitxer gravat
    String doExport(String password) throws Exception {
        byte[] iv = randomIV();
        SimpleAESCryptCBC simpleCrypt = new SimpleAESCryptCBC(password.getBytes(), iv);
        String iv_b64 = Base64.encodeBytes(iv);
        Log.v("DEBUG_EXPORT","IV: " + Arrays.toString(iv));


        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream os = simpleCrypt.cryptedOutputStream(baos);
        ByteArrayInputStream bais = new ByteArrayInputStream(getSecretsXMLString().getBytes());
        copyStream(bais,os);
        os.flush();
        os.close();
        String data_b64 = Base64.encodeBytes(baos.toByteArray());
        Log.v("DEBUG_IMPORT","Data: " + Arrays.toString(baos.toByteArray()));

        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = db.newDocument();
        Element rootElement = doc.createElement("data");
        doc.appendChild(rootElement);

        Element e = doc.createElement("iv");
        rootElement.appendChild(e);
        e.appendChild(doc.createTextNode(iv_b64));

        e = doc.createElement("xmldata");
        rootElement.appendChild(e);
        e.appendChild(doc.createTextNode(data_b64));

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        File dir = getSafeDirectory();
        String filename = String.format("safe.backup.crypt.%d.xml", System.currentTimeMillis());
        Log.v(SafeApp.LOG_TAG, "Writing " + filename);
        File file = new File(dir, filename);

        transformer.transform(new DOMSource(doc), new StreamResult(new FileOutputStream(file)));
        return filename;
    }


    private String getSecretsXMLString() throws Exception {
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

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        transformer.transform(new DOMSource(doc), new StreamResult(baos));

        return baos.toString();
    }


    void doImport(String filename, String password) throws Exception {
        File dir = getSafeDirectory();
        File file = new File(dir, filename);

        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = db.newDocument();

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.transform(new StreamSource(new FileInputStream(file)), new DOMResult(doc));

        byte[] iv = null;
        byte[] xmldata = null;

        Log.v("DEBUG_IMPORT","1111");

        Node root = doc.getFirstChild();
        NodeList nl = root.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeName().equals("iv")) {
                String sn = ((Text) n.getFirstChild()).getData();
                iv = Base64.decode(sn.getBytes());
            }

            if (n.getNodeName().equals("xmldata")) {
                String sn = ((Text) n.getFirstChild()).getData();
                xmldata = Base64.decode(sn.getBytes());
            }
        }

        Log.v("DEBUG_IMPORT","IV: " + Arrays.toString(iv));
        Log.v("DEBUG_IMPORT","Data: " + Arrays.toString(xmldata));

        if (iv == null || xmldata == null)
            throw new RuntimeException();

        Log.v("DEBUG_IMPORT","IV lenght: " + Integer.toString(iv.length));
        Log.v("DEBUG_IMPORT","Data lenght: " + Integer.toString(xmldata.length));

        Log.v("DEBUG_IMPORT","11");
        Document doc2 = db.newDocument();
        Log.v("DEBUG_IMPORT","12");
        SimpleAESCryptCBC simpleCrypt = new SimpleAESCryptCBC(password.getBytes(), iv);
        Log.v("DEBUG_IMPORT","13");
        InputStream is = simpleCrypt.decryptedInputStream(new ByteArrayInputStream(xmldata));
        Log.v("DEBUG_IMPORT","14");
        Transformer transformer2 = TransformerFactory.newInstance().newTransformer();
        Log.v("DEBUG_IMPORT","15");
        transformer2.transform(new StreamSource(is), new DOMResult(doc2));
        Log.v("DEBUG_IMPORT","16");
        doRealImport(doc2);
        Log.v("DEBUG_IMPORT","17");
    }

    private void doRealImport(Document doc) {
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
