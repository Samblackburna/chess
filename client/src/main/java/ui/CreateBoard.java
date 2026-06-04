package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

import static ui.EscapeSequences.*;

public class CreateBoard {
    public void draw(ChessBoard board, ChessGame.TeamColor viewSide) {
        int StartRow;
        int endRow;
        int rowDirection;
        int startCol;
        int endCol;
        int colDirection;

        if (viewSide == ChessGame.TeamColor.WHITE) {
            StartRow = 8;
            endRow = 0;
            rowDirection = -1;
            startCol = 1;
            endCol = 9;
            colDirection = 1;
        } else {
            StartRow = 1;
            endRow = 9;
            rowDirection = 1;
            startCol = 8;
            endCol = 0;
            colDirection = -1;
        }

        printColHeaders(startCol, endCol, colDirection);

        for (int row = StartRow; row != endRow; row += rowDirection) {
            System.out.print(RESET_BG_COLOR + " " + row + " ");
            for (int col = startCol; col != endCol; col += colDirection) {
                boolean lightSquare = (row + col) % 2 == 0;
                String bg;
                if (lightSquare) {
                    bg = SET_BG_COLOR_LIGHT_GREY;
                } else {
                    bg = SET_BG_COLOR_DARK_GREY;
                }
                System.out.print(bg + getPieceString(board, row, col));
            }
            System.out.println(RESET_BG_COLOR + " " + row + " ");
        }

        printColHeaders(startCol, endCol, colDirection);
        System.out.print(RESET_BG_COLOR + RESET_TEXT_COLOR);
    }

    private void printColHeaders(int colStart, int colEnd, int colStep) {
        System.out.print(RESET_BG_COLOR + "   ");
        for (int col = colStart; col != colEnd; col += colStep) {
            System.out.print(" " + (char)('a' + col - 1) + " ");
        }
        System.out.println();
    }

    private String getPieceString(ChessBoard board, int row, int col) {
        ChessPiece piece = board.getPiece(new ChessPosition(row, col));
        if (piece == null) {
            return EMPTY;
        }
        String color;
        if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
            color = SET_TEXT_COLOR_WHITE;
        } else {
            color = SET_TEXT_COLOR_BLACK;
        }

        String symbol;
        if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
            symbol = switch (piece.getPieceType()) {
                case KING   -> WHITE_KING;
                case QUEEN  -> WHITE_QUEEN;
                case BISHOP -> WHITE_BISHOP;
                case KNIGHT -> WHITE_KNIGHT;
                case ROOK   -> WHITE_ROOK;
                case PAWN   -> WHITE_PAWN;
            };
        } else {
            symbol = switch (piece.getPieceType()) {
                case KING   -> BLACK_KING;
                case QUEEN  -> BLACK_QUEEN;
                case BISHOP -> BLACK_BISHOP;
                case KNIGHT -> BLACK_KNIGHT;
                case ROOK   -> BLACK_ROOK;
                case PAWN   -> BLACK_PAWN;
            };
        }
        return color + symbol;
    }
}
