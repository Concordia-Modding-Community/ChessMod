package chessmod.blockentity;

import chessmod.block.QuantumChessBoardBlock;
import chessmod.common.dom.model.chess.board.Board;
import chessmod.common.dom.model.chess.board.SerializedBoard;
import chessmod.setup.Registration;
import com.mojang.datafixers.util.Either;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class QuantumChessBoardBlockEntity extends ChessboardBlockEntity{
    public QuantumChessBoardBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
        super(Registration.QUANTUM_CHESSBOARD_BE.get(), pWorldPosition, pBlockState);
    }

    protected BlockPos linkedBoardPos;
    protected String linkedDimension;

    public void setLinkedBoardPos(BlockPos pos) {
        setLinkedBoardPos(pos, level.dimension().location().toString());
    }


    public static ServerLevel getServerLevel(String dimensionLocationString) {
        ResourceLocation dimensionLocation = new ResourceLocation(dimensionLocationString);
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        ResourceKey<Level> dimensionKey = ResourceKey.create(Registries.DIMENSION, dimensionLocation);
        return server.getLevel(dimensionKey);
    }

    public static BlockEntity getBlockEntityInDimension(String dimensionLocationString, BlockPos blockPos) {
        ServerLevel targetLevel = getServerLevel(dimensionLocationString);
        System.out.println("targetLevel: " + targetLevel);
        System.out.println("blockPos: " + blockPos);
        if (targetLevel != null && blockPos != null) {
            // Load the chunk containing the block position
            ChunkPos chunkPos = new ChunkPos(blockPos);

            CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> futureChunk = targetLevel.getChunkSource().getChunkFuture(chunkPos.x, chunkPos.z, ChunkStatus.FULL, true);

            try {
                Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure> chunk = futureChunk.get();
                System.out.println("No, we got in here!");
                return targetLevel.getBlockEntity(blockPos);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        System.out.println("No, we got in here!");
        return null;
    }

    public void setLinkedBoardPos(BlockPos pos, String dimension) {
        System.out.println("Setting linked board pos to " + pos);

        this.linkedBoardPos = pos;
        System.out.println("TWITCH STREAM!!! " + dimension);
        this.linkedDimension = dimension;
        System.out.println("TWITCH This Chessboard!!! " + getBlockEntityInDimension(linkedDimension, linkedBoardPos));
        this.setChanged();
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
        if (getLinkedBoard() != null && !(this.getBlockPos().equals(getLinkedBoardPos()) && level.dimension().location().toString().equals(linkedDimension))) {
            System.out.println("checking for a linked board");
            if(getLinkedBoard().getLinkedBoardPos() != this.getBlockPos()) {
                return true;
            } else {
                /*
                 * Until we're happily certain that boards can't be unlinked badly, we need to make sure any
                 * problematic stuff is taken care of.
                 *
                 * In theory, we could clean everything, but since every board should be checking itself and
                 * this is called inside a synchronized method, let's just clean our immediate selves.
                 */
                //Too bad this solution messes stuff up...
                //this.linkedBoardPos = null;
                notifyClientOfBoardChange();
            }
        } else {
            System.out.println("checking for a linked board, but not finding one.");
            System.out.println(getLinkedBoardPos());
        }
        return false;
    }

    public QuantumChessBoardBlockEntity getLinkedBoardEntity() {
        if (hasLinkedBoard()) {
            // Retrieve the QuantumChessBoardBlockEntity from the linked position
            if(getBlockEntityInDimension(linkedDimension, linkedBoardPos) instanceof QuantumChessBoardBlockEntity linkedEntity) {
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
            pTag.putString("linkedDimension", linkedDimension);
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
           System.out.println("Nuking linkedBoardPos");
           linkedBoardPos = null;
        } else {
            linkedBoardPos = new BlockPos(lbp[0], lbp[1], lbp[2]);
            linkedDimension = pTag.getString("linkedDimension");
        }
    }

    public BlockPos getLinkedBoardPos() {
        return linkedBoardPos;
    }

    public QuantumChessBoardBlockEntity getLinkedBoard() {
        if(linkedBoardPos == null) return null;
        BlockEntity be = getBlockEntityInDimension(linkedDimension, linkedBoardPos);
        if(be instanceof QuantumChessBoardBlockEntity qcbe) {
            return qcbe;
        }
        return null;
    }

    public static class FailureToLinkQuantumChessBoardEntityException extends Throwable {
        public FailureToLinkQuantumChessBoardEntityException(String s) {
            super(s);
        }
    }
    public static class NotALinkedQuantumChessBoardEntityException extends Throwable {
    }

    /**
     * This unlinks a board by setting its
     *
     * TODO: We're leaving the linkedDimension. This will only lead to suffering, fix it eventually.
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