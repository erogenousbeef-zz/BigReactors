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
- Reactors are always marked as inactive when worlds are loaded

TODO - Alpha
------------

### Core
- Add maximum-dimension API and checks to BeefCore
- Create ITileEntityProxy to handle things like fuel columns more nicely
- Add "regenerative worldgen" so existing worlds don't need to be restarted (see Artifice for examples)

### Graphics & UI
- Make halfway-decent UIs with better visualizations than just text
- Add labels to access port UI, maybe a tooltip or two
- Make glass have a border when disassembled, no border when assembled
- Make waste ejection a setting; manual waste ejection (button-activated)/automatic waste ejection (threshold-activated)

### Refactor fuel rods
- (DONE) Make them a pseudo-multiblock controlled/rendered by the control rod
- (IN PROGRESS) Add ability to toggle control rods on/off, changing radiation absorption properties
- (IN PROGRESS) Make control rod insertion matter.
- (DONE) Implement game interfaces on new control rods (IRadiationModerator, etc.)
- Fix registry, so fuels/wastes can supply dynamic colors again
- Fix internal MultiblockReactor simulation to use new control rods instead of old ones

### Balance & Completion
- (DONE) Enricher machine. Consumes cyanite and some kind of liquid to produce yellorium
- (DONE) More-proper heat/radiation numbers. Reasonable heat production. See notes (on paper, sorry internet).
- (IN TESTING) Finish integrating new "incident radiation" system to fix above and retest numbers.
- (DONE) Blutonium. Properly integrate it into the fuel cycle. 

### Graphics & UI
- Finish the RTG for mid/early-game power. Should be easy with the new TE framework I've built.
- Make the current GUI's not suck quite as much. Decent alignment, colors, etc.
- Make the Enricher GUI not blow goats

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
- Unshitty artwork.
- Graphical GUI meters & tooltips

### Reactor Mechanics
- Liquid interface blocks, so fuel can be pumped into/out of a reactor as a liquid
- Add active cooling system; coolant pipes and stuff
- Blutonium: give it different properties than yellorium.

Wishlist / Post-Release
-----------------------
- Make better APIs/interfaces for extending the reactor
- Add different ways to refine/recycle fuels & wastes, maybe use TE's liquid redstone/glowstone for better yield
- Different types of coolant, liquid coolant pipes
- Indirect power generation: consume water in coolant pipes to produce steam
- Run steam through a generator to produce even more power
