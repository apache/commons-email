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
package org.apache.commons.mail;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.mail.mocks.MockHtmlEmailConcrete;
import org.apache.commons.mail.settings.EmailConfiguration;

/**
 * JUnit test case verifying bugzilla issue 30973 is fixed.
 *
 * @author <a href="mailto:corey.scott@gmail.com">Corey Scott</a>
 * @version $Id$
 */

public class SendWithAttachmentsTest extends BaseEmailTestCase
{
    /** */
    private MockHtmlEmailConcrete email = null;

    /**
     * @param name name
     */
    public SendWithAttachmentsTest(String name)
    {
        super(name);
    }

    /** */
    protected void setUp()
    {
        super.setUp();
        // reusable objects to be used across multiple tests
        this.email = new MockHtmlEmailConcrete();
    }

    /** */
    public void testSendNoAttachments()
    {
        try
        {
            this.getMailServer();

            String strSubject = "Test HTML Send #1 Subject (w charset)";

            this.email = new MockHtmlEmailConcrete();
            this.email.setHostName(this.strTestMailServer);
            this.email.setSmtpPort(this.intTestMailServerPort);
            this.email.setFrom(this.strTestMailFrom);
            this.email.addTo(this.strTestMailTo);

            this.email.setAuthentication(this.strTestUser, this.strTestPasswd);

            this.email.setCharset(Email.ISO_8859_1);
            this.email.setSubject(strSubject);

            URL url = new URL(EmailConfiguration.TEST_URL);
            String cid = this.email.embed(url, "Apache Logo");

            String strHtmlMsg =
                "<html>The Apache logo - <img src=\"cid:" + cid + "\"><html>";

            this.email.setHtmlMsg(strHtmlMsg);
            this.email.setTextMsg(
                "Your email client does not support HTML emails");

            this.email.send();
            this.fakeMailServer.stop();

            // validate txt message
            validateSend(
                this.fakeMailServer,
                strSubject,
                this.email.getTextMsg(),
                this.email.getFromAddress(),
                this.email.getToList(),
                this.email.getCcList(),
                this.email.getBccList(),
                true);

            // validate html message
            validateSend(
                this.fakeMailServer,
                strSubject,
                this.email.getHtmlMsg(),
                this.email.getFromAddress(),
                this.email.getToList(),
                this.email.getCcList(),
                this.email.getBccList(),
                false);
        }

        catch (MalformedURLException e)
        {
            e.printStackTrace();
            fail("Unexpected exception thrown");
        }
        catch (IOException e)
        {
            e.printStackTrace();
            fail("Failed to save email to output file");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail("Unexpected exception thrown");
        }

    }

	/** */
	public void testSendWAttachments()
	{
		EmailAttachment attachment = new EmailAttachment();
		File testFile = null;

		try
		{
			/** File to used to test file attachmetns (Must be valid) */
			testFile = File.createTempFile("commons-email-testfile", ".txt");
		}
		catch (IOException e)
		{
			e.printStackTrace();
			fail("Test file cannot be found");
		}

		// ====================================================================
		// Test Success
		// ====================================================================
		try
		{
			this.getMailServer();

			String strSubject = "Test HTML Send #1 Subject (w charset)";

			this.email = new MockHtmlEmailConcrete();
			this.email.setHostName(this.strTestMailServer);
			this.email.setSmtpPort(this.intTestMailServerPort);
			this.email.setFrom(this.strTestMailFrom);
			this.email.addTo(this.strTestMailTo);

			/** File to used to test file attachmetns (Must be valid) */
			attachment.setName("Test Attachment");
			attachment.setDescription("Test Attachment Desc");
			attachment.setPath(testFile.getAbsolutePath());
			this.email.attach(attachment);

			this.email.setAuthentication(this.strTestUser, this.strTestPasswd);

			this.email.setCharset(Email.ISO_8859_1);
			this.email.setSubject(strSubject);

			URL url = new URL(EmailConfiguration.TEST_URL);
			String cid = this.email.embed(url, "Apache Logo");

			String strHtmlMsg =
				"<html>The Apache logo - <img src=\"cid:" + cid + "\"><html>";

			this.email.setHtmlMsg(strHtmlMsg);
			this.email.setTextMsg(
				"Your email client does not support HTML emails");

			this.email.send();
			this.fakeMailServer.stop();
			// validate txt message
			validateSend(
				this.fakeMailServer,
				strSubject,
				this.email.getTextMsg(),
				this.email.getFromAddress(),
				this.email.getToList(),
				this.email.getCcList(),
				this.email.getBccList(),
				true);

			// validate html message
			validateSend(
				this.fakeMailServer,
				strSubject,
				this.email.getHtmlMsg(),
				this.email.getFromAddress(),
				this.email.getToList(),
				this.email.getCcList(),
				this.email.getBccList(),
				false);

			// validate attachment
			validateSend(
				this.fakeMailServer,
				strSubject,
				attachment.getName(),
				this.email.getFromAddress(),
				this.email.getToList(),
				this.email.getCcList(),
				this.email.getBccList(),
				false);
		}

		catch (MalformedURLException e)
		{
			e.printStackTrace();
			fail("Unexpected exception thrown");
		}
		catch (IOException e)
		{
			e.printStackTrace();
			fail("Failed to save email to output file");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail("Unexpected exception thrown");
		}
    }

}