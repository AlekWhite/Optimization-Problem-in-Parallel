package com.awsite;

import java.io.*;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public class Board extends Thread implements Serializable {

    private double[] affinityVector = new double[config.MAX_STATION_COUNT+1];
    private double[][][] points;
    private int[][][][] board;

    public Board(boolean created) throws IOException, ClassNotFoundException {

        if (created){
            Board b = (Board) new ObjectInputStream(new FileInputStream("out.src")).readObject();
            board = b.board;
            return;}

        board = new int[config.FUNCTION_COUNT][config.MAX_STATION_COUNT][config.MAX_CELLS_IN_STATION][2];
        // build empty board
        for (int i=0; i<board.length; i++){
            for (int j=0; j<board[i].length; j++){
                for (int k=0; k<board[i][j].length; k++){
                    board[i][j][k][0] = -1;
                    board[i][j][k][1] = -1;}}}
    }

    public void run(){

        if (Main.doExchange){
            if (affinityVector[0] == Main.best.affinityVector[0]) return;
            try {exchangeWith(Main.best);}
            catch (IOException | ClassNotFoundException e) {e.printStackTrace();}
            return;}

        for (int i=0; i<config.MAX_PERMUTATIONS_ATTEMPTS; i++){
            preformPermutation();}
        constructPointArray();
        affinityVector = constructAffinityVector(points);}

    /* --- Cell Blocking ---*/

    // check if cells can be added to the board
    public Boolean cellsBlocked(int[][] cells, int[][] exceptionCells){

        // ensure cells are in the grid
        for (int[] cell: cells){
            if ((cell[0] >= config.GRID_SIZE) || (cell[1] >= config.GRID_SIZE) || (cell[0] < 0) || (cell[1] < 0)) {
                return true;}}

        // for each set of station
        for(int k=0; k<config.FUNCTION_COUNT; k++){
            for (int[][] stationCells: board[k]){
                boolean cellsMatch = false;

                // see if the station occupies any of the same cells in cells
                for (int[] stationCell : stationCells) {

                    // ignore exception cells
                    boolean isActuallyEmpty = false;
                    if (exceptionCells != null){
                        for (int[] exCell: exceptionCells){
                            if((exCell[0] == stationCell[0]) &&  exCell[1] == stationCell[1]){
                                isActuallyEmpty = true;
                                break;}}}

                    if (!isActuallyEmpty){
                        for (int[] cell : cells) {
                            if ((stationCell[0] == cell[0]) && (stationCell[1] == cell[1])) {
                                cellsMatch = true;
                                break;}}}}

                if (cellsMatch) return true;}}
        return false;}

    // adds cells to the board array
    public void blockCells(int[][] cells, int function){
        /*
        StringBuilder ot = new StringBuilder("blocking val " + function + " :");
        boolean p = false;
        for (int[] cell: cells){
            ot.append("(").append(cell[0]).append(" ").append(cell[1]).append(" )");
            if ((cell[0] >= GRID_SIZE) | (cell[1] >= GRID_SIZE) | (cell[0] < 0) | (cell[1] < 0)){
                p = true;}}
        System.out.println(ot.append(" ").append(p).append("\n"));

         */

        int[][][] slot = board[function].clone();
        for (int i=0; i<slot.length; i++){
            if ((slot[i][0][0] == -1) && (slot[i][0][1] == -1)){
                slot[i] = cells.clone();
                break;}}
        board[function] = slot.clone();}

    // removes cells to the board array
    public void unblockCells(int[][] cells, int function){
        int[][][] slot = board[function].clone();
        for (int i=0; i<slot.length; i++){
            if ((slot[i][0][0] == cells[0][0]) && (slot[i][0][1] == cells[0][1])){
                slot[i] = new int[][]{{-1, -1}};
                break;}}
        board[function] = slot.clone();}

    /* --- Affinity ---*/

    // get array of points
    public void constructPointArray(){
        double[][][] points = new double[config.FUNCTION_COUNT][config.MAX_STATION_COUNT][2];
        for (int i=0; i<config.FUNCTION_COUNT; i++){
            for (int j=0; j<config.MAX_STATION_COUNT; j++){

                // find average point of the station
                int xAvg = 0, yAvg = 0;
                for (int k=0; k<board[i][j].length; k++){
                    if (board[i][j][k][0] == -1) continue;
                    xAvg += board[i][j][k][0];
                    yAvg += board[i][j][k][1];}

                if ((xAvg == 0) && (yAvg == 0)) {
                    points[i][j] = new double[]{-1.0, -1.0};
                    continue;}
                points[i][j] = new double[]{(double) xAvg / board[i][j].length, (double) yAvg / board[i][j].length};
            }}
        this.points = points;}

    // calculate overall affinity score, and individual ones
    public static double[] constructAffinityVector(double[][][] points){

        // for every point
        double[] affinityVector = new double[config.MAX_STATION_COUNT+1];
        int affinityIndex = 1;
        double score, boardsScore = 0;
        for (int pointFunction=0; pointFunction<points.length; pointFunction++){
            for (int pointIndex=0; pointIndex<points[pointFunction].length; pointIndex++){

                // get the affinity score for that station
                if (affinityIndex > config.MAX_STATION_COUNT) break;
                score = Station.calculateAffinityScore(points, pointIndex, pointFunction);
                affinityVector[affinityIndex] = score;
                boardsScore += score;
                affinityIndex += 1;}}

        affinityVector[0] = boardsScore / config.MAX_STATION_COUNT;
        return affinityVector;}

    /* --- Board Modification ---*/

    // attempt to move randomly + rotate, or shift by one
    private void preformPermutation(){
        int[] stationsSeen = new int[config.MAX_STATION_COUNT];
        int seenCount = 0;
        int function, stationIndex;
        int[][] newCells = null;
        while (seenCount < 10){

            // select new random station
            function = ThreadLocalRandom.current().nextInt(0, config.FUNCTION_COUNT);
            stationIndex = ThreadLocalRandom.current().nextInt(0, config.STATION_DISTRIBUTION[function]);
            if (board[function][stationIndex][0][0] == -1){continue;}

            // ensure station has not been seen yet
            boolean seenBefore = false;
            for (int seenIndex: stationsSeen){
                if (seenIndex == stationIndex*(function+1)){ seenBefore = true; break;}}
            if (seenBefore) continue;
            stationsSeen[seenCount] = stationIndex*(function+1);
            ++seenCount;

            boolean attemptLoc = ThreadLocalRandom.current().nextBoolean(), wasNotBlocked = false;
            int[][] seenCells = new int[config.GRID_SIZE*config.GRID_SIZE][2];
            int[][] cell = board[function][stationIndex].clone();
            int seenCellsCount = 0, stlCount = 0, axis=0, dir=0;

            while (stlCount < 100){
                for (int t=0; t<2; t++){

                    // attempt local move
                    if (attemptLoc){

                        // attempt to move one unit in any direction
                        boolean exitTrail = false;
                        for(;axis<2; axis++){
                            for (;dir<2; dir++){

                                // get moved cells
                                newCells = cell.clone();
                                int[] delta = new int[]{0, 0};
                                delta[axis] = 1-(2*dir);
                                for(int i=0; i<newCells.length; i++){
                                    if (newCells[i][0] == -1) continue;
                                    newCells[i] = new int[]{newCells[i][0] + delta[0], newCells[i][1] + delta[1]};}

                                // see if blocked
                                if (!cellsBlocked(newCells, cell)) {
                                    exitTrail = true;
                                    wasNotBlocked = true;
                                    break;}}

                            if (exitTrail) break;}}

                    // attempt translations
                    else {
                        while (seenCellsCount < config.GRID_SIZE*config.GRID_SIZE){

                            // find a new random center for this station
                            int [] newOriginCell = {ThreadLocalRandom.current().nextInt(0, config.GRID_SIZE),
                                    ThreadLocalRandom.current().nextInt(0, config.GRID_SIZE)};

                            // ensure this center has not been checked yet
                            boolean seenCellBefore = false;
                            for (int[] scell: seenCells){
                                if ((scell[0] == newOriginCell[0]) && (scell[1] == newOriginCell[1])) {seenCellBefore =true; break;}}
                            if (seenCellBefore) continue;
                            seenCells[seenCellsCount] = newOriginCell.clone();
                            seenCellsCount++;

                            // get new cells
                            int[] delta = {newOriginCell[0] - cell[0][0], newOriginCell[1] - cell[0][1]};
                            newCells = cell.clone();
                            for(int i=0; i<newCells.length; i++){
                                newCells[i] = new int[]{newCells[i][0] + delta[0], newCells[i][1] + delta[1]};}

                            // do a random rotation
                            int rot = ThreadLocalRandom.current().nextInt(0, 5);
                            if (rot > 3) rot = 0;
                            newCells = Station.attemptToRotate(newCells, rot);

                            // see if blocked
                            if(!cellsBlocked(newCells, cell)){
                                wasNotBlocked = true;
                                break;}}}

                    // switch between rotation and translation if one fails entirely
                    if (!wasNotBlocked){
                        attemptLoc = !attemptLoc;}
                    else break;}

                if (!wasNotBlocked){
                    break;}

                // find the new average location for after the rotation / translation
                int xAvg=0, yAvg=0;
                for (int[] ncell: newCells){
                    xAvg += ncell[0];
                    yAvg += ncell[1];}
                double[][][] newPoints = points.clone();
                newPoints[function][stationIndex] = new double[]{ (double) xAvg/newCells.length, (double) yAvg/newCells.length};

                // compare values and update board
                //double newTotalAffinity = constructAffinityVector(newPoints)[0];

                double newAffinity = Station.calculateAffinityScore(newPoints, stationIndex, function);

                if (newAffinity > affinityVector[0]){
                    double[] newAv = constructAffinityVector(newPoints);
                    if (newAv[0] > affinityVector[0]){
                        unblockCells(board[function][stationIndex], function);
                        blockCells(newCells, function);
                        this.points[function][stationIndex] = newPoints[function][stationIndex].clone();
                        affinityVector = newAv;
                        return;}}
                stlCount ++;
            }
        }
    }

    // swap parts of this board with another
    private void exchangeWith(Board child) throws IOException, ClassNotFoundException {

        double[] primary = getAffinityVector();
        double[] secondary = child.getAffinityVector();
        int[][][][] primaryBoard = getBoard();
        int[][][][] secondaryBoard = child.getBoard();
        int[][][][] bestBoard = new int[config.FUNCTION_COUNT][config.MAX_STATION_COUNT][config.MAX_CELLS_IN_STATION][2];
        int[][][][] fallBackBoard = new int[config.FUNCTION_COUNT][config.MAX_STATION_COUNT][config.MAX_CELLS_IN_STATION][2];
        int[][] locations = new int[config.MAX_STATION_COUNT][2];
        double[] bestScores = new double[config.MAX_STATION_COUNT];

        // build the best and worst boards
        int function = 0;
        int count = 0;
        for (int i=1; i<primary.length; i++){

            // record best score for this location
            locations[i-1] = new int[]{function, count};
            bestScores[i-1] = Math.max(primary[i], secondary[i]);

            // add better scoring cells to bestBoard
            if (primary[i] > secondary[i]){
                bestBoard[function][count] = primaryBoard[function][count].clone();
                fallBackBoard[function][count] = secondaryBoard[function][count].clone();}

            // add worse scoring cells to fallBack
            else {
                bestBoard[function][count] = secondaryBoard[function][count].clone();
                fallBackBoard[function][count] = primaryBoard[function][count].clone();}

            // note function change through iteration of affinity vector
            count += 1;
            if (count == config.STATION_DISTRIBUTION[function]){
                count = 0;
                function++;}}

        // sort the best scores
        Board out = new Board(false);
        double[] unsortedScores = bestScores.clone();
        Arrays.sort(bestScores);

        // ensure each location corresponds to its sorted score
        int[][] newLocations = new int[config.MAX_STATION_COUNT][2];
        int[] usedIndex = new int[config.MAX_STATION_COUNT];
        Arrays.fill(usedIndex, -1);

        for (int i=0; i<bestScores.length; i++){
            int locationIndex = 0;
            for (; locationIndex < unsortedScores.length; locationIndex++)
                if (unsortedScores[locationIndex] == bestScores[i]) break;

            // ensure every station is put on the new board
            boolean wasUsed;
            do {
                wasUsed = false;
                for(int index: usedIndex){
                    if (index == locationIndex){
                        locationIndex += 1;
                        wasUsed = true;
                        break;
                    }}
            } while (wasUsed);
            usedIndex[i] = locationIndex;
            newLocations[locationIndex] = locations[i];}

        // make new board
        for(int i=bestScores.length-1; i>=0; i--){
            int[][] idealCells = bestBoard[newLocations[i][0]][newLocations[i][1]];
            if (out.cellsBlocked(idealCells, null))
                idealCells = fallBackBoard[newLocations[i][0]][newLocations[i][1]];
            out.blockCells(idealCells, newLocations[i][0]);}

        // return the new board, if its better
        out.constructPointArray();
        out.affinityVector = constructAffinityVector(out.points);
        if (out.affinityVector[0] < child.affinityVector[0]) return;

        board = out.board;
        affinityVector = out.affinityVector;
        points = out.points;
    }

    /* --- Getters, adders, setters, toStrings ---*/

    // return a clone of the board
    public int[][][][] getBoard(){return board.clone();}

    public double[] getAffinityVector(){ return affinityVector;}

    // prints all data in the board array
    public String toString(){
        StringBuilder output = new StringBuilder();
        for (int i=0; i<board.length; i++){
            output.append("Stations with function #").append(i).append("\n");

            for (int j=0; j<board[i].length; j++){
                StringBuilder station = new StringBuilder("[ ");
                for (int k=0; k<board[i][j].length; k++){
                    station.append("(").append(board[i][j][k][0]).append(", ").append(board[i][j][k][1]).append("), ");}
                station.append(" ]\n");
                output.append(station);}}

        return output.toString();}


    @Serial
    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        board = new int[config.FUNCTION_COUNT][config.MAX_STATION_COUNT][config.MAX_CELLS_IN_STATION][2];
        for (int function=0; function<config.FUNCTION_COUNT; function++){
            int[][][] slot = new int[config.MAX_STATION_COUNT][config.MAX_CELLS_IN_STATION][2];
            for(int stationCount=0; stationCount<config.MAX_STATION_COUNT;){
                int stationSize = s.readInt();
                int[][] station = new int[stationSize][2];
                for (int i=0; i<stationSize; i++){
                    station[i][0] = s.readInt();
                    station[i][1] = s.readInt();}
                slot[stationCount] = station;
                stationCount++;}
            board[function] = slot;}
    }

    @Serial
    private void writeObject(ObjectOutputStream s) throws IOException {
        for (int[][][] slot: board){
            for(int[][] station: slot){
                s.writeInt(station.length);
                for (int[] ints : station) {
                    s.writeInt(ints[0]);
                    s.writeInt(ints[1]);
                }
            }
        }
    }
}
