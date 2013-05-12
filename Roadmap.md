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
- Finish the RTG for mid/early-game power
- Make halfway-decent UIs with better visualizations than just text
- Actually try to balance power production
- Make glass have a border when disassembled, no border when assembled
- Add graphite dust if a mod with grinders is detected

### Refactor fuel rods
- Make them a pseudo-multiblock controlled/rendered by the control rod
- Remove internal LiquidTank usage, just use integers, jesus.
- Make sure bounding box is set properly
- Add ability to toggle control rods on/off, changing radiation absorption properties

### Add redstone interfaces
- Reactor on/off switch
- Control rod in/out switch
- Signal emitter if temperature gets above/below a certain level

### Basic fuel cycle
- "Enricher" or some way to recycle cyanite into blutonium
- Way to use blutonium as fuel, with different reactivity than yellorium

### Proper Coolant System
- Create cooling API, similar to the IRadiationModerator API
- Use this for fuel rod heat transfer

TODO - Beta
-----------
- Unshitty artwork.
- Refactor liquids using new Forge liquid interfaces
- Making flowing-liquid blocks
 - Add MFR compatibility for drinking with a straw
 - Add nasty side effects for going near pools of yellorium
- RedNet integration
- Liquid interface blocks, so fuel can be pumped into/out of a reactor as a liquid
- ComputerCraft integration!
- Make better APIs/interfaces for extending the reactor
- Add active cooling system

Wishlist / Post-Release
-----------------------

- Expand fuel cycle
 - Add different types of fuels
 - Add different ways to refine/recycle fuels & wastes
