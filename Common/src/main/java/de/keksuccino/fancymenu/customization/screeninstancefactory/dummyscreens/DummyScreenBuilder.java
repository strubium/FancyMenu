package de.keksuccino.fancymenu.customization.screeninstancefactory.dummyscreens;

import de.keksuccino.fancymenu.customization.screeninstancefactory.ScreenInstanceFactory;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class DummyScreenBuilder {

    @NotNull
    protected String screenIdentifier;
    @NotNull
    protected Component screenDisplayName;
    @Nullable
    protected List<Component> screenDescription;
    @Nullable
    protected Supplier<? extends Screen> screenProvider;

    public DummyScreenBuilder(@NotNull String screenIdentifier, @NotNull Component screenDisplayName, @Nullable Supplier<? extends Screen> screenProvider) {
        this.screenIdentifier = Objects.requireNonNull(screenIdentifier);
        this.screenDisplayName = Objects.requireNonNull(screenDisplayName);
        this.screenProvider = screenProvider;
    }

    @Nullable
    public Screen tryConstruct() {
        if (this.screenProvider != null) return this.screenProvider.get();
        return ScreenInstanceFactory.tryConstruct(this.screenIdentifier);
    }

    @NotNull
    public String getScreenIdentifier() {
        return this.screenIdentifier;
    }

    @Nullable
    public Supplier<? extends Screen> getScreenProvider() {
        return this.screenProvider;
    }

    @NotNull
    public Component getScreenDisplayName() {
        return this.screenDisplayName;
    }

    public DummyScreenBuilder setScreenDescription(@Nullable List<Component> description) {
        this.screenDescription = description;
        return this;
    }

    @Nullable
    public List<Component> getScreenDescription() {
        return this.screenDescription;
    }

}
