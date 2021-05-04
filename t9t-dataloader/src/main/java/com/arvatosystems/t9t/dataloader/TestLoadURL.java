/*
 * Copyright (c) 2012 - 2020 Arvato Systems GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.arvatosystems.t9t.dataloader;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("all")
// TODO either delete this class or remove the 1000 warnings
public class TestLoadURL {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestLoadURL.class);

    public static void main(String[] args) throws Throwable {

        String username = "ZINK04";
        String password = "ZINK04";

        String auth = username.concat(":").concat(password);

        final URL url = new URL(
                "http://degtlun2951.server.arvato-systems.de:8088/xwiki/bin/download/aroma42+Features/aroma42+Business+Docs+%28afds%29/Gesamt%2DPDF_Sommerflyer_2013.pdf");

        String encoding = Base64.getEncoder().encodeToString(auth.getBytes());
        final URLConnection conn = url.openConnection();
        conn.setRequestProperty("Authorization", "Basic " + encoding);
        final InputStream is = new BufferedInputStream(conn.getInputStream());
        final OutputStream os = new BufferedOutputStream(new FileOutputStream("test.docx"));
        byte[] chunk = new byte[1024];
        int chunkSize;
        while ((chunkSize = is.read(chunk)) != -1) {
            os.write(chunk, 0, chunkSize);
        }
        String myFileContent = new String(chunk, "UTF8");
        os.flush();
        os.close();
        is.close();

    }

    // public static void main(String[] args) {
    // // download the file
    // byte[] file = null;
    // String username = "ZINK04";
    // String password = "ZINK04";
    // String myUrl =
    // "http://degtlun2951.server.arvato-systems.de:8088/xwiki/bin/download/aroma42+Features/aroma42+Business+Docs+%28afds%29/FT%2D368%2DProcessFeedbackforInvoiceDocumentCreation.docx";
    //
    // HttpURLConnection connection = null;
    // OutputStreamWriter wr = null;
    // BufferedReader rd = null;
    // StringBuilder sb = null;
    // String line = null;
    //
    // URL serverAddress = null;
    //
    // try {
    // serverAddress = new
    // URL("http://degtlun2951.server.arvato-systems.de:8088/xwiki/bin/download/aroma42+Features/aroma42+Business+Docs+%28afds%29/FT%2D368%2DProcessFeedbackforInvoiceDocumentCreation.docx");
    // //set up out communications stuff
    // connection = null;
    //
    // //Set up the initial connection
    // connection = (HttpURLConnection)serverAddress.openConnection();
    // connection.setRequestMethod("GET");
    // connection.setDoOutput(true);
    // connection.setReadTimeout(10000);
    // connection.connect();
    //
    // //get the output stream writer and write the output to the server
    // //not needed in this example
    // //wr = new OutputStreamWriter(connection.getOutputStream());
    // //wr.write("");
    // //wr.flush();
    //
    // //read the result from the server
    // rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
    // sb = new StringBuilder();
    //
    // while ((line = rd.readLine()) != null)
    // {
    // sb.append(line + '\n');
    // }
    //
    // System.out.println(sb.toString());
    //
    // } catch (MalformedURLException e) {
    // e.printStackTrace();
    // } catch (ProtocolException e) {
    // e.printStackTrace();
    // } catch (IOException e) {
    // e.printStackTrace();
    // }
    // finally
    // {
    // //close the connection, set all objects to null
    // connection.disconnect();
    // rd = null;
    // sb = null;
    // wr = null;
    // connection = null;
    // }
    // }
    //
    //
    // try {
    // // read user and password from configuration
    // String auth = username.concat(":").concat(password);
    // URL url = new URL(myUrl);
    // String encoding = new sun.misc.BASE64Encoder().encode(auth.getBytes());
    // URLConnection uc = url.openConnection();
    // uc.setRequestProperty("Authorization", "Basic " + encoding);
    // InputStream data = (InputStream) uc.getInputStream();
    // data.read(file);
    // }
    // catch (Exception e) {
    // LOGGER.error("Exception occured during file download - Error Message ", e);
    // }

}
