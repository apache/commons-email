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
 * Constants used by Email classes
 */
public interface EmailConstants
{
    // Constants used by Email classes
   
    String SENDER_EMAIL = "sender.email";
    String SENDER_NAME = "sender.name";
    String RECEIVER_EMAIL = "receiver.email";
    String RECEIVER_NAME = "receiver.name";
    String EMAIL_SUBJECT = "email.subject";
    String EMAIL_BODY = "email.body";
    String CONTENT_TYPE = "content.type";

    // Charset constants
    String KOI8_R = "koi8-r";
    String ISO_8859_1 = "iso-8859-1";
    String US_ASCII = "us-ascii";
    String UTF_8 = "utf-8";

    String SMTP = "smtp";
    String TEXT_HTML = "text/html";
    String TEXT_PLAIN = "text/plain";
    String ATTACHMENTS = "attachments";
    String FILE_SERVER = "file.server";
    String MAIL_DEBUG = "mail.debug";

    String MAIL_HOST = "mail.smtp.host";
    String MAIL_PORT = "mail.smtp.port";
    String MAIL_SMTP_FROM = "mail.smtp.from";
    String MAIL_SMTP_AUTH = "mail.smtp.auth";
    String MAIL_SMTP_USER = "mail.smtp.user";
    String MAIL_SMTP_PASSWORD = "mail.smtp.password";
    String MAIL_TRANSPORT_PROTOCOL = "mail.transport.protocol";

    // since 1.1
    String MAIL_TRANSPORT_TLS = "mail.smtp.starttls.enable";
    String MAIL_SMTP_SOCKET_FACTORY_FALLBACK = "mail.smtp.socketFactory.fallback";
    String MAIL_SMTP_SOCKET_FACTORY_CLASS = "mail.smtp.socketFactory.class";
    String MAIL_SMTP_SOCKET_FACTORY_PORT = "mail.smtp.socketFactory.port";

    // since 1.2

    /**
     * Socket connection timeout value in milliseconds. Default is infinite timeout.
     * @since 1.2
     */
     String MAIL_SMTP_CONNECTIONTIMEOUT = "mail.smtp.connectiontimeout";

    /**
     * Socket I/O timeout value in milliseconds. Default is infinite timeout.
     * @since 1.2
     */
     String MAIL_SMTP_TIMEOUT = "mail.smtp.timeout";
}
