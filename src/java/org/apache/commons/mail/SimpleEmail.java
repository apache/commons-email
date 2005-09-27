/*
 * Copyright 2001-2005 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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

/**
 * This class is used to send simple internet email messages without
 * attachments.
 *
 * @since 1.0
 * @author <a href="mailto:quintonm@bellsouth.net">Quinton McCombs</a>
 * @author <a href="mailto:colin.chalmers@maxware.nl">Colin Chalmers</a>
 * @author <a href="mailto:jon@latchkey.com">Jon S. Stevens</a>
 * @author <a href="mailto:frank.kim@clearink.com">Frank Y. Kim</a>
 * @author <a href="mailto:bmclaugh@algx.net">Brett McLaughlin</a>
 * @author <a href="mailto:unknown">Regis Koenig</a>
 * @version $Id$
*/
public class SimpleEmail extends Email
{
    /**
     * Set the content of the mail
     *
     * @param msg A String.
     * @return An Email.
     * @throws EmailException see javax.mail.internet.MimeBodyPart
     *  for definitions
     * @since 1.0
     */
    public Email setMsg(String msg) throws EmailException
    {
        if (EmailUtils.isEmpty(msg))
        {
            throw new EmailException("Invalid message supplied");
        }

        setContent(msg, TEXT_PLAIN);
        return this;
    }
}
