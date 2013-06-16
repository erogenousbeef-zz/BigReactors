Big Reactors Roadmap
====================

What is this?
-------------

This is a tentative plan for the development of Big Reactors. It serves as something of a TODO.

Nothing that you see in this file should be taken as gospel. It's a collection of notes, little more. If, in the course of implementing a feature, it turns out that something does not work nicely as described here, I will not hestitate to do what's fun rather than what's planned.

Known Bugs
----------
- Tank/Inventory exposure buttons only show the top-left pixel of their respective image. Fix GuiImageButton.

TODO - Beta
-----------

### Core
- Create ITileEntityProxy to handle things like fuel columns more nicely

### Gameplay
- Finish the RTG for mid/early-game power. Should be easy with the new TE framework I've built.
- Change refueling to be per-column instead of pan-machine. This considerably simplifies the logic
  and prevents problems with unbalanced fuel distribution across rods.
- Add graphite dust & coal dust
- Add yellorite dust

### Graphics & UI
- Highlight inventory slots when they are exposed via the right-hand-side buttons
- Add graduation marks to liquid progress bars
- Add a cool mixed fuel/waste bar to the control rods
- Show fuel/waste overall mix in reactor controller
- Multi-page reactor controller to remote-control control rods
- Cool particle effects when the reactor is on!

### Add redstone interfaces
- RedNet Interface block
- Redstone Interface block
- These are configurable. RedNet allows for up to 16 I/O channels with continuous I/O.
- Reactor on/off
- Control rod in/out, or set specific % insertion via RedNet
- Signal emitter if temperature goes below X
- Signal emitter if global waste % goes over X
- RedNet Versions: Emit waste %/amt, Emit raw temperature

### Reactor Mechanics
- Add fuel->waste and waste->fuel item mappings to registry; remove hardcoded references to Ore Dictionary
- Blutonium: give it different properties than yellorium.
- Radiation reflectors: a passive internal block that reverses the direction of a radiation packet, at the cost of some scattering
- Radiation refractor: a passive internal block that refracts radiation (changes direction by up to 90deg), at the cost of some scattering

### Multiblock Reprocessing
- Electrode controllers & electrode stacks, discharge large amounts of electricity between nearby electrodes.
- Any reprocessing tanks between the stacks have some waste converted to fuel
- Reprocessing tanks are mounted atop reprocessing tank valve blocks.
- Needs sweet lightning effects

Wishlist / Post-Release
-----------------------
- Make better APIs/interfaces for extending the reactor
- Add different ways to refine/recycle fuels & wastes, maybe use TE's liquid redstone/glowstone for better yield

### ComputerCraft
- Reactor peripheral? Requires research.

### Liquids Refactor (minecraft 1.6)
- Refactor liquids using new Forge liquid interfaces
- Making flowing-liquid blocks
- Add nasty side effects for going near pools of yellorium
- Add MFR compatibility for drinking with a straw
- Add liquid interfaces/liquid fuel cycle

### Active Coolant Loop
- Coolant buffer in main reactor controller
- Coolant I/O interface blocks
- Reactor no longer generates power from heat directly, but instead converts coolant to superheated coolant
- Too much superheated coolant = EXPLOSION!
- Superheated Coolant can be processed directly into power, converts back into regular coolant

### Advanced coolant add-ons
- Coolant manifolds inside reactor allow fuel rod heat to convert coolant
- Heat exchanger allows conversion of superheated coolant + water -> steam + coolant
- Different types of coolant with different transference properties