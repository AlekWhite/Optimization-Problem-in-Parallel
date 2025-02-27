package com.awsite;
import java.io.*;
import java.util.concurrent.Phaser;

public class Main {

    static Gui gui = new Gui();
    public static boolean doExchange = false;
    public static Board best;

    public static void main(String[] args) throws IOException, ClassNotFoundException {

        // load board from file to display only
        if (config.DISPLAY_ONLY){
           Board b = null;
            { try {b = new Board(true);}
            catch (IOException | ClassNotFoundException e) {e.printStackTrace();}}
            assert b != null;
            gui.displayCells(b.getBoard());
            return;}

        int nSum = 0;
        for (int n: config.STATION_DISTRIBUTION) nSum = nSum + n;
        if (nSum != config.MAX_STATION_COUNT) return;

        // create empty boards in new threads
        Thread[] boards = new Thread[config.BOARD_COUNT];
        for (int i=0; i<config.BOARD_COUNT; i++){
            boards[i] = new Board(false);
            int function = 0, functionCount = config.STATION_DISTRIBUTION[function];
            for(int j=0; j<config.MAX_STATION_COUNT; j++){
                --functionCount;
                new Station(function, (Board)boards[i]);
                if ((functionCount == 0) && (function != config.FUNCTION_COUNT-1)){
                    ++function;
                    functionCount = config.STATION_DISTRIBUTION[function];}}}
        for (Thread b: boards) {
            ((Board)b).constructPointArray();}

        // do permutations and exchange
        int bestInd;
        double startingAf = 0;
        best = (Board)boards[0];
        for (int j=0; j<config.EXCHANGER_CYCLES; j++){
            if (j == 1) startingAf = best.getAffinityVector()[0];

            // do permutations, and wait for all threads to finish
            System.out.println("doing permutations");
            doTasks(boards);
            System.out.println("done waiting");

            // exchange best board with others
            doExchange = true;
            doTasks(boards);
            doExchange = false;

            // display new best board
            bestInd = getBestBoard(boards);
            best = ((Board)boards[bestInd]);
            gui.displayCells(best.getBoard());
            System.out.println(best.getAffinityVector()[0]);}

        // print save and show final board
        System.out.println(best);
        System.out.println("Starting Score: " + startingAf);
        System.out.println("Final Score: " + (best.getAffinityVector()[0]));
        System.out.println("Difference: " + (best.getAffinityVector()[0] - startingAf) +
                " (" + (((best.getAffinityVector()[0]-startingAf)/startingAf)*100) + "%)");
        FileOutputStream fos2 = new FileOutputStream("out.src");
        ObjectOutputStream oos2 = new ObjectOutputStream(fos2);
        oos2.writeObject(best);
        oos2.close();

    }

    static int getBestBoard(Thread[] boards){
        int bestInd = 0;
        double bestScore = 0, ns;
        for (int i=0; i<boards.length; i++){
            ns = ((Board)boards[i]).getAffinityVector()[0];
            if (ns > bestScore){
                bestScore = ns;
                bestInd = i;}}
        return bestInd;}

    // start everything, do stuff, wait, come back here when done
    static void doTasks(Runnable[] tasks) {
        Phaser phaser = new Phaser(1);
        for (Runnable task : tasks) {
            phaser.register();
            new Thread(() -> {
                phaser.arriveAndAwaitAdvance();
                task.run();
                phaser.arriveAndDeregister();
            }).start();}
        phaser.arriveAndAwaitAdvance();
        phaser.arriveAndDeregister();
        phaser.register();
        while (!phaser.isTerminated() && (phaser.getRegisteredParties() != 1)){
            phaser.arriveAndAwaitAdvance();}
        phaser.arriveAndDeregister();}

}
