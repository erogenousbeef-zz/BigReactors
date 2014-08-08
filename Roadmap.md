Big Reactors Roadmap
====================

What is this?
-------------

This is a tentative plan for the development of Big Reactors. It serves as something of a TODO.

Nothing that you see in this file should be taken as gospel. It's a collection of notes, little more. If, in the course of implementing a feature, it turns out that something does not work nicely as described here, I will not hestitate to do what's fun rather than what's planned.

Technical Debt / Fixes
----------------------
- Update the wiki!

Known Issues
------------
- WR-CBE receivers do not activate redstone ports in input mode correctly if they are placed directly next to the input port. Workaround: Place one tile's worth of redstone (or another mod's redstone wire) between the receiver and the input port.

- Setting the dormantChunkCacheSize Forge setting to something other than 0 (which is the default) will cause Big Reactors to break in strange and unusual ways. This setting is not supported.

TODO - 0.4: The 1.7.10 Update
-----------------------------
Okay, we're well into the 1.7.10 update now! **A lot of the massive refactoring is DONE**, including:
- *DONE* Fully rewritten networking and message-routing system
- *DONE* Reactor part rewritten to not do stupid things with metadata
  - *DONE* MUCH faster to assemble/disassemble, like 90% less network traffic on your average reactor
  - *DONE* Exponential reduction on very large reactors (99%+ less traffic)
- *DONE* Reactor part icon selection rewritten thanks to above
- *DONE* Reactor control rod merged into base reactor part, lots of dead code removed
- *DONE* Enabled reactor parts on top/bottom faces thanks to all of the above
- *DONE* Control Rod GUI has been overhauled
  - *DONE* Can set all control rods from single GUI
  - *DONE* Has tooltips explaining WTF control rod insertion does
  
Remaining Test Items:
- Ensure that multiblocks still reform when build across chunk boundaries
- Ensure that multiblocks still reform on chunk loads
- Ensure that turbines still assemble and operate
- Ensure that reactors operate properly and GUI updates propagate properly
- Ensure that cyanite reprocessor still works & inventory/fluid side changes still work
- Ensure that all of the above also work on multiplayer servers
- Test interoperability with TE 1.7.10 beta

When all of the above are marked **done**, I'll release a public experimental (i.e. test) build. It will be *0.4.0X1*.

TODO - 0.5: The Exotic Coolant Update
------------------------------
### Internals
- Coolant fluid pairs can be registered in a registry, allowing different types of fluids to be used as coolants
- Rebalance reactor interior list so it's not just a race to enderium
  - Make blocks mostly good at either generating energy OR moderating radiation, not both
  - Moderators improve as they become more transparent to slow radiation
  - Generators improve as they become less transparent to slow radiation

### Multiblock Turbine
- Optional explosion during severe overspeed conditions
- Redstone port.
- Different types of rotors and blades, made of different metals
- Additional very-high-end coil parts that extract Gratuitous Amounts of Energy

### Heat Exchanger
- Big multiblock machine which converts exotic coolants into steam
- Absorbs heat from exotic coolants into heat buffer, transfers buffered heat to water tank to create steam
- Has "special steam" modes which create compressed steam (x10 energy), ultradense steam (x100 energy) and steamium (x1000 energy)
- Primary fluid inlet accepts all mapped fluid pairs from coolant registry

### Multiblock Reactor
- Coolant manifolds inside reactor add extra surface area - must be adjacent to casing, other manifold or fuel rod
- Coolant inlet accepts all mapped fluid pairs from coolant registry

### For fun
- Add "hojillion"/"ho" as a prefix for ludicrously high amounts of energy

TODO - 0.6: The Fueling Update
------------------------------
### Core
- Change reactor icon selection mechanism to be like turbines and reorganize metadata to not use metadata entries so wastefully

### Gameplay
- Finish the RTG for mid/early-game power. Refactor the TE framework to operate via composition.

### Reactor Mechanics
- Blutonium: give it different properties than yellorium.
- Blutonium: Create a proper fluid for it so it can be handled as a first-class member of the fuel cycle
- Control Rods: Add "dump contents" button so they can be forcibly emptied
- Add fluid fuel interfaces & magma crucible recipes for TE to fluidize fuel
- Add "fluidizer" small machine - consumes power, outputs fluid fuel
- Add "fluidic reprocessor" small machine - reprocesses fluid wastes into fluid fuels
- Figure out how to do fuels/wastes with mixed composition?
- "Moxine" ingots/fluids.

### Reactor meltdowns
- Add option to enable reactor meltdowns to config
- When overheated, low chance of meltdown, based on heat.
- When meltdown occurs:
-- Reactor disassembles
-- One or more fuel rod blocks convert to corium fluid at the reactor's bottom
-- Zero or more explosions near the reactor's top
- Corium fluid acts like acid; slowly eats through materials beneath it.
- Touching corium fluid swiftly kills the shit out of you.
- Corium fluid eventually hardens into corium.
- Touching corium damages and withers you.

TODO - 0.7: The Reprocessing Update
-----------------------------------
### Multiblock Reprocessing
- Electrode controllers & electrode stacks, discharge large amounts of electricity between nearby electrodes.
- Any reprocessing tanks between the stacks have some waste converted to fuel
- Reprocessing tanks are mounted atop reprocessing tank valve blocks.
- Needs sweet lightning effects

### Blended fuels
- Create a way to breed or blend fuel fluids into better fuels

TODO - General
--------------

### Graphics & UI
- Highlight inventory & fluid slots dynamically when they are exposed via the right-hand-side buttons
- Change all UI strings to be in the localization file to allow for full localization

### Reactor Mechanics
- Radiation refactor: a passive internal block that refracts radiation (changes direction by up to 90deg), at the cost of some scattering

Wishlist
--------
- Make better APIs/interfaces for extending the reactor
- Migrate APIs to operate via IMC instead of requiring direct calls

## User Interface/Graphics
- Add a temperature gauge block and other "display blocks" that can be plugged into reactor
- Add remote versions of above that read their inputs from RedNet
- Cool particle effects when the reactor is on! (requires making my own particle, bleah)

### Interoperability
- Add MFR compatibility for drinking BR fluids with a straw
- Some way of extracting fuel from thaumcraft nodes?

### More stuff with liquids
- Add nasty side effects for going near pools of yellorium
- Add liquid interfaces/liquid fuel cycle

### Multiblock Power Storage (0.6?)
- Molten salt/liquid metal batteries
- Build big banks of highly-vertical battery cells
- Must warm up (reduced storage efficiency on startup)
- Once warmed up, remains warm so long as there's more power than a certain threshold inside
- Cools slowly if power within drops below threshold

### Fuel Pre-Processing (0.7?)
- Provide better ways of pre-processing reactor fuel dusts directly into fuel fluids at an enhanced rate
- Create a "fear engine" that gives bonuses to power output/fuel generation when exposed to hostile mobs

### Beamguide machine
- Goddamn it looks cool
