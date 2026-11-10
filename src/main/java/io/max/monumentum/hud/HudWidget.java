package io.max.monumentum.hud;

import io.wispforest.owo.ui.core.Component;
import net.minecraft.util.Identifier;

public interface HudWidget {
	Identifier id();

	String editorLabel();

	boolean isTurnedOn();

	void setTurnedOn(boolean turnedOn);

	boolean isVisibleNow();

	Component buildComponent();

	void updateComponent(Component component);

	HudPosition position();

	void setPosition(HudPosition position);

	void setRedrawRequest(Runnable redrawRequest);
}