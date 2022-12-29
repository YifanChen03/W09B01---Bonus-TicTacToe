package pgdp.tictactoe.ai;

import java.util.*;
import java.util.stream.Collectors;

import pgdp.tictactoe.Field;
import pgdp.tictactoe.Game;
import pgdp.tictactoe.Move;
import pgdp.tictactoe.PenguAI;

public class SimpleAIV1 extends PenguAI {

    private Random random;
    private List<Integer> valuesLeft;
    private List<Integer> oppValuesLeft;
    private List<int[]> ownFields;
    private List<int[]> oppFields;
    private boolean thisIsFirstPlayer;

    public SimpleAIV1() {
        random = new Random();
        valuesLeft = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            valuesLeft.add(i);
        }
        oppValuesLeft = new ArrayList<>();
        ownFields = new ArrayList<>();
        oppFields = new ArrayList<>();
        thisIsFirstPlayer = true;
    }

    @Override
    public Move makeMove(Field[][] board, boolean firstPlayer, boolean[] firstPlayedPieces,
                         boolean[] secondPlayedPieces) {
        //still needs to calculate the defending move that makes opponent unable to win
        //Game.printBoard(board);

        //determine once ifFirstPlayer
        thisIsFirstPlayer = firstPlayer;

        if (firstPlayer) {
            oppValuesLeft = convertOppValues(firstPlayedPieces);
        } else {
            oppValuesLeft = convertOppValues(secondPlayedPieces);
        }

        oppFields = findOppFields(board);
        ownFields = findOwnFields(board);
        //oppFields.stream().forEach(le -> System.out.println(Arrays.toString(le)));

        //calculate legalMoves
        List<int[]> legalMoves = calcLegalMoves(board);
        List<int[]> oppLegalMoves = calcOppLegalMoves(board);
        /*System.out.println("legal Moves");
        legalMoves.stream().forEach(le -> System.out.print(Arrays.toString(le) + " "));
        System.out.println("opp legal Moves");
        oppLegalMoves.stream().forEach(le -> System.out.print(Arrays.toString(le) + " "));*/

        //calculate winningMoves for this AI and play if possible
        //System.out.println("own Moves");
        List<int[]> winningMoves = calcWinningMoves(legalMoves, ownFields);
        if (winningMoves.size() > 0) {
            //System.out.println("winning moves: ");
            //winningMoves.stream().forEach(le -> System.out.println(Arrays.toString(le)));
            System.out.println("played winning move: ");
            return chooseMove(winningMoves, board);
        }

        //calculate optimal move to defend at least one winning move of opponent
        //System.out.println("opp Moves");
        List<int[]> bestDefendingMoves = calculateBestDefendingMoves(board, legalMoves);
        if (bestDefendingMoves.size() > 0) {
            //System.out.println("defending moves: ");
            //defendingMoves.stream().forEach(le -> System.out.println(Arrays.toString(le)));
            System.out.println("played optimal defending move: ");
            return chooseMove(bestDefendingMoves, board);
        }

        //calculate winningMoves for opponent and play if possible
        List<int[]> defendingMoves = calcWinningMoves(oppLegalMoves, oppFields);
        if (defendingMoves.size() > 0) {
            System.out.println("played defending move: ");
            return chooseMove(defendingMoves, board);
        }

        //otherwise play any legalMove
        System.out.println("played legal move: ");
        return chooseMove(legalMoves, board);
    }

    private List<int[]> calcLegalMoves(Field[][] board) {
        List<int[]> output = new ArrayList<>();

        for (int f_y = 0; f_y < board.length; f_y++) {
            for (int f_x = 0; f_x < board.length; f_x++) {
                if (board[f_x][f_y] == null ||
                        (board[f_x][f_y].value() < valuesLeft.stream().mapToInt(n -> n).max().orElse(-1)
                                && board[f_x][f_y].firstPlayer() != thisIsFirstPlayer)) {
                    output.add(new int[]{f_x, f_y});
                }
            }
        }

        return output;
    }

    private List<int[]> calcOppLegalMoves(Field[][] board) {
        List<int[]> output = new ArrayList<>();

        for (int f_y = 0; f_y < board.length; f_y++) {
            for (int f_x = 0; f_x < board.length; f_x++) {
                if (board[f_x][f_y] == null ||
                        (board[f_x][f_y].value() < oppValuesLeft.stream().mapToInt(n -> n).max().orElse(-1)
                                && board[f_x][f_y].firstPlayer() == thisIsFirstPlayer)) {
                    output.add(new int[]{f_x, f_y});
                }
            }
        }

        return output;
    }

    private List<int[]> calcWinningMoves(List<int[]> xlegalMoves, List<int[]> xFields) {
        List<int[]> output = new ArrayList<>();
        //xFields.forEach(le -> System.out.println(Arrays.toString(le)));
        //check for every Field of legalMoves if owning it would be a win
        for (int[] testField : xlegalMoves) {
            xFields.add(testField);
            if (Game.checkWin(xFields, null) == 1) {
                output.add(testField);
            }
            xFields.removeIf(lE -> Arrays.toString(lE).equals(Arrays.toString(testField)));
        }

        return output;
    }

    private List<int[]> calculateBestDefendingMoves(Field[][] board, List<int[]> legalMoves) {
        List<int[]> output = new ArrayList<>();
        Field[][] testBoard = new Field[3][3];
        //make copy of board into testBoard
        for (int f_y = 0; f_y < board.length; f_y++) {
            for (int f_x = 0; f_x < board.length; f_x++) {
                if (board[f_x][f_y] != null) {
                    testBoard[f_x][f_y] = new Field(board[f_x][f_y].value(), board[f_x][f_y].firstPlayer());
                }
            }
        }

        for (int[] field : legalMoves) {

            /*if (testBoard[field[0]][field[1]] == null) {
                testBoard[field[0]][field[1]] = new Field(valuesLeft.stream().mapToInt(n -> n).min().orElse(-1),
                        thisIsFirstPlayer);
            } else {
                testBoard[field[0]][field[1]] = new Field(testBoard[field[0]][field[1]].value() + 1,
                        thisIsFirstPlayer);
            }*/
            testBoard[field[0]][field[1]] = new Field(valuesLeft.stream().mapToInt(n -> n).max().orElse(-1),
                    thisIsFirstPlayer);
            List<int[]> oppLegalMoves = calcOppLegalMoves(testBoard);
            if (calcWinningMoves(oppLegalMoves, oppFields).size() == 0) {
                output.add(field);
            }
            if (board[field[0]][field[1]] == null) {
                testBoard[field[0]][field[1]] = null;
            } else {
                testBoard[field[0]][field[1]] = new Field(board[field[0]][field[1]].value(),
                        board[field[0]][field[1]].firstPlayer());
            }
        }
        //output.forEach(le -> System.out.println(Arrays.toString(le)));
        return output;
    }

    private List<int[]> findOppFields(Field[][] board) {
        List<int[]> output = new ArrayList<>();

        //check for every Field of board if it is owned by opponent
        for (int f_y = 0; f_y < board.length; f_y++) {
            for (int f_x = 0; f_x < board.length; f_x++) {
                int[] checkField = new int[]{f_x, f_y};
                if (board[f_x][f_y] != null && board[f_x][f_y].firstPlayer() != thisIsFirstPlayer) {
                    output.add(checkField);
                }
            }
        }

        return output;
    }

    private List<int[]> findOwnFields(Field[][] board) {
        List<int[]> output = new ArrayList<>();

        //check for every Field of board if it is owned by opponent
        for (int f_y = 0; f_y < board.length; f_y++) {
            for (int f_x = 0; f_x < board.length; f_x++) {
                int[] checkField = new int[]{f_x, f_y};
                if (board[f_x][f_y] != null && board[f_x][f_y].firstPlayer() == thisIsFirstPlayer) {
                    output.add(checkField);
                }
            }
        }

        return output;
    }

    private Move chooseMove(List<int[]> moveSet, Field[][] board) {
        int[] xy = moveSet.get(random.nextInt(moveSet.size()));
        Integer valueToPlay;
        /*if (board[xy[0]][xy[1]] == null) {
            //choose the smallest number
            valueToPlay = valuesLeft.stream().mapToInt(n -> n).min().orElse(-1);
        } else {
            //choose the biggest number
            valueToPlay = board[xy[0]][xy[1]].value() + 1;
        }*/
        valueToPlay = valuesLeft.stream().mapToInt(n -> n).max().orElse(-1);
        Move m = new Move(xy[0], xy[1], valueToPlay);
        valuesLeft.removeIf(n -> n == valueToPlay);
        System.out.println(Arrays.toString(xy));
        return m;
    }

    private List<Integer> convertOppValues(boolean[] playedPieces) {
        List<Integer> output = new ArrayList<>();

        for (int i = 0; i < playedPieces.length; i++) {
            if (!playedPieces[i]) {
                output.add(i);
            }
        }

        return output;
    }
}

