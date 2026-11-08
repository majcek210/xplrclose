package majcek210.xplrclose.client;

import java.util.*;

import majcek210.xplrclose.client.ui.XplrcloseHUD;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.BuiltInRegistries;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import majcek210.xplrclose.client.config.XplrcloseConfig;
import majcek210.xplrclose.client.notifications.NotificationManager;





public class XplrcloseClient implements ClientModInitializer {
	public static final String MOD_ID = "xplrclose";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);


	static final Identifier PLAYER_NEARBY_SOUND = Identifier.fromNamespaceAndPath("xplrclose", "player_nearby_ping");
	static final SoundEvent PLAYER_NEARBY_SOUND_EVENT = SoundEvent.createVariableRangeEvent(PLAYER_NEARBY_SOUND);



	private int tick = 0;


	final Minecraft client = Minecraft.getInstance();

	private void playNearbySound() {
		if (client.player == null) return;

		client.player.playSound(
				PLAYER_NEARBY_SOUND_EVENT,
                1.0f,
				1.0f
		);
	}

	private List<Player> detectPlayers(ClientLevel level) {

		final List<Player> players = new ArrayList<>();

		if (level == null) return players;



		for (var player: level.players()) {
			String playerUUID = player.getUUID().toString();
            assert client.player != null;
            String localUUID = client.player.getUUID().toString();
			if (playerUUID.equals(localUUID)) {
				continue;
			}
			players.add(player);
		}
		return players;

	}

	private record NearbyPlayer(Player player, double distance) {}

	private List<NearbyPlayer> filterOnlyClose(List<Player> List, int RADIUS) {

		final List<NearbyPlayer> filteredPlayers = new ArrayList<>();

		for (var player: List) {
			if (player == null || client.player == null) {
				continue;
			}

			var playerPosition = player.position();
			var clientPosition = client.player.position();

			double difference = playerPosition.subtract(clientPosition).horizontalDistance();
			boolean isClose = difference < RADIUS;

			if (isClose) {
				filteredPlayers.add(new NearbyPlayer(player, difference));
			};

		}
		return filteredPlayers;
	}
	@Override
	public void onInitializeClient() {
		LOGGER.info("xplrclose | made by @majcek210");

		XplrcloseConfig.load();
		NotificationManager.reset();

		EspHandler.register();

		Registry.register(
				BuiltInRegistries.SOUND_EVENT,
				PLAYER_NEARBY_SOUND,
				PLAYER_NEARBY_SOUND_EVENT
		);

		int CHECK_PER = XplrcloseConfig.checkInterval();
		int RADIUS = XplrcloseConfig.detectionRadius();

		HudElementRegistry.attachElementBefore(
				VanillaHudElements.CHAT,
				Identifier.fromNamespaceAndPath(MOD_ID, "notifications"),
				XplrcloseHUD::extract
		);


		ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
			if (!(screen instanceof ChatScreen)) return;
			ScreenMouseEvents.allowMouseClick(screen).register((s, event) ->
				!XplrcloseHUD.handleOnClick(event));
		});

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null || client.level == null) {
				EspHandler.targetList.clear();
				NotificationManager.reset();
				tick = 0;
				return;
			}

			NotificationManager.tick();

			tick += 1;
			if (tick >= CHECK_PER) {
				final List<Player> players = detectPlayers(client.level);
				final List<NearbyPlayer> filtered = filterOnlyClose(players, RADIUS);

				EspHandler.targetList.clear();
				StringBuilder names = new StringBuilder();

				var message = "Many players found nearby!";
				var closestRadius = 20.00;
				final Map<UUID, String> close = new HashMap<>();

				for (var playerObject: filtered) {

					close.put(playerObject.player.getUUID(), playerObject.player.getPlainTextName());
					EspHandler.targetList.add(playerObject.player);

				}

				boolean success = NotificationManager.update(close);
				if (success && XplrcloseConfig.soundEnabled()) {
					playNearbySound();
				}
				tick = 0;
			}
		});
	}

	private void triggerAlert(String msgContent) {
		if (client.gui == null) return;
		Component message = Component.literal(msgContent);


		client.gui.hud.setOverlayMessage(message, false);
	}
}