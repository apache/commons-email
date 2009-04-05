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
public abstract class Email
{
    /** Constants used by Email classes. */

    /** */
    public static final String SENDER_EMAIL = "sender.email";
    /** */
    public static final String SENDER_NAME = "sender.name";
    /** */
    public static final String RECEIVER_EMAIL = "receiver.email";
    /** */
    public static final String RECEIVER_NAME = "receiver.name";
    /** */
    public static final String EMAIL_SUBJECT = "email.subject";
    /** */
    public static final String EMAIL_BODY = "email.body";
    /** */
    public static final String CONTENT_TYPE = "content.type";

    /** */
    public static final String MAIL_HOST = "mail.smtp.host";
    /** */
    public static final String MAIL_PORT = "mail.smtp.port";
    /** */
    public static final String MAIL_SMTP_FROM = "mail.smtp.from";
    /** */
    public static final String MAIL_SMTP_AUTH = "mail.smtp.auth";
    /** */
    public static final String MAIL_SMTP_USER = "mail.smtp.user";
    /** */
    public static final String MAIL_SMTP_PASSWORD = "mail.smtp.password";
    /** */
    public static final String MAIL_TRANSPORT_PROTOCOL =
        "mail.transport.protocol";
    /**
     * @since 1.1
     */
    public static final String MAIL_TRANSPORT_TLS = "mail.smtp.starttls.enable";
    /** */
    public static final String MAIL_SMTP_SOCKET_FACTORY_FALLBACK = "mail.smtp.socketFactory.fallback";
    /** */
    public static final String MAIL_SMTP_SOCKET_FACTORY_CLASS = "mail.smtp.socketFactory.class";
    /** */
    public static final String MAIL_SMTP_SOCKET_FACTORY_PORT = "mail.smtp.socketFactory.port";

       
    /**
     * Socket connection timeout value in milliseconds. Default is infinite timeout.
     * @since 1.2
     */
    public static final String MAIL_SMTP_CONNECTIONTIMEOUT = "mail.smtp.connectiontimeout";

    /**
     * Socket I/O timeout value in milliseconds. Default is infinite timeout.
     * @since 1.2  
     */
    public static final String MAIL_SMTP_TIMEOUT = "mail.smtp.timeout";


    /** */
    public static final String SMTP = "smtp";
    /** */
    public static final String TEXT_HTML = "text/html";
    /** */
    public static final String TEXT_PLAIN = "text/plain";
    /** */
    public static final String ATTACHMENTS = "attachments";
    /** */
    public static final String FILE_SERVER = "file.server";
    /** */
    public static final String MAIL_DEBUG = "mail.debug";

    /** */
    public static final String KOI8_R = "koi8-r";
    /** */
    public static final String ISO_8859_1 = "iso-8859-1";
    /** */
    public static final String US_ASCII = "us-ascii";

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

    /** does server require TLS encryption for authentication */
    protected boolean tls;
    /** does the current transport use SSL encryption? */
    protected boolean ssl;

    /** socket I/O timeout value in milliseconds */
    protected int socketTimeout;
    /** socket connection timeout value in milliseconds */
    protected int socketConnectionTimeout;

    /** The Session to mail with */
    private Session session;

    /**
     * Setting to true will enable the display of debug information.
     *
     * @param d A boolean.
     * @since 1.0
     */
    public void setDebug(boolean d)
    {
        this.debug = d;
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
     * @see DefaultAuthenticator
     * @see #setAuthenticator
     * @since 1.0
     */
    public void setAuthentication(String userName, String password)
    {
        this.authenticator = new DefaultAuthenticator(userName, password);
        this.setAuthenticator(this.authenticator);
    }

    /**
     * Sets the <code>Authenticator</code> to be used when authentication
     * is requested from the mail server.
     * <p>
     * This method should be used when your outgoing mail server requires
     * authentication.  Your mail server must also support RFC2554.
     *
     * @param newAuthenticator the <code>Authenticator</code> object.
     * @see Authenticator
     * @since 1.0
     */
    public void setAuthenticator(Authenticator newAuthenticator)
    {
        this.authenticator = newAuthenticator;
    }

    /**
     * Set the charset of the message.
     *
     * @param newCharset A String.
     * @throws java.nio.charset.IllegalCharsetNameException if the charset name is invalid
     * @throws java.nio.charset.UnsupportedCharsetException if no support for the named charset
     * exists in the current JVM
     * @since 1.0
     */
    public void setCharset(String newCharset)
    {
        Charset set = Charset.forName(newCharset);
        this.charset = set.name();
    }

    /**
     * Set the emailBody to a MimeMultiPart
     *
     * @param aMimeMultipart aMimeMultipart
     * @since 1.0
     */
    public void setContent(MimeMultipart aMimeMultipart)
    {
        this.emailBody = aMimeMultipart;
    }

    /**
     * Set the content & contentType
     *
     * @param   aObject aObject
     * @param   aContentType aContentType
     * @since 1.0
     */
    public void setContent(Object aObject, String aContentType)
    {
        this.content = aObject;
        this.updateContentType(aContentType);
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
     * @since 1.0
     */
    public void setHostName(String aHostName)
    {
        this.hostName = aHostName;
    }

    /**
     * Set or disable the TLS encryption
     *
     * @param withTLS true if TLS needed, false otherwise
     * @since 1.1
     */
    public void setTLS(boolean withTLS)
    {
        this.tls = withTLS;
    }

    /**
     * Set the port number of the outgoing mail server.
     * @param   aPortNumber aPortNumber
     * @since 1.0
     */
    public void setSmtpPort(int aPortNumber)
    {
        if (aPortNumber < 1)
        {
            throw new IllegalArgumentException(
                "Cannot connect to a port number that is less than 1 ( "
                    + aPortNumber
                    + " )");
        }

        this.smtpPort = Integer.toString(aPortNumber);
    }

    /**
     * Supply a mail Session object to use. Please note that passing
     * a username and password (in the case of mail authentication) will
     * create a new mail session with a DefaultAuthenticator. This is a
     * convience but might come unexpected.
     *
     * @param aSession mail session to be used
     * @since 1.0
     */
    public void setMailSession(Session aSession)
    {
        EmailUtils.notNull(aSession, "no mail session supplied");
        
        Properties sessionProperties = aSession.getProperties();
        String auth = sessionProperties.getProperty(MAIL_SMTP_AUTH);

        if ("true".equalsIgnoreCase(auth))
        {
            String userName = sessionProperties.getProperty(MAIL_SMTP_USER);
            String password = sessionProperties.getProperty(MAIL_SMTP_PASSWORD);

            if(EmailUtils.isNotEmpty(userName) && EmailUtils.isNotEmpty(password))
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
    }

    /**
     * Supply a mail Session object from a JNDI directory
     * @param jndiName name of JNDI ressource (javax.mail.Session type), ressource
     * if searched in java:comp/env if name dont start with "java:"
     * @throws IllegalArgumentException JNDI name null or empty
     * @throws NamingException ressource can be retrieved from JNDI directory
     * @since 1.1
     */
    public void setMailSessionFromJNDI(String jndiName) throws NamingException
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
    }

    /**
     * Initialise a mailsession object
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

            properties.setProperty(MAIL_PORT, smtpPort);
            properties.setProperty(MAIL_HOST, hostName);
            properties.setProperty(MAIL_DEBUG, String.valueOf(this.debug));

            if (this.authenticator != null)
            {
                properties.setProperty(MAIL_TRANSPORT_TLS, tls ? "true" : "false");
                properties.setProperty(MAIL_SMTP_AUTH, "true");
            }

            if (this.ssl)
            {
                properties.setProperty(MAIL_PORT, sslSmtpPort);
                properties.setProperty(MAIL_SMTP_SOCKET_FACTORY_PORT, sslSmtpPort);
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
            this.session =
                Session.getInstance(properties, this.authenticator);
        }
        return this.session;
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
     * Set the FROM field of the email to use the specified address. The email
     * address will also be used as the personal name. The name will be encoded
     * using the Java platform's default charset (UTF-16) if it contains
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
     * specified personal name. The name will be encoded using the Java
     * platform's default charset (UTF-16) if it contains non-ASCII
     * characters; otherwise, it is used as is.
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
        return setFrom(email, name, null);
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
     * address will also be used as the personal name. The name will be encoded
     * using the Java platform's default charset (UTF-16) if it contains
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
     * specified personal name. The name will be encoded using the Java
     * platform's default charset (UTF-16) if it contains non-ASCII
     * characters; otherwise, it is used as is.
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
        return addTo(email, name, null);
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
     * address will also be used as the personal name. The name will be encoded
     * using the Java platform's default charset (UTF-16) if it contains
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
     * specified personal name. The name will be encoded using the Java
     * platform's default charset (UTF-16) if it contains non-ASCII
     * characters; otherwise, it is used as is.
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
        return addCc(email, name, null);
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
     * address will also be used as the personal name. The name will be encoded
     * using the Java platform's default charset (UTF-16) if it contains
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
     * the specified personal name. The name will be encoded using the Java
     * platform's default charset (UTF-16) if it contains non-ASCII
     * characters; otherwise, it is used as is.
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
        return addBcc(email, name, null);
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
     * @param   aCollection collection of <code>InternetAddress</code> objects
     * @return  An Email.
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
     * address will also be used as the personal name. The name will be encoded
     * using the Java platform's default charset (UTF-16) if it contains
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
     * the specified personal name. The name will be encoded using the Java
     * platform's default charset (UTF-16) if it contains non-ASCII
     * characters; otherwise, it is used as is.
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
        return addReplyTo(email, name, null);
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
     * @since 1.0
     */
    public void setHeaders(Map map)
    {
        Iterator iterKeyBad = map.entrySet().iterator();

        while (iterKeyBad.hasNext())
        {
            Map.Entry entry = (Map.Entry) iterKeyBad.next();
            String strName = (String) entry.getKey();
            String strValue = (String) entry.getValue();

            if (EmailUtils.isEmpty(strName))
            {
                throw new IllegalArgumentException("name can not be null");
            }
            if (EmailUtils.isEmpty(strValue))
            {
                throw new IllegalArgumentException("value can not be null");
            }
        }

        // all is ok, update headers
        this.headers = map;
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

        this.headers.put(name, value);
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
        this.bounceAddress = email;
        return this;
    }


    /**
     * Define the content of the mail.  It should be overidden by the
     * subclasses.
     *
     * @param msg A String.
     * @return An Email.
     * @throws EmailException generic exception.
     * @since 1.0
     */
    public abstract Email setMsg(String msg) throws EmailException;

    /**
     * Build the internal MimeMessage to be sent.
     *
     * @throws EmailException if there was an error.
     * @since 1.0
     */
    public void buildMimeMessage() throws EmailException
    {
        try
        {
            this.getMailSession();
            this.message = this.createMimeMessage(this.session);

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
                    this.message.addHeader(name, value);
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
     * current date if not explictly set.
     *
     * @param date Date to use as the sent date on the email
     * @since 1.0
     */
    public void setSentDate(Date date)
    {
        this.sentDate = date;
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
        return this.sentDate;
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
        if (EmailUtils.isNotEmpty(this.hostName))
        {
            return this.hostName;
        }
        else if (this.session != null)
        {
            return this.session.getProperty(MAIL_HOST);
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
        if (EmailUtils.isNotEmpty(this.smtpPort))
        {
            return this.smtpPort;
        }
        else if (this.session != null)
        {
            return this.session.getProperty(MAIL_PORT);
        }
        return null;
    }

    /**
     * Gets encryption mode for authentication
     *
     * @return true if using TLS for authentication, false otherwise
     * @since 1.1
     */
    public boolean isTLS()
    {
        return this.tls;
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
     * @param newPopBeforeSmtp Wether or not to log into pop3
     *      server before sending mail.
     * @param newPopHost The pop3 host to use.
     * @param newPopUsername The pop3 username.
     * @param newPopPassword The pop3 password.
     * @since 1.0
     */
    public void setPopBeforeSmtp(
        boolean newPopBeforeSmtp,
        String newPopHost,
        String newPopUsername,
        String newPopPassword)
    {
        this.popBeforeSmtp = newPopBeforeSmtp;
        this.popHost = newPopHost;
        this.popUsername = newPopUsername;
        this.popPassword = newPopPassword;
    }

    /**
     * Returns whether SSL encryption for the transport is currently enabled.
     * @return true if SSL enabled for the transport
     */
    public boolean isSSL()
    {
        return ssl;
    }

    /**
     * Sets whether SSL encryption should be enabled for the SMTP transport.
     * @param ssl whether to enable the SSL transport
     */
    public void setSSL(boolean ssl)
    {
        this.ssl = ssl;
    }

    /**
     * Returns the current SSL port used by the SMTP transport.
     * @return the current SSL port used by the SMTP transport
     */
    public String getSslSmtpPort()
    {
        if (EmailUtils.isNotEmpty(this.sslSmtpPort))
        {
            return this.sslSmtpPort;
        }
        else if (this.session != null)
        {
            return this.session.getProperty(MAIL_SMTP_SOCKET_FACTORY_PORT);
        }
        return null;
    }

    /**
     * Sets the SSL port to use for the SMTP transport. Defaults to the standard
     * port, 465.
     * @param sslSmtpPort the SSL port to use for the SMTP transport
     */
    public void setSslSmtpPort(String sslSmtpPort)
    {
        this.sslSmtpPort = sslSmtpPort;
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
     * Default is infinite timeout.
     *
     * @param socketConnectionTimeout the connection timeout
     * @since 1.2
     */
    public void setSocketConnectionTimeout( int socketConnectionTimeout )
    {
        this.socketConnectionTimeout = socketConnectionTimeout;
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
     * Default is infinite timeout.
     *
     * @param socketTimeout the socket I/O timeout
     * @since 1.2
     */
    public void setSocketTimeout( int socketTimeout )
    {
        this.socketTimeout = socketTimeout;
    }
}
