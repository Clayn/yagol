# Yet Another Game Of Life [![Build Status](https://travis-ci.org/Clayn/yagol.svg?branch=development)](https://travis-ci.org/Clayn/yagol)
Yagol is another implementation of [Conway's Game Of Life](https://en.wikipedia.org/wiki/Conway%27s_Game_of_Life) written in Java. 
The project is separated into two Maven Modules:

 - Yagol-Core
 - Yagol-FX
 
The core project is used to provide the functionality and basic classes so that you could create your own UI or use it for other purposes. 
The FX project is a sampel UI to display the Game of Life using JavaFX.

## Usage
Yagol is really easy to use. So lets see what we need to create a new Game of Life 
and use it:

    Field field=new Field();
Thats it. We now have a field with the default size of 20. (Note that this will produce some warnings/exceptions. These are expected and will be "fixed" in further versions). But wait? What about the cells? Are they dead? Sadly, yes.
So lets make some of them alive!

    field.setCell(x,y,true);
Now the cell at the position (x,y) is alive. Of course we need some more alive cells but you know how to make this happen. 

Finally, when we have created our field and made some cells live we want to let them do their daily work.

    field.tick();

Now one generation passed. 

    field.isAlive(x,y);
Use this to see if a cell is alive or not. To count the alive and dead cells use `field.getDeadCells();`
`field.getAliveCells();`

Thats it. Now you know the basic usage of Yagol and are ready to explore the world of [Conway's Game Of Life](https://en.wikipedia.org/wiki/Conway%27s_Game_of_Life)!

<!--stackedit_data:
eyJoaXN0b3J5IjpbLTE5NTg0NjkzNzFdfQ==
-->