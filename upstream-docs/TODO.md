# TODO

## Major Features
* Programmable Mind
  * A specialized artificial mind that may be encoded with a hex
  * Can be loaded into a flayed villager or a slate golem
  * May cast hexes, but draws media from their inventory
  * May move, interact with its inventory, fight, etc.
* Reprogrammed Villager
  * Repurposed flayed villager housing a programmable mind
  * Can wear armor
* Slate Golem
  * Artificial construct meant to house programmable minds
  * Cannot wear armor
    * Has natural armor equivalent to iron
    * May allow upgraded parts to improve stats
  * Does not naturally regenerate (use the regen zenith to heal)

## Minor Features
* Cognitive Inducer
  * New Model?
    * Pedestal
    * If something sided, can change the search area to be projected out from the direction it points
* Advancements
  * Use advancements to guide progression through the mod
  * Only if there's enough progression to warrant it

## Technical
* Configuration
  * Deny List for spells accessible to Thought Sieve
* Change Mind Fatigue to no longer be an effect
  * Store the expiration time in the villager's NBT
  * Hook the potion particle code to produce particles when mind fatigue is active
