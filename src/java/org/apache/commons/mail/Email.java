/*
 * Copyright 2001-2005 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 ( the "License" );
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
package org.apache.commons.mail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.lang.StringUtils;

/**
 * The base class for all email messages.  This class sets the
 * sender's email & name, receiver's email & name, subject, and the
 * sent date.  Subclasses are responsible for setting the message
 * body.
 *
 * @author <a href="mailto:quintonm@bellsouth.net">Quinton McCombs</a>
 * @author <a href="mailto:jon@latchkey.com">Jon S. Stevens</a>
 * @author <a href="mailto:frank.kim@clearink.com">Frank Y. Kim</a>
 * @author <a href="mailto:bmclaugh@algx.net">Brett McLaughlin</a>
 * @author <a href="mailto:greg@shwoop.com">Greg Ritter</a>
 * @author <a href="mailto:unknown">Regis Koenig</a>
 * @author <a href="mailto:colin.chalmers@maxware.nl">Colin Chalmers</a>
 * @author <a href="mailto:matthias@wessendorf.net">Matthias Wessendorf</a>
 * @author <a href="mailto:corey.scott@gmail.com">Corey Scott</a>
 * @version $Revision: 1.6 $ $Date$
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
    public static final String MAIL_HOST = "mail.host";
    /** */
    public static final String MAIL_PORT = "mail.smtp.port";
    /** */
    public static final String MAIL_SMTP_FROM = "mail.smtp.from";
    /** */
    public static final String MAIL_SMTP_AUTH = "mail.smtp.auth";
    /** */
    public static final String MAIL_TRANSPORT_PROTOCOL =
        "mail.transport.protocol";
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
    protected boolean debug = false;

    /** Sent date */
    protected Date sentDate;

    /** The Session to mail with */
    private Session session;

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
    protected String bounceAddress = null;

    /**
     * Used to specify the mail headers.  Example:
     *
     * X-Mailer: Sendmail, X-Priority: 1( highest )
     * or  2( high ) 3( normal ) 4( low ) and 5( lowest )
     * Disposition-Notification-To: user@domain.net
     */
    protected Hashtable headers = new Hashtable();

    /**
     * Used to determine whether to use pop3 before smtp, and if so the settings.
     */

    /** */
    protected boolean popBeforeSmtp = false;
    /** */
    protected String popHost = null;
    /** */
    protected String popUsername = null;
    /** */
    protected String popPassword = null;

    /**
     * Setting to true will enable the display of debug information.
     *
     * @param d A boolean.
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
     */
    public void setAuthenticator(Authenticator newAuthenticator)
    {
        this.authenticator = newAuthenticator;
    }

    /**
     * Set the charset of the message.
     *
     * @param newCharset A String.
     */
    public void setCharset(String newCharset)
    {
        this.charset = newCharset;
    }

    /**
     * Set the emailBody to a MimeMultiPart
     *
     * @param aMimeMultipart aMimeMultipart
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
     */
    public void setContent(Object aObject, String aContentType)
    {
        this.content = aObject;
        if (!StringUtils.isNotEmpty(aContentType))
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
        }
    }

    /**
     * Set the hostname of the outgoing mail server
     *
     * @param   aHostName aHostName
     */
    public void setHostName(String aHostName)
    {
        this.hostName = aHostName;
    }

    /**
     * Set the port number of the outgoing mail server.
     * @param   aPortNumber aPortNumber
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
     * Supply a mail Session object to use
     * @param aSession mail session to be used
     */
    public void setMailSession(Session aSession)
    {
        this.session = aSession;
    }
    /**
     * Initialise a mailsession object
     *
     * @return A Session.
     * @throws EmailException thrown when host name was not set
     */
    protected Session getMailSession() throws EmailException
    {
        if (this.session == null)
        {
            Properties properties = new Properties(System.getProperties());
            properties.setProperty(MAIL_TRANSPORT_PROTOCOL, SMTP);

            if (!StringUtils.isNotEmpty(this.hostName))
            {
                this.hostName = properties.getProperty(MAIL_HOST);
            }

            if (!StringUtils.isNotEmpty(this.hostName))
            {
                throw new EmailException(
                    "Cannot find valid hostname for mail session");
            }

            properties.setProperty(MAIL_PORT, smtpPort);
            properties.setProperty(MAIL_HOST, hostName);
            properties.setProperty(MAIL_DEBUG, String.valueOf(this.debug));

            if (this.authenticator != null)
            {
                properties.setProperty(MAIL_SMTP_AUTH, "true");
            }

            if (this.bounceAddress != null)
            {
                properties.setProperty(MAIL_SMTP_FROM, this.bounceAddress);
            }

            // changed this (back) to getInstance due to security exceptions 
            // caused when testing using maven
            this.session =
                Session.getInstance(properties, this.authenticator);
        }
        return this.session;
    }
    
    /**
    * Creates a InternetAddress
    * @param email An Email
    * @param name A Name.  
    * @return An internet address
    * @throws EmailException thrown when the address supplied or name were invalid
    */
    private InternetAddress createInternetAddress(String email, String name) 
        throws EmailException
    {
        InternetAddress address = null;
        
        try 
        {
            // check name input
            if (!StringUtils.isNotEmpty(name))
            {
                name = email;
            }
            
            if (StringUtils.isNotEmpty(this.charset))
            {
                address = new InternetAddress(email, name, this.charset);
            }
            else
            {
                address = new InternetAddress(email, name);
            }
            
            address.validate();
        }
        catch (Exception e)
        {
            throw new EmailException(e);
        }
        return address;
    }
    
    
    /**
     * Set the FROM field of the email.
     *
     * @param email A String.
     * @return An Email.
     * @throws EmailException Indicates an invalid email address
     */
    public Email setFrom(String email) 
        throws EmailException 
    {
        return setFrom(email, null);
    }

    /**
     * Set the FROM field of the email.
     *
     * @param email A String.
     * @param name A String.
     * @return An Email.
     * @throws EmailException Indicates an invalid email address
     */
    public Email setFrom(String email, String name) 
        throws EmailException
    {
        this.fromAddress = createInternetAddress(email, name);

        return this;
    }

    /**
     * Add a recipient TO to the email.
     *
     * @param email A String.
     * @return An Email.
     * @throws EmailException Indicates an invalid email address
     */
    public Email addTo(String email) 
        throws EmailException
    {
        return addTo(email, null);
    }

    /**
     * Add a recipient TO to the email.
     *
     * @param email A String.
     * @param name A String.
     * @return An Email.
     * @throws EmailException Indicates an invalid email address
     */
    public Email addTo(String email, String name) 
        throws EmailException
    {
        this.toList.add(createInternetAddress(email, name));

        return this;
    }

    /**
     * Set a list of "TO" addresses
     *
     * @param   aCollection collection of InternetAddress objects
     * @return An Email.
     * @throws EmailException Indicates an invalid email address
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
     * Add a recipient CC to the email.
     *
     * @param email A String.
     * @return An Email.
     * @throws EmailException Indicates an invalid email address
     */
    public Email addCc(String email) 
        throws EmailException
    {
        return this.addCc(email, null);
    }

    /**
     * Add a recipient CC to the email.
     *
     * @param email A String.
     * @param name A String.
     * @return An Email.
     * @throws EmailException Indicates an invalid email address
     */
    public Email addCc(String email, String name) 
        throws EmailException
    {
        this.ccList.add(createInternetAddress(email, name));

        return this;
    }

    /**
     * Set a list of "CC" addresses
     *
     * @param   aCollection collection of InternetAddress objects
     * @return An EmailException.
     * @throws EmailException Indicates an invalid email address
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
     * Add a blind BCC recipient to the email.
     *
     * @param email A String.
     * @return An Email.
     * @throws EmailException Indicates an invalid email address
     */
    public Email addBcc(String email) 
        throws EmailException
    {
        return this.addBcc(email, null);
    }

    /**
     * Add a blind BCC recipient to the email.
     *
     * @param email A String.
     * @param name A String.
     * @return An Email.
     * @throws EmailException Indicates an invalid email address
     */
    public Email addBcc(String email, String name) 
        throws EmailException
    {

        this.bccList.add(createInternetAddress(email, name));

        return this;
    }

    /**
     * Set a list of "BCC" addresses
     *
     * @param   aCollection collection of InternetAddress objects
     * @return  An Email.
     * @throws EmailException Indicates an invalid email address
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
     * Add a reply to address to the email.
     *
     * @param email A String.
     * @return An Email.
     * @throws EmailException Indicates an invalid email address
     */
    public Email addReplyTo(String email) 
        throws EmailException
    {
        return this.addReplyTo(email, null);
    }

    /**
     * Add a reply to address to the email.
     *
     * @param email A String.
     * @param name A String.
     * @return An Email.
     * @throws EmailException Indicates an invalid email address
     */
    public Email addReplyTo(String email, String name)
        throws EmailException
    {
        
        this.replyList.add(createInternetAddress(email, name));

        return this;
    }

    /**
     * Used to specify the mail headers.  Example:
     *
     * X-Mailer: Sendmail, X-Priority: 1( highest )
     * or  2( high ) 3( normal ) 4( low ) and 5( lowest )
     * Disposition-Notification-To: user@domain.net
     *
     * @param ht A Hashtable.
     */
    public void setHeaders(Hashtable ht)
    {
        Enumeration enumKeyBad = ht.keys();

        while (enumKeyBad.hasMoreElements())
        {
            String strName = (String) enumKeyBad.nextElement();
            String strValue = (String) ht.get(strName);

            if (!StringUtils.isNotEmpty(strName))
            {
                throw new IllegalArgumentException("name can not be null");
            }
            if (!StringUtils.isNotEmpty(strValue))
            {
                throw new IllegalArgumentException("value can not be null");
            }
        }

        // all is ok, update headers
        this.headers = ht;
    }

    /**
     * Adds a header ( name, value ) to the headers Hashtable.
     *
     * @param name A String with the name.
     * @param value A String with the value.
     */
    public void addHeader(String name, String value)
    {
        if (!StringUtils.isNotEmpty(name))
        {
            throw new IllegalArgumentException("name can not be null");
        }
        if (!StringUtils.isNotEmpty(value))
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
     * @throws EmailException generic exception
     */
    public abstract Email setMsg(String msg) throws EmailException;

    /**
     * Does the work of actually sending the email.
     *
     * @throws EmailException if there was an error.
     */
    public void send() throws EmailException
    {
        try 
        {
            this.getMailSession();
            this.message = new MimeMessage(this.session);
    
            if (StringUtils.isNotEmpty(this.subject))
            {
                if (StringUtils.isNotEmpty(this.charset))
                {
                    this.message.setSubject(this.subject, this.charset);
                }
                else
                {
                    this.message.setSubject(this.subject);
                }
            }
    
            // ========================================================
            // Start of replacement code
            if (this.content != null)
            {
                this.message.setContent(this.content, this.contentType);
            }
            // end of replacement code
            // ========================================================
            else if (this.emailBody != null)
            {
                this.message.setContent(this.emailBody);
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
                throw new EmailException("Sender address required");
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
                Enumeration enumHeaderKeys = this.headers.keys();
    
                while (enumHeaderKeys.hasMoreElements())
                {
                    String name = (String) enumHeaderKeys.nextElement();
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
    
            Transport.send(this.message);
        }
        catch (MessagingException me)
        {
            throw new EmailException(me);
        }    
    }

    /**
     * Sets the sent date for the email.  The sent date will default to the
     * current date if not explictly set.
     *
     * @param date Date to use as the sent date on the email
     */
    public void setSentDate(Date date)
    {
        this.sentDate = date;
    }

    /**
     * Gets the sent date for the email.
     *
     * @return date to be used as the sent date for the email
     */
    public Date getSentDate()
    {
        if (this.sentDate == null)
        {
            return new Date();
        }
        else
        {
            return this.sentDate;
        }
    }

    /**
     * Utility to copy List of known InternetAddress objects into an
     * array.
     *
     * @param aList A List of InternetAddress.
     * @return An InternetAddress[].
     */
    protected InternetAddress[] toInternetAddressArray(List aList)
    {
        InternetAddress[] ia =
            (InternetAddress[]) aList.toArray(new InternetAddress[0]);

        return ia;
    }

    /**
     * Set details regarding "pop3 before smtp" authentication
     * @param newPopBeforeSmtp Wether or not to log into pop3 
     *      server before sending mail
     * @param newPopHost The pop3 host to use.
     * @param newPopUsername The pop3 username.
     * @param newPopPassword The pop3 password.
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
}