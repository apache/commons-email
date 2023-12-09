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
 * <p> This class is created to replace the usage of {@code org.apache.commons.mail.ByteArrayDataSource} and {@code javax.mail.util.ByteArrayDataSource},
 * as both implementations load attachment binary in eager manner.
 *
 * <p> In order to cater the scenario that user only access the metadata (Name, Type) but not interested in the actual attachment binary,
 * in this scenario, the memory usage can be further reduced as attachment binary only loaded when .getInputStream() called.
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
     * Constructs a new instance to read all necessary information for an email attachment.
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
     * Gets an {@code ByteArrayDataSource} instance, to represent the email attachment.
     *
     * @return An ByteArrayDataSource instance which contain the email attachment.
     * @throws IOException resolving the email attachment failed
     */
    @Override
    public synchronized InputStream getInputStream() throws IOException {
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
