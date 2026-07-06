package dev.kubabin.livechat;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
@EventBusSubscriber(modid = Livechat.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec.ConfigValue<String> SOCKET_HOST = BUILDER.comment("The host to use for the server").define("socketHost", "0.0.0.0");
    public static final ModConfigSpec.ConfigValue<Integer> SOCKET_PORT = BUILDER.comment("The port to use for the server").defineInRange("socketPort", 8080, 1, 65535);

    static final ModConfigSpec SPEC = BUILDER.build();


    public static String socketHost;
    public static int socketPort;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        socketHost = SOCKET_HOST.get();
        socketPort = SOCKET_PORT.get();
    }
}
