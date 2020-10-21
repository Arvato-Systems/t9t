/*
 * Copyright (c) 2012 - 2018 Arvato Systems GmbH
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
package com.arvatosystems.t9t.tfi.general;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InitServlet extends HttpServlet {


    /**
     * The <code>Log</code> instance for this application.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(InitServlet.class);


    public static final String CONFIGURATION = "configuration";
    public static final String CONFIGURATION_PROPERTIES = "configuration.properties";
    public static final String SYSTEM_PROPERTIES = "system.properties";


    private static final long serialVersionUID = 1L;


    public InitServlet() {
        LOGGER.info("InitServlet CONSTRUCTOR");
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        LOGGER.info("+++ "+ InitServlet.class.getSimpleName() +" is initializing application");

        // Int field can handle NULL inputs
        // if COERCE_TO_ZERO = true (default) all NULL int fields will be converted to 0
        // See: org/zkoss/zel/impl/parser/AstValue.java setValue(EvaluationContext ctx, Object value)
        //-Dorg.zkoss.zel.impl.parser.COERCE_TO_ZERO=false
        //System.setProperty("org.zkoss.zel.impl.parser.COERCE_TO_ZERO", "false");


        super.init(config);
        try {
            initializeEnvironment();
        } catch (IOException e) {
            LOGGER.error("Error while init application", e);
        }
    }



    /**
     * Reads the environment from the servlet context and initialize it.
     * @throws IOException
     */
    private void initializeEnvironment() throws IOException {
        // set environment parameter
        String sysenv = System.getProperty(CONFIGURATION);
        String contextenv = getServletContext().getInitParameter(CONFIGURATION);
        ApplicationUtil.setVersion(getMavenVersion(this.getClass()));
        LOGGER.info("*******************************************************");
        LOGGER.info("** Version: ");
        LOGGER.info("**      " + ApplicationUtil.getVersion());
        LOGGER.info("** Configuration: ");
        LOGGER.info("**      system  parameter: "+ sysenv + (sysenv != null ? " <- USED" : ""));
        LOGGER.info("**      context parameter: "+ contextenv +((contextenv != null) && (sysenv == null) ? " <- USED" : ""));
        setConfiguration(sysenv != null ? sysenv : contextenv);
        LOGGER.info("** Settings: ");
        LOGGER.info("**      file.encoding    : "+ System.getProperty("file.encoding"));
        setSystemProperties();
        setConfigProperties();
        LOGGER.info("*******************************************************");
    }

    private static String getMavenVersion(Class<?> clazz)  {
        try {
            return clazz.getPackage().getImplementationVersion();
        } catch (Exception e) {
            LOGGER.error("Caught exception while trying to determine the version.", e);
            return "n/a";
        }
    }

    private void setSystemProperties()  {
        String sysp = getServletContext().getInitParameter(SYSTEM_PROPERTIES);
        if (sysp == null) {
            return;
        }
        LOGGER.info("**      system.properties: ");
        String[] props = sysp.replaceAll("\n", "").split(";");
        for (String prop : props) {
            String[] keyVal = prop.split("=");
            if (keyVal.length != 2) {
                continue;
            }
            LOGGER.info("**                       :  {} = {}", keyVal[0].trim(), keyVal[1].trim());
            System.setProperty(keyVal[0].trim(), keyVal[1].trim());
        }

    }

    private void setConfigProperties()  {
        String sysp = System.getProperty((CONFIGURATION_PROPERTIES));
        if (sysp == null) {
            return;
        }
        Properties p = ApplicationUtil.getConfiguration();
        if (p == null) {
            p = new Properties();
        }

        LOGGER.info("**      configuration.properties: ");
        String[] props = sysp.split(",");
        for (String prop : props) {
            String[] keyVal = prop.split("=");
            if (keyVal.length != 2) {
                continue;
            }
            LOGGER.info("**                      :  {} = {}", keyVal[0].trim(), keyVal[1].trim());
            p.setProperty(keyVal[0].trim(), keyVal[1].trim());
        }
        ApplicationUtil.setConfiguration(p);
    }

    private void setConfiguration(String path) throws IOException {
        Properties config = new Properties();
        File configFile = null;
        if (checkFile(path)) {
            configFile =new File(path);
        } else if (checkFile(getServletRealPath() +"/"+ path)) {
            LOGGER.info("**      corrected path   : "+ (getServletRealPath() +"/"+ path));
            configFile =new File(getServletRealPath() +"/"+ path);
        } else {
            LOGGER.warn("**      NO CONFIGURATION FOUND");
            //configFile =new File(path);
        }
        if (configFile != null) {
            config.load(new FileInputStream(configFile));
        }
        ApplicationUtil.setConfiguration(config);
    }



    private boolean checkFile(String path) {
        if (path == null) {
            return false;
        }
        boolean exist;
        File file = new File(path);
        if (file.exists() && file.isFile()) {
            exist = true;
        } else {
            exist = false;
        }
        return exist;
    }

    public String getServletRealPath() {
        return this.getServletContext().getRealPath("");
    }

}
