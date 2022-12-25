package pgdp.tictactoe;

import pgdp.tictactoe.ai.HumanPlayer;

import java.util.Arrays;

import java.util.stream.Stream;

public class Game {
    private PenguAI first;
    private PenguAI second;
    private Field[][] board;
    private boolean firstPlayer;
    private boolean[] firstPlayedPieces;
    private boolean[] secondPlayedPieces;
    private PenguAI winner;

    public Game(PenguAI first, PenguAI second) {
        this.first = first;
        this.second = second;

        board = new Field[3][3];
        firstPlayer = true;
        firstPlayedPieces = new boolean[9];
        secondPlayedPieces = new boolean[9];
        //fill arrays with false
        Arrays.fill(firstPlayedPieces, false);
        Arrays.fill(secondPlayedPieces, false);
        winner = null;
    }

    public PenguAI getWinner() {
        return winner;
    }

    public void playGame() {
        Move m;
        boolean[] currPlayedPieces;
        int x;
        int y;
        int value;
        //as long as one of the players has pieces left
        while (checkIfPiecesLeft()) {
            if (firstPlayer) {
                m = first.makeMove(board, true, firstPlayedPieces, secondPlayedPieces);
                currPlayedPieces = firstPlayedPieces;
            } else {
                m = second.makeMove(board, false, firstPlayedPieces, secondPlayedPieces);
                currPlayedPieces = secondPlayedPieces;
            }
            x = m.x();
            y = m.y();
            value = m.value();
            //check if input position was outside of board and if player still has chosen piece
            if (x <= 2 && x >= 0 && y <= 2 && y >= 0 && !currPlayedPieces[value]) {
                //check if input position is already occupied if yes then check if value is smaller than chosen piece
                if (board[x][y] == null) {
                    board[x][y] = new Field(value, firstPlayer);
                    currPlayedPieces[value] = true;
                } else {
                    if (board[x][y].value() < value) {
                        board[x][y] = new Field(value, firstPlayer);
                        currPlayedPieces[value] = true;
                    } else {
                        illegalMove();
                        break;
                    }
                }
                printBoard(board);
            } else {
                illegalMove();
                break;
            }
            if (firstPlayer) {
                 firstPlayedPieces = currPlayedPieces;
            } else {
                secondPlayedPieces = currPlayedPieces;
            }
            firstPlayer = !firstPlayer;
            printBoard(board);
        }
    }

    public static void printBoard(Field[][] board) {
        System.out.println("┏━━━┳━━━┳━━━┓");
        for (int y = 0; y < board.length; y++) {
            System.out.print("┃");
            for (int x = 0; x < board.length; x++) {
                if (board[x][y] != null) {
                    System.out.print(board[x][y] + "┃");
                } else {
                    System.out.print("   ┃");
                }
            }
            System.out.println();
            if (y != board.length - 1) {
                System.out.println("┣━━━╋━━━╋━━━┫");
            }
        }
        System.out.println("┗━━━┻━━━┻━━━┛");
    }

    public boolean checkIfPiecesLeft() {
        for (boolean b : firstPlayedPieces) {
            if (!b) {
                return true;
            }
        }
        for (boolean b : secondPlayedPieces) {
            if (!b) {
                return true;
            }
        }
        return false;
    }

    public void illegalMove() {
        if (firstPlayer) {
            winner = second;
        } else {
            winner = first;
        }
    }

    public static void main(String[] args) {
        PenguAI firstPlayer = new HumanPlayer();
        PenguAI secondPlayer = new HumanPlayer();
        Game game = new Game(firstPlayer, secondPlayer);
        game.playGame();
        if(firstPlayer == game.getWinner()) {
            System.out.println("Herzlichen Glückwunsch erster Spieler");
        } else if(secondPlayer == game.getWinner()) {
            System.out.println("Herzlichen Glückwunsch zweiter Spieler");
        } else {
            System.out.println("Unentschieden");
        }
    }
}
