Big Reactors Changelog
======================

Next Release (Anticipated Version: 0.2.0)
-----------------------------------------

Current Release (0.1.10A)
--------------------------------
- Added: Optional new recipes to craft graphite bars from 2 gravel and 1 coal or charcoal, for compatibility with UE mods. They default to off.
- Added: Four new config options, which control the recipes for creating graphite bars, two for smelting and two for crafting.
- Bugfix: Reactors can no longer be built without yellorium fuel rods inside
- Bugfix: Reactors that were built without yellorium fuel rods no longer crash the client

Older Releases
--------------

### Release 0.1.9A
- Bugfix: Fixed a crash in BeefCore that occasionally happens when players log out from SMP servers
- Bugfix: Mekanism 5.5.6.63 + BigReactors 0.1.8A would crash when BR added TE pulverizer recipes. This is fixed.
- Bugfix: Mekanism enrichment chamber recipe was not producing 2 dusts per yellorite ore. This is fixed.

### Release 0.1.8A
- Bugfix: Blutonium ingots no longer show up as blutonium dusts
- Bugfix: Grinders no longer convert yellorite ore into 2 blutonium dusts
- Bugfix: AE's grindstone now properly doubles yellorite ore into 2 yellorite dusts instead of 1


### Release 0.1.7A
- Bugfix: Applied Energistics grindstone recipes now work, if you're using AE v.11b or above
- Bugfix: Reactors now only tick once per world tick. Significant CPU usage reduction, fixes many strange bugs.
- Bugfix: Reprocessor textures now show up as on/off at the appropriate times.
- Due to above, reactors needed a rebalance, as they were actually only producing at least 1/3 of their observed output, maybe less.
- Rebalance: Reactor designs with water are mildly nerfed, waterless reactors are considerably worse.
- Feature: Reactors now show their average power production per tick in their main UI
- Feature: Mekanism machines now recognize BR ores and ingots, can produce dusts. Mekanism 5.5.4+.


### Release 0.1.6A
- Fixed: Some very large reactors would bug out, eat all their fuel and produces infinite power. They have been taught to behave.
- Fixed: Automation objects (pipes, ME buses) on the top/bottom sides of a cyanite reprocessor now work.
- Dusts for yellorium, cyanite, blutonium and graphite now exist.
- All dusts can be smelted into ingots in any smelter.
- Dusts can also be smelted in TE Induction Smelters.
- Yellorite ore can be ground into 2 Yellorium Dusts
- All ingots can be ground into dusts (1:1 ratio).
- Dusts can only be obtained with other mods' grinders (e.g. TE Pulverizer)
- Known Bug: Ores, ingots cannot be crushed with AE's Grindstone as of AE rv 10.n.

### Release 0.1.5A
- Fixed: SMP servers no longer crash on opening a Big Reactors GUI

### Release 0.1.4A
- Cyanite reprocessors are now craftable. Oops.

### Release 0.1.3A
- First public alpha!
- (BeefCore) Maximum dimension checks added
- Maximum valid reactor dimensions can now be controlled via config, defaults to 32 blocks horizontal, 48 blocks vertical.
- Fixed: Recipes use ore dictionary properly.
- Changed: Recipes use much less yellorium in general, much more graphite, as originally intended.

### Release 0.1.2A
- (BeefCore) Fixed a rare crash on load
- Fixed: Reactors no longer become "corrupted" if you activate them with an empty fuel rod inside
- Fixed: Fuel rods no longer go into negative heat due to floating point error, causing reactor corruption
- Fixed: Reprocessors no longer give you 100x the energy you actually input
- NOTE: Due to the above fix, any existing reprocessors in your worlds will have bad data in them. Break and re-place them.
- Fixed pipes connecting to the wrong inventories when inventories should not have been exposed
- Fixed pipes not connecting/disconnecting when liquid tanks become exposed or unexposed
- This was an internal playtest version, no public release.

### Release 0.1.1A
- Balance: Reactor heat loss minimum changed to 1% or 1C/sec instead of 1% or 1C/tick
- Fixed crash bug when connecting liquid pipes to cyanite reprocessors
- Fixed liquid pipe connectivity in general
- Fixed inventory item slots accepting the wrong item type on shift-click
- Fixed inventory item slots duplicating items
- Fixed reactors generating 1 free MJ/t for no reason whatsoever
- Fixed liquiduct crashes
- This was an internal playtest version, no public release

### Release 0.1.0A
- Control rods now have an actual control rod to extend/retract, and it affects the radiation simulation
- Lots of internal refactoring. Tall fuel columns are now markedly more efficient in both messaging and rendering.
- Heat/Radiation simulation rewritten to be more comprehensible than the original. Fuel = radiation. Radiation = heat & more radiation. Most power comes from heat, but some from radiation in certain coolants (e.g. water).
- A basic cyanite reprocessor now exists. Cyanite + Water + Power = Blutonium. This is neither final nor balanced.
- Blutonium is now usable as reactor fuel. At the moment, it is identical to yellorium aside from the item's appearance.
- Yellorite generation is now controllable via config, in the WorldGen section
- Several new or reworked textures
- This was an internal playtest version, no public release

### Release 0.0.2-playtest
- First-pass optimization of BeefCore. Truly large reactors (16x16x16 at least) are now possible.
- Access ports marked as outlets (blue arrows) now auto-emit waste if there are pipes attached

### Release 0.0.1-playtest
- Initial release
