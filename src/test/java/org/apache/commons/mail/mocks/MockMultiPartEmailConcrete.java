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
package org.apache.commons.mail.mocks;

import java.io.IOException;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.mail.MultiPartEmail;

/**
 * Extension of MultiPartEmail Class
 * (used to allow testing only)
 *
 * @since 1.0
 */
public class MockMultiPartEmailConcrete extends MultiPartEmail
{

    /**
     * Retrieve the message content
     * @return Message Content
     */
    public String getMsg()
    {
        try
        {
            return this.getPrimaryBodyPart().getContent().toString();
        }
        catch (final IOException ioE)
        {
            return null;
        }
        catch (final MessagingException msgE)
        {
            return null;
        }
    }

    /**
     */
    public void initTest()
    {
        this.init();
    }

    /**
     * @return fromAddress
     */
    @Override
    public InternetAddress getFromAddress()
    {
        return this.fromAddress;
    }

}
