package de.keksuccino.fancymenu.customization.background.backgrounds.image;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.keksuccino.fancymenu.customization.layout.editor.elements.ChooseFilePopup;
import de.keksuccino.fancymenu.rendering.ui.UIBase;
import de.keksuccino.fancymenu.rendering.ui.tooltip.Tooltip;
import de.keksuccino.fancymenu.rendering.ui.tooltip.TooltipHandler;
import de.keksuccino.fancymenu.rendering.ui.widget.Button;
import de.keksuccino.fancymenu.utils.LocalizationUtils;
import de.keksuccino.konkrete.gui.screens.popup.PopupHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class ImageMenuBackgroundConfigScreen extends Screen {

    protected Screen parent;
    protected ImageMenuBackground background;
    protected Consumer<ImageMenuBackground> callback;

    protected Button chooseImageButton;
    protected Button toggleSlideButton;
    protected Button cancelButton;
    protected Button doneButton;

    protected ImageMenuBackgroundConfigScreen(@Nullable Screen parent, @NotNull ImageMenuBackground background, @NotNull Consumer<ImageMenuBackground> callback) {

        super(Component.translatable("fancymenu.background.image.configure"));

        this.parent = parent;
        this.background = background;
        this.callback = callback;

        this.chooseImageButton = new Button(0, 0, 300, 20, Component.translatable("fancymenu.background.image.configure.choose_image"), true, (press) -> {
            ChooseFilePopup p = new ChooseFilePopup((call) -> {
                if (call != null) {
                    if (call.replace(" ", "").length() > 0) {
                        this.background.imagePath = call;
                    } else {
                        this.background.imagePath = null;
                    }
                }
            }, "png", "jpg", "jpeg");
            if (this.background.imagePath != null) {
                p.setText(this.background.imagePath);
            }
            PopupHandler.displayPopup(p);
        });

        this.toggleSlideButton = new Button(0, 0, 300, 20, Component.literal(""), true, (press) -> {
            this.background.slideLeftRight = !this.background.slideLeftRight;
        }) {
            @Override
            public void render(@NotNull PoseStack $$0, int $$1, int $$2, float $$3) {
                if (!background.slideLeftRight) {
                    this.setMessage(Component.translatable("fancymenu.background.image.configure.slide.off"));
                } else {
                    this.setMessage(Component.translatable("fancymenu.background.image.configure.slide.on"));
                }
                super.render($$0, $$1, $$2, $$3);
            }
        };

        this.doneButton = new Button(0, 0, 145, 20, Component.translatable("fancymenu.guicomponents.done"), true, (press) -> {
            Minecraft.getInstance().setScreen(this.parent);
            this.callback.accept(this.background);
        }) {
            @Override
            public void render(@NotNull PoseStack $$0, int $$1, int $$2, float $$3) {
                this.active = background.imagePath != null;
                if (!this.active) {
                    TooltipHandler.INSTANCE.addWidgetTooltip(this, Tooltip.create(LocalizationUtils.splitLocalizedLines("fancymenu.background.image.configure.no_image_chosen")), false, true);
                }
                super.render($$0, $$1, $$2, $$3);
            }
        };

        this.cancelButton = new Button(0, 0, 145, 20, Component.translatable("fancymenu.guicomponents.cancel"), true, (press) -> {
            this.onClose();
        });

    }

    @Override
    public void render(@NotNull PoseStack pose, int mouseX, int mouseY, float partial) {

        RenderSystem.enableBlend();

        fill(pose, 0, 0, this.width, this.height, UIBase.SCREEN_BACKGROUND_COLOR.getRGB());

        int centerX = this.width / 2;
        int centerY = this.height = 2;

        MutableComponent title = this.title.copy().withStyle(ChatFormatting.BOLD);
        int titleWidth = this.font.width(title);
        this.font.draw(pose, title, (float)centerX - ((float)titleWidth / 2F), 20, -1);

        this.chooseImageButton.setX(centerX - (this.chooseImageButton.getWidth() / 2));
        this.chooseImageButton.setY(centerY - 20 - 3);
        this.chooseImageButton.render(pose, mouseX, mouseY, partial);

        this.toggleSlideButton.setX(centerX - (this.toggleSlideButton.getWidth() / 2));
        this.toggleSlideButton.setY(centerY + 2);
        this.toggleSlideButton.render(pose, mouseX, mouseY, partial);

        this.doneButton.setX((this.width / 2) - this.doneButton.getWidth() - 5);
        this.doneButton.setY(this.height - 40);
        this.doneButton.render(pose, mouseX, mouseY, partial);

        this.cancelButton.setX((this.width / 2) + 5);
        this.cancelButton.setY(this.height - 40);
        this.cancelButton.render(pose, mouseX, mouseY, partial);

    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(this.parent);
        this.callback.accept(null);
    }

}
