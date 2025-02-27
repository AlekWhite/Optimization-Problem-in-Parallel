package com.awsite;
import java.awt.*;

public class config {

    static boolean DISPLAY_ONLY = false;
    static final int BOARD_COUNT = 8 ;
    public final static int MAX_PERMUTATIONS_ATTEMPTS = 10;
    public final static int EXCHANGER_CYCLES = 50;

    // station / shape stuff
    static final int[] STATION_DISTRIBUTION = {10, 5, 20, 10, 10, 10};
    public final static int MAX_STATION_COUNT = 65;
    public final static int MAX_CELLS_IN_STATION = 5;
    public final static int GRID_SIZE = 50;
    public final static int FUNCTION_COUNT = 6;

    final static int[][] affinityMatrix = {
        {0, 1, -2, 9, 4, 5},
        {3, 0, 4, 1, 5, 2},
        {2, 5, 0, -4, 3, 1},
        {5, 3, 1, 0, 2, 4},
        {1, -4, 5, 2, 0, 3},
        {9, 2, 3, 5, -1, 0}};

    final static int[][][] shapes = {
            { {0, 0}, {-1, 0}, {1, 0}},
            { {0, 0}, {-1, 0}, {0, -1} },
            { {0, 0}, {-1, 0} },
            { {0, 0}, {-1, 0}, {1, 0}, {-1, -1} },
            { {0, 0}, {0, 1}, {0, -1}, {1, 1}, {-1, -1}},
            { {0, 0}, {1, 0}, {0, -1}, {1, -1} }};

    final static int[][][] rotationKey = {
            { {-1, 1}, {0,  1}, {1,  1}, {-1, 0}, { 1, 0}, {-1, -1}, { 0, -1}, { 1, -1}},
            { { 2, 0}, {1, -1}, {0, -2}, { 1, 1}, {-1, -1}, { 0,  2}, {-1,  1}, {-2,  0}}};

    static Color[] STATION_COLORS = {new Color(78, 78, 80),
            new Color(231, 67, 67),
            new Color(54, 122, 77),
            new Color(113, 113, 236),
            new Color(234, 201, 37),
            new Color(174, 102, 241),
            new Color(245, 140, 65)};







}
