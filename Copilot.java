package sample;

import javafx.scene.canvas.GraphicsContext;

import java.util.Scanner;

public class Copilot {
    //create a tic tac toe board
    private int[][] board = new int[3][3];
    private int turn = 0;
    private int player = 1;
    private int winner = 0;
    private int draw = 0;
    //create constructor
    public Copilot() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = 0;
            }
        }
    }

    //add an X to the board
    public void addX(int x, int y) {
        board[x][y] = 1;
        turn++;
        player = 2;
    }

    //add an O to the board
    public void addO(int x, int y) {
        board[x][y] = 2;
        turn++;
        player = 1;
    }
    //see if there is three xs or os in a row
    public void checkWin() {
        //check rows
        for (int i = 0; i < 3; i++) {
            if (board[i][0] == board[i][1] && board[i][1] == board[i][2]) {
                winner = board[i][0];
            }
        }
        //check columns
        for (int i = 0; i < 3; i++) {
            if (board[0][i] == board[1][i] && board[1][i] == board[2][i]) {
                winner = board[0][i];
            }
        }
        //check diagonals
        if (board[0][0] == board[1][1] && board[1][1] == board[2][2]) {
            winner = board[0][0];
        }
        if (board[0][2] == board[1][1] && board[1][1] == board[2][0]) {
            winner = board[0][2];
        }
        //check for draw
        if (turn == 9) {
            draw = 1;
        }
    }
    //draw the board on console
    public void drawBoard() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                System.out.print(board[i][j] + " ");
            }
            System.out.println();
        }
    }

    //get the winner
    public int getWinner() {
        return winner;
    }
    //get the draw
    public int getDraw() {
        return draw;
    }
    //get the turn
    public int getTurn() {
        return turn;
    }
    //get the player
    public int getPlayer() {
        return player;
    }

    //reset the board
    public void reset() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = 0;
            }
        }
        turn = 0;
        player = 1;
        winner = 0;
        draw = 0;
    }

    //make sure the move is valid
    public boolean validMove(int x, int y) {
        if (board[x][y] == 0) {
            return true;
        } else {
            return false;
        }
    }

    //handle user input
    public void handleInput(int x, int y) {
        //check to see if x and y are in range
        if (x >= 0 && x < 3 && y >= 0 && y < 3) {
            if (player == 1) {
                addX(x, y);
            }
            else if (player == 2) {
                addO(x, y);
            }
        }
    }

    //get user input from system.in
    public void getInput() {

        int x = 0;
        int y = 0;
        try {
            Scanner in = new Scanner(System.in);
            x = Integer.parseInt(in.nextLine());
            y = Integer.parseInt(in.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input");
        }
        handleInput(x, y);
    }

    //main method
    public static void main(String[] args) {
        //get user input until game ends
        Copilot game = new Copilot();
        while (game.getWinner() == 0 && game.getDraw() == 0) {
            game.drawBoard();
            game.getInput();
            game.checkWin();
            game.checkDraw();
        }
        //print out the winner
        if (game.getWinner() == 1) {
            System.out.println("X wins");
        }
        if (game.getWinner() == 2) {
            System.out.println("O wins");
        }
        if (game.getDraw() == 1) {
            System.out.println("Draw");
        }
        //start the game again
        game.reset();
    }
    //see if the game is drawn
    public void checkDraw() {
        if (turn == 9) {
            draw = 1;
        }
    }
}
