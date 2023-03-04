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

import javax.activation.DataSource;
import javax.mail.util.ByteArrayDataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * <p>Wrapper class for ByteArrayDataSource, which contain reference of MimePartDataSource for given attachment.
 * Both type and name are duplicated stored in this class, in order to delay the load of attachment binary till getInputStream() is called.
 * </p>
 *
 * @since 1.5
 */
public class LazyByteArrayDataSource implements DataSource {

    /** InputStream reference for the email attachment binary. */
    private final InputStream referenceInputStream;

    /** ByteArrayDateSource instance which contain email attachment binary in the form of byte array. */
    private ByteArrayDataSource ds;

    /** Name of the attachment. */
    private final String name;

    /** Type of the attachment. */
    private final String type;

    /**
     * Constructor for this class to read all necessary information for an email attachment.
     *
     * @param is the InputStream which represent the attachment binary.
     * @param type the type of the attachment.
     * @param name the name of the attachment.
     */
    public LazyByteArrayDataSource(InputStream is, String type, String name) {
        this.referenceInputStream = is;
        this.type = type;
        this.name = name;
    }

    /**
     * To return an {@code ByteArrayDataSource} instance which represent the email attachment.
     *
     * @return An ByteArrayDataSource instance which contain the email attachment.
     * @throws IOException resolving the email attachment failed
     */
    @Override
    public InputStream getInputStream() throws IOException {
        if (ds == null) {
            //Only read attachment data to memory when getInputStream() is called.
            ds = new ByteArrayDataSource(referenceInputStream, type);
            ds.setName(name);
        }
        return ds.getInputStream();
    }

    /**
     * Not supported.
     *
     * @return  N/A
     */
    @Override
    public OutputStream getOutputStream() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("cannot do this");
    }

    /**
     * Gets the content type.
     *
     * @return A String.
     */
    @Override
    public String getContentType() {
        return type;
    }

    /**
     * Gets the name.
     *
     * @return A String.
     */
    @Override
    public String getName() {
        return name;
    }
}
