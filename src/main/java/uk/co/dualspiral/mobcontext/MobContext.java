package uk.co.dualspiral.mobcontext;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.event.service.ChangeServiceProviderEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.permission.PermissionService;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Plugin(
        id = "mob-context",
        name = "Mob Context",
        version = "1.0.0-SNAPSHOT",
        description = "Permission contexts for hostile entities"
)
public class MobContext {

    private final Logger logger;
    private final PluginContainer pluginContainer;
    private final ConfigurationLoader<CommentedConfigurationNode> configurationLoader;
    private final ContextProvider contextProvider = new ContextProvider();

    private boolean isRegistered = false;

    @Inject
    public MobContext(final Logger logger,
                      final PluginContainer pluginContainer,
                      final @DefaultConfig(sharedRoot = false) ConfigurationLoader<CommentedConfigurationNode> configurationLoader) {
        this.logger = logger;
        this.pluginContainer = pluginContainer;
        this.configurationLoader = configurationLoader;
    }

    @Listener
    public void onInit(final GamePreInitializationEvent event) {
        try {
            final ConfigurationNode node = this.configurationLoader.load();
            node.mergeValuesFrom(configurationLoader.createEmptyNode().setValue(TypeToken.of(Config.class), new Config()));
            this.configurationLoader.save(node);
        } catch (final IOException | ObjectMappingException e) {
            this.logger.error("Could not load mob-context config: {}", e.getMessage(), e);
        }
    }

    @Listener
    public void onServiceProvision(final ChangeServiceProviderEvent event) {
        if (event.getService().equals(PermissionService.class)) {
            ((PermissionService) event.getNewProvider()).registerContextCalculator(contextProvider);
            this.isRegistered = true;
        }
    }

    @Listener
    public void onServerStart(final GameStartedServerEvent event) {
        if (!this.isRegistered) {
            Sponge.getServiceManager().provideUnchecked(PermissionService.class).registerContextCalculator(contextProvider);
            this.isRegistered = true;
        }
        Sponge.getScheduler().createSyncExecutor(this.pluginContainer).scheduleAtFixedRate(contextProvider::updateContexts, 1, 1, TimeUnit.SECONDS);
    }

    @Listener
    public void onPlayerQuit(final ClientConnectionEvent.Disconnect event, @Getter("getTargetEntity") final Player player) {
        this.contextProvider.removeFromCache(player.getUniqueId());
    }

    @Listener
    public void onGameReload(final GameReloadEvent event) {
        this.loadConfig();
    }

    private void loadConfig() {
        try {
            this.contextProvider.updateConfig(this.configurationLoader.load().getValue(TypeToken.of(Config.class)));
        } catch (final Exception e) {
            this.logger.error("Could not load config: {}", e.getMessage(), e);
        }
    }
}
