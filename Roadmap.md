Big Reactors Roadmap
====================

What is this?
-------------
This is a tentative plan for the development of Big Reactors. It serves as something of a TODO.

Nothing that you see in this file should be taken as gospel. It's a collection of notes. In the course of implementing a feature, if something doesn't work out as nicely as described here, I will not hestitate to do what's fun rather than what's planned.

Technical Debt / Fixes
----------------------
- Update the wiki!

Known Issues
------------
- WR-CBE receivers do not activate redstone ports in input mode correctly if they are placed directly next to the input port. Workaround: Place one tile's worth of redstone (or another mod's redstone wire) between the receiver and the input port.

- Setting the dormantChunkCacheSize Forge setting to something other than 0 (which is the default) will cause Big Reactors to break in strange and unusual ways. This setting is not supported.

- Turbines do not properly (visually) assemble and have spotty UI updates if mod blocks are used as coils. This can be worked around prior to rc9 by using vanilla blocks, and after rc9 by using vanilla blocks or ludicrite blocks. This is due to a bug in CoFHCore and will be fixed in CoFHCore 3.0.0B7 (or newer).

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
- Add reactivity penalty to control rods, so a reactor with high control rod insertion is less efficient than a smaller reactor with lower insertion. Encourages right-sizing designs.

TODO - 0.6: The Fueling Update
------------------------------
### Gameplay
- Finish the RTG for mid/early-game power. Refactor the TE framework to operate via composition.

### Reactor Mechanics
- Blutonium: give it different properties than yellorium.
- Blutonium: Create a proper fluid for it so it can be handled as a first-class member of the fuel cycle
- Add fluid fuel interfaces & magma crucible recipes for TE to fluidize fuel
- Add "fluidizer" small machine - consumes power, outputs fluid fuel
- Add "fluidic reprocessor" small machine - reprocesses fluid wastes into fluid fuels
- Allow mixed reactants inside reactor, so long as their products are identical

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

Wishlist
--------
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

### Multiblock Power Storage (0.8?)
- Molten salt/liquid metal batteries
- Build big banks of highly-vertical battery cells
- Must warm up (reduced storage efficiency on startup)
- Once warmed up, remains warm so long as there's more power than a certain threshold inside
- Cools slowly if power within drops below threshold

### Fuel Pre-Processing (0.9?)
- Provide better ways of pre-processing reactor fuel dusts directly into fuel fluids at an enhanced rate
- Create a "fear engine" that gives bonuses to power output/fuel generation when exposed to hostile mobs

### Beamguide machine (0.8, definitely)
- Goddamn it looks cool
