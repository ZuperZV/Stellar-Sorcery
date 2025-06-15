package net.zuperz.stellar_sorcery.entity.custom;

import net.minecraft.network.protocol.Packet;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

public class SigilOrbEntity extends Mob {

    public SigilOrbEntity(EntityType<? extends Mob> type, Level level) {
        super(type, level);
        this.noCulling = true;
        this.noPhysics = false;
    }

    @Override
    protected void registerGoals() {}

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(Entity entityIn) {}

    @Override
    public boolean isInvulnerable() {
        return true;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float damageMultiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 1.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D);
    }

    @Override
    public boolean canCollideWith(Entity other) {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public void die(DamageSource cause) {
    }
}