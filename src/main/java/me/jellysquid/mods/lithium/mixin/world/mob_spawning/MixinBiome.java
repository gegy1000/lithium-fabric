package me.jellysquid.mods.lithium.mixin.world.mob_spawning;

import com.google.common.collect.Maps;
import me.jellysquid.mods.lithium.common.util.collections.HashedList;
import net.minecraft.entity.EntityCategory;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Mixin(Biome.class)
public class MixinBiome {
    @Mutable
    @Shadow
    @Final
    private Map<EntityCategory, List<Biome.SpawnEntry>> spawns;

    /**
     * Re-initialize the spawn category lists with a much faster backing collection type for enum keys. This provides
     * a modest speed-up for mob spawning as {@link Biome#getEntitySpawnList(EntityCategory)} is a rather hot method.
     * <p>
     * Additionally, the list containing each spawn entry is modified to include a hash table for lookups, making them
     * O(1) instead of O(n) and providing another boost when lists get large. Since a simple wrapper type is used, this
     * should provide good compatibility with other mods which modify spawn entries.
     */
    @Inject(method = "<init>", at = @At("RETURN"))
    private void reinit(Biome.Settings settings, CallbackInfo ci) {
        Map<EntityCategory, List<Biome.SpawnEntry>> spawns = Maps.newEnumMap(EntityCategory.class);

        for (Map.Entry<EntityCategory, List<Biome.SpawnEntry>> entry : this.spawns.entrySet()) {
            spawns.put(entry.getKey(), HashedList.wrapper(entry.getValue()));
        }

        this.spawns = spawns;
    }
}
