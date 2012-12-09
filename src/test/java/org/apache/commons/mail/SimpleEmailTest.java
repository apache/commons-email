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

import java.io.IOException;

import org.apache.commons.mail.mocks.MockSimpleEmail;

/**
 * JUnit test case for SimpleEmailTest
 * @since 1.0
 * @version $Revision$ $Date$
 */
public class SimpleEmailTest extends BaseEmailTestCase
{
    /** */
    private MockSimpleEmail email;

    /**
     * @param name name
     */
    public SimpleEmailTest(String name)
    {
        super(name);
    }

    /**
     * @throws Exception  */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        // reusable objects to be used across multiple tests
        this.email = new MockSimpleEmail();
    }

    /**
     * @throws EmailException  */
    public void testGetSetMsg() throws EmailException
    {
        // ====================================================================
        // Test Success
        // ====================================================================
        for (int i = 0; i < testCharsValid.length; i++)
        {
            this.email.setMsg(testCharsValid[i]);
            assertEquals(testCharsValid[i], this.email.getMsg());
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
            catch (EmailException e)
            {
                assertTrue(true);
            }
        }

    }

    /**
     * @throws EmailException when a bad address is set.
     * @throws IOException when sending fails
     * @todo Add code to test the popBeforeSmtp() settings
     */
    public void testSend() throws EmailException, IOException
    {
        // ====================================================================
        // Test Success
        // ====================================================================
        this.getMailServer();

        this.email = new MockSimpleEmail();
        this.email.setHostName(this.strTestMailServer);
        this.email.setSmtpPort(this.getMailServerPort());
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
            this.email.getToAddresses(),
            this.email.getCcAddresses(),
            this.email.getBccAddresses(),
            true);
    }
}
