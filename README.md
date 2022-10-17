# nonogram

Simple Nonogram Solver

1. Type in row and column count separated by space
2. Type in row numbers from left to right and from bottom to top
3. Type in column numbers from bottom to top and left to right

When row and column count both are less than 10, no spaces are needed between the numbers.

                2
        2 2 4 2 1
    1 1 
      3 
      2 
    3 1 
      2 
    
Input:

    5 5 [Enter] 2 [Enter] 31 [Enter] 2 [Enter] 3 [Enter] 11 [Enter] 2 [Enter] 2 [Enter] 4 [Enter] 2 [Enter] 12 [Enter]

Output:

    ┌───┬───┬───┬───┬───┐
    │   │   │ █ │   │ █ │
    ├───┼───┼───┼───┼───┤
    │   │   │ █ │ █ │ █ │
    ├───┼───┼───┼───┼───┤
    │   │   │ █ │ █ │   │
    ├───┼───┼───┼───┼───┤
    │ █ │ █ │ █ │   │ █ │
    ├───┼───┼───┼───┼───┤
    │ █ │ █ │   │   │   │
    └───┴───┴───┴───┴───┘
