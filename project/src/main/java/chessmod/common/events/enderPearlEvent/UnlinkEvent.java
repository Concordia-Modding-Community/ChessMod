package chessmod.common.events.enderPearlEvent;

import chessmod.block.QuantumChessBoardBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

//@Mod.EventBusSubscriber(Dist.CLIENT)
//    public class UnlinkEvent extends EnderPearlEventHandler{
//
//
//        //event when a table is broken
//        @SubscribeEvent(priority = EventPriority.HIGHEST)
//        public void onBlockBreak(BlockEvent.BreakEvent event){
//            if(event.getState().getBlock() instanceof QuantumChessBoardBlock){
//                BlockPos linkedBlock = event.getPos();
//                if(linkedChessboards.containsKey(linkedBlock)){
//                    unlinkChessboards((linkedBlock));
//                    event.getPlayer().displayClientMessage(Component.literal(" A Chess Board is destroyed and Unlinked" ), false);
//
//
//                }
//            }
//        }
//}
