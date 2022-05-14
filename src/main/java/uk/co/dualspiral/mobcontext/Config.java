package uk.co.dualspiral.mobcontext;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public final class Config {

    @Setting(value = "player-safe-radius")
    private int playerSafeRadius = 20;

    @Setting(value = "hostile-mob-safe-radius")
    private int hostileMobSafeDistance = 20;

    public int getPlayerSafeRadius() {
        return this.playerSafeRadius;
    }

    public int getHostileMobSafeDistance() {
        return this.hostileMobSafeDistance;
    }
}
