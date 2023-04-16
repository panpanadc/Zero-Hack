package me.zero.mixins.core.gui;

import cum.xiaomao.zerohack.command.CommandManager;
import cum.xiaomao.zerohack.gui.mc.ZeroGuiChat;
import cum.xiaomao.zerohack.util.Wrapper;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiChat.class)
public abstract class MixinGuiChat extends GuiScreen {

    @Shadow protected GuiTextField inputField;
    @Shadow private String historyBuffer;
    @Shadow private int sentHistoryCursor;

    @Inject(method = "keyTyped(CI)V", at = @At("RETURN"))
    public void returnKeyTyped(char typedChar, int keyCode, CallbackInfo info) {
        GuiScreen currentScreen = Wrapper.getMinecraft().currentScreen;
        if (currentScreen instanceof GuiChat && !(currentScreen instanceof ZeroGuiChat)
            && inputField.getText().startsWith(CommandManager.INSTANCE.getPrefix())) {
            Wrapper.getMinecraft().displayGuiScreen(new ZeroGuiChat(inputField.getText(), historyBuffer, sentHistoryCursor));
        }
    }

}
