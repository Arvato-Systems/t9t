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
package com.arvatosystems.t9t.out.be.impl;
//package com.arvatosystems.t9t.io.be.impl;
//
//import java.io.OutputStream;
//import java.sql.Connection;
//import java.util.Properties;
//
//import javax.jms.BytesMessage;
//import javax.jms.ConnectionFactory;
//import javax.jms.Destination;
//import javax.jms.JMSException;
//import javax.jms.MessageProducer;
//import javax.jms.XAConnectionFactory;
//import javax.naming.Context;
//import javax.naming.InitialContext;
//import javax.naming.NamingException;
//import javax.sql.XAConnection;
//
//import org.eclipse.persistence.sessions.Session;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import com.arvatosystems.t9t.core.T9tException;
//
///**
// * Implementation of {@linkplain OutputResource} which writes to JMS Queue and Topic.
// *
// * @author LIEE001
// */
//public class QueueTopicOutputResource implements OutputResource {
//
//    private static final Logger LOGGER = LoggerFactory.getLogger(QueueTopicOutputResource.class);
//
//    private String jmsJndiInitialContextFactory;
//    private String jmsJndiProviderUrl;
//    private String jmsConnectionFactoryName;
//
//    private Connection connection;
//    private XAConnection xaConnection;
//    private Session session;
//    private MessageProducer messageProducer;
//
//    private String queueOrTopicName;
//    private Destination destination;
//
//    /**
//     * Constructor.
//     * @param jmsJndiInitialContextFactory JNDI initial context factory class to connect to remote JMS server
//     * @param jmsJndiProviderUrl JNDI url to connect to remote JMS server
//     * @param jmsConnectionFactoryName JMS connection factory name to create connection for queue/topic
//     * @param queueOrTopicName target queue or topic
//     */
//    public QueueTopicOutputResource(final String jmsJndiInitialContextFactory, final String jmsJndiProviderUrl,
//            final String jmsConnectionFactoryName, final String queueOrTopicName) {
//        this.jmsJndiInitialContextFactory = jmsJndiInitialContextFactory;
//        this.jmsJndiProviderUrl = jmsJndiProviderUrl;
//        this.jmsConnectionFactoryName = jmsConnectionFactoryName;
//        this.queueOrTopicName = queueOrTopicName;
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public void open() {
//        ConnectionFactory connectionFactory = null;
//        XAConnectionFactory xaConnectionFactory = null;
//
//        // here we need to check if bounded object to JNDI under jmsConnectionFactoryName
//        // is actually a XAConnectionFactory or normal ConnectionFactory
//        // the reason is because in some implementation of JMS provider e.g. Glassfish OpenMQ
//        // it actually only supports ConnectionFactory (from interface point of view)
//        // but we can configure the ConnectionFactory to use XATransaction hence resulting in
//        // a ConnectionFactory instance that is JTA aware (but the interface is NOT XAConnectionFactory)
//        // priority is to use XAConnectionFactory then fallback to ConnectionFactory if it's not possible
//        try {
//
//            // create JNDI context to lookup for JMS resources
//            // there can be 2 scenarios here:
//            // - fortytwo is hosted inside a full blown application server with the JMS server embedded inside application server
//            //   in which they are sharing the same JNDI context
//            // - fortytwo is hosted inside a full blown application server but using remote JMS server e.g. GlassFish with remote HornetQ
//            //   in which we need to give the JNDI url for the context
//            Context context;
//            if (((jmsJndiInitialContextFactory != null) && !jmsJndiInitialContextFactory.isEmpty()) &&
//                    ((jmsJndiProviderUrl != null) && !jmsJndiProviderUrl.isEmpty())) {
//                Properties contextProps = new Properties();
//                contextProps.put(Context.INITIAL_CONTEXT_FACTORY, jmsJndiInitialContextFactory);
//                contextProps.put(Context.PROVIDER_URL, jmsJndiProviderUrl);
//
//                context = new InitialContext(contextProps);
//            } else {
//                context = new InitialContext();
//            }
//
//            Object possibleConnFactory = context.lookup(jmsConnectionFactoryName);
//            if (possibleConnFactory instanceof XAConnectionFactory) {
//                xaConnectionFactory = (XAConnectionFactory) possibleConnFactory;
//            } else if (possibleConnFactory instanceof ConnectionFactory) {
//                connectionFactory = (ConnectionFactory) possibleConnFactory;
//                // warn user to make sure they know what they're doing here
//                LOGGER.warn("Expecting a XAConnectionFactory but found a ConnectionFactory instead. Please make sure this ConnectionFactory is JTA aware.");
//            }
//
//            if ((connectionFactory == null) && (xaConnectionFactory == null)) {
//                LOGGER.error("Bounded object in JNDI for JSM connection factory is not a XAConnectionFactory or ConnectionFactory");
//                throw new T9tException(T9tException.OUTPUT_JMS_EXCEPTION,
//                        String.format("Bounded object in %s is not a JMS connection factory", jmsConnectionFactoryName));
//            }
//
//            destination = (Destination) context.lookup(queueOrTopicName);
//        } catch (NamingException ex) {
//            LOGGER.error(String.format("Failed to connect to queue/topic. Queue/topic %s is not exist", queueOrTopicName), ex);
//            throw new T9tException(T9tException.OUTPUT_JMS_EXCEPTION, "Queue/topic is not exist");
//        }
//
//        // obtain connection and session
//        try {
//            if (xaConnectionFactory != null) {
//                xaConnection = xaConnectionFactory.createXAConnection();
//            } else if (connectionFactory != null) {
//                connection = connectionFactory.createConnection();
//            }
//
//            if (xaConnection != null) {
//                session = xaConnection.createXASession();
//            } else {
//                // this can resulted in non-JTA aware JMS session
//                // however this can resulted in JTA aware JMS session is well
//                // (see above explanation)
//                session = connection.createSession(true, 0);
//            }
//
//            messageProducer = session.createProducer(destination);
//        } catch (JMSException ex) {
//            LOGGER.error("Failed to acquire connection to JMS provider", ex);
//            throw new T9tException(T9tException.OUTPUT_JMS_EXCEPTION, "Can't acquire connection to JMS provider");
//        }
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public void write(final byte[] data) {
//        try {
//            BytesMessage msg = session.createBytesMessage();
//            msg.writeBytes(data);
//
//            messageProducer.send(msg);
//        } catch (JMSException ex) {
//            LOGGER.error("Failed to send bytes message to JMS provider", ex);
//            throw new T9tException(T9tException.OUTPUT_JMS_EXCEPTION, "Can't send bytes message to JMS provider");
//        }
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public void write(final String data) {
//        try {
//            messageProducer.send(session.createTextMessage(data));
//        } catch (JMSException ex) {
//            LOGGER.error("Failed to send text message to JMS provider", ex);
//            throw new T9tException(T9tException.OUTPUT_JMS_EXCEPTION, "Can't send text message to JMS provider");
//        }
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public void close() {
//        try {
//            session.close();
//            if (xaConnection != null) {
//                xaConnection.close();
//            }
//
//            if (connection != null) {
//                connection.close();
//            }
//        } catch (JMSException ex) {
//            LOGGER.error("Failed to close JMS connection", ex);
//            throw new T9tException(T9tException.OUTPUT_JMS_EXCEPTION, "Can't close JMS connection");
//        }
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public OutputStream getOutputStream() {
//        return null; // for queues or topics, there is no single OutputStream at the moment. A future extension would provide a stream which writes directly
//                     // into a message.
//    }
//}
