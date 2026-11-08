package majcek210.xplrclose.client.ui;

import java.util.List;


import majcek210.xplrclose.Xplrclose;
import majcek210.xplrclose.client.notifications.NotificationManager;
import majcek210.xplrclose.client.ui.NotificationUI.Rectangle;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import com.mojang.blaze3d.platform.InputConstants;

public class XplrcloseHUD {
    private XplrcloseHUD() {}

    static public void extract(GuiGraphicsExtractor g, DeltaTracker dt) {
        Minecraft instance = Minecraft.getInstance();

        int sw = instance.getWindow().getGuiScaledWidth();
        int sh = instance.getWindow().getGuiScaledHeight();

        List<NotificationManager.Notification> list = NotificationManager.getActive();

        for (int i = 0; i < list.size(); i++) {
            Rectangle box = NotificationUI.box(i, sw, sh);
            g.fill(box.x(), box.y(), box.x() + box.w(), box.y() + box.h(), 0xE0202020);
            g.outline(box.x(), box.y(), box.w(), box.h(),0xFF3A96DD);
            g.text(instance.font, "Player nearby", box.x() + 6, box.y() + 5, 0xFFFFFFFF, false);
            g.text(instance.font, list.get(i).playerName, box.x() + 6, box.y() + 17, 0xFFAAAAAA, false);

            Rectangle closeButton = NotificationUI.closeButton(box);
            g.text(instance.font, "X", closeButton.x() + 3, closeButton.y() + 2, 0xFFFFFFFF, false);

            Rectangle ignoreButton = NotificationUI.ignoreButton(box);
            g.fill(ignoreButton.x(), ignoreButton.y(), ignoreButton.x() + ignoreButton.w(), ignoreButton.y() + ignoreButton.h(), 0xFF444444);
            g.text(instance.font, "Ignore", ignoreButton.x() + 6, ignoreButton.y() + 2, 0xFFFFFFFF, false);
        }
    }

    public static boolean handleOnClick(MouseButtonEvent event) {
        if (event.button() != InputConstants.MOUSE_BUTTON_LEFT) return false;

        double mx = event.x();
        double my = event.y();

        Minecraft instance = Minecraft.getInstance();

        int sw = instance.getWindow().getGuiScaledWidth();
        int sh = instance.getWindow().getGuiScaledHeight();

        List<NotificationManager.Notification> list = NotificationManager.getActive();

        for (int i = 0; i < list.size(); i++) {
            Rectangle box = NotificationUI.box(i, sw, sh);
            NotificationManager.Notification notifi = list.get(i);

            if (NotificationUI.closeButton(box).contains(mx, my)) {
                NotificationManager.dismiss(notifi);
                return true;
            }

            if (NotificationUI.ignoreButton(box).contains(mx, my)) {
                NotificationManager.ignoreAndDismiss(notifi);
                return true;
            }

        }
        return false;
    }
}
