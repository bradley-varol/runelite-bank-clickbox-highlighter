package com.bankhighlighter;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("bankHighlighter")
public interface BankHighlighterConfig extends Config
{
    @Alpha
    @ConfigItem(
        keyName = "highlightColor",
        position = 0,
        name = "Highlight colour",
        description = "Colour used to highlight bank clickboxes"
    )
    default Color highlightColor()
    {
        return new Color(0, 200, 255, 180);
    }

    @ConfigItem(
        keyName = "highlightDepositBoxes",
        position = 1,
        name = "Highlight deposit boxes",
        description = "Also highlight bank deposit boxes and other deposit-only bank fixtures"
    )
    default boolean highlightDepositBoxes()
    {
        return false;
    }

    @ConfigItem(
        keyName = "highlightGroupStorage",
        position = 2,
        name = "Highlight Group storage",
        description = "Also highlight dedicated Group Ironman storage chests"
    )
    default boolean highlightGroupStorage()
    {
        return false;
    }
}
