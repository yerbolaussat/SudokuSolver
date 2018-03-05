import java.lang.Math;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Collections;


public class SudokuSolver {

    // all cellID -> value assignments to the moment
    public static HashMap<Integer, Integer> assignments;

    // list of all unassigned variables (cells)
    public static LinkedList<Integer> unassignedVar;

    // counting nodes
    public static int nodesExpanded;

    // starting time
    public static long startTime;

    // read initial assignments from the Sudoku board
    public static void readInitialBoard(int[][] sudokuBoard) {
        assignments = new HashMap<Integer, Integer>();
        unassignedVar = new LinkedList<Integer>();
        int cellID = 1;
        for (int i = 0; i < sudokuBoard.length; i++) {
            for (int j = 0; j < sudokuBoard[0].length; j++) {
                int value = sudokuBoard[i][j];
                if (value != 0) {
                    assignments.put(cellID, value);
                } else {
                    unassignedVar.add(cellID);
                }
                cellID++;
            }
        }
    }

    // conversion from cell ID to i,j position in the matrix
    public static ArrayList<Integer> cellIDtoIJ(int var) {
        ArrayList<Integer> coeff = new ArrayList<Integer>();
        int i, j;
        if (var % 9 == 0) {
            i = (var / 9);
            j = 9;
        } else {
            i = (var / 9) + 1;
            j = var % 9;
        }
        coeff.add(i);
        coeff.add(j);
        return coeff;
    }

    // conversion from i,j position in the matrix to cell ID
    public static int ijToCellID(int i, int j) {
        return (i - 1) * 9 + j;
    }

    // return true if a value is consistent with assignment given the constraints
    public static boolean isConsistent(int var, int value, HashMap<Integer, Integer> assignments) {
        int i = cellIDtoIJ(var).get(0);
        int j = cellIDtoIJ(var).get(1);
        boolean result = rowConsistent(i, j, value, assignments) && columnConsistent(i, j, value, assignments) && regionConsistent(i, j, value, assignments);
        return result;
    }


    // consistent with respect to rows
    public static boolean rowConsistent(int i, int j, int value, HashMap<Integer, Integer> assignments) {
        for (int j_bar = 1; j_bar <= 9; j_bar++) {
            int cellID = ijToCellID(i, j_bar);
            if (assignments.containsKey(cellID) && (assignments.get(cellID) == value)) {
                return false;
            }
        }
        return true;
    }

    // consistent with respect to columns
    public static boolean columnConsistent(int i, int j, int value, HashMap<Integer, Integer> assignments) {
        for (int i_bar = 1; i_bar <= 9; i_bar++) {
            int cellID = ijToCellID(i_bar, j);
            if (assignments.containsKey(cellID) && (assignments.get(cellID) == value)) {
                return false;
            }
        }
        return true;    
    }

    // consistent with respect to 3x3 regions
    public static boolean regionConsistent(int i, int j, int value, HashMap<Integer, Integer> assignments) {
        
        int iRegion = (int) Math.ceil((double)i/3);
        int jRegion = (int) Math.ceil((double)j/3);

        for (int i_bar = (3 * iRegion - 2); i_bar <= (3 * iRegion); i_bar++) {
            for (int j_bar = (3 * jRegion - 2); j_bar <= (3 * jRegion); j_bar++) {
                int cellID = ijToCellID(i_bar, j_bar);
                if (assignments.containsKey(cellID) && (assignments.get(cellID) == value)) {
                    return false;
                }
            }
        }        
        return true;  
    }

    // shaffle unassigned variables
    public static void shaffleUnassignedVar() {
        Collections.shuffle(unassignedVar);
    }

    // printing solved sudoku board
    public static void print_grid() {
        System.out.println("Solution: ");
        long estimatedTime = System.currentTimeMillis() - startTime;

        int cellID = 1;
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                System.out.print(assignments.get(cellID) + "   ");
                cellID++;
            }
            System.out.println();
        }

        //System.out.println("Size: " + assignments.size());
        System.out.println("Time: " + estimatedTime + "ms.");
        System.out.println("Number of Nodes Expanded: " + nodesExpanded);

    }


    // backtracking search
    public static boolean backTrackingSearch(LinkedList<Integer> unassignedVar, HashMap<Integer, Integer> assignments) {
        
        // terminate if all 81 cells are assigned (board is complete)
        if (assignments.size() == 81) {
            print_grid();
            return true;
        }

        // select an unassigned variable and remove it from the list
        int var = unassignedVar.pop();

        // create domain of values and shuffle it
        ArrayList<Integer> values = new ArrayList<Integer>();
        for (int i = 1; i <=9; i++) {
            values.add(i);
        }
        Collections.shuffle(values);

        
        // iterate through the values domain
        for (int value : values) {

            // checking consistency
            if (isConsistent(var, value, assignments)) {
                assignments.put(var, value);

                nodesExpanded++;


                // recursively keep solving the csp problem with updated assignments and unassignedVar
                if (backTrackingSearch(unassignedVar, assignments)) {
                    return true;
                }
            }
        } 

        // put the variable back to unassigned variables list, and remove it from the assignments map
        unassignedVar.push(var);
        assignments.remove(var);
        return false;
    }

    // checking whether any of the unassigned neighbors have empty domains
    // if any affected set is empty, returns false
    public static boolean forwardCheck(LinkedList<Integer> unassignedVar, HashMap<Integer, Integer> assignments, int var) {
        
        // updates domain
        HashMap<Integer, ArrayList<Integer>> allowedValMatrix = calculateAllowedVal(unassignedVar, assignments);

        // set of neighboring variable that were affected by the assignment
        HashSet<Integer> affectedVarSet = findAffected(unassignedVar, assignments, var);

        for (int affectedVar : affectedVarSet) {

            if (allowedValMatrix.get(affectedVar).size() == 0) {

                return false;
            }
        }
        return true;
    }

    // find all variable "nighboring" to var, that are affected by the assignment of a value to it
    // we are only interested in unassigned variables
    public static HashSet<Integer> findAffected(LinkedList<Integer> unassignedVar, HashMap<Integer,Integer> assignments, int var) {
        int i_pos = cellIDtoIJ(var).get(0);
        int j_pos = cellIDtoIJ(var).get(1);

        HashSet<Integer> affected = new HashSet<Integer>();

        for (int i = 1; i <= 9 ; i++) {
            affected.add(ijToCellID(i, j_pos));
        }

        for (int j = 1; j <= 9 ; j++) {
            affected.add(ijToCellID(i_pos, j));
        }

        int iRegion = (int) Math.ceil((double)i_pos/3);
        int jRegion = (int) Math.ceil((double)j_pos/3);

        for (int i = (3 * iRegion - 2); i <= (3 * iRegion); i++) {
            for (int j = (3 * jRegion - 2); j <= (3 * jRegion); j++) {
                affected.add(ijToCellID(i, j)); 
            }
        }  

        // removing elements that are assigned
        Iterator<Integer> iterator = affected.iterator();
        while (iterator.hasNext()) {
            Integer element = iterator.next();
            if (assignments.containsKey(element)) {
                iterator.remove();
            } 
        }
        return affected;
    }

    // backtracking search with dorward checking
    public static boolean backTrackingWithFC(LinkedList<Integer> unassignedVar, HashMap<Integer, Integer> assignments) {
        
        // terminate if all 81 cells are assigned (board is complete)
        if (assignments.size() == 81) {
            print_grid();
            return true;
        }

        // create domain of values for each cell: cellID -> list of allowed values for that cell
        HashMap<Integer, ArrayList<Integer>> allowedValMatrix = calculateAllowedVal(unassignedVar, assignments);

        // select an unassigned variable and remove it from the list
        int var = unassignedVar.pop();


        // allowed list of values (shuffled) for this specific variable
        ArrayList<Integer> allowedVal = allowedValMatrix.get(var);
        Collections.shuffle(allowedVal);

        // iterate through the values domain
        for (int value : allowedVal) {

            // checking consistency
            if (isConsistent(var, value, assignments)) {
                assignments.put(var, value);

                nodesExpanded++;

                // forward checking to eliminate assignments in advance
                
                if (forwardCheck(unassignedVar, assignments, var)) {     
                    if (backTrackingWithFC(unassignedVar, assignments)) {
                        return true;
                    }
                }
            }
        } 
        // put the variable back to unassigned variables list
        unassignedVar.push(var);
        assignments.remove(var);

        return false;
    } 


    // calculate the matrix of allowed values for each of the cells: cellID -> List of allowed values for this cell
    public static HashMap<Integer, ArrayList<Integer>> calculateAllowedVal(LinkedList<Integer> unassignedVar, HashMap<Integer, Integer> assignments) {
        
        HashMap<Integer, ArrayList<Integer>> allowedValMatrix = new HashMap<Integer, ArrayList<Integer>>();

        for (int var : unassignedVar) {
            ArrayList<Integer> allowedVal = new ArrayList<Integer>();
            
            // iterate through the domain (from 1 to 9)
            for (int i = 1; i <= 9; i++) {
                if (isConsistent(var, i, assignments)) {
                    allowedVal.add(i);
                }
            }
            allowedValMatrix.put(var, allowedVal);
        }
        return allowedValMatrix;
    }


    // return the most constrained variable (minimum remaining values (MRV) heuristic)
    // if a tie - select most constraining variable
    public static int determineMRV(LinkedList<Integer> unassignedVar, HashMap<Integer, Integer> assignments, HashMap<Integer, ArrayList<Integer>> allowedValMatrix) {
        // initialize minRemaininVal as a big number

        int minVar = 0;
        int minRemainingVal = 100;
        for (int var : unassignedVar) {
            int remainingVal = allowedValMatrix.get(var).size();
            if (remainingVal <  minRemainingVal) {
                minRemainingVal = remainingVal;
                minVar = var;
            } else if (remainingVal ==  minRemainingVal) {
                int degreeVar = determineDegree(unassignedVar, assignments, var);
                int degreeMinVar = determineDegree(unassignedVar, assignments, minVar);
                // prefer most constraining variable
                if (degreeVar > degreeMinVar) {
                    minVar = var;
                }
            }
        }
        return minVar;
    }

    // return list based on least constraining value (LCS) heuristic
    public static ArrayList<Integer> determineLCV(LinkedList<Integer> unassignedVar, HashMap<Integer, Integer> assignments, HashMap<Integer, ArrayList<Integer>> allowedValMatrix, int var) {

        // value -> (number of choices ruled out for neighboring variables)
        HashMap<Integer, Integer> counts = new HashMap<Integer, Integer>();

        ArrayList<Integer> allowedVal = allowedValMatrix.get(var);
        HashSet<Integer> affectedVarSet = findAffected(unassignedVar, assignments, var);

        for (int value : allowedVal) {
            int count = 0;
            for (int affectedVar : affectedVarSet) {
                ArrayList<Integer> valuesOfAffectedVar = allowedValMatrix.get(affectedVar);
                for (int val : valuesOfAffectedVar) {
                    if (value == val) {
                        count++;
                    }
                }
            }
            counts.put(value, count);
        }

        ArrayList<Integer> valuesByCount = new ArrayList<Integer>();
        for (int val : counts.keySet()) {
            valuesByCount.add(val);
        }

        Comparator<Integer> comp = new Comparator<Integer>() {
            public int compare(Integer i1, Integer i2) {
                return counts.get(i1).compareTo(counts.get(i2));
            }
        };

        Collections.sort(valuesByCount, comp);
        return valuesByCount;
    }
    // return the number of constrained neighbors (degree heuristic)
    public static int determineDegree(LinkedList<Integer> unassignedVar, HashMap<Integer, Integer> assignments, int var) {
            HashSet<Integer> affectedVarSet = findAffected(unassignedVar, assignments, var);
            int degree = affectedVarSet.size();
        return degree;
    }

    // backtracking search with forward checking and heuristics
    public static boolean backTrackingWithFCandH(LinkedList<Integer> unassignedVar, HashMap<Integer, Integer> assignments) {
        
        // terminate if all 81 cells are assigned (board is complete)
        if (assignments.size() == 81) {
            print_grid();
            return true;
        }

        // create domain of values for each cell: cellID -> list of allowed values for that cell
        HashMap<Integer, ArrayList<Integer>> allowedValMatrix = calculateAllowedVal(unassignedVar, assignments);

        // select an unassigned variable using minimum remaining values heuristic
        
        int var = determineMRV(unassignedVar, assignments, allowedValMatrix);

        // remove this variable from the unassigned list
        unassignedVar.remove((Integer) var);

        // allowed list of values (shuffled) for this specific variable
        ArrayList<Integer> orderedAllowedVal = determineLCV(unassignedVar, assignments, allowedValMatrix, var);

        // iterate through the values domain
        for (int value : orderedAllowedVal) {

            // checking consistency
            if (isConsistent(var, value, assignments)) {
                assignments.put(var, value);

                nodesExpanded++;

                // forward checking to eliminate assignments in advance
                if (forwardCheck(unassignedVar, assignments, var)) {     
                    if (backTrackingWithFCandH(unassignedVar, assignments)) {
                        return true;
                    }
                }
            }
        } 
        // put the variable back to unassigned variables list
        unassignedVar.push(var);
        assignments.remove(var);
        

        return false;
    } 




    public static void main(String[] args) {
        int[][] easyBoard = new int[][]{
            { 0, 6, 1, 0, 0, 0, 0, 5, 2},
            { 8, 0, 0, 0, 0, 0, 0, 0, 1},
            { 7, 0, 0, 5, 0, 0, 4, 0, 0},
            { 9, 0, 3, 6, 0, 2, 0, 4, 7},
            { 0, 0, 6, 7, 0, 1, 5, 0, 0},
            { 5, 7, 0, 9, 0, 3, 2, 0, 6},
            { 0, 0, 4, 0, 0, 9, 0, 0, 5},
            { 1, 0, 0, 0, 0, 0, 0, 0, 8},
            { 6, 2, 0, 0, 0, 0, 9, 3, 0}
        };

        int[][] mediumBoard = new int[][]{
            {5, 0, 0, 6, 1, 0, 0, 0, 0},
            {0, 2, 0, 4, 5, 7, 8, 0, 0},
            {1, 0, 0, 0, 0, 0, 5, 0, 3},
            {0, 0, 0, 0, 2, 1, 0, 0, 0},
            {4, 0, 0, 0, 0, 0, 0, 0, 6},
            {0, 0, 0, 3, 6, 0, 0, 0, 0},
            {9, 0, 3, 0, 0, 0, 0, 0, 2},
            {0, 0, 6, 7, 3, 9, 0, 8, 0},
            {0, 0, 0, 0, 8, 6, 0, 0, 5}
        };

        int[][] hardBoard = new int[][]{
            {0, 4, 0, 0, 2, 5, 9, 0, 0},
            {0, 0, 0, 0, 3, 9, 4, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 6, 1},
            {0, 1, 7, 0, 0, 0, 0, 0, 0},
            {6, 0, 0, 7, 5, 4, 0, 0, 9},
            {0, 0, 0, 0, 0, 0, 7, 3, 0},
            {4, 2, 0, 0, 0, 0, 0, 0, 0},
            {0, 9, 0, 5, 4, 0, 0, 0, 0},
            {0, 0, 8, 9, 6, 0, 0, 5, 0}  
        };


        int[][] evilBoard = new int[][]{
            {0, 6, 0, 8, 2, 0, 0, 0, 0},
            {0, 0, 2, 0, 0, 0, 8, 0, 1},
            {0, 0, 0, 7, 0, 0, 0, 5, 0},
            {4, 0, 0, 5, 0, 0, 0, 0, 6},
            {0, 9, 0, 6, 0, 7, 0, 3, 0},
            {2, 0, 0, 0, 0, 1, 0, 0, 7},
            {0, 2, 0, 0, 0, 9, 0, 0, 0},
            {8, 0, 4, 0, 0, 0, 7, 0, 0},
            {0, 0, 0, 0, 4, 8, 0, 2, 0}
        };

        // Choose one of the boards:
        // int[][] sudokuBoard = easyBoard;
        //int[][] sudokuBoard = mediumBoard;
        //int[][] sudokuBoard = hardBoard;
        int[][] sudokuBoard = evilBoard;
 
        // printing initial matrix
        System.out.println("Sudoku Problem:");
        for (int i = 0; i < sudokuBoard.length; i++) {
            for (int j = 0; j < sudokuBoard[0].length; j++) {
                System.out.print(sudokuBoard[i][j] + "   ");
            }
            System.out.println();
        }


        // initializing the list of unassigned variables and existing assignments
        readInitialBoard(sudokuBoard);

        System.out.println("----------------------------------------------");

        shaffleUnassignedVar();
        nodesExpanded = 1;
        startTime = System.currentTimeMillis();

        // Choose one of the methods:
        //backTrackingSearch(unassignedVar, assignments);  
        //backTrackingWithFC(unassignedVar, assignments);  
        backTrackingWithFCandH(unassignedVar, assignments); 

    }
}