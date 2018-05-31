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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Clayn <clayn_osmato@gmx.de>
 */
public class NewMain
{

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException
    {
        Logger log = LoggerFactory.getLogger(NewMain.class);
        Field f = new Field();
        f.setCell(5, 5, true);
        f.setCell(5, 7, true);
        f.setCell(5, 6, true);
        f.print(System.out);
        String input = "";
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                System.in));
        long time = 0;
        long diff = 0;
        time = System.nanoTime();
        int generations = 1000000;
        for (int i = 0; i < generations; ++i)
        {
            f.tick();
        }
        diff = System.nanoTime() - time;
        log.debug("{} generations took {} ns; {} ms; {} s to calculate",
                generations, diff, diff / 1000000, diff / 1000000000);
        diff = diff / generations;
        log.debug("{} ns; {} ms; {} s per generation", diff, diff / 1000000,
                diff / 1000000000);
        f.stop();
    }

}
