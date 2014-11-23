Big Reactors Changelog
======================

Next Release (Anticipated Version: 0.5.0A)
------------------------------------------

Current Release (0.4.0A)
--------------------------------
- **Dependency**: CoFHCore dependency update to **3.0.0B8**
- Bugfix: Power taps no longer show up as green when adjacent to ME cables, as ME cables don't actually accept RF connections.

Older Releases
--------------

### 0.4.0rc11
- **Dependency**: Forge dependency update to **10.13.2.1232**
- Compatibility: OpenComputers support upgraded from 1.3 to 1.4. Big Reactors now works with OC 1.4 instead of 1.3.
- Compatiblity: Yellorite ore now has the additional oredict name `oreYellorium`, for the convenience of mods which automatically add ore/dust/ingot recipes based on names, such as AOBD.
- Localization: Updates to Russian, Chinese and Korean translations (thanks Adaptivity, ViKaleidoscope and puyo061!)
- Localization: Czech translation added (thanks nalimleinad!)

### 0.4.0rc10
- Bugfix: Cyanite reprocessors no longer drop twice as many ingots as they should
- Bugfix: BR now correctly registers recipes with both Mekanism 7.1.1 and 7.1.0
- Bugfix: `useExpensiveGlass` setting now works properly with Thermal Expansion Hardened Glass again
- Bugfix: Blutonium can again be used as a fuel and can be inserted into reactors which already contain yellorium or other fuels
- Bugfix: Ludicrite can no longer be crafted out of 9 yellorium dusts
- Enhancement: Ingots and dusts are now available for ludicrite. They do nothing, for now.

### 0.4.0rc9
- Bugfix: Fixed reactor controller not turning green or red when reactors were activated/deactivated via computer, redstone or rednet
- Bugfix: Fixed some bugs whereby rednet/redstone port settings would not be saved properly
- Bugfix: Fixed in-game requirement checks. BigReactors will now properly make Minecraft complain if CoFHCore is missing.
- Bugfix: In some cases, power taps were not re-rendering on chunk reloads, which would cause power taps to not work
- Bugfix: Fixed reactors dumping too much waste/fuel when ejecting waste/fuel.
- Enhancement: Added **ludicrite** blocks, which can serve as a very-high-end coil material in turbines. They cannot go in reactors, though.
- Enhancement: Added `dimensionWhitelist` setting, which allows you to specify exceptions to the `enableWorldGenInNegativeDimensions` setting. If `enableWorldGenInNegativeDimensions` is true, you may add dimensions to the `dimensionWhitelist` to permit BR world generation to operate in those dimensions.
- Workaround: OreDict problems (which will be fixed in CoFHCore 3.0.0B7) can be worked around in turbines by using Ludicrite blocks.
- Localization: Russian and Chinese translations updated (thanks Adaptivity & ViKaleidoscope!)

### 0.4.0rc8
- Bugfix: Everything. Literally anything that involves adjacent blocks (power taps, access ports, fluid ports) was broken.

### 0.4.0rc7
- **Dependency**: CoFHCore dependency update to **3.0.0B6-32**.
- Bugfix: Reactors and turbines should no longer appear disassembled on the client when using non-Vanilla blocks in SMP.
- Bugfix: Turbine blades should no longer have their textures corrupted when left running while loading/unloading texture packs at runtime.
- Bugfix: Fixed crash when loading extremely large turbines/reactors on the client.
- Bugfix: Fixed turbine/reactor glass being uncraftable in some modpack configurations
- Enhancement: Added `turbineCoilDragMultiplier`, `turbineAeroDragMultiplier`, `turbineMassDragMultiplier`, and `turbineFluidPerBladeMultiplier` settings.
- Enhancement: Added Version Checker integration!
- Enhancement: BeefTronic(tm) Diagnostics Tool now has (really crappy) icon

### 0.4.0rc5
- **Dependency:** Forge dependency update to **10.13.0.1205**.
- **Dependency:** CoFHCore dependency update to **3.0.0B5-30**. Thanks, _mysticdrew_!
- Bugfix: Fuel ejection was duping fuel. This has been fixed.
- Bugfix: Reactor controller GUI was triggering fuel ejection instead of waste ejection. Fixed.
- Bugfix: Yellorite Ore texture was improperly sized at 32x32. It is now 16x16, as vanilla textures should be.
- Bugfix: Cyanite reprocessors were kicking their inputs out of the input slot when distributing blutonium. This has been fixed.
- Bugfix: `requireSteelForIron` config was not always being obeyed, depending on mod load order. Fixed.
- Change: Turbine fluid ports can now only be toggled with wrenches (and wrench-like tools, e.g. Crescent Hammers), not by hand

### 0.4.0rc4
- Debugging: Added 'BeefTronic(tm) Debugging Tool' to help debug strange BR issues.

### 0.4.0rc3
- Bugfix: Cyanite reprocessors erroneously dismantled themselves when NEI's new overlay system queried them, causing them to dismantle whenever a player looked at them. This has been fixed.
- Bugfix: Rebuilt with SunJDK, which seems to fix odd, unexplainable errors when rc2 (which was the first and only release built on openJDK) runs on plain-vanilla Forge servers. (Installing Cauldron resolves the odd errors. Go figure.)

### 0.4.0rc2
- Bugfix: Cyanite reprocessors were unable to accept inputs from hoppers, pipes, etc. This has been fixed.

### 0.4.0rc1
- **CoFHCore is now a hard dependency**. Big Reactors will *not* work without CoFHCore installed.
- Upgrade: Upgraded to Forge 10.13.0.1198. Thanks to Parker8283 for the help!
- Refactor: Completely new network messaging system, originally based on Pahimar and Parker8283's systems.
- Refactor: Gradle build system modernized and properly uses Git submodules now.
- Refactor: Reactor icon selection mechanism moved to client-side. Reactors no longer change metadata on assembly. Reduces reactor assembly CPU and network load by many orders of magnitude.
- Refactor: Reactor control rod is now a subblock of the regular reactor parts instead of its own block.
- Refactor: Cyanite reprocessor now based on CoFHCore classes. Can be dismantled with TE crescent hammer (shift+rightclick).
- Refactor: Rewrote inventory/fluid port exposure mechanism on reprocessors. Faster, cleaner code.
- Refactor: Added @Optional annotations so BR no longer needs to include any external API code.
- Refactor: Rewrote reactor fuel/waste tracking to use new "reactant" system instead of fluids. Faster, cleaner, and allows for easier addition of more fuels and reactions.
- Refactor: Made most reactor functionality data-driven in preparation for 0.5.
- Refactor: Eliminated direct reads from Ore Dictionary; now using CoFHCore's OreDict helper for significant speed improvements.
- Refactor: Reprocessors and access ports now cache adjacent inventory connections instead of querying every time. Lighter on servers, esp. when a reactor's access port fills up.
- Usability: Control rod GUI now has visible control rod meter. Can also step the control rod in/out by 1, 5, 10 or 100 percent, and can also set ALL control rods on a reactor at once.
- Bugfix: RF/t gauge on turbines and reactors was inaccurate at very high (megaRF+) values. This has been fixed.
- Bugfix: Appropriate tile-change events are now scattered when access ports and fluid ports (coolant & turbine) load; should fix "pipe does not connect" issues.
- Bugfix: A rare combination of config settings could cause startup crashes. This has been fixed.
- Bugfix: Pipes can no longer fill up turbine fluid ports in outlet mode by accident.
- Enhancement: Lead, Zinc and Manyullyn blocks added to list of accepted blocks for reactor interiors and turbine coils.
- Enhancement: Reactor parts (controllers, power taps, etc.) can now be placed on top and bottom faces of the reactor, just like turbines.
- Enhancement: Turbine and Reactor computer ports now expose `getMinimumCoordinate` and `getMaximumCoordinate` commands for machine-size computations.
- Localization: Russian translation updated. Thanks Adaptivity!

### Release 0.3.4A2
- Bugfix: Proxy network code fixed. Coil clutch now works properly.

### Release 0.3.4A
- Bugfix: Fixed rare crash when the Forge fluid registry has not finished initializing (race condition)
- Bugfix: Reactors properly prioritize ejecting waste to outlet ports when outlets are available
- Bugfix: RedNet inputs for control rod insertion are now properly clamped to [0,100] range.
- Bugfix: Creative coolant inlets now completely fill reactor coolant tanks each tick
- Bugfix: Rotors' visible RPMs now match value shown in UI, regardless of framerate
- Bugfix: Turbines no longer hit "NaN" RPM if no coils are attached to the rotor
- Bugfix: Reactor and turbine blocks are now marked as invalid for monster spawning. Mobs should no longer spawn on or in reactors and turbines
- Balance: Hardness of blocks has been adjusted. Blocks should no longer take forever to break.
- Balance: Turbine fluid intake tank expanded to 4000mB to permit better buffering of fluids. Processing rate still limited to 2000mB/t.
- Enhancement: Recipe for creating cyanite ingots from yellorium ingots and sand added. Can be disabled via enableCyaniteFromYelloriumRecipe setting.
- Enhancement: Turbine height limits may now be independently configured via maxTurbineHeight config value
- Enhancement: Reactor Computercraft API now permits querying of maximum amount of hot fluid and coolant permitted in their respective tanks. getCoolantAmountMax() and getHotFluidAmountMax()
- Enhancement: Turbine coils may now be engaged/disengaged via a "clutch" in the GUI. This allows turbines to spin up to operating speed faster.
- Enhancement: Turbine "clutch" may be controlled via Turbine ComputerCraft API: getInductorEngaged() and setInductorEngaged(boolean)
- Enhancement: Turbine GUI reorganized. Tooltips clarified and tooltips for flow rate governor added.
- Optimization: Waste ejection code rewritten for efficiency and clarity. Should be somewhat faster now.

### Release 0.3.3A
- Bugfix: Disabling creative parts no longer causes client crashes when turbines are assembled
- Bugfix: Corrupted metadata on Ingots no longer causes crashes
- Bugfix: Passive reactors were cooling off (and generating energy) very slightly too fast. This has been fixed.
- Bugfix: On Reactor Redstone Ports, the "While off" setting for control rod insertion was being ignored. This has been fixed.
- Enhancement: When using the eject fuel/waste buttons on an access port, fuel/waste now only appears in the access port whose GUI is open
- Enhancement: Reactor Power Tap crafting recipe can now be disabled in the config via the enableReactorPowerTapRecipe setting
- Enhancement: Reactor's Computer Port now exposes getHotFluidProducedLastTick(), which returns 0 when passively cooled and the mB of hot fluid produced when actively cooled.
- Enhancement: Turbine's Computer Port can now manipulate vent settings via setVentNone(), setVentOverflow(), and setVentAll(). No arguments required.
- Enhancement: Spanish translation, thanks k3936!

### Release 0.3.2A
- Enhancement: Swedish translation, thanks erucolindo!
- Upgrade: German translation is now current for 0.3, thanks Vexatos!
- Bugfix: Alternate translations can now actually be used. Thanks Vexatos!
- Bugfix: Reactor/Turbine computer ports can now connect to Computercraft 1.6 computers and modems. This was broken in 0.3.1A.
- Bugfix: Fixed crashes related to teleporting near a turbine fluid port (introduced in 0.3.1A)
- Bugfix: Added some code so that chunk boundary handling is sliiiiightly nicer and should fail not quite as often. Still, putting ports on the boundary of non-chunkloaded chunks is still wonky. Blame minecraft.
- Did some behind-the-scenes upgrades so automated building is nicer, thanks AbrarSyed!


### Release 0.3.1A
- Forge: Big Reactors now requires Forge version 953 or higher.
- Enhancement: Brazilian Portuguese translation, thanks Kevin8082!
- Enhancement: OpenComputers, versions 335 and higher for 1.6.4, is now supported via the Reactor and Turbine computer ports. Thanks fnuecke!
- Upgrade: Big Reactors is now compatible with ComputerCraft 1.6 (CC 1.5 support has been dropped)
- Bugfix: Reactors no longer fail to operate if cryotheum freezes water inside them during operation
- Bugfix: Turbine fluid ports now properly show whether they're inlets or outlets while a turbine is not assembled
- Bugfix: Turbine fluid ports can now be toggled between inlet/outlet mode while a turbine is disassembled via the usual methods (empty hand, wrench)

### Release 0.3.0A
- Bugfix: Fuel rods no longer visually "overflow" with fuel when one or more fuel rods are broken on a reactor with fuel inside
- Bugfix: Fuel rods now update their visual states when they should, instead of randomly/infrequently
- Bugfix: Turbine power taps now light up when compatible wires are placed next to them
- Bugfix: Cyanite reprocessors can again be managed with itemducts and other automation tools (e.g. AE)
- Bugfix: Potential fix for a rare crash due to cross-mod interference with some Minecraft GUI code
- Bugfix: Turbine computer port actually has a recipe now

### Release 0.3.0rc3
- Enhancement: Turbine and reactor fluid/coolant ports set to "outlet" mode will now automatically attempt to pump fluids into nearby fluid pipes/containers
- Enhancement: Yellorium and blutonium blocks can now be used to fuel reactors.
- Enhancement: Polish translation now available! Thanks, kostek00!
- Change: Size of a reactor's active coolant tanks is now based on the size of its casing. 100 mB per casing block, maximum 50 buckets.
- Change: Waste ejection has been simplified. The only options are now "auto-eject" or "do not auto-eject".
- Bugfix: Fixed a crash which could occur when breaking a reactor via a control rod while the reactor is running
- Bugfix: Mariculture's Titanium actually supported for turbine coils now
- Bugfix: TE Pyrotheum can be placed in reactor cores again
- Bugfix: "Dump all fuel" option now actually works

### Release 0.3.0rc2
- Enhancement: Russian translation, thanks to Vladimir Gendel!
- Enhancement: Glass can now be used inside a reactor. It's not a very good moderator or heat conductor, but is useful for corralling fluids.
- Enhancement: Invar and enderium blocks can now be used as turbine coil parts.
- Enhancement: TE, Mekanism and Metallurgy metals can now be used inside a reactor, similar to iron/gold/diamond/emerald blocks.
- Enhancement: Redstone Arsenal's fluxed electrum blocks can now be used in reactors and turbine coils.
- Enhancement: 3 new methods added to Turbine Computercraft API
- Enhancement: More-expensive coil parts now extract more rotor energy per tick instead of just being more efficient at converting rotor energy. This means you need fewer coil parts when using very expensive blocks.
- Balance: Cryotheum's heat transfer, moderation capability and heat efficiency have been significantly boosted
- Config: Metallurgy's fantasy metals can be disallowed as coil parts in the config if you feel them unbalanced. Set "enableMetallurgyFantasyMetals" to false.
- Config: Added "comedy" option. If enabled, allows MFR's sewage, meat blocks and fluid meat/pink-slime to be used as a slight upgrade to water inside a reactor. Disabled by default.
- Bugfix: Rotor turbines no longer become invisible if a turbine is broken while activated
- Bugfix: Control rods no longer cause fertility to skyrocket when inserted. (Thanks XXX!)
- Bugfix: Rotors no longer render incorrectly when bearings are on the top, east or south sides of the reactor.
- Bugfix: Hi-rez GUI icons no longer render at insane sizes
- Bugfix: Can now switch the direction of reactor coolant ports with both empty hands and a wrench, and it no longer spams chat messages
- Bugfix: Reactor coolant ports no longer delay their render update when not part of a reactor, so the visual switch between inlet/outlet happens instantly
- Bugfix: Reactors no longer occasionally "go haywire" and superfill themselves with fuel during chunk loading on SMP servers
- Bugfix: Turbines no longer randomly go super-overspeed during chunk loading on SMP servers

### Release 0.3.0rc1
- Core: Full rewrite of internal reactor mechanics. Substantially more efficient on servers.
- Core: Big Reactors and BeefCore now log to their own channels, for server admins' convenience.
- Feature: Reactors now have fluid inlet/outlet ports available to convert them to active cooling.
- Feature: Active-cooled reactors convert water into steam. Can be used in any mod which uses steam.
- Feature: Turbines have been introduced. Build a big turbine, pipe steam into it to generate power. Generally more efficient than passively-cooled reactors.
- Feature: Yellorium, cyanite, blutonium and graphite can now be converted to/from blocks for decoration and storage.
- Feature: Graphite blocks can now be used inside reactors as a radiation moderator. They're very good at this role.
- Feature: Turbines can be controlled via Computercraft. Build a Turbine Computer Port.
- Enhancement: Can now toggle direction of access ports by right-clicking them with a wrench, as well as within the UI.
- Enhancement: Reactor and turbine parts are more resilient to being hit, so they aren't accidentally disassembled so easily.
- Compatibility: Most metallurgy, Mekanism, TE and TiCo metals can be used inside turbines as induction coil parts. Rarer generally = better.
- Balance: Rebalanced reactor heat, energy production and fuel consumption. Most reactors should see all three rise compared to 0.2.
- UI: Reactor UI redone, tooltips on EVERYTHING! English only, currently.
- Bugfix: Fixed a rare crash that could occur with itemducts attached to reactor access ports set to "out" mode.

### Release 0.2.15A
- Core: Changed how block attachment is tracked to a much more robust system
- Bugfix: Fixed crashes that could occur when logging into chunks with lots of machines, on a low-spec computer running a large modpack.
- Bugfix: Fixed some potential spontaneous-disassembly bugs.
- Bugfix: Fixed a client crash that could occur when right-clicking the reactor to get debug output, in rare cases.

### Release 0.2.14A
- Enhancement: The empty-hand right-click validation tool now has more-informative error messages.
- Bugfix: Fixed a compatibility glitch between BR and Railcraft. Reactors should no longer mysteriously disassemble when BR is used alongside Railcraft.
- Bugfix: Tinker's Construct's fluid ender was performing no better than water inside a reactor. This has been fixed, it is now as good as TE's.
- Bugfix: Reactors which span two chunks should no longer throw random NPE crashes or [SEVERE] log warnings randomly when used on MCPC+ with asynchronous chunkloading enabled.

### Release 0.2.13A2
- Bugfix: (A2) Fixed a crash that could occur with a double chest split across chunk boundaries next to an access port set to output.
- Bugfix: Reactors now send radiation west, as well as the other cardinal directions. Silly typo.
- Bugfix: Fixed a crash that could occur with inventory-polling objects next to access ports (#83), such as AE import/export buses.
- Bugfix: Fixed the source of the weird "this should not be here" crashes (#81, #82, #77). Turned out there were odd cases with chunk unloading and/or modifying a large reactor that could make certain internal tracking lists inaccurate.
- Bugfix: Fixed a client crash that could occur when placing blocks really really fast after breaking them in SSP.
- Refactor: Changed from lists of coordinates to lists of parts for many operations, internally. This is considerably cheaper, CPU-wise, on a per-tick basis and also considerably more stable.

### Release 0.2.12A
- Bugfix: Fixed a rare crash when reactors ate the last of their fuel and then tried to create waste
- Bugfix: Working around a crash that appears to be caused by an interaction with Forge Multipart. Log lines will be spammed to help further debug this issue.
- Bugfix: Maximum-dimension problems with assembly were not being reported by empty-hand right-clicks. This is fixed.

### Release 0.2.11A
- Bugfix: Cyanite reprocessors were not working when the 'registerYelloriumAsUranium' config was set to false. This has been fixed.

### Release 0.2.10A
- Config: Added 'Debugging' config section with 'debugMultiblocks' option. Set this to true if you like seeing debugging spam and/or you have a reproducible reactor bug and want to send in a debugging log.
- Config: Added 'useSteelForIron' config under the 'Recipes' section. Set this to true if you want Big Reactors to require steel ingots instead of iron ingots. If you do not have an installed mod which adds steel ingots, this setting is ignored. Defaults to off.
- Config: Added 'useExpensiveGlass' config under the 'Recipes' section. Set this to true if you want Big Reactors to require IC2 hardened glass or TE reinforced glass instead of plain glass. If you do not have an installed mod which adds reinforced or hardened glass, this setting is ignored. Defaults to off.
- Bugfix: Yellorium and graphite ingots should no longer be renamed by Gregtech if you install Big Reactors alongside Gregtech.
- Bugfix: Reactor online state should properly persist, finally. Reactors should maintain their online/offline state across world or chunk loads/unloads.
- Feature: Right-click on an incomplete reactor with empty hands, and you will receive a message indicating why the reactor is not completed. Note that this only tells you the first rule that was invalid, but should help when figuring out why very large reactors aren't assembling right.
- Core: Rewrote most of the multiblock library. It is now exponentially more efficient during chunk loading/unloading.
- Core: Rewritten multiblock code should be fully thread-safe, so it can be safely used with MCPC+'s asynchronous chunk loading feature. This should entirely eliminate "double add" exceptions and similar crashes related to asynchronous block changes.
- Core: Multiblock system now supports tracking multiple types of multiblocks. Not yet used in Big Reactors.
- Core: Client-side multiblock handlers now recalculate their size properly and also have their own update loop, in case there's some client-only multiblock game logic. This is not yet used in Big Reactors.
- Localization: German localization improved, thanks Vexatos!

### Release 0.2.9A3
- Bugfix: A more paranoid version of the 0.2.9A NPE crash fixes. Auto-repairs a situation that leads to a NPE during reactor assembly. If a reactor ends up corrupted, it is also now possible to tear it down and rebuild it to fix it.

### Release 0.2.9A
- Bugfix: Fixed a rare crash that could occur when adding fuel rods to a reactor and leaving them unfueled before starting the reactor again
- Config: Added "registerYelloriumAsUranium" under Recipes section. When set to true, yellorium ingots will be aliased in the ore dictionary as "ingotUranium", for use with other mods, and BR recipes will also accept uranium in place of yellorium. When false, recipes will be forced to use yellorium and yellorium will only be registered in the ore dictionary as ingotYellorium. Defaults to true.
- Config: Added "registerYelloriteSmeltsToUranium" under Recipes section. When set to true, yellorite ore will smelt into whatever has been set as "ingotUranium" in the ore dictionary, for use with other mods. When false, yellorite will be forced to smelt into yellorium ingots. Defaults to true.
- Revert: Reverted the bugfix for reactors going offline on chunkload from 0.2.8A. It was causing frequent crashes on servers. Will try to fix this again later.

### Release 0.2.8A
- Bugfix: Computer Port's getEnergyStored method now returns full amount of energy stored, as documented.
- Bugfix: World generation code streamlined, fixed to be compatible with mods that change the underground significantly, e.g. Underground Biomes. Big Reactors now uses vanilla's worldgen code. Ore clusters will look like vanilla clusters and have more variability in size.
- Bugfix: Squashed the reactors-go-offline-when-chunks-unload bug, finally. Again. I think.
- Bugfix: Thermal Expansion's new energy conduits properly connect to power taps again (only affects TE b9c and above)
- Config: Yellorite cluster size and cluster number configs have had their names and default values changed due to the above.
- Balance: Cryotheum rebalanced to be better than water. Derp.
- Compatibility: Reactor glass can now be made with anything in the ore dictionary as "glass", such as Tinker's Construct or Extra Utilies glass.

### Release 0.2.7A2
- Bugfix: Fixing a crash that can occur when using EnderIO power conduits with reactor power taps

### Release 0.2.7A
- Feature: Reactor interiors can now have iron, gold and diamond blocks put inside them. These perform better than water, in general.
- Feature: Reactor interiors can now have TE's redstone, glowstone, ender, pyrotheum and cryotheum fluids placed inside them. These perform better than water and iron, in general.
- UI: Stored-energy meters now visually match Thermal Expansion
- UI: Reactor controller now have visual meters for fueling and heat
- UI: Some tooltips have been improved, e.g. the cyanite reprocessor's fluid tank
- UI: Added red/green outlines to cyanite reprocessor to clarify how the inventory exposure stuff works
- Bugfix: Fixed crash on resizing the screen with the cyanite reprocessor's GUI open
- Bugfix: Fixed all GUIs having their controls show up twice when the screen is resized while they're open
- Bugfix: Computer Ports were not allowing per-control-rod queries or most "set" methods, due to a data type mixup. Fixed them.
- Bugfix: Fixed a crash that happened when putting single-size chests next to outlet ports or cyanite reprocessors

### Release 0.2.6A
- Feature: Reactor Computer Ports added. Control your reactor with a ComputerCraft computer!
- Feature: RedNet and Redstone ports now have an output option to read how full the reactor's internal energy buffer is, as a percentile (0-100) value.
- UI: Reactor Controller now shows how full the reactor is with fuel and waste. Mouse-over for an absolute value in the tooltip.
- Bugfix: Fixed a crash that occured when connecting UE pipes to a cyanite reprocessor
- Bugfix: Fixed a crash that could occur when opening the cyanite reprocessor's UI on Java 1.7

### Release 0.2.5A2
- Bugfix: Prevent server crashes due to inclusion of GUI methods when creating redstone ports

### Release 0.2.5A
- Feature: Redstone Ports, which can accept input and provide output via regular old redstone and anything compatible with it.
- Performance: RedNet ports no longer send updates every tick. They now send updates every 20 ticks, by default. This rate can be controlled via the ticksPerRedstoneUpdate config value and also applies to redstone ports.
- Bugfix: Cyanite reprocessor GUI's icons for exposing inventories & fluid tanks are now proper icons, not just colors.

### Release 0.2.4A
- Bugfix: Blutonium can again be added to reactor access ports via automation (e.g. itemducts)
- Bugfix: Energy output number displayed for ludicrously-huge reactors was inaccurate. This is fixed.
- Bugfix: Cyanite reprocessors were starting with 5000 free RF inside themselves. This has been removed.
- BeefCore: Client-side data is now properly calculated on world load. Internal fix only, no visible effects.

### Release 0.2.3A
- Bugfix: Tooltips now work in the RedNet port UI
- Optimization: Fuel rods now use a MUCH more efficient rendering method. Large reactors should be much less laggy to look at.
- KNOWN ISSUE: Control rods are sometimes lit improperly

### Release 0.2.2A
- UI: Reactors now show estimated fuel consumption. Not very accurate for small reactors!
- UI: Reactors now show fuel richness as a percentage. This is the percent of stuff inside the reactor that's fuel (as opposed to waste).
- Feature: Cyanite reprocessors and access ports will now emit products into chests and other adjacent inventories, such as TE machines
- Rebalance: Heat & radiation mechanics overhauled and simplified. Base RF output is up, overheating penalties are higher, efficiencies available via water and fuel fertilization are also higher. Should no longer have cryogenic fuel rods.
- Optimization: Heat transfer in tall reactors should now be much, much less expensive on the CPU, at the cost of a small degree of accuracy
- Bugfix: TE pulverizer and induction smelter recipes work again

### Release 0.2.1A
- Bugfix: Fixed a startup crash that prevented SMP servers from running

### Release 0.2.0A
- FORGE: Big Reactors now requires Forge 916 for Minecraft 1.6.4
- Feature: RedNet connection block added. Read data from, and send commands to, your reactors via MFR's RedNet.
- Feature: Control Rods can now be named via their UI
- Compatibility: Support for Thermal Expansion 3's Redstone Flux (RF) added.
- Compatibility: Support for Thermal Expansion 3's Item Conduits added.
- Compatibility: BuildCraft MJ power is no longer supported.
- Compatibility: Universal Electricity Joules are no longer supported. As of 1.6.4 versions, UE now supports TE's Redstone Flux.
- Optimization: Small reductions in network traffic due to minor internal refactoring
- Settings: userWorldGenVersion. If you want to re-run world regeneration, increment this number in your settings file.

### Release 0.1.14A
- Bugfix: Fixed a rare crash that occurs in long-running, very large reactors

### Release 0.1.13A
- BeefCore: Fixed a SMP server load crash that happened when chunkloaders were used in conjunction with reactors (#16)
- BeefCore: Fixed several issues that caused reactors to corrupt and crash related to breaking/re-assembling reactors (#18)
- BeefCore: Multiblocks on client now properly fission and reconnect when a reactor is broken/assembled (#19)
- BeefCore: Fixed a memory clean in singleplayer; reactors were not being cleaned up from the registry
- Bugfix: Mekanism combiners no longer provide free yellorium doubling
- Bugfix: Power taps now work on all 4 faces, not just east/west. Derp. (#15)

### Release 0.1.12A
- Feature: Global reactor output can now be modified with the floating-point "powerOutputMultiplier" config setting. Defaults to 1.0.
- Bugfix: Reactors no longer cause crashes if you replace power taps with non-power-tap blocks
- Bugfix: Reactors now reconnect to wires properly when a world reloads
- Bugfix: Can now insert non-BR fuels (e.g. UE uranium) into access ports via pipes/automation
- Bugfix: Can now insert non-BR wastes & fuels (e.g. UE uranium) into the cyanite reprocessor via pipes/automation
- Bugfix: Mekanism's crusher can no longer refine graphite dust into blutonium dust, but it can properly crush blutonium ingots.

### Release 0.1.11A
- (BeefCore) Fixed many bugs related to chunks loading/unloading as you move about the world
- (BeefCore) Fixing UI jitter caused by improper tick updates
- (BeefCore) Fixes some bugs where machines wouldn't re-assemble properly
- (BeefCore) Eliminated a lot of chunk-thrashing when blocks load or get removed on chunk boundaries

### Release 0.1.10A
- Added: Optional new recipes to craft graphite bars from 2 gravel and 1 coal or charcoal, for compatibility with UE mods. They default to off.
- Added: Four new config options, which control the recipes for creating graphite bars, two for smelting and two for crafting.
- Bugfix: Reactors can no longer be built without yellorium fuel rods inside
- Bugfix: Reactors that were built without yellorium fuel rods no longer crash the client

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
