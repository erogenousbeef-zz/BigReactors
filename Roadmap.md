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

### Core
- (DONE) Change BeefCore so client-side machines know when they're assembled, etc.
- (DONE FOR TURBINES) Change icon selection mechanism; instead of using metadata to determine texture, use TileEntity state information
- (DONE) Calculate & cache the side on which a reactor block is located on assembly, reset it on disassembly.
- (DONE) Corify the calculation & caching of sides, create a "cubic" base multiblock controller and TE that add those methods

- Add BeefLogger which parents to the FML logger. Makes logs easier to read.
- Remove debug rightclick code

### Fuel refactor
- (DONE) Move fuel and waste pools up to reactor level. Size determined by # fuel rods.
- (DONE) Add way to track when fuel was last updated, send world updates as necessary to transmit fuel data
- (DONE) Add ISBRH for fuel rods and move fuel rendering into there. Keep control rod ISBRH for the rod itself.
- Rebalance fuel usage

### Radiation refactor number two
- (DONE) Radiate from one random rod each tick, but radiate in all four directions. Extrapolate results to entire reactor.
- (DONE) Fix neutron hardness to actually do something aside from being a straight nutpunch
- (DONE) Fix fertilization and expose fertility in UI.

### Heat Refactor number two
- (DONE) Fuel Rod heat pool instead of heat in individual fuel rods
- (DONE) Heat transfer rate precalculated on reactor assembly, based on surfaces in contact with non-fuel-rod stuff
- (DONE) Calculate "effective coolant surface area" based on interior surface area of reactor housing
- (DONE) Passively-cooled reactors generate power based on effective coolant surface area
- (DONE) Add minimum heat transfer/loss so we don't hit floating point problems.
- (DONE) Fix reactor rednet, computer and redstone ports to not address individual control rods for heat, fuel and waste

### Active Coolant Loop
- (DONE) Actively-cooled reactors use surface area to determine how much heat is available to heat coolant per tick
- (DONE) Coolant buffer in main reactor controller
- (DONE) Coolant I/O interface blocks
- (DONE) When coolant I/O blocks are present during assembly, reactor no longer generates power directly, instead converts coolant.
- (DONE) Add Computercraft methods to monitor the coolant system

### Multiblock Turbine
- (DONE) Turbine consumes steam, produces power and water.
- (DONE) Turbine glass. Solve the connected-texture problem.
- (DONE) Validation of internal turbine & coil shape on assembly.
- (DONE) Add flow governor to limit rate of input fluid usage.
- (DONE) Do an art pass.
- (DONE) Fix turbine RPM bar to be green at the right places
- (DONE) Add efficiency curve that peaks at 900/1800 RPMs
- (DONE) Tune and optimize turbine torque & drag equations. Fluid speed is function of fluid, not of input volume. Add input volume as multiplier.
- (DONE) Water/other outputs can be vented. Vent setting can be changed. Turbine uses less input fluid when output tank is full and turbine is not venting.
- (DONE) Renderer to show off the turbine blade.
- (DONE) Test inflow/outflow ports with TE
- (DONE) Particle effects for venting steam and stuff!
- TESR for rotating turbine when machine is active
- BUG: Turbine does not always save its "active" state

### Graphics
- (DONE) Fix reactor glass texture. Change to a nicer texture and port connected-texture code from turbines.
- (DONE) Fix the lighting bug on control rods.
- (DONE) Add some flair to the reactor and turbine glass textures. They're TOO invisible now.
- (DONE) Add flair to turbine glass texture
- (DONE) Redo turbine exterior textures with "metallic blue" color instead of ugly current job

### User Interface
- (DONE) Add imagebutton for reactor on/off
- (DONE) Add graphical bars for coolant system: system heat, coolant/vapor tanks. Hide when passively cooled.
- (DONE) Change current heat bar to show fuel heat
- (DONE) Add RPM bar to turbine
- (DONE) Add "dump fuel" button to access port GUI
- Add turbine stuff to German and Chinese localization files

### Items
- Add nuggets for the 4 types of ingots
- Add blocks for the 4 types of ingots

### Multiblock Turbine (post-0.3.0)
- Cool particle effects when the reactor is on! (requires making my own particle, bleah)
- Optional explosion during severe overspeed conditions
- Redstone port.
- Computer port.

### Advanced coolant add-ons (post-0.3.0)
- Coolant manifolds inside reactor add extra surface area - must be adjacent to casing, other manifold or fuel rod
- Multiblock heat exchanger allows conversion of superheated coolant + water -> steam + coolant
- Different types of coolant with different transference properties

### Reactor meltdowns (post-0.3.0, may slip to 0.4)
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