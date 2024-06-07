package chessmod.block;

import javax.annotation.Nullable;

import chessmod.blockentity.ChessboardBlockEntity;
import chessmod.blockentity.QuantumChessBoardBlockEntity;
import chessmod.client.gui.entity.GoldChessboardGui;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.server.ServerLifecycleHooks;


public class QuantumChessBoardBlock extends GoldChessboardBlock {
    public QuantumChessBoardBlock(){
        super();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state){
        return new QuantumChessBoardBlockEntity(pos, state);
    }
    
    @OnlyIn(Dist.CLIENT)
    @Override
    protected void openGui(final Level levelIn, final BlockPos pos){
        final BlockEntity blockEntity = levelIn.getBlockEntity(pos);
        if (blockEntity instanceof ChessboardBlockEntity) {
            Minecraft.getInstance().setScreen(new GoldChessboardGui((ChessboardBlockEntity)blockEntity));
        }
    }

    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult pHit) {

        ItemStack heldItem = player.getMainHandItem();
        if (!level.isClientSide && heldItem.is(Items.ENDER_PEARL)) {
            try {
                CompoundTag nbtData = heldItem.getOrCreateTag();

                if (nbtData.getInt("BlockPosX") == 0) {
                    // first board selection
                    QuantumChessBoardBlockEntity blockEntity = (QuantumChessBoardBlockEntity) level.getBlockEntity(pos);
                    if (blockEntity != null) {
                        nbtData.putInt("BlockPosX", pos.getX());
                        nbtData.putInt("BlockPosY", pos.getY());
                        nbtData.putInt("BlockPosZ", pos.getZ());
                        nbtData.putString("Dimension", level.dimension().location().toString());
                        heldItem.setTag(nbtData);
                        player.displayClientMessage(Component.translatable("chessmod.quantum.first_notification", pos.getX(), pos.getY(), pos.getZ(), level.dimension().location().toString()), false);
                    }
                } else {
                    // second boardselection
                    BlockPos firstPosition = new BlockPos(
                            nbtData.getInt("BlockPosX"),
                            nbtData.getInt("BlockPosY"),
                            nbtData.getInt("BlockPosZ"));
                    String firstDimension = nbtData.getString("Dimension");

                    QuantumChessBoardBlockEntity secondBoardEntity = (QuantumChessBoardBlockEntity) level.getBlockEntity(pos);
                    if (secondBoardEntity != null) {
                        // id match link the chessboards
                        linkChessboards(player, firstPosition, firstDimension, pos, level.dimension().location().toString());
                        player.getItemInHand(InteractionHand.MAIN_HAND).shrink(1);
                        // clear stored data for next selection
                        nbtData.remove("BlockPosX");
                        nbtData.remove("BlockPosY");
                        nbtData.remove("BlockPosZ");
                        nbtData.remove("Dimension");
                        heldItem.setTag(nbtData);
                    } else {
                        // Handle error if Ids don't match or not a chessboard block
                        player.displayClientMessage(Component.translatable("chessmod.quantum.unmatched_board_notification"), false);
                    }
                }
            } catch (QuantumChessBoardBlockEntity.FailureToLinkQuantumChessBoardEntityException e) {
                player.displayClientMessage(Component.translatable(e.getMessage()), false);
            }

            return InteractionResult.PASS;
        } else if (level.isClientSide && !heldItem.is(Items.ENDER_PEARL)) {
            super.use(state, level, pos, player, hand, pHit);
        }

        return InteractionResult.SUCCESS;
    }

public static ServerLevel getServerLevel(String dimensionLocationString) {
    ResourceLocation dimensionLocation = new ResourceLocation(dimensionLocationString);
    MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
    ResourceKey<Level> dimensionKey = ResourceKey.create(Registries.DIMENSION, dimensionLocation);
    return server.getLevel(dimensionKey);
}

public static BlockEntity getBlockEntityInDimension(String dimensionLocationString, BlockPos blockPos) {
    ServerLevel targetLevel = getServerLevel(dimensionLocationString);
    if (targetLevel != null && blockPos != null) {
        // Load the chunk containing the block position
        ChunkPos chunkPos = new ChunkPos(blockPos);
        ChunkAccess chunk = targetLevel.getChunk(chunkPos.x, chunkPos.z, ChunkStatus.FULL, true);
        if (chunk != null) {
            return targetLevel.getBlockEntity(blockPos);
        }
    }
    return null;
}


    private void linkChessboards(Player player, BlockPos firstPosition, String firstDimension, BlockPos secondPosition, String secondDimension) throws QuantumChessBoardBlockEntity.FailureToLinkQuantumChessBoardEntityException {
        if(firstPosition.equals(secondPosition) && firstDimension.equals(secondDimension)) throw new QuantumChessBoardBlockEntity.FailureToLinkQuantumChessBoardEntityException("chessmod.quantum.circular_linkage_notification");

        if(getBlockEntityInDimension(firstDimension, firstPosition) instanceof QuantumChessBoardBlockEntity firstEntity){
            if(getBlockEntityInDimension(secondDimension, secondPosition) instanceof QuantumChessBoardBlockEntity secondEntity){
                //Make sure the old boards are unlinked
                firstEntity.unlinkChessboard();
                secondEntity.unlinkChessboard();

                // use quantumImprint to clone the board state before linking
                firstEntity.quantumImprint(secondEntity);
                firstEntity.setLinkedBoardPos(secondPosition, secondDimension);
                firstEntity.notifyClientOfBoardChange();
                secondEntity.setLinkedBoardPos(firstPosition, firstDimension);
                secondEntity.notifyClientOfBoardChange();
                player.displayClientMessage(Component.translatable("chessmod.quantum.second_notification",
                        firstPosition.getX(), firstPosition.getY(), firstPosition.getZ(), firstDimension,
                        secondPosition.getX(), secondPosition.getY(), secondPosition.getZ(), secondDimension), false);
                return;
            }
        }
        throw new QuantumChessBoardBlockEntity.FailureToLinkQuantumChessBoardEntityException("chessmod.quantum.unmatched_board_notification");
    }

    @Override
    public void onRemove(BlockState oldState, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        super.onRemove(oldState, world, pos, newState, isMoving);
        if (!oldState.is(newState.getBlock())) {
            // Custom logic when the block is broken or replaced
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof QuantumChessBoardBlockEntity qcbe) {
                qcbe.unlinkChessboard();
            }
        }
    }

}
