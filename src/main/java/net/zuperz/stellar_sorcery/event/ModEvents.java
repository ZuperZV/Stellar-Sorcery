package net.zuperz.stellar_sorcery.event;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.zuperz.stellar_sorcery.StellarSorcery;
import net.zuperz.stellar_sorcery.block.entity.custom.AstralAltarBlockEntity;

import java.util.stream.Stream;

@EventBusSubscriber(modid = StellarSorcery.MOD_ID)
public class ModEvents {

    @SubscribeEvent
    public static void onAnimalSacrifice(LivingDeathEvent event) {
        Level level = event.getEntity().level();
        AABB blocksAround = AABB.ofSize(event.getEntity().blockPosition().getCenter(), 5, 3, 5);
        Stream<BlockPos> position = BlockPos.betweenClosedStream(blocksAround);

        position.forEach(pos -> {
            if(level.getBlockEntity(pos) instanceof AstralAltarBlockEntity pedestalBlockEntity) {
                pedestalBlockEntity.setSacrificedEntity(event.getEntity().getType());
            }
        });
    }
}
