package twilightforest.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import twilightforest.entity.boss.IBossLootBuffer;
import twilightforest.init.TFAdvancements;

import java.util.List;
import java.util.UUID;

public interface MultiplayerFlexibleEnemy extends IBossLootBuffer {

	List<ServerPlayer> getQualifiedPlayers();

	default void maybeAddQualifiedPlayer(Entity entity) {
		if (entity instanceof ServerPlayer player && !this.getQualifiedPlayers().contains(player)) {
			this.getQualifiedPlayers().add(player);
		}
	}

	default void grantGroupAdvancement(LivingEntity boss) {
		for (ServerPlayer player : this.getQualifiedPlayers()) {
			TFAdvancements.HURT_BOSS.get().trigger(player, boss);
		}
	}
}
