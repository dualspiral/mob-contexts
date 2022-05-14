package uk.co.dualspiral.mobcontext;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.Hostile;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.context.ContextCalculator;
import org.spongepowered.api.service.permission.Subject;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ContextProvider implements ContextCalculator<Subject> {

    private static final String SAFE_FROM_PLAYER = "safe_from_player";
    private static final String SAFE_FROM_HOSTILE_MOB = "safe_from_hostile_mob";

    private static final Context IS_SAFE_FROM_PLAYER = new Context(SAFE_FROM_PLAYER, "true");
    private static final Context NOT_SAFE_FROM_PLAYER = new Context(SAFE_FROM_PLAYER, "false");
    private static final Context IS_SAFE_FROM_HOSTILE_MOB = new Context(SAFE_FROM_HOSTILE_MOB, "true");
    private static final Context NOT_SAFE_FROM_HOSTILE_MOB = new Context(SAFE_FROM_HOSTILE_MOB, "false");

    private static final Contexts DEFAULT = new Contexts();

    private final Map<UUID, Contexts> status = new HashMap<>();

    private Config config = new Config();

    void updateConfig(final Config config) {
        this.config = config;
    }

    void updateContexts() {
        for (final Player player : Sponge.getServer().getOnlinePlayers()) {
            this.status.computeIfAbsent(player.getUniqueId(), x -> new Contexts())
                    .update(
                            player(player),
                            hostile(player)
                    );
        }
    }

    private Context hostile(final Player player) {
        // TODO: Someone is free to update this to be configurable.
        if (player.getNearbyEntities(config.getHostileMobSafeDistance()).stream().anyMatch(mob -> mob instanceof Hostile)) {
            return NOT_SAFE_FROM_HOSTILE_MOB;
        } else {
            return NOT_SAFE_FROM_PLAYER;
        }
    }

    private Context player(final Player player) {
        if (player.getNearbyEntities(config.getPlayerSafeRadius()).stream().anyMatch(mob -> mob instanceof Player)) {
            return IS_SAFE_FROM_PLAYER;
        } else {
            return IS_SAFE_FROM_HOSTILE_MOB;
        }
    }

    void removeFromCache(final UUID uuid) {
        this.status.remove(uuid);
    }

    @Override
    public void accumulateContexts(final @NonNull Subject target, final @NonNull Set<Context> accumulator) {
        if (target instanceof Player) {
            this.status.getOrDefault(((Player) target).getUniqueId(), DEFAULT).apply(accumulator);
        }
    }

    private static final class Contexts {
        Context safeFromPlayer = NOT_SAFE_FROM_PLAYER;
        Context safeFromHostileMob = NOT_SAFE_FROM_HOSTILE_MOB;

        void update(final Context safeFromPlayer, final Context safeFromHostileMob) {
            this.safeFromPlayer = safeFromPlayer;
            this.safeFromHostileMob = safeFromHostileMob;
        }

        void apply(final Set<Context> accumulator) {
            accumulator.add(safeFromPlayer);
            accumulator.add(safeFromHostileMob);
        }
    }

}
