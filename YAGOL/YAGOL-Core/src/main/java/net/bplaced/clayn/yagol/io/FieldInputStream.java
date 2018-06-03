package net.bplaced.clayn.yagol.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import net.bplaced.clayn.yagol.Field;

/**
 *
 * @author Clayn <clayn_osmato@gmx.de>
 * @since 0.1
 */
public class FieldInputStream implements AutoCloseable
{

    private final BufferedReader reader;

    public FieldInputStream(InputStream in)
    {
        reader = new BufferedReader(new InputStreamReader(in));
    }

    public FieldInputStream(File f) throws FileNotFoundException
    {
        this(new FileInputStream(f));
    }

    public Field readField() throws IOException
    {
        String line = null;
        Field f = null;
        int y = 0;
        while ((line = reader.readLine()) != null)
        {
            if (f == null)
            {
                f = new Field(line.length());
            }
            if (line.length() != f.getSize())
            {
                throw new RuntimeException("Illegal line length");
            }
            for (int i = 0; i < f.getSize(); ++i)
            {
                f.setCell(i, y, line.charAt(i) == '1');
            }
            y++;
        }
        return f;

    }

    @Override
    public void close() throws IOException
    {
        reader.close();
    }
}
