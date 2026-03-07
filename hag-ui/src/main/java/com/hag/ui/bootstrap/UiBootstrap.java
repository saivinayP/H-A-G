package com.hag.ui.bootstrap;

import com.hag.core.dispatcher.ActionRegistry;

// ── Navigation ───────────────────────────────
import com.hag.ui.action.navigation.NavigateAction;

// ── Mouse interactions ───────────────────────
import com.hag.ui.action.click.ClickAction;
import com.hag.ui.action.hover.HoverAction;
import com.hag.ui.action.drag.DragDropAction;

// ── Form controls ────────────────────────────
import com.hag.ui.action.input.InputTextAction;
import com.hag.ui.action.select.SelectAction;
import com.hag.ui.action.clear.ClearFieldAction;

// ── Page interactions ────────────────────────
import com.hag.ui.action.scroll.ScrollAction;

// ── Waits ────────────────────────────────────
import com.hag.ui.action.wait.WaitAction;

// ── Assertions — Text & Counts ───────────────
import com.hag.ui.action.assertion.AssertTextAction;
import com.hag.ui.action.assertion.AssertElementCountAction;
import com.hag.ui.action.assertion.AssertAttributeAction;

// ── Assertions — Visibility & State ─────────
import com.hag.ui.action.assertion.AssertVisibleAction;
import com.hag.ui.action.assertion.AssertHiddenAction;
import com.hag.ui.action.assertion.AssertEnabledAction;
import com.hag.ui.action.assertion.AssertDisabledAction;
import com.hag.ui.action.assertion.AssertSelectedAction;

// ── Frame & Window ───────────────────────────
import com.hag.ui.action.frame.SwitchFrameAction;
import com.hag.ui.action.window.SwitchWindowAction;

// ── JavaScript ───────────────────────────────
import com.hag.ui.action.js.JsClickAction;
import com.hag.ui.action.js.JsExecuteAction;

// ── Data extraction ──────────────────────────
import com.hag.ui.action.store.GetTextAction;

/**
 * UiBootstrap
 *
 * <p>Registers all Selenium-based UI actions into the {@link ActionRegistry}.
 * Total: 23 actions (18 original + 5 new state-assertion actions from Phase 4).
 */
public final class UiBootstrap {

    private UiBootstrap() {}

    public static void registerUiActions(ActionRegistry registry) {

        // ── Navigation ───────────────────────────────────────────────────
        registry.register(new NavigateAction());           // NAVIGATE / NAVIGATE:BACK / :FORWARD / :REFRESH

        // ── Mouse interactions ───────────────────────────────────────────
        registry.register(new ClickAction());              // CLICK / CLICK:DOUBLE / :RIGHT / :HOLD
        registry.register(new HoverAction());              // HOVER
        registry.register(new DragDropAction());           // DRAG_DROP

        // ── Form controls ────────────────────────────────────────────────
        registry.register(new InputTextAction());          // INPUT / INPUT:KEY / INPUT:FILE
        registry.register(new SelectAction());             // SELECT / SELECT:TEXT / SELECT:INDEX
        registry.register(new ClearFieldAction());         // CLEAR

        // ── Page interactions ────────────────────────────────────────────
        registry.register(new ScrollAction());             // SCROLL / SCROLL:TOP / SCROLL:BOTTOM

        // ── Waits ────────────────────────────────────────────────────────
        registry.register(new WaitAction());               // WAIT:VISIBLE / :INVISIBLE / :PRESENCE / :CLICKABLE / :TEXT

        // ── Assertions — Text ────────────────────────────────────────────
        registry.register(new AssertTextAction());         // ASSERT_TEXT / ASSERT_TEXT:CONTAINS / :REGEX
        registry.register(new AssertElementCountAction()); // ASSERT_COUNT / ASSERT_COUNT:AT_LEAST
        registry.register(new AssertAttributeAction());    // ASSERT_ATTRIBUTE / ASSERT_ATTRIBUTE:CONTAINS

        // ── Assertions — Visibility & Element State ──────────────────────
        registry.register(new AssertVisibleAction());      // ASSERT_VISIBLE
        registry.register(new AssertHiddenAction());       // ASSERT_HIDDEN
        registry.register(new AssertEnabledAction());      // ASSERT_ENABLED
        registry.register(new AssertDisabledAction());     // ASSERT_DISABLED
        registry.register(new AssertSelectedAction());     // ASSERT_SELECTED

        // ── Frame & Window ───────────────────────────────────────────────
        registry.register(new SwitchFrameAction());        // SWITCH_FRAME / SWITCH_FRAME:DEFAULT / :PARENT
        registry.register(new SwitchWindowAction());       // SWITCH_WINDOW:NEW / :TITLE / :CLOSE

        // ── JavaScript ───────────────────────────────────────────────────
        registry.register(new JsClickAction());            // JS_CLICK
        registry.register(new JsExecuteAction());          // JS_EXECUTE / JS_EXECUTE:STORE

        // ── Data extraction ──────────────────────────────────────────────
        registry.register(new GetTextAction());            // STORE_DATA (UI alias: GET_TEXT)
    }
}