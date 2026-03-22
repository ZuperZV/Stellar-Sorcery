package net.zuperz.stellar_sorcery.api.jei;

import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.runtime.IClickableIngredient;
import net.zuperz.stellar_sorcery.screen.CodexArcanumScreen;

import java.util.Optional;

public class CodexArcanumGuiHandler implements IGuiContainerHandler<CodexArcanumScreen> {
    @Override
    public Optional<IClickableIngredient<?>> getClickableIngredientUnderMouse(CodexArcanumScreen containerScreen, double mouseX, double mouseY) {
        return containerScreen.getJeiClickableIngredient(mouseX, mouseY);
    }
}
