package chessmod.block;

import javax.annotation.Nullable;

import chessmod.blockentity.ChessboardBlockEntity;
import chessmod.blockentity.QuantumChessBoardBlockEntity;
import chessmod.client.gui.entity.GoldChessboardGui;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
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
            System.out.println("Event fired for main hand with ender pearl");
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
                        player.displayClientMessage(Component.literal("First chessboard selected at: " + pos), false);
                        // spawn particle effect
                        //level.addParticle(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 0, 0, 0);

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

                        // spawn particle effet
                        level.addParticle(ParticleTypes.END_ROD, pos.getX() , pos.getY() , pos.getZ() , 0, 0, 0);


                    } else {
                        // Handle error if Ids don't match or not a chessboard block
                        player.displayClientMessage(Component.literal("Selected boards don't match!"), false);
                    }
                }
            } catch (QuantumChessBoardBlockEntity.FailureToLinkQuantumChessBoardEntityException e) {
                player.displayClientMessage(Component.literal(e.getMessage()), false);
            }

            return InteractionResult.PASS;
        } else if (level.isClientSide && !heldItem.is(Items.ENDER_PEARL)) {
            super.use(state, level, pos, player, hand, pHit);
        }

        return InteractionResult.SUCCESS;
    }

    private static String formatBlockPos(BlockPos pos) {
        return String.format("(%d, %d, %d)", pos.getX(), pos.getY(), pos.getZ());
    }

    private void linkChessboards(Player player, Level level, BlockPos firstPosition, BlockPos secondPosition) throws QuantumChessBoardBlockEntity.FailureToLinkQuantumChessBoardEntityException {
        if(firstPosition.equals(secondPosition)) throw new QuantumChessBoardBlockEntity.FailureToLinkQuantumChessBoardEntityException("Can't link to originating board.");
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
                player.displayClientMessage(Component.literal(String.format("The board at %s is now quantum-linked to the board at %s.",
                        formatBlockPos(firstPosition), formatBlockPos(secondPosition))), false);
                return;
            }
        }
        throw new QuantumChessBoardBlockEntity.FailureToLinkQuantumChessBoardEntityException("Both linked boards must be Quantum Chess Boards.");
    }

}
