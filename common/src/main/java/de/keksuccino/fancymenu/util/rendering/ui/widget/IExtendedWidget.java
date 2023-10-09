package de.keksuccino.fancymenu.util.rendering.ui.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public interface IExtendedWidget {

    default void renderScrollingLabel(@NotNull AbstractWidget widget, @NotNull PoseStack pose, @NotNull Font font, int spaceLeftRight, boolean labelShadow, int textColor) {
        int xMin = widget.getX() + spaceLeftRight;
        int xMax = widget.getX() + widget.getWidth() - spaceLeftRight;
        //Use getMessage() here to not break custom label handling of CustomizableWidget
        this.renderScrollingLabelInternal(pose, font, widget.getMessage(), xMin, widget.getY(), xMax, widget.getY() + widget.getHeight(), labelShadow, textColor);
    }

    default void renderScrollingLabelInternal(@NotNull PoseStack pose, Font font, @NotNull Component text, int xMin, int yMin, int xMax, int yMax, boolean labelShadow, int textColor) {
        int textWidth = font.width(text);
        int textPosY = (yMin + yMax - 9) / 2 + 1;
        int maxTextWidth = xMax - xMin;
        if (textWidth > maxTextWidth) {
            int diffTextWidth = textWidth - maxTextWidth;
            double scrollTime = (double) Util.getMillis() / 1000.0D;
            double $$13 = Math.max((double)diffTextWidth * 0.5D, 3.0D);
            double $$14 = Math.sin((Math.PI / 2D) * Math.cos((Math.PI * 2D) * scrollTime / $$13)) / 2.0D + 0.5D;
            double textPosX = Mth.lerp($$14, 0.0D, diffTextWidth);
            GuiComponent.enableScissor(xMin, yMin, xMax, yMax);
            if (!labelShadow) {
                font.draw(pose, text, xMin - (int)textPosX, textPosY, textColor);
            } else {
                font.drawShadow(pose, text, xMin - (int)textPosX, textPosY, textColor);
            }
            GuiComponent.disableScissor();
        } else {
            if (!labelShadow) {
                font.draw(pose, text, (int)(((xMin + xMax) / 2F) - (font.width(text) / 2F)), textPosY, textColor);
            } else {
                font.drawShadow(pose, text, (int)(((xMin + xMax) / 2F) - (font.width(text) / 2F)), textPosY, textColor);
            }
        }
    }

}
