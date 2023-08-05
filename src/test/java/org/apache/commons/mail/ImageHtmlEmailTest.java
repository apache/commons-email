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

import static org.junit.Assert.*;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.mail.resolver.DataSourceClassPathResolver;
import org.apache.commons.mail.resolver.DataSourceCompositeResolver;
import org.apache.commons.mail.resolver.DataSourceUrlResolver;
import org.apache.commons.mail.mocks.MockImageHtmlEmailConcrete;
import org.apache.commons.mail.util.MimeMessageParser;
import org.apache.commons.mail.util.MimeMessageUtils;
import org.junit.Before;
import org.junit.Test;

import javax.activation.DataSource;
import javax.mail.internet.MimeMessage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImageHtmlEmailTest extends HtmlEmailTest {

    private static final boolean TEST_IS_LENIENT = true;
    private static final URL TEST_IMAGE_URL = ImageHtmlEmailTest.class.getResource("/images/asf_logo_wide.gif");
    private static final File TEST_IMAGE_DIR = new File(TEST_IMAGE_URL.getPath()).getParentFile();
    private static final URL TEST_HTML_URL = ImageHtmlEmailTest.class.getResource("/attachments/download_email.cgi.html");
    private static final URL TEST2_HTML_URL = ImageHtmlEmailTest.class.getResource("/attachments/classpathtest.html");

    private MockImageHtmlEmailConcrete email;

    @Before
    public void setupImageHtmlEmailTest() {
        // reusable objects to be used across multiple tests
        email = new MockImageHtmlEmailConcrete();
    }

    // ======================================================================
    // Start of Tests
    // ======================================================================

    @Test
    public void testSendHtml() throws Exception {

        Logger.getLogger(ImageHtmlEmail.class.getName()).setLevel(Level.FINEST);

        // Create the email message
        getMailServer();

        final String strSubject = "Test HTML Send default";

        email = new MockImageHtmlEmailConcrete();
        email.setDataSourceResolver(new DataSourceUrlResolver(TEST_IMAGE_DIR.toURI().toURL(), TEST_IS_LENIENT));
        email.setHostName(strTestMailServer);
        email.setSmtpPort(getMailServerPort());
        email.setFrom(strTestMailFrom);
        email.addTo(strTestMailTo);
        email.setSubject(strSubject);

        final String html = loadUrlContent(TEST_HTML_URL);

        // set the html message
        email.setHtmlMsg(html);

        // set the alternative message
        email.setTextMsg("Your email client does not support HTML messages");

        // send the email
        email.send();

        fakeMailServer.stop();

        assertEquals(1, fakeMailServer.getMessages().size());
        final MimeMessage mimeMessage = fakeMailServer.getMessages().get(0).getMimeMessage();

        final MimeMessageParser mimeMessageParser = new MimeMessageParser(mimeMessage).parse();
        assertTrue(mimeMessageParser.getHtmlContent().contains("\"cid:"));
        assertEquals(3, mimeMessageParser.getAttachmentList().size());
    }

    @Test
    public void testSendEmptyHTML() throws Exception {
        Logger.getLogger(ImageHtmlEmail.class.getName()).setLevel(Level.FINEST);

        // Create the email message
        final ImageHtmlEmail email = new ImageHtmlEmail();

        // set the html message
        try {
            email.setHtmlMsg(null);
            fail("Should fail here!");
        } catch (final EmailException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("Invalid message supplied"));
        }
    }

    @Test
    public void testSendEmptyHTML2() throws Exception {
        Logger.getLogger(ImageHtmlEmail.class.getName()).setLevel(Level.FINEST);

        // Create the email message
        final ImageHtmlEmail email = new ImageHtmlEmail();

        // set the html message
        try {
            email.setHtmlMsg("");
            fail("Should fail here!");
        } catch (final EmailException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("Invalid message supplied"));
        }

    }

    @Test
    public void testSendHtmlUrl() throws Exception {
        Logger.getLogger(ImageHtmlEmail.class.getName()).setLevel(Level.FINEST);

        getMailServer();

        final String strSubject = "Test HTML Send default with URL";

        // Create the email message
        email = new MockImageHtmlEmailConcrete();
        email.setHostName(strTestMailServer);
        email.setSmtpPort(getMailServerPort());
        email.setFrom(strTestMailFrom);
        email.addTo(strTestMailTo);
        email.setSubject(strSubject);
        email.setDataSourceResolver(new DataSourceUrlResolver(TEST_IMAGE_DIR.toURI().toURL(), TEST_IS_LENIENT));

        // set the html message
        email.setHtmlMsg(
                "<html><body><img src=\"http://www.apache.org/images/feather.gif\"/></body></html>"
        );

        // send the email
        email.send();

        fakeMailServer.stop();
        // validate txt message
        validateSend(fakeMailServer, strSubject, email.getHtmlMsg(),
                email.getFromAddress(), email.getToAddresses(),
                email.getCcAddresses(), email.getBccAddresses(), true);
    }

    @Test
    public void testSendHTMLAbsoluteLocalFile() throws Exception {
        Logger.getLogger(ImageHtmlEmail.class.getName()).setLevel(Level.FINEST);

        // Create the email message
        getMailServer();

        final String strSubject = "Test HTML Send default with absolute local path";

        // Create the email message
        email = new MockImageHtmlEmailConcrete();
        email.setHostName(strTestMailServer);
        email.setSmtpPort(getMailServerPort());
        email.setFrom(strTestMailFrom);
        email.addTo(strTestMailTo);
        email.setSubject(strSubject);
        email.setDataSourceResolver(new DataSourceUrlResolver(TEST_IMAGE_DIR.toURI().toURL(), TEST_IS_LENIENT));

        final File file = File.createTempFile("emailtest", ".tst");
        FileUtils.writeStringToFile(file,
                "just some silly data that we won't be able to display anyway");

        // set the html message
        email.setHtmlMsg("<html><body><img src=\"" + file.getAbsolutePath()
                + "\"/></body></html>"
        );

        // send the email
        email.send();

        fakeMailServer.stop();
        // validate txt message
        validateSend(fakeMailServer, strSubject, email.getHtmlMsg(),
                email.getFromAddress(), email.getToAddresses(),
                email.getCcAddresses(), email.getBccAddresses(), true);
    }

    @Test
    public void testSendHTMLClassPathFile() throws Exception {
        Logger.getLogger(ImageHtmlEmail.class.getName()).setLevel(Level.FINEST);

        // Create the email message
        getMailServer();

        final String strSubject = "Test HTML Send default";

        email = new MockImageHtmlEmailConcrete();
        email.setDataSourceResolver(new DataSourceClassPathResolver("/", TEST_IS_LENIENT));
        email.setHostName(strTestMailServer);
        email.setSmtpPort(getMailServerPort());
        email.setFrom(strTestMailFrom);
        email.addTo(strTestMailTo);
        email.setSubject(strSubject);

        final String html = loadUrlContent(TEST2_HTML_URL);

        // set the html message
        email.setHtmlMsg(html);

        // set the alternative message
        email.setTextMsg("Your email client does not support HTML messages");

        // send the email
        email.send();

        fakeMailServer.stop();

        assertEquals(1, fakeMailServer.getMessages().size());
        final MimeMessage mimeMessage = fakeMailServer.getMessages().get(0).getMimeMessage();
        MimeMessageUtils.writeMimeMessage(mimeMessage, new File("./target/test-emails/testSendHTMLClassPathFile.eml"));

        final MimeMessageParser mimeMessageParser = new MimeMessageParser(mimeMessage).parse();
        assertTrue(mimeMessageParser.getHtmlContent().contains("\"cid:"));
        assertEquals(1, mimeMessageParser.getAttachmentList().size());
    }

    @Test
    public void testSendClassPathFileWithNullName() throws Exception {
        Logger.getLogger(ImageHtmlEmail.class.getName()).setLevel(Level.FINEST);

        // Create the email message
        getMailServer();

        final String strSubject = "Test HTML Send default";

        email = new MockImageHtmlEmailConcrete();
        email.setDataSourceResolver(new MockDataSourceClassPathResolver("/", TEST_IS_LENIENT));
        email.setHostName(strTestMailServer);
        email.setSmtpPort(getMailServerPort());
        email.setFrom(strTestMailFrom);
        email.addTo(strTestMailTo);
        email.setSubject(strSubject);

        final String html = loadUrlContent(TEST2_HTML_URL);

        // set the html message
        email.setHtmlMsg(html);

        // set the alternative message
        email.setTextMsg("Your email client does not support HTML messages");

        // send the email
        email.send();

        fakeMailServer.stop();

        assertEquals(1, fakeMailServer.getMessages().size());
        final MimeMessage mimeMessage = fakeMailServer.getMessages().get(0).getMimeMessage();
        MimeMessageUtils.writeMimeMessage(mimeMessage,
                new File("./target/test-emails/testSendClassPathFileWithNullName.eml"));

        final MimeMessageParser mimeMessageParser = new MimeMessageParser(mimeMessage).parse();
        assertTrue(mimeMessageParser.getHtmlContent().contains("\"cid:"));
        assertEquals(1, mimeMessageParser.getAttachmentList().size());
    }

    @Test
    public void testSendHTMLAutoResolveFile() throws Exception {
        Logger.getLogger(ImageHtmlEmail.class.getName()).setLevel(Level.FINEST);

        // Create the email message
        getMailServer();

        final String strSubject = "Test HTML Send default";

        email = new MockImageHtmlEmailConcrete();
        final DataSourceResolver dataSourceResolvers[] = new DataSourceResolver[2];
        dataSourceResolvers[0] = new DataSourceUrlResolver(new URL("http://foo"), true);
        dataSourceResolvers[1] = new DataSourceClassPathResolver("/", true);

        email.setDataSourceResolver(new DataSourceCompositeResolver(dataSourceResolvers));
        email.setHostName(strTestMailServer);
        email.setSmtpPort(getMailServerPort());
        email.setFrom(strTestMailFrom);
        email.addTo(strTestMailTo);
        email.setSubject(strSubject);

        final String html = loadUrlContent(TEST2_HTML_URL);

        // set the html message
        email.setHtmlMsg(html);

        // set the alternative message
        email.setTextMsg("Your email client does not support HTML messages");

        // send the email
        email.send();

        fakeMailServer.stop();

        assertEquals(1, fakeMailServer.getMessages().size());
        final MimeMessage mimeMessage = fakeMailServer.getMessages().get(0).getMimeMessage();
        MimeMessageUtils.writeMimeMessage(mimeMessage, new File("./target/test-emails/testSendHTMLAutoFile.eml"));

        final MimeMessageParser mimeMessageParser = new MimeMessageParser(mimeMessage).parse();
        assertTrue(mimeMessageParser.getHtmlContent().contains("\"cid:"));
        assertEquals(1, mimeMessageParser.getAttachmentList().size());
    }

    @Test
    public void testSendHTMLAutoResolveMultipleFiles() throws Exception {
        Logger.getLogger(ImageHtmlEmail.class.getName()).setLevel(Level.FINEST);

        // Create the email message
        getMailServer();

        final String strSubject = "Test HTML Send default";

        email = new MockImageHtmlEmailConcrete();
        final DataSourceResolver dataSourceResolver = new DataSourceClassPathResolver("/", true);

        email.setDataSourceResolver(dataSourceResolver);
        email.setHostName(strTestMailServer);
        email.setSmtpPort(getMailServerPort());
        email.setFrom(strTestMailFrom);
        email.addTo(strTestMailTo);
        email.setSubject(strSubject);

        final String html = "<p>First image  <img src=\"images/contentTypeTest.gif\"/></p>" +
                      "<p>Second image <img src=\"images/contentTypeTest.jpg\"/></p>" +
                      "<p>Third image  <img src=\"images/contentTypeTest.png\"/></p>";

        // set the html message
        email.setHtmlMsg(html);

        // set the alternative message
        email.setTextMsg("Your email client does not support HTML messages");

        // send the email
        email.send();

        fakeMailServer.stop();

        assertEquals(1, fakeMailServer.getMessages().size());
        final MimeMessage mimeMessage = fakeMailServer.getMessages().get(0).getMimeMessage();
        MimeMessageUtils.writeMimeMessage(mimeMessage, new File("./target/test-emails/testSendHTMLAutoMultipleFiles.eml"));

        final MimeMessageParser mimeMessageParser = new MimeMessageParser(mimeMessage).parse();
        assertTrue(mimeMessageParser.getHtmlContent().contains("\"cid:"));
        assertEquals(3, mimeMessageParser.getAttachmentList().size());
    }

    @Test
    public void testRegex() {
        final Pattern pattern = Pattern.compile(ImageHtmlEmail.REGEX_IMG_SRC);

        // ensure that the regex that we use is catching the cases correctly
        Matcher matcher = pattern
                .matcher("<html><body><img src=\"h\"/></body></html>");
        assertTrue(matcher.find());
        assertEquals("h", matcher.group(2));

        matcher = pattern
                .matcher("<html><body><img id=\"laskdasdkj\" src=\"h\"/></body></html>");
        assertTrue(matcher.find());
        assertEquals("h", matcher.group(2));

        // uppercase
        matcher = pattern
                .matcher("<html><body><IMG id=\"laskdasdkj\" SRC=\"h\"/></body></html>");
        assertTrue(matcher.find());
        assertEquals("h", matcher.group(2));

        // matches twice
        matcher = pattern
                .matcher("<html><body><img id=\"laskdasdkj\" src=\"http://dstadler1.org/\"/><img id=\"laskdasdkj\" src=\"http://dstadler2.org/\"/></body></html>");
        assertTrue(matcher.find());
        assertEquals("http://dstadler1.org/", matcher.group(2));
        assertTrue(matcher.find());
        assertEquals("http://dstadler2.org/", matcher.group(2));

        // what about newlines
        matcher = pattern
                .matcher("<html><body><img\n \rid=\"laskdasdkj\"\n \rsrc=\"http://dstadler1.org/\"/><img id=\"laskdasdkj\" src=\"http://dstadler2.org/\"/></body></html>");
        assertTrue(matcher.find());
        assertEquals("http://dstadler1.org/", matcher.group(2));
        assertTrue(matcher.find());
        assertEquals("http://dstadler2.org/", matcher.group(2));

        // what about newlines and other whitespaces
        matcher = pattern
                .matcher("<html><body><img\n \t\rid=\"laskdasdkj\"\n \rsrc \n =\r  \"http://dstadler1.org/\"/><img  \r  id=\" laskdasdkj\"    src    =   \"http://dstadler2.org/\"/></body></html>");
        assertTrue(matcher.find());
        assertEquals("http://dstadler1.org/", matcher.group(2));
        assertTrue(matcher.find());
        assertEquals("http://dstadler2.org/", matcher.group(2));

        // what about some real markup
        matcher = pattern.matcher("<img alt=\"Chart?ck=xradar&amp;w=120&amp;h=120&amp;c=7fff00|7fff00&amp;m=4&amp;g=0\" src=\"/chart?ck=xradar&amp;w=120&amp;h=120&amp;c=7fff00|7fff00&amp;m=4&amp;g=0.2&amp;l=A,C,S,T&amp;v=3.0,3.0,2.0,2.0\"");
        assertTrue(matcher.find());
        assertEquals("/chart?ck=xradar&amp;w=120&amp;h=120&amp;c=7fff00|7fff00&amp;m=4&amp;g=0.2&amp;l=A,C,S,T&amp;v=3.0,3.0,2.0,2.0", matcher.group(2));

        // had a problem with multiple img-source tags
        matcher = pattern
                .matcher("<img src=\"file1\"/><img src=\"file2\"/>");
        assertTrue(matcher.find());
        assertEquals("file1", matcher.group(2));
        assertTrue(matcher.find());
        assertEquals("file2", matcher.group(2));

        matcher = pattern
                .matcher("<img src=\"file1\"/><img src=\"file2\"/><img src=\"file3\"/><img src=\"file4\"/><img src=\"file5\"/>");
        assertTrue(matcher.find());
        assertEquals("file1", matcher.group(2));
        assertTrue(matcher.find());
        assertEquals("file2", matcher.group(2));
        assertTrue(matcher.find());
        assertEquals("file3", matcher.group(2));
        assertTrue(matcher.find());
        assertEquals("file4", matcher.group(2));
        assertTrue(matcher.find());
        assertEquals("file5", matcher.group(2));

        // try with invalid HTML that is seems sometimes, i.e. without closing "/" or "</img>"
        matcher = pattern.matcher("<img src=\"file1\"><img src=\"file2\">");
        assertTrue(matcher.find());
        assertEquals("file1", matcher.group(2));
        assertTrue(matcher.find());
        assertEquals("file2", matcher.group(2));
    }

    @Test
    public void testEmail127() throws Exception {
        Logger.getLogger(ImageHtmlEmail.class.getName()).setLevel(Level.FINEST);

        getMailServer();

        final String strSubject = "Test HTML Send default with URL";

        // Create the email message
        email = new MockImageHtmlEmailConcrete();
        email.setHostName(strTestMailServer);
        email.setSmtpPort(getMailServerPort());
        email.setFrom(strTestMailFrom);
        email.addTo(strTestMailTo);
        email.setSubject(strSubject);
        email.setDataSourceResolver(new DataSourceUrlResolver(TEST_IMAGE_DIR.toURI().toURL(), TEST_IS_LENIENT));

        // set the html message
        email.setHtmlMsg("<html><body><img title=\"$\" src=\"http://www.apache.org/images/feather.gif\"/></body></html>");

        // send the email
        email.send();

        fakeMailServer.stop();
        // validate txt message
        validateSend(fakeMailServer, strSubject, email.getHtmlMsg(),
                     email.getFromAddress(), email.getToAddresses(),
                     email.getCcAddresses(), email.getBccAddresses(), true);
    }

    private String loadUrlContent(final URL url) throws IOException {
        try (final InputStream stream = url.openStream()) {
            final StringBuilder html = new StringBuilder();
            try {
                final List<String> lines = IOUtils.readLines(stream);
                for (final String line : lines) {
                    html.append(line).append("\n");
                }
            } finally {
                stream.close();
            }
            return html.toString();
        }
    }

    private static final class MockDataSourceClassPathResolver extends DataSourceClassPathResolver {

        public MockDataSourceClassPathResolver(final String classPathBase, final boolean lenient) {
            super(classPathBase, lenient);
        }

        @Override
        public DataSource resolve(final String resourceLocation, final boolean isLenient) throws IOException {
            final javax.mail.util.ByteArrayDataSource ds =
                    (javax.mail.util.ByteArrayDataSource) super.resolve(resourceLocation, isLenient);
            ds.setName(null);
            return ds;
        }

    }
}
