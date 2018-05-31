package net.bplaced.clayn.yagol.util;

import net.bplaced.clayn.yagol.Cell;

/**
 *
 * @author Clayn <clayn_osmato@gmx.de>
 * @since 0.1
 */
public interface CellConsumer
{

    public void accept(int x, int y, Cell c);
}
