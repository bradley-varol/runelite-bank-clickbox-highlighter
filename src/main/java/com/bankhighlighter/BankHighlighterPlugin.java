package com.bankhighlighter;

import com.google.inject.Provides;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Locale;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.ObjectComposition;
import net.runelite.api.Tile;
import net.runelite.api.TileObject;
import net.runelite.api.WorldView;
import net.runelite.api.events.DecorativeObjectDespawned;
import net.runelite.api.events.DecorativeObjectSpawned;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GroundObjectDespawned;
import net.runelite.api.events.GroundObjectSpawned;
import net.runelite.api.events.WallObjectDespawned;
import net.runelite.api.events.WallObjectSpawned;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
    name = "Bank Clickbox Highlighter",
    description = "Highlights bank clickboxes, with options for deposit boxes and Group storage",
    tags = {"bank", "booth", "chest", "highlight"}
)
public class BankHighlighterPlugin extends Plugin
{
    @Inject private Client client;
    @Inject private OverlayManager overlayManager;
    @Inject private BankHighlighterConfig config;

    // A large object can occupy several tiles, so track the object instance only once.
    private final Map<TileObject, ObjectComposition> bankObjects = new IdentityHashMap<>();
    private final Map<Integer, ObjectComposition> definitions = new HashMap<>();
    private final Map<Integer, BankTargetType> targetDefinitions = new HashMap<>();
    private final Map<Integer, Boolean> bankCandidates = new HashMap<>();
    private BankHighlighterOverlay overlay;

    @Provides
    BankHighlighterConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(BankHighlighterConfig.class);
    }

    @Override
    protected void startUp()
    {
        overlay = new BankHighlighterOverlay(this, config, client);
        overlayManager.add(overlay);
        if (client.getGameState() == GameState.LOGGED_IN)
        {
            scanScene();
        }
    }

    @Override
    protected void shutDown()
    {
        overlayManager.remove(overlay);
        overlay = null;
        clearScene();
    }

    Map<TileObject, ObjectComposition> getBankObjects()
    {
        return bankObjects;
    }

    BankTargetType getActiveTargetType(TileObject object, ObjectComposition definition)
    {
        if (definition.getImpostorIds() != null)
        {
            definition = definition.getImpostor();
        }
        if (definition == null)
        {
            return BankTargetType.NONE;
        }

        BankTargetType type = classifyDefinition(definition);
        return type != BankTargetType.NONE ? type
            : hasBankOverride(object) ? BankTargetType.BANK : BankTargetType.NONE;
    }

    private BankTargetType classifyDefinition(ObjectComposition definition)
    {
        return targetDefinitions.computeIfAbsent(definition.getId(),
            id -> classify(definition.getName(), definition.getActions()));
    }

    private static boolean hasBankOverride(TileObject object)
    {
        for (int i = 0; i < 5; i++)
        {
            if ("Bank".equalsIgnoreCase(object.getOpOverride(i)))
            {
                return true;
            }
        }
        return false;
    }

    static BankTargetType classify(String name, String[] actions)
    {
        if (actions == null)
        {
            return BankTargetType.NONE;
        }

        boolean bankAction = false;
        boolean depositAction = false;
        boolean useAction = false;
        boolean openAction = false;
        for (String action : actions)
        {
            if ("Bank".equalsIgnoreCase(action))
            {
                bankAction = true;
            }
            else if ("Deposit".equalsIgnoreCase(action))
            {
                depositAction = true;
            }
            else if ("Use".equalsIgnoreCase(action))
            {
                useAction = true;
            }
            else if ("Open".equalsIgnoreCase(action))
            {
                openAction = true;
            }
        }

        String lowerName = name == null ? "" : name.toLowerCase(Locale.ROOT);
        boolean depositBox = lowerName.equals("deposit box") || lowerName.endsWith(" deposit box");
        boolean namedBank = lowerName.equals("bank") || lowerName.startsWith("bank ")
            || lowerName.endsWith(" bank");
        if (depositBox && (bankAction || depositAction || useAction))
        {
            return BankTargetType.DEPOSIT_BOX;
        }
        if (bankAction || (namedBank && useAction))
        {
            return BankTargetType.BANK;
        }
        if (namedBank && depositAction)
        {
            return BankTargetType.DEPOSIT_BOX;
        }
        if (lowerName.equals("group storage") && openAction)
        {
            return BankTargetType.GROUP_STORAGE;
        }
        return BankTargetType.NONE;
    }

    private boolean isBankCandidate(ObjectComposition definition)
    {
        if (classifyDefinition(definition) != BankTargetType.NONE)
        {
            return true;
        }
        int[] impostorIds = definition.getImpostorIds();
        if (impostorIds != null)
        {
            for (int id : impostorIds)
            {
                if (id >= 0)
                {
                    ObjectComposition impostor = client.getObjectDefinition(id);
                    if (impostor != null && classifyDefinition(impostor) != BankTargetType.NONE)
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void track(TileObject object)
    {
        if (object == null)
        {
            return;
        }

        int id = object.getId();
        ObjectComposition definition = definitions.computeIfAbsent(id, client::getObjectDefinition);
        if (definition != null && (bankCandidates.computeIfAbsent(id,
            ignored -> isBankCandidate(definition)) || hasBankOverride(object)))
        {
            bankObjects.put(object, definition);
        }
    }

    private void scanTile(Tile tile)
    {
        if (tile == null)
        {
            return;
        }
        GameObject[] gameObjects = tile.getGameObjects();
        if (gameObjects != null)
        {
            for (GameObject object : gameObjects)
            {
                track(object);
            }
        }
        track(tile.getWallObject());
        track(tile.getGroundObject());
        track(tile.getDecorativeObject());
    }

    private void clearScene()
    {
        bankObjects.clear();
        definitions.clear();
        targetDefinitions.clear();
        bankCandidates.clear();
    }

    private void scanScene()
    {
        bankObjects.clear();
        WorldView worldView = client.getTopLevelWorldView();
        if (worldView == null || worldView.getScene() == null)
        {
            return;
        }
        Tile[][][] tiles = worldView.getScene().getTiles();
        if (tiles == null)
        {
            return;
        }
        for (Tile[][] plane : tiles)
        {
            if (plane == null)
            {
                continue;
            }
            for (Tile[] column : plane)
            {
                if (column == null)
                {
                    continue;
                }
                for (Tile tile : column)
                {
                    scanTile(tile);
                    if (tile != null)
                    {
                        scanTile(tile.getBridge());
                    }
                }
            }
        }
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event)
    {
        if (event.getGameState() == GameState.LOGGED_IN)
        {
            scanScene();
        }
        else
        {
            clearScene();
        }
    }

    @Subscribe public void onGameObjectSpawned(GameObjectSpawned event) { track(event.getGameObject()); }
    @Subscribe public void onGameObjectDespawned(GameObjectDespawned event) { bankObjects.remove(event.getGameObject()); }
    @Subscribe public void onWallObjectSpawned(WallObjectSpawned event) { track(event.getWallObject()); }
    @Subscribe public void onWallObjectDespawned(WallObjectDespawned event) { bankObjects.remove(event.getWallObject()); }
    @Subscribe public void onGroundObjectSpawned(GroundObjectSpawned event) { track(event.getGroundObject()); }
    @Subscribe public void onGroundObjectDespawned(GroundObjectDespawned event) { bankObjects.remove(event.getGroundObject()); }
    @Subscribe public void onDecorativeObjectSpawned(DecorativeObjectSpawned event) { track(event.getDecorativeObject()); }
    @Subscribe public void onDecorativeObjectDespawned(DecorativeObjectDespawned event) { bankObjects.remove(event.getDecorativeObject()); }
}
