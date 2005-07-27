/*
 * Copyright 2001-2004 The Apache Software Foundation
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

import org.apache.commons.lang.exception.NestableException;

/**
 * EmailException
 * @author jakarta-commons
 * @since 1.0
 */
public class EmailException extends NestableException
{
    /** serialization version */
    static final long serialVersionUID = 5550674499282474616L;

    /**
     * Create a new EmailException with no message and no cause.
     * Note: This constructor should only be used as a last resort. Please 
     * provide at least a message.
     * @since 1.0
     */
    public EmailException()
    {
        super();
    }

    /**
     * Create a new EmailException with a message but no other cause.
     * @param msg the reason for this exception.
     * @since 1.0
     */
    public EmailException(String msg)
    {
        super(msg);
    }

    /**
     * Create a new EmailException with a message and a cause.
     * @param msg the reason for this exception.
     * @param cause the contributing Throwable (e.g. some other Exception)
     * @since 1.0
     */
    public EmailException(String msg, Throwable cause)
    {
        super(msg, cause);
    }

    /**
     * Create a new EmailException with a cause but no message.
     * @param cause the contributing Throwable (e.g. some other Exception)
     * @since 1.0
     */
    public EmailException(Throwable cause)
    {
        super(cause);
    }

}
