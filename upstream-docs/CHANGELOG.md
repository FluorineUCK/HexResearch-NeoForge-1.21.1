= Changelog =

== 1.0.1 ==

* Updated zh_ch - ChuijkYahus
* Updated dependencies

== 1.0.0 ==

* Cognitive Inducer
  * No longer able to continuously farm minds from a single villager
    * Impression marks are retained for one hour (configurable)
    * If a villager has as many marks as is required for their own level of mind (as per config), they no longer contribute.
  * Added comparator support (Rank of induced mind)
* Refactored block registration to use the more standard style
* Refactored out block entity access into its own class
* Added version to mod initialization log message

== 0.1.3 ==

* Implemented Configuration
* Marked HexGloop as an optional dependency in mod metadata
* Updated zh_ch - ChuijkYahus

== 0.1.2 ==

* Cognitive Inducer
  * Allow right-clicking media to fill the inducer
* HexGloop Interop
  * Imbue Mind now works with the HexGloop Flayer's Table
* Bugfixes
  * Cognitive Inducer may now spend all of its stored media
  * Mishaps for failed Mind Imbue casts now use the correct localization key
  * Fixed use of ClientLevel.getRandom() on the network thread

== 0.1.1 ==

* Bugfixes
  * Cognitive Inducers now drop when mined
  * Fixed access to client classes in dedicated server
  * Fixed Cognitive Inducer not consuming media on Forge

== 0.1.0 ==

* Added Vegan Mind Flaying
  * Added Block: Cognitive Inducer
  * Added Great Spell: Imbue Mind
    * If adding this to an existing world, run /hexcasting recalcPatterns to generate the per-world pattern entry

== 0.0.6 ==

* Documented Mind Fatigue
* Added Mixin Extras  
  https://github.com/LlamaLad7/MixinExtras

== 0.0.5 ==

* Fixed a bug where closed patterns (those that start and end at the same point) would fail to match unless the exact
  same position was chosen in both patterns.

== 0.0.4 ==

* Updated zh_ch - ChuijkYahus

== 0.0.3 ==

* Fixed Mind Fatigue being removable with milk.

== 0.0.2 ==

* Simplified Chinese Translation - ChuijkYahus

== 0.0.1 ==

* Initial Release
