package chessmod.blockentity;

import chessmod.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import chessmod.blockentity.GoldChessboardBlockEntity;

public class QuantumChessBoardBlockEntity extends GoldChessboardBlockEntity{
//    public QuantumChessBoardBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
//        super(Registration.QUANTUM_CHESSBOARD_BE.get(), pWorldPosition, pBlockState);
//    }

    public QuantumChessBoardBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
        super(pWorldPosition, pBlockState); // Remove the blockEntityType argument when extending GoldChessBaord
    }
    public BlockPos linkedBoardPos;

    public void setLinkedBoardPos(BlockPos pos) {
        this.linkedBoardPos = pos;
    }

    public QuantumChessBoardBlockEntity getLinkedBoard() {
        // Check if linkedBoardPos is not null
        if (linkedBoardPos != null) {
            // Retrieve the QuantumChessBoardBlockEntity from the linked position
            BlockEntity linkedEntity = level.getBlockEntity(linkedBoardPos);
            if (linkedEntity instanceof QuantumChessBoardBlockEntity) {
                return (QuantumChessBoardBlockEntity) linkedEntity;
            }
        }
        return null;
    }
}