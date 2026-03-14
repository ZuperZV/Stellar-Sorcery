package net.zuperz.stellar_sorcery.gaze;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.zuperz.stellar_sorcery.data.gaze.GazeDefinition;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class GazeSacrificeTypes {

    public interface Handler {
        boolean canPay(GazeSpellContext context, GazeDefinition.Sacrifice sacrifice);
        void pay(GazeSpellContext context, GazeDefinition.Sacrifice sacrifice);
    }

    private static final Map<String, Handler> HANDLERS = new HashMap<>();

    static {
        register("none", new Handler() {
            @Override
            public boolean canPay(GazeSpellContext context, GazeDefinition.Sacrifice sacrifice) { return true; }
            @Override
            public void pay(GazeSpellContext context, GazeDefinition.Sacrifice sacrifice) {}
        });

        register("item", new Handler() {
            @Override
            public boolean canPay(GazeSpellContext ctx, GazeDefinition.Sacrifice s) {
                ResourceLocation itemId = ResourceLocation.tryParse(s.item());
                if (itemId == null) return false;
                int needed = Math.max(1, s.count());

                int count = 0;
                for (ItemStack stack : ctx.player().getInventory().items) {
                    if (!stack.isEmpty() && BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(itemId)) {
                        count += stack.getCount();
                        if (count >= needed) return true;
                    }
                }
                return false;
            }

            @Override
            public void pay(GazeSpellContext ctx, GazeDefinition.Sacrifice s) {
                ResourceLocation itemId = ResourceLocation.tryParse(s.item());
                if (itemId == null) return;
                int remaining = Math.max(1, s.count());

                for (int i = 0; i < ctx.player().getInventory().items.size(); i++) {
                    ItemStack stack = ctx.player().getInventory().items.get(i);
                    if (stack.isEmpty()) continue;
                    if (!BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(itemId)) continue;

                    int take = Math.min(remaining, stack.getCount());
                    stack.shrink(take);
                    remaining -= take;
                    if (remaining <= 0) break;
                }
            }
        });

        register("fluid", new Handler() {
            @Override
            public boolean canPay(GazeSpellContext ctx, GazeDefinition.Sacrifice s) {
                ResourceLocation fluidId = ResourceLocation.tryParse(s.fluid());
                if (fluidId == null) return false;

                int needed = Math.max(1, s.amount());

                for (ItemStack stack : ctx.player().getInventory().items) {
                    if (stack.isEmpty()) continue;
                    var opt = FluidUtil.getFluidHandler(stack);
                    if (opt.isEmpty()) continue;

                    FluidStack drained = opt.get().drain(new FluidStack(BuiltInRegistries.FLUID.get(fluidId), needed),
                            net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.SIMULATE);

                    needed -= drained.getAmount();
                    if (needed <= 0) return true;
                }
                return false;
            }

            @Override
            public void pay(GazeSpellContext ctx, GazeDefinition.Sacrifice s) {
                ResourceLocation fluidId = ResourceLocation.tryParse(s.fluid());
                if (fluidId == null) return;

                int remaining = Math.max(1, s.amount());

                for (ItemStack stack : ctx.player().getInventory().items) {
                    if (stack.isEmpty()) continue;
                    var opt = FluidUtil.getFluidHandler(stack);
                    if (opt.isEmpty()) continue;

                    FluidStack drained = opt.get().drain(new FluidStack(BuiltInRegistries.FLUID.get(fluidId), remaining),
                            net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);

                    remaining -= drained.getAmount();
                    if (remaining <= 0) break;
                }
            }
        });

        register("entity", new Handler() {
            @Override
            public boolean canPay(GazeSpellContext ctx, GazeDefinition.Sacrifice s) {
                ResourceLocation entityId = ResourceLocation.tryParse(s.entity());
                if (entityId == null) return false;

                int needed = Math.max(1, s.count());
                BlockPos origin = getOrigin(ctx, s);

                List<LivingEntity> entities = ctx.level().getEntitiesOfClass(LivingEntity.class,
                        new net.minecraft.world.phys.AABB(origin).inflate(s.radius()));

                int count = 0;
                for (LivingEntity living : entities) {
                    if (living instanceof Player) continue;
                    if (BuiltInRegistries.ENTITY_TYPE.getKey(living.getType()).equals(entityId)) {
                        count++;
                        if (count >= needed) return true;
                    }
                }

                return false;
            }

            @Override
            public void pay(GazeSpellContext ctx, GazeDefinition.Sacrifice s) {
                ResourceLocation entityId = ResourceLocation.tryParse(s.entity());
                if (entityId == null) return;

                int remaining = Math.max(1, s.count());
                BlockPos origin = getOrigin(ctx, s);

                List<LivingEntity> entities = ctx.level().getEntitiesOfClass(LivingEntity.class,
                        new net.minecraft.world.phys.AABB(origin).inflate(s.radius()));

                for (LivingEntity living : entities) {
                    if (living instanceof Player) continue;
                    if (!BuiltInRegistries.ENTITY_TYPE.getKey(living.getType()).equals(entityId)) continue;

                    living.hurt(ctx.player().damageSources().magic(), Float.MAX_VALUE);
                    remaining--;
                    if (remaining <= 0) break;
                }
            }
        });

        register("health", new Handler() {
            @Override
            public boolean canPay(GazeSpellContext ctx, GazeDefinition.Sacrifice s) {
                return ctx.player().getHealth() > s.health();
            }

            @Override
            public void pay(GazeSpellContext ctx, GazeDefinition.Sacrifice s) {
                ctx.player().hurt(ctx.player().damageSources().magic(), s.health());
            }
        });

        register("life_energy", new Handler() {
            @Override
            public boolean canPay(GazeSpellContext ctx, GazeDefinition.Sacrifice s) {
                return ctx.player().getFoodData().getFoodLevel() >= s.lifeEnergy();
            }

            @Override
            public void pay(GazeSpellContext ctx, GazeDefinition.Sacrifice s) {
                var food = ctx.player().getFoodData();
                int next = Math.max(0, food.getFoodLevel() - s.lifeEnergy());
                food.setFoodLevel(next);
            }
        });

        register("time_of_day", new Handler() {
            @Override
            public boolean canPay(GazeSpellContext ctx, GazeDefinition.Sacrifice s) {
                long time = ctx.level().getDayTime() % 24000L;
                int min = s.timeMin();
                int max = s.timeMax();
                if (min < 0 || max < 0) return true;

                if (min <= max) {
                    return time >= min && time <= max;
                }
                return time >= min || time <= max;
            }

            @Override
            public void pay(GazeSpellContext ctx, GazeDefinition.Sacrifice s) {}
        });

        register("block_structure", new Handler() {
            @Override
            public boolean canPay(GazeSpellContext ctx, GazeDefinition.Sacrifice s) {
                BlockPos origin = getOrigin(ctx, s);
                Level level = ctx.level();

                for (GazeDefinition.BlockRequirement req : s.blocks()) {
                    ResourceLocation blockId = ResourceLocation.tryParse(req.block());
                    if (blockId == null) return false;

                    Block block = BuiltInRegistries.BLOCK.get(blockId);
                    BlockPos checkPos = origin.offset(req.x(), req.y(), req.z());
                    if (!level.getBlockState(checkPos).is(block)) {
                        return false;
                    }
                }

                return true;
            }

            @Override
            public void pay(GazeSpellContext ctx, GazeDefinition.Sacrifice s) {}
        });
    }

    private GazeSacrificeTypes() {}

    public static void register(String type, Handler handler) {
        HANDLERS.put(type.toLowerCase(Locale.ROOT), handler);
    }

    public static Handler get(String type) {
        return HANDLERS.getOrDefault(type.toLowerCase(Locale.ROOT), HANDLERS.get("none"));
    }

    private static BlockPos getOrigin(GazeSpellContext ctx, GazeDefinition.Sacrifice s) {
        String origin = s.origin();
        if (origin == null) origin = "player";
        if ("target".equalsIgnoreCase(origin)) {
            return ctx.target().getCenterPos(ctx.player().blockPosition());
        }
        return ctx.player().blockPosition();
    }
}
