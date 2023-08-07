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
package org.apache.commons.mail.util;

import javax.activation.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A DataSource create with InputStream.
 * It is possible to set a name and type.
 *
 * @since 1.5
 */
public class AttachmentDataSource implements DataSource {
    private final InputStream is;
    private final String type;
    private String name = "";

    /**
     * Create a AttachmentDataSource with data from the
     * specified InputStream and with the specified MIME type.
     *
     * @param is   the InputStream
     * @param type the MIME type
     */
    public AttachmentDataSource(InputStream is, String type) {
        this.is = is;
        this.type = type;
    }

    /**
     * Create a AttachmentDataSource with data from the
     * specified InputStream and with the specified MIME type
     * and name for this DataSource.
     *
     * @param is
     * @param type
     * @param name
     */
    public AttachmentDataSource(InputStream is, String type, String name) {
        this.is = is;
        this.type = type;
        this.name = name;
    }

    /**
     * Return an InputStream.
     * @return the InputStream
     */
    public InputStream getInputStream() {
        return is;
    }

    /**
     * Return an OutputStream for the data.
     * Writing the data is not supported; an <code>IOException</code>
     * is always thrown.
     *
     * @throws IOException
     */
    public OutputStream getOutputStream() throws IOException {
        throw new IOException("cannot do this");
    }

    /** Get the content type. */
    public String getContentType() {
        return this.type;
    }

    /** Get the name. */
    public String getName() {
        return this.name;
    }

    /** Sets the name for this DataSource. */
    public void setName(String name) {
        this.name = name;
    }

}
