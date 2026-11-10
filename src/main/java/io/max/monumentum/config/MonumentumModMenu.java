package io.max.monumentum.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import io.max.monumentum.Monumentum;
import io.max.monumentum.config.screen.MonumentumConfigScreen;

public final class MonumentumModMenu implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return parent -> new MonumentumConfigScreen(parent, Monumentum.CONFIG);
	}
}
