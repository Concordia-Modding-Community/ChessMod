package chessmod.blockentity;

import chessmod.common.dom.model.chess.board.Board;
import chessmod.common.dom.model.chess.board.SerializedBoard;
import chessmod.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;

public class QuantumChessBoardBlockEntity extends ChessboardBlockEntity{
    public QuantumChessBoardBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
        super(Registration.QUANTUM_CHESSBOARD_BE.get(), pWorldPosition, pBlockState);
    }

    protected BlockPos linkedBoardPos;

    public void setLinkedBoardPos(BlockPos pos) {
        this.linkedBoardPos = pos;
    }

    public void quantumImprint(QuantumChessBoardBlockEntity otherBoard) {
        // clone the internal chessboard state
        Board b = SerializedBoard.serialize(getBoard()).deSerialize();
        otherBoard.setBoard(b);
    }

    /**
     * This tests a variety of things that can be considered as not having a properly linked board.
     * This method is not pure. If it detects corruption, or other weirdness, it'll clean house a bit.
     * For Example, if boards are linked, but not to each other, it'll unlink the current board and leave
     * the other board to take care of itself, likely when it calls this method.
     *
     * @return true if there is a linked QuantumChessBoardBlockEntity, false otherwise
     */
    public synchronized boolean hasLinkedBoard() {
        try {
            if (getLinkedBoardPos() != null && !this.getBlockPos().equals(getLinkedBoardPos())) {
                System.out.println("checking for a linked board");
                if ((Objects.requireNonNull(level).getBlockEntity(getLinkedBoardPos())
                        instanceof QuantumChessBoardBlockEntity linkedEntity)) {
                    if(linkedEntity.getLinkedBoardPos() != this.getBlockPos()) {
                        return true;
                    } else {
                        /*
                         * Until we're happily certain that boards can't be unlinked badly, we need to make sure any
                         * problematic stuff is taken care of.
                         *
                         * In theory, we could clean everything, but since every board should be checking itself and
                         * this is called inside a synchronized method, let's just clean our immediate selves.
                         */
                        this.linkedBoardPos = null;
                        notifyClientOfBoardChange();
                    }
                } else {
                    throw new NotALinkedQuantumChessBoardEntityException();
                }
            } else {
                System.out.println("checking for a linked board, but not finding one.");
                System.out.println(getLinkedBoardPos());
            }
        } catch (NullPointerException | NotALinkedQuantumChessBoardEntityException e) {
            this.linkedBoardPos = null;
            notifyClientOfBoardChange();
        }
        return false;
    }

    public QuantumChessBoardBlockEntity getLinkedBoardEntity() {
        if (hasLinkedBoard()) {
            // Retrieve the QuantumChessBoardBlockEntity from the linked position
            if(Objects.requireNonNull(level).getBlockEntity(getLinkedBoardPos()) instanceof QuantumChessBoardBlockEntity linkedEntity){
                return linkedEntity;
            }
        }
        return null;
    }

    @Override
    /* This methis is called when saving the
     * block entity's data (serialization? encoding?)
     * stores the linked board position coordinates as an integer array
     * within the NBT data (linkedBoardPos) */
    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);
        if(getLinkedBoardPos() != null) {
            pTag.putIntArray("linkedBoardPos", new int[]{getLinkedBoardPos().getX(), getLinkedBoardPos().getY(), getLinkedBoardPos().getZ()});
        }
    }
    @Override
    /* This method is called when loading the
     * block entity's data from NBT (deserialisation? decoding?)
     * returns the stored linked board position from linkedBoardPos
     */
    public void load(CompoundTag pTag) {
        super.load(pTag);
        int[] lbp = pTag.getIntArray("linkedBoardPos");
        if(lbp.length != 3) {
           linkedBoardPos = null;
        } else {
            linkedBoardPos = new BlockPos(lbp[0], lbp[1], lbp[2]);
        }
    }

    public BlockPos getLinkedBoardPos() {
        return linkedBoardPos;
    }

    public static class FailureToLinkQuantumChessBoardEntityException extends Throwable {
        public FailureToLinkQuantumChessBoardEntityException(String s) {
            super(s);
        }
    }
    public static class NotALinkedQuantumChessBoardEntityException extends Throwable {
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        unlinkChessboard();
    }

    /**
     * This unlinks a board by setting its
     */
    public void unlinkChessboard() {
        if(hasLinkedBoard()) {
            getLinkedBoardEntity().setLinkedBoardPos(null);
            getLinkedBoardEntity().notifyClientOfBoardChange();
        }
        setLinkedBoardPos(null);
        notifyClientOfBoardChange();
    }
}