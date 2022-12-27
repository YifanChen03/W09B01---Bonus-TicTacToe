package pgdp.tictactoe;

import pgdp.tictactoe.ai.HumanPlayer;

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
    private boolean firstPlayer;
    private boolean[] firstPlayedPieces;
    private boolean[] secondPlayedPieces;
    private PenguAI winner;
    private List<int[]> fP_fields;
    private List<int[]> sP_fields;

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
            if (firstPlayer) {
                m = first.makeMove(board, true, firstPlayedPieces, secondPlayedPieces);
                x = m.x();
                y = m.y();
                value = m.value();

                //check if input position was outside of board and if player still has chosen piece
                if (x <= 2 && x >= 0 && y <= 2 && y >= 0 && !firstPlayedPieces[value]) {
                    int[] toAdd = new int[]{x, y};
                    //check if input position is already occupied if yes then check if value is smaller than chosen piece
                    if (board[x][y] == null) {
                        board[x][y] = new Field(value, firstPlayer);
                        firstPlayedPieces[value] = true;
                        fP_fields.add(toAdd);
                    } else {
                        if (board[x][y].value() < value) {
                            board[x][y] = new Field(value, firstPlayer);
                            firstPlayedPieces[value] = true;
                            fP_fields.add(toAdd);
                            sP_fields.removeIf(lE -> lE.equals(toAdd));
                        } else {
                            illegalMove();
                            break;
                        }
                    }
                } else {
                    illegalMove();
                    break;
                }
            } else {
                m = second.makeMove(board, true, firstPlayedPieces, secondPlayedPieces);
                x = m.x();
                y = m.y();
                value = m.value();

                //check if input position was outside of board and if player still has chosen piece
                if (x <= 2 && x >= 0 && y <= 2 && y >= 0 && !secondPlayedPieces[value]) {
                    int[] toAdd = new int[]{x, y};
                    //check if input position is already occupied if yes then check if value is smaller than chosen piece
                    if (board[x][y] == null) {
                        board[x][y] = new Field(value, firstPlayer);
                        secondPlayedPieces[value] = true;
                        sP_fields.add(toAdd);
                    } else {
                        if (board[x][y].value() < value) {
                            board[x][y] = new Field(value, firstPlayer);
                            secondPlayedPieces[value] = true;
                            sP_fields.add(toAdd);
                            fP_fields.removeIf(lE -> lE.equals(toAdd));
                        } else {
                            illegalMove();
                            break;
                        }
                    }
                } else {
                    illegalMove();
                    break;
                }
            }
            PenguAI temp = checkWin();
            if (temp != null) {
                winner = temp;
                break;
            }
            firstPlayer = !firstPlayer;
        }
        //Move m;
        /*boolean[] currPlayedPieces;
        //int x;
        //int y;
        //int value;

        while (checkIfPiecesLeft()) {
            if (firstPlayer) {
                //m = first.makeMove(board, true, firstPlayedPieces, secondPlayedPieces);
                currPlayedPieces = firstPlayedPieces;
            } else {
                m = second.makeMove(board, false, firstPlayedPieces, secondPlayedPieces);
                currPlayedPieces = secondPlayedPieces;
            }
            //x = m.x();
            //y = m.y();
            //value = m.value();

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
            } else {
                illegalMove();
                break;
            }
            if (firstPlayer) {
                 firstPlayedPieces = currPlayedPieces;
                 fP_fields.add(new int[]{x, y});
            } else {
                secondPlayedPieces = currPlayedPieces;
                sP_fields.add(new int[]{x, y});
            }
            if (checkWin() != null)
                return;
            firstPlayer = !firstPlayer;
        }*/
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

    public PenguAI checkWin() {
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
            if (fP_fields.stream().map(le -> Arrays.toString(le)).collect(Collectors.toList())
                    .containsAll(row.stream().map(le -> Arrays.toString(le)).collect(Collectors.toList()))) {
                return first;
            }
            if (sP_fields.stream().map(le -> Arrays.toString(le)).collect(Collectors.toList())
                    .containsAll(row.stream().map(le -> Arrays.toString(le)).collect(Collectors.toList()))) {
                return second;
            }
        }
        return null;
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
