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

import javax.mail.PasswordAuthentication;

import junit.framework.TestCase;

/**
 * JUnit test case for DefaultAuthenticator Class
 *
 * @since 1.0
 * @author <a href="mailto:corey.scott@gmail.com">Corey Scott</a>
 * @version $Id$
 */

public class DefaultAuthenticatorTest extends TestCase
{
    /**
     * @param name name
     */
    public DefaultAuthenticatorTest(String name)
    {
        super(name);
    }

    /** */
    public void testDefaultAuthenticatorConstructor()
    {
        //insert code testing basic functionality
        String strUsername = "user.name";
        String strPassword = "user.pwd";
        DefaultAuthenticator authenicator =
            new DefaultAuthenticator(strUsername, strPassword);

        assertTrue(
            PasswordAuthentication.class.isInstance(
                authenicator.getPasswordAuthentication()));
        assertEquals(
            strUsername,
            authenicator.getPasswordAuthentication().getUserName());
        assertEquals(
            strPassword,
            authenicator.getPasswordAuthentication().getPassword());
    }

}
