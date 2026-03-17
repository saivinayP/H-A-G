package com.hag.core.db;

import com.hag.core.adapter.DbClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * DbClientRegistry — manages a named set of {@link DbClient} instances and
 * tracks the currently active client for a test thread.
 *
 * <h3>Design</h3>
 * <ul>
 *   <li>Clients are registered by name at suite startup (from {@code testdata.config.yml}).</li>
 *   <li>The first registered client automatically becomes the active one under the name
 *       {@code "default"}.</li>
 *   <li>The active client is switched mid-test via the {@code DB_SWITCH} CSV action.</li>
 *   <li>All registered clients are closed together in suite teardown via {@link #closeAll()}.</li>
 * </ul>
 *
 * <h3>Thread-safety</h3>
 * The registry map itself is immutable after suite setup (all registrations happen before
 * any test thread runs). The active-client pointer is per-instance and should be kept in
 * a {@link ThreadLocal} by the caller (see {@code HagTestBase}) when running in parallel.
 */
public class DbClientRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(DbClientRegistry.class);

    public static final String DEFAULT_NAME = "default";

    /** Ordered map so the first registered entry is reliably the default. */
    private final Map<String, DbClient> clients = new LinkedHashMap<>();

    private String activeName;

    // ── Registration ──────────────────────────────────────────────────────

    /**
     * Registers a {@link DbClient} under the given name.
     *
     * <p>The first client registered automatically becomes active.
     *
     * @param name   logical profile name (e.g. {@code "default"}, {@code "orders_db"})
     * @param client the client to register — must not be {@code null}
     * @throws IllegalArgumentException if {@code name} is blank or {@code client} is null
     * @throws IllegalStateException    if a client with the same name was already registered
     */
    public synchronized void register(String name, DbClient client) {
        Objects.requireNonNull(name,   "DB client name must not be null");
        Objects.requireNonNull(client, "DB client must not be null");
        if (name.isBlank()) throw new IllegalArgumentException("DB client name must not be blank");
        if (clients.containsKey(name)) {
            throw new IllegalStateException("A DB client named '" + name + "' is already registered");
        }
        clients.put(name, client);
        if (activeName == null) {
            activeName = name;   // first registered → active
        }
        LOG.info("HAG → DB → Registered client '{}'{}", name, name.equals(activeName) ? " (active)" : "");
    }

    // ── Active client ─────────────────────────────────────────────────────

    /**
     * Returns the currently active {@link DbClient}.
     *
     * @return the active client, or {@code null} if no clients are registered
     */
    public DbClient getActive() {
        if (activeName == null) return null;
        return clients.get(activeName);
    }

    /**
     * Returns the name of the currently active client.
     */
    public String getActiveName() {
        return activeName;
    }

    /**
     * Switches the active client to the one registered under {@code name}.
     *
     * @param name the profile name to switch to
     * @throws IllegalArgumentException if {@code name} is not registered
     */
    public void setActive(String name) {
        if (!clients.containsKey(name)) {
            throw new IllegalArgumentException(
                "DB_SWITCH failed — no DB client registered with name '" + name + "'. "
                + "Registered names: " + clients.keySet());
        }
        this.activeName = name;
        LOG.info("HAG → DB → Active client switched to '{}'", name);
    }

    // ── Named access ──────────────────────────────────────────────────────

    /**
     * Returns the named client, or {@code null} if not registered.
     *
     * @param name profile name
     */
    public DbClient get(String name) {
        return clients.get(name);
    }

    /**
     * Returns an unmodifiable view of all registered clients.
     */
    public Map<String, DbClient> all() {
        return Collections.unmodifiableMap(clients);
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────

    /**
     * Closes all registered clients.
     * Errors from individual closes are logged and suppressed so all clients are attempted.
     */
    public void closeAll() {
        for (Map.Entry<String, DbClient> entry : clients.entrySet()) {
            try {
                entry.getValue().close();
                LOG.debug("HAG → DB → Client '{}' closed", entry.getKey());
            } catch (Exception e) {
                LOG.warn("HAG → DB → Error closing client '{}': {}", entry.getKey(), e.getMessage());
            }
        }
        LOG.info("HAG → DB → All DB clients closed");
    }

    /**
     * @return {@code true} when at least one client is registered
     */
    public boolean hasClients() {
        return !clients.isEmpty();
    }
}
