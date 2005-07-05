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
package org.apache.commons.mail.mocks;

import java.util.List;

import javax.mail.internet.InternetAddress;

import org.apache.commons.mail.SimpleEmail;

/**
 * Extension of SimpleEmail Class 
 * (used to allow testing only)
 *
 * @since 1.0
 * @author <a href="mailto:corey.scott@gmail.com">Corey Scott</a>
 * @version $Id$
 */
public class MockSimpleEmail extends SimpleEmail
{
    /**
     * Retrieve the message content
     * @return Message Content
     */
    public String getMsg()
    {
        return (String) this.content;
    }

    /**
     * @return fromAddress
     */
    public InternetAddress getFromAddress()
    {
        return this.fromAddress;
    }

    /**
     * @return toList
     */
    public List getToList()
    {
        return this.toList;
    }

    /**
     * @return bccList
     */
    public List getBccList()
    {
        return this.bccList;
    }

    /**
     * @return ccList
     */
    public List getCcList()
    {
        return this.ccList;
    }

}
