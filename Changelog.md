Big Reactors Changelog
======================

Next Release (Anticipated Version: 0.2.0)
-----------------------------------------

Current Release (0.1.4A)
--------------------------------
- Cyanite reprocessors are now craftable. Oops.

Older Releases
--------------

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
