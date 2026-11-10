package io.max.monumentum.config.screen;

import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Sizing;
import java.util.ArrayList;
import java.util.List;

final class AnimatedRows extends FlowLayout {
  private static final float TICKS_PER_ROW = 0.6f;

  private final List<Component> rows = new ArrayList<>();
  private int shown;
  private int target;
  private float waited;

  AnimatedRows() {
    super(Sizing.fill(100), Sizing.content(), Algorithm.VERTICAL);
    this.gap(1);
  }

  void setRows(List<? extends Component> newRows) {
    this.clearChildren();
    this.rows.clear();
    this.rows.addAll(newRows);
    this.children(this.rows);
    this.shown = this.rows.size();
    this.target = this.shown;
  }

  void setExpanded(boolean expanded) {
    this.target = expanded ? this.rows.size() : 0;
  }

  @Override
  protected void parentUpdate(float delta, int mouseX, int mouseY) {
    if (this.shown == this.target) {
      this.waited = 0;
      return;
    }
    this.waited += delta;
    while (this.waited >= TICKS_PER_ROW && this.shown != this.target) {
      this.waited -= TICKS_PER_ROW;
      if (this.shown < this.target) {
        this.child(this.rows.get(this.shown++));
      } else {
        this.removeChild(this.rows.get(--this.shown));
      }
    }
  }
}
