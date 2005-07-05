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
package org.apache.commons.mail.settings;

/**
 * @since 1.0
 * @author Eric Pugh 
 * @version $Revision$ $Date$
 */
public final class EmailConfiguration
{
    /** */
    public static final String MAIL_SERVER = "localhost";
    /** */
    public static final int MAIL_SERVER_PORT = 2500;
    /** */
    public static final String TEST_FROM = "test_from@apache.org";
    /** */
    public static final String TEST_TO = "test_to@apache.org";
    /** */
    public static final String TEST_USER = "user";
    /** */
    public static final String TEST_PASSWD = "passwd";
    /** */
    public static final String TEST_URL =
        "http://www.apache.org/images/asf_logo_wide.gif";
    /** Amount of time to wait for Dumbster to start up */
    public static final int TIME_OUT = 500;
}
