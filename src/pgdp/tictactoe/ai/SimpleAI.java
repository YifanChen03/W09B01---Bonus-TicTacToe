package pgdp.tictactoe.ai;

import java.util.*;
import java.util.stream.Collectors;

import pgdp.tictactoe.Field;
import pgdp.tictactoe.Game;
import pgdp.tictactoe.Move;
import pgdp.tictactoe.PenguAI;

public class SimpleAI extends PenguAI {

    private Random random;
    private List<Integer> ownValuesLeft;
    private int ownMax;
    private List<Integer> oppValuesLeft;
    private int oppMax;
    private List<int[]> ownFields;
    private List<int[]> oppFields;
    private List<int[]> allFields;
    private Field[][] board;
    private boolean firstPlayer;
    private boolean[] firstPlayedPieces;
    private boolean[] secondPlayedPieces;

    public SimpleAI() {
        random = new Random();
        ownValuesLeft = new ArrayList<>();
        ownMax = 8;
        oppValuesLeft = new ArrayList<>();
        oppMax = 8;
        ownFields = new ArrayList<>();
        oppFields = new ArrayList<>();
        allFields = generateAllFields();
        allFields.forEach(le -> Arrays.toString(le));
        //board is initialized later
        //firstPlayer is initialized later
        //firstPlayedPieces is initialized later
        //secondPlayedPieces is initialized later
    }

    @Override
    public Move makeMove(Field[][] board, boolean firstPlayer, boolean[] firstPlayedPieces,
            boolean[] secondPlayedPieces) {
        //Game.printBoard(board);

        //initialize parameters
        this.board = board;
        this.firstPlayer = firstPlayer;
        this.firstPlayedPieces = firstPlayedPieces;
        this.secondPlayedPieces = secondPlayedPieces;

        if (firstPlayer) {
            ownValuesLeft = convertPlayedValues(firstPlayedPieces);
            oppValuesLeft = convertPlayedValues(secondPlayedPieces);
        } else {
            ownValuesLeft = convertPlayedValues(secondPlayedPieces);
            oppValuesLeft = convertPlayedValues(firstPlayedPieces);
        }
        if (ownValuesLeft.size() > 0) {
            ownMax = ownValuesLeft.get(ownValuesLeft.size() - 1);
        } else {
            ownMax = -1;
        }
        if (oppValuesLeft.size() > 0) {
            oppMax = oppValuesLeft.get(oppValuesLeft.size() - 1);
        } else {
            oppMax = -1;
        }

        ownFields = findFields(board, true);
        oppFields = findFields(board, false);

        //calculate legalMoves
        List<int[]> ownLegalMoves = calcLegalMoves(board, true);
        List<int[]> oppLegalMoves = calcLegalMoves(board, false);

        //calculate winningMoves for this AI and play if possible
        List<int[]> winningMoves = calcWinningMoves(ownLegalMoves, ownFields);
        //winningMoves.forEach(le -> System.out.println(Arrays.toString(le)));
        if (winningMoves.size() > 0) {
            System.out.println("played winning move: ");
            return chooseMove(winningMoves, board);
        }

        //calculate optimal move to defend all winning moves of opponent
        List<int[]> optimalDefendingMoves = calculateOptimalDefendingMoves(board, ownLegalMoves);
        if (optimalDefendingMoves.size() > 0) {
            System.out.println("played optimal defending move: ");
            return chooseMove(optimalDefendingMoves, board);
        }

        //calculate winningMoves for opponent and play if possible
        List<int[]> defendingMoves = calcWinningMoves(oppLegalMoves, oppFields);
        //filter winningMoves of opponent for ownLegalMoves
        defendingMoves = defendingMoves.stream()
                .filter(le -> (ownLegalMoves.stream().map(lm -> Arrays.toString(lm)).collect(Collectors.toList())
                        .contains(Arrays.toString(le))))
                .collect(Collectors.toList());
        if (defendingMoves.size() > 0) {
            System.out.println("played defending move: ");
            return chooseMove(defendingMoves, board);
        }

        //otherwise play any legalMove
        System.out.println("played legal move: ");
        return chooseMove(ownLegalMoves, board);
    }

    private List<int[]> generateAllFields() {
        List<int[]> output = new ArrayList<>();

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                output.add(new int[]{x, y});
            }
        }
        return output;
    }

    private List<int[]> calcLegalMoves(Field[][] board, boolean forThisAI) {
        List<int[]> output = new ArrayList<>();

        for (int[] field : allFields) {
            int x = field[0];
            int y = field[1];
            if (forThisAI) {
                if (board[x][y] == null ||
                        (board[x][y].value() < ownMax && board[x][y].firstPlayer() != firstPlayer)) {
                    output.add(new int[]{x, y});
                }
            } else {
                if (board[x][y] == null ||
                        (board[x][y].value() < oppMax && board[x][y].firstPlayer() == firstPlayer)) {
                    output.add(new int[]{x, y});
                }
            }
        }

        return output;
    }

    private List<int[]> calcWinningMoves(List<int[]> xLegalMoves, List<int[]> xFields) {
        List<int[]> output = new ArrayList<>();

        //check for every Field of xlegalMoves if owning it would be a win
        for (int[] field : xLegalMoves) {
            xFields.add(field);
            if (Game.checkWin(xFields, null) == 1) {
                output.add(field);
            }
            xFields.remove(xFields.size() - 1);
        }

        return output;
    }

    private List<int[]> calculateOptimalDefendingMoves(Field[][] board, List<int[]> xLegalMoves) {
        List<int[]> output = new ArrayList<>();
        Field[][] testBoard = new Field[3][3];

        //make copy of board into testBoard
        for (int[] field : allFields) {
            int x = field[0];
            int y = field[1];
            if (board[x][y] != null) {
                testBoard[x][y] = new Field(board[x][y].value(), board[x][y].firstPlayer());
            }
        }

        for (int[] field : xLegalMoves) {
            int x = field[0];
            int y = field[1];
            testBoard[x][y] = new Field(ownMax, firstPlayer);
            List<int[]> tempOppLegalMoves = calcLegalMoves(testBoard, false);
            List<int[]> tempOppFields = findFields(testBoard, false);
            if (calcWinningMoves(tempOppLegalMoves, tempOppFields).size() == 0) {
                //makes winning impossible for opponent
                output.add(field);
            }
            //reset field in testBoard
            if (board[x][y] == null) {
                testBoard[x][y] = null;
            } else {
                testBoard[x][y] = new Field(board[x][y].value(), board[x][y].firstPlayer());
            }
        }
        return output;
    }

    private List<int[]> findFields(Field[][] board, boolean forThisAI) {
        List<int[]> output = new ArrayList<>();

        //check for every Field of board if it is owned by opponent
        for (int f_y = 0; f_y < board.length; f_y++) {
            for (int f_x = 0; f_x < board.length; f_x++) {
                int[] checkField = new int[]{f_x, f_y};
                if (forThisAI) {
                    //for this AI
                    if (board[f_x][f_y] != null && board[f_x][f_y].firstPlayer() == firstPlayer) {
                        output.add(checkField);
                    }
                } else {
                    //for opponent
                    if (board[f_x][f_y] != null && board[f_x][f_y].firstPlayer() != firstPlayer) {
                        output.add(checkField);
                    }
                }
            }
        }

        return output;
    }

    private List<Integer> convertPlayedValues(boolean[] playedPieces) {
        List<Integer> output = new ArrayList<>();

        for (int i = 0; i < playedPieces.length; i++) {
            if (!playedPieces[i]) {
                output.add(i);
            }
        }

        return output;
    }
    private Move chooseMove(List<int[]> moveSet, Field[][] board) {
        int[] xy = moveSet.get(random.nextInt(moveSet.size()));
        Move m = new Move(xy[0], xy[1], ownMax);
        System.out.println(Arrays.toString(xy) + " " + ownMax);
        return m;
        /*if (xy[0] >= 0 && xy[0] <= 2 && xy[1] >= 0 && xy[1] <= 2 && ownMax != -1 && ownValuesLeft.size() > 0) {
            if ((firstPlayer && !firstPlayedPieces[ownMax]) || (!firstPlayer && !secondPlayedPieces[ownMax])) {
                if (board[xy[0]][xy[1]] == null || (board[xy[0]][xy[1]].firstPlayer() != firstPlayer
                        && board[xy[0]][xy[1]].value() < ownMax)){
                    return m;
                } else {
                    Game.printBoard(board);
                    System.out.println(Arrays.toString(xy) + " " + ownMax);
                    if (firstPlayer) {
                        System.out.println("ich bin erster Spieler");
                    } else {
                        System.out.println("ich bin zweiter Spieler");
                    }
                    System.out.println("firstPlayedPieces: " + Arrays.toString(firstPlayedPieces));
                    System.out.println("secondPlayedPieces: " + Arrays.toString(secondPlayedPieces));
                    throw new RuntimeException("Der Zug kann nicht mehr gespielt werden");
                }
            } else {
                Game.printBoard(board);
                System.out.println(Arrays.toString(xy) + " " + ownMax);
                if (firstPlayer) {
                    System.out.println("ich bin erster Spieler");
                } else {
                    System.out.println("ich bin zweiter Spieler");
                }
                System.out.println("firstPlayedPieces: " + Arrays.toString(firstPlayedPieces));
                System.out.println("secondPlayedPieces: " + Arrays.toString(secondPlayedPieces));
                throw new RuntimeException("Der Zug ist kein legalMove");
            }
        } else {
            throw new RuntimeException("Es wurde ein illegaler Zug gespielt");
        }*/
        //return makeMove(board, firstPlayer, firstPlayedPieces,secondPlayedPieces);
    }
}
