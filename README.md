# MythicMobsNames

MythicMobsNames adds **per-player hologram indicators** above MythicMobs NPCs and remembers which NPCs each player has already interacted with.

Once a player interacts with an NPC, the hologram disappears permanently for that player.

---

## Features

- Per-player holograms (packet-based)
- Persistent interaction tracking (MySQL)
- Holograms hide after interaction
- Configurable range, update interval, and symbol
- Supports ItemsAdder glyphs (use actual glyph characters)
- Optimized proximity detection
- Reload, reset, and debug commands

---

## Requirements

- Paper / Spigot 1.21+
- ProtocolLib
- MythicMobs
- MySQL / MariaDB

---

## Commands

### /mmdebug
Shows nearby entities and detection status  
Permission: `mythicmobsnames.debug`

### /mmresetdata <player>
Resets interaction data for a player  
Permission: `mythicmobsnames.admin`

### /mmreload
Reloads the plugin configuration  
Permission: `mythicmobsnames.admin`

---

## Notes

- Holograms are client-side and per-player
- ItemsAdder emoji tokens (like `:heart:`) do not work in entity names, Use the actual glyph character for custom symbols


