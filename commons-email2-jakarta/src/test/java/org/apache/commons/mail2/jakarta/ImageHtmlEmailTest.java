/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.mail2.jakarta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.mail2.core.EmailException;
import org.apache.commons.mail2.jakarta.mocks.MockImageHtmlEmailConcrete;
import org.apache.commons.mail2.jakarta.resolver.DataSourceClassPathResolver;
import org.apache.commons.mail2.jakarta.resolver.DataSourceCompositeResolver;
import org.apache.commons.mail2.jakarta.resolver.DataSourceUrlResolver;
import org.apache.commons.mail2.jakarta.util.MimeMessageParser;
import org.apache.commons.mail2.jakarta.util.MimeMessageUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.activation.DataSource;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ImageHtmlEmailTest extends HtmlEmailTest {

    private static final class MockDataSourceClassPathResolver extends DataSourceClassPathResolver {

        public MockDataSourceClassPathResolver(final String classPathBase, final boolean lenient) {
            super(classPathBase, lenient);
        }

        @Override
        public DataSource resolve(final String resourceLocation, final boolean isLenient) throws IOException {
            final jakarta.mail.util.ByteArrayDataSource ds = (jakarta.mail.util.ByteArrayDataSource) super.resolve(resourceLocation, isLenient);
            ds.setName(null);
            return ds;
        }

    }

    private static final boolean TEST_IS_LENIENT = true;
    private static final URL TEST_IMAGE_URL = ImageHtmlEmailTest.class.getResource("/images/asf_logo_wide.gif");
    private static final File TEST_IMAGE_DIR = new File(TEST_IMAGE_URL.getPath()).getParentFile();
    private static final URL TEST_HTML_URL = ImageHtmlEmailTest.class.getResource("/attachments/download_email.cgi.html");

    private static final URL TEST2_HTML_URL = ImageHtmlEmailTest.class.getResource("/attachments/classpathtest.html");

    private static final Pattern imgSrcPattern = Pattern.compile(ImageHtmlEmail.REGEX_IMG_SRC);
    private static final Pattern scriptSrcPattern = Pattern.compile(ImageHtmlEmail.REGEX_SCRIPT_SRC);
    private MockImageHtmlEmailConcrete email;

    private String loadUrlContent(final URL url) throws IOException {
        try (final InputStream stream = url.openStream()) {
            final StringBuilder html = new StringBuilder();
            try {
                final List<String> lines = IOUtils.readLines(stream, StandardCharsets.UTF_8);
                for (final String line : lines) {
                    html.append(line).append("\n");
                }
            } finally {
                stream.close();
            }
            return html.toString();
        }
    }

    @BeforeEach
    public void setupImageHtmlEmailTest() {
        // reusable objects to be used across multiple tests
        email = new MockImageHtmlEmailConcrete();
    }
    // Start of Tests

    @Test
    void testEmail127() throws Exception {
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
        validateSend(fakeMailServer, strSubject, email.getHtml(), email.getFromAddress(), email.getToAddresses(), email.getCcAddresses(),
                email.getBccAddresses(), true);
    }

    @Test
    void testRegex() {
        final Pattern pattern = Pattern.compile(ImageHtmlEmail.REGEX_IMG_SRC);

        // ensure that the regex that we use is catching the cases correctly
        Matcher matcher = pattern.matcher("<html><body><img src=\"h\"/></body></html>");
        assertTrue(matcher.find());
        assertEquals("h", matcher.group(2));

        matcher = pattern.matcher("<html><body><img id=\"laskdasdkj\" src=\"h\"/></body></html>");
        assertTrue(matcher.find());
        assertEquals("h", matcher.group(2));

        // uppercase
        matcher = pattern.matcher("<html><body><IMG id=\"laskdasdkj\" SRC=\"h\"/></body></html>");
        assertTrue(matcher.find());
        assertEquals("h", matcher.group(2));

        // matches twice
        matcher = pattern.matcher(
                "<html><body><img id=\"laskdasdkj\" src=\"http://dstadler1.org/\"/><img id=\"laskdasdkj\" src=\"http://dstadler2.org/\"/></body></html>");
        assertTrue(matcher.find());
        assertEquals("http://dstadler1.org/", matcher.group(2));
        assertTrue(matcher.find());
        assertEquals("http://dstadler2.org/", matcher.group(2));

        // what about newlines
        matcher = pattern.matcher(
                "<html><body><img\n \rid=\"laskdasdkj\"\n \rsrc=\"http://dstadler1.org/\"/><img id=\"laskdasdkj\" src=\"http://dstadler2.org/\"/></body></html>");
        assertTrue(matcher.find());
        assertEquals("http://dstadler1.org/", matcher.group(2));
        assertTrue(matcher.find());
        assertEquals("http://dstadler2.org/", matcher.group(2));

        // what about newlines and other whitespaces
        matcher = pattern.matcher(
                "<html><body><img\n \t\rid=\"laskdasdkj\"\n \rsrc \n =\r  \"http://dstadler1.org/\"/><img  \r  id=\" laskdasdkj\"    src    =   \"http://dstadler2.org/\"/></body></html>");
        assertTrue(matcher.find());
        assertEquals("http://dstadler1.org/", matcher.group(2));
        assertTrue(matcher.find());
        assertEquals("http://dstadler2.org/", matcher.group(2));

        // what about some real markup
        matcher = pattern.matcher(
                "<img alt=\"Chart?ck=xradar&amp;w=120&amp;h=120&amp;c=7fff00|7fff00&amp;m=4&amp;g=0\" src=\"/chart?ck=xradar&amp;w=120&amp;h=120&amp;c=7fff00|7fff00&amp;m=4&amp;g=0.2&amp;l=A,C,S,T&amp;v=3.0,3.0,2.0,2.0\"");
        assertTrue(matcher.find());
        assertEquals("/chart?ck=xradar&amp;w=120&amp;h=120&amp;c=7fff00|7fff00&amp;m=4&amp;g=0.2&amp;l=A,C,S,T&amp;v=3.0,3.0,2.0,2.0", matcher.group(2));

        // had a problem with multiple img-source tags
        matcher = pattern.matcher("<img src=\"file1\"/><img src=\"file2\"/>");
        assertTrue(matcher.find());
        assertEquals("file1", matcher.group(2));
        assertTrue(matcher.find());
        assertEquals("file2", matcher.group(2));

        matcher = pattern.matcher("<img src=\"file1\"/><img src=\"file2\"/><img src=\"file3\"/><img src=\"file4\"/><img src=\"file5\"/>");
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

        // should not match any other tags
        matcher = pattern.matcher("<nomatch src=\"s\" />");
        assertFalse(matcher.find());

        matcher = pattern.matcher("<imgx src=\"file1\">");
        assertFalse(matcher.find());

        // should not match any other attribute
        matcher = pattern.matcher("<img xsrc=\"file1\">");
        assertFalse(matcher.find());
    }

    @ParameterizedTest
    @MethodSource
    public void testScriptRegex(String inputHtml, List<String> srcMatches) {
        Matcher matcher = scriptSrcPattern.matcher(inputHtml);
        for (String expectedMatch : srcMatches) {
            assertTrue(matcher.find());
            assertEquals(expectedMatch, matcher.group(2));
        }
        assertFalse(matcher.find());
    }

    private static Stream<Arguments> testScriptRegex() {
        Stream<Arguments> argumentsStream = Stream.of(
                // ensure that the regex that we use is catching the cases correctly
                Arguments.of("<html><body><script src=\"s\"></script></body></html>", Arrays.asList("s")),
                Arguments.of("<html><body><script blocking=\"render\" async src=\"s\"></script></body></html>", Arrays.asList("s")),
                // uppercase
                Arguments.of("<html><body><SCRIPT BLOCKING=\"render\" ASYNC SRC=\"s\"></script></body></html>", Arrays.asList("s")),
                // matches twice
                Arguments.of("<html><body><script src=\"s1\"></script><script src=\"s2\"></script></body></html>", Arrays.asList("s1", "s2")),
                // what about newlines
                Arguments.of("<html><body><script\n \rsrc=\"s1\"></script><script \nsrc=\"s2\"></script></body></html>", Arrays.asList("s1", "s2")),
                // what about newlines and other whitespaces
                Arguments.of("<html><body><script\n \rsrc  = \t \"s1\" ></script><script \nsrc =\t\"s2\" ></script></body></html>", Arrays.asList("s1", "s2")),
                // what about some real markup
                Arguments.of("<script defer=\"\" nomodule=\"\" nonce=\"\" src=\"/jkao/_next/static/chunks/polyfills-c67a75d1b6f99dc8.js\"></script>", Arrays.asList("/jkao/_next/static/chunks/polyfills-c67a75d1b6f99dc8.js")),
                // try with 5
                Arguments.of("<html><body><script src=\"s1\"></script><script src=\"s2\"></script><script src=\"s3\"></script><script src=\"s4\"></script><script src=\"s5\"></script></body></html>", Arrays.asList("s1", "s2", "s3", "s4", "s5")),
                // try with invalid scripts
                Arguments.of("<script src=\"s\" />", Arrays.asList("s")),
                Arguments.of("<script src=\"s\">", Arrays.asList("s")),
                // should not match any other tags
                Arguments.of("<nomatch src=\"s\" />", Arrays.asList()),
                Arguments.of("<scriptx src=\"s\" />", Arrays.asList()),
                // should not match any other attribute
                Arguments.of("<script xsrc=\"s\" />", Arrays.asList())
        );
        return argumentsStream;
    }

    @Test
    void testSendClassPathFileWithNullName() throws Exception {
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
        MimeMessageUtils.writeMimeMessage(mimeMessage, new File("./target/test-emails/testSendClassPathFileWithNullName.eml"));

        final MimeMessageParser mimeMessageParser = new MimeMessageParser(mimeMessage).parse();
        assertTrue(mimeMessageParser.getHtmlContent().contains("\"cid:"));
        assertEquals(1, mimeMessageParser.getAttachmentList().size());
    }

    @Test
    void testSendEmptyHTML() throws Exception {
        Logger.getLogger(ImageHtmlEmail.class.getName()).setLevel(Level.FINEST);

        // Create the email message
        final ImageHtmlEmail email = new ImageHtmlEmail();

        // set the HTML message
        final EmailException e = assertThrows(EmailException.class, () -> email.setHtmlMsg(null));
        assertTrue(e.getMessage().contains("Invalid message."), e.getMessage());
    }

    @Test
    void testSendEmptyHTML2() throws Exception {
        Logger.getLogger(ImageHtmlEmail.class.getName()).setLevel(Level.FINEST);

        // Create the email message
        final ImageHtmlEmail email = new ImageHtmlEmail();

        // set the HTML message
        final EmailException e = assertThrows(EmailException.class, () -> email.setHtmlMsg(""));
        assertTrue(e.getMessage().contains("Invalid message."), e.getMessage());

    }

    @Test
    void testSendHtml() throws Exception {

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
    void testSendHTMLAbsoluteLocalFile() throws Exception {
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
        FileUtils.writeStringToFile(file, "just some silly data that we won't be able to display anyway", StandardCharsets.UTF_8);

        // set the html message
        email.setHtmlMsg("<html><body><img src=\"" + file.getAbsolutePath() + "\"/></body></html>");

        // send the email
        email.send();

        fakeMailServer.stop();
        // validate txt message
        validateSend(fakeMailServer, strSubject, email.getHtml(), email.getFromAddress(), email.getToAddresses(), email.getCcAddresses(),
                email.getBccAddresses(), true);
    }

    @Test
    void testSendHTMLAutoResolveFile() throws Exception {
        Logger.getLogger(ImageHtmlEmail.class.getName()).setLevel(Level.FINEST);

        // Create the email message
        getMailServer();

        final String strSubject = "Test HTML Send default";

        email = new MockImageHtmlEmailConcrete();
        final DataSourceResolver[] dataSourceResolvers = new DataSourceResolver[2];
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
    void testSendHTMLAutoResolveMultipleFiles() throws Exception {
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

        final String html = "<p>First image  <img src=\"images/contentTypeTest.gif\"/></p><p>Second image <img src=\"images/contentTypeTest.jpg\"/></p>"
                + "<p>Third image  <img src=\"images/contentTypeTest.png\"/></p>";

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
        assertTrue(mimeMessageParser.getHtmlContent().contains("\"cid:"), mimeMessageParser.getHtmlContent());
        assertEquals(3, mimeMessageParser.getAttachmentList().size());
    }

    @Test
    void testSendHTMLClassPathFile() throws Exception {
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
    void testSendHtmlUrl() throws Exception {
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
        email.setHtmlMsg("<html><body><img src=\"http://www.apache.org/images/feather.gif\"/></body></html>");

        // send the email
        email.send();

        fakeMailServer.stop();
        // validate txt message
        validateSend(fakeMailServer, strSubject, email.getHtml(), email.getFromAddress(), email.getToAddresses(), email.getCcAddresses(),
                email.getBccAddresses(), true);
    }

    @Test
    public void testEmailWithScript() throws Exception {
        Logger.getLogger(ImageHtmlEmail.class.getName()).setLevel(Level.FINEST);

        getMailServer();

        final String strSubject = "Test HTML Send with Script";

        // Create the email message
        email = new MockImageHtmlEmailConcrete();
        final DataSourceResolver dataSourceResolver = new DataSourceClassPathResolver("/", true);

        email.setDataSourceResolver(dataSourceResolver);
        email.setHostName(strTestMailServer);
        email.setSmtpPort(getMailServerPort());
        email.setFrom(strTestMailFrom);
        email.addTo(strTestMailTo);
        email.setSubject(strSubject);

        // set the html message
        email.setHtmlMsg("<html><body><script  type=\"text/javascript\" src=\"scripts/example-script.js\"/></body></html>");

        // send the email
        email.send();

        fakeMailServer.stop();

        assertEquals(1, fakeMailServer.getMessages().size());
        final MimeMessage mimeMessage = fakeMailServer.getMessages().get(0).getMimeMessage();
        MimeMessageUtils.writeMimeMessage(mimeMessage, new File("./target/test-emails/testEmailWithScript.eml"));

        final MimeMessageParser mimeMessageParser = new MimeMessageParser(mimeMessage).parse();
        assertTrue(mimeMessageParser.getHtmlContent().contains("\"cid:"));
        assertEquals(1, mimeMessageParser.getAttachmentList().size());
    }
}
