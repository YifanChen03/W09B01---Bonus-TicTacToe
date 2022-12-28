package pgdp.tictactoe.ai;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import pgdp.tictactoe.Field;
import pgdp.tictactoe.Game;
import pgdp.tictactoe.Move;
import pgdp.tictactoe.PenguAI;

public class SimpleAI extends PenguAI {

    private Random random;
    private List<Integer> valuesLeft;
    private List<int[]> ownFields;
    private List<int[]> oppFields;
    private boolean thisIsFirstPlayer;

    public SimpleAI() {
        random = new Random();
        valuesLeft = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            valuesLeft.add(i);
        }
        ownFields = new ArrayList<>();
        oppFields = new ArrayList<>();
        thisIsFirstPlayer = true;
    }

    @Override
    public Move makeMove(Field[][] board, boolean firstPlayer, boolean[] firstPlayedPieces,
            boolean[] secondPlayedPieces) {
        //determine once ifFirstPlayer
        if (ownFields.size() == 0) {
            thisIsFirstPlayer = determineIfFirstPlayer();
        }
        oppFields = getOppFields(board);
        oppFields.stream().forEach(le -> System.out.println(Arrays.toString(le)));

        //calculate legalMoves
        List<int[]> legalMoves = calcLegalMoves(board);
        //legalMoves.stream().forEach(le -> System.out.println(Arrays.toString(le)));

        //calculate winningMoves for this AI and play if possible
        List<int[]> winningMoves = calcWinningMoves(board, legalMoves, ownFields);
        if (winningMoves.size() > 0) {
            System.out.println("played winning move: ");
            return chooseMove(winningMoves);
        }
        //winningMoves.stream().forEach(le -> System.out.println(Arrays.toString(le)));

        //calculate winningMoves for opponent and play if possible
        List<int[]> defendingMoves = calcWinningMoves(board, legalMoves, oppFields);
        if (defendingMoves.size() > 0) {
            System.out.println("played defending move: ");
            return chooseMove(defendingMoves);
        }
        //defendingMoves.stream().forEach(le -> System.out.println(Arrays.toString(le)));

        //otherwise play any legalMove
        System.out.println("played legal move: ");
        return chooseMove(legalMoves);
    }

    private List<int[]> calcLegalMoves(Field[][] board) {
        List<int[]> output = new ArrayList<>();

        for (int f_y = 0; f_y < board.length; f_y++) {
            for (int f_x = 0; f_x < board.length; f_x++) {
                if (board[f_x][f_y] == null ||
                        board[f_x][f_y].value() < valuesLeft.stream().mapToInt(n -> n).max().orElse(-1)) {
                    output.add(new int[]{f_x, f_y});
                }
            }
        }

        return output;
    }

    private List<int[]> calcWinningMoves(Field[][] board, List<int[]> legalMoves, List<int[]> xFields) {
        List<int[]> output = new ArrayList<>();

        //check for every Field of board if owning it would be a win
        for (int f_y = 0; f_y < board.length; f_y++) {
            for (int f_x = 0; f_x < board.length; f_x++) {
                int[] testField = new int[]{f_x, f_y};
                if (legalMoves.stream().map(le -> Arrays.toString(le)).collect(Collectors.toList())
                        .contains(Arrays.toString(testField))) {
                    xFields.add(testField);
                    if (Game.checkWin(xFields, null) == 1) {
                        output.add(testField);
                    }
                    xFields.removeIf(lE -> Arrays.toString(lE).equals(Arrays.toString(testField)));
                }
            }
        }

        return output;
    }

    private List<int[]> getOppFields(Field[][] board) {
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

    private Move chooseMove(List<int[]> moveSet) {
        int[] xy = moveSet.get(random.nextInt(moveSet.size()));
        Integer valueToPlay = valuesLeft.stream().mapToInt(n -> n).max().orElse(-1);
        Move m = new Move(xy[0], xy[1], valueToPlay);
        valuesLeft.removeIf(n -> n == valueToPlay);
        ownFields.add(xy);
        System.out.println(Arrays.toString(xy));
        return m;
    }

    private boolean determineIfFirstPlayer() {
        return Game.getfP_fields().equals(ownFields);
    }
}
