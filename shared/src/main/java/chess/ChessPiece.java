package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece piece = board.getPiece(myPosition);
        if (piece.getPieceType() == PieceType.BISHOP) {
            Collection<ChessMove> moves = new ArrayList<>();
            int[][] directions = {{1,1},{1,-1},{-1,1},{-1,-1}};
            for (int[] dir : directions) {
                int row = myPosition.getRow();
                int column = myPosition.getColumn();

                while (true) {
                    row += dir[0];
                    column += dir[1];

                    if (row < 1 || row > 8 || column < 1 || column > 8) break;

                    ChessPosition newPosition = new ChessPosition(row, column);
                    ChessPiece target = board.getPiece(newPosition);
                    if (target == null) {
                        moves.add(new ChessMove(myPosition, newPosition, null));
                    } else {
                        if (target.getTeamColor() != piece.getTeamColor()) {
                            moves.add(new ChessMove(myPosition, newPosition, null));
                        }
                        break;
                    }
                }
            }
            return moves;
            // return List.of(new ChessMove(new ChessPosition(5,4), new ChessPosition(1, 8), null));
        } else if (piece.getPieceType() == PieceType.PAWN) {
            Collection<ChessMove> moves = new ArrayList<>();
            int row = myPosition.getRow();
            int column = myPosition.getColumn();
            int direction;
            if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                direction = 1;} else {direction = -1;}
            int startRow;
            if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                startRow = 2;} else {startRow = 7;}
            int promotionRow;
            if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                promotionRow = 8;} else {
                promotionRow = 1;
            }

            // just normal movement code
            ChessPosition oneForward = new ChessPosition(row + direction, column);
            if (board.getPiece(oneForward) == null) {
                if (row + direction == promotionRow) {
                    moves.add(new ChessMove(myPosition, oneForward, PieceType.QUEEN));
                    moves.add(new ChessMove(myPosition, oneForward, PieceType.ROOK));
                    moves.add(new ChessMove(myPosition, oneForward, PieceType.BISHOP));
                    moves.add(new ChessMove(myPosition, oneForward, PieceType.KNIGHT));
                } else {
                    moves.add(new ChessMove(myPosition, oneForward, null));
                }

                if (row == startRow) {
                    ChessPosition twoForward = new ChessPosition(row + direction * 2, column);
                    if (board.getPiece(twoForward) == null) {
                        moves.add(new ChessMove(myPosition, twoForward, null));
                    }
                }
            }

            // diagonal capture code block
            int[] captureColumns = {column - 1, column + 1};
            for (int captureCol : captureColumns) {
                if (captureCol < 1 || captureCol > 8) continue;
                ChessPosition diagonal = new ChessPosition(row + direction, captureCol);
                ChessPiece target = board.getPiece(diagonal);
                if (target != null && target.getTeamColor() != piece.getTeamColor()) {
                    if (row + direction == promotionRow) {
                        moves.add(new ChessMove(myPosition, diagonal, PieceType.QUEEN));
                        moves.add(new ChessMove(myPosition, diagonal, PieceType.ROOK));
                        moves.add(new ChessMove(myPosition, diagonal, PieceType.BISHOP));
                        moves.add(new ChessMove(myPosition, diagonal, PieceType.KNIGHT));
                    } else {
                        moves.add(new ChessMove(myPosition, diagonal, null));
                    }
                }
            }
            return moves;
        }
        else if (piece.getPieceType() == PieceType.KNIGHT) {
            Collection<ChessMove> moves = new ArrayList<>();
            for (int row = 1; row <= 8; row++) {
                for (int column = 1; column <= 8; column++) {
                    int rowDiff = Math.abs(row - myPosition.getRow());
                    int columnDiff = Math.abs(column - myPosition.getColumn());

                    ChessPosition newPosition = new ChessPosition(row, column);
                    ChessPiece target = board.getPiece(newPosition);
                    if ((rowDiff == 2 && columnDiff == 1) || (rowDiff == 1 && columnDiff == 2)) {
                        if (target == null) {
                            moves.add(new ChessMove(myPosition, newPosition, null));
                        } else {
                            if (target.getTeamColor() != piece.getTeamColor()) {
                                moves.add(new ChessMove(myPosition, newPosition, null));
                            }
                        }
                    }
                }
            }
            return moves;
        }
        else if (piece.getPieceType() == PieceType.ROOK) {
            Collection<ChessMove> moves = new ArrayList<>();
            int[][] directions = {{1,0},{0,1},{-1,0},{0,-1}};
            for (int[] dir : directions) {
                int row = myPosition.getRow();
                int column = myPosition.getColumn();

                while (true) {
                    row += dir[0];
                    column += dir[1];

                    if (row < 1 || row > 8 || column < 1 || column > 8) break;

                    ChessPosition newPosition = new ChessPosition(row, column);
                    ChessPiece target = board.getPiece(newPosition);
                    if (target == null) {
                        moves.add(new ChessMove(myPosition, newPosition, null));
                    } else {
                        if (target.getTeamColor() != piece.getTeamColor()) {
                            moves.add(new ChessMove(myPosition, newPosition, null));
                        }
                        break;
                    }
                }
            }
            return moves;
        }
        else if (piece.getPieceType() == PieceType.QUEEN) {
            Collection<ChessMove> moves = new ArrayList<>();
            int[][] directions = {{1,0},{0,1},{-1,0},{0,-1},{1,1},{1,-1},{-1,1},{-1,-1}};
            for (int[] dir : directions) {
                int row = myPosition.getRow();
                int column = myPosition.getColumn();

                while (true) {
                    row += dir[0];
                    column += dir[1];

                    if (row < 1 || row > 8 || column < 1 || column > 8) break;

                    ChessPosition newPosition = new ChessPosition(row, column);
                    ChessPiece target = board.getPiece(newPosition);
                    if (target == null) {
                        moves.add(new ChessMove(myPosition, newPosition, null));
                    } else {
                        if (target.getTeamColor() != piece.getTeamColor()) {
                            moves.add(new ChessMove(myPosition, newPosition, null));
                        }
                        break;
                    }
                }
            }
            return moves;
        }
        else if (piece.getPieceType() == PieceType.KING) {
            Collection<ChessMove> moves = new ArrayList<>();
            for (int row = 1; row <= 8; row++) {
                for (int column = 1; column <= 8; column++) {
                    int rowDiff = Math.abs(row - myPosition.getRow());
                    int columnDiff = Math.abs(column - myPosition.getColumn());

                    ChessPosition newPosition = new ChessPosition(row, column);
                    ChessPiece target = board.getPiece(newPosition);
                    if ((rowDiff == 0 && columnDiff == 1) || (rowDiff == 1 && columnDiff == 0) || (rowDiff == 1 && columnDiff == 1)) {
                        if (target == null) {
                            moves.add(new ChessMove(myPosition, newPosition, null));
                        } else {
                            if (target.getTeamColor() != piece.getTeamColor()) {
                                moves.add(new ChessMove(myPosition, newPosition, null));
                            }
                        }
                    }
                }
            }
            return moves;
        }
        return List.of();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }
}