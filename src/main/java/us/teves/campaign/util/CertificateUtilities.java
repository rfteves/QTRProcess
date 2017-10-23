/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.teves.campaign.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 *
 * @author rfteves
 */
public class CertificateUtilities {

    /*
     * To change this template, choose Tools | Templates
     * and open the template in the editor.
     */
    public static void main(String[] args) throws IOException {
        CertificateUtilities.initializeCertificate("smtp.bizmail.yahoo.com", 465);
    }

    public static SSLContext createSSLContext(
            boolean clientMode)
            throws Exception {
        char[] passphrase = "ccbroker*2009".toCharArray();
        KeyStore ks = KeyStore.getInstance("JKS");
        char SEP = File.separatorChar;
        File securityDir = new File(System.getProperty("java.home") + SEP + "lib" + SEP + "security");
        File ccbrokercerts = new File(securityDir, "ccbrokercacerts");

        ks.load(new FileInputStream(ccbrokercerts), passphrase);
        SSLContext sslContext = SSLContext.getInstance("TLS");
        if (clientMode) {
            // TrustManager's decide whether to allow connections.
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(ks);
            sslContext.init(null, tmf.getTrustManagers(), null);

        } else {
            // KeyManager's decide which key material to use.
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, passphrase);
            sslContext.init(kmf.getKeyManagers(), null, null);
        }
        return sslContext;
    }

    private static void initializeCertificate(String host, int port) throws IOException {
        char[] passphrase = "changeit".toCharArray();
        char SEP = File.separatorChar;
        File securityDir = new File(System.getProperty("java.home") + SEP + "lib" + SEP + "security");
        File jssecacerts = new File(securityDir, "cacerts");
        System.out.println("file " + jssecacerts.getCanonicalPath());
        SSLSocketFactory factory = null;
        InputStream in = null;
        SavingTrustManager tm = null;
        KeyStore ks = null;
        SSLSocket socket = null;
        try {
            File certs = jssecacerts;
            ks = KeyStore.getInstance(KeyStore.getDefaultType());
            if (jssecacerts.exists() == false) {
                certs = new File(securityDir, "cacerts");
                in = new FileInputStream(certs);
                ks.load(in, "changeit".toCharArray());
            } else {
                in = new FileInputStream(certs);
                ks.load(in, passphrase);
            }
            in.close();
            SSLContext context = SSLContext.getInstance("TLS");
            TrustManagerFactory tmf =
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ks);
            X509TrustManager defaultTrustManager = (X509TrustManager) tmf.getTrustManagers()[0];
            tm = new SavingTrustManager(defaultTrustManager);
            context.init(null, new TrustManager[]{tm}, null);
            factory = context.getSocketFactory();
            socket = (SSLSocket) factory.createSocket(host, port);
            socket.setSoTimeout(15000);
            socket.startHandshake();
            CertificateUtilities.displayCertificates(tm.chain);
        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                X509Certificate[] chain = tm.chain;
                if (chain == null) {
                    throw new Exception(
                            "Unable to obtain server certificate chain");
                }
                MessageDigest sha1 = MessageDigest.getInstance("SHA1");
                MessageDigest md5 = MessageDigest.getInstance("MD5");
                int index = 0;
                for (X509Certificate cert : chain) {
                    sha1.update(cert.getEncoded());
                    md5.update(cert.getEncoded());
                    ks.setCertificateEntry(String.format("%s:%05d", host, port), cert);
                    CertificateUtilities.storeCertificate(jssecacerts, passphrase, ks);
                }
            } catch (Exception exx) {
            }
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ex1) {
                }
            }
        }

    }

    private static class SavingTrustManager implements X509TrustManager {

        private final X509TrustManager tm;
        private X509Certificate[] chain;

        SavingTrustManager(X509TrustManager tm) {
            this.tm = tm;
        }

        public X509Certificate[] getAcceptedIssuers() {
            throw new UnsupportedOperationException();
        }

        public void checkClientTrusted(X509Certificate[] chain, String authType) throws
                CertificateException {
            throw new UnsupportedOperationException();
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType) throws
                CertificateException {
            this.chain = chain;
            tm.checkServerTrusted(chain, authType);
        }
    }

    private static void displayCertificates(X509Certificate[] chain) throws
            NoSuchAlgorithmException, CertificateEncodingException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA1");
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        for (X509Certificate cert : chain) {
            sha1.update(cert.getEncoded());
            md5.update(cert.getEncoded());
        }

    }

    private static void storeCertificate(File jssecacerts, char[] passphrase,
            KeyStore ks) {
        OutputStream out = null;
        try {
            out = new FileOutputStream(jssecacerts);
            ks.store(out, passphrase);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ex1) {
                }
            }
        }
    }
}




