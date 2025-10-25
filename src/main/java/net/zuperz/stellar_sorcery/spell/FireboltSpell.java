package net.zuperz.stellar_sorcery.spell;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class FireboltSpell implements ISpell {

    @Override
    public void cast(Level level, Player player) {
        if (!level.isClientSide) {
            Vec3 look = player.getLookAngle();
            Vec3 eyePos = player.getEyePosition();

            SmallFireball fireball = new SmallFireball(EntityType.SMALL_FIREBALL, level);
            fireball.setPos(eyePos.x + look.x * 1.5, eyePos.y + look.y * 1.5, eyePos.z + look.z * 1.5);

            fireball.setDeltaMovement(look.scale(0.5));
            fireball.setOwner(player);

            level.addFreshEntity(fireball);
            level.playSound(null, player.blockPosition(), SoundEvents.BLAZE_SHOOT, player.getSoundSource(), 1.0F, 1.0F);
        }
    }

    @Override
    public String getName() {
        return "firebolt";
    }
}
