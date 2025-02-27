package com.awsite;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Gui extends JFrame {

    JPanel[][] cells;

    public Gui(){

        // main stuff
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.setLayout(null);
        this.setTitle("CSC375A1-AW");
        this.setSize(1000, 950);
        this.getContentPane().setBackground(new Color(0x353836));
        this.setVisible(true);

        // add background image
        BufferedImage myPicture = null;
        try {
            myPicture = ImageIO.read(new File("gui/background.png"));
        } catch (IOException e) {
            e.printStackTrace();}
        JLabel picLabel = new JLabel(new ImageIcon(myPicture));
        picLabel.setBounds(0, 0, 1000, 950);
        picLabel.setVisible(true);

        // draw and create empty cells
        cells = new JPanel[config.GRID_SIZE][config.GRID_SIZE];
        for (int i = 0; i<config.GRID_SIZE; i++){
            for (int j = 0; j<config.GRID_SIZE; j++){
                JPanel panel = new JPanel();
                panel.setBackground(config.STATION_COLORS[0]);
                panel.setBounds(110+(j*15), 50+(i*15), 15, 15);
                panel.setBorder(BorderFactory.createLineBorder(Color.black));
                this.add(panel);
                cells[i][j] = panel;}}

        this.add(picLabel);
        this.repaint();
    }

    // displays a given board
    public void displayCells(int[][][][] board){
        resetDisplay();
        for (int k=0; k<board.length; k++){
            for(int j=0; j<board[k].length; j++){
                for(int i=0; i<board[k][j].length; i++){
                    if (board[k][j][i][0] == -1) break;
                    cells[(config.GRID_SIZE-1) -board[k][j][i][1]][board[k][j][i][0]].setBackground(config.STATION_COLORS[k+1]);
                }
            }
        }
    }

    private void resetDisplay(){
        for (JPanel[] jPanels : cells) {
            for (JPanel cell : jPanels) {
                cell.setBackground(config.STATION_COLORS[0]);}}}
}
