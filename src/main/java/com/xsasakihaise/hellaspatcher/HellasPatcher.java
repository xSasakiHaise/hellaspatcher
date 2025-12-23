package com.xsasakihaise.hellaspatcher;

import com.xsasakihaise.hellascontrol.api.sidemods.HellasAPIControlPatcher;
import com.xsasakihaise.hellaspatcher.HellasPatcherInfoConfig;
import com.xsasakihaise.hellaspatcher.commands.PatcherVersionCommand;
import com.xsasakihaise.hellaspatcher.commands.PatcherDependenciesCommand;
import com.xsasakihaise.hellaspatcher.commands.PatcherFeaturesCommand;
import com.xsasakihaise.hellaspatcher.internal.PatcherGate;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.ModList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

@Mod("hellaspatcher")
public class HellasPatcher {

    public static HellasPatcherInfoConfig infoConfig;
    private static final Logger LOGGER = LogManager.getLogger("HellasPatcher");

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
        if (!ModList.get().isLoaded("hellascontrol")) {
            PatcherGate.DISABLE_REASON = "HellasControl missing";
            PatcherGate.ENABLED = false;
            LOGGER.warn("HellasPatcher disabled: {}", PatcherGate.DISABLE_REASON);
            return;
        }

        try {
            HellasAPIControlPatcher.verify();
            PatcherGate.ENABLED = true;
            PatcherGate.DISABLE_REASON = "OK";
            LOGGER.info("HellasPatcher enabled (license OK)");
        } catch (Exception exception) {
            PatcherGate.ENABLED = false;
            PatcherGate.DISABLE_REASON = "License invalid";
            LOGGER.warn("HellasPatcher disabled: {}", PatcherGate.DISABLE_REASON, exception);
        }
    }

    @SubscribeEvent
    public void onServerStart(FMLServerStartingEvent event) {
        if (!PatcherGate.ENABLED) {
            return;
        }
        File serverRoot = event.getServer().getServerDirectory();
        infoConfig.load(serverRoot); // Überschreibt ggf. mit Serverdatei
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        if (!PatcherGate.ENABLED) {
            return;
        }
        PatcherVersionCommand.register(event.getDispatcher(), infoConfig);
        PatcherDependenciesCommand.register(event.getDispatcher(), infoConfig);
        PatcherFeaturesCommand.register(event.getDispatcher(), infoConfig);
    }
}
