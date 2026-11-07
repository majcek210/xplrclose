package majcek210.xplrclose.client;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.Minecraft;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.world.entity.player.Player;

import net.minecraft.network.chat.Component;


public class XplrcloseClient implements ClientModInitializer {
	public static final String MOD_ID = "xplrclose";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);




	private int tick = 0;
	private final EspHandler espRenderer = new EspHandler();

	int CHECK_PER = 20;
	int RADIUS = 20;


	final Minecraft client = Minecraft.getInstance();

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

	private List<NearbyPlayer> filterOnlyClose(List<Player> List) {

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

		EspHandler.register();

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null || client.level == null) {
				EspHandler.targetList.clear();
				return;
			}
			tick += 1;
			if (tick >= CHECK_PER) {
				final List<Player> players = detectPlayers(client.level);
				final List<NearbyPlayer> filtered = filterOnlyClose(players);

				EspHandler.targetList.clear();
				StringBuilder names = new StringBuilder();
				for (var playerObject: filtered) {
					triggerAlert(playerObject.player.getPlainTextName(), playerObject.distance);
					EspHandler.targetList.add(playerObject.player);
				}
				tick = 0;
			}
		});
	}

	private void triggerAlert(String playerName, double radius) {
		if (client.gui == null) return;
		Component message = Component.literal("Player " + playerName + " is in " + Math.round(radius)+ " blocks radius");

		client.gui.hud.setOverlayMessage(message, false);
	}
}