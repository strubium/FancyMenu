package de.keksuccino.fancymenu.menu.fancy.item;

import java.io.File;
import java.io.IOException;

import de.keksuccino.fancymenu.FancyMenu;
import de.keksuccino.fancymenu.menu.button.ButtonData;
import de.keksuccino.fancymenu.menu.fancy.MenuCustomization;
import de.keksuccino.fancymenu.menu.fancy.menuhandler.MenuHandlerBase;
import de.keksuccino.fancymenu.menu.loadingrequirement.v2.internal.LoadingRequirementContainer;
import de.keksuccino.konkrete.input.StringUtils;
import de.keksuccino.konkrete.math.MathUtils;
import de.keksuccino.konkrete.properties.PropertiesSection;
import de.keksuccino.konkrete.sound.SoundHandler;
import net.minecraft.client.gui.GuiScreen;

public class VanillaButtonCustomizationItem extends CustomizationItemBase {
	
	public ButtonData parent;
	
	private String normalLabel = "";
	private boolean hovered = false;

	public String hoverLabelRaw;
	public String labelRaw;
	protected boolean normalLabelCached = false;

	public MenuHandlerBase handler;
	public LoadingRequirementContainer loadingRequirements = null;
	
	public VanillaButtonCustomizationItem(PropertiesSection item, ButtonData parent, MenuHandlerBase handler) {
		super(item);
		this.parent = parent;
		this.handler = handler;

		if ((this.action != null) && (this.parent != null)) {

			if (this.action.equalsIgnoreCase("addhoversound")) {
				this.value = fixBackslashPath(item.getEntryValue("path"));
				if (this.value != null) {
					File f = new File(MenuCustomization.getAbsoluteGameDirectoryPath(this.value));
					if (f.exists() && f.isFile()) {
						if (!SoundHandler.soundExists(this.value)) {
							MenuCustomization.registerSound(this.value, this.value);
						}
					} else {
						FancyMenu.LOGGER.error("################### ERROR ###################");
                        FancyMenu.LOGGER.error("Soundfile '{}' for 'addhoversound' customization action not found!", this.value);
						FancyMenu.LOGGER.error("#############################################");
						this.value = null;
					}
				}
			}

			if (this.action.equalsIgnoreCase("sethoverlabel")) {
				this.hoverLabelRaw = item.getEntryValue("label");
				if (this.parent != null) {
					this.normalLabel = this.parent.getButton().displayString;
				}
				this.updateValues();
			}

			if (this.action.equalsIgnoreCase("renamebutton") || this.action.equalsIgnoreCase("setbuttonlabel")) {
				this.labelRaw = item.getEntryValue("value");
				this.updateValues();
			}

			if (action.equalsIgnoreCase("movebutton")) {

				String x = item.getEntryValue("x");
				String y = item.getEntryValue("y");
				if (x != null) {
					x = de.keksuccino.fancymenu.menu.placeholder.v2.PlaceholderParser.replacePlaceholders(x);
					if (MathUtils.isInteger(x)) {
						this.posX = Integer.parseInt(x);
					}
				}
				if (y != null) {
					y = de.keksuccino.fancymenu.menu.placeholder.v2.PlaceholderParser.replacePlaceholders(y);
					if (MathUtils.isInteger(y)) {
						this.posY = Integer.parseInt(y);
					}
				}

				String o = item.getEntryValue("orientation");
				if (o != null) {
					this.orientation = o;
				}

				String oe = item.getEntryValue("orientation_element");
				if (oe != null) {
					this.orientationElementIdentifier = oe;
				}

			}
			
		}
	}

	@Override
	public void render(GuiScreen menu) throws IOException {
		if (this.parent != null) {

			this.updateValues();

			if (action.equalsIgnoreCase("vanilla_button_visibility_requirements")) {
				if (this.loadingRequirements != null) {
					if (!this.handler.isVanillaButtonHidden(this.parent.getButton())) {
						//TODO übernehmenn
						this.loadingRequirementContainer = this.loadingRequirements;
						this.parent.getButton().visible = this.loadingRequirementsMet();
						//----------------
					}
				}
			}

			if (this.action.equals("addhoversound")) {
				if (this.parent.getButton().isMouseOver() && this.parent.getButton().enabled && !hovered && (this.value != null)) {
					SoundHandler.resetSound(this.value);
					SoundHandler.playSound(this.value);
					this.hovered = true;
				}
				if (!this.parent.getButton().isMouseOver()) {
					this.hovered = false;
				}
			}

			if (this.action.equals("sethoverlabel")) {
				if (this.value != null) {
					this.parent.hasHoverLabel = true;
					if (this.parent.getButton().isMouseOver() && this.parent.getButton().enabled) {
						if (!this.normalLabelCached) {
							this.normalLabelCached = true;
							this.normalLabel = this.parent.getButton().displayString;
						}
						this.parent.getButton().displayString = this.value;
					} else {
						if (this.normalLabelCached) {
							this.normalLabelCached = false;
							this.parent.getButton().displayString = this.normalLabel;
						}
					}
				}
			}

			if (this.action.equalsIgnoreCase("renamebutton") || this.action.equalsIgnoreCase("setbuttonlabel")) {
				if (this.value != null) {
					if (!this.parent.getButton().isMouseOver() || !this.parent.hasHoverLabel) {
						this.parent.getButton().displayString = this.value;
					}
				}
			}

			if (action.equalsIgnoreCase("movebutton")) {
				this.parent.getButton().x = this.getPosX(menu);
				this.parent.getButton().y = this.getPosY(menu);
			}

			if (action.equalsIgnoreCase("resizebutton")) {
				this.parent.getButton().width = (this.getWidth());
				this.parent.getButton().height = (this.getHeight());
			}
			
		}
	}

	protected void updateValues() {

		if (this.action.equalsIgnoreCase("renamebutton") || this.action.equalsIgnoreCase("setbuttonlabel")) {
			if (this.labelRaw != null) {
				if (!isEditorActive()) {
					this.value = de.keksuccino.fancymenu.menu.placeholder.v2.PlaceholderParser.replacePlaceholders(this.labelRaw);
				} else {
					this.value = StringUtils.convertFormatCodes(this.labelRaw, "&", "§");
				}
			}
		}

		if (this.action.equals("sethoverlabel")) {
			if (this.hoverLabelRaw != null) {
				if (!isEditorActive()) {
					this.value = de.keksuccino.fancymenu.menu.placeholder.v2.PlaceholderParser.replacePlaceholders(this.hoverLabelRaw);
				} else {
					this.value = StringUtils.convertFormatCodes(this.hoverLabelRaw, "&", "§");
				}
			}
		}

	}

	public String getButtonId() {
		if (this.parent.getCompatibilityId() != null) {
			return this.parent.getCompatibilityId();
		}
		return "" + this.parent.getId();
	}

}
