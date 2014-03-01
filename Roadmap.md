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

TODO - 0.3: The Coolant Update
------------------------------
### Bugs
- Waste can somehow go negative when auto-replace waste is turned on. This leads to bizarre-ass shit.
- When reactors overfill, their graphics glitch the fuck out
- Water appears to vaporize into 1/10th its input?!

### Gameplay Tweaks
- Change how different level coils work. Better metals should strip more energy off as well as being more efficient.
- Document CC API: getEnergyProducedLastTick - also returns fluid flow
- CC API: # of blades, rotor size/mass, rotor blade efficiency numbers for turbines

### Graphics
- Cool particle effects when the reactor is on! (requires making my own particle, bleah)

### Multiblock Turbine
- Optional explosion during severe overspeed conditions
- Redstone port.
- Different types of rotors and blades, made of different metals

### Advanced coolant add-ons
- Coolant manifolds inside reactor add extra surface area - must be adjacent to casing, other manifold or fuel rod
- Multiblock heat exchanger allows conversion of superheated coolant + water -> steam + coolant
- Different types of coolant with different transference properties
- Add "special steam" mode for people who complain about Power Converters/Railcraft steam

### Reactor meltdowns (may slip to 0.4)
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

TODO - 0.4: The Fueling Update
------------------------------
### Core
- Change reactor icon selection mechanism to be like turbines and reorganize metadata to not use metadata entries so wastefully

### Gameplay
- (DONE) Rewrite fertilization mechanics to be more sane/useful and expose fertility via control rod UI
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

TODO - 0.5: The Reprocessing Update
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
- Radiation refractor: a passive internal block that refracts radiation (changes direction by up to 90deg), at the cost of some scattering

Wishlist
--------
- Make better APIs/interfaces for extending the reactor
- Migrate APIs to operate via IMC instead of requiring direct calls

## User Interface/Graphics
- Add a temperature gauge block and other "display blocks" that can be plugged into reactor
- Add remote versions of above that read their inputs from RedNet

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