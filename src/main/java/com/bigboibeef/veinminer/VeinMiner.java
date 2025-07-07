package com.bigboibeef.veinminer;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VeinMiner implements ModInitializer {
	public static final String MOD_ID = "veinminer";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final TagKey<Block> ores = TagKey.of(Registries.BLOCK.getKey(), Identifier.of("veinminer", "ores"));

	public static final List<TagKey<Block>> ORE_TAGS = List.of(
			TagKey.of(Registries.BLOCK.getKey(), Identifier.of("minecraft", "coal_ores")),
			TagKey.of(Registries.BLOCK.getKey(), Identifier.of("minecraft", "copper_ores")),
			TagKey.of(Registries.BLOCK.getKey(), Identifier.of("minecraft", "diamond_ores")),
			TagKey.of(Registries.BLOCK.getKey(), Identifier.of("minecraft", "emerald_ores")),
			TagKey.of(Registries.BLOCK.getKey(), Identifier.of("minecraft", "gold_ores")),
			TagKey.of(Registries.BLOCK.getKey(), Identifier.of("minecraft", "iron_ores")),
			TagKey.of(Registries.BLOCK.getKey(), Identifier.of("minecraft", "lapis_ores")),
			TagKey.of(Registries.BLOCK.getKey(), Identifier.of("minecraft", "redstone_ores"))
	);

	@Override
	public void onInitialize() {
		LOGGER.info("Initialized BIGBOIBEEF's Vein Miner.");
		PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
			if (isOre(state)) {


				int max = 0;
				List<BlockPos> offsetsList = new ArrayList<>();
				for (int dx = -1; dx <= 1; dx++) {
					for (int dy = -1; dy <= 1; dy++) {
						for (int dz = -1; dz <= 1; dz++) {
							if (dx == 0 && dy == 0 && dz == 0) continue; // skip center
							offsetsList.add(new BlockPos(dx, dy, dz));
						}
					}
				}
				BlockPos[] OFFSETS = offsetsList.toArray(new BlockPos[0]);


				Set<BlockPos> vein = new HashSet<>();
				Set<BlockPos> frontier = new HashSet<>();

				vein.add(pos);
				frontier.add(pos);

				while (!frontier.isEmpty() && max <= 20) {
					if (vein.size() >= 100) {
						break;
					}

					Set<BlockPos> nextFrontier = new HashSet<>();

					for (BlockPos current : frontier) {
						for (BlockPos offset : OFFSETS) {
							BlockPos neighbor = current.add(offset);
							if (vein.contains(neighbor)) {
								continue;
							}

							BlockState newState = world.getBlockState(neighbor);
							if (isOre(newState)) {
								vein.add(neighbor);
								nextFrontier.add(neighbor);
							}
						}
					}

					frontier = nextFrontier;
					max++;
				}

				vein.forEach(ore -> {
					if (player instanceof ServerPlayerEntity serverPlayer) {
						serverPlayer.interactionManager.tryBreakBlock(ore);
					}
				});

			}
		});
	}

	public boolean isOre(BlockState state) {
		for (TagKey<Block> tag : ORE_TAGS) {
			if (state.isIn(tag)) return true;
		}
		return false;
	}

}
