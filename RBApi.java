package com.example.RuneBotApi;

import lombok.SneakyThrows;
import net.runelite.api.Client;
import net.runelite.client.RuneLite;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.WorldService;
import net.runelite.client.util.Text;
import net.runelite.client.util.WildcardMatcher;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.stream.Collectors;

public class RBApi {


    public static Client getClient()
    {
        return RuneLite.getInjector().getProvider(Client.class).get();
    }

    public static ItemManager getItemManager()
    {
        return RuneLite.getInjector().getProvider(ItemManager.class).get();
    }

    public static ClientThread getClientThread()
    {
        return RuneLite.getInjector().getProvider(ClientThread.class).get();
    }

    public static WorldService getWorldService()
    {
        return RuneLite.getInjector().getProvider(WorldService.class).get();
    }

    public static ConfigManager getConfigManager()
    {
        return RuneLite.getInjector().getProvider(ConfigManager.class).get();
    }

    public static void runOnClientThread(Runnable r)
    {
        getClientThread().invoke(r);
    }

    @SneakyThrows
    public static void sendKeystroke(KeyStroke options)
    {
        Client client = getClient();
        KeyEvent keyPress = new KeyEvent(client.getCanvas(), KeyEvent.KEY_PRESSED, System.currentTimeMillis(), InputEvent.BUTTON1_DOWN_MASK, options.getKeyEvent());
        KeyEvent keyRelease = new KeyEvent(client.getCanvas(), KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, options.getKeyEvent());
        KeyEvent keyTyped = new KeyEvent(client.getCanvas(), KeyEvent.KEY_TYPED, System.currentTimeMillis(), 0, options.getKeyEvent());
        client.getCanvas().dispatchEvent(keyPress);
        client.getCanvas().dispatchEvent(keyRelease);
        client.getCanvas().dispatchEvent(keyTyped);
    }


    /**
     * provide the config.itemList or config.npcList or w/e and
     * use the resulting value for the configCSVParser. This helps
     * improve time complexity if the user spells the item name
     * correctly since having to search through every entity for every
     * config entry runs in quadratic time which is no good
     * @return configString -> HashSet<String>.forEach(str -> str.toLower().trim())
     */
    public static HashSet<String> configCSVToHashSet(String inputText)
    {
        return Text.fromCSV(inputText.toLowerCase()).stream()
                .map(String::trim)
                .collect(Collectors.toCollection(HashSet::new));
    }

    /**
     * The nature of this being static (and potentially used concurrently)
     * requires the instantiation of the HashSet to be done from within the calling method in
     * order to see any performance benefit. See RBApi.configCSVToHashSet()
     * @return true if item is in the config list
     */
    public static boolean configMatcher(HashSet<String> providedConfigEntries, String entityName)
    {
        if (providedConfigEntries.contains(entityName.toLowerCase())) return true;

        return providedConfigEntries.stream()
                .anyMatch(pattern ->
                        WildcardMatcher.matches(pattern, entityName.toLowerCase())
                );
    }

}
