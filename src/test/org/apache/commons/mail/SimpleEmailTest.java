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

import java.io.IOException;

import javax.mail.MessagingException;

import org.apache.commons.mail.mocks.MockSimpleEmail;

/**
 * JUnit test case for SimpleEmailTest
 * @version $Revision: 1.1 $ $Date: 2004/11/25 11:14:53 $
 */

public class SimpleEmailTest extends BaseEmailTestCase
{
    /** */
    private MockSimpleEmail email = null;

    /**
     * @param name name
     */
    public SimpleEmailTest(String name)
    {
        super(name);
    }

    /** */
    protected void setUp()
    {
        super.setUp();
        // reusable objects to be used across multiple tests
        this.email = new MockSimpleEmail();
    }

    /** */
    public void testGetSetMsg()
    {
        // ====================================================================
        // Test Success
        // ====================================================================
        try
        {
            for (int i = 0; i < testCharsValid.length; i++)
            {
                this.email.setMsg(testCharsValid[i]);
                assertEquals(testCharsValid[i], this.email.getMsg());
            }
        }
        catch (MessagingException e)
        {
            e.printStackTrace();
            fail("Unexpected exception thrown");
        }

        // ====================================================================
        // Test Exception
        // ====================================================================
        for (int i = 0; i < this.testCharsNotValid.length; i++)
        {
            try
            {
                this.email.setMsg(this.testCharsNotValid[i]);
                fail("Should have thrown an exception");
            }
            catch (MessagingException e)
            {
                assertTrue(true);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                fail("Unexpected exception thrown");
            }
        }

    }

    /**
     * @todo Add code to test the popBeforeSmtp() settings
     */
    public void testSend()
    {
        // ====================================================================
        // Test Success
        // ====================================================================
        try
        {
            this.getMailServer();

            this.email = new MockSimpleEmail();
            this.email.setHostName(this.strTestMailServer);
            this.email.setFrom(this.strTestMailFrom);
            this.email.addTo(this.strTestMailTo);

            if (this.strTestUser != null && this.strTestPasswd != null)
            {
                this.email.setAuthentication(
                    this.strTestUser,
                    this.strTestPasswd);
            }

            String strSubject = "Test Msg Subject";
            String strMessage = "Test Msg Body";

            this.email.setCharset(Email.ISO_8859_1);
            this.email.setSubject(strSubject);

            this.email.setMsg(strMessage);

            this.email.send();

            this.fakeMailServer.stop();
            validateSend(
                this.fakeMailServer,
                strSubject,
                this.email.getMsg(),
                this.email.getFromAddress(),
                this.email.getToList(),
                this.email.getCcList(),
                this.email.getBccList(),
                true);
        }
        catch (MessagingException e)
        {
            e.printStackTrace();
            fail("Unexpected exception thrown");
        }
        catch (IOException e)
        {
            e.printStackTrace();
            fail("failed to save email to output file");
        }
    }
}