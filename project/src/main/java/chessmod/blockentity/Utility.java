package chessmod.blockentity;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Utility {
    public static ServerLevel getServerLevel(String dimensionLocationString) {
        ResourceLocation dimensionLocation = new ResourceLocation(dimensionLocationString);
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        ResourceKey<Level> dimensionKey = ResourceKey.create(Registries.DIMENSION, dimensionLocation);
        return server.getLevel(dimensionKey);
    }

    public static BlockEntity getBlockEntityInDimension(String dimensionLocationString, BlockPos blockPos) {
        ServerLevel targetLevel = getServerLevel(dimensionLocationString);
        if (targetLevel != null && blockPos != null) {

            // Load the chunk containing the block position and make sure it's loaded before we proceed
            ChunkPos chunkPos = new ChunkPos(blockPos);
            CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> futureChunk = targetLevel.getChunkSource().getChunkFuture(chunkPos.x, chunkPos.z, ChunkStatus.FULL, true);

            try {
                Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure> chunk = futureChunk.get();
                return targetLevel.getBlockEntity(blockPos);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
