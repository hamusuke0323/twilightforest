package twilightforest.loot.conditions;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import twilightforest.capabilities.CapabilityList;
import twilightforest.capabilities.giant_pick.GiantPickMineCapability;
import twilightforest.init.TFLoot;

import java.util.Set;

public record GiantPickUsedCondition(LootContext.EntityTarget target) implements LootItemCondition {

    public static final Codec<GiantPickUsedCondition> CODEC = RecordCodecBuilder.create(instance -> instance.group(LootContext.EntityTarget.CODEC.fieldOf("entity").forGetter(o -> o.target)).apply(instance, GiantPickUsedCondition::new));

    @Override
    public LootItemConditionType getType() {
        return TFLoot.GIANT_PICK_USED_CONDITION.get();
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(this.target.getParam());
    }

    @Override
    public boolean test(LootContext context) {
        if (context.getParamOrNull(this.target.getParam()) instanceof Player player) {
            GiantPickMineCapability capability = player.getCapability(CapabilityList.GIANT_PICK_MINE).resolve().orElse(null);
            if (capability != null) {
                return player.level().getGameTime() == capability.getMining() && capability.canMakeGiantBlock();
            }
        }
        return false;
    }

    public static LootItemCondition.Builder builder(LootContext.EntityTarget target) {
        return () -> new GiantPickUsedCondition(target);
    }
}