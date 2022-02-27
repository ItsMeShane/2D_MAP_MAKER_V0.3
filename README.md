# Map_Maker_V0.3
A 2D map editor designed for creating a number map which my game (next project) ill 
convert into a visual map. Contains over 1900 unique 32x32 tiles.

Version 0.3 comes with the implementation of a "Fill" action. This is very simular 
to a "Paint Bucket" in photoshop and works exactly as expected. You click a tile 
and all tiles around clicked tile with the same tile-ID convert to your selected tile.
This "Fill" is done by using the Flood Fill Algorithm (credit to the wikipedia page 
that incudes the pseudocode https://en.wikipedia.org/wiki/Flood_fill) which is a simple 
but inefficient filling algorithm. The flaw is that is that is often will search tiles
that have already been filled. This is something that can be fixed, but as you wont be 
filling anymore than 10000 tiles at a time it probably wont be necessary.

Version 0.3 also contains many bug fixes, reworks and QOL improvements such as the following:

-Drawing a rectangle no longer searches through every tile and checks if tile is contained 
 within selection. Now we only search tiles between pointA (mouse press point) and pointB 
 (mouse current point).

-Fixed the "Undo" bug where it would get disabled after switching layers. This was due to the
 map losing focus after a button press.

-Reworked zooming in and out. Before, the zooming effect was done by changing the scale of the
 tiles. This was flawed because zooming would drag to the left of the screen, and not relative to 
 the mouse position.
 
-Changed how the properties grid and layer numbers are shown. Instead of drawing an idividual 
 square around each tile, we now just draw straight lines vertically and horizontally across 
 the map. 
 
-Changed how the checkered background is drawn. We now only draw the background tile if there 
 are no tiles to draw draw on top of it (cause you cant see it if theres a tile ontop of it).
 
There are a few more changes that were made but these were the major ones. Please let me know 
of suggestions, bugs, or problems you have.
