package majcek210.xplrclose.client.config;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class XplrcloseConfig {

    private static final Path  CONFIG_PATH =
            FabricLoader.getInstance()
                    .getConfigDir()
                    .resolve("xplrclose.properties");

    private static final Properties properties = new Properties();

    private static final Map<UUID, String> ignoredPlayers = new LinkedHashMap<>();

    private static int detectionRadius = 40;
    private static int checkTickInterval = 20;
    private static int notificationDuration = 5;

    private static boolean soundEnabled = true;

    private static int safeInt(String key, int def) {
        try {
            return Integer.parseInt(properties.getProperty(key, String.valueOf(def)).trim());
        } catch(NumberFormatException e) {
            return def;
        }
    }

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            save();
            return;
        }

        try (InputStream input = Files.newInputStream(CONFIG_PATH)) {
            properties.clear();
            properties.load(input);

            soundEnabled = Boolean.parseBoolean(properties.getProperty("sound", "true"));

            detectionRadius = safeInt("radius", 40);
            checkTickInterval = safeInt("checkInterval", 20);
            notificationDuration = safeInt("notificationDuration", 5);

            ignoredPlayers.clear();
            int count = safeInt("ignored.count", 0);

            for (int i = 0; i < count; i++) {
                String id = properties.getProperty("ignored." + i);
                if (id == null) continue;
                try {
                    UUID uuid = UUID.fromString(id);
                    ignoredPlayers.put(uuid, properties.getProperty("ignored." + i + ".name", id));
                } catch (IllegalArgumentException ignored) {}
            }

        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        properties.clear();

        properties.setProperty("sound", String.valueOf(soundEnabled));

        properties.setProperty("radius", String.valueOf(detectionRadius));
        properties.setProperty("checkInterval", String.valueOf(checkTickInterval));
        properties.setProperty("notificationDuration", String.valueOf(notificationDuration));

        properties.setProperty("ignored.count",
                String.valueOf(ignoredPlayers.size()));

        int i = 0;
        for (var e : ignoredPlayers.entrySet()) {
            properties.setProperty("ignored." + i, e.getKey().toString());
            properties.setProperty("ignored." + i + ".name", e.getValue());
            i++;
        }

        try {
            Files.createDirectories(CONFIG_PATH.getParent());

            try (OutputStream output = Files.newOutputStream(CONFIG_PATH)) {
                properties.store(output, "Xplrclose Configuration");
            } catch(IOException e) {
                e.printStackTrace();
            }

        }  catch(IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean soundEnabled() {
        return soundEnabled;
    }

    public static void setSoundEnabled(boolean state) {
        soundEnabled = state;
        save();
    }

    public static int detectionRadius() {
        return detectionRadius;
    }

    public static void setDetectionRadius(int numb) {
        detectionRadius = numb;
        save();
    }

    public static int checkInterval() {
        return checkTickInterval;
    }

    public static void setCheckInterval(int numb) {
        checkTickInterval = numb;
        save();
    }

    public static int notificationDuration() {
        return notificationDuration;
    }

    public static void setNotificationDuration(int numb) {
        notificationDuration = numb;
        save();
    }

    public static void ignorePlayer(UUID uuid, String name) {
       ignoredPlayers.put(uuid, name);

       save();
    }

    public static boolean isIgnored(UUID uuid) {
        return ignoredPlayers.containsKey(uuid);
    }


    // currently is not used yet
    public static void unignorePlayer(UUID uuid) {
        ignoredPlayers.remove(uuid);
        save();
    }

    public static Map<UUID, String> getIgnoredPlayers() {
        return Collections.unmodifiableMap(ignoredPlayers);
    }

    public static String uningnorePlayerByName(String name) {
        for (var e : ignoredPlayers.entrySet()) {
            if (e.getValue().equalsIgnoreCase(name)) {
                ignoredPlayers.remove(e.getKey());
                save();
                return e.getValue();
            }
        }
        return null;
    }
}
