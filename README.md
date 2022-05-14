MobContext
==========

A simple plugin for SpongeAPI 7 (at the moment) that adds the permission contexts `safe_from_player` and `safe_from_hostile_mob`. They will be added to all players, and either set to `true` or `false`. You can use these to set permissions based on whether a player is safe from potentially hostile entities.

To attempt to save on resources, the contexts are cached and updated once a second. To guard against players trying to disconnect and reconnect, the contexts are defaulted to false when a player connects.

If you are using LuckPerms, [see their wiki page on contexts for more info on how to use these contexts](https://luckperms.net/wiki/Context).

Config
------

There are two config options - `player-safe-radius` and `hostile-mob-safe-radius` - radius is in blocks.