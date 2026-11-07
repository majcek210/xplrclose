package majcek210.xplrclose.client;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.Minecraft;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.world.entity.player.Player;

import net.minecraft.network.chat.Component;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

import net.minecraft.sounds.SoundSource;




public class XplrcloseClient implements ClientModInitializer {
	public static final String MOD_ID = "xplrclose";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);


	static final Identifier PLAYER_NEARBY_SOUND = Identifier.fromNamespaceAndPath("xplrclose", "player_nearby_ping");
	static final SoundEvent PLAYER_NEARBY_SOUND_EVENT = SoundEvent.createVariableRangeEvent(PLAYER_NEARBY_SOUND);

	private int tick = 0;

	int CHECK_PER = 20;
	int RADIUS = 20;


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

		Registry.register(
				BuiltInRegistries.SOUND_EVENT,
				PLAYER_NEARBY_SOUND,
				PLAYER_NEARBY_SOUND_EVENT
		);

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

				var message = "Many players found nearby!";
				var closestRadius = 20.00;

				for (var playerObject: filtered) {

					EspHandler.targetList.add(playerObject.player);
					if (filtered.size() == 1) {
						message = playerObject.player.getPlainTextName() + " is nearby in " + Math.round(playerObject.distance) + " block radius.";
						break;
					}
					if (playerObject.distance < closestRadius) {
						closestRadius = playerObject.distance;
					}
				}
				if (filtered.size() > 1) {
					message = "Multiple players nearby! Closest one is in " + Math.round(closestRadius) + " block radius.";
				}
				if (!filtered.isEmpty()) {
					triggerAlert(message);
				}
				tick = 0;
			}
		});
	}

	private void triggerAlert(String msgContent) {
		if (client.gui == null) return;
		Component message = Component.literal(msgContent);

		playNearbySound();
		client.gui.hud.setOverlayMessage(message, false);
	}
}