package de.keksuccino.fancymenu.mixin.client;

import de.keksuccino.fancymenu.FancyMenu;
import de.keksuccino.fancymenu.menu.fancy.MenuCustomization;
import de.keksuccino.fancymenu.mixin.cache.MixinCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ColorHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(AbstractGui.class)
public abstract class MixinAbstractGui {

    private static final int SPLASH_BACKGROUND_COLOR = ColorHelper.PackedColor.packColor(255, 239, 50, 61);

    @ModifyVariable(at = @At("HEAD"), method = "fill(Lcom/mojang/blaze3d/matrix/MatrixStack;IIIII)V", argsOnly = true, index = 5)
    private static int modifyColor(int color) {
        if (FancyMenu.config == null) {
            return color;
        }
        if (Minecraft.getInstance().loadingGui == null) {
            MixinCache.isSplashScreenRendering = false;
        }
        if (MixinCache.isSplashScreenRendering) {
            int backColor = SPLASH_BACKGROUND_COLOR;
            int alpha = MixinCache.currentSplashAlpha;

            Screen current = Minecraft.getInstance().currentScreen;
            if ((current != null) && (MenuCustomization.isMenuCustomizable(current) || FancyMenu.config.getOrDefault("preloadanimations", true))) {
                alpha = 255;
            }

            return withAlpha(backColor, alpha);
        }

        return color;
    }

    private static int withAlpha(int color, int alpha) {
        return color & 16777215 | alpha << 24;
    }

}
