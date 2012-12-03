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
package org.apache.commons.mail.util;

import junit.framework.TestCase;

import javax.activation.DataSource;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.util.List;
import java.util.Properties;

/**
 * Testing the MimeMessageParser.
 */
public class MimeMessageParserTest
    extends TestCase
{
    /**
     * Defines the test case name for JUnit.
     *
     * @param name the test case's name.
     */
    public MimeMessageParserTest(String name)
    {
        super(name);
    }

    // ======================================================================
    // Start of Tests
    // ======================================================================

    public void testParseSimpleEmail() throws Exception
    {
        Session session = Session.getDefaultInstance(new Properties());
        MimeMessage message = MimeMessageUtils.createMimeMessage(session, new File("./src/test/resources/eml/simple.eml"));
        MimeMessageParser mimeMessageParser = new MimeMessageParser(message);

        mimeMessageParser.parse();

        assertEquals("Test HTML Send #1 Subject (wo charset)", mimeMessageParser.getSubject());
        assertNotNull(mimeMessageParser.getMimeMessage());
        assertTrue(mimeMessageParser.isMultipart());
        assertFalse(mimeMessageParser.hasHtmlContent());
        assertTrue(mimeMessageParser.hasPlainContent());
        assertNotNull(mimeMessageParser.getPlainContent());
        assertNull(mimeMessageParser.getHtmlContent());
        assertTrue(mimeMessageParser.getTo().size() == 1);
        assertTrue(mimeMessageParser.getCc().size() == 0);
        assertTrue(mimeMessageParser.getBcc().size() == 0);
        assertEquals("test_from@apache.org", mimeMessageParser.getFrom());
        assertEquals("test_from@apache.org", mimeMessageParser.getReplyTo());
        assertFalse(mimeMessageParser.hasAttachments());
    }

    public void testParseSimpleReplyEmail() throws Exception
    {
        Session session = Session.getDefaultInstance(new Properties());
        MimeMessage message = MimeMessageUtils.createMimeMessage(session, new File("./src/test/resources/eml/simple-reply.eml"));
        MimeMessageParser mimeMessageParser = new MimeMessageParser(message);

        mimeMessageParser.parse();

        assertEquals("Re: java.lang.NoClassDefFoundError: org/bouncycastle/asn1/pkcs/PrivateKeyInfo", mimeMessageParser.getSubject());
        assertNotNull(mimeMessageParser.getMimeMessage());
        assertFalse(mimeMessageParser.isMultipart());
        assertFalse(mimeMessageParser.hasHtmlContent());
        assertTrue(mimeMessageParser.hasPlainContent());
        assertNotNull(mimeMessageParser.getPlainContent());
        assertNull(mimeMessageParser.getHtmlContent());
        assertTrue(mimeMessageParser.getTo().size() == 1);
        assertTrue(mimeMessageParser.getCc().size() == 0);
        assertTrue(mimeMessageParser.getBcc().size() == 0);
        assertEquals("coheigea@apache.org", mimeMessageParser.getFrom());
        assertEquals("dev@ws.apache.org", mimeMessageParser.getReplyTo());
        assertFalse(mimeMessageParser.hasAttachments());
    }

    public void testParseHtmlEmailWithAttachments() throws Exception
    {
        DataSource dataSource;
        Session session = Session.getDefaultInstance(new Properties());
        MimeMessage message = MimeMessageUtils.createMimeMessage(session, new File("./src/test/resources/eml/html-attachment.eml"));
        MimeMessageParser mimeMessageParser = new MimeMessageParser(message);

        mimeMessageParser.parse();

        assertEquals("Test", mimeMessageParser.getSubject());
        assertNotNull(mimeMessageParser.getMimeMessage());        
        assertTrue(mimeMessageParser.isMultipart());
        assertTrue(mimeMessageParser.hasHtmlContent());
        assertTrue(mimeMessageParser.hasPlainContent());
        assertNotNull(mimeMessageParser.getPlainContent());
        assertNotNull(mimeMessageParser.getHtmlContent());
        assertTrue(mimeMessageParser.getTo().size() == 1);
        assertTrue(mimeMessageParser.getCc().size() == 0);
        assertTrue(mimeMessageParser.getBcc().size() == 0);
        assertEquals("siegfried.goeschl@it20one.at", mimeMessageParser.getFrom());
        assertEquals("siegfried.goeschl@it20one.at", mimeMessageParser.getReplyTo());
        assertTrue(mimeMessageParser.hasAttachments());
        List attachmentList = mimeMessageParser.getAttachmentList();
        assertTrue(attachmentList.size() == 2);

        dataSource = mimeMessageParser.findAttachmentByName("Wasserlilien.jpg");
        assertNotNull(dataSource);
        assertEquals("image/jpeg", dataSource.getContentType());

        dataSource = mimeMessageParser.findAttachmentByName("it20one.pdf");
        assertNotNull(dataSource);
        assertEquals("application/pdf", dataSource.getContentType());
    }

    /**
     * This test parses an "email read notification" where the resulting data source has no name. Originally
     * the missing name caused a NPE in MimeUtility.decodeText().
     *
     * @throws Exception the test failed
     */
    public void testParseMultipartReport() throws Exception
    {
        DataSource dataSource;
        Session session = Session.getDefaultInstance(new Properties());
        MimeMessage message = MimeMessageUtils.createMimeMessage(session, new File("./src/test/resources/eml/multipart-report.eml"));
        MimeMessageParser mimeMessageParser = new MimeMessageParser(message);

        mimeMessageParser.parse();

        assertEquals("Gelesen: ", mimeMessageParser.getSubject());
        assertNotNull(mimeMessageParser.getMimeMessage());
        assertTrue(mimeMessageParser.isMultipart());
        assertTrue(mimeMessageParser.hasHtmlContent());
        assertFalse(mimeMessageParser.hasPlainContent());
        assertNull(mimeMessageParser.getPlainContent());
        assertNotNull(mimeMessageParser.getHtmlContent());
        assertTrue(mimeMessageParser.getTo().size() == 1);
        assertTrue(mimeMessageParser.getCc().size() == 0);
        assertTrue(mimeMessageParser.getBcc().size() == 0);
        assertEquals("siegfried.goeschl@it20one.at", mimeMessageParser.getFrom());
        assertEquals("siegfried.goeschl@it20one.at", mimeMessageParser.getReplyTo());
        assertTrue(mimeMessageParser.hasAttachments());
        List attachmentList = mimeMessageParser.getAttachmentList();
        assertTrue(attachmentList.size() == 1);

        dataSource = (DataSource) attachmentList.get(0);
        assertNotNull(dataSource);
        assertNull(dataSource.getName());
        assertEquals("message/disposition-notification", dataSource.getContentType());
    }

    /**
     * This test parses a SAP generated email which only contains a PDF but no email
     * text.
     *
     * @throws Exception the test failed
     */
    public void testAttachmentOnly() throws Exception
    {
        DataSource dataSource;
        Session session = Session.getDefaultInstance(new Properties());
        MimeMessage message = MimeMessageUtils.createMimeMessage(session, new File("./src/test/resources/eml/attachment-only.eml"));
        MimeMessageParser mimeMessageParser = new MimeMessageParser(message);

        mimeMessageParser.parse();

        assertEquals("Kunde 100029   Auftrag   3600", mimeMessageParser.getSubject());
        assertNotNull(mimeMessageParser.getMimeMessage());
        assertFalse(mimeMessageParser.isMultipart());
        assertFalse(mimeMessageParser.hasHtmlContent());
        assertFalse(mimeMessageParser.hasPlainContent());
        assertNull(mimeMessageParser.getPlainContent());
        assertNull(mimeMessageParser.getHtmlContent());
        assertTrue(mimeMessageParser.getTo().size() == 1);
        assertTrue(mimeMessageParser.getCc().size() == 0);
        assertTrue(mimeMessageParser.getBcc().size() == 0);
        assertEquals("siegfried.goeschl@it20one.at", mimeMessageParser.getFrom());
        assertEquals("siegfried.goeschl@it20one.at", mimeMessageParser.getReplyTo());
        assertTrue(mimeMessageParser.hasAttachments());
        List attachmentList = mimeMessageParser.getAttachmentList();
        assertTrue(attachmentList.size() == 1);

        dataSource = mimeMessageParser.findAttachmentByName("Kunde 100029   Auftrag   3600.pdf");
        assertNotNull(dataSource);
        assertEquals("application/pdf", dataSource.getContentType());
    }
    
    /**
     * This test parses an eml file published with issue EMAIL-110.
     * This eml file has a corrupted attachment but should not create
     * an OutOfMemoryException.
     * 
     * @throws Exception the test failed
     */
    public void testParseNoHeaderSeperatorWithOutOfMemory() throws Exception
    {
        Session session = Session.getDefaultInstance(new Properties());
        MimeMessage message = MimeMessageUtils.createMimeMessage(session, new File("./src/test/resources/eml/outofmemory-no-header-seperation.eml"));
        MimeMessageParser mimeMessageParser = new MimeMessageParser(message);

        mimeMessageParser.parse();

        assertEquals("A corrupt Attachment", mimeMessageParser.getSubject());
        assertNotNull(mimeMessageParser.getMimeMessage());
        assertTrue(mimeMessageParser.isMultipart());
        assertFalse(mimeMessageParser.hasHtmlContent());
        assertFalse(mimeMessageParser.hasPlainContent());
        assertNull(mimeMessageParser.getPlainContent());
        assertNull(mimeMessageParser.getHtmlContent());
        assertEquals(mimeMessageParser.getTo().size(), 1);
        assertEquals(mimeMessageParser.getCc().size(), 0);
        assertEquals(mimeMessageParser.getBcc().size(), 0);
    }
}
