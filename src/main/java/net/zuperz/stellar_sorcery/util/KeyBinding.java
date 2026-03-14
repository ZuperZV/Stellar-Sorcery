package net.zuperz.stellar_sorcery.util;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class KeyBinding {
    public static final String KEY_CATEGORY_STELLAR_SORCERY = "key.category.stellar_sorcery";
    public static final String KEY_OPEN_BOOK = "key.stellar_sorcery.open_codex_arcanum";
    public static final String KEY_CAST_GAZE = "key.stellar_sorcery.cast_gaze";

    public static final KeyMapping OPEN_BOOK = new KeyMapping(KEY_OPEN_BOOK, KeyConflictContext.GUI,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_CONTROL, KEY_CATEGORY_STELLAR_SORCERY);

    public static final KeyMapping CAST_GAZE = new KeyMapping(KEY_CAST_GAZE, KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, KEY_CATEGORY_STELLAR_SORCERY);
}
