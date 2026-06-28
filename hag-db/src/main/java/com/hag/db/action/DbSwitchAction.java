package com.hag.db.action;

import com.hag.core.context.ExecutionContext;
import com.hag.core.db.DbClientRegistry;
import com.hag.core.dispatcher.descriptor.ActionDescriptor;
import com.hag.core.executor.Action;
import com.hag.core.executor.ActionCategory;
import com.hag.core.model.Step;
import com.hag.core.result.ExecutionResult;

/**
 * DB_SWITCH action — switches the active DbClient in the registry to the named profile.
 *
 * <h3>CSV Syntax</h3>
 * <pre>
 *   DB_SWITCH,,,orders_db     → switch active connection to "orders_db"
 *   DB_SWITCH,,,default       → switch back to the default connection
 * </pre>
 *
 * <ul>
 *   <li><b>Recipient</b> — the profile name registered in {@code testdata.config.yml}
 *       under {@code databases:} (e.g. {@code orders_db})</li>
 * </ul>
 *
 * Fails gracefully with a descriptive error if the named profile is not registered.
 * Does NOT impact currently running queries on other threads.
 */
public final class DbSwitchAction implements Action {

    @Override
    public String name() { return "DB_SWITCH"; }

    @Override
    public ActionCategory category() { return ActionCategory.DB; }

    @Override
    public ExecutionResult execute(Step step, ActionDescriptor descriptor, ExecutionContext context) {

        DbClientRegistry registry = context.getDbClientRegistry();
        if (registry == null || !registry.hasClients()) {
            return ExecutionResult.failure("DB_SWITCH — no DbClientRegistry available in ExecutionContext");
        }

        String profileName = step.getRecipient();
        if (profileName == null || profileName.isBlank()) {
            return ExecutionResult.failure("DB_SWITCH requires the target profile name in the Recipient column");
        }

        // resolve any ${...} variables in the profile name
        Object resolved = context.resolveValue(profileName);
        String name = resolved == null ? profileName : resolved.toString().trim();

        try {
            registry.setActive(name);
            return ExecutionResult.success("DB_SWITCH → active connection is now '" + name + "'");
        } catch (IllegalArgumentException e) {
            return ExecutionResult.failure(e.getMessage());
        }
    }
}
