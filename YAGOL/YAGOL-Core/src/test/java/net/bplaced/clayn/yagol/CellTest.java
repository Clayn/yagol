/*
 * The MIT License
 *
 * Copyright 2018 Clayn <clayn_osmato@gmx.de>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.bplaced.clayn.yagol;

import net.bplaced.clayn.yagol.util.HDirection;
import net.bplaced.clayn.yagol.util.VDirection;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Clayn <clayn_osmato@gmx.de>
 */
public class CellTest
{

    private Cell cell;

    public CellTest()
    {
    }

    @BeforeClass
    public static void setUpClass()
    {
    }

    @AfterClass
    public static void tearDownClass()
    {
    }

    @Before
    public void setUp()
    {
        cell = new Cell();
    }

    @After
    public void tearDown()
    {
    }

    @Test
    public void testSetNeighbourSame()
    {
        for (int x = -1; x <= 1; x++)
        {
            for (int y = -1; y <= 1; y++)
            {
                if (x == 0 && y == 0)
                {
                    continue;
                }
                try
                {
                    cell.setNeighbour(HDirection.getForDiff(x),
                            VDirection.getForDiff(y), cell);
                    Assert.fail();
                } catch (Exception e)
                {
                }
            }
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetNeighbourThis()
    {
        cell.setNeighbour(HDirection.NONE, VDirection.NONE, new Cell());
    }

    @Test(expected = NullPointerException.class)
    public void testSetNeighbourHDirNull()
    {
        cell.setNeighbour(null, VDirection.NONE, new Cell());
    }

    @Test(expected = NullPointerException.class)
    public void testSetNeighbourVDirNull()
    {
        cell.setNeighbour(HDirection.NONE, null, new Cell());
    }

    @Test(expected = NullPointerException.class)
    public void testSetNeighbourCellNull()
    {
        cell.setNeighbour(HDirection.NONE, VDirection.NONE, null);
    }

    @Test
    public void testCalcNoChangeNoNeighbours()
    {
        Assert.assertFalse(cell.isAlive());
        cell.calc();
        Assert.assertFalse(cell.isAlive());
    }

    @Test
    public void testCalcNoChange()
    {
        Assert.assertFalse(cell.isAlive());
        for (int x = -1; x <= 1; x++)
        {
            for (int y = -1; y <= 1; y++)
            {
                if (x == 0 && y == 0)
                {
                    continue;
                }
                cell.setNeighbour(HDirection.getForDiff(x),
                        VDirection.getForDiff(y), new Cell());
            }
        }
        cell.calc();
        Assert.assertFalse(cell.isAlive());
    }

    @Test
    public void testCalcNoChangeBeforeTick()
    {
        Assert.assertFalse(cell.isAlive());
        for (int x = -1; x <= 1; x++)
        {
            for (int y = -1; y <= 1; y++)
            {
                if (x == 0 && y == 0)
                {
                    continue;
                }
                cell.setNeighbour(HDirection.getForDiff(x),
                        VDirection.getForDiff(y), new Cell());
            }
        }
        Cell c = new Cell();
        c.setAlive(true);
        cell.setNeighbour(HDirection.LEFT, VDirection.NONE, c);
        c = new Cell();
        c.setAlive(true);
        cell.setNeighbour(HDirection.RIGHT, VDirection.NONE, c);
        cell.calc();
        Assert.assertFalse(cell.isAlive());
    }
    
    @Test
    public void testCalcChangeAfterTick()
    {
        Assert.assertFalse(cell.isAlive());
        for (int x = -1; x <= 1; x++)
        {
            for (int y = -1; y <= 1; y++)
            {
                if (x == 0 && y == 0)
                {
                    continue;
                }
                cell.setNeighbour(HDirection.getForDiff(x),
                        VDirection.getForDiff(y), new Cell());
            }
        }
        Cell c = new Cell();
        c.setAlive(true);
        cell.setNeighbour(HDirection.LEFT, VDirection.NONE, c);
        c = new Cell();
        c.setAlive(true);
        cell.setNeighbour(HDirection.RIGHT, VDirection.NONE, c);
        c = new Cell();
        c.setAlive(true);
        cell.setNeighbour(HDirection.RIGHT, VDirection.UP, c);
        cell.calc();
        cell.tick();
        Assert.assertTrue(cell.isAlive());
    }
     @Test
    public void testNoChangeAfterTickWithoutCalc()
    {
        Assert.assertFalse(cell.isAlive());
        for (int x = -1; x <= 1; x++)
        {
            for (int y = -1; y <= 1; y++)
            {
                if (x == 0 && y == 0)
                {
                    continue;
                }
                cell.setNeighbour(HDirection.getForDiff(x),
                        VDirection.getForDiff(y), new Cell());
            }
        }
        Cell c = new Cell();
        c.setAlive(true);
        cell.setNeighbour(HDirection.LEFT, VDirection.NONE, c);
        c = new Cell();
        c.setAlive(true);
        cell.setNeighbour(HDirection.RIGHT, VDirection.NONE, c);
        cell.tick();
        Assert.assertFalse(cell.isAlive());
    }
}
