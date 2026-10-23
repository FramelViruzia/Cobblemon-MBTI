package dev.cobblemonmbti.mixin;

import com.cobblemon.mod.common.client.gui.summary.widgets.screens.info.InfoWidget;
import com.cobblemon.mod.common.pokemon.Pokemon;
import dev.cobblemonmbti.MbtiBadgeRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * InfoWidget is only created while Summary's Info tab is open (Summary.displayMainScreen), so no tab check is
 * needed. Cobblemon declares the constructor parameter as "private val pokemon: Pokemon".
 */
@Mixin(InfoWidget.class)
public abstract class InfoWidgetMixin {

    @Shadow(remap = false)
    @Final
    private Pokemon pokemon;

    @Inject(method = "renderWidget", at = @At("TAIL"))
    private void cobblemonMbti$drawBadge(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        AbstractWidget self = (AbstractWidget) (Object) this;
        MbtiBadgeRenderer.render(context, this.pokemon, self.getX(), self.getY());
    }
}
