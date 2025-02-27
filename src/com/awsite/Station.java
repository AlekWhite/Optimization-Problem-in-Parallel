package com.awsite;

import java.util.concurrent.ThreadLocalRandom;

public class Station {

    int[][] blockedCells;
    final int function;
    Board board;

    // create a new station, add it to the board
    public Station(int function, Board board){
        this.function = function;
        this.board = board;

        // give random starting position
        int[][] startingCells;
        while (true){
            startingCells = config.shapes[function].clone();
            int deltaX = ThreadLocalRandom.current().nextInt(0, config.GRID_SIZE);
            int deltaY = ThreadLocalRandom.current().nextInt(0, config.GRID_SIZE);
            for (int i=0; i<startingCells.length; i++){
                startingCells[i] = new int[]{startingCells[i][0] + deltaX, startingCells[i][1] + deltaY};}

            // mark the selected cells as blocked
            if (!board.cellsBlocked(startingCells, null)) {
                blockedCells = startingCells;
                board.blockCells(blockedCells, function);
                break;}}}

    // average distance*affinity between this station and all other in the points array
    public static double calculateAffinityScore(double[][][] points, int pointIndex, int function){
        double[] point = points[function][pointIndex];
        double maxDist = Math.sqrt(Math.pow(config.GRID_SIZE, 2)*2);

        // sum the distance*affinityScore
        double affinityScore = 0;
        for (int x=0; x<points.length; x++){
            for (int y=0; y<points[x].length; y++){
                if (points[x][y][0] == -1.0) continue;
                if ((x == function) && (y == pointIndex)) continue;
                double distance = Math.sqrt( Math.pow(Math.abs((point[0] - points[x][y][0])),2)  +  Math.pow(Math.abs((point[1] -points[x][y][1])),2));
                affinityScore += (maxDist-distance)*config.affinityMatrix[function][x];}}

        // get the affinity score for that station
        return affinityScore/(config.MAX_STATION_COUNT-1);}

    // rotates the station if possible
    public static int[][] attemptToRotate(int[][] targetCells, int angle){
        int[][] oldBlockedCells = targetCells.clone();

        // do angle number of 90 degree turns
        for (int k=0; k<angle; k++){

            // find each pieces displacement from the center
            int[][] displacements = new int[oldBlockedCells.length-1][2];
            for (int i=1; i<oldBlockedCells.length; i++){
                displacements[i-1] = new int[] {oldBlockedCells[i][0] - oldBlockedCells[0][0], oldBlockedCells[i][1] - oldBlockedCells[0][1]};}

            // apply the corresponding delta x, y to the base point, for the given displacement
            int[][] newBlockedCells = new int[oldBlockedCells.length][2];
            newBlockedCells[0] = oldBlockedCells[0];
            for (int i=0; i<displacements.length; i++){
                for (int j=0; j<config.rotationKey[0].length; j++){
                    if ((displacements[i][0] == config.rotationKey[0][j][0]) && (displacements[i][1] == config.rotationKey[0][j][1])){
                        newBlockedCells[i+1] = new int[]{oldBlockedCells[i+1][0] + config.rotationKey[1][j][0],
                                oldBlockedCells[i+1][1] + config.rotationKey[1][j][1]};
                        break;}}}
            oldBlockedCells = newBlockedCells.clone();}

            return oldBlockedCells;}
}
