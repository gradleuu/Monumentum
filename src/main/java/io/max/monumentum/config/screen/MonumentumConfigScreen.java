package io.max.monumentum.config.screen;

import io.max.monumentum.Monumentum;
import io.max.monumentum.util.Kaleidoscope;
import io.max.monumentum.util.font.Glyph;
import io.wispforest.owo.config.ConfigWrapper;
import io.wispforest.owo.config.Option;
import io.wispforest.owo.config.ui.ConfigScreen;
import io.wispforest.owo.config.ui.OptionComponentFactory;
import io.wispforest.owo.config.ui.OptionComponents;
import io.wispforest.owo.config.ui.component.ConfigTextBox;
import io.wispforest.owo.config.ui.component.ListOptionContainer;
import io.wispforest.owo.config.ui.component.SearchAnchorComponent;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.CollapsibleContainer;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.ParentComponent;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.Surface;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIParsing;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public final class MonumentumConfigScreen extends ConfigScreen {
  private static final String GITHUB_URL = "https://github.com/gradleuu/Monumentum";
  private static final String WIKI_URL = "https://deepwiki.com/gradleuu/Monumentum";

  private static final Predicate<Option<?>> BOOLEAN_OPTION =
      option -> option.clazz() == Boolean.class || option.clazz() == boolean.class;
  private static final OptionComponentFactory<Boolean> BOOLEAN_FACTORY =
      MonumentumConfigScreen::makeBooleanOption;

  private static boolean componentsRegistered;

  private final List<ConfigPage> pages = new ArrayList<>();
  private ConfigSidebar sidebar;
  private FlowLayout optionPanel;
  private ScrollContainer optionScroll;
  private FlowLayout footerStatuses;
  private LabelComponent searchMatchIndicator;
  private TextBoxComponent searchField;

  public static synchronized void registerCustomComponents() {
    if (componentsRegistered) return;
    UIParsing.registerFactory("monumentum-toggle-button", element -> new MonumentumToggleButton());
    componentsRegistered = true;
  }

  public MonumentumConfigScreen(Screen parent, ConfigWrapper<?> config) {
    super(new Identifier(Monumentum.MOD_ID, "config"), config, parent);
    this.extraFactories.put(BOOLEAN_OPTION, BOOLEAN_FACTORY);
  }

  @Override
  protected void build(FlowLayout root) {
    this.pages.clear();
    super.build(root);

    root.surface(Surface.flat(Kaleidoscope.BLACK.argb()));

    bindTopBar(root);
    bindPanels(root);
    styleRuntimeComponents(root, Collections.newSetFromMap(new IdentityHashMap<>()));
    bindFooter();

    createPages();
    this.sidebar =
        new ConfigSidebar(root.childById(FlowLayout.class, "page-sidebar-items"), this::selectPage);
    this.sidebar.show(this.pages);
    selectPage(this.pages.get(0));
  }

  private void bindTopBar(FlowLayout root) {
    root.childById(LabelComponent.class, "title")
        .text(Text.translatable("monumentum.config.title").styled(style -> style.withBold(true)));
    root.childById(LabelComponent.class, "version-label")
        .text(Text.translatable("monumentum.config.version", modVersion()));
    root.childById(LabelComponent.class, "brand-logo").text(Glyph.MONUMENTUM_LOGO.rawText());

    FlowLayout actions = root.childById(FlowLayout.class, "topbar-actions");
    bindLink(
        actions, root.childById(ButtonComponent.class, "github-button"), Glyph.GITHUB, GITHUB_URL);
    bindLink(actions, root.childById(ButtonComponent.class, "wiki-button"), Glyph.WEB, WIKI_URL);
  }

  private static void bindLink(
      FlowLayout actions, ButtonComponent button, Glyph glyph, String url) {
    if (url.isBlank()) {
      actions.removeChild(button);
      return;
    }
    button.setMessage(glyph.rawText());
    button.onPress(pressed -> Util.getOperatingSystem().open(url));
  }

  private void bindPanels(FlowLayout root) {
    FlowLayout mainPanel = root.childById(FlowLayout.class, "main-panel");
    FlowLayout sidebarPanel = root.childById(FlowLayout.class, "page-sidebar");
    FlowLayout contentPanel = root.childById(FlowLayout.class, "option-panel-container");
    FlowLayout toolbar = root.childById(FlowLayout.class, "page-toolbar");

    this.optionScroll = root.childById(ScrollContainer.class, "option-panel-scroll");
    this.optionPanel = root.childById(FlowLayout.class, "option-panel");
    this.searchField = root.childById(TextBoxComponent.class, "search-field");
    this.searchMatchIndicator = root.childById(LabelComponent.class, "search-match-indicator");
    this.footerStatuses = root.childById(FlowLayout.class, "footer-statuses");

    root.childById(LabelComponent.class, "search-icon")
            .text(Glyph.GLYPH_EC06.text(Kaleidoscope.CONFIG_TEXT_FAINT));
    this.searchField.setSuggestion(I18n.translate("monumentum.config.search.placeholder"));

    for (Component child : new ArrayList<>(mainPanel.children())) {
      if (child != sidebarPanel && child != contentPanel) mainPanel.removeChild(child);
    }
    for (Component child : new ArrayList<>(contentPanel.children())) {
      if (child != toolbar && child != this.optionScroll) contentPanel.removeChild(child);
    }
  }

  private void bindFooter() {
    if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
      addFooterStatus(
          Glyph.DEBUG,
          Text.translatable("monumentum.config.status.development"),
          Kaleidoscope.CONFIG_WARNING.rgb());
    }
  }

  private void addFooterStatus(Glyph glyph, Text tooltip, int color) {
    LabelComponent icon = Components.label(glyph.text(color & 0xFFFFFF));
    icon.tooltip(tooltip);
    this.footerStatuses.child(icon);
  }

  private void createPages() {
    List<Component> general = new ArrayList<>();
    for (Component child : new ArrayList<>(this.optionPanel.children())) {
      this.optionPanel.removeChild(child);
      if (child instanceof CollapsibleContainer category
          && !(child instanceof ListOptionContainer<?>)) {
        List<Component> options = new ArrayList<>(category.collapsibleChildren());
        options.forEach(category::removeChild);
        if (!options.isEmpty()) this.pages.add(createPage(categoryKey(category), options));
      } else {
        general.add(child);
      }
    }
    if (!general.isEmpty() || this.pages.isEmpty())
      this.pages.add(0, createPage("general", general));
  }

  private ConfigPage createPage(String key, List<Component> options) {
    String translationKey = "text.config." + this.config.name() + ".category." + key;
    Text title =
        I18n.hasTranslation(translationKey)
            ? Text.translatable(translationKey)
            : Text.literal(humanize(key));
    Text subtitle =
        I18n.hasTranslation(translationKey + ".description")
            ? Text.translatable(translationKey + ".description")
            : Text.empty();

    List<Component> components = new ArrayList<>();
    components.add(pageHeader(title, subtitle));
    components.addAll(options);
    return new ConfigPage(key, title, subtitle, ConfigGroup.of(key), components);
  }

  private static FlowLayout pageHeader(Text title, Text subtitle) {
    FlowLayout header = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
    header.gap(2);
    header.padding(Insets.of(4));
    header.margins(Insets.bottom(4));
    header.child(
        Components.label(
            title.copy().setStyle(Kaleidoscope.style(Kaleidoscope.CONFIG_TEXT).withBold(true))));
    if (!subtitle.getString().isEmpty()) {
      header.child(
          Components.label(subtitle.copy().setStyle(Kaleidoscope.style(Kaleidoscope.CONFIG_TEXT_MUTED))));
    }
    return header;
  }

  private void selectPage(ConfigPage page) {
    this.optionPanel.clearChildren();
    this.optionPanel.children(page.components());
    this.sidebar.select(page);
    resetSearch();
    this.optionScroll.scrollTo(page.components().get(0));
  }

  private void resetSearch() {
    this.currentMatches = null;
    this.currentMatchIndex = 0;
    this.searchMatchIndicator.text(Text.empty());
    this.searchField.text("");
    this.searchField.setEditableColor(TextBoxComponent.DEFAULT_EDITABLE_COLOR);
    this.lastSearchFieldText = "";
  }

  private static String categoryKey(CollapsibleContainer category) {
    for (Component component : category.titleLayout().children()) {
      if (!(component instanceof LabelComponent label)) continue;
      if (!(label.text().getContent() instanceof TranslatableTextContent translated)) continue;
      String key = translated.getKey();
      int marker = key.lastIndexOf(".category.");
      if (marker >= 0) return key.substring(marker + ".category.".length());
    }
    return "options";
  }

  private static String humanize(String key) {
    StringBuilder result = new StringBuilder();
    for (String word : key.replace('-', '_').split("_")) {
      if (word.isEmpty()) continue;
      if (!result.isEmpty()) result.append(' ');
      result.append(word.substring(0, 1).toUpperCase(Locale.ROOT)).append(word.substring(1));
    }
    return result.isEmpty() ? "Options" : result.toString();
  }

  private static String modVersion() {
    return FabricLoader.getInstance()
        .getModContainer(Monumentum.MOD_ID)
        .map(container -> container.getMetadata().getVersion().getFriendlyString())
        .orElse("dev");
  }

  private static OptionComponentFactory.Result<FlowLayout, MonumentumToggleButton>
      makeBooleanOption(UIModel model, Option<Boolean> option) {
    var optionComponent =
        model.expandTemplate(
            FlowLayout.class,
            "boolean-toggle-config-option",
            OptionComponents.packParameters(option.translationKey(), option.value().toString()));
    var toggleButton = optionComponent.childById(MonumentumToggleButton.class, "toggle-button");
    var resetButton = optionComponent.childById(ButtonComponent.class, "reset-button");

    toggleButton.enabled(option.value());
    if (option.detached()) {
      resetButton.active = false;
      toggleButton.active(false);
    } else {
      resetButton.active = option.value() != option.defaultValue();
      resetButton.onPress(
          button -> {
            toggleButton.enabled(option.defaultValue());
            button.active = false;
          });
      toggleButton.onPress(
          button -> resetButton.active = toggleButton.parsedValue() != option.defaultValue());
    }

    optionComponent.child(
        new SearchAnchorComponent(
            optionComponent,
            option.key(),
            () -> optionComponent.childById(LabelComponent.class, "option-name").text().getString(),
            () -> toggleButton.getMessage().getString()));
    return new OptionComponentFactory.Result<>(optionComponent, toggleButton);
  }

  private static void styleRuntimeComponents(Component component, Set<Component> visited) {
    if (!visited.add(component)) return;

    if (component instanceof CollapsibleContainer collapsible) {
      collapsible.surface(ConfigSurfaces.BOTTOM_HAIRLINE);
      collapsible.titleLayout().surface(Surface.flat(0x00000000));
      if (collapsible.children().size() > 1
          && collapsible.children().get(1) instanceof FlowLayout content) {
        content.surface(ConfigSurfaces.leftGuide(6, Kaleidoscope.CONFIG_HAIRLINE.rgb()));
        content.padding(Insets.left(13));
      }

      for (Component titleChild : new ArrayList<>(collapsible.titleLayout().children())) {
        if (titleChild instanceof LabelComponent label) {
          label.text(label.text().copy().setStyle(Kaleidoscope.style(Kaleidoscope.CONFIG_TEXT)));
        }
        if (titleChild instanceof ButtonComponent button
            && !(button instanceof MonumentumToggleButton)) {
          styleButton(button);
        }
      }

      if (collapsible instanceof ListOptionContainer<?> list) {
        for (Component entry : list.collapsibleChildren()) {
          if (!(entry instanceof FlowLayout row)) continue;
          row.surface(Surface.flat(Kaleidoscope.CONFIG_CONTROL.rgb()));
          row.padding(Insets.left(5));
          for (Component field : row.children()) {
            if (field instanceof ConfigTextBox input) {
              input.setDrawsBackground(false);
              input.validColor(Kaleidoscope.CONFIG_TEXT.rgb());
              input.invalidColor(Kaleidoscope.CONFIG_INVALID.rgb());
            }
            if (field instanceof ButtonComponent button) styleButton(button);
          }
        }
      }

      for (Component nested : collapsible.collapsibleChildren())
        styleRuntimeComponents(nested, visited);
    }

    if (component instanceof ParentComponent parent) {
      for (Component child : new ArrayList<>(parent.children()))
        styleRuntimeComponents(child, visited);
    }
  }

  private static void styleButton(ButtonComponent button) {
    button.renderer(
        ButtonComponent.Renderer.flat(0x00000000, Kaleidoscope.CONFIG_CONTROL_HOVER.rgb(), 0x00000000));
    button.textShadow(false);
  }
}
