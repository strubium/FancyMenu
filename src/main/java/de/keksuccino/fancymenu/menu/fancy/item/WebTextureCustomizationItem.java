package de.keksuccino.fancymenu.menu.fancy.item;

import java.io.IOException;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import de.keksuccino.fancymenu.menu.fancy.DynamicValueHelper;
import de.keksuccino.konkrete.properties.PropertiesSection;
import de.keksuccino.konkrete.resources.TextureHandler;
import de.keksuccino.konkrete.resources.WebTextureResourceLocation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IngameGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.texture.TextureManager;

public class WebTextureCustomizationItem extends CustomizationItemBase {
	
	public WebTextureResourceLocation texture;
	public String rawURL = "";
	
	public WebTextureCustomizationItem(PropertiesSection item) {
		super(item);
		
		if ((this.action != null) && this.action.equalsIgnoreCase("addwebtexture")) {
			this.value = item.getEntryValue("url");
			if (this.value != null) {
				this.rawURL = this.value;
				this.value = DynamicValueHelper.convertFromRaw(this.value);
				try {
					try {
						this.texture = TextureHandler.getWebResource(this.value);

						if ((this.texture == null) || !this.texture.isReady()) {
							//TODO übernehmen
							this.setWidth(100);
							this.setHeight(100);
							//------------------
							return;
						}
						
						int w = this.texture.getWidth();
						int h = this.texture.getHeight();
						double ratio = (double) w / (double) h;

						//TODO übernehmen
						//Calculate missing width
						if ((this.getWidth() < 0) && (this.getHeight() >= 0)) {
							this.setWidth((int)(this.getHeight() * ratio));
						}
						//Calculate missing height
						if ((this.getHeight() < 0) && (this.getWidth() >= 0)) {
							this.setHeight((int)(this.getWidth() / ratio));
						}
						//-------------------
					} catch (Exception e) {
						e.printStackTrace();
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				
			}
		}
	}

	public void render(MatrixStack matrix, Screen menu) throws IOException {
		if (this.shouldRender()) {
			
			int x = this.getPosX(menu);
			int y = this.getPosY(menu);

			if (this.texture != null) {
				Minecraft.getInstance().getTextureManager().bindTexture(this.texture.getResourceLocation());
			} else {
				Minecraft.getInstance().getTextureManager().bindTexture(TextureManager.RESOURCE_LOCATION_EMPTY);
			}
			
			RenderSystem.enableBlend();
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.opacity);
			//TODO übernehmen
			IngameGui.blit(matrix, x, y, 0.0F, 0.0F, this.getWidth(), this.getHeight(), this.getWidth(), this.getHeight());
			RenderSystem.disableBlend();
		}
	}

	//TODO übernehmen
	@Override
	public boolean shouldRender() {
		if ((this.getWidth() < 0) || (this.getHeight() < 0)) {
			return false;
		}
		return super.shouldRender();
	}

}
