# Sudoku Solver

Java implementation of a bot that efficiently solves Sudoku puzzles.

The algorithm implements the backtracking search algorithm with  forward checking and three heuristics:
Minimum- remaining-values heuristic, degree heuristic, and least- constraining-value heuristic.
More information can be found in the attached pdf.

The bot models Sudoku Puzzle as a Constraint Satisfaction Problem (CSP) as follows:

Variables: 81 variables, one for each square / cell

Domains: Empty squares have domain {1, 2, 3, 4, 5, 6, 7, 8, 9} and the prefilled squares have a domain consisting of a single value.

Constraints: There are 27 different Alldiff constraints: one for each row, column and 3x3 box. For example:

Alldiff( (0,0), (0,1), (0,2) ... (0,8) )

Alldiff( (1,0), (1,1), (1,2) ... (1,8) )

...

Alldiff( (0,0), (1,0), (2,0) ... (8,0) )

Alldiff( (0,1), (1,1), (2,1) ... (8,1) )

...

Alldiff( (0,0), (0,1), (0,2), (1,0), (1,1), (1, 2) , (2,0), (2,1), (2, 2)) Alldiff( (3,0), (3,1), (3,2), (4,0), (4,1), (4, 2) , (5,0), (5,1), (5, 2)) ...
