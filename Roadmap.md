Big Reactors Roadmap
====================

What is this?
-------------

This is a tentative plan for the development of Big Reactors. It serves as something of a TODO.

Nothing that you see in this file should be taken as gospel. It's a collection of notes, little more. If, in the course of implementing a feature, it turns out that something does not work nicely as described here, I will not hestitate to do what's fun rather than what's planned.

Known Bugs
----------
- Tank/Inventory exposure buttons only show the top-left pixel of their respective image. Fix GuiImageButton.
- Reactors do not keep their chunks loaded if placed across multiple chunks; use external chunkloaders for now!

TODO - Alpha
------------

### Core
- Add maximum-dimension API and checks to BeefCore
- Fix onDisassemble and invalidate(). Use onChunkUnload to remove a block and disassemble the machine without fucking up the user's settings. Also have it disable the machine due to chunk-unload.

TODO - Beta
-----------

### Add redstone interfaces
- Reactor on/off switch
- Control rod in/out switch
- Signal emitter if temperature gets above/below a certain level

### Interoperability
- Add graphite dust if a mod with grinders is detected
- Refactor liquids using new Forge liquid interfaces
- Making flowing-liquid blocks
- Add MFR compatibility for drinking with a straw
- Add nasty side effects for going near pools of yellorium
- RedNet integration
- ComputerCraft integration!

### Gameplay
- Finish the RTG for mid/early-game power. Should be easy with the new TE framework I've built.
- Change refueling to be per-column instead of pan-machine. This considerably simplifies the logic
  and prevents problems with unbalanced fuel distribution across rods.

### Graphics & UI
- Add graduation marks to liquid progress bars
- Add a cool mixed fuel/waste bar to the control rods
- Show fuel/waste overall mix in reactor controller
- Multi-page reactor controller to remote-control control rods
- Unshitty artwork.
- Cool particle effects when the reactor is on!
- Graphical GUI meters & tooltips

### Reactor Mechanics
- Liquid interface blocks, so fuel can be pumped into/out of a reactor as a liquid
- Add active cooling system; coolant pipes and stuff
- Blutonium: give it different properties than yellorium.
- Radiation reflectors: a passive internal block that reverses the direction of a radiation packet, at the cost of some scattering
- Radiation refractor: a passive internal block that refracts radiation (changes direction by up to 90deg), at the cost of some scattering

### Multiblock Reprocessing
- Electrode controllers & electrode stacks, discharge large amounts of electricity between nearby electrodes.
- Any reprocessing tanks between the stacks have some waste converted to fuel
- Reprocessing tanks are mounted atop reprocessing tank valve blocks.
- Needs sweet lightning effects

### Core
- Create ITileEntityProxy to handle things like fuel columns more nicely
- Add ChunkLoading mechanic. Either reactors must act as chunkloaders for all occupied chunks,
or they must somehow keep all of their constituent chunks loaded if any single chunk is loaded
by players.

Wishlist / Post-Release
-----------------------
- Make better APIs/interfaces for extending the reactor
- Add different ways to refine/recycle fuels & wastes, maybe use TE's liquid redstone/glowstone for better yield
- Different types of coolant, liquid coolant pipes
- Indirect power generation: consume water in coolant pipes to produce steam
- Run steam through a generator to produce even more power
