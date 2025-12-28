package com.xsasakihaise.hellaspatcher;

import com.xsasakihaise.hellaspatcher.HellasPatcherInfoConfig;
import com.xsasakihaise.hellaspatcher.commands.PatcherVersionCommand;
import com.xsasakihaise.hellaspatcher.commands.PatcherDependenciesCommand;
import com.xsasakihaise.hellaspatcher.commands.PatcherFeaturesCommand;
import com.xsasakihaise.hellascontrol.api.CoreCheck;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.api.distmarker.Dist;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

@Mod("hellaspatcher")
public class HellasPatcher {

    public static HellasPatcherInfoConfig infoConfig;
    private static final Logger LOGGER = LogManager.getLogger("HellasPatcher");
    private static final String ENTITLEMENT_KEY = "hellaspatcher";
    private static volatile boolean ENABLED = false;
    private static volatile String DISABLE_REASON = "UNINITIALIZED";

    public HellasPatcher() {
        // Config initialisieren und Defaults sofort laden, damit Commands valide Werte haben
        infoConfig = new HellasPatcherInfoConfig();
        infoConfig.loadDefaultsFromResource();

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onCommonSetup);
        // Event-Bus registrieren für Serverstart und Command-Registration
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(this::initGate);
    }

    private void initGate() {
        if (FMLEnvironment.dist != Dist.DEDICATED_SERVER) {
            ENABLED = true;
            DISABLE_REASON = "OK (non-dedicated)";
            return;
        }

        if (!ModList.get().isLoaded("hellascontrol")) {
            ENABLED = false;
            DISABLE_REASON = "HellasControl missing";
            LOGGER.warn("[HellasPatcher] disabled: {}", DISABLE_REASON);
            return;
        }

        try {
            CoreCheck.verifyCoreLoaded();
            CoreCheck.verifyEntitled(ENTITLEMENT_KEY);
            ENABLED = true;
            DISABLE_REASON = "OK";
            LOGGER.info("[HellasPatcher] enabled (license OK) entitlement='{}'", ENTITLEMENT_KEY);
        } catch (Exception exception) {
            ENABLED = false;
            DISABLE_REASON = "License invalid";
            LOGGER.warn("[HellasPatcher] disabled: {} entitlement='{}'", DISABLE_REASON, ENTITLEMENT_KEY, exception);
        }
    }

    @SubscribeEvent
    public void onServerStart(FMLServerStartingEvent event) {
        if (!ENABLED) {
            return;
        }
        File serverRoot = event.getServer().getServerDirectory();
        infoConfig.load(serverRoot); // Überschreibt ggf. mit Serverdatei
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        if (!ENABLED) {
            return;
        }
        PatcherVersionCommand.register(event.getDispatcher(), infoConfig);
        PatcherDependenciesCommand.register(event.getDispatcher(), infoConfig);
        PatcherFeaturesCommand.register(event.getDispatcher(), infoConfig);
    }
}
