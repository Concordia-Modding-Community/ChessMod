package chessmod.blockentity;

import chessmod.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class AIChessboardBlockEntity extends GoldChessboardBlockEntity {

	public AIChessboardBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
		super(Registration.AI_CHESSBOARD_BE.get(), pWorldPosition, pBlockState);
	}
	
}
