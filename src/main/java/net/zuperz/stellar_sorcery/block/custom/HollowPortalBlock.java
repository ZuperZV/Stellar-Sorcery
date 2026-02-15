package net.zuperz.stellar_sorcery.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.zuperz.stellar_sorcery.StellarSorcery;
import net.zuperz.stellar_sorcery.worldgen.portal.HollowPortalShape;

import java.util.Optional;

public class HollowPortalBlock extends NetherPortalBlock {
    public HollowPortalBlock() {
        super(BlockBehaviour.Properties.of().noCollission().randomTicks().pushReaction(PushReaction.BLOCK).strength(-1.0F).sound(SoundType.GLASS).lightLevel(s -> 0).noLootTable());
    }

    @Override
    public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
    }

    public static void portalSpawn(Level world, BlockPos pos) {
        Optional<HollowPortalShape> optional = HollowPortalShape.findEmptyHollowPortalShape(world, pos, Direction.Axis.X);
        if (optional.isPresent()) {
            optional.get().createPortalBlocks();
        }
    }

    @Override
    public BlockState updateShape(BlockState p_54928_, Direction p_54929_, BlockState p_54930_, LevelAccessor p_54931_, BlockPos p_54932_, BlockPos p_54933_) {
        Direction.Axis direction$axis = p_54929_.getAxis();
        Direction.Axis direction$axis1 = p_54928_.getValue(AXIS);
        boolean flag = direction$axis1 != direction$axis && direction$axis.isHorizontal();

        return !flag && !p_54930_.is(this) && !(new HollowPortalShape(p_54931_, p_54932_, direction$axis1)).isComplete() ? Blocks.AIR.defaultBlockState()
                : super.updateShape(p_54928_, p_54929_, p_54930_, p_54931_, p_54932_, p_54933_);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
        for (int i = 0; i < 4; i++) {
            double px = pos.getX() + random.nextFloat();
            double py = pos.getY() + random.nextFloat();
            double pz = pos.getZ() + random.nextFloat();
            double vx = (random.nextFloat() - 0.5) / 2.;
            double vy = (random.nextFloat() - 0.5) / 2.;
            double vz = (random.nextFloat() - 0.5) / 2.;
            int j = random.nextInt(4) - 1;
            if (world.getBlockState(pos.west()).getBlock() != this && world.getBlockState(pos.east()).getBlock() != this) {
                px = pos.getX() + 0.5 + 0.25 * j;
                vx = random.nextFloat() * 2 * j;
            } else {
                pz = pos.getZ() + 0.5 + 0.25 * j;
                vz = random.nextFloat() * 2 * j;
            }
            //world.addParticle(ModParticles.BOG_PORTAL_PARTICLES.get(), px, py, pz, vx, vy, vz);
        }
        if (random.nextInt(110) == 0)
            world.playSound(
                    null,
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5,
                    SoundEvents.PORTAL_AMBIENT,
                    SoundSource.BLOCKS,
                    0.5f,
                    random.nextFloat() * 0.4f + 0.8f
            );
    }

    @Override
    public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity) {
        if (entity.canChangeDimensions(world, world) && !entity.level().isClientSide() && true) {
            if (entity.isOnPortalCooldown()) {
                entity.setPortalCooldown();
            } else if (entity.level().dimension() != ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "hollowdim"))) {//stellar_sorcery/dimension/hollowdim
                entity.setPortalCooldown();
                teleportToDimension(entity, pos, ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "hollowdim")));//stellar_sorcery/dimension/hollowdim
            } else {
                entity.setPortalCooldown();
                teleportToDimension(entity, pos, Level.OVERWORLD);
            }
        }
    }

    private void teleportToDimension(Entity entity, BlockPos pos, ResourceKey<Level> destinationType) {
        ServerLevel targetLevel = entity.getServer().getLevel(destinationType);
        if (targetLevel != null) {
            Vec3 targetPos = new Vec3(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            Vec3 velocity = entity.getDeltaMovement();
            float yRot = entity.getYRot();
            float xRot = entity.getXRot();

            DimensionTransition transition = new DimensionTransition(
                    targetLevel,    // måldimension
                    targetPos,      // destination position
                    velocity,       // entity's momentum
                    yRot,           // rotation yaw
                    xRot,           // rotation pitch
                    DimensionTransition.DO_NOTHING // hvad der skal ske efter teleport
            );

            entity.changeDimension(transition);
        }
    }
}