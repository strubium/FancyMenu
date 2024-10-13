package de.keksuccino.fancymenu.menu.fancy.menuhandler;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.keksuccino.fancymenu.FancyMenu;
import de.keksuccino.fancymenu.api.background.MenuBackground;
import de.keksuccino.fancymenu.api.background.MenuBackgroundType;
import de.keksuccino.fancymenu.api.background.MenuBackgroundTypeRegistry;
import de.keksuccino.fancymenu.api.item.CustomizationItem;
import de.keksuccino.fancymenu.api.item.CustomizationItemContainer;
import de.keksuccino.fancymenu.api.item.CustomizationItemRegistry;
import de.keksuccino.fancymenu.events.*;
import de.keksuccino.fancymenu.mainwindow.MainWindowHandler;
import de.keksuccino.fancymenu.menu.animation.AdvancedAnimation;
import de.keksuccino.fancymenu.menu.animation.AnimationHandler;
import de.keksuccino.fancymenu.menu.button.ButtonCache;
import de.keksuccino.fancymenu.menu.button.ButtonCachedEvent;
import de.keksuccino.fancymenu.menu.button.ButtonData;
import de.keksuccino.fancymenu.menu.button.VanillaButtonDescriptionHandler;
import de.keksuccino.fancymenu.menu.fancy.helper.ui.compat.AbstractGui;
import de.keksuccino.fancymenu.menu.fancy.helper.ui.compat.RenderSystem;
import de.keksuccino.fancymenu.menu.loadingrequirement.v2.internal.LoadingRequirementContainer;
import de.keksuccino.fancymenu.menu.placeholder.v1.DynamicValueHelper;
import de.keksuccino.fancymenu.menu.fancy.MenuCustomization;
import de.keksuccino.fancymenu.menu.fancy.MenuCustomizationProperties;
import de.keksuccino.fancymenu.menu.fancy.gameintro.GameIntroHandler;
import de.keksuccino.fancymenu.menu.fancy.guicreator.CustomGuiBase;
import de.keksuccino.fancymenu.menu.fancy.guicreator.CustomGuiLoader;
import de.keksuccino.fancymenu.menu.fancy.helper.MenuReloadedEvent;
import de.keksuccino.fancymenu.menu.fancy.helper.layoutcreator.LayoutEditorScreen;
import de.keksuccino.fancymenu.menu.fancy.item.AnimationCustomizationItem;
import de.keksuccino.fancymenu.menu.fancy.item.ButtonCustomizationItem;
import de.keksuccino.fancymenu.menu.fancy.item.CustomizationItemBase;
import de.keksuccino.fancymenu.menu.fancy.item.ShapeCustomizationItem;
import de.keksuccino.fancymenu.menu.fancy.item.SlideshowCustomizationItem;
import de.keksuccino.fancymenu.menu.fancy.item.SplashTextCustomizationItem;
import de.keksuccino.fancymenu.menu.fancy.item.StringCustomizationItem;
import de.keksuccino.fancymenu.menu.fancy.item.TextureCustomizationItem;
import de.keksuccino.fancymenu.menu.fancy.item.VanillaButtonCustomizationItem;
import de.keksuccino.fancymenu.menu.fancy.item.WebStringCustomizationItem;
import de.keksuccino.fancymenu.menu.fancy.item.WebTextureCustomizationItem;
import de.keksuccino.fancymenu.menu.fancy.item.playerentity.PlayerEntityCustomizationItem;
import de.keksuccino.fancymenu.menu.panorama.ExternalTexturePanoramaRenderer;
import de.keksuccino.fancymenu.menu.panorama.PanoramaHandler;
import de.keksuccino.fancymenu.menu.slideshow.ExternalTextureSlideshowRenderer;
import de.keksuccino.fancymenu.menu.slideshow.SlideshowHandler;
import de.keksuccino.konkrete.gui.screens.popup.PopupHandler;
import de.keksuccino.konkrete.input.MouseInput;
import de.keksuccino.konkrete.math.MathUtils;
import de.keksuccino.konkrete.properties.PropertiesSection;
import de.keksuccino.konkrete.properties.PropertiesSet;
import de.keksuccino.konkrete.rendering.RenderUtils;
import de.keksuccino.konkrete.rendering.animation.IAnimationRenderer;
import de.keksuccino.konkrete.resources.ExternalTextureResourceLocation;
import de.keksuccino.konkrete.resources.TextureHandler;
import de.keksuccino.konkrete.sound.SoundHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiButtonImage;
import net.minecraft.client.gui.GuiButtonLanguage;
import net.minecraft.client.gui.GuiControls;
import net.minecraft.client.gui.GuiCustomizeSkin;
import net.minecraft.client.gui.GuiLanguage;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiScreenOptionsSounds;
import net.minecraft.client.gui.GuiScreenResourcePacks;
import net.minecraft.client.gui.GuiScreenServerList;
import net.minecraft.client.gui.GuiSnooper;
import net.minecraft.client.gui.GuiVideoSettings;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.ScreenChatOptions;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class MenuHandlerBase {

	public List<CustomizationItemBase> frontRenderItems = new ArrayList<CustomizationItemBase>();
	public List<CustomizationItemBase> backgroundRenderItems = new ArrayList<CustomizationItemBase>();
	
	protected Map<String, Boolean> audio = new HashMap<String, Boolean>();
	protected IAnimationRenderer backgroundAnimation = null;
	protected IAnimationRenderer lastBackgroundAnimation = null;
	protected List<IAnimationRenderer> backgroundAnimations = new ArrayList<IAnimationRenderer>();
	protected int backgroundAnimationId = 0;
	protected ExternalTextureResourceLocation backgroundTexture = null;
	protected String identifier;
	protected boolean backgroundDrawable;
	protected boolean panoramaback = false;
	protected int panoTick = 0;
	protected double panoPos = 0.0;
	protected boolean panoMoveBack = false;
	protected boolean panoStop = false;
	protected boolean keepBackgroundAspectRatio = false;

	protected ExternalTexturePanoramaRenderer panoramacube;

	protected ExternalTextureSlideshowRenderer slideshow;

	protected MenuBackground customMenuBackground = null;
	public float backgroundOpacity = 1.0F;

	protected List<ButtonData> hidden = new ArrayList<ButtonData>();
	protected Map<GuiButton, ButtonCustomizationContainer> vanillaButtonCustomizations = new HashMap<GuiButton, ButtonCustomizationContainer>();
	protected Map<GuiButton, LoadingRequirementContainer> vanillaButtonLoadingRequirementContainers = new HashMap<>();

	protected Map<ButtonData, Float> delayAppearanceVanilla = new HashMap<ButtonData, Float>();
	protected Map<ButtonData, Float> fadeInVanilla = new HashMap<ButtonData, Float>();
	protected List<String> delayAppearanceFirstTime = new ArrayList<String>();
	protected List<Long> delayAppearanceFirstTimeVanilla = new ArrayList<Long>();
	protected List<ThreadCaller> delayThreads = new ArrayList<ThreadCaller>();
	protected volatile Map<GuiButton, Float> buttonAlphas = new HashMap<GuiButton, Float>();

	protected boolean preinit = false;
	
	protected Map<String, RandomLayoutContainer> randomLayoutGroups = new HashMap<String, RandomLayoutContainer>();
	protected List<PropertiesSet> normalLayouts = new ArrayList<PropertiesSet>();
	protected SharedLayoutProperties sharedLayoutProps = new SharedLayoutProperties();

	protected String closeAudio;
	protected String openAudio;

	protected Map<LoadingRequirementContainer, Boolean> cachedLayoutWideLoadingRequirements = new HashMap<>();
	
	protected static int oriscale = Minecraft.getMinecraft().gameSettings.guiScale;
	protected static GuiScreen scaleChangedIn = null;

	/**
	 * @param identifier Has to be the valid and full class name of the GUI screen.
	 */
	public MenuHandlerBase(@Nonnull String identifier) {
		this.identifier = identifier;
	}

	public String getMenuIdentifier() {
		return this.identifier;
	}

	@SubscribeEvent
	public void onSoftReload(SoftMenuReloadEvent e) {
		if (this.shouldCustomize(e.screen)) {
			this.delayAppearanceFirstTimeVanilla.clear();
			this.delayAppearanceFirstTime.clear();
			this.delayAppearanceVanilla.clear();
			this.fadeInVanilla.clear();
			for (RandomLayoutContainer c : this.randomLayoutGroups.values()) {
				c.lastLayoutPath = null;
			}

			if (this.lastBackgroundAnimation != null) {
				this.lastBackgroundAnimation.resetAnimation();
			}
		}
	}

	@SubscribeEvent
	public void onMenuReloaded(MenuReloadedEvent e) {
		this.delayAppearanceFirstTimeVanilla.clear();
		this.delayAppearanceFirstTime.clear();
		this.delayAppearanceVanilla.clear();
		this.fadeInVanilla.clear();
		for (RandomLayoutContainer c : this.randomLayoutGroups.values()) {
			c.lastLayoutPath = null;
		}

		if (this.lastBackgroundAnimation != null) {
			this.lastBackgroundAnimation.resetAnimation();
		}
	}
	
	@SubscribeEvent
	public void onRenderButton(RenderWidgetEvent.Pre e) {
		if (this.buttonAlphas.containsKey(e.getWidget())) {
			e.setAlpha(this.buttonAlphas.get(e.getWidget()));
		}
	}

	@SubscribeEvent
	public void onInitPre(GuiScreenEvent.InitGuiEvent.Pre e) {

		for (ThreadCaller t : this.delayThreads) {
			t.running.set(false);
		}
		this.delayThreads.clear();

		//Resetting scale to the normal value if it was changed in another screen
		if ((scaleChangedIn != null) && (scaleChangedIn != e.getGui())) {
			scaleChangedIn = null;
			Minecraft.getMinecraft().gameSettings.guiScale = oriscale;
			ScaledResolution res = new ScaledResolution(Minecraft.getMinecraft());
			e.getGui().width = res.getScaledWidth();
			e.getGui().height = res.getScaledHeight();
		}
				
		if (e.getGui() != Minecraft.getMinecraft().currentScreen) {
			return;
		}

		if (!MenuCustomization.isValidScreen(e.getGui())) {
			return;
		}
		if (!this.shouldCustomize(e.getGui())) {
			return;
		}
		if (!AnimationHandler.isReady()) {
			return;
		}
		if (!GameIntroHandler.introDisplayed) {
			return;
		}
		if (LayoutEditorScreen.isActive) {
			return;
		}
		if (ButtonCache.isCaching()) {
			return;
		}
		if (!MenuCustomization.isMenuCustomizable(e.getGui())) {
			return;
		}

		preinit = true;

		List<PropertiesSet> rawLayouts = MenuCustomizationProperties.getPropertiesWithIdentifier(this.getMenuIdentifier());
		String defaultGroup = "-100397";

		this.normalLayouts.clear();

		for (RandomLayoutContainer c : this.randomLayoutGroups.values()) {
			c.onlyFirstTime = false;
			c.clearLayouts();
		}

		this.sharedLayoutProps = new SharedLayoutProperties();

		this.cachedLayoutWideLoadingRequirements.clear();

		for (PropertiesSet s : rawLayouts) {
			
			List<PropertiesSection> metas = s.getPropertiesOfType("customization-meta");
			if (metas.isEmpty()) {
				metas = s.getPropertiesOfType("type-meta");
			}
			if (metas.isEmpty()) {
				continue;
			}

			LoadingRequirementContainer layoutWideRequirementContainer = LoadingRequirementContainer.deserializeRequirementContainer(metas.get(0));
			this.cachedLayoutWideLoadingRequirements.put(layoutWideRequirementContainer, layoutWideRequirementContainer.requirementsMet());
			if (!layoutWideRequirementContainer.requirementsMet()) {
				continue;
			}

			String biggerthanwidth = metas.get(0).getEntryValue("biggerthanwidth");
			if (biggerthanwidth != null) {
				biggerthanwidth = biggerthanwidth.replace(" ", "");
				if (MathUtils.isInteger(biggerthanwidth)) {
					int i = Integer.parseInt(biggerthanwidth);
					if (MainWindowHandler.getWindowGuiWidth() < i) {
						continue;
					}
				}
			}

			String biggerthanheight = metas.get(0).getEntryValue("biggerthanheight");
			if (biggerthanheight != null) {
				biggerthanheight = biggerthanheight.replace(" ", "");
				if (MathUtils.isInteger(biggerthanheight)) {
					int i = Integer.parseInt(biggerthanheight);
					if (MainWindowHandler.getWindowGuiHeight() < i) {
						continue;
					}
				}
			}

			String smallerthanwidth = metas.get(0).getEntryValue("smallerthanwidth");
			if (smallerthanwidth != null) {
				smallerthanwidth = smallerthanwidth.replace(" ", "");
				if (MathUtils.isInteger(smallerthanwidth)) {
					int i = Integer.parseInt(smallerthanwidth);
					if (MainWindowHandler.getWindowGuiWidth() > i) {
						continue;
					}
				}
			}

			String smallerthanheight = metas.get(0).getEntryValue("smallerthanheight");
			if (smallerthanheight != null) {
				smallerthanheight = smallerthanheight.replace(" ", "");
				if (MathUtils.isInteger(smallerthanheight)) {
					int i = Integer.parseInt(smallerthanheight);
					if (MainWindowHandler.getWindowGuiHeight() > i) {
						continue;
					}
				}
			}

			String randomMode = metas.get(0).getEntryValue("randommode");
			if ((randomMode != null) && randomMode.equalsIgnoreCase("true")) {
				
				String group = metas.get(0).getEntryValue("randomgroup");
				if (group == null) {
					group = defaultGroup;
				}
				if (!this.randomLayoutGroups.containsKey(group)) {
					this.randomLayoutGroups.put(group, new RandomLayoutContainer(group, this));
				}
				RandomLayoutContainer c = this.randomLayoutGroups.get(group);
				if (c != null) {
					String randomOnlyFirstTime = metas.get(0).getEntryValue("randomonlyfirsttime");
					if ((randomOnlyFirstTime != null) && randomOnlyFirstTime.equalsIgnoreCase("true")) {
						c.setOnlyFirstTime(true);
					}
					c.addLayout(s);
				}
				
			} else {
				
				this.normalLayouts.add(s);
				
			}
			
		}
		
		List<String> trashLayoutGroups = new ArrayList<String>();
		for (Map.Entry<String, RandomLayoutContainer> m : this.randomLayoutGroups.entrySet()) {
			if (m.getValue().getLayouts().isEmpty()) {
				trashLayoutGroups.add(m.getKey());
			}
		}
		for (String s : trashLayoutGroups) {
			this.randomLayoutGroups.remove(s);
		}
		
		//Applying customizations which needs to be done before other ones
		for (PropertiesSet s : this.normalLayouts) {
			for (PropertiesSection sec : s.getPropertiesOfType("customization")) {
				this.applyLayoutPre(sec, e);
			}
		}
		for (RandomLayoutContainer c : this.randomLayoutGroups.values()) {
			PropertiesSet s = c.getRandomLayout();
			if (s != null) {
				for (PropertiesSection sec : s.getPropertiesOfType("customization")) {
					this.applyLayoutPre(sec, e);
				}
			}
		}

		//Resetting scale in the same menu when scale customization action was removed
		if (!this.sharedLayoutProps.scaled) {
			if (scaleChangedIn != null) {
				scaleChangedIn = null;
				Minecraft.getMinecraft().gameSettings.guiScale = oriscale;
				ScaledResolution res = new ScaledResolution(Minecraft.getMinecraft());
				e.getGui().width = res.getScaledWidth();
				e.getGui().height = res.getScaledHeight();
			}
		}

		//Unused
		//Handle auto scaling
		if ((this.sharedLayoutProps.autoScaleBaseWidth != 0) && (this.sharedLayoutProps.autoScaleBaseHeight != 0)) {
			// EMPTY ------------
		}

	}
	
	protected void applyLayoutPre(PropertiesSection sec, GuiScreenEvent.InitGuiEvent.Pre e) {
		
		String action = sec.getEntryValue("action");
		if (action != null) {
			String identifier = sec.getEntryValue("identifier");
			
			if (action.equalsIgnoreCase("overridemenu")) {
				if ((identifier != null) && CustomGuiLoader.guiExists(identifier)) {
					CustomGuiBase cus = CustomGuiLoader.getGui(identifier, (GuiScreen)null, e.getGui(), (onClose) -> {
						e.getGui().onGuiClosed();
					});
					Minecraft.getMinecraft().displayGuiScreen(cus);
					return;
				}
			}

			if (action.contentEquals("setscale")) {
				//Prevent force-scaling in screens that save gamesettings (this is crap, will change this later)
				if (isForcescalingAllowed(e.getGui())) {
					String scale = sec.getEntryValue("scale");
					if ((scale != null) && (MathUtils.isInteger(scale.replace(" ", "")) || MathUtils.isDouble(scale.replace(" ", "")))) {
						if (scaleChangedIn == null) {
							oriscale = Minecraft.getMinecraft().gameSettings.guiScale;
						}
						scaleChangedIn = e.getGui();
						int newscale = (int) Double.parseDouble(scale.replace(" ", ""));
						if (newscale <= 0) {
							newscale = 1;
						}
						Minecraft.getMinecraft().gameSettings.guiScale = newscale;
						ScaledResolution res = new ScaledResolution(Minecraft.getMinecraft());
						e.getGui().width = res.getScaledWidth();
						e.getGui().height = res.getScaledHeight();
						this.sharedLayoutProps.scaled = true;
					}
				}
			}

			//Unused
			if (action.equalsIgnoreCase("autoscale")) {
				String baseWidth = sec.getEntryValue("basewidth");
				if (MathUtils.isInteger(baseWidth)) {
					this.sharedLayoutProps.autoScaleBaseWidth = Integer.parseInt(baseWidth);
				}
				String baseHeight = sec.getEntryValue("baseheight");
				if (MathUtils.isInteger(baseHeight)) {
					this.sharedLayoutProps.autoScaleBaseHeight = Integer.parseInt(baseHeight);
				}
			}
		}
		
	}

	@SubscribeEvent
	public void onButtonsCached(ButtonCachedEvent e) {

		if (e.getGui() != Minecraft.getMinecraft().currentScreen) {
			return;
		}
		if (!MenuCustomization.isValidScreen(e.getGui())) {
			return;
		}
		if (!this.shouldCustomize(e.getGui())) {
			return;
		}
		if (!AnimationHandler.isReady()) {
			return;
		}
		if (!GameIntroHandler.introDisplayed) {
			return;
		}
		if (LayoutEditorScreen.isActive) {
			return;
		}
		if (ButtonCache.isCaching()) {
			return;
		}
		if (!MenuCustomization.isMenuCustomizable(e.getGui())) {
			return;
		}

		if (!this.preinit) {
			System.out.println("################ WARNING [FANCYMENU] ################");
			System.out.println("MenuHandler pre-init skipped! Trying to re-initialize menu!");
			System.out.println("Menu Type: " + e.getGui().getClass().getName());
			System.out.println("Menu Handler: " + this.getClass().getName());
			System.out.println("This probably happened because a mod has overridden a menu with this one.");
			System.out.println("#####################################################");
			e.getGui().setWorldAndResolution(Minecraft.getMinecraft(), e.getGui().width, e.getGui().height);
			return;
		}

		this.hidden.clear();
		this.buttonAlphas.clear();
		this.delayAppearanceVanilla.clear();
		this.fadeInVanilla.clear();
		this.vanillaButtonCustomizations.clear();
		this.vanillaButtonLoadingRequirementContainers.clear();
		audio.clear();
		frontRenderItems.clear();
		backgroundRenderItems.clear();
		this.panoramacube = null;
		this.slideshow = null;
		this.customMenuBackground = null;
		this.backgroundOpacity = 1.0F;
		this.backgroundAnimation = null;
		this.backgroundAnimations.clear();
		if ((this.backgroundAnimation != null) && (this.backgroundAnimation instanceof AdvancedAnimation)) {
			((AdvancedAnimation)this.backgroundAnimation).stopAudio();
		}
		this.backgroundDrawable = false;

		for (PropertiesSet s : this.normalLayouts) {
			List<PropertiesSection> metas = s.getPropertiesOfType("customization-meta");
			if (metas.isEmpty()) {
				metas = s.getPropertiesOfType("type-meta");
			}
			String renderOrder = metas.get(0).getEntryValue("renderorder");
			for (PropertiesSection sec : s.getPropertiesOfType("customization")) {
				this.applyLayout(sec, renderOrder, e);
			}
		}
		for (RandomLayoutContainer c : this.randomLayoutGroups.values()) {
			PropertiesSet s = c.getRandomLayout();
			if (s != null) {
				List<PropertiesSection> metas = s.getPropertiesOfType("customization-meta");
				if (metas.isEmpty()) {
					metas = s.getPropertiesOfType("type-meta");
				}
				String renderOrder = metas.get(0).getEntryValue("renderorder");
				for (PropertiesSection sec : s.getPropertiesOfType("customization")) {
					this.applyLayout(sec, renderOrder, e);
				}
			}
		}

		MenuHandlerRegistry.setActiveHandler(this.getMenuIdentifier());

		for (Map.Entry<ButtonData, String> m : this.sharedLayoutProps.descriptions.entrySet()) {
			GuiButton w = m.getKey().getButton();
			if (w != null) {
				VanillaButtonDescriptionHandler.setDescriptionFor(w, m.getValue());
			}
		}
		
		for (String s : MenuCustomization.getSounds()) {
			if (!this.audio.containsKey(s) && !s.equals(this.openAudio) && !s.equals(this.closeAudio)) {
				SoundHandler.stopSound(s);
				SoundHandler.resetSound(s);
			}
		}

		if (!this.sharedLayoutProps.closeAudioSet && (this.closeAudio != null)) {
			MenuCustomization.unregisterSound(this.closeAudio);
			this.closeAudio = null;
		}

		if (!this.sharedLayoutProps.openAudioSet && (this.openAudio != null)) {
			MenuCustomization.unregisterSound(this.openAudio);
			this.openAudio = null;
		}

		for (Map.Entry<String, Boolean> m : this.audio.entrySet()) {
			SoundHandler.playSound(m.getKey());
			if (m.getValue()) {
				SoundHandler.setLooped(m.getKey(), true);
			}
		}

		if (!this.sharedLayoutProps.backgroundTextureSet) {
			this.backgroundTexture = null;
		}

		for (ButtonData d : this.hidden) {
			d.getButton().visible = false;
		}

		for (CustomizationItemBase i : this.frontRenderItems) {
			if (MenuCustomization.isNewMenu()) {
				this.handleAppearanceDelayFor(i);
			}
			if (i.orientation.equals("element") && (i.orientationElementIdentifier != null)) {
				i.orientationElement = this.getItemByActionId(i.orientationElementIdentifier);
			}
		}
		for (CustomizationItemBase i : this.backgroundRenderItems) {
			if (MenuCustomization.isNewMenu()) {
				this.handleAppearanceDelayFor(i);
			}
			if (i.orientation.equals("element") && (i.orientationElementIdentifier != null)) {
				i.orientationElement = this.getItemByActionId(i.orientationElementIdentifier);
			}
		}

		//Handle vanilla button visibility requirements
		for (Map.Entry<GuiButton, LoadingRequirementContainer> m : this.vanillaButtonLoadingRequirementContainers.entrySet()) {
			boolean isBtnHidden = false;
			for (ButtonData d : this.hidden) {
				if (d.getButton() == m.getKey()) {
					isBtnHidden = true;
					break;
				}
			}
			if (!isBtnHidden) {
				PropertiesSection dummySec = new PropertiesSection("customization");
				dummySec.addEntry("action", "vanilla_button_visibility_requirements");
				ButtonData btn = null;
				for (ButtonData d : ButtonCache.getButtons()) {
					if (d.getButton() == m.getKey()) {
						btn = d;
						break;
					}
				}
				if (btn != null) {
					VanillaButtonCustomizationItem i = new VanillaButtonCustomizationItem(dummySec, btn, this);
					i.loadingRequirements = m.getValue();
					this.backgroundRenderItems.add(i);
				}
			}
		}

		for (Map.Entry<ButtonData, Float> m : this.delayAppearanceVanilla.entrySet()) {
			if (!hidden.contains(m.getKey())) {
				if (this.vanillaButtonLoadingRequirementsMet(m.getKey().getButton())) {
					this.handleVanillaAppearanceDelayFor(m.getKey());
				}
			}
		}

		//Cache custom buttons
		ButtonCache.clearCustomButtonCache();
		for (CustomizationItemBase c : this.backgroundRenderItems) {
			if (c instanceof ButtonCustomizationItem) {
				ButtonCache.cacheCustomButton(c.getActionId(), ((ButtonCustomizationItem) c).button);
			}
		}
		for (CustomizationItemBase c : this.frontRenderItems) {
			if (c instanceof ButtonCustomizationItem) {
				ButtonCache.cacheCustomButton(c.getActionId(), ((ButtonCustomizationItem) c).button);
			}
		}
		
	}
	
	protected void applyLayout(PropertiesSection sec, String renderOrder, ButtonCachedEvent e) {
		
		String action = sec.getEntryValue("action");
		if (action != null) {
			String identifier = sec.getEntryValue("identifier");
			GuiButton b = null;
			ButtonData bd = null;
			if (identifier != null) {
				bd = getButton(identifier);
				if (bd != null) {
					b = bd.getButton();
				}
			}

			if (action.equalsIgnoreCase("backgroundoptions")) {
				String keepAspect = sec.getEntryValue("keepaspectratio");
				if ((keepAspect != null) && keepAspect.equalsIgnoreCase("true")) {
					this.keepBackgroundAspectRatio = true;
				}
			}

			if (action.equalsIgnoreCase("setbackgroundslideshow")) {
				String name = sec.getEntryValue("name");
				if (name != null) {
					if (SlideshowHandler.slideshowExists(name)) {
						this.slideshow = SlideshowHandler.getSlideshow(name);
					}
				}
			}
			
			if (action.equalsIgnoreCase("setbackgroundpanorama")) {
				String name = sec.getEntryValue("name");
				if (name != null) {
					if (PanoramaHandler.panoramaExists(name)) {
						this.panoramacube = PanoramaHandler.getPanorama(name);
					}
				}
			}
			
			if (action.equalsIgnoreCase("texturizebackground")) {
				String value = CustomizationItemBase.fixBackslashPath(sec.getEntryValue("path"));
				String pano = sec.getEntryValue("wideformat");
				if (pano == null) {
					pano = sec.getEntryValue("panorama");
				}
				if (value != null) {
					File f = new File(value.replace("\\", "/"));
					if (!f.exists() || !f.getAbsolutePath().replace("\\", "/").startsWith(Minecraft.getMinecraft().mcDataDir.getAbsolutePath().replace("\\", "/"))) {
						value = Minecraft.getMinecraft().mcDataDir.getAbsolutePath().replace("\\", "/") + "/" + value.replace("\\", "/");
						f = new File(value);
					}
					if (f.exists() && f.isFile() && (f.getName().toLowerCase().endsWith(".jpg") || f.getName().toLowerCase().endsWith(".jpeg") || f.getName().toLowerCase().endsWith(".png"))) {
						if ((this.backgroundTexture == null) || !this.backgroundTexture.getPath().equals(value)) {
							this.backgroundTexture = TextureHandler.getResource(value);
						}
						if ((pano != null) && pano.equalsIgnoreCase("true")) {
							this.panoramaback = true;
						} else {
							this.panoramaback = false;
						}
						this.sharedLayoutProps.backgroundTextureSet = true;
					}
				}
			}

			if (action.equalsIgnoreCase("animatebackground")) {
				String value = sec.getEntryValue("name");
				String random = sec.getEntryValue("random");
				boolean ran = false;
				if ((random != null) && random.equalsIgnoreCase("true")) {
					ran = true;
				}
				boolean restartOnLoad = false;
				String restartOnLoadString = sec.getEntryValue("restart_on_load");
				if ((restartOnLoadString != null) && restartOnLoadString.equalsIgnoreCase("true")) {
					restartOnLoad = true;
				}
				if (value != null) {
					if (value.contains(",")) {
						for (String s2 : value.split("[,]")) {
							int i = 0;
							for (char c : s2.toCharArray()) {
								if (c != " ".charAt(0)) {
									break;
								}
								i++;
							}
							if (i > s2.length()) {
								continue;
							}
							String temp = new StringBuilder(s2.substring(i)).reverse().toString();
							int i2 = 0;
							for (char c : temp.toCharArray()) {
								if (c != " ".charAt(0)) {
									break;
								}
								i2++;
							}
							String name = new StringBuilder(temp.substring(i2)).reverse().toString();
							if (AnimationHandler.animationExists(name)) {
								this.backgroundAnimations.add(AnimationHandler.getAnimation(name));
							}
						}
					} else {
						if (AnimationHandler.animationExists(value)) {
							this.backgroundAnimations.add(AnimationHandler.getAnimation(value));
						}
					}

					if (!this.backgroundAnimations.isEmpty()) {
						if (restartOnLoad && MenuCustomization.isNewMenu()) {
							for (IAnimationRenderer r : this.backgroundAnimations) {
								r.resetAnimation();
							}
						}
						if (ran) {
							if (MenuCustomization.isNewMenu()) {
								this.backgroundAnimationId = MathUtils.getRandomNumberInRange(0, this.backgroundAnimations.size()-1);
							}
							this.backgroundAnimation = this.backgroundAnimations.get(this.backgroundAnimationId);
						} else {
							if ((this.lastBackgroundAnimation != null) && this.backgroundAnimations.contains(this.lastBackgroundAnimation)) {
								this.backgroundAnimation = this.lastBackgroundAnimation;
							} else {
								this.backgroundAnimationId = 0;
								this.backgroundAnimation = this.backgroundAnimations.get(0);
							}
							this.lastBackgroundAnimation = this.backgroundAnimation;
						}
					}
				}
			}

			//Custom background handling (API)
			if (action.equalsIgnoreCase("api:custombackground")) {
				String typeId = sec.getEntryValue("type_identifier");
				String backId = sec.getEntryValue("background_identifier");
				String inputString = sec.getEntryValue("input_string");
				if (typeId != null) {
					MenuBackgroundType type = MenuBackgroundTypeRegistry.getBackgroundTypeByIdentifier(typeId);
					if (type != null) {
						if (type.needsInputString() && (inputString != null)) {
							try {
								this.customMenuBackground = type.createInstanceFromInputString(inputString);
							} catch (Exception ex) {
								ex.printStackTrace();
							}
							if (this.customMenuBackground != null) {
								if (MenuCustomization.isNewMenu()) {
									this.customMenuBackground.onOpenMenu();
								}
							}
						} else if (backId != null) {
							this.customMenuBackground = type.getBackgroundByIdentifier(backId);
							if (this.customMenuBackground != null) {
								if (MenuCustomization.isNewMenu()) {
									this.customMenuBackground.onOpenMenu();
								}
							}
						}
					}
				}
			}

			if (action.equalsIgnoreCase("hidebuttonfor")) {
				String time = sec.getEntryValue("seconds");
				String onlyfirsttime = sec.getEntryValue("onlyfirsttime");
				String fadein = sec.getEntryValue("fadein");
				String fadeinspeed = sec.getEntryValue("fadeinspeed");
				if (b != null) {
					if (MenuCustomization.isNewMenu()) {
						boolean ft = false;
						if ((onlyfirsttime != null) && onlyfirsttime.equalsIgnoreCase("true")) {
							ft = true;
						}
						if ((time != null) && MathUtils.isFloat(time)) {
							if (!ft || !this.delayAppearanceFirstTimeVanilla.contains(bd.getId())) {
								this.delayAppearanceVanilla.put(bd, Float.parseFloat(time));
							}
						}
						if (ft) {
							if (!this.delayAppearanceFirstTimeVanilla.contains(bd.getId())) {
								this.delayAppearanceFirstTimeVanilla.add(bd.getId());
							}
						}
						if ((fadein != null) && fadein.equalsIgnoreCase("true")) {
							float speed = 1.0F;
							if ((fadeinspeed != null) && MathUtils.isFloat(fadeinspeed)) {
								speed = Float.parseFloat(fadeinspeed);
							}
							this.fadeInVanilla.put(bd, speed);
						}
					}
				}
			}

			if (action.equalsIgnoreCase("hidebutton")) {
				if (b != null) {
					this.hidden.add(bd);
				}
			}

			if (action.equalsIgnoreCase("renamebutton") || action.equalsIgnoreCase("setbuttonlabel")) {
				if (b != null) {
					backgroundRenderItems.add(new VanillaButtonCustomizationItem(sec, bd, this));
				}
			}

			if (action.equalsIgnoreCase("resizebutton")) {
				if (b != null) {
					backgroundRenderItems.add(new VanillaButtonCustomizationItem(sec, bd, this));
				}
			}

			if (action.equalsIgnoreCase("movebutton")) {
				if (b != null) {
					backgroundRenderItems.add(new VanillaButtonCustomizationItem(sec, bd, this));
				}
			}

			if (action.equalsIgnoreCase("setbuttontexture")) {
				if (b != null) {
					String loopBackAnimations = sec.getEntryValue("loopbackgroundanimations");
					if ((loopBackAnimations != null) && loopBackAnimations.equalsIgnoreCase("false")) {
						this.getContainerForVanillaButton(b).loopAnimation = false;
					}
					String restartBackAnimationsOnHover = sec.getEntryValue("restartbackgroundanimations");
					if ((restartBackAnimationsOnHover != null) && restartBackAnimationsOnHover.equalsIgnoreCase("false")) {
						this.getContainerForVanillaButton(b).restartAnimationOnHover = false;
					}
					String backNormal = CustomizationItemBase.fixBackslashPath(sec.getEntryValue("backgroundnormal"));
					String backHover = CustomizationItemBase.fixBackslashPath(sec.getEntryValue("backgroundhovered"));
					if (backNormal != null) {
						this.getContainerForVanillaButton(b).normalBackground = backNormal;
					} else {
						String backAniNormal = sec.getEntryValue("backgroundanimationnormal");
						if (backAniNormal != null) {
							this.getContainerForVanillaButton(b).normalBackground = "animation:" + backAniNormal;
						}
					}
					if (backHover != null) {
						this.getContainerForVanillaButton(b).hoverBackground = backHover;
					} else {
						String backAniHover = sec.getEntryValue("backgroundanimationhovered");
						if (backAniHover != null) {
							this.getContainerForVanillaButton(b).hoverBackground = "animation:" + backAniHover;
						}
					}
				}
			}

			if (action.equalsIgnoreCase("setbuttonclicksound")) {
				if (b != null) {
					String path = CustomizationItemBase.fixBackslashPath(sec.getEntryValue("path"));
					if (path != null) {
						this.getContainerForVanillaButton(b).clickSound = path;
					}
				}
			}

			if (action.equalsIgnoreCase("vanilla_button_visibility_requirements")) {
				if (b != null) {
					this.vanillaButtonLoadingRequirementContainers.put(b, LoadingRequirementContainer.deserializeRequirementContainer(sec));
				}
			}

			if (action.equalsIgnoreCase("addhoversound")) {
				if (b != null) {
					if ((renderOrder != null) && renderOrder.equalsIgnoreCase("background")) {
						backgroundRenderItems.add(new VanillaButtonCustomizationItem(sec, bd, this));
					} else {
						frontRenderItems.add(new VanillaButtonCustomizationItem(sec, bd, this));
					}
				}
			}

			if (action.equalsIgnoreCase("sethoverlabel")) {
				if (b != null) {
					if ((renderOrder != null) && renderOrder.equalsIgnoreCase("background")) {
						backgroundRenderItems.add(new VanillaButtonCustomizationItem(sec, bd, this));
					} else {
						frontRenderItems.add(new VanillaButtonCustomizationItem(sec, bd, this));
					}
				}
			}

			if (action.equalsIgnoreCase("clickbutton")) {
				if (b != null) {
					String clicks = sec.getEntryValue("clicks");
					if ((clicks != null) && (MathUtils.isInteger(clicks))) {
						for (int i = 0; i < Integer.parseInt(clicks); i++) {
							b.mousePressed(Minecraft.getMinecraft(), MouseInput.getMouseX(), MouseInput.getMouseY());
							try {
								
								//Method m = ReflectionHelper.findMethod(GuiScreen.class, "actionPerformed", "func_146284_a", GuiButton.class);
								Method m = ObfuscationReflectionHelper.findMethod(GuiScreen.class, "func_146284_a", Void.class, GuiButton.class);
								m.invoke(Minecraft.getMinecraft().currentScreen, b);
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						}
					}
				}
			}

			if (action.equalsIgnoreCase("addtext")) {
				if ((renderOrder != null) && renderOrder.equalsIgnoreCase("background")) {
					backgroundRenderItems.add(new StringCustomizationItem(sec));
				} else {
					frontRenderItems.add(new StringCustomizationItem(sec));
				}
			}

			if (action.equalsIgnoreCase("addwebtext")) {
				if ((renderOrder != null) && renderOrder.equalsIgnoreCase("background")) {
					backgroundRenderItems.add(new WebStringCustomizationItem(sec));
				} else {
					frontRenderItems.add(new WebStringCustomizationItem(sec));
				}
			}

			if (action.equalsIgnoreCase("addtexture")) {
				if ((renderOrder != null) && renderOrder.equalsIgnoreCase("background")) {
					backgroundRenderItems.add(new TextureCustomizationItem(sec));
				} else {
					frontRenderItems.add(new TextureCustomizationItem(sec));
				}
			}

			if (action.equalsIgnoreCase("addwebtexture")) {
				if ((renderOrder != null) && renderOrder.equalsIgnoreCase("background")) {
					backgroundRenderItems.add(new WebTextureCustomizationItem(sec));
				} else {
					frontRenderItems.add(new WebTextureCustomizationItem(sec));
				}
			}

			if (action.equalsIgnoreCase("addanimation")) {
				if ((renderOrder != null) && renderOrder.equalsIgnoreCase("background")) {
					backgroundRenderItems.add(new AnimationCustomizationItem(sec));
				} else {
					frontRenderItems.add(new AnimationCustomizationItem(sec));
				}
			}

			if (action.equalsIgnoreCase("addshape")) {
				if ((renderOrder != null) && renderOrder.equalsIgnoreCase("background")) {
					backgroundRenderItems.add(new ShapeCustomizationItem(sec));
				} else {
					frontRenderItems.add(new ShapeCustomizationItem(sec));
				}
			}

			if (action.equalsIgnoreCase("addslideshow")) {
				if ((renderOrder != null) && renderOrder.equalsIgnoreCase("background")) {
					backgroundRenderItems.add(new SlideshowCustomizationItem(sec));
				} else {
					frontRenderItems.add(new SlideshowCustomizationItem(sec));
				}
			}

			if (FancyMenu.config.getOrDefault("allow_level_registry_interactions", false)) {
				if (action.equalsIgnoreCase("addentity")) {
					if ((renderOrder != null) && renderOrder.equalsIgnoreCase("background")) {
						backgroundRenderItems.add(new PlayerEntityCustomizationItem(sec));
					} else {
						frontRenderItems.add(new PlayerEntityCustomizationItem(sec));
					}
				}
			}

			if (action.equalsIgnoreCase("addbutton")) {
				ButtonCustomizationItem i = new ButtonCustomizationItem(sec);

				if ((renderOrder != null) && renderOrder.equalsIgnoreCase("background")) {
					backgroundRenderItems.add(i);
				} else {
					frontRenderItems.add(i);
				}
			}

			if (action.equalsIgnoreCase("addaudio")) {
				if (FancyMenu.config.getOrDefault("playbackgroundsounds", true)) {
					if ((Minecraft.getMinecraft().world == null) || FancyMenu.config.getOrDefault("playbackgroundsoundsinworld", false)) {
						String path = CustomizationItemBase.fixBackslashPath(sec.getEntryValue("path"));
						String loopString = sec.getEntryValue("loop");

						boolean loop = false; 
						if ((loopString != null) && loopString.equalsIgnoreCase("true")) {
							loop = true;
						}
						if (path != null) {
							File f = new File(path);
							if (!f.exists() || !f.getAbsolutePath().replace("\\", "/").startsWith(Minecraft.getMinecraft().mcDataDir.getAbsolutePath().replace("\\", "/"))) {
								path = Minecraft.getMinecraft().mcDataDir.getAbsolutePath().replace("\\", "/") + "/" + path;
								f = new File(path);
							}
							if (f.isFile() && f.exists() && f.getName().endsWith(".wav")) {
								try {
									String name = path + Files.size(f.toPath());
									MenuCustomization.registerSound(name, path);
									this.audio.put(name, loop);
								} catch (Exception ex) {
									ex.printStackTrace();
								}
							}
						}
					}
				}
			}
			
			if (action.equalsIgnoreCase("setcloseaudio")) {
				String path = CustomizationItemBase.fixBackslashPath(sec.getEntryValue("path"));

				if (path != null) {
					File f = new File(path);
					if (!f.exists() || !f.getAbsolutePath().replace("\\", "/").startsWith(Minecraft.getMinecraft().mcDataDir.getAbsolutePath().replace("\\", "/"))) {
						path = Minecraft.getMinecraft().mcDataDir.getAbsolutePath().replace("\\", "/") + "/" + path;
						f = new File(path);
					}
					if (f.isFile() && f.exists() && f.getName().endsWith(".wav")) {
						try {
							String name = "closesound_" + path + Files.size(f.toPath());
							MenuCustomization.registerSound(name, path);
							this.closeAudio = name;
							this.sharedLayoutProps.closeAudioSet = true;
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				}
			}

			if (action.equalsIgnoreCase("setopenaudio")) {
				if (MenuCustomization.isNewMenu()) {
					String path = CustomizationItemBase.fixBackslashPath(sec.getEntryValue("path"));
					if (path != null) {
						File f = new File(path);
						if (!f.exists() || !f.getAbsolutePath().replace("\\", "/").startsWith(Minecraft.getMinecraft().mcDataDir.getAbsolutePath().replace("\\", "/"))) {
							path = Minecraft.getMinecraft().mcDataDir.getAbsolutePath().replace("\\", "/") + "/" + path;
							f = new File(path);
						}
						if (f.isFile() && f.exists() && f.getName().endsWith(".wav")) {
							try {
								String name = "opensound_" + path + Files.size(f.toPath());
								MenuCustomization.registerSound(name, path);
								SoundHandler.resetSound(name);
								SoundHandler.playSound(name);
								this.openAudio = name;
								this.sharedLayoutProps.openAudioSet = true;
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						}
					}
				}
			}

			if (action.equalsIgnoreCase("setbuttondescription")) {
				if (b != null) {
					String desc = sec.getEntryValue("description");
					if (desc != null) {
						this.sharedLayoutProps.descriptions.put(bd, de.keksuccino.fancymenu.menu.placeholder.v2.PlaceholderParser.replacePlaceholders(desc));
					}
				}
			}

			if (action.equalsIgnoreCase("addsplash")) {
				String file = CustomizationItemBase.fixBackslashPath(sec.getEntryValue("splashfilepath"));
				String text = sec.getEntryValue("text");
				if ((file != null) || (text != null)) {
					
					SplashTextCustomizationItem i = new SplashTextCustomizationItem(sec);
					
					if ((renderOrder != null) && renderOrder.equalsIgnoreCase("background")) {
						backgroundRenderItems.add(i);
					} else {
						frontRenderItems.add(i);
					}
					
				}
			}

			/** CUSTOM ITEMS (API) **/
			if (action.startsWith("custom_layout_element:")) {
				String cusId = action.split("[:]", 2)[1];
				CustomizationItemContainer cusItem = CustomizationItemRegistry.getItem(cusId);
				if (cusItem != null) {
					CustomizationItem cusItemInstance = cusItem.constructCustomizedItemInstance(sec);
					if ((renderOrder != null) && renderOrder.equalsIgnoreCase("background")) {
						backgroundRenderItems.add(cusItemInstance);
					} else {
						frontRenderItems.add(cusItemInstance);
					}
				}
			}

		}
		
	}

	protected void handleAppearanceDelayFor(CustomizationItemBase i) {
		if (!(i instanceof VanillaButtonCustomizationItem)) {
			if (i.delayAppearance) {
				
				if (i.getActionId() == null) {
					return;
				}
				if (!i.delayAppearanceEverytime && delayAppearanceFirstTime.contains(i.getActionId())) {
					return;
				}
				if (!i.delayAppearanceEverytime) {
					if (!this.delayAppearanceFirstTime.contains(i.getActionId())) {
						delayAppearanceFirstTime.add(i.getActionId());
					}
				}
				
				i.visible = false;
				
				if (i.fadeIn) {
					i.opacity = 0.1F;
				}
				
				ThreadCaller c = new ThreadCaller();
				this.delayThreads.add(c);
				
				Thread t = new Thread(new Runnable() {
					@Override
					public void run() {
						long start = System.currentTimeMillis();
						float delay = (float) (1000.0 * i.delayAppearanceSec);
						boolean fade = false;
						while (c.running.get()) {
							try {
								long now = System.currentTimeMillis();
								if (!fade) {
									if (now >= start + (int)delay) {
										i.visible = true;
										if (!i.fadeIn) {
											return;
										} else {
											fade = true;
										}
									}
								} else {
									float o = i.opacity + (0.03F * i.fadeInSpeed);
									if (o > 1.0F) {
										o = 1.0F;
									}
									if (i.opacity < 1.0F) {
										i.opacity = o;
									} else {
										return;
									}
								}
								
								Thread.sleep(50);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				});
				t.start();
				
			}
		}
	}

	protected void handleVanillaAppearanceDelayFor(ButtonData d) {
		if (this.delayAppearanceVanilla.containsKey(d)) {
			
			boolean fadein = this.fadeInVanilla.containsKey(d);
			float delaysec = this.delayAppearanceVanilla.get(d);

			LoadingRequirementContainer reqs = this.vanillaButtonLoadingRequirementContainers.get(d.getButton());

			d.getButton().visible = false;
			if (reqs != null) {
				reqs.forceRequirementsNotMet = true;
			}
			
			if (fadein) {
				buttonAlphas.put(d.getButton(), 0.1F);
			}
			
			ThreadCaller c = new ThreadCaller();
			this.delayThreads.add(c);
			
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					float fadespeed = 1.0F;
					if (fadein) {
						if (fadeInVanilla.containsKey(d)) {
							fadespeed = fadeInVanilla.get(d);
						}
					}
					float opacity = 0.1F;
					long start = System.currentTimeMillis();
					float delay = (float) (1000.0 * delaysec);
					boolean fade = false;
					while (c.running.get()) {
						try {
							long now = System.currentTimeMillis();
							if (!fade) {
								if (now >= start + (int)delay) {
									d.getButton().visible = true;
									if (reqs != null) {
										reqs.forceRequirementsNotMet = false;
									}
									if (!fadein) {
										return;
									} else {
										fade = true;
									}
								}
							} else {
								float o = opacity + (0.03F * fadespeed);
								if (o > 1.0F) {
									o = 1.0F;
								}
								if (opacity < 1.0F) {
									opacity = o;
									buttonAlphas.put(d.getButton(), opacity);
								} else {
									return;
								}
							}
							
							Thread.sleep(50);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			});
			t.start();
			
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onRenderPre(GuiScreenEvent.DrawScreenEvent.Pre e) {

		if (PopupHandler.isPopupActive()) {
			return;
		}
		if (!this.shouldCustomize(e.getGui())) {
			return;
		}
		if (!MenuCustomization.isMenuCustomizable(e.getGui())) {
			return;
		}

		//Re-init screen if layout-wide requirements changed
		for (Map.Entry<LoadingRequirementContainer, Boolean> m : this.cachedLayoutWideLoadingRequirements.entrySet()) {
			if (m.getKey().requirementsMet() != m.getValue()) {
				e.getGui().setWorldAndResolution(Minecraft.getMinecraft(), e.getGui().width, e.getGui().height);
				break;
			}
		}

	}

	@SubscribeEvent
	public void onRenderPost(GuiScreenEvent.DrawScreenEvent.Post e) {
		if (PopupHandler.isPopupActive()) {
			return;
		}
		if (!this.shouldCustomize(e.getGui())) {
			return;
		}
		if (!MenuCustomization.isMenuCustomizable(e.getGui())) {
			return;
		}

		if (!this.backgroundDrawable) {
			//Rendering all items that SHOULD be rendered in the background IF it's not possible to render them in the background (In this case, they will be forced to render in the foreground)
			List<CustomizationItemBase> backItems = new ArrayList<CustomizationItemBase>();
			backItems.addAll(this.backgroundRenderItems);
			for (CustomizationItemBase i : backItems) {
				try {
					i.render(e.getGui());
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}

		//Rendering all items that should be rendered in the foreground
		List<CustomizationItemBase> frontItems = new ArrayList<CustomizationItemBase>();
		frontItems.addAll(this.frontRenderItems);
		for (CustomizationItemBase i : frontItems) {
			try {
				i.render(e.getGui());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	@SubscribeEvent
	public void drawToBackground(GuiScreenEvent.BackgroundDrawnEvent e) {
		if (!MenuCustomization.isCurrentMenuScrollable()) {
			this.renderBackground(e.getGui());
		}
	}

	protected void renderBackground(GuiScreen s) {
		if (this.shouldCustomize(s)) {
			if (!MenuCustomization.isMenuCustomizable(s)) {
				return;
			}

			//Rendering the background animation to the menu
			if (this.canRenderBackground()) {
				if ((this.backgroundAnimation != null) && this.backgroundAnimation.isReady()) {
					boolean b = this.backgroundAnimation.isStretchedToStreensize();
					int wOri = this.backgroundAnimation.getWidth();
					int hOri = this.backgroundAnimation.getHeight();
					int xOri = this.backgroundAnimation.getPosX();
					int yOri = this.backgroundAnimation.getPosY();
					if (!this.keepBackgroundAspectRatio) {
						this.backgroundAnimation.setStretchImageToScreensize(true);
					} else {
						double ratio = (double) wOri / (double) hOri;
						int wfinal = (int)(s.height * ratio);
						int screenCenterX = s.width / 2;
						if (wfinal < s.width) {
							this.backgroundAnimation.setStretchImageToScreensize(true);
						} else {
							this.backgroundAnimation.setWidth(wfinal + 1);
							this.backgroundAnimation.setHeight(s.height + 1);
							this.backgroundAnimation.setPosX(screenCenterX - (wfinal / 2));
							this.backgroundAnimation.setPosY(0);
						}
					}
					this.backgroundAnimation.setOpacity(this.backgroundOpacity);
					this.backgroundAnimation.render();
					this.backgroundAnimation.setWidth(wOri);
					this.backgroundAnimation.setHeight(hOri);
					this.backgroundAnimation.setPosX(xOri);
					this.backgroundAnimation.setPosY(yOri);
					this.backgroundAnimation.setStretchImageToScreensize(b);
					this.backgroundAnimation.setOpacity(1.0F);
				} else if (this.backgroundTexture != null) {
					GlStateManager.enableBlend();
					Minecraft.getMinecraft().getTextureManager().bindTexture(this.backgroundTexture.getResourceLocation());
					RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.backgroundOpacity);
					if (!this.panoramaback) {
						if (!this.keepBackgroundAspectRatio) {
							Gui.drawModalRectWithCustomSizedTexture(0, 0, 1.0F, 1.0F, s.width + 1, s.height + 1, s.width + 1, s.height + 1);
						} else {
							int w = this.backgroundTexture.getWidth();
							int h = this.backgroundTexture.getHeight();
							double ratio = (double) w / (double) h;
							int wfinal = (int)(s.height * ratio);
							int screenCenterX = s.width / 2;
							if (wfinal < s.width) {
								Gui.drawModalRectWithCustomSizedTexture(0, 0, 1.0F, 1.0F, s.width + 1, s.height + 1, s.width + 1, s.height + 1);
							} else {
								Gui.drawModalRectWithCustomSizedTexture(screenCenterX - (wfinal / 2), 0, 1.0F, 1.0F, wfinal + 1, s.height + 1, wfinal + 1, s.height + 1);
							}
						}
					} else {
						int w = this.backgroundTexture.getWidth();
						int h = this.backgroundTexture.getHeight();
						double ratio = (double) w / (double) h;
						int wfinal = (int)(s.height * ratio);

						//Check if the panorama background should move to the left side or to the ride side
						if ((panoPos + (wfinal - s.width)) <= 0) {
							panoMoveBack = true;
						}
						if (panoPos >= 0) {
							panoMoveBack = false;
						}

						//Fix pos after resizing
						if (panoPos + (wfinal - s.width) < 0) {
							panoPos = 0 - (wfinal - s.width);
						}
						if (panoPos > 0) {
							panoPos = 0;
						}

						if (!panoStop) {
							if (panoTick >= 1) {
								panoTick = 0;
								if (panoMoveBack) {
									panoPos = panoPos + 0.5;
								} else {
									panoPos = panoPos - 0.5;
								}

								if (panoPos + (wfinal - s.width) == 0) {
									panoStop = true;
								}
								if (panoPos == 0) {
									panoStop = true;
								}
							} else {
								panoTick++;
							}
						} else {
							if (panoTick >= 300) {
								panoStop = false;
								panoTick = 0;
							} else {
								panoTick++;
							}
						}
						if (wfinal <= s.width) {
							AbstractGui.blit(0, 0, 1.0F, 1.0F, s.width + 1, s.height + 1, s.width + 1, s.height + 1);
						} else {
							RenderUtils.doubleBlit(panoPos, 0, 1.0F, 1.0F, wfinal, s.height + 1);
						}
					}

					GlStateManager.disableBlend();
					RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

				} else if (this.panoramacube != null) {

					float opacity = this.panoramacube.opacity;
					this.panoramacube.opacity = this.backgroundOpacity;
					this.panoramacube.render();
					this.panoramacube.opacity = opacity;

				} else if (this.slideshow != null) {

					int sw = this.slideshow.width;
					int sh = this.slideshow.height;
					int sx = this.slideshow.x;
					int sy = this.slideshow.y;
					float opacity = this.slideshow.slideshowOpacity;

					if (!this.keepBackgroundAspectRatio) {
						this.slideshow.width = s.width + 1;
						this.slideshow.height = s.height +1;
						this.slideshow.x = 0;
					} else {
						double ratio = (double) sw / (double) sh;
						int wfinal = (int)(s.height * ratio);
						int screenCenterX = s.width / 2;
						if (wfinal < s.width) {
							this.slideshow.width = s.width + 1;
							this.slideshow.height = s.height +1;
							this.slideshow.x = 0;
						} else {
							this.slideshow.width = wfinal + 1;
							this.slideshow.height = s.height +1;
							this.slideshow.x = screenCenterX - (wfinal / 2);
						}
					}
					this.slideshow.y = 0;
					this.slideshow.slideshowOpacity = this.backgroundOpacity;

					this.slideshow.render();

					this.slideshow.width = sw;
					this.slideshow.height = sh;
					this.slideshow.x = sx;
					this.slideshow.y = sy;
					this.slideshow.slideshowOpacity = opacity;
				} else if (this.customMenuBackground != null) {

					this.customMenuBackground.opacity = this.backgroundOpacity;
					this.customMenuBackground.render(s, this.keepBackgroundAspectRatio);
					this.customMenuBackground.opacity = 1.0F;

				}

			}

			if (PopupHandler.isPopupActive()) {
				return;
			}

			//Rendering all items which should be rendered in the background
			List<CustomizationItemBase> backItems = new ArrayList<CustomizationItemBase>();
			backItems.addAll(this.backgroundRenderItems);
			for (CustomizationItemBase i : backItems) {
				try {
					i.render(s);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}

			this.backgroundDrawable = true;
		}
	}

	@SubscribeEvent
	public void onButtonClickSound(PlayWidgetClickSoundEvent.Pre e) {

		if (this.shouldCustomize(Minecraft.getMinecraft().currentScreen)) {
			if (MenuCustomization.isMenuCustomizable(Minecraft.getMinecraft().currentScreen)) {

				ButtonCustomizationContainer c = this.vanillaButtonCustomizations.get(e.getWidget());

				if (c != null) {
					if (c.clickSound != null) {
						File f = new File(c.clickSound);
						if (!f.exists() || !f.getAbsolutePath().replace("\\", "/").startsWith(Minecraft.getMinecraft().mcDataDir.getAbsolutePath().replace("\\", "/"))) {
							c.clickSound = Minecraft.getMinecraft().mcDataDir.getAbsolutePath().replace("\\", "/") + "/" + c.clickSound;
							f = new File(c.clickSound);
						}
						if (f.exists() && f.isFile() && f.getPath().toLowerCase().endsWith(".wav")) {

							SoundHandler.registerSound(f.getPath(), f.getPath());
							SoundHandler.resetSound(f.getPath());
							SoundHandler.playSound(f.getPath());

							e.setCanceled(true);

						}
					}
				}

			}
		}

	}

	@SubscribeEvent
	public void onButtonRenderBackground(RenderWidgetBackgroundEvent.Pre e) {
		if (this.shouldCustomize(Minecraft.getMinecraft().currentScreen)) {
			if (MenuCustomization.isMenuCustomizable(Minecraft.getMinecraft().currentScreen)) {

				GuiButton w = e.getWidget();
				ButtonCustomizationContainer c = this.vanillaButtonCustomizations.get(w);
				if (c != null) {
					String normalBack = c.normalBackground;
					String hoverBack = c.hoverBackground;
					boolean hasCustomBackground = false;
					boolean restart = false;
					if (c.lastHoverState != w.isMouseOver()) {
						if (w.isMouseOver() && c.restartAnimationOnHover) {
							restart = true;
						}
					}
					c.lastHoverState = w.isMouseOver();

					if (!w.isMouseOver()) {
						if (normalBack != null) {
							if (this.renderCustomButtomBackground(e, normalBack, restart)) {
								hasCustomBackground = true;
							}
						}
					}

					if (w.isMouseOver()) {
						if (w.enabled) {
							if (hoverBack != null) {
								if (this.renderCustomButtomBackground(e, hoverBack, restart)) {
									hasCustomBackground = true;
								}
							}
						} else {
							if (normalBack != null) {
								if (this.renderCustomButtomBackground(e, normalBack, restart)) {
									hasCustomBackground = true;
								}
							}
						}
					}

					if (hasCustomBackground) {
						if ((w instanceof GuiButtonImage) || (w instanceof GuiButtonLanguage)) {
							String msg = w.displayString;
							if (msg != null) {
								int j = 14737632;
								if (w.packedFGColour != 0) {
									j = w.packedFGColour;
								} else if (!w.enabled) {
									j = 10526880;
								} else if (w.isMouseOver()) {
									j = 16777120;
								}
								w.drawCenteredString(Minecraft.getMinecraft().fontRenderer, msg, w.x + w.width / 2, w.y + (w.height - 8) / 2, j | MathHelper.ceil(e.getAlpha() * 255.0F) << 24);
							}
						}

						e.setCanceled(true);
					}

				}

			}
		}
	}

	protected boolean renderCustomButtomBackground(RenderWidgetBackgroundEvent e, String background, boolean restartAnimationBackground) {
		GuiButton w = e.getWidget();
		ButtonCustomizationContainer c = this.vanillaButtonCustomizations.get(w);
		if (c != null) {
			if (w != null) {
				if (background != null) {
					if (background.startsWith("animation:")) {
						String aniName = background.split("[:]", 2)[1];
						if (AnimationHandler.animationExists(aniName)) {
							IAnimationRenderer a = AnimationHandler.getAnimation(aniName);
							if (restartAnimationBackground) {
								a.resetAnimation();
							}
							this.renderBackgroundAnimation(e, a);
							if (!c.cachedAnimations.contains(a)) {
								c.cachedAnimations.add(a);
							}
							return true;
						}
					} else {
						File f = new File(background);
						if (!f.exists() || !f.getAbsolutePath().replace("\\", "/").startsWith(Minecraft.getMinecraft().mcDataDir.getAbsolutePath().replace("\\", "/"))) {
							background = Minecraft.getMinecraft().mcDataDir.getAbsolutePath().replace("\\", "/") + "/" + background;
							f = new File(background);
						}
						if (f.isFile()) {
							if (f.getPath().toLowerCase().endsWith(".gif")) {
								IAnimationRenderer a =  TextureHandler.getGifResource(f.getPath());
								if (restartAnimationBackground) {
									a.resetAnimation();
								}
								this.renderBackgroundAnimation(e, a);
								if (!c.cachedAnimations.contains(a)) {
									c.cachedAnimations.add(a);
								}
								return true;
							} else if (f.getPath().toLowerCase().endsWith(".jpg") || f.getPath().toLowerCase().endsWith(".jpeg") || f.getPath().toLowerCase().endsWith(".png")) {
								ExternalTextureResourceLocation back = TextureHandler.getResource(f.getPath());
								if (back != null) {
									RenderUtils.bindTexture(back.getResourceLocation());
									GlStateManager.enableBlend();
									GlStateManager.color(1.0F, 1.0F, 1.0F, e.getAlpha());
									Gui.drawModalRectWithCustomSizedTexture(w.x, w.y, 0.0F, 0.0F, w.width, w.height, w.width, w.height);
									return true;
								}
							}
						}
					}
				}
			}
		}
		return false;
	}

	protected void renderBackgroundAnimation(RenderWidgetBackgroundEvent e, IAnimationRenderer ani) {
		GuiButton w = e.getWidget();
		ButtonCustomizationContainer c = this.vanillaButtonCustomizations.get(w);
		if (c != null) {
			if (ani != null) {
				if (!ani.isReady()) {
					ani.prepareAnimation();
				}

				int aniX = ani.getPosX();
				int aniY = ani.getPosY();
				int aniWidth = ani.getWidth();
				int aniHeight = ani.getHeight();
				boolean aniLoop = ani.isGettingLooped();

				ani.setPosX(w.x);
				ani.setPosY(w.y);
				ani.setWidth(w.width);
				ani.setHeight(w.height);
				ani.setLooped(c.loopAnimation);
				ani.setOpacity(e.getAlpha());
				if (ani instanceof AdvancedAnimation) {
					((AdvancedAnimation) ani).setMuteAudio(true);
				}

				ani.render();

				ani.setPosX(aniX);
				ani.setPosY(aniY);
				ani.setWidth(aniWidth);
				ani.setHeight(aniHeight);
				ani.setLooped(aniLoop);
				ani.setOpacity(1.0F);
				if (ani instanceof AdvancedAnimation) {
					((AdvancedAnimation) ani).setMuteAudio(false);
				}
			}
		}
	}

	protected ButtonCustomizationContainer getContainerForVanillaButton(GuiButton w) {
		if (!this.vanillaButtonCustomizations.containsKey(w)) {
			ButtonCustomizationContainer c = new ButtonCustomizationContainer();
			this.vanillaButtonCustomizations.put(w, c);
			return c;
		}
		return this.vanillaButtonCustomizations.get(w);
	}

	public CustomizationItemBase getItemByActionId(String actionId) {
		for (CustomizationItemBase c : this.backgroundRenderItems) {
			if (c instanceof VanillaButtonCustomizationItem) {
				String id = "vanillabtn:" + ((VanillaButtonCustomizationItem)c).getButtonId();
				if (id.equals(actionId)) {
					return c;
				}
			} else {
				if (c.getActionId().equals(actionId)) {
					return c;
				}
			}
		}
		for (CustomizationItemBase c : this.frontRenderItems) {
			if (c instanceof VanillaButtonCustomizationItem) {
				String id = "vanillabtn:" + ((VanillaButtonCustomizationItem)c).getButtonId();
				if (id.equals(actionId)) {
					return c;
				}
			} else {
				if (c.getActionId().equals(actionId)) {
					return c;
				}
			}
		}
		if (actionId.startsWith("vanillabtn:")) {
			String idRaw = actionId.split("[:]", 2)[1];
			ButtonData d;
			if (MathUtils.isLong(idRaw)) {
				d = ButtonCache.getButtonForId(Long.parseLong(idRaw));
			} else {
				d = ButtonCache.getButtonForCompatibilityId(idRaw);
			}
			if ((d != null) && (d.getButton() != null)) {
				VanillaButtonCustomizationItem vb = new VanillaButtonCustomizationItem(new PropertiesSection("customization"), d, this);
				vb.orientation = "top-left";
				vb.posX = d.getButton().x;
				vb.posY = d.getButton().y;
				vb.width = d.getButton().width;
				vb.height = d.getButton().height;
				return vb;
			}
		}
		return null;
	}

	protected boolean vanillaButtonLoadingRequirementsMet(GuiButton b) {
		LoadingRequirementContainer c = this.vanillaButtonLoadingRequirementContainers.get(b);
		if (c != null) {
			return c.requirementsMet();
		}
		return true;
	}

	@SubscribeEvent
	public void onRenderListBackground(RenderGuiListBackgroundEvent.Post e) {

		GuiScreen s = Minecraft.getMinecraft().currentScreen;
		
		if (this.shouldCustomize(s)) {
			if (MenuCustomization.isMenuCustomizable(s)) {

				//Allow background stuff to be rendered in scrollable GUIs
				if (Minecraft.getMinecraft().currentScreen != null) {
					
					this.renderBackground(s);
					
				}

			}
		}

	}

	private static ButtonData getButton(String identifier) {
		if (identifier.startsWith("%id=")) {
			String p = identifier.split("[=]")[1].replace("%", "");
			if (MathUtils.isLong(p)) {
				return ButtonCache.getButtonForId(Long.parseLong(p));
			} else if (p.startsWith("button_compatibility_id:")) {
				return ButtonCache.getButtonForCompatibilityId(p);
			}
		} else {
			ButtonData b;
			if (I18n.hasKey(identifier)) {
				b = ButtonCache.getButtonForKey(identifier);
			} else {
				b = ButtonCache.getButtonForName(identifier);
			}
			return b;
		}
		return null;
	}

	protected boolean shouldCustomize(GuiScreen menu) {
		if (menu == null) {
			return false;
		}
		if (getMenuIdentifier() != null) {
			if (!this.getMenuIdentifier().equals(menu.getClass().getName())) {
				return false;
			}
		}
		return true;
	}

	public boolean canRenderBackground() {
		return ((this.backgroundAnimation != null) || (this.backgroundTexture != null) || (this.panoramacube != null) || (this.slideshow != null) || (this.customMenuBackground != null));
	}

	public boolean setBackgroundAnimation(int id) {
		if (id < this.backgroundAnimations.size()) {
			this.backgroundAnimationId = id;
			this.backgroundAnimation = this.backgroundAnimations.get(id);
			this.lastBackgroundAnimation = this.backgroundAnimation;
			return true;
		}
		return false;
	}

	public int getCurrentBackgroundAnimationId() {
		return this.backgroundAnimationId;
	}

	public List<IAnimationRenderer> backgroundAnimations() {
		return this.backgroundAnimations;
	}
	
	//TODO change force scaling later, this is high level garbage
	private static boolean isForcescalingAllowed(GuiScreen screen) {
		if (screen instanceof GuiVideoSettings) {
			return false;
		}
		if (screen instanceof GuiControls) {
			return false;
		}
		if (screen instanceof GuiCustomizeSkin) {
			return false;
		}
		if (screen instanceof GuiLanguage) {
			return false;
		}
		if (screen instanceof GuiOptions) {
			return false;
		}
		if (screen instanceof GuiScreenOptionsSounds) {
			return false;
		}
		if (screen instanceof GuiSnooper) {
			return false;
		}
		if (screen instanceof ScreenChatOptions) {
			return false;
		}
		if (screen instanceof GuiScreenResourcePacks) {
			return false;
		}
		if (screen instanceof GuiScreenServerList) {
			return false;
		}
		
		return true;
	}

	private static class ThreadCaller {
		AtomicBoolean running = new AtomicBoolean(true);
	}
	
	public static class RandomLayoutContainer {
		
		public final String id;
		protected List<PropertiesSet> layouts = new ArrayList<PropertiesSet>();
		protected boolean onlyFirstTime = false;
		protected String lastLayoutPath = null;
		
		public MenuHandlerBase parent;
		
		public RandomLayoutContainer(String id, MenuHandlerBase parent) {
			this.id = id;
			this.parent = parent;
		}
		
		public List<PropertiesSet> getLayouts() {
			return this.layouts;
		}
		
		public void addLayout(PropertiesSet layout) {
			this.layouts.add(layout);
		}
		
		public void addLayouts(List<PropertiesSet> layouts) {
			this.layouts.addAll(layouts);
		}
		
		public void clearLayouts() {
			this.layouts.clear();
		}
		
		public void setOnlyFirstTime(boolean b) {
			this.onlyFirstTime = b;
		}
		
		public boolean isOnlyFirstTime() {
			return this.onlyFirstTime;
		}
		
		public void resetLastLayout() {
			this.lastLayoutPath = null;
		}
		
		@Nullable
		public PropertiesSet getRandomLayout() {
			if (!this.layouts.isEmpty()) {
				if ((this.onlyFirstTime || !MenuCustomization.isNewMenu()) && (this.lastLayoutPath != null)) {
					File f = new File(this.lastLayoutPath);
					if (!f.exists() || !f.getAbsolutePath().replace("\\", "/").startsWith(Minecraft.getMinecraft().mcDataDir.getAbsolutePath().replace("\\", "/"))) {
						this.lastLayoutPath = Minecraft.getMinecraft().mcDataDir.getAbsolutePath().replace("\\", "/") + "/" + this.lastLayoutPath;
						f = new File(this.lastLayoutPath);
					}
					if (f.exists()) {
						for (PropertiesSet s : this.layouts) {
							List<PropertiesSection> metas = s.getPropertiesOfType("customization-meta");
							if (metas.isEmpty()) {
								metas = s.getPropertiesOfType("type-meta");
							}
							if (metas.isEmpty()) {
								continue;
							}
							String path = metas.get(0).getEntryValue("path");
							if ((path != null) && path.equals(this.lastLayoutPath)) {
								return s;
							}
						}
					} else {
						MenuCustomization.stopSounds();
						MenuCustomization.resetSounds();
						AnimationHandler.resetAnimations();
						AnimationHandler.resetAnimationSounds();
						AnimationHandler.stopAnimationSounds();
					}
				}
				int i = MathUtils.getRandomNumberInRange(0, this.layouts.size()-1);
				PropertiesSet s = this.layouts.get(i);
				List<PropertiesSection> metas = s.getPropertiesOfType("customization-meta");
				if (metas.isEmpty()) {
					metas = s.getPropertiesOfType("type-meta");
				}
				if (!metas.isEmpty()) {
					String path = metas.get(0).getEntryValue("path");
					if ((path != null)) {
						this.lastLayoutPath = path;
						return s;
					}
				}
			}
			return null;
		}
		
	}

	public boolean isVanillaButtonHidden(GuiButton w) {
		for (ButtonData d : this.hidden) {
			if (d.getButton() == w) {
				return true;
			}
		}
		return false;
	}

	public static class SharedLayoutProperties {
		
		public boolean scaled = false;
		public int autoScaleBaseWidth = 0;
		public int autoScaleBaseHeight = 0;
		public boolean backgroundTextureSet = false;
		public boolean openAudioSet = false;
		public boolean closeAudioSet = false;
		public Map<ButtonData, String> descriptions = new HashMap<ButtonData, String>();
		
	}

	public static class ButtonCustomizationContainer {

		public String normalBackground = null;
		public String hoverBackground = null;
		public boolean loopAnimation = true;
		public boolean restartAnimationOnHover = true;
		public String clickSound = null;
		public String hoverSound = null;
		public String hoverLabel = null;
		public int autoButtonClicks = 0;
		public String customButtonLabel = null;
		public String buttonDescription = null;
		public boolean isButtonHidden = false;
		public LoadingRequirementContainer loadingRequirementContainer = null;

		public List<IAnimationRenderer> cachedAnimations = new ArrayList<IAnimationRenderer>();
		public boolean lastHoverState = false;

	}

}
