package pgdp.tictactoe;

import pgdp.tictactoe.ai.CompetitionAI;
import pgdp.tictactoe.ai.HumanPlayer;
import pgdp.tictactoe.ai.SimpleAI;

import java.util.ArrayList;
import java.util.Arrays;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Game {
    private PenguAI first;
    private PenguAI second;
    private Field[][] board;
    private boolean isFirstPlayer;
    private boolean[] firstPlayedPieces;
    private boolean[] secondPlayedPieces;
    private PenguAI winner;
    private List<int[]> fP_fields;
    private List<int[]> sP_fields;

    public Game(PenguAI first, PenguAI second) {
        this.first = first;
        this.second = second;

        board = new Field[3][3];
        isFirstPlayer = true;
        firstPlayedPieces = new boolean[9];
        secondPlayedPieces = new boolean[9];
        //fill arrays with false
        Arrays.fill(firstPlayedPieces, false);
        Arrays.fill(secondPlayedPieces, false);
        winner = null;
        fP_fields = new ArrayList<>();
        sP_fields = new ArrayList<>();
    }

    public PenguAI getWinner() {
        return winner;
    }

    public void playGame() {
        Move m;
        int x;
        int y;
        int value;

        //as long as one of the players has pieces left
        while (checkIfPiecesLeft()) {
            if (isFirstPlayer) {
                if (noMovesLeft()) {
                    winner = second;
                    System.out.println("Player 1 has no moves left");
                    break;
                }
                m = first.makeMove(board, true, firstPlayedPieces, secondPlayedPieces);
                x = m.x();
                y = m.y();
                value = m.value();

                //check if input position was outside of board and if player still has chosen piece
                if (x <= 2 && x >= 0 && y <= 2 && y >= 0 && value < 9 && value >= 0
                        && !firstPlayedPieces[value]) {
                    int[] toAdd = new int[]{x, y};
                    //check if input position is already occupied if yes then check if value is smaller than chosen piece
                    if (board[x][y] == null) {
                        addToBoard(x, y, value, toAdd);
                    } else {
                        if (board[x][y].value() < value && board[x][y].firstPlayer() == false) {
                            addToBoard(x, y, value, toAdd);
                            sP_fields.removeIf(lE -> Arrays.toString(lE).equals(Arrays.toString(toAdd)));
                        } else {
                            System.out.println("Move value too small");
                            illegalMove();
                            break;
                        }
                    }
                } else {
                    System.out.println("Move was outside of board or piece has been played already");
                    illegalMove();
                    break;
                }
            } else {
                if (noMovesLeft()) {
                    winner = first;
                    System.out.println("Player 2 has no moves left");
                    break;
                }
                m = second.makeMove(board, false, firstPlayedPieces, secondPlayedPieces);
                x = m.x();
                y = m.y();
                value = m.value();

                //check if input position was outside of board and if player still has chosen piece
                if (x <= 2 && x >= 0 && y <= 2 && y >= 0 && value < 9 && value >= 0
                        && !secondPlayedPieces[value]) {
                    int[] toAdd = new int[]{x, y};
                    //check if input position is already occupied if yes then check if value is smaller than chosen piece
                    if (board[x][y] == null) {
                        addToBoard(x, y, value, toAdd);
                    } else {
                        if (board[x][y].value() < value && board[x][y].firstPlayer() == true) {
                            addToBoard(x, y, value, toAdd);
                            fP_fields.removeIf(lE -> Arrays.toString(lE).equals(Arrays.toString(toAdd)));
                        } else {
                            System.out.println("Move value too small");
                            illegalMove();
                            break;
                        }
                    }
                } else {
                    System.out.println("Move was outside of board or piece has been played already");
                    illegalMove();
                    break;
                }
            }
            int temp = checkWin(fP_fields, sP_fields);
            if (temp == 1) {
                winner = first;
                break;
            }
            if (temp == 2) {
                winner = second;
                break;
            }
            isFirstPlayer = !isFirstPlayer;
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
        if (isFirstPlayer) {
            System.out.println("Player 1 played an illegal move");
            winner = second;
        } else {
            System.out.println("Player 2 played an illegal move");
            winner = first;
        }
    }

    public static int checkWin(List<int[]> fields1, List<int[]> fields2) {
        List<List<int[]>> winningPlaces = new ArrayList<>();

        List<int[]> firstRow = Arrays.asList(new int[][]{{0, 0}, {1, 0}, {2, 0}});
        List<int[]> secondRow = Arrays.asList(new int[][]{{0, 1}, {1, 1}, {2, 1}});
        List<int[]> thirdRow = Arrays.asList(new int[][]{{0, 2}, {1, 2}, {2, 2}});
        List<int[]> firstCol = Arrays.asList(new int[][]{{0, 0}, {0, 1}, {0, 2}});
        List<int[]> secondCol = Arrays.asList(new int[][]{{1, 0}, {1, 1}, {1, 2}});
        List<int[]> thirdCol = Arrays.asList(new int[][]{{2, 0}, {2, 1}, {2, 2}});
        List<int[]> firstDia = Arrays.asList(new int[][]{{0, 0}, {1, 1}, {2, 2}});
        List<int[]> secondDia = Arrays.asList(new int[][]{{0, 2}, {1, 1}, {2, 0}});

        winningPlaces.add(firstRow);
        winningPlaces.add(secondRow);
        winningPlaces.add(thirdRow);
        winningPlaces.add(firstCol);
        winningPlaces.add(secondCol);
        winningPlaces.add(thirdCol);
        winningPlaces.add(firstDia);
        winningPlaces.add(secondDia);

        for (List<int[]> row : winningPlaces) {
            if (fields1 != null && fields1.stream().map(le -> Arrays.toString(le)).collect(Collectors.toList())
                    .containsAll(row.stream().map(le -> Arrays.toString(le)).collect(Collectors.toList()))) {
                return 1;
            }
            if (fields2 != null && fields2.stream().map(le -> Arrays.toString(le)).collect(Collectors.toList())
                    .containsAll(row.stream().map(le -> Arrays.toString(le)).collect(Collectors.toList()))) {
                return 2;
            }
        }
        return 0;
    }

    public void addToBoard(int x, int y, int value, int[] toAdd) {
        if (isFirstPlayer) {
            board[x][y] = new Field(value, true);
            firstPlayedPieces[value] = true;
            fP_fields.add(toAdd);
        } else {
            board[x][y] = new Field(value, false);
            secondPlayedPieces[value] = true;
            sP_fields.add(toAdd);
        }
    }

    private boolean noMovesLeft() {
        if (isFirstPlayer) {
            for (int f_y = 0; f_y < board.length; f_y++) {
                for (int f_x = 0; f_x < board.length; f_x++) {
                    if (board[f_x][f_y] == null || (board[f_x][f_y].value() < fPHighest() &&
                            board[f_x][f_y].firstPlayer() != isFirstPlayer)) {
                        return false;
                    }
                }
            }
        } else {
            for (int f_y = 0; f_y < board.length; f_y++) {
                for (int f_x = 0; f_x < board.length; f_x++) {
                    if (board[f_x][f_y] == null || (board[f_x][f_y].value() < sPHighest() &&
                            board[f_x][f_y].firstPlayer() != isFirstPlayer)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private int fPHighest() {
        int n = -1;

        for (int i = 0; i < firstPlayedPieces.length; i++) {
            if (!firstPlayedPieces[i]) {
                n = i;
            }
        }

        return n;
    }

    private int sPHighest() {
        int n = -1;

        for (int i = 0; i < secondPlayedPieces.length; i++) {
            if (!secondPlayedPieces[i]) {
                n = i;
            }
        }

        return n;
    }

    public static void main(String[] args) {
        PenguAI firstPlayer = new HumanPlayer();
        PenguAI secondPlayer = new CompetitionAI();
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
