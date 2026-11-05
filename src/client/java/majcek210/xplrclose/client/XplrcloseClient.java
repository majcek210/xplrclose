package majcek210.xplrclose.client;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;


import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.Minecraft;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.world.entity.player.Player;





public class XplrcloseClient implements ClientModInitializer {
	public static final String MOD_ID = "xplrclose";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private int tick = 0;
	private boolean active = false;

	int CHECK_PER = 20;

	final Minecraft client = Minecraft.getInstance();

	private List<Player> detectPlayers(ClientLevel level) {

		final List<Player> players = new ArrayList<>();

		if (level == null) return players;



		players.clear();


		for (var player: level.players()) {
			String playerUUID = player.getUUID().toString();
            assert client.player != null;
            String localUUID = client.player.getUUID().toString();
			if (playerUUID.equals(localUUID)) {
				continue;
			}
			players.add(player);
			LOGGER.info("Player" + player.getName().getString());
		}
		return players;

	}
	@Override
	public void onInitializeClient() {
		LOGGER.info("xplrclose | made by @majcek210");

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null || client.level == null) {
				return;
			}
			tick += 1;
			if (tick >= CHECK_PER) {
				if (active) return;
				active = true;
				final List<Player> players= detectPlayers(client.level);
				LOGGER.info(players.toString());
				active = false;
				tick = 0;
			}
		});
	}
}