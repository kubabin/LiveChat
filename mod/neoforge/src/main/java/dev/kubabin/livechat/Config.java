package dev.kubabin.livechat;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
@EventBusSubscriber(modid = LivechatNeoforge.MODID)
public class Config
{
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.IntValue PORT = BUILDER
            .comment("The port the HTTP server will listen on")
            .defineInRange("port", 8080, 1, 65535);
    private static final ModConfigSpec.ConfigValue<String> HOST = BUILDER
            .comment("The host the HTTP server will bind to")
            .define("host", "0.0.0.0");
    static final ModConfigSpec SPEC = BUILDER.build();
    public static int port;
    public static String host;
    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        port = PORT.get();
        host = HOST.get();
    }
}
