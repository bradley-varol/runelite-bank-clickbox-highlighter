package com.bankhighlighter;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.util.Map;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.ObjectComposition;
import net.runelite.api.TileObject;
import net.runelite.api.WorldView;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

public class BankHighlighterOverlay extends Overlay
{
    private static final Stroke OUTLINE = new BasicStroke(2);
    private final BankHighlighterPlugin plugin;
    private final BankHighlighterConfig config;
    private final Client client;

    BankHighlighterOverlay(BankHighlighterPlugin plugin, BankHighlighterConfig config, Client client)
    {
        this.plugin = plugin;
        this.config = config;
        this.client = client;
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (client.getGameState() != GameState.LOGGED_IN || client.getLocalPlayer() == null)
        {
            return null;
        }

        Map<TileObject, ObjectComposition> bankObjects = plugin.getBankObjects();
        if (bankObjects.isEmpty())
        {
            return null;
        }

        WorldView worldView = client.getTopLevelWorldView();
        if (worldView == null)
        {
            return null;
        }

        Color color = config.highlightColor();
        Color fill = new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha() / 4);
        boolean highlightDepositBoxes = config.highlightDepositBoxes();
        boolean highlightGroupStorage = config.highlightGroupStorage();
        Color oldColor = graphics.getColor();
        Stroke oldStroke = graphics.getStroke();
        graphics.setStroke(OUTLINE);
        for (Map.Entry<TileObject, ObjectComposition> entry : bankObjects.entrySet())
        {
            TileObject object = entry.getKey();
            if (object.getWorldView() != worldView || object.getPlane() != worldView.getPlane())
            {
                continue;
            }
            BankTargetType type = plugin.getActiveTargetType(entry.getValue());
            if (type == BankTargetType.NONE
                || (type == BankTargetType.DEPOSIT_BOX && !highlightDepositBoxes)
                || (type == BankTargetType.GROUP_STORAGE && !highlightGroupStorage))
            {
                continue;
            }
            Shape clickbox = object.getClickbox();
            if (clickbox != null)
            {
                graphics.setColor(fill);
                graphics.fill(clickbox);
                graphics.setColor(color);
                graphics.draw(clickbox);
            }
        }
        graphics.setStroke(oldStroke);
        graphics.setColor(oldColor);
        return null;
    }
}
