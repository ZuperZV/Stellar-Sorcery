package net.zuperz.stellar_sorcery.mixin;

import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.Map;

@Mixin(ModelPart.class)
public interface ModelPartAccessor {

    @Accessor("children")
    public Map<String, ModelPart> stellar_sorcery$getChildren();

    @Accessor("cubes")
    List<ModelPart.Cube> stellar_sorcery$getCubes();
}