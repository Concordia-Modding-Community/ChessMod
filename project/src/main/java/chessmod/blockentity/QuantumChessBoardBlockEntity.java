package chessmod.blockentity;

import chessmod.common.dom.model.chess.board.Board;
import chessmod.common.dom.model.chess.board.SerializedBoard;
import chessmod.setup.Registration;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

public class QuantumChessBoardBlockEntity extends ChessboardBlockEntity{
    public QuantumChessBoardBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
        super(Registration.QUANTUM_CHESSBOARD_BE.get(), pWorldPosition, pBlockState);
    }

    protected BlockPos linkedBoardPos;
    protected String linkedDimension;

    public void setLinkedBoard(BlockPos pos, String dimension) {
        this.linkedBoardPos = pos;
        this.linkedDimension = dimension;
        this.setChanged();
    }

    /**
     * @return true if there is a linked QuantumChessBoardBlockEntity, false otherwise
     */
    public synchronized boolean hasLinkedBoard() {
        try {//The try/catch is just to avoid any weirdness relating to the complex
             //logic of chunkloading/finding entities in getBlockEntityInDimension
             //I have no evidence that this goes sideways, but I'd hate to crash
             //someone's server because of a dumb NPE that may happen on some versions
             //of MC and not others.
            return linkedBoardPos != null &&
                    Utility.getBlockEntityInDimension(linkedDimension, linkedBoardPos) instanceof QuantumChessBoardBlockEntity;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public QuantumChessBoardBlockEntity getLinkedBoard() {
        try {
            if (hasLinkedBoard()) {
                // Retrieve the QuantumChessBoardBlockEntity from the linked dimension/position
                if(Utility.getBlockEntityInDimension(linkedDimension, linkedBoardPos) instanceof QuantumChessBoardBlockEntity linkedEntity) {
                    return linkedEntity;
                }
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void quantumImprint(QuantumChessBoardBlockEntity otherBoard) {
        // clone the internal chessboard state
        Board b = SerializedBoard.serialize(getBoard()).deSerialize();
        otherBoard.setBoard(b);
    }

    @Override
    /* This method is called when saving the
     * block entity's data (serialization? encoding?)
     * stores the linked board position coordinates as an integer array
     * within the NBT data (linkedBoardPos) */
    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);
        if(linkedBoardPos != null) {
            pTag.putIntArray("linkedBoardPos", new int[]{linkedBoardPos.getX(), linkedBoardPos.getY(), linkedBoardPos.getZ()});
            pTag.putString("linkedDimension", linkedDimension);
        }
    }
    @Override
    /* This method is called when loading the
     * block entity's data from NBT (deserialization? decoding?)
     * returns the stored linked board position from linkedBoardPos
     */
    public void load(CompoundTag pTag) {
        super.load(pTag);
        int[] lbp = pTag.getIntArray("linkedBoardPos");
        if(lbp.length != 3) {
           linkedBoardPos = null;
        } else {
            linkedBoardPos = new BlockPos(lbp[0], lbp[1], lbp[2]);
            linkedDimension = pTag.getString("linkedDimension");
        }
    }

    public static class FailureToLinkQuantumChessBoardEntityException extends Throwable {
        public FailureToLinkQuantumChessBoardEntityException(String s) {
            super(s);
        }
    }

    public void linkChessBoards(QuantumChessBoardBlockEntity second) {
        //Make sure the old boards are unlinked
        unlinkChessboards();

        // use quantumImprint to clone the board state before linking
        quantumImprint(second);
        setLinkedBoard(second.getBlockPos(), second.getDimension());
        notifyClientOfBoardChange();
        second.setLinkedBoard(getBlockPos(), getDimension());
        second.notifyClientOfBoardChange();
    }

    /**
     * This unlinks a board by setting its position and dimension to null
     */
    public void unlinkChessboards() {
        if(hasLinkedBoard()) {
            getLinkedBoard().setLinkedBoard(null, null);
            getLinkedBoard().notifyClientOfBoardChange();
        }
        setLinkedBoard(null, null);
        notifyClientOfBoardChange();
    }
}