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

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.DataHandler;
import javax.mail.internet.MimeMessage;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.mail.mocks.MockImageHtmlEmailConcrete;
import org.subethamail.wiser.WiserMessage;

public class ImageHtmlEmailTest extends HtmlEmailTest {

    private static final URL TEST_IMAGE_URL = ImageHtmlEmailTest.class.getResource("/images/asf_logo_wide.gif");    
    private static final File TEST_IMAGE_DIR = new File(TEST_IMAGE_URL.getPath()).getParentFile();
    private static final URL TEST_HTML_URL = ImageHtmlEmailTest.class.getResource("/attachments/download_email.cgi.html");

    public ImageHtmlEmailTest(String name) throws IOException {
		super(name);
	}

	/** */
	private MockImageHtmlEmailConcrete email;

	/**
	 * @throws Exception
	 */
	protected void setUp() throws Exception {
		super.setUp();
		// reusable objects to be used across multiple tests
		email = new MockImageHtmlEmailConcrete();
	}

	public void testSendHTML() throws Exception {

		Logger.getLogger(ImageHtmlEmail.class.getName()).setLevel(Level.FINEST);

		// Create the email message
		getMailServer();

		String strSubject = "Test HTML Send default";

		email = new MockImageHtmlEmailConcrete();
		email.setHostName(strTestMailServer);
		email.setSmtpPort(getMailServerPort());
		email.setFrom(strTestMailFrom);
		email.addTo(strTestMailTo);
		email.setSubject(strSubject);

		URL url = TEST_HTML_URL;
		InputStream stream = url.openStream();
		StringBuilder str = new StringBuilder();
		try {
			List lines = IOUtils.readLines(stream);
			for(int i=0;i<lines.size();i++) {
                String line = (String) lines.get(i);
				str.append(line).append("\n");
			}
		} finally {
			stream.close();
		}
		String html = str.toString();

		// set the html message
		email.setHtmlMsg(html, TEST_IMAGE_DIR.toURI().toURL(), false);

		// set the alternative message
		//email.setTextMsg("Your email client does not support HTML messages");

		// send the email
		email.send();

		fakeMailServer.stop();

		// Cannot validate as the HTML is changed during inclusion
		// validate html message
		//validateSend(fakeMailServer, strSubject, email.getHtmlMsg(),
		//		email.getFromAddress(), email.getToAddresses(),
		//		email.getCcAddresses(), email.getBccAddresses(), false);
		assertEquals(1, fakeMailServer.getMessages().size());
		MimeMessage mimeMessage = ((WiserMessage) fakeMailServer.getMessages().get(0)).getMimeMessage();

		DataHandler dataHandler = mimeMessage.getDataHandler();
		ByteArrayOutputStream byteArrayOutStream = new ByteArrayOutputStream();
		BufferedOutputStream buffOs = new BufferedOutputStream(byteArrayOutStream);
		dataHandler.writeTo(buffOs);
		buffOs.flush();

		String msg = new String(byteArrayOutStream.toByteArray());
		// the mail should contain Content-ID-Entries
		assertTrue(msg, msg.contains("\"cid:"));
		// at least the logo that we store locally should be replaced by an included cid now
		assertFalse(msg, msg.contains("\"asf_logo_wide.gif\""));
	}

	public void testSendEmptyHTML() throws Exception {
		Logger.getLogger(ImageHtmlEmail.class.getName()).setLevel(Level.FINEST);

		// Create the email message
		ImageHtmlEmail email = new ImageHtmlEmail();

		// set the html message
		try {
			email.setHtmlMsg(null, new File("/tmp").toURI().toURL(), false);
			fail("Should fail here!");
		} catch (EmailException e) {
			assertTrue(e.getMessage(), e.getMessage().contains(
					"Invalid message supplied"));
		}
	}

	public void testSendEmptyHTML2() throws Exception {
		Logger.getLogger(ImageHtmlEmail.class.getName()).setLevel(Level.FINEST);

		// Create the email message
		ImageHtmlEmail email = new ImageHtmlEmail();

		// set the html message
		try {
			email.setHtmlMsg("", new File("/tmp").toURI().toURL(), false);
			fail("Should fail here!");
		} catch (EmailException e) {
			assertTrue(e.getMessage(), e.getMessage().contains(
					"Invalid message supplied"));
		}

	}

	public void testSendHTMLURL() throws Exception {
		Logger.getLogger(ImageHtmlEmail.class.getName()).setLevel(Level.FINEST);

		getMailServer();

		String strSubject = "Test HTML Send default with URL";

		// Create the email message
		email = new MockImageHtmlEmailConcrete();
		email.setHostName(strTestMailServer);
		email.setSmtpPort(getMailServerPort());
		email.setFrom(strTestMailFrom);
		email.addTo(strTestMailTo);
		email.setSubject(strSubject);

		// set the html message
		email.setHtmlMsg("<html><body><img src=\"http://dstadler.org/mambo2/templates/jo_beetle_adjusted/images/beetle2.jpg\"/></body></html>");

		// send the email
		email.send();

		fakeMailServer.stop();
		// validate txt message
		validateSend(fakeMailServer, strSubject, email.getHtmlMsg(),
				email.getFromAddress(), email.getToAddresses(),
				email.getCcAddresses(), email.getBccAddresses(), true);
	}

	public void testSendHTMLAbsoluteLocalFile() throws Exception {
		Logger.getLogger(ImageHtmlEmail.class.getName()).setLevel(Level.FINEST);

		// Create the email message
		getMailServer();

		String strSubject = "Test HTML Send default with absolute local path";

		// Create the email message
		email = new MockImageHtmlEmailConcrete();
		email.setHostName(strTestMailServer);
		email.setSmtpPort(getMailServerPort());
		email.setFrom(strTestMailFrom);
		email.addTo(strTestMailTo);
		email.setSubject(strSubject);

		File file = File.createTempFile("emailtest", ".tst");
		FileUtils.writeStringToFile(file,
				"just some silly data that we won't be able to display anyway");

		// set the html message
		email.setHtmlMsg("<html><body><img src=\"" + file.getAbsolutePath()
				+ "\"/></body></html>", new File("").toURI().toURL(), false);

		// send the email
		email.send();

		fakeMailServer.stop();
		// validate txt message
		validateSend(fakeMailServer, strSubject, email.getHtmlMsg(),
				email.getFromAddress(), email.getToAddresses(),
				email.getCcAddresses(), email.getBccAddresses(), true);
	}

	public void testRegex() 
	{	
		Pattern pattern = Pattern.compile(ImageHtmlEmail.REGEX_IMG_SRC);

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

		// try with invalid HTML that is seens sometimes, i.e. without closing "/" or "</img>"
		matcher = pattern
				.matcher("<img src=\"file1\"><img src=\"file2\">");
		assertTrue(matcher.find());
		assertEquals("file1", matcher.group(2));
		assertTrue(matcher.find());
		assertEquals("file2", matcher.group(2));        
	}
}
