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

import java.net.URL;

/**
 * This class models an email attachment.  Used by MultiPartEmail.
 *
 * @author <a href="mailto:frank.kim@clearink.com">Frank Y. Kim</a>
 * @version $Id: EmailAttachment.java,v 1.1 2004/11/25 09:56:56 epugh Exp $
 */
public class EmailAttachment
{
    /** Defintion of the part being an attachment */
    public static final String ATTACHMENT = javax.mail.Part.ATTACHMENT;
    /** Defintion of the part being inline */
    public static final String INLINE = javax.mail.Part.INLINE;

    /** The name of this attachment. */
    private String name = "";

    /** The description of this attachment. */
    private String description = "";

    /** The path to this attachment (ie c:/path/to/file.jpg). */
    private String path = "";

    /** The HttpURI where the file can be got. */
    private URL url;

    /** The disposition. */
    private String disposition = EmailAttachment.ATTACHMENT;

    /**
     * Get the description.
     *
     * @return A String.
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Get the name.
     *
     * @return A String.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Get the path.
     *
     * @return A String.
     */
    public String getPath()
    {
        return path;
    }

    /**
     * Get the URL.
     *
     * @return A URL.
     */
    public URL getURL()
    {
        return url;
    }

    /**
     * Get the disposition.
     *
     * @return A String.
     */
    public String getDisposition()
    {
        return disposition;
    }

    /**
     * Set the description.
     *
     * @param desc A String.
     */
    public void setDescription(String desc)
    {
        this.description = desc;
    }

    /**
     * Set the name.
     *
     * @param aName A String.
     */
    public void setName(String aName)
    {
        this.name = aName;
    }

    /**
     * Set the path to the attachment.  The path can be absolute or relative
     * and should include the filename.
     * <p>
     * Example: /home/user/images/image.jpg<br>
     * Example: images/image.jpg
     *
     * @param aPath A String.
     */
    public void setPath(String aPath)
    {
        this.path = aPath;
    }

    /**
     * Set the URL.
     *
     * @param aUrl A URL.
     */
    public void setURL(URL aUrl)
    {
        this.url = aUrl;
    }

    /**
     * Set the disposition.
     *
     * @param aDisposition A String.
     */
    public void setDisposition(String aDisposition)
    {
        this.disposition = aDisposition;
    }
}
