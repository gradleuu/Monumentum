package io.max.monumentum.config.screen;

import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.VerticalAlignment;
import io.wispforest.owo.ui.util.Delta;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.RotationAxis;

final class ChevronLabel extends LabelComponent {
  private float rotation = 90f;
  private float targetRotation = 90f;
  static final int SLOT = 11;

  ChevronLabel() {
    super(Text.literal("›"));
    this.horizontalTextAlignment(HorizontalAlignment.CENTER);
    this.verticalTextAlignment(VerticalAlignment.CENTER);
    this.sizing(Sizing.fixed(SLOT), Sizing.fixed(SLOT));
  }

  void setExpanded(boolean expanded) {
    this.targetRotation = expanded ? 90f : 0f;
  }

  @Override
  public void update(float delta, int mouseX, int mouseY) {
    super.update(delta, mouseX, mouseY);
    this.rotation += Delta.compute(this.rotation, this.targetRotation, delta * 0.65f);
  }

  @Override
  public void draw(
      OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
    float centerX = this.x + this.width / 2f;
    float centerY = this.y + this.height / 2f - 0.5f;
    MatrixStack matrices = context.getMatrices();
    matrices.push();
    matrices.translate(centerX, centerY, 0);
    matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(this.rotation));
    matrices.translate(-centerX, -centerY, 0);
    super.draw(context, mouseX, mouseY, partialTicks, delta);
    matrices.pop();
  }
}
