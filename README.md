# Bank Clickbox Highlighter

A RuneLite plugin for OSRS that highlights the clickboxes of nearby bank access objects. It detects objects with a **Bank** action, including bank booths, bank chests, and the Bank Crab at The Pandemonium. It also recognizes named bank fixtures with a **Use** action.

**Highlight colour** controls the outline and fill, including transparency. **Highlight deposit boxes** separately controls deposit-only bank fixtures such as Bank deposit boxes and Deposit boxes. **Highlight Group storage** separately controls Group Ironman storage chests, which have an **Open** action. Both optional categories are disabled by default and use the same highlight colour.

The plugin follows game objects and wall objects as they appear and disappear. It scans the scene when enabled or after logging in, so nearby banks are highlighted immediately. Plain chests without a **Bank** action and unrelated objects with a **Deposit** action are not highlighted. Group storage chests are tracked by their exact name and **Open** action, then shown only when their option is enabled. NPC bankers are not outlined because RuneLite does not expose an NPC clickbox through the object API.

## Build

Requires Java 11+. Run `./gradlew build` in this directory. The Gradle wrapper downloads Gradle and dependencies automatically when needed.

## Test locally

Run `./gradlew run` to start a separate RuneLite development client with this plugin loaded. Open the plugin list and enable **Bank Clickbox Highlighter**. Visit a bank booth and a bank chest, then change **Highlight colour**, **Highlight deposit boxes**, and **Highlight Group storage** in the plugin settings to verify the overlay updates. The outline follows the clickable shape of each bank object rather than its tile.

If you use a Jagex Account and the development client cannot log in, follow [RuneLite's Jagex Account development guide](https://github.com/runelite/runelite/wiki/Using-Jagex-Accounts). Keep the credentials file described there private.
