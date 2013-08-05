package org.apache.commons.mail.resolver;

import java.io.IOException;
import java.io.InputStream;

import javax.activation.DataSource;

import org.apache.commons.io.IOUtils;

public abstract class AbstractDataSourceResolverTest {

    protected final int IMG_SIZE = 5866;

    protected byte[] toByteArray(DataSource dataSource) throws IOException
    {
        if(dataSource != null)
        {
            InputStream is = dataSource.getInputStream();
            return IOUtils.toByteArray(is);
        }
        else
        {
            return null;
        }
    }

}
