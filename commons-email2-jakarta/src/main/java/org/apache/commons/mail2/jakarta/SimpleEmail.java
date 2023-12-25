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
package org.apache.commons.mail2.jakarta;

import org.apache.commons.mail2.core.EmailConstants;
import org.apache.commons.mail2.core.EmailException;

/**
 * Sends simple Internet email messages without attachments.
 *
 * @since 1.0
 */
public class SimpleEmail extends Email {

    /**
     * Constructs a new instance.
     */
    public SimpleEmail() {
        // empty
    }

    /**
     * Sets the content of the mail.
     *
     * @param msg A String.
     * @return An Email.
     * @throws EmailException see jakarta.mail.internet.MimeBodyPart for definitions
     * @since 1.0
     */
    @Override
    public Email setMsg(final String msg) throws EmailException {
        EmailException.checkNonEmpty(msg, () -> "Invalid message.");
        setContent(msg, EmailConstants.TEXT_PLAIN);
        return this;
    }
}
