/*
 * Copyright 2001-2004 The Apache Software Foundation
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
package org.apache.commons.mail.mocks;

import java.util.ArrayList;
import java.util.Hashtable;

import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;

/**
 * Concrete Implementation on the Abstract Email 
 * Class (used to allow testing only).  Supplies
 * getters for methods that normally only have setters.
 *
 * @author <a href="mailto:corey.scott@gmail.com">Corey Scott</a>
 * @version $Id: MockEmailConcrete.java,v 1.2 2004/11/29 17:33:12 epugh Exp $
 */
public class MockEmailConcrete extends Email
{

    /**
     * Not Implemented, should be implemented in subclasses of Email
     * @param msg The email message
     * @return Email msg.
     * @throws MessagingException MessagingException
     */
    public Email setMsg(String msg) throws EmailException
    {
        // This abstract method should be tested in the concrete 
        // implementation classes only.
        return null;
    }

    /**
     * Retrieve the current debug setting
     * @return debug
     */
    public boolean isDebug()
    {
        return this.debug;
    }

    /**
     * Retrieve the current authentication setting
     * @return Authenticator Authenticator
     */
    public Authenticator getAuthenticator()
    {
        return this.authenticator;
    }

    /**
     * @return bccList
     */
    public ArrayList getBccList()
    {
        return this.bccList;
    }

    /**
     * @return ccList
     */
    public ArrayList getCcList()
    {
        return this.ccList;
    }

    /**
     * @return charset
     */
    public String getCharset()
    {
        return this.charset;
    }

    /**
     * @return content
     */
    public Object getContentObject()
    {
        return this.content;
    }

    /**
     * @return content
     */
    public MimeMultipart getContentMimeMultipart()
    {
        return this.emailBody;
    }

    /**
     * @return emailBody
     */
    public MimeMultipart getEmailBody()
    {
        return this.emailBody;
    }

    /**
     * @return fromAddress
     */
    public InternetAddress getFromAddress()
    {
        return this.fromAddress;
    }

    /**
     * @return headers
     */
    public Hashtable getHeaders()
    {
        return this.headers;
    }

    /**
     * @return hostName
     */
    public String getHostName()
    {
        return this.hostName;
    }

    /**
     * @return message
     */
    public MimeMessage getMessage()
    {
        return this.message;
    }

    /**
     * @return popHost
     */
    public String getPopHost()
    {
        return this.popHost;
    }

    /**
     * @return popPassword
     */
    public String getPopPassword()
    {
        return this.popPassword;
    }

    /**
     * @return popUsername
     */
    public String getPopUsername()
    {
        return this.popUsername;
    }

    /**
     * @return replyList
     */
    public ArrayList getReplyList()
    {
        return this.replyList;
    }

    /**
     * @return smtpPort
     */
    public String getSmtpPort()
    {
        return this.smtpPort;
    }

    /**
     * @return subject
     */
    public String getSubject()
    {
        return this.subject;
    }

    /**
     * @return toList
     */
    public ArrayList getToList()
    {
        return this.toList;
    }

    /**
     * @return contentType
     */
    public String getContentType()
    {
        return contentType;
    }

    /**
     * @return popBeforeSmtp
     */
    public boolean isPopBeforeSmtp()
    {
        return popBeforeSmtp;
    }

    /**
     * @return Session
     * @throws EmailException EmailException
     */
    public Session getSession()
        throws EmailException
    {
        return this.getMailSession();
    }

}
