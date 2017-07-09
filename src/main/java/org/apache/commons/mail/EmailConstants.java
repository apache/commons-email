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

/**
 * Constants used by Email classes.
 *
 * A description of the mail session parameter you find at
 * <a href="http://java.sun.com/products/javamail/javadocs/com/sun/mail/smtp/package-summary.html">
 * http://java.sun.com/products/javamail/javadocs/com/sun/mail/smtp/package-summary.html</a>.
 *
 * @since 1.3
 */
public final class EmailConstants
{
    /** @deprecated since 1.3, not in use since 1.0 */
    @Deprecated
    public static final String SENDER_EMAIL = "sender.email";

    /** @deprecated since 1.3, not in use since 1.0 */
    @Deprecated
    public static final String SENDER_NAME = "sender.name";

    /** @deprecated since 1.3, not in use since 1.0 */
    @Deprecated
    public static final String RECEIVER_EMAIL = "receiver.email";

    /** @deprecated since 1.3, not in use since 1.0 */
    @Deprecated
    public static final String RECEIVER_NAME = "receiver.name";

    /** @deprecated since 1.3, not in use since 1.0 */
    @Deprecated
    public static final String EMAIL_SUBJECT = "email.subject";

    /** @deprecated since 1.3, not in use since 1.0 */
    @Deprecated
    public static final String EMAIL_BODY = "email.body";

    /** @deprecated since 1.3, not in use since 1.0 */
    @Deprecated
    public static final String CONTENT_TYPE = "content.type";

    /** @deprecated since 1.3, not in use since 1.0 */
    @Deprecated
    public static final String ATTACHMENTS = "attachments";

    /** @deprecated since 1.3, not in use since 1.0 */
    @Deprecated
    public static final String FILE_SERVER = "file.server";

    // Charset constants

    /** charset constant for koi8-r */
    public static final String KOI8_R = "koi8-r";

    /** charset constant for iso-8859-1 */
    public static final String ISO_8859_1 = "iso-8859-1";

    /** charset constant for us-ascii */
    public static final String US_ASCII = "us-ascii";

    /** charset constant for utf-8 */
    public static final String UTF_8 = "utf-8";

    /** The debug mode to be used. */
    public static final String MAIL_DEBUG = "mail.debug";

    /** The host name of the mail server. */
    public static final String MAIL_HOST = "mail.smtp.host";

    /** The port number of the mail server. */
    public static final String MAIL_PORT = "mail.smtp.port";

    /** The email address to use for SMTP MAIL command. */
    public static final String MAIL_SMTP_FROM = "mail.smtp.from";

    /** If set to true, tries to authenticate the user using the AUTH command. */
    public static final String MAIL_SMTP_AUTH = "mail.smtp.auth";

    /** The SMTP user name. */
    public static final String MAIL_SMTP_USER = "mail.smtp.user";

    /** The SMTP password. */
    public static final String MAIL_SMTP_PASSWORD = "mail.smtp.password";

    /** Specifies the default transport protocol */
    public static final String MAIL_TRANSPORT_PROTOCOL = "mail.transport.protocol";

    /** the value to use SMTP as transport protocol */
    public static final String SMTP = "smtp";

    /** defines the text/html content type */
    public static final String TEXT_HTML = "text/html";

    /** defines the html subtype */
    public static final String TEXT_SUBTYPE_HTML = "html";

    /** defines the text/plain content type */
    public static final String TEXT_PLAIN = "text/plain";

    /////////////////////////////////////////////////////////////////////////
    // since 1.1
    /////////////////////////////////////////////////////////////////////////

    /** @deprecated since 1.3 */
    @Deprecated
    public static final String MAIL_TRANSPORT_TLS = "mail.smtp.starttls.enable";

    /**
     * Indicates if the STARTTLS command shall be used to initiate a TLS-secured connection.
     * @since 1.1
     */
    public static final String MAIL_TRANSPORT_STARTTLS_ENABLE = "mail.smtp.starttls.enable";

    /**
     * Whether to use {@link java.net.Socket} as a fallback if the initial connection fails or not.
     * @since 1.1
     */
    public static final String MAIL_SMTP_SOCKET_FACTORY_FALLBACK = "mail.smtp.socketFactory.fallback";

    /**
     * Specifies the {@link javax.net.SocketFactory} class to create smtp sockets.
     * @since 1.1
     */
    public static final String MAIL_SMTP_SOCKET_FACTORY_CLASS = "mail.smtp.socketFactory.class";

    /**
     * Specifies the port to connect to when using a socket factory.
     * @since 1.1
     */
    public static final String MAIL_SMTP_SOCKET_FACTORY_PORT = "mail.smtp.socketFactory.port";

    /////////////////////////////////////////////////////////////////////////
    // since 1.2
    /////////////////////////////////////////////////////////////////////////

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

    /////////////////////////////////////////////////////////////////////////
    // since 1.3
    /////////////////////////////////////////////////////////////////////////

    /**
     * Default socket timeout.
     * @since 1.3
     */
    public static final int SOCKET_TIMEOUT_MS = 60000;

    /**
     * If true, requires the use of the STARTTLS command. If the server doesn't support
     * the STARTTLS command, the connection will fail.
     * @since 1.3
     */
    public static final String MAIL_TRANSPORT_STARTTLS_REQUIRED = "mail.smtp.starttls.required";

    /**
     * If set to true, use SSL to connect and use the SSL port by default.
     * @since 1.3
     */
    public static final String MAIL_SMTP_SSL_ENABLE = "mail.smtp.ssl.enable";

    /**
     * If set to true, check the server identity as specified in RFC 2595.
     * @since 1.3
     */
    public static final String MAIL_SMTP_SSL_CHECKSERVERIDENTITY = "mail.smtp.ssl.checkserveridentity";

    /**
     * Specifies the {@link javax.net.ssl.SSLSocketFactory} class to use to create SMTP SSL sockets.
     * @since 1.3
     */
    public static final String MAIL_SMTP_SSL_SOCKET_FACTORY_CLASS = "mail.smtp.ssl.socketFactory.class";

    /**
     * Specifies the port to connect to when using the SMTP SSL socket factory.
     * @since 1.3
     */
    public static final String MAIL_SMTP_SSL_SOCKET_FACTORY_PORT = "mail.smtp.ssl.socketFactory.port";

    /////////////////////////////////////////////////////////////////////////
    // since 1.3.2
    /////////////////////////////////////////////////////////////////////////

    /**
     * If set to true, and a message has some valid and some invalid addresses, send the message anyway,
     * reporting the partial failure with a SendFailedException.
     * If set to false (the default), the message is not sent to any of the recipients
     * if there is an invalid recipient address.
     * @since 1.3.2
     */
    public static final String MAIL_SMTP_SEND_PARTIAL = "mail.smtp.sendpartial";

    /**
     * If set to true, and a message has some valid and some invalid addresses, send the message anyway,
     * reporting the partial failure with a SendFailedException.
     * If set to false (the default), the message is not sent to any of the recipients
     * if there is an invalid recipient address.
     * @since 1.3.2
     */
    public static final String MAIL_SMTPS_SEND_PARTIAL = "mail.smtps.sendpartial";

    /**
     * Defines the default mime charset to use when none has been specified for the message.
     * @since 1.3.2
     */
    public static final String MAIL_MIME_CHARSET = "mail.mime.charset";

    /////////////////////////////////////////////////////////////////////////
    // since 1.4
    /////////////////////////////////////////////////////////////////////////

    /**
     * The from email address.
     * @since 1.4
     */
    public static final String MAIL_FROM = "mail.from";


    /** Hide constructor. */
    private EmailConstants()
    {
        // do nothing
    }

}
