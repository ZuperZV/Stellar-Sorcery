package net.zuperz.stellar_sorcery.block.custom;

import net.minecraft.BlockUtil;
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
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.Portal;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.zuperz.stellar_sorcery.StellarSorcery;
import net.zuperz.stellar_sorcery.block.entity.custom.GlowingBlockEntity;
import net.zuperz.stellar_sorcery.block.light.IGlowingBlock;
import net.zuperz.stellar_sorcery.worldgen.dimension.ModDimensions;
import net.zuperz.stellar_sorcery.worldgen.portal.HollowPortalForcer;
import net.zuperz.stellar_sorcery.worldgen.portal.HollowPortalShape;

import javax.annotation.Nullable;
import java.util.Optional;

public class HollowPortalBlock extends NetherPortalBlock implements Portal, EntityBlock, IGlowingBlock {
    public HollowPortalBlock() {
        super(BlockBehaviour.Properties.of().noCollission().randomTicks().pushReaction(PushReaction.BLOCK).strength(-1.0F).sound(SoundType.GLASS).lightLevel(s -> 11).noLootTable());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GlowingBlockEntity(pos, state);
    }

    @Override
    public float getGlowScale() {
        return 0.8f;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
    }

    public static void portalSpawn(Level world, BlockPos pos) {
        Optional<HollowPortalShape> optional = HollowPortalShape.findEmptyHollowPortalShape(world, pos, Direction.Axis.X);
        optional.ifPresent(HollowPortalShape::createPortalBlocks);
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

    @Nullable
    @Override
    public DimensionTransition getPortalDestination(ServerLevel p_350444_, Entity p_350334_, BlockPos p_350764_) {
        ResourceKey<Level> resourcekey = p_350444_.dimension() == ModDimensions.HOLLOWDIM_LEVEL_KEY ? Level.OVERWORLD : ModDimensions.HOLLOWDIM_LEVEL_KEY;
        ServerLevel serverlevel = p_350444_.getServer().getLevel(resourcekey);
        if (serverlevel == null) {
            return null;
        } else {
            boolean flag = serverlevel.dimension() == ModDimensions.HOLLOWDIM_LEVEL_KEY;
            WorldBorder worldborder = serverlevel.getWorldBorder();
            double d0 = DimensionType.getTeleportationScale(p_350444_.dimensionType(), serverlevel.dimensionType());
            BlockPos blockpos = worldborder.clampToBounds(p_350334_.getX() * d0, p_350334_.getY(), p_350334_.getZ() * d0);
            return this.getExitPortal(serverlevel, p_350334_, p_350764_, blockpos, flag, worldborder);
        }
    }

    @Nullable
    private DimensionTransition getExitPortal(
            ServerLevel targetLevel,
            Entity entity,
            BlockPos portalPos,
            BlockPos scaledPos,
            boolean isTargetHollow,
            WorldBorder worldBorder
    ) {
        HollowPortalForcer forcer = new HollowPortalForcer(targetLevel);

        Optional<BlockPos> optional =
                forcer.findClosestPortalPosition(scaledPos, isTargetHollow, worldBorder);

        BlockUtil.FoundRectangle rectangle;
        DimensionTransition.PostDimensionTransition postTransition;

        if (optional.isPresent()) {

            BlockPos foundPos = optional.get();
            BlockState blockstate = targetLevel.getBlockState(foundPos);

            rectangle = BlockUtil.getLargestRectangleAround(
                    foundPos,
                    blockstate.getValue(BlockStateProperties.HORIZONTAL_AXIS),
                    21,
                    Direction.Axis.Y,
                    21,
                    pos -> targetLevel.getBlockState(pos) == blockstate
            );

            postTransition = DimensionTransition.PLAY_PORTAL_SOUND
                    .then(entity1 -> entity1.placePortalTicket(foundPos));

        } else {

            Direction.Axis axis = entity.level()
                    .getBlockState(portalPos)
                    .getOptionalValue(AXIS)
                    .orElse(Direction.Axis.X);

            Optional<BlockUtil.FoundRectangle> createdPortal =
                    forcer.createPortal(scaledPos, axis);

            if (createdPortal.isEmpty()) {
                return null;
            }

            rectangle = createdPortal.get();
            postTransition = DimensionTransition.PLAY_PORTAL_SOUND
                    .then(DimensionTransition.PLACE_PORTAL_TICKET);
        }

        return getDimensionTransitionFromExit(
                entity,
                portalPos,
                rectangle,
                targetLevel,
                postTransition
        );
    }

    private static DimensionTransition getDimensionTransitionFromExit(
            Entity entity,
            BlockPos originalPortalPos,
            BlockUtil.FoundRectangle rectangle,
            ServerLevel targetLevel,
            DimensionTransition.PostDimensionTransition postTransition
    ) {

        BlockState state = entity.level().getBlockState(originalPortalPos);

        Direction.Axis axis;
        Vec3 relativePos;

        if (state.hasProperty(BlockStateProperties.HORIZONTAL_AXIS)) {

            axis = state.getValue(BlockStateProperties.HORIZONTAL_AXIS);

            BlockUtil.FoundRectangle originalRectangle =
                    BlockUtil.getLargestRectangleAround(
                            originalPortalPos,
                            axis,
                            21,
                            Direction.Axis.Y,
                            21,
                            pos -> entity.level().getBlockState(pos) == state
                    );

            relativePos = entity.getRelativePortalPosition(axis, originalRectangle);

        } else {
            axis = Direction.Axis.X;
            relativePos = new Vec3(0.5, 0.0, 0.0);
        }

        return createDimensionTransition(
                targetLevel,
                rectangle,
                axis,
                relativePos,
                entity,
                entity.getDeltaMovement(),
                entity.getYRot(),
                entity.getXRot(),
                postTransition
        );
    }

    private static DimensionTransition createDimensionTransition(
            ServerLevel targetLevel,
            BlockUtil.FoundRectangle rectangle,
            Direction.Axis axis,
            Vec3 relativePos,
            Entity entity,
            Vec3 velocity,
            float yRot,
            float xRot,
            DimensionTransition.PostDimensionTransition postTransition
    ) {

        BlockPos minCorner = rectangle.minCorner;
        BlockState state = targetLevel.getBlockState(minCorner);

        Direction.Axis portalAxis =
                state.getOptionalValue(BlockStateProperties.HORIZONTAL_AXIS)
                        .orElse(Direction.Axis.X);

        double width = rectangle.axis1Size;
        double height = rectangle.axis2Size;

        EntityDimensions dimensions = entity.getDimensions(entity.getPose());

        int rotation = axis == portalAxis ? 0 : 90;

        Vec3 newVelocity =
                axis == portalAxis ? velocity : new Vec3(velocity.z, velocity.y, -velocity.x);

        double offsetX = dimensions.width() / 2.0
                + (width - dimensions.width()) * relativePos.x();

        double offsetY = (height - dimensions.height()) * relativePos.y();
        double offsetZ = 0.5 + relativePos.z();

        boolean xAxis = portalAxis == Direction.Axis.X;

        Vec3 teleportPos = new Vec3(
                minCorner.getX() + (xAxis ? offsetX : offsetZ),
                minCorner.getY() + offsetY,
                minCorner.getZ() + (xAxis ? offsetZ : offsetX)
        );

        Vec3 safePos = HollowPortalShape.findCollisionFreePosition(
                teleportPos,
                targetLevel,
                entity,
                dimensions
        );

        return new DimensionTransition(
                targetLevel,
                safePos,
                newVelocity,
                yRot + rotation,
                xRot,
                postTransition
        );
    }
}
