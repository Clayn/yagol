package net.bplaced.clayn.yagol;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import net.bplaced.clayn.yagol.util.CellConsumer;
import net.bplaced.clayn.yagol.util.HDirection;
import net.bplaced.clayn.yagol.util.VDirection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Clayn <clayn_osmato@gmx.de>
 * @since 0.1
 */
public class Field
{

    private static final Logger LOG = LoggerFactory.getLogger(Field.class);
    private final int size;
    private final Cell[][] cells;
    private final ExecutorService service;
    private final Map<Integer, boolean[][]> generationCache = new HashMap<>();
    private int generation = 0;
    private final AtomicInteger aliveCounters[] = new AtomicInteger[4];

    public boolean caching = false;

    public Field(int size)
    {

        if (size <= 5)
        {
            throw new IllegalArgumentException();
        }
        this.size = size;
        cells = new Cell[size][size];
        initField();
        service = Executors.newFixedThreadPool(4);
        for (int i = 0; i < aliveCounters.length; ++i)
        {
            aliveCounters[i] = new AtomicInteger(0);
        }
    }

    public Field()
    {
        this(20);
    }

    public void stop()
    {
        service.shutdown();
    }

    private void initField()
    {
        for (int x = 0; x < size; x++)
        {
            for (int y = 0; y < size; y++)
            {
                cells[x][y] = new Cell();
            }
        }
        for (int x = 0; x < size; x++)
        {
            for (int y = 0; y < size; y++)
            {
                Cell c = cells[x][y];
                for (int i = -1; i <= 1; ++i)
                {
                    for (int j = -1; j <= 1; j++)
                    {
                        if (i == 0 && j == 0)
                        {
                            continue;
                        }
                        HDirection hDir = HDirection.getForDiff(i);
                        VDirection vDir = VDirection.getForDiff(j);
                        try
                        {
                            c.setNeighbour(hDir, vDir, cells[x + i][y + j]);
                        } catch (ArrayIndexOutOfBoundsException e)
                        {
                            LOG.warn(
                                    "Catched exception while setting neighbours. Should happen for some cells",
                                    e);
                        }
                    }
                }
            }
        }
    }

    public int getGeneration()
    {
        return generation;
    }

    public void setCaching(boolean caching)
    {
        this.caching = caching;
    }

    public boolean isCaching()
    {
        return caching;
    }

    public int getSize()
    {
        return size;
    }

    public void setCell(int x, int y, boolean alive)
    {
        cells[x][y].setAlive(alive);
    }

    public void print(PrintStream ps)
    {
        doAction(new CellConsumer()
        {
            @Override
            public void accept(int x, int y, Cell c)
            {
                ps.print(c.isAlive() ? "x" : "o");
                if (y == size - 1)
                {
                    ps.println();
                }
            }
        });
    }

    private void doAction(CellConsumer con)
    {
        for (int x = 0; x < size; x++)
        {
            for (int y = 0; y < size; y++)
            {
                con.accept(x, y, cells[x][y]);
            }
        }
    }

    public void tick(Runnable onFinish) throws InterruptedException, ExecutionException
    {
        int xHalf = size / 2;
        int yHalf = xHalf;
        List<Callable<Void>> calculators = new ArrayList<>();
        List<Callable<Void>> tickers = new ArrayList<>();
        boolean currentGen[][] = caching
                ? new boolean[size][size] : null;
        for (int i = 0; i < aliveCounters.length; ++i)
        {
            aliveCounters[i].set(0);
        }
        createCalculators(calculators, xHalf, yHalf);
        createTickers(tickers, xHalf, yHalf, currentGen);
        for (Future<Void> task : service.invokeAll(calculators))
        {
            task.get();
        }
        for (Future<Void> task : service.invokeAll(tickers))
        {
            task.get();
        }
        if (caching)
        {
            generationCache.put(generation, currentGen);
        }
        generation++;
        Optional.ofNullable(onFinish).ifPresent(Runnable::run);
    }

    public void tick() throws InterruptedException, ExecutionException
    {
        tick(null);
    }

    public boolean isAlive(int x, int y)
    {
        return cells[x][y].isAlive();
    }

    public int getAliveCells()
    {
        return Arrays.stream(aliveCounters).mapToInt(AtomicInteger::get)
                .sum();
    }

    public int getDeadCells()
    {
        return size * size - getAliveCells();
    }

    private void createTickers(List<Callable<Void>> tickers, int xHalf,
            int yHalf, boolean[][] currentGen)
    {
        tickers.add(new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                for (int x = 0; x < xHalf; ++x)
                {
                    for (int y = 0; y < yHalf; y++)
                    {
                        cells[x][y].tick();
                        if (cells[x][y].isAlive())
                        {
                            aliveCounters[0].incrementAndGet();
                        }
                        if (currentGen != null)
                        {
                            currentGen[x][y] = cells[x][y].isAlive();
                        }
                    }
                }
                return null;
            }
        });
        tickers.add(new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                for (int x = xHalf; x < size; ++x)
                {
                    for (int y = 0; y < yHalf; y++)
                    {
                        cells[x][y].tick();
                        if (cells[x][y].isAlive())
                        {
                            aliveCounters[1].incrementAndGet();
                        }
                        if (currentGen != null)
                        {
                            currentGen[x][y] = cells[x][y].isAlive();
                        }
                    }
                }
                return null;
            }
        });
        tickers.add(new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                for (int x = 0; x < xHalf; ++x)
                {
                    for (int y = yHalf; y < size; y++)
                    {
                        cells[x][y].tick();
                        if (cells[x][y].isAlive())
                        {
                            aliveCounters[2].incrementAndGet();
                        }
                        if (currentGen != null)
                        {
                            currentGen[x][y] = cells[x][y].isAlive();
                        }
                    }
                }
                return null;
            }
        });
        tickers.add(new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                for (int x = xHalf; x < size; ++x)
                {
                    for (int y = yHalf; y < size; y++)
                    {
                        cells[x][y].tick();
                        if (cells[x][y].isAlive())
                        {
                            aliveCounters[3].incrementAndGet();
                        }
                        if (currentGen != null)
                        {
                            currentGen[x][y] = cells[x][y].isAlive();
                        }
                    }
                }
                return null;
            }
        });
    }

    private void createCalculators(List<Callable<Void>> calculators, int xHalf,
            int yHalf)
    {
        calculators.add(new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                for (int x = 0; x < xHalf; ++x)
                {
                    for (int y = 0; y < yHalf; y++)
                    {
                        cells[x][y].calc();

                    }
                }
                return null;
            }
        });
        calculators.add(new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                for (int x = xHalf; x < size; ++x)
                {
                    for (int y = 0; y < yHalf; y++)
                    {
                        cells[x][y].calc();
                    }
                }
                return null;
            }
        });
        calculators.add(new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                for (int x = 0; x < xHalf; ++x)
                {
                    for (int y = yHalf; y < size; y++)
                    {
                        cells[x][y].calc();
                    }
                }
                return null;
            }
        });
        calculators.add(new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                for (int x = xHalf; x < size; ++x)
                {
                    for (int y = yHalf; y < size; y++)
                    {
                        cells[x][y].calc();
                    }
                }
                return null;
            }
        });
    }
}
