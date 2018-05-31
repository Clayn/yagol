package net.bplaced.clayn.yagol;

import java.util.Objects;
import net.bplaced.clayn.yagol.util.HDirection;
import net.bplaced.clayn.yagol.util.VDirection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Clayn <clayn_osmato@gmx.de>
 * @since 0.1
 */
public class Cell
{

    private static final Logger LOG = LoggerFactory.getLogger(Cell.class);
    private final Cell[][] neighbours = new Cell[3][3];
    private boolean alive = false;
    private boolean nextAlive = false;

    public boolean isAlive()
    {
        return alive;
    }

    public void calc()
    {
        int calcAlive = getAliveNeighbours();
        if (!isAlive())
        {
            nextAlive = calcAlive == 3;
        } else
        {
            nextAlive = calcAlive >= 2 && calcAlive <= 3;
        }
    }

    private int getAliveNeighbours()
    {
        int count = 0;
        for (int x = 0; x < 3; x++)
        {
            for (int y = 0; y < 3; y++)
            {
                Cell c = neighbours[x][y];
                count += c != null && c.isAlive() ? 1 : 0;
            }
        }
        return count;
    }

    public void setAlive(boolean alive)
    {
        this.alive = alive;
    }

    public void tick()
    {
        alive = nextAlive;
    }

    public void setNeighbour(HDirection hDir, VDirection vDir, Cell c)
    {
        Objects.requireNonNull(hDir);
        Objects.requireNonNull(vDir);
        Objects.requireNonNull(c);

        if (c == this || (hDir == HDirection.NONE && vDir == VDirection.NONE))
        {
            throw new IllegalArgumentException();
        }
        int x = -5;
        int y = -5;
        switch (hDir)
        {
            case NONE:
                x = 0;
                break;
            case LEFT:
                x = -1;
                break;
            case RIGHT:
                x = 1;
        }
        switch (vDir)
        {
            case NONE:
                y = 0;
                break;
            case UP:
                y = -1;
                break;
            case DOWN:
                y = 1;
        }
        neighbours[1 + x][1 + y] = c;
    }
}
