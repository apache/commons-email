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
package org.apache.commons.mail.mocks;

import javax.mail.Authenticator;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;

/**
 * Concrete Implementation on the Abstract Email Class (used to allow testing only).
 * Supplies getters for methods that normally only have setters.
 *
 * @since 1.0
 */
public class MockEmailConcrete extends Email
{

    /**
     * Not Implemented, should be implemented in subclasses of Email
     * @param msg The email message
     * @return Email msg.
     */
    @Override
    public Email setMsg(final String msg)
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
     * @return hostName
     */
    @Override
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
