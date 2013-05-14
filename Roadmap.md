Big Reactors Roadmap
====================

What is this?
-------------

This is a tentative plan for the development of Big Reactors. It serves as something of a TODO.

Nothing that you see in this file should be taken as gospel. It's a collection of notes, little more. If, in the course of implementing a feature, it turns out that something does not work nicely as described here, I will not hestitate to do what's fun rather than what's planned.

Known Bugs
----------
- When a wire burns out, it does not disconnect the power tap
- Reactor glass breaks instead of dropping a block

TODO - Pre-Alpha
----------------

- Add auto-emit, like MFR's machines, so BC pipes don't need power

TODO - Alpha
------------

### Core
- Add maximum-dimension API and checks to BeefCore

### Graphics & UI
- Make halfway-decent UIs with better visualizations than just text
- Add labels to access port UI, maybe a tooltip or two
- Make glass have a border when disassembled, no border when assembled

### Refactor fuel rods
- Make them a pseudo-multiblock controlled/rendered by the control rod
- Make sure bounding box is set properly
- Add ability to toggle control rods on/off, changing radiation absorption properties

### Balance & Completion
- Enricher machine. Consumes cyanite and some kind of liquid to produce yellorium
- More-proper heat/radiation numbers. Reasonable heat production. See notes (on paper, sorry internet).

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

### Graphics & UI
- Finish the RTG for mid/early-game power
- Unshitty artwork.

### Reactor Mechanics
- Liquid interface blocks, so fuel can be pumped into/out of a reactor as a liquid
- Add active cooling system; coolant pipes and stuff
- Blutonium. Change cyanite reprocessing to produce blutonium, give it different heat/radiation properties.

Wishlist / Post-Release
-----------------------
- Make better APIs/interfaces for extending the reactor
- Add different ways to refine/recycle fuels & wastes, maybe use TE's liquid redstone/glowstone for better yield
- Different types of coolant, liquid coolant pipes
- Indirect power generation: consume water in coolant pipes to produce steam
- Run steam through a generator to produce even more power
