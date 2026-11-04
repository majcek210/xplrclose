package majcek210.xplrclose.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.api.ModInitializer;

import net.minecraft.client.multiplayer.ClientLevel;

import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




public class XplrcloseClient implements ClientModInitializer {
	public static final String MOD_ID = "xplrclose";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private int tick = 0;
	private boolean active = false;

	int CHECK_PER = 20;


	private void detectPlayers(ClientLevel level) {
		if (level == null) return;

		for (var player: level.players()) {
			LOGGER.info("Player" + player.getName().getString());
		};

	};

	@Override
	public void onInitializeClient() {
		LOGGER.info("xplrclose | made by @majcek210");

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null || client.level == null) {
				return;
			};
			tick += 1;
			if (tick >= CHECK_PER) {
				if (active) return;
				active = true;
				detectPlayers(client.level);
				LOGGER.info("something should fire here");
				active = false;
				tick = 0;
			};
		});
	}
}