/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.mail;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * The base class for all email messages.  This class sets the
 * sender's email & name, receiver's email & name, subject, and the
 * sent date.  Subclasses are responsible for setting the message
 * body.
 *
 * @since 1.0
 * @author <a href="mailto:quintonm@bellsouth.net">Quinton McCombs</a>
 * @author <a href="mailto:jon@latchkey.com">Jon S. Stevens</a>
 * @author <a href="mailto:frank.kim@clearink.com">Frank Y. Kim</a>
 * @author <a href="mailto:bmclaugh@algx.net">Brett McLaughlin</a>
 * @author <a href="mailto:greg@shwoop.com">Greg Ritter</a>
 * @author <a href="mailto:unknown">Regis Koenig</a>
 * @author <a href="mailto:colin.chalmers@maxware.nl">Colin Chalmers</a>
 * @author <a href="mailto:matthias@wessendorf.net">Matthias Wessendorf</a>
 * @author <a href="mailto:corey.scott@gmail.com">Corey Scott</a>
 * @version $Revision$ $Date$
 * @version $Id$
 */
public abstract class Email implements EmailConstants
{
    /** The email message to send. */
    protected MimeMessage message;

    /** The charset to use for this message */
    protected String charset;

    /** The Address of the sending party, mandatory */
    protected InternetAddress fromAddress;

    /** The Subject  */
    protected String subject;

    /** An attachment  */
    protected MimeMultipart emailBody;

    /** The content  */
    protected Object content;

    /** The content type  */
    protected String contentType;

    /** Set session debugging on or off */
    protected boolean debug;

    /** Sent date */
    protected Date sentDate;

    /**
     * Instance of an <code>Authenticator</code> object that will be used
     * when authentication is requested from the mail server.
     */
    protected Authenticator authenticator;

    /**
     * The hostname of the mail server with which to connect. If null will try
     * to get property from system.properties. If still null, quit
     */
    protected String hostName;

    /**
     * The port number of the mail server to connect to.
     * Defaults to the standard port ( 25 ).
     */
    protected String smtpPort = "25";

    /**
     * The port number of the SSL enabled SMTP server;
     * defaults to the standard port, 465.
     */
    protected String sslSmtpPort = "465";

    /** List of "to" email adresses */
    protected List toList = new ArrayList();

    /** List of "cc" email adresses */
    protected List ccList = new ArrayList();

    /** List of "bcc" email adresses */
    protected List bccList = new ArrayList();

    /** List of "replyTo" email adresses */
    protected List replyList = new ArrayList();

    /**
     * Address to which undeliverable mail should be sent.
     * Because this is handled by JavaMail as a String property
     * in the mail session, this property is of type <code>String</code>
     * rather than <code>InternetAddress</code>.
     */
    protected String bounceAddress;

    /**
     * Used to specify the mail headers.  Example:
     *
     * X-Mailer: Sendmail, X-Priority: 1( highest )
     * or  2( high ) 3( normal ) 4( low ) and 5( lowest )
     * Disposition-Notification-To: user@domain.net
     */
    protected Map headers = new HashMap();

    /**
     * Used to determine whether to use pop3 before smtp, and if so the settings.
     */
    protected boolean popBeforeSmtp;

    /** the host name of the pop3 server */
    protected String popHost;

    /** the user name to log into the pop3 server */
    protected String popUsername;

    /** the password to log into the pop3 server */
    protected String popPassword;

    /** does client want STARTTLS encryption */
    protected boolean startTlsEnabled;

    /** does client require STARTTLS encryption */
    protected boolean startTlsRequired;

    /** does the current transport use SSL/TLS encryption upon connection? */
    protected boolean sslOnConnect;

    /** socket I/O timeout value in milliseconds */
    protected int socketTimeout = SOCKET_TIMEOUT_MS;

    /** socket connection timeout value in milliseconds */
    protected int socketConnectionTimeout = SOCKET_TIMEOUT_MS;

    /** The Session to mail with */
    private Session session;

    /**
     * Setting to true will enable the display of debug information.
     *
     * @param d A boolean.
     * @return An Email.
     * @since 1.0
     */
    public Email setDebug(boolean d)
    {
        this.debug = d;
        return this;
    }

    /**
     * Sets the userName and password if authentication is needed.  If this
     * method is not used, no authentication will be performed.
     * <p>
     * This method will create a new instance of
     * <code>DefaultAuthenticator</code> using the supplied parameters.
     *
     * @param userName User name for the SMTP server
     * @param password password for the SMTP server
     * @return An Email.
     * @see DefaultAuthenticator
     * @see #setAuthenticator(Authenticator)
     * @since 1.0
     */
    public Email setAuthentication(String userName, String password)
    {
        return this.setAuthenticator(new DefaultAuthenticator(userName, password));
    }

    /**
     * Sets the <code>Authenticator</code> to be used when authentication
     * is requested from the mail server.
     * <p>
     * This method should be used when your outgoing mail server requires
     * authentication.  Your mail server must also support RFC2554.
     *
     * @param newAuthenticator the <code>Authenticator</code> object.
     * @return Email.
     * @see Authenticator
     * @since 1.0
     */
    public Email setAuthenticator(Authenticator newAuthenticator)
    {
        this.authenticator = newAuthenticator;
        return this;
    }

    /**
     * Set the charset of the message. Please note that you should set the charset before
     * adding the message content.
     *
     * @param newCharset A String.
     * @throws java.nio.charset.IllegalCharsetNameException if the charset name is invalid
     * @throws java.nio.charset.UnsupportedCharsetException if no support for the named charset
     * exists in the current JVM
     * @return An Email.
     * @since 1.0
     */
    public Email setCharset(String newCharset)
    {
        Charset set = Charset.forName(newCharset);
        this.charset = set.name();
        return this;
    }

    /**
     * Set the emailBody to a MimeMultiPart
     *
     * @param aMimeMultipart aMimeMultipart
     * @return An Email.
     * @since 1.0
     */
    public Email setContent(MimeMultipart aMimeMultipart)
    {
        this.emailBody = aMimeMultipart;
        return this;
    }

    /**
     * Set the content & contentType
     *
     * @param  aObject aObject
     * @param  aContentType aContentType
     * @return An Email.
     * @since 1.0
     */
    public Email setContent(Object aObject, String aContentType)
    {
        this.content = aObject;
        this.updateContentType(aContentType);
        return this;
    }

    /**
     * Update the contentType.
     *
     * @param   aContentType aContentType
     * @since 1.2
     */
    public void updateContentType(final String aContentType)
    {
        if (EmailUtils.isEmpty(aContentType))
        {
            this.contentType = null;
        }
        else
        {
            // set the content type
            this.contentType = aContentType;

            // set the charset if the input was properly formed
            String strMarker = "; charset=";
            int charsetPos = aContentType.toLowerCase().indexOf(strMarker);

            if (charsetPos != -1)
            {
                // find the next space (after the marker)
                charsetPos += strMarker.length();
                int intCharsetEnd =
                    aContentType.toLowerCase().indexOf(" ", charsetPos);

                if (intCharsetEnd != -1)
                {
                    this.charset =
                        aContentType.substring(charsetPos, intCharsetEnd);
                }
                else
                {
                    this.charset = aContentType.substring(charsetPos);
                }
            }
            else
            {
                // use the default charset, if one exists, for messages
                // whose content-type is some form of text.
                if (this.contentType.startsWith("text/") && EmailUtils.isNotEmpty(this.charset))
                {
                    StringBuffer contentTypeBuf = new StringBuffer(this.contentType);
                    contentTypeBuf.append(strMarker);
                    contentTypeBuf.append(this.charset);
                    this.contentType = contentTypeBuf.toString();
                }
            }
        }
    }

    /**
     * Set the hostname of the outgoing mail server
     *
     * @param   aHostName aHostName
     * @return An Email.
     * @since 1.0
     */
    public Email setHostName(String aHostName)
    {
        checkSessionAlreadyInitialized();
        this.hostName = aHostName;
        return this;
    }

    /**
     * Set or disable the STARTTLS encryption. Please see EMAIL-105
     * for the reasons of deprecation.
     *
     * @deprecated since 1.3
     * @param startTlsEnabled true if STARTTLS requested, false otherwise
     * @return An Email.
     * @since 1.1
     */
    public Email setTLS(boolean startTlsEnabled)
    {
        return setStartTLSEnabled(startTlsEnabled);
    }

    /**
     * Set or disable the STARTTLS encryption.
     *
     * @param startTlsEnabled true if STARTTLS requested, false otherwise
     * @return An Email.
     */
    public Email setStartTLSEnabled(boolean startTlsEnabled)
    {
        checkSessionAlreadyInitialized();
        this.startTlsEnabled = startTlsEnabled;
        return this;
    }

    /**
     * Set or disable the STARTTLS encryption.
     *
     * @param startTlsRequired true if STARTTLS requested, false otherwise
     * @return An Email.
     */
    public Email setStartTLSRequired(boolean startTlsRequired)
    {
        checkSessionAlreadyInitialized();
        this.startTlsRequired = startTlsRequired;
        return this;
    }

    /**
     * Set the port number of the outgoing mail server.
     *
     * @param  aPortNumber aPortNumber
     * @return An Email.
     * @since 1.0
     */
    public Email setSmtpPort(int aPortNumber)
    {
        checkSessionAlreadyInitialized();

        if (aPortNumber < 1)
        {
            throw new IllegalArgumentException(
                "Cannot connect to a port number that is less than 1 ( "
                    + aPortNumber
                    + " )");
        }

        this.smtpPort = Integer.toString(aPortNumber);
        return this;
    }

    /**
     * Supply a mail Session object to use. Please note that passing
     * a user name and password (in the case of mail authentication) will
     * create a new mail session with a DefaultAuthenticator. This is a
     * convenience but might come unexpected.
     *
     * If mail authentication is used but NO username and password
     * is supplied the implementation assumes that you have set a
     * authenticator and will use the existing mail session (as expected).
     *
     * @param aSession mail session to be used
     * @return An Email.
     * @since 1.0
     */
    public Email setMailSession(Session aSession)
    {
        EmailUtils.notNull(aSession, "no mail session supplied");

        Properties sessionProperties = aSession.getProperties();
        String auth = sessionProperties.getProperty(MAIL_SMTP_AUTH);

        if ("true".equalsIgnoreCase(auth))
        {
            String userName = sessionProperties.getProperty(MAIL_SMTP_USER);
            String password = sessionProperties.getProperty(MAIL_SMTP_PASSWORD);

            if (EmailUtils.isNotEmpty(userName) && EmailUtils.isNotEmpty(password))
            {
                // only create a new mail session with an authenticator if
                // authentication is required and no user name is given
                this.authenticator = new DefaultAuthenticator(userName, password);
                this.session = Session.getInstance(sessionProperties, this.authenticator);
            }
            else
            {
                // assume that the given mail session contains a working authenticator
                this.session = aSession;
            }
        }
        else
        {
            this.session = aSession;
        }

        return this;
    }

    /**
     * Supply a mail Session object from a JNDI directory.
     *
     * @param jndiName name of JNDI ressource (javax.mail.Session type), ressource
     * if searched in java:comp/env if name dont start with "java:"
     * @return An Email.
     * @throws IllegalArgumentException JNDI name null or empty
     * @throws NamingException ressource can be retrieved from JNDI directory
     * @since 1.1
     */
    public Email setMailSessionFromJNDI(String jndiName) throws NamingException
    {
        if (EmailUtils.isEmpty(jndiName))
        {
            throw new IllegalArgumentException("JNDI name missing");
        }
        Context ctx = null;
        if (jndiName.startsWith("java:"))
        {
            ctx = new InitialContext();
        }
        else
        {
            ctx = (Context) new InitialContext().lookup("java:comp/env");

        }
        this.setMailSession((Session) ctx.lookup(jndiName));
        return this;
    }

    /**
     * Determines the mail session used when sending this Email, creating
     * the Session if necessary. When a mail session is already
     * initialized setting the session related properties will cause
     * an IllegalStateException.
     *
     * @return A Session.
     * @throws EmailException thrown when host name was not set.
     * @since 1.0
     */
    public Session getMailSession() throws EmailException
    {
        if (this.session == null)
        {
            Properties properties = new Properties(System.getProperties());
            properties.setProperty(MAIL_TRANSPORT_PROTOCOL, SMTP);

            if (EmailUtils.isEmpty(this.hostName))
            {
                this.hostName = properties.getProperty(MAIL_HOST);
            }

            if (EmailUtils.isEmpty(this.hostName))
            {
                throw new EmailException(
                    "Cannot find valid hostname for mail session");
            }

            properties.setProperty(MAIL_PORT, this.smtpPort);
            properties.setProperty(MAIL_HOST, this.hostName);
            properties.setProperty(MAIL_DEBUG, String.valueOf(this.debug));

            properties.setProperty(MAIL_TRANSPORT_STARTTLS_ENABLE, startTlsEnabled ? "true" : "false");
            properties.setProperty(MAIL_TRANSPORT_STARTTLS_REQUIRED, startTlsRequired ? "true" : "false");

            if (this.authenticator != null)
            {
                properties.setProperty(MAIL_SMTP_AUTH, "true");
            }

            if (this.sslOnConnect || this.startTlsEnabled || this.startTlsRequired)
            {
                properties.setProperty(MAIL_SMTP_SSL_SOCKET_FACTORY_PORT, this.sslSmtpPort);
                properties.setProperty(MAIL_SMTP_SSL_SOCKET_FACTORY_CLASS, "javax.net.ssl.SSLSocketFactory");
                properties.setProperty(MAIL_SMTP_SOCKET_FACTORY_FALLBACK, "false");
                properties.put(MAIL_SMTP_SSL_CHECKSERVERIDENTITY, Boolean.TRUE);
            }

            if (this.sslOnConnect)
            {
                properties.put(MAIL_SMTP_SSL_ENABLE, Boolean.TRUE);
                properties.setProperty(MAIL_PORT, this.sslSmtpPort);
                properties.setProperty(MAIL_SMTP_SOCKET_FACTORY_PORT, this.sslSmtpPort);
                properties.setProperty(MAIL_SMTP_SOCKET_FACTORY_CLASS, "javax.net.ssl.SSLSocketFactory");
                properties.setProperty(MAIL_SMTP_SOCKET_FACTORY_FALLBACK, "false");
            }

            if (this.bounceAddress != null)
            {
                properties.setProperty(MAIL_SMTP_FROM, this.bounceAddress);
            }

            if (this.socketTimeout > 0)
            {
                properties.setProperty(MAIL_SMTP_TIMEOUT, Integer.toString(this.socketTimeout));
            }

            if (this.socketConnectionTimeout > 0)
            {
                properties.setProperty(MAIL_SMTP_CONNECTIONTIMEOUT, Integer.toString(this.socketConnectionTimeout));
            }

            // changed this (back) to getInstance due to security exceptions
            // caused when testing using maven
            this.session = Session.getInstance(properties, this.authenticator);
        }
        return this.session;
    }

    /**
     * Set the FROM field of the email to use the specified address. The email
     * address will also be used as the personal name.
     * The name will be encoded by the charset of {@link #setCharset(java.lang.String) setCharset()}.
     * If it is not set, it will be encoded using
     * the Java platform's default charset (UTF-16) if it contains
     * non-ASCII characters; otherwise, it is used as is.
     *
     * @param email A String.
     * @return An Email.
     * @throws EmailException Indicates an invalid email address.
     * @since 1.0
     */
    public Email setFrom(String email)
        throws EmailException
    {
        return setFrom(email, null);
    }

    /**
     * Set the FROM field of the email to use the specified address and the
     * specified personal name.
     * The name will be encoded by the charset of {@link #setCharset(java.lang.String) setCharset()}.
     * If it is not set, it will be encoded using
     * the Java platform's default charset (UTF-16) if it contains
     * non-ASCII characters; otherwise, it is used as is.
     *
     * @param email A String.
     * @param name A String.
     * @throws EmailException Indicates an invalid email address.
     * @return An Email.
     * @since 1.0
     */
    public Email setFrom(String email, String name)
        throws EmailException
    {
        return setFrom(email, name, this.charset);
    }

    /**
     * Set the FROM field of the email to use the specified address, personal
     * name, and charset encoding for the name.
     *
     * @param email A String.
     * @param name A String.
     * @param charset The charset to encode the name with.
     * @throws EmailException Indicates an invalid email address or charset.
     * @return An Email.
     * @since 1.1
     */
    public Email setFrom(String email, String name, String charset)
        throws EmailException
    {
        this.fromAddress = createInternetAddress(email, name, charset);
        return this;
    }

    /**
     * Add a recipient TO to the email. The email
     * address will also be used as the personal name.
     * The name will be encoded by the charset of
     * {@link #setCharset(java.lang.String) setCharset()}.
     * If it is not set, it will be encoded using
     * the Java platform's default charset (UTF-16) if it contains
     * non-ASCII characters; otherwise, it is used as is.
     *
     * @param email A String.
     * @throws EmailException Indicates an invalid email address.
     * @return An Email.
     * @since 1.0
     */
    public Email addTo(String email)
        throws EmailException
    {
        return addTo(email, null);
    }

    /**
     * Add a recipient TO to the email using the specified address and the
     * specified personal name.
     * The name will be encoded by the charset of
     * {@link #setCharset(java.lang.String) setCharset()}.
     * If it is not set, it will be encoded using
     * the Java platform's default charset (UTF-16) if it contains
     * non-ASCII characters; otherwise, it is used as is.
     *
     * @param email A String.
     * @param name A String.
     * @throws EmailException Indicates an invalid email address.
     * @return An Email.
     * @since 1.0
     */
    public Email addTo(String email, String name)
        throws EmailException
    {
        return addTo(email, name, this.charset);
    }

    /**
     * Add a recipient TO to the email using the specified address, personal
     * name, and charset encoding for the name.
     *
     * @param email A String.
     * @param name A String.
     * @param charset The charset to encode the name with.
     * @throws EmailException Indicates an invalid email address or charset.
     * @return An Email.
     * @since 1.1
     */
    public Email addTo(String email, String name, String charset)
        throws EmailException
    {
        this.toList.add(createInternetAddress(email, name, charset));
        return this;
    }

    /**
     * Set a list of "TO" addresses. All elements in the specified
     * <code>Collection</code> are expected to be of type
     * <code>java.mail.internet.InternetAddress</code>.
     *
     * @param  aCollection collection of <code>InternetAddress</code> objects.
     * @throws EmailException Indicates an invalid email address.
     * @return An Email.
     * @see javax.mail.internet.InternetAddress
     * @since 1.0
     */
    public Email setTo(Collection aCollection) throws EmailException
    {
        if (aCollection == null || aCollection.isEmpty())
        {
            throw new EmailException("Address List provided was invalid");
        }

        this.toList = new ArrayList(aCollection);
        return this;
    }

    /**
     * Add a recipient CC to the email. The email
     * address will also be used as the personal name.
     * The name will be encoded by the charset of {@link #setCharset(java.lang.String) setCharset()}.
     * If it is not set, it will be encoded using
     * the Java platform's default charset (UTF-16) if it contains
     * non-ASCII characters; otherwise, it is used as is.
     *
     * @param email A String.
     * @return An Email.
     * @throws EmailException Indicates an invalid email address.
     * @since 1.0
     */
    public Email addCc(String email)
        throws EmailException
    {
        return this.addCc(email, null);
    }

    /**
     * Add a recipient CC to the email using the specified address and the
     * specified personal name.
     * The name will be encoded by the charset of {@link #setCharset(java.lang.String) setCharset()}.
     * If it is not set, it will be encoded using
     * the Java platform's default charset (UTF-16) if it contains
     * non-ASCII characters; otherwise, it is used as is.
     *
     * @param email A String.
     * @param name A String.
     * @throws EmailException Indicates an invalid email address.
     * @return An Email.
     * @since 1.0
     */
    public Email addCc(String email, String name)
        throws EmailException
    {
        return addCc(email, name, this.charset);
    }

    /**
     * Add a recipient CC to the email using the specified address, personal
     * name, and charset encoding for the name.
     *
     * @param email A String.
     * @param name A String.
     * @param charset The charset to encode the name with.
     * @throws EmailException Indicates an invalid email address or charset.
     * @return An Email.
     * @since 1.1
     */
    public Email addCc(String email, String name, String charset)
        throws EmailException
    {
        this.ccList.add(createInternetAddress(email, name, charset));
        return this;
    }

    /**
     * Set a list of "CC" addresses. All elements in the specified
     * <code>Collection</code> are expected to be of type
     * <code>java.mail.internet.InternetAddress</code>.
     *
     * @param aCollection collection of <code>InternetAddress</code> objects.
     * @return An Email.
     * @throws EmailException Indicates an invalid email address.
     * @see javax.mail.internet.InternetAddress
     * @since 1.0
     */
    public Email setCc(Collection aCollection) throws EmailException
    {
        if (aCollection == null || aCollection.isEmpty())
        {
            throw new EmailException("Address List provided was invalid");
        }

        this.ccList = new ArrayList(aCollection);
        return this;
    }

    /**
     * Add a blind BCC recipient to the email. The email
     * address will also be used as the personal name.
     * The name will be encoded by the charset of {@link #setCharset(java.lang.String) setCharset()}.
     * If it is not set, it will be encoded using
     * the Java platform's default charset (UTF-16) if it contains
     * non-ASCII characters; otherwise, it is used as is.
     *
     * @param email A String.
     * @return An Email.
     * @throws EmailException Indicates an invalid email address
     * @since 1.0
     */
    public Email addBcc(String email)
        throws EmailException
    {
        return this.addBcc(email, null);
    }

    /**
     * Add a blind BCC recipient to the email using the specified address and
     * the specified personal name.
     * The name will be encoded by the charset of {@link #setCharset(java.lang.String) setCharset()}.
     * If it is not set, it will be encoded using
     * the Java platform's default charset (UTF-16) if it contains
     * non-ASCII characters; otherwise, it is used as is.
     *
     * @param email A String.
     * @param name A String.
     * @return An Email.
     * @throws EmailException Indicates an invalid email address
     * @since 1.0
     */
    public Email addBcc(String email, String name)
        throws EmailException
    {
        return addBcc(email, name, this.charset);
    }

    /**
     * Add a blind BCC recipient to the email using the specified address,
     * personal name, and charset encoding for the name.
     *
     * @param email A String.
     * @param name A String.
     * @param charset The charset to encode the name with.
     * @return An Email.
     * @throws EmailException Indicates an invalid email address
     * @since 1.1
     */
    public Email addBcc(String email, String name, String charset)
        throws EmailException
    {
        this.bccList.add(createInternetAddress(email, name, charset));
        return this;
    }

    /**
     * Set a list of "BCC" addresses. All elements in the specified
     * <code>Collection</code> are expected to be of type
     * <code>java.mail.internet.InternetAddress</code>.
     *
     * @param  aCollection collection of <code>InternetAddress</code> objects
     * @return An Email.
     * @throws EmailException Indicates an invalid email address
     * @see javax.mail.internet.InternetAddress
     * @since 1.0
     */
    public Email setBcc(Collection aCollection) throws EmailException
    {
        if (aCollection == null || aCollection.isEmpty())
        {
            throw new EmailException("Address List provided was invalid");
        }

        this.bccList = new ArrayList(aCollection);
        return this;
    }

    /**
     * Add a reply to address to the email. The email
     * address will also be used as the personal name.
     * The name will be encoded by the charset of {@link #setCharset(java.lang.String) setCharset()}.
     * If it is not set, it will be encoded using
     * the Java platform's default charset (UTF-16) if it contains
     * non-ASCII characters; otherwise, it is used as is.
     *
     * @param email A String.
     * @return An Email.
     * @throws EmailException Indicates an invalid email address
     * @since 1.0
     */
    public Email addReplyTo(String email)
        throws EmailException
    {
        return this.addReplyTo(email, null);
    }

    /**
     * Add a reply to address to the email using the specified address and
     * the specified personal name.
     * The name will be encoded by the charset of {@link #setCharset(java.lang.String) setCharset()}.
     * If it is not set, it will be encoded using
     * the Java platform's default charset (UTF-16) if it contains
     * non-ASCII characters; otherwise, it is used as is.
     *
     * @param email A String.
     * @param name A String.
     * @return An Email.
     * @throws EmailException Indicates an invalid email address
     * @since 1.0
     */
    public Email addReplyTo(String email, String name)
        throws EmailException
    {
        return addReplyTo(email, name, this.charset);
    }

    /**
     * Add a reply to address to the email using the specified address,
     * personal name, and charset encoding for the name.
     *
     * @param email A String.
     * @param name A String.
     * @param charset The charset to encode the name with.
     * @return An Email.
     * @throws EmailException Indicates an invalid email address or charset.
     * @since 1.1
     */
    public Email addReplyTo(String email, String name, String charset)
        throws EmailException
    {
        this.replyList.add(createInternetAddress(email, name, charset));
        return this;
    }

    /**
     * Set a list of reply to addresses. All elements in the specified
     * <code>Collection</code> are expected to be of type
     * <code>java.mail.internet.InternetAddress</code>.
     *
     * @param   aCollection collection of <code>InternetAddress</code> objects
     * @return  An Email.
     * @throws EmailException Indicates an invalid email address
     * @see javax.mail.internet.InternetAddress
     * @since 1.1
     */
    public Email setReplyTo(Collection aCollection) throws EmailException
    {
        if (aCollection == null || aCollection.isEmpty())
        {
            throw new EmailException("Address List provided was invalid");
        }

        this.replyList = new ArrayList(aCollection);
        return this;
    }

    /**
     * Used to specify the mail headers.  Example:
     *
     * X-Mailer: Sendmail, X-Priority: 1( highest )
     * or  2( high ) 3( normal ) 4( low ) and 5( lowest )
     * Disposition-Notification-To: user@domain.net
     *
     * @param map A Map.
     * @return An Email.
     * @since 1.0
     */
    public Email setHeaders(Map map)
    {
        this.headers.clear();

        Iterator iterKeyBad = map.entrySet().iterator();

        while (iterKeyBad.hasNext())
        {
            Map.Entry entry = (Map.Entry) iterKeyBad.next();
            String name = (String) entry.getKey();
            this.headers.put(name, createFoldedHeaderValue(name, entry.getValue()));
        }

        return this;
    }

    /**
     * Adds a header ( name, value ) to the headers Map.
     *
     * @param name A String with the name.
     * @param value A String with the value.
     * @since 1.0
     */
    public void addHeader(String name, String value)
    {
        if (EmailUtils.isEmpty(name))
        {
            throw new IllegalArgumentException("name can not be null");
        }
        if (EmailUtils.isEmpty(value))
        {
            throw new IllegalArgumentException("value can not be null");
        }

        this.headers.put(name, createFoldedHeaderValue(name, value));
    }

    /**
     * Set the email subject.
     *
     * @param aSubject A String.
     * @return An Email.
     * @since 1.0
     */
    public Email setSubject(String aSubject)
    {
        this.subject = aSubject;
        return this;
    }

    /**
     * Set the "bounce address" - the address to which undeliverable messages
     * will be returned.  If this value is never set, then the message will be
     * sent to the address specified with the System property "mail.smtp.from",
     * or if that value is not set, then to the "from" address.
     *
     * @param email A String.
     * @return An Email.
     * @since 1.0
     */
    public Email setBounceAddress(String email)
    {
        checkSessionAlreadyInitialized();
        this.bounceAddress = email;
        return this;
    }


    /**
     * Define the content of the mail. It should be overidden by the
     * subclasses.
     *
     * @param msg A String.
     * @return An Email.
     * @throws EmailException generic exception.
     * @since 1.0
     */
    public abstract Email setMsg(String msg) throws EmailException;

    /**
     * Does the work of actually building the MimeMessage. Please note that
     * a user rarely calls this method directly and only if he/she is
     * interested in the sending the underlying MimeMessage without
     * commons-email.
     *
     * @exception EmailException if there was an error.
     * @since 1.0
     */
    public void buildMimeMessage() throws EmailException
    {
        if (this.message != null)
        {
            // [EMAIL-95] we assume that an email is not reused therefore invoking
            // buildMimeMessage() more than once is illegal.
            throw new IllegalStateException("The MimeMessage is already built.");
        }

        try
        {
            this.message = this.createMimeMessage(this.getMailSession());

            if (EmailUtils.isNotEmpty(this.subject))
            {
                if (EmailUtils.isNotEmpty(this.charset))
                {
                    this.message.setSubject(this.subject, this.charset);
                }
                else
                {
                    this.message.setSubject(this.subject);
                }
            }

            // update content type (and encoding)
            this.updateContentType(this.contentType);

            if (this.content != null)
            {
                this.message.setContent(this.content, this.contentType);
            }
            else if (this.emailBody != null)
            {
                if (this.contentType == null)
                {
                    this.message.setContent(this.emailBody);
                }
                else
                {
                    this.message.setContent(this.emailBody, this.contentType);
                }
            }
            else
            {
                this.message.setContent("", Email.TEXT_PLAIN);
            }

            if (this.fromAddress != null)
            {
                this.message.setFrom(this.fromAddress);
            }
            else
            {
                if (session.getProperty(MAIL_SMTP_FROM) == null)
                {
                    throw new EmailException("From address required");
                }
            }

            if (this.toList.size() + this.ccList.size() + this.bccList.size() == 0)
            {
                throw new EmailException(
                            "At least one receiver address required");
            }

            if (this.toList.size() > 0)
            {
                this.message.setRecipients(
                    Message.RecipientType.TO,
                    this.toInternetAddressArray(this.toList));
            }

            if (this.ccList.size() > 0)
            {
                this.message.setRecipients(
                    Message.RecipientType.CC,
                    this.toInternetAddressArray(this.ccList));
            }

            if (this.bccList.size() > 0)
            {
                this.message.setRecipients(
                    Message.RecipientType.BCC,
                    this.toInternetAddressArray(this.bccList));
            }

            if (this.replyList.size() > 0)
            {
                this.message.setReplyTo(
                    this.toInternetAddressArray(this.replyList));
            }


            if (this.headers.size() > 0)
            {
                Iterator iterHeaderKeys = this.headers.keySet().iterator();
                while (iterHeaderKeys.hasNext())
                {
                    String name = (String) iterHeaderKeys.next();
                    String value = (String) headers.get(name);
                    String foldedValue = createFoldedHeaderValue(name, value);
                    this.message.addHeader(name, foldedValue);
                }
            }

            if (this.message.getSentDate() == null)
            {
                this.message.setSentDate(getSentDate());
            }

            if (this.popBeforeSmtp)
            {
                Store store = session.getStore("pop3");
                store.connect(this.popHost, this.popUsername, this.popPassword);
            }
        }
        catch (MessagingException me)
        {
            throw new EmailException(me);
        }
    }

    /**
     * Sends the previously created MimeMessage to the SMTP server.
     *
     * @return the message id of the underlying MimeMessage
     * @throws EmailException the sending failed
     */
    public String sendMimeMessage()
       throws EmailException
    {
        EmailUtils.notNull(this.message, "message");

        try
        {
            Transport.send(this.message);
            return this.message.getMessageID();
        }
        catch (Throwable t)
        {
            String msg = "Sending the email to the following server failed : "
                + this.getHostName()
                + ":"
                + this.getSmtpPort();

            throw new EmailException(msg, t);
        }
    }

    /**
     * Returns the internal MimeMessage. Please not that the
     * MimeMessage is build by the buildMimeMessage() method.
     *
     * @return the MimeMessage
     */
    public MimeMessage getMimeMessage()
    {
        return this.message;
    }

    /**
     * Sends the email. Internally we build a MimeMessage
     * which is afterwards sent to the SMTP server.
     *
     * @return the message id of the underlying MimeMessage
     * @throws EmailException the sending failed
     */
    public String send() throws EmailException
    {
        this.buildMimeMessage();
        return this.sendMimeMessage();
    }

    /**
     * Sets the sent date for the email.  The sent date will default to the
     * current date if not explicitly set.
     *
     * @param date Date to use as the sent date on the email
     * @return An Email.
     * @since 1.0
     */
    public Email setSentDate(Date date)
    {
        if (date != null)
        {
            // create a separate instance to keep findbugs happy
            this.sentDate = new Date(date.getTime());
        }
        return this;
    }

    /**
     * Gets the sent date for the email.
     *
     * @return date to be used as the sent date for the email
     * @since 1.0
     */
    public Date getSentDate()
    {
        if (this.sentDate == null)
        {
            return new Date();
        }
        return new Date(this.sentDate.getTime());
    }

    /**
     * Gets the subject of the email.
     *
     * @return email subject
     */
    public String getSubject()
    {
        return this.subject;
    }

    /**
     * Gets the sender of the email.
     *
     * @return from address
     */
    public InternetAddress getFromAddress()
    {
        return this.fromAddress;
    }

    /**
     * Gets the host name of the SMTP server,
     *
     * @return host name
     */
    public String getHostName()
    {
        if (this.session != null)
        {
            return this.session.getProperty(MAIL_HOST);
        }
        else if (EmailUtils.isNotEmpty(this.hostName))
        {
            return this.hostName;
        }
        return null;
    }

    /**
     * Gets the listening port of the SMTP server.
     *
     * @return smtp port
     */
    public String getSmtpPort()
    {
        if (this.session != null)
        {
            return this.session.getProperty(MAIL_PORT);
        }
        else if (EmailUtils.isNotEmpty(this.smtpPort))
        {
            return this.smtpPort;
        }
        return null;
    }

    /**
     * Gets whether the client is configured to require STARTTLS.
     *
     * @return true if using STARTTLS for authentication, false otherwise
     */
    public boolean isStartTLSRequired()
    {
        return this.startTlsRequired;
    }

    /**
     * Gets whether the client is configured to try to enable STARTTLS.
     *
     * @return true if using STARTTLS for authentication, false otherwise
     */
    public boolean isStartTLSEnabled()
    {
        return this.startTlsEnabled;
    }

    /**
     * Gets whether the client is configured to try to enable STARTTLS.
     * See EMAIL-105 for reason of deprecation.
     *
     * @deprecated since 1.3
     * @return true if using STARTTLS for authentication, false otherwise
     * @since 1.1
     */
    public boolean isTLS()
    {
        return isStartTLSEnabled();
    }

    /**
     * Utility to copy List of known InternetAddress objects into an
     * array.
     *
     * @param list A List.
     * @return An InternetAddress[].
     * @since 1.0
     */
    protected InternetAddress[] toInternetAddressArray(List list)
    {
        InternetAddress[] ia =
            (InternetAddress[]) list.toArray(new InternetAddress[list.size()]);

        return ia;
    }

    /**
     * Set details regarding "pop3 before smtp" authentication.
     *
     * @param newPopBeforeSmtp Wether or not to log into pop3 server before sending mail.
     * @param newPopHost The pop3 host to use.
     * @param newPopUsername The pop3 username.
     * @param newPopPassword The pop3 password.
     * @return An Email.
     * @since 1.0
     */
    public Email setPopBeforeSmtp(
        boolean newPopBeforeSmtp,
        String newPopHost,
        String newPopUsername,
        String newPopPassword)
    {
        this.popBeforeSmtp = newPopBeforeSmtp;
        this.popHost = newPopHost;
        this.popUsername = newPopUsername;
        this.popPassword = newPopPassword;
        return this;
    }

    /**
     * Returns whether SSL/TLS encryption for the transport is currently enabled (SMTPS/POPS).
     * See EMAIL-105 for reason of deprecation.
     *
     * @deprecated since 1.3
     * @return true if SSL enabled for the transport
     */
    public boolean isSSL()
    {
        return isSSLOnConnect();
    }

    /**
     * Returns whether SSL/TLS encryption for the transport is currently enabled (SMTPS/POPS).
     *
     * @return true if SSL enabled for the transport
     */
    public boolean isSSLOnConnect()
    {
        return sslOnConnect;
    }

    /**
     * Sets whether SSL/TLS encryption should be enabled for the SMTP transport upon connection (SMTPS/POPS).
     * See EMAIL-105 for reason of deprecation.
     *
     * @deprecated since 1.3
     * @param ssl whether to enable the SSL transport
     * @return An Email.
     */
    public Email setSSL(boolean ssl)
    {
        return setSSLOnConnect(ssl);
    }

    /**
     * Sets whether SSL/TLS encryption should be enabled for the SMTP transport upon connection (SMTPS/POPS).
     *
     * @param ssl whether to enable the SSL transport
     * @return An Email.
     */
    public Email setSSLOnConnect(boolean ssl)
    {
        checkSessionAlreadyInitialized();
        this.sslOnConnect = ssl;
        return this;
    }

    /**
     * Returns the current SSL port used by the SMTP transport.
     *
     * @return the current SSL port used by the SMTP transport
     */
    public String getSslSmtpPort()
    {
        if (this.session != null)
        {
            return this.session.getProperty(MAIL_SMTP_SOCKET_FACTORY_PORT);
        }
        else if (EmailUtils.isNotEmpty(this.sslSmtpPort))
        {
            return this.sslSmtpPort;
        }
        return null;
    }

    /**
     * Sets the SSL port to use for the SMTP transport. Defaults to the standard
     * port, 465.
     *
     * @param sslSmtpPort the SSL port to use for the SMTP transport
     * @return An Email.
     */
    public Email setSslSmtpPort(String sslSmtpPort)
    {
        checkSessionAlreadyInitialized();
        this.sslSmtpPort = sslSmtpPort;
        return this;
    }

    /**
     * Get the list of "To" addresses.
     *
     * @return List addresses
     */
    public List getToAddresses()
    {
        return this.toList;
    }

    /**
     * Get the list of "CC" addresses.
     *
     * @return List addresses
     */
    public List getCcAddresses()
    {
        return this.ccList;
    }

    /**
     * Get the list of "Bcc" addresses.
     *
     * @return List addresses
     */
    public List getBccAddresses()
    {
        return this.bccList;
    }

    /**
     * Get the list of "Reply-To" addresses.
     *
     * @return List addresses
     */
    public List getReplyToAddresses()
    {
        return this.replyList;
    }

    /**
     * Get the socket connection timeout value in milliseconds.
     *
     * @return the timeout in milliseconds.
     * @since 1.2
     */
    public int getSocketConnectionTimeout()
    {
        return this.socketConnectionTimeout;
    }

    /**
     * Set the socket connection timeout value in milliseconds.
     * Default is a 60 second timeout.
     *
     * @param socketConnectionTimeout the connection timeout
     * @return An Email.
     * @since 1.2
     */
    public Email setSocketConnectionTimeout(int socketConnectionTimeout)
    {
        checkSessionAlreadyInitialized();
        this.socketConnectionTimeout = socketConnectionTimeout;
        return this;
    }

    /**
     * Get the socket I/O timeout value in milliseconds.
     *
     * @return the socket I/O timeout
     * @since 1.2
     */
    public int getSocketTimeout()
    {
        return this.socketTimeout;
    }

    /**
     * Set the socket I/O timeout value in milliseconds.
     * Default is 60 second timeout.
     *
     * @param socketTimeout the socket I/O timeout
     * @return An Email.
     * @since 1.2
     */
    public Email setSocketTimeout(int socketTimeout)
    {
        checkSessionAlreadyInitialized();
        this.socketTimeout = socketTimeout;
        return this;
    }

    /**
     * Factory method to create a customized MimeMessage which can be
     * implemented by a derived class, e.g. to set the message id.
     *
     * @param aSession mail session to be used
     * @return the newly created message
     */
    protected MimeMessage createMimeMessage(Session aSession)
    {
        return new MimeMessage(aSession);
    }

    /**
     * Create a folded header value containing 76 character chunks.
     *
     * @param name the name of the header
     * @param value the value of the header
     * @return the folded header value
     */
    private String createFoldedHeaderValue(String name, Object value)
    {
        String result;

        if (EmailUtils.isEmpty(name))
        {
            throw new IllegalArgumentException("name can not be null");
        }
        if (value == null || EmailUtils.isEmpty(value.toString()))
        {
            throw new IllegalArgumentException("value can not be null");
        }

        try
        {
            result = MimeUtility.fold(name.length() + 2, MimeUtility.encodeText(value.toString(), this.charset, null));
        }
        catch (UnsupportedEncodingException e)
        {
            result = value.toString();
        }

        return result;
    }

    /**
     * Creates a InternetAddress.
     *
     * @param email An email address.
     * @param name A name.
     * @param charsetName The name of the charset to encode the name with.
     * @return An internet address.
     * @throws EmailException Thrown when the supplied address, name or charset were invalid.
     */
    private InternetAddress createInternetAddress(String email, String name, String charsetName)
        throws EmailException
    {
        InternetAddress address = null;

        try
        {
            address = new InternetAddress(email);

            // check name input
            if (EmailUtils.isEmpty(name))
            {
                name = email;
            }

            // check charset input.
            if (EmailUtils.isEmpty(charsetName))
            {
                address.setPersonal(name);
            }
            else
            {
                // canonicalize the charset name and make sure
                // the current platform supports it.
                Charset set = Charset.forName(charsetName);
                address.setPersonal(name, set.name());
            }

            // run sanity check on new InternetAddress object; if this fails
            // it will throw AddressException.
            address.validate();
        }
        catch (AddressException e)
        {
            throw new EmailException(e);
        }
        catch (UnsupportedEncodingException e)
        {
            throw new EmailException(e);
        }
        return address;
    }

    /**
     * When a mail session is already initialized setting the
     * session properties has no effect. In order to flag the
     * problem throw an IllegalStateException.
     *
     * @throws IllegalStateException when the mail session is
     *      already initialized
     */
    private void checkSessionAlreadyInitialized()
    {
        if (this.session != null)
        {
            throw new IllegalStateException("The mail session is already initialized");
        }
    }
}
