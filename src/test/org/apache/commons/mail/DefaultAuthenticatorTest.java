package org.apache.commons.mail;

import javax.mail.PasswordAuthentication;

import junit.framework.TestCase;

/**
 * JUnit test case for DefaultAuthenticator Class
 *
 * @author <a href="mailto:corey.scott@gmail.com">Corey Scott</a>
 * @version $Id$
 */

public class DefaultAuthenticatorTest extends TestCase
{
    /**
     * @param name name
     */
    public DefaultAuthenticatorTest(String name)
    {
        super(name);
    }

    /** */
    public void testDefaultAuthenticatorConstructor()
    {
        //insert code testing basic functionality
        String strUsername = "user.name";
        String strPassword = "user.pwd";
        DefaultAuthenticator authenicator =
            new DefaultAuthenticator(strUsername, strPassword);

        assertTrue(
            PasswordAuthentication.class.isInstance(
                authenicator.getPasswordAuthentication()));
        assertEquals(
            strUsername,
            authenicator.getPasswordAuthentication().getUserName());
        assertEquals(
            strPassword,
            authenicator.getPasswordAuthentication().getPassword());
    }

}