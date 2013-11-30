Big Reactors Roadmap
====================

What is this?
-------------

This is a tentative plan for the development of Big Reactors. It serves as something of a TODO.

Nothing that you see in this file should be taken as gospel. It's a collection of notes, little more. If, in the course of implementing a feature, it turns out that something does not work nicely as described here, I will not hestitate to do what's fun rather than what's planned.

Technical Debt / Fixes
----------------------

Known Bugs
----------
- Tank/Inventory exposure buttons only show the top-left pixel of their respective image. Fix GuiImageButton.
- Tooltips in BeefGUI seem to cause NEI GUI colors to get inverted. Fix this.

TODO - 0.2: The Redstone Update
-------------------------------
- Redstone interface block
- Allow different types of blocks inside reactor, e.g. iron blocks and TE cryotheum liquids
- Finish the RTG for mid/early-game power. Refactor the TE framework to operate via composition.

TODO - 0.3: The Coolant Update
------------------------------
- Cool particle effects when the reactor is on!

### Active Coolant Loop
- Coolant buffer in main reactor controller
- Coolant I/O interface blocks
- Reactor no longer generates power from heat directly, but instead converts coolant to superheated coolant
- Superheated Coolant can be processed directly into power, converts back into regular coolant

### Advanced coolant add-ons
- Coolant manifolds inside reactor allow fuel rod heat to convert coolant
- Heat exchanger allows conversion of superheated coolant + water -> steam + coolant
- Different types of coolant with different transference properties
- Particle effects for venting steam and stuff!

TODO - 0.4: The Fueling Update
------------------------------
### Gameplay
- Change refueling to be per-column instead of pan-machine. This considerably simplifies the logic
  and prevents problems with unbalanced fuel distribution across rods.

### Reactor Mechanics
- Blutonium: give it different properties than yellorium.
- Blutonium: Create a proper fluid for it so it can be handled as a first-class member of the fuel cycle
- Control Rods: Add "dump contents" button so they can be forcibly emptied

TODO - 0.5: The Reprocessing Update
-----------------------------------
### Multiblock Reprocessing
- Electrode controllers & electrode stacks, discharge large amounts of electricity between nearby electrodes.
- Any reprocessing tanks between the stacks have some waste converted to fuel
- Reprocessing tanks are mounted atop reprocessing tank valve blocks.
- Needs sweet lightning effects

TODO - General
--------------

### Core
- Move away from using metadata to distinguish assembled/disassembled blocks; this will fix client lag on assembly/disassembly

### Graphics & UI
- Highlight inventory slots when they are exposed via the right-hand-side buttons
- Add graduation marks to liquid progress bars
- Add a cool mixed fuel/waste bar to the control rods

### Reactor Mechanics
- Radiation reflectors: a passive internal block that reverses the direction of a radiation packet, at the cost of some scattering
- Radiation refractor: a passive internal block that refracts radiation (changes direction by up to 90deg), at the cost of some scattering

Wishlist
--------
- Make better APIs/interfaces for extending the reactor
- Migrate APIs to operate via IMC instead of requiring direct calls

### Interoperability
- ComputerCraft reactor peripheral? Requires research.
- Add IAntiPoisonBlock interface to reactor blocks from Atomic Science
- Add MFR compatibility for drinking BR fluids with a straw

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