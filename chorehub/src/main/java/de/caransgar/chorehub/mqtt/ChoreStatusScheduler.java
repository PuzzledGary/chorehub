package de.caransgar.chorehub.mqtt;

import de.caransgar.chorehub.entity.Chore;
import de.caransgar.chorehub.services.ChoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Scheduled task for periodically publishing MQTT status for all chores.
 * Ensures MQTT state is synchronized even if status changes are missed.
 */
@Component
public class ChoreStatusScheduler {

    private static final Logger LOG = LoggerFactory.getLogger(ChoreStatusScheduler.class);

    private final ChoreService choreService;
    private final ChoreStatePublisher statePublisher;

    public ChoreStatusScheduler(ChoreService choreService, ChoreStatePublisher statePublisher) {
        this.choreService = choreService;
        this.statePublisher = statePublisher;
    }

    /**
     * Periodically refresh MQTT status for all chores.
     * Runs every 5 minutes to ensure state stays in sync.
     */
    @Scheduled(fixedRate = 300000)  // 5 minutes
    public void refreshAllChoreStates() {
        try {
            List<Chore> allChores = choreService.getAllChores();
            if (allChores.isEmpty()) {
                LOG.debug("No chores to refresh");
                return;
            }

            LOG.debug("Refreshing MQTT status for {} chores", allChores.size());

            for (Chore chore : allChores) {
                try {
                    statePublisher.publishStatusAndAttributes(chore);
                } catch (Exception e) {
                    LOG.warn("Failed to refresh state for chore {}", chore.getId(), e);
                }
            }

            LOG.debug("MQTT status refresh completed");
        } catch (Exception e) {
            LOG.error("Error in scheduled chore status refresh", e);
        }
    }
}
