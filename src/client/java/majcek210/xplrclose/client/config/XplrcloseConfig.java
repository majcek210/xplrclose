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

    private static final Set<UUID> ignoredPlayers = new HashSet<>();

    private static int detectionRadius = 20;
    private static int checkTickInterval = 20;
    private static int notificationDuration = 5;

    private static boolean soundEnabled = true;

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            save();
            return;
        }

        try (InputStream input = Files.newInputStream(CONFIG_PATH)) {
            properties.clear();
            properties.load(input);

            soundEnabled = Boolean.parseBoolean(properties.getProperty("sound", "true"));

            detectionRadius = Integer.parseInt(properties.getProperty("radius", "20"));
            checkTickInterval = Integer.parseInt(properties.getProperty("checkInterval", "20"));
            notificationDuration = Integer.parseInt(properties.getProperty("notificationDuration", "5"));

            ignoredPlayers.clear();
            int count = Integer.parseInt(properties.getProperty("ignored.count", "0"));

            for (int i = 0; i < count; i++) {
                String uuid = properties.getProperty("ignored." + i);

                if (uuid != null) {
                    try {
                        ignoredPlayers.add(UUID.fromString(uuid));
                    } catch (IllegalArgumentException ignored) {}
                }
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

        int index = 0;

        for (UUID uuid: ignoredPlayers) {
            properties.setProperty("ignored." + index++, uuid.toString());
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

    public static void ignorePlayer(UUID uuid) {
       ignoredPlayers.add(uuid);

       save();
    }

    public static boolean isIgnored(UUID uuid) {
        return ignoredPlayers.contains(uuid);
    }

    public static void uningnorePlayer(UUID uuid) {
        ignoredPlayers.remove(uuid);
        save();
    }

    public static Set<UUID> getIgnoredPlayers() {
        return Collections.unmodifiableSet(ignoredPlayers);
    }
}
