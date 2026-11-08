package majcek210.xplrclose.client.mixin;

import majcek210.xplrclose.client.ui.XplrcloseHUD;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.input.MouseButtonEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {
    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void xplrclose$onClick(MouseButtonEvent event, boolean doubleClick,
                                   CallbackInfoReturnable<Boolean> cir) {
        if (XplrcloseHUD.handleOnClick(event)) cir.setReturnValue(true);
    }
}