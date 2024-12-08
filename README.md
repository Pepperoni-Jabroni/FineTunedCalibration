<p align="center">
  <img src="https://github.com/Pepperoni-Jabroni/FineTunedCalibration/assets/17690401/fedd246b-7854-4d25-a547-11133ca98f9a">
  </br></br>
  Expands Calibrated Sculk Sensors output from 15 frequencies to 100+ frequencies
  </br></br>
  <a href="https://www.curseforge.com/minecraft/mc-mods/fabric-api"><img src="https://i.imgur.com/Ol1Tcf8.png" width="149" height="50" title="Fabric API" alt="Fabric API"></a>
  </br></br>
  <a href="https://www.curseforge.com/minecraft/mc-mods/fine-tuned-calibration"><img alt="Curseforge" src="https://cf.way2muchnoise.eu/full_709799_downloads.svg"></a> <a href="https://modrinth.com/mod/finetunedcalibration"><img alt="Modrinth" src="https://img.shields.io/modrinth/dt/finetunedcalibration?label=Modrinth%20Downloads"></a> <a href="https://github.com/Pepperoni-Jabroni/FineTunedCalibration"><img alt="GitHub" src="https://img.shields.io/github/downloads/Pepperoni-Jabroni/FineTunedCalibration/total?label=Downloads&logo=github"></a>
</p>

## 📖 About
This mod gives extra functionality to Calibrated Sculk Sensors which allow them to output a variable Redstone signal based on Vibrational differences *within* the same Frequency, meaning you can make your Sculk contraptions *even more specifc*! 

Typical vanilla "Calibrated Sculk Sensors" allow you to "calibrate" them by selecting for a specific frequency value (e.g. frequency 4) via inputting a redstone strength (e.g. signal strength 4). But then, after filtering for the signal, the sensor still outputs *exactly* the value you filtered for. That's not very useful. What if instead it used the entire 1-15 frequencies to communicate *even more specific* information about the source of the vibration? Then - instead of all Boots having the same frequency, eah boot type (e.g. Leather) can have its own separate frequency! This is exactly what this mod does.

This mod increases the number of Frequencies that result from a Calibrated Sculk Sensor + Comparator **from 15 Frequencies to 100+ Frequencies**! Some things you can detect with this mod:
- Which Goat Horn type was blown ("Ponder", "Sing", ...)?
- Which Music Disc was played in a Jukebox ("Block", "13", ...)?
- Which slot of a Chiseled Bookshelf was interacted with (1-6)?
- What type of boots is the player wearing (none, leather, ...)?
- Was the Entity Damage a Player Death?
- What's the tune of the Note Block?
- Was a Redstone Lever just activated?
- And much more!

## 📃 Changelog
Changelogs are available [at the GitHub Releases section](https://github.com/Pepperoni-Jabroni/FineTunedCalibration/releases)
   
## 📸 Media
![https://i.imgur.com/BMw3ZTB.png](https://i.imgur.com/BMw3ZTB.png)
This is the Music Disc Blocks being detected as Frequency 3 for a Calibrated Sculk Sensor set to Page 11 in a Lectern (see Table below). Under vanilla mechanics, all Music Discs have the same Frequency - 11.

[Demo](https://i.imgur.com/i15SfSr.mp4) of Comparator output Calibrated to Freq 1, which includes player armor frequencies (1 - no boots, 2 - leather boots, ...).

## 📋 Full Table
| Output |                                                                                                                                                                      Vibration type, id, description                                                                                                                                                                      |                                                                                                                                                                                                                                                                                     Calibrated Outputs                                                                                                                                                                                                                                                                                    |
|:------:|:-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------:|:-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------:|
|    1   |                                                                                                 Step        minecraft:step        Player or entity step<br>Swim        minecraft:swim        Player or entity swim<br>Flap        minecraft:flap        Entity flaps (bat)                                                                                                |                                                                                                              1 -  player stepping no boots<br>2 -  player stepping leather boots<br>3 -  player stepping golden boots<br>4 -  player stepping chainmail boots<br>5 -  player stepping iron boots<br>6 -  player stepping diamond boots<br>7 -  player stepping netherite boots<br>8 -  entity stepping<br>9 -  player swimming<br>10 -  entity swimming<br>11 -  entity flap                                                                                                              |
|    2   |                                     Projectile Land        minecraft:projectile_land        Projectile lands (snowball)<br>Hit Ground        minecraft:hit_ground        Player or entity hits ground post-jump or fall<br>Splash        minecraft:splash        Player or entity splashes. Bubble Column, water surface, boat paddles                                    |                                                                                                                                                                                                                                          1 - projectile land<br>2 - player falls<br>3 - entity falls<br>4 - player splashes<br>5 - entity spashes                                                                                                                                                                                                                                         |
|    3   |                                                  Item Interact Finish        minecraft:item_interact_finish        Item Interaction (Shield, Spyglass)<br>Projectile Shoot        minecraft:projectile_shoot        Projectile Shoots (arrow)<br>Instrument Play        minecraft:instrument_play        Goat Horn Plays                                                  |                                                                                             1 -  Goat Horn "Ponder<br>2 -  Goat Horn "Sing",<br>3 -  Goat Horn "Seek"<br>4 -  Goat Horn "Feel",<br>5 -  Goat Horn "Admire<br>6 -  Goat Horn "Call",<br>7 -  Goat Horn "Yearn"<br>8 -  Goat Horn "Dream"<br>9 -  shield interaction, snowball shoot<br>10 - bow interact/shoot<br>11 - crossbow interact/shoot<br>12 - fishing rod interact/shoot<br>13 - spyglass interaction, firework shoot                                                                                             |
|    4   |                                                                     Entity Roar        minecraft:entity_roar        Entity Roars (Ravager)<br>Entity Shake        minecraft:entity_shake        Entity Shakes (wolf after swimming)<br>Elytra Glide        minecraft:elytra_glide        Player glides                                                                    |                                                                                                                                                                                                                                                                   1- entity roar<br>2 - entity shake<br>3 - elytra glide                                                                                                                                                                                                                                                                  |
|    5   |                                                                                                             Entity Dismount        minecraft:entity_dismount        Player Dismount<br>Equip        minecraft:equip        Armor equip in armor slot or stand                                                                                                             |                                                                                                                                                                                                                                                     1 - Player dismount<br>2 - Entity dismount<br>3 - Player equip<br>4 - Entity equip                                                                                                                                                                                                                                                    |
|    6   |                                                                     Entity Mount        minecraft:entity_mount        Player Mount (horse)<br>Entity Interact        minecraft:entity_interact        Player interaction with entity (breeding)<br>Shear        minecraft:shear        Shears a sheep                                                                     |                                                                                                                                                                                                                                                   1 - Player entity mount<br>2 - Entity entity mount<br>3 - entity interact<br>4 - shear                                                                                                                                                                                                                                                  |
|    7   |                                                                                                                                    Entity Damage        minecraft:entity_damage        Damage or death to entities (except armor stand)                                                                                                                                   |                                                                                                                                                                                                                                                       1 - Player damage<br>2 - Entity damage<br>3 - Player death<br>4 - Entity death                                                                                                                                                                                                                                                      |
|    8   |                                                                                                                            Drink        minecraft:drink        Player or entity drinks<br>Eat        minecraft:eat        Player or entity eats                                                                                                                           |                                                                                                                                                                                                                                                         1 - Player drinks<br>2 - Entity drinks<br>3- Player eats<br>4- Entity eats                                                                                                                                                                                                                                                        |
|    9   |       Container Close        minecraft:container_close        Close container (Chest, shulkerbox, hopper)<br>Block Close        minecraft:block_close        Door Close (trapdoor and fencegate)<br>Block Deactivate        minecraft:block_deactivate        Lever or button deactivation<br>Block Detach        minecraft:block_detach        Tripwire detachment       |                                                                                                                                                                                                                                                     1 - Container close<br>2 - Block close<br>3 - Block deactivate<br>4 - Block detach                                                                                                                                                                                                                                                    |
|   10   | Block Open        minecraft:block_open        Open Door<br>Block Activate        minecraft:block_activate        Activate Blocks<br>Block Attach        minecraft:block_attach        Attachment of Tripwire hook<br>Prime Fuse        minecraft:prime_fuse        TNT or creeper activation<br>Note Block Play        minecraft:note_block_play        Note Block Sounds |                                                                                        1 - Container open, Note block [0-1]<br>2 - Block open, Note block [2-3]<br>3 - Block activate, Note block [4-5]<br>4 - Block attach, Note block [6-7]<br>5 - Prime fuse, Note block [8-9]<br>6 - Note block [10-11]<br>7 - Note block [12-13]<br>8 - Note block [14-15]<br>9 - Note block [16-17]<br>10 - Note block [18-19]<br>11 - Note block [20-21]<br>12 - Note block [22-23]<br>13 - Note block [24]                                                                                        |
|   11   |                                                                                                                                        Block Change        minecraft:block_change        Block Change (Chiseled Bookshelf, Lectern)                                                                                                                                       | 1 - Music Disc 13, chiseled bookshelf slot 1 use<br>2 - Music Disc Cat, chiseled bookshelf slot 2 use<br>3 - Music Disc Blocks, chiseled bookshelf slot 3 use<br>4 - Music Disc Chirp, chiseled bookshelf slot 4 use<br>5 - Music Disc Far, chiseled bookshelf slot 5 use<br>6 - Music Disc Mall, chiseled bookshelf slot 6 use<br>7 - Music Disc Mellohi, lectern use<br>8 - Music Disc Stal, composter use<br>9 - Music Disc Strad<br>10 - Music Disc Ward<br>11 - Music Disc 11<br>12 - Music Disc Wait<br>13 - Music Disc Otherside <br>14 - Music Disc 5<br>15 - Music Disc Pigstep  |
|   12   |                                                                                               Block Destroy        minecraft:block_destroy        Block Destruction<br>Fluid Pickup        minecraft:fluid_pickup        Gathered Fluid (Water, honey bottle, powdered snow)                                                                                              |                                                                                                                                                                                                                                                                           1 - Block destroy<br>2 - Fluid pickup                                                                                                                                                                                                                                                                           |
|   13   |                                                                                                                     Block Place        minecraft:block_place        Block Placement<br>Fluid Place        minecraft:fluid_place        Fluid Placement                                                                                                                    |                                                                                                                                                                                                                                                                             1 - Block place<br>2 - Fluid place                                                                                                                                                                                                                                                                            |
|   14   |                                          Entity Place        minecraft:entity_place        Entity Placement via Spawn-egg or Mob Spawner<br>Lightning Strike        minecraft:lightning_strike        Lightning Strike<br>Teleport        minecraft:teleport        Endermen, Chorus Fruit, Shulker, or Enderpearl Teleportation                                          |                                                                                                                                                                                                                                                                  1 - Entity place<br>2 - Lightning strike<br>3 - Teleport                                                                                                                                                                                                                                                                 |
|   15   |                                                                                                     Entity Die        minecraft:entity_die        Armor Stand Dies<br>Explode        minecraft:explode        TNT, End Crystal, Bed/Respawn Anchor, Creeper Explosion                                                                                                     |                                                                                                                                                                                                                                                                               1 - Entity die<br>2 - Explode                                                                                                                                                                                                                                                                               |
