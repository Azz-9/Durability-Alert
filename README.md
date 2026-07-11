# Durability Alert

![Fabric](https://img.shields.io/badge/Loader-Fabric-blue)
![NeoForge](https://img.shields.io/badge/Loader-NeoForge-orange)
![License: MIT](https://img.shields.io/badge/License-MIT-yellow)
![Side: Client](https://img.shields.io/badge/Side-Client-red)

Receive a sound and/or message alert when your gear drops below a configurable durability threshold.

---

## Features

- Alerts when an item goes below a durability percentage.
- Supports sound alerts, message alerts, or both.
- Customizable alert sound.
- Configurable minimum delay between alerts.
- Works only outside Creative and Spectator mode.
- Optional armor durability checks.
- Optional Elytra-only armor check.
- Whitelist or blacklist specific items.
- Client-side detection when item durability changes.

---

## Installation

1. Download the jar for your loader from [Modrinth](https://modrinth.com/project/durability_alert)
   or [CurseForge](https://www.curseforge.com/minecraft/mc-mods/durabilityalert)
2. Drop it into your `mods/` folder

**Optional but recommended:**

- [ModMenu](https://modrinth.com/mod/modmenu) — adds a mod list screen where you can access the config screen (fabric)
- [Cloth Config](https://modrinth.com/mod/cloth-config) — required for the in-game config screen

Without these, the mod works out of the box with its default settings. You can still configure it by editing the config
file manually (see below).

---

## Configuration

The config file is located at `.minecraft/config/durability_alert.json` and is created automatically on first launch.

| Option                    | Type       | Default                      | Description                                                                     |
|---------------------------|------------|------------------------------|---------------------------------------------------------------------------------|
| `enabled`                 | Boolean    | `true`                       | Enables or disables durability alerts.                                          |
| `threshold`               | Integer    | `10`                         | Durability percentage below which an alert is triggered.                        |
| `alertType`               | Enum       | `SOUND_AND_MESSAGE`          | Alert mode: sound, message, or both.                                            |
| `sound`                   | SoundEvent | `minecraft:block.anvil.land` | Sound played when an alert is triggered.                                        |
| `pitch`                   | float      | `2.0`                        | Pitch of the alert sound.                                                       |
| `volume`                  | float      | `1.0`                        | Volume of the alert sound.                                                      |
| `checkArmorPieces`        | Boolean    | `true`                       | Enables durability alerts for equipped armor pieces.                            |
| `checkElytraOnly`         | Boolean    | `false`                      | Only checks Elytras among armor slots when armor checking is not fully enabled. |
| `minAlertIntervalSeconds` | Integer    | `60`                         | Minimum delay between two alerts for the same item type.                        |
| `listType`                | Enum       | `BLACKLIST`                  | Defines whether `itemList` acts as a whitelist or blacklist.                    |
| `itemList`                | List<Item> | Empty list                   | Items affected by the whitelist or blacklist mode.                              |

---

## License

MIT — see [LICENSE](LICENSE) for details.