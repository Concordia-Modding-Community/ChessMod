package chessmod.block;

import javax.annotation.Nullable;

import chessmod.blockentity.ChessboardBlockEntity;
import chessmod.blockentity.QuantumChessBoardBlockEntity;
import chessmod.client.gui.entity.GoldChessboardGui;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;


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
                        heldItem.setTag(nbtData);
                        player.displayClientMessage(Component.translatable("chessmod.quantum.first_notification", pos.getX(), pos.getY(), pos.getZ()), false);
                    }
                } else {
                    // second boardselection
                    BlockPos firstPosition = new BlockPos(
                            nbtData.getInt("BlockPosX"),
                            nbtData.getInt("BlockPosY"),
                            nbtData.getInt("BlockPosZ"));

                    QuantumChessBoardBlockEntity secondBoardEntity = (QuantumChessBoardBlockEntity) level.getBlockEntity(pos);
                    if (secondBoardEntity != null) {
                        // id match link the chessboards
                        linkChessboards(player, level, firstPosition, pos);
                        player.getItemInHand(InteractionHand.MAIN_HAND).shrink(1);
                        // clear stored data for next selection
                        nbtData.remove("BlockPosX");
                        nbtData.remove("BlockPosY");
                        nbtData.remove("BlockPosZ");
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

    private void linkChessboards(Player player, Level level, BlockPos firstPosition, BlockPos secondPosition) throws QuantumChessBoardBlockEntity.FailureToLinkQuantumChessBoardEntityException {
        if(firstPosition.equals(secondPosition)) throw new QuantumChessBoardBlockEntity.FailureToLinkQuantumChessBoardEntityException("chessmod.quantum.circular_linkage_notification");
        if(level.getBlockEntity(firstPosition) instanceof QuantumChessBoardBlockEntity firstEntity){
            if(level.getBlockEntity(secondPosition) instanceof QuantumChessBoardBlockEntity secondEntity){
                //Make sure the old boards are unlinked
                firstEntity.unlinkChessboard();
                secondEntity.unlinkChessboard();

                // use quantumImprint to clone the board state before linking
                firstEntity.quantumImprint(secondEntity);
                firstEntity.setLinkedBoardPos(secondPosition);
                firstEntity.notifyClientOfBoardChange();
                secondEntity.setLinkedBoardPos(firstPosition);
                secondEntity.notifyClientOfBoardChange();
                player.displayClientMessage(Component.translatable("chessmod.quantum.second_notification",
                        firstPosition.getX(), firstPosition.getY(), firstPosition.getZ(),
                        secondPosition.getX(), secondPosition.getY(), secondPosition.getZ()), false);
                return;
            }
        }
        throw new QuantumChessBoardBlockEntity.FailureToLinkQuantumChessBoardEntityException("chessmod.quantum.unmatched_board_notification");
    }

}
