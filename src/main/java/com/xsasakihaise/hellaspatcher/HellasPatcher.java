package com.xsasakihaise.hellaspatcher;

import com.xsasakihaise.hellascontrol.api.sidemods.HellasAPIControlPatcher;
import com.xsasakihaise.hellaspatcher.HellasPatcherInfoConfig;
import com.xsasakihaise.hellaspatcher.commands.PatcherVersionCommand;
import com.xsasakihaise.hellaspatcher.commands.PatcherDependenciesCommand;
import com.xsasakihaise.hellaspatcher.commands.PatcherFeaturesCommand;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;

import java.io.File;

@Mod("hellaspatcher")
public class HellasPatcher {

    public static HellasPatcherInfoConfig infoConfig;

    static {
        // Direkter, verpflichtender Verifizierungsaufruf (API ist nicht optional)
        HellasAPIControlPatcher.verify();
    }

    public HellasPatcher() {
        // Config initialisieren und Defaults sofort laden, damit Commands valide Werte haben
        infoConfig = new HellasPatcherInfoConfig();
        infoConfig.loadDefaultsFromResource();

        // Event-Bus registrieren für Serverstart und Command-Registration
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerStart(FMLServerStartingEvent event) {
        File serverRoot = event.getServer().getServerDirectory();
        infoConfig.load(serverRoot); // Überschreibt ggf. mit Serverdatei
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        PatcherVersionCommand.register(event.getDispatcher(), infoConfig);
        PatcherDependenciesCommand.register(event.getDispatcher(), infoConfig);
        PatcherFeaturesCommand.register(event.getDispatcher(), infoConfig);
    }
}