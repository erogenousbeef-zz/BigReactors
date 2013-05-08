Big Reactors Roadmap
====================

What is this?
-------------

This is a tentative plan for the development of Big Reactors. It serves as something of a TODO.

Nothing that you see in this file should be taken as gospel. It's a collection of notes, little more. If, in the course of implementing a feature, it turns out that something does not work nicely as described here, I will not hestitate to do what's fun rather than what's planned.

Known Bugs
----------
- (CRITICAL) Client-side reactors do not recognize assembly properly when being loaded
- When a wire burns out, it does not disconnect the power tap
- Reactor glass breaks instead of dropping a block

TODO - Pre-Alpha
----------------
- (DONE) Split project into BeefCore and BigReactors
- (DONE) Finish initial multiblock library in BeefCore
- (DONE) Conflict resolution for colliding multiblock machines
- (DONE) Test difficult-to-detect errors, such as multiblock fusion/mitosis
- (DONE) Port BigReactors to BeefCore, fix bugs

### Fix NBT save/load for MultiblockReactor objects
- (DONE) Implement save/load on MultiblockReactor class
 - (DONE) Save/load user configuration ("active" state)
 - (DONE) Save/load game state (heat)

### Fix network communication for MultiblockReactor objects
- (DONE) Send updates properly through the network delegate

### Fuel access port block
- (DONE) Create fuel access port block
 - Yellorium ingots in the "intake" section are automatically consumed into the reactor to refill fuel rods
 - Depleted Yellorium is spat out as an ingot when there's a full ingot's worth in the fuel rods
- (DONE) Create UI for access port block
- (DONE) Configuration: User can choose which inventory slot is externally-addressible
 - (DONE) This provides LogiPipes/AE/BC pipe compatibility
- (PARTIAL) Add auto-emit, like MFR's machines, so BC pipes don't need power

### Add in initial crafting recipes for reactor parts
- Add graphite ingots (coal & cobble, unshaped recipe)
- Add graphite dust if a mod with grinders is detected
- Basic reactor plating
- Reactor core
- Fuel rod
- Control rod
- Power tap
- Fuel access port
- Rename "Depleted Yellorium" to "Blutonium" or something cute.
- (DONE) Create reactor glass blocks so people can see inside

- Take a stab at balancing power production

TODO - Alpha
------------
- Finish the RTG for mid/early-game power
- Make halfway-decent UIs with better visualizations than just text
- Actually try to balance power production
- Make glass have a border when disassembled, no border when assembled

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
- "Enricher" or some way to recycle depleted yellorium
- "Dump" or "incinerator" or some way of destroying depleted yellorium
 - Maybe just let people use void pipes or something?

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

Wishlist / Post-Release
-----------------------

- Expand fuel cycle
 - Add different types of fuels
 - Add different ways to refine/recycle fuels & wastes
- Add coolant cells & coolant API
