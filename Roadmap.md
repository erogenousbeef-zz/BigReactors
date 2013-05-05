Big Reactors Roadmap
====================

What is this?
-------------

This is a tentative plan for the development of Big Reactors. It serves as something of a TODO.

Nothing that you see in this file should be taken as gospel. It's a collection of notes, little more. If, in the course of implementing a feature, it turns out that something does not work nicely as described here, I will not hestitate to do what's fun rather than what's planned.

Known Bugs
----------
- [ ] When a wire burns out, it does not disconnect the power tap (UE)
-- Disconnect power taps that detect their connected TEs no longer exist during an update loop

- [ ] Windows larger than 2 blocks in any dimension will not properly break the reactor when broken
-- Create a special "reactor glass" type.

TODO - Pre-Alpha
----------------

- [X] Split project into BeefCore and BigReactors
- [X] Finish initial multiblock library in BeefCore
-- [X] Conflict resolution for colliding multiblock machines
-- [X] Test difficult-to-detect errors, such as multiblock fusion/mitosis

- [ ] Fix NBT save/load for MultiblockReactor objects
-- [ ] Implement save/load on MultiblockReactor class

- [ ] Fix network communication for MultiblockReactor objects
-- [ ] Send updates properly through the network delegate

- [ ] Create fuel access port block
-- [ ] Create UI for access port block
-- [ ] Configuration: User can choose which inventory slot is externally-addressible
--- This provides LogiPipes/AE/BC pipe compatibiltiy
-- Yellorium ingots in the "intake" section are automatically consumed into the reactor to refill fuel rods
-- Depleted Yellorium is spat out as an ingot when there's a full ingot's worth in the fuel rods
-- [ ] Add auto-emit, like MFR's machines, so BC pipes don't need power

- [ ] Rename "Depleted Yellorium" to "Blutonium" or something cute.

- [ ] Add graphite ingots
-- [ ] Add graphite dust if a mod with grinders is detected
-- Made from coal & cobblestone, unshaped recipe

- [ ] Add in initial crafting recipes for reactor parts
-- [ ] Basic reactor plating
-- [ ] Reactor core
-- [ ] Fuel rod
-- [ ] Control rod
-- [ ] Power tap
-- [ ] Fuel access port

- [ ] Create reactor glass blocks, remove support for standard glass.

TODO - Alpha
------------

- [ ] Finish the RTG for mid/early-game power

- [ ] Make halfway-decent UIs with better visualizations than just text

- [ ]  Refactor fuel rods
-- [ ] Make them a pseudo-multiblock controlled/rendered by the control rod
-- [ ] Remove internal LiquidTank usage, just use integers, jesus.
-- [ ] Make sure bounding box is set properly

- [ ] Add ability to toggle control rods on/off, changing radiation absorption properties

- [ ] Add redstone interfaces
-- [ ] Reactor on/off switch
-- [ ] Control rod in/out switch
-- [ ] Signal emitter if temperature gets above/below a certain level

- [ ] Basic fuel cycle
-- [ ] "Enricher" or some way to recycle depleted yellorium
-- [ ] "Dump" or "incinerator" or some way of destroying depleted yellorium
--- Maybe just let people use void pipes or something

TODO - Beta
-----------

- [ ] Unshitty artwork.

- [ ] Refactor liquids using new Forge liquid interfaces

- [ ] Make real liquids for yellorium & depleted yellorium
-- Liquids should flow
-- [ ] Add MFR compatibility for drinking with a straw
-- [ ] Add nasty side effects for going near pools of yellorium

- [ ] RedNet integration
-- Do I have to do more than just provide a plain redstone interface implementation?

- [ ] Liquid interface blocks, so fuel can be pumped into/out of a reactor as a liquid
-- Maybe do this per-fuel-column?

- [ ] ComputerCraft integration!

- [ ] Make better APIs/interfaces for extending the reactor

Wishlist / Post-Release
-----------------------

- [ ] Expand fuel cycle
- [ ] Add different types of fuels
- [ ] Add coolant cells & coolant API
