package com.crisnello.notereader.util;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


public class Internet
{
    public static String postHttpEntity(String url, MyHttpEntity entity)
    {
        String result="";
       // url= Config.WS_BASE_URL+Config.WS_PATH_URL+url+".php";
        BufferedReader bufferedReader = null;
        Log.d("PostHttpEntity",url);


        try{
            URL obj = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);

            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.addRequestProperty("Content-length", entity.getContentLength() + "");
            conn.addRequestProperty(entity.getContentType().getName(), entity.getContentType().getValue());
            OutputStream os = conn.getOutputStream();
            entity.writeTo(conn.getOutputStream());
            os.close();
            conn.connect();


            //HttpResponse response = httpclient.execute(httppost);

            //Retorna o retultado Html em String
            bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuffer stringBuffer = new StringBuffer("");
            String line = "";
            String NL = System.getProperty("line.separator");

            while ((line = bufferedReader.readLine()) != null){
                stringBuffer.append(line + NL);
            }
            bufferedReader.close();

            Log.d("PostHttpEntity", stringBuffer.toString());

            result = stringBuffer.toString();

        }catch (IOException e)
        {
            e.printStackTrace();
        }

        return result;
    }

    public static String postHttp(String url, HashMap<String,String> map) {
        String result = "";

        //url = Config.WS_BASE_URL + Config.WS_PATH_URL + url + ".php";
        Log.e("URL: ", url);
        BufferedReader bufferedReader = null;
        HttpURLConnection con = null;

        HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;

        try {
            URL obj = new URL(url);
            con = (HttpURLConnection) obj.openConnection();

            //con.setDefaultHostnameVerifier(hostnameVerifier);

            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", "android");
            con.setRequestProperty("Accept-Language", "UTF-8");

            // Adiciona os valores para o post
            StringBuilder params = new StringBuilder("");

            for (Entry<String, String> item : map.entrySet()) {
                //Log.i("Internet.postHttp",item.getKey()+"="+item.getValue());
                params.append("&" + item.getKey() + "=");
                if (item.getValue() != null)
                    params.append(URLEncoder.encode(item.getValue(), "UTF-8"));
                else
                    params.append("");
            }

            con.setDoOutput(true);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(con.getOutputStream());
            outputStreamWriter.write(params.toString());

            outputStreamWriter.flush();

            int responseCode = con.getResponseCode();
            Log.e("Internet.postHttp", "Response Code: " + responseCode);

            //Retorna o retultado Html em String
            bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuffer stringBuffer = new StringBuffer("");
            String line = "";
            String NL = System.getProperty("line.separator");

            while ((line = bufferedReader.readLine()) != null){
                stringBuffer.append(line + NL);
            }
            bufferedReader.close();
            //Log.e("POST HTTP RETORNO", stringBuffer.toString());

            result = stringBuffer.toString();
            return result;
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        return result;
    }



    // always verify the host - dont check for certificate
    final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

    /**
     * Trust every server - dont check for any certificate
     */
    private static void trustAllHosts() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[] {};
            }

            public void checkClientTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
            }

            public void checkServerTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
            }
        } };

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection
                    .setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
