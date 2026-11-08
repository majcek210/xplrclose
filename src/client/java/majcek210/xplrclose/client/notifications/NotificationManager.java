package majcek210.xplrclose.client.notifications;

import majcek210.xplrclose.Xplrclose;
import majcek210.xplrclose.client.config.XplrcloseConfig;

import java.util.*;


public class NotificationManager {

    public static final class Notification {
        public final UUID playerUuid;
        public final String playerName;
        public final long createdAt = System.currentTimeMillis();

        Notification(UUID playerUuid, String playerName) {
            this.playerUuid = playerUuid;
            this.playerName = playerName;

        }
    }

    private static final int MAX_NOTI = 4;
    private static final List<Notification> active = new ArrayList<>();
    private static  final Set<UUID> nearby = new HashSet<>();


    private NotificationManager() {

    }

    public static void tick() {
        long maxAge = XplrcloseConfig.notificationDuration() * 1000L;
        long now = System.currentTimeMillis();
        active.removeIf(n -> now - n.createdAt > maxAge);
    }

    public static List<Notification> getActive() {
        return Collections.unmodifiableList(active);
    }
    public static void reset() {
        active.clear();
    }

    public static void dismiss(Notification notifi) {
        active.remove(notifi);
    }

    public static void ignoreAndDismiss(Notification notifi) {
        XplrcloseConfig.ignorePlayer(notifi.playerUuid);
        active.remove(notifi);
    }

    public static boolean update(Map<UUID, String> nowNearby) {
        boolean entered = false;

        for (Map.Entry<UUID, String> e : nowNearby.entrySet()) {
            if (!nearby.contains(e.getKey())) {
                entered = true;
                pushNotification(e.getKey(), e.getValue());
            }
        }

        nearby.clear();
        nearby.addAll(nowNearby.keySet());
        return entered;
    }
    public static void pushNotification(UUID uuid, String name) {
        active.add(new Notification(uuid, name));
        while (active.size() > MAX_NOTI) {
            active.removeFirst();
        }

    }


}
