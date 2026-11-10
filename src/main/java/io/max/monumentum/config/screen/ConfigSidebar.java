package io.max.monumentum.config.screen;

import io.max.monumentum.util.Kaleidoscope;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.Surface;
import io.wispforest.owo.ui.core.VerticalAlignment;
import io.wispforest.owo.ui.util.UISounds;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.text.Text;

final class ConfigSidebar {
  private static final int HEADING_PADDING = 0;
  private static final int HEADING_GAP = 0;
  private static final int GUIDE_X = HEADING_PADDING + ChevronLabel.SLOT / 2;
  private static final int ENTRY_TEXT_X = ChevronLabel.SLOT + 3;

  private record ItemView(ConfigPage page, FlowLayout row, LabelComponent label) {}

  private static final class GroupView {
    private final ConfigGroup group;
    private final ChevronLabel chevron;
    private final LabelComponent title;
    private final AnimatedRows rows;
    private final List<ItemView> items = new ArrayList<>();
    private boolean expanded = true;

    private GroupView(
        ConfigGroup group, ChevronLabel chevron, LabelComponent title, AnimatedRows rows) {
      this.group = group;
      this.chevron = chevron;
      this.title = title;
      this.rows = rows;
    }
  }

  private final FlowLayout container;
  private final Consumer<ConfigPage> onPageClicked;
  private final List<GroupView> groups = new ArrayList<>();
  private final List<ItemView> items = new ArrayList<>();
  private ConfigPage selected;

  ConfigSidebar(FlowLayout container, Consumer<ConfigPage> onPageClicked) {
    this.container = container;
    this.onPageClicked = onPageClicked;
  }

  void show(List<ConfigPage> pages) {
    for (ConfigGroup group : ConfigGroup.values()) {
      List<ConfigPage> inGroup = pages.stream().filter(page -> page.group() == group).toList();
      if (!inGroup.isEmpty()) addGroup(group, inGroup);
    }
  }

  void select(ConfigPage page) {
    this.selected = page;
    items.forEach(this::refreshItem);
    groups.forEach(this::refreshGroup);
  }

  private void addGroup(ConfigGroup group, List<ConfigPage> pages) {
    FlowLayout wrapper = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
    FlowLayout heading = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(18));
    heading.alignment(HorizontalAlignment.LEFT, VerticalAlignment.CENTER);
    heading.padding(Insets.left(HEADING_PADDING));
    heading.allowOverflow(true);
    heading.cursorStyle(CursorStyle.HAND);

    ChevronLabel chevron = new ChevronLabel();
    LabelComponent title = Components.label(Text.empty());
    title.cursorStyle(CursorStyle.HAND);
    heading.child(chevron);
    heading.child(title);

    AnimatedRows rows = new AnimatedRows();
    //    rows.surface(ConfigSurfaces.leftGuide(GUIDE_X, ConfigColors.HAIRLINE));

    GroupView view = new GroupView(group, chevron, title, rows);
    for (ConfigPage page : pages) {
      ItemView item = createItem(page);
      view.items.add(item);
      items.add(item);
    }
    rows.setRows(view.items.stream().map(ItemView::row).toList());

    heading
        .mouseDown()
        .subscribe(
            (mouseX, mouseY, button) -> {
              if (button != 0) return false;
              view.expanded = !view.expanded;
              rows.setExpanded(view.expanded);
              refreshGroup(view);
              UISounds.playInteractionSound();
              return true;
            });

    wrapper.child(heading);
    wrapper.child(rows);
    container.child(wrapper);
    groups.add(view);
    refreshGroup(view);
  }

  private ItemView createItem(ConfigPage page) {
    FlowLayout row = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(12));
    row.alignment(HorizontalAlignment.LEFT, VerticalAlignment.CENTER);
    row.padding(Insets.left(ENTRY_TEXT_X));
    row.cursorStyle(CursorStyle.HAND);

    LabelComponent label = Components.label(page.title());
    label.cursorStyle(CursorStyle.HAND);
    row.child(label);

    ItemView item = new ItemView(page, row, label);
    row.mouseDown()
        .subscribe(
            (mouseX, mouseY, button) -> {
              if (button != 0) return false;
              onPageClicked.accept(page);
              UISounds.playInteractionSound();
              return true;
            });
    row.mouseEnter()
        .subscribe(
            () -> {
              if (selected != page) row.surface(Surface.flat(Kaleidoscope.CONFIG_HOVER.rgb()));
            });
    row.mouseLeave().subscribe(() -> refreshItem(item));
    refreshItem(item);
    return item;
  }

  private void refreshItem(ItemView item) {
    boolean isSelected = item.page() == selected;
    item.row()
        .surface(
            isSelected
                ? Surface.flat(Kaleidoscope.CONFIG_SELECTED.rgb())
                : Surface.flat(0x00000000));
    item.label()
        .text(
            item.page()
                .title()
                .copy()
                .setStyle(
                    Kaleidoscope.style(
                        isSelected ? Kaleidoscope.CONFIG_TEXT : Kaleidoscope.CONFIG_TEXT_MUTED)));
  }

  private void refreshGroup(GroupView view) {
    boolean active = selected != null && selected.group() == view.group;
    view.chevron.setExpanded(view.expanded);
    view.chevron.text(
        Text.literal("›")
            .setStyle(
                Kaleidoscope.style(
                    active ? Kaleidoscope.CONFIG_ACCENT : Kaleidoscope.CONFIG_TEXT_FAINT)));
    view.title.text(
        Text.literal(view.group.title().getString())
            .setStyle(
                Kaleidoscope.style(
                    active ? Kaleidoscope.CONFIG_TEXT : Kaleidoscope.CONFIG_TEXT_FAINT)));
  }
}
