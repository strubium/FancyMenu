package de.keksuccino.fancymenu.menu.animation;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import de.keksuccino.fancymenu.FancyMenu;
import de.keksuccino.konkrete.rendering.animation.ExternalTextureAnimationRenderer;
import de.keksuccino.konkrete.rendering.animation.IAnimationRenderer;

import de.keksuccino.konkrete.resources.ExternalTextureResourceLocation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;

public class ResourcePackAnimationRenderer implements IAnimationRenderer {

    protected String resourceNamespace;
    protected List<String> frameNames;
    protected int fps;
    protected boolean loop;
    protected int width;
    protected int height;
    protected int x;
    protected int y;
    public List<ResourceLocation> resources = new ArrayList<ResourceLocation>();
    protected boolean stretch = false;
    protected boolean hide = false;
    protected volatile boolean done = false;

    private int frame = 0;
    private long prevTime = 0;

    protected float opacity = 1.0F;

    private boolean ready = false;
    protected boolean sizeSet = false;

    /** Renders an animation out of multiple images (frames). **/
    public ResourcePackAnimationRenderer(@Nullable String resourceNamespace, List<String> frameNames, int fps, boolean loop, int posX, int posY, int width, int height) {
        this.fps = fps;
        this.loop = loop;
        this.x = posX;
        this.y = posY;
        this.width = width;
        this.height = height;
        this.resourceNamespace = resourceNamespace;
        this.frameNames = frameNames;
        this.loadAnimationFrames();
        ExternalTextureAnimationRenderer r;
    }

    private void loadAnimationFrames() {
        try {
            for (String s : this.frameNames) {
                ResourceLocation r;
                if (this.resourceNamespace == null) {
                    r = new ResourceLocation(s);
                } else {
                    r = new ResourceLocation(this.resourceNamespace, s);
                }
                this.resources.add(r);
            }
            this.ready = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void render() {
        if (this.resources != null && !this.resources.isEmpty()) {
            if (this.ready) {
                if (this.fps < 0) {
                    this.fps = -1;
                }

                if (this.frame > this.resources.size() - 1) {
                    if (this.loop) {
                        this.resetAnimation();
                    } else {
                        this.done = true;
                        if (this.hide) {
                            return;
                        }

                        this.frame = this.resources.size() - 1;
                    }
                }

                this.renderFrame();
                long time = System.currentTimeMillis();
                if (this.fps == -1) {
                    this.updateFrame(time);
                } else if (this.prevTime + (long)(1000 / this.fps) <= time) {
                    this.updateFrame(time);
                }

            }
        }
    }

    protected void renderFrame() {
        int h = this.height;
        int w = this.width;
        int x2 = this.x;
        int y2 = this.y;

        if (this.stretch) {
            h = Minecraft.getMinecraft().currentScreen.height;
            w = Minecraft.getMinecraft().currentScreen.width;
            x2 = 0;
            y2 = 0;
        }

        Minecraft.getMinecraft().getTextureManager().bindTexture(this.resources.get(this.frame));
        GlStateManager.enableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, this.opacity);
        GuiIngame.drawModalRectWithCustomSizedTexture(x2, y2, 0.0F, 0.0F, w, h, (float)w, (float)h);
        GlStateManager.disableBlend();
    }

    public void setOpacity(float opacity) {
        this.opacity = opacity;
    }

    private void updateFrame(long time) {
        this.frame++;
        this.prevTime = time;
    }

    @Override
    public void resetAnimation() {
        this.frame = 0;
        this.prevTime = 0;
        this.done = false;
    }

    @Override
    public void setStretchImageToScreensize(boolean b) {
        this.stretch = b;
    }

    @Override
    public void setHideAfterLastFrame(boolean b) {
        this.hide = b;
    }

    @Override
    public boolean isFinished() {
        return this.done;
    }

    @Override
    public void setWidth(int width) {
        this.width = width;
    }

    @Override
    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public int currentFrame() {
        return this.frame;
    }

    @Override
    public boolean isReady() {
        return this.ready;
    }

    @Override
    public void setPosX(int x) {
        this.x = x;
    }

    @Override
    public void setPosY(int y) {
        this.y = y;
    }

    @Override
    public int animationFrames() {
        return this.resources.size();
    }

    public List<ResourceLocation> getAnimationFrames() {
        return this.resources;
    }

    @Override
    public String getPath() {
        return this.resourceNamespace;
    }

    @Override
    public void setFPS(int fps) {
        this.fps = fps;
    }

    @Override
    public int getFPS() {
        return this.fps;
    }

    @Override
    public void setLooped(boolean b) {
        this.loop = b;
    }

    @Override
    public void prepareAnimation() {
    }

    @Override
    public boolean isGettingLooped() {
        return this.loop;
    }

    @Override
    public boolean isStretchedToStreensize() {
        return this.stretch;
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public int getPosX() {
        return this.x;
    }

    @Override
    public int getPosY() {
        return this.y;
    }

    public boolean setupAnimationSize() {
        if (sizeSet) {
            return true;
        }
        try {
            List<ResourceLocation> l = this.getAnimationFrames();
            if (!l.isEmpty()) {
                ResourceLocation r = l.get(0);
                IResource res = Minecraft.getMinecraft().getResourceManager().getResource(r);
                if (res != null) {
                    InputStream in = res.getInputStream();
                    if (in != null) {
                        BufferedImage i = ImageIO.read(in);
                        this.width = i.getWidth();
                        this.height = i.getHeight();
                        FancyMenu.LOGGER.info("Successfully updated width and height for resource pack animation: {}", this.resourceNamespace);
                        this.sizeSet = true;
                        return true;
                    }
                }
            }
        } catch (Exception ex) {}
        System.err.println("[FANCYMENU] ERROR: Failed to update width and height for resource pack animation: " + this.resourceNamespace);
        return false;
    }

}
