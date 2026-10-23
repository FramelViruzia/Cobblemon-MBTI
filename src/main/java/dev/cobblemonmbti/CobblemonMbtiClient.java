package dev.cobblemonmbti;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CobblemonMbtiClient implements ClientModInitializer {
    public static final String MOD_ID = "cobblemon_mbti";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        MbtiConfig.reload();
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public ResourceLocation getFabricId() {
                return ResourceLocation.fromNamespaceAndPath(MOD_ID, "mbti_data");
            }

            @Override
            public void onResourceManagerReload(ResourceManager manager) {
                MbtiBadgeRenderer.resetFailure();
                MbtiData.load(manager);
            }
        });
        ClientCommandRegistrationCallback.EVENT.register(MbtiCommands::register);
    }
}
