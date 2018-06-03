package net.bplaced.clayn.yagol.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import net.bplaced.clayn.yagol.Field;

/**
 *
 * @author Clayn <clayn_osmato@gmx.de>
 * @since 0.1
 */
public class FieldOutputStream implements AutoCloseable
{

    private final BufferedWriter writer;

    public FieldOutputStream(OutputStream out)
    {
        writer = new BufferedWriter(new OutputStreamWriter(out));
    }

    public FieldOutputStream(File f) throws FileNotFoundException
    {
        this(new FileOutputStream(f));
    }

    public void writeField(Field f) throws IOException
    {
        for (int y = 0; y < f.getSize(); ++y)
        {
            StringBuilder builder = new StringBuilder(f.getSize());
            for (int x = 0; x < f.getSize(); x++)
            {
                builder.append(f.isAlive(x, y) ? '1' : '0');
            }
            writer.write(builder.toString());
            writer.newLine();
        }
    }

    @Override
    public void close() throws IOException
    {
        writer.flush();
        writer.close();
    }

}
