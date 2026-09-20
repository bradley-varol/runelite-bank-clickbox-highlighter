package com.bankhighlighter;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class BankHighlighterDetectionTest
{
    @Test
    public void detectsBanksByActionEvenWhenTheNameIsDifferent()
    {
        assertEquals(BankTargetType.BANK,
            BankHighlighterPlugin.classify("Chest", new String[] {"Open", "Bank"}));
        assertEquals(BankTargetType.BANK,
            BankHighlighterPlugin.classify("<col=ffff00>Bank Crab</col>", new String[] {"Bank", "Collect"}));
    }

    @Test
    public void detectsNamedBankFixturesWithDifferentActions()
    {
        assertEquals(BankTargetType.BANK,
            BankHighlighterPlugin.classify("Bank chest", new String[] {"Use"}));
        assertEquals(BankTargetType.BANK,
            BankHighlighterPlugin.classify("Bank chest", new String[] {"Use", "Collect"}));
    }

    @Test
    public void keepsDepositBoxesSeparate()
    {
        assertEquals(BankTargetType.DEPOSIT_BOX,
            BankHighlighterPlugin.classify("Bank deposit box", new String[] {"Deposit"}));
        assertEquals(BankTargetType.DEPOSIT_BOX,
            BankHighlighterPlugin.classify("Deposit box", new String[] {"Deposit"}));
        assertEquals(BankTargetType.DEPOSIT_BOX,
            BankHighlighterPlugin.classify("Bank deposit box", new String[] {"Bank", "Deposit"}));
    }

    @Test
    public void keepsGroupStorageSeparate()
    {
        assertEquals(BankTargetType.GROUP_STORAGE,
            BankHighlighterPlugin.classify("Group storage", new String[] {"Open"}));
        assertEquals(BankTargetType.NONE,
            BankHighlighterPlugin.classify("Group storage", new String[] {"Use"}));
    }

    @Test
    public void ignoresUnrelatedObjects()
    {
        assertEquals(BankTargetType.NONE,
            BankHighlighterPlugin.classify("Chest", new String[] {"Open"}));
        assertEquals(BankTargetType.NONE,
            BankHighlighterPlugin.classify("Bank chest", new String[] {"Open"}));
        assertEquals(BankTargetType.NONE,
            BankHighlighterPlugin.classify("Bank of Gielinor sign", new String[] {"Read"}));
        assertEquals(BankTargetType.NONE,
            BankHighlighterPlugin.classify("Ore hopper", new String[] {"Deposit"}));
        assertEquals(BankTargetType.NONE,
            BankHighlighterPlugin.classify("Bank chest", null));
    }
}
