package com.hag.ui.bootstrap;

import com.hag.core.dispatcher.ActionRegistry;

// Existing actions
import com.hag.ui.action.click.ClickAction;
import com.hag.ui.action.input.InputTextAction;
import com.hag.ui.action.navigation.NavigateAction;
import com.hag.ui.action.wait.WaitAction;

// New — Form controls
import com.hag.ui.action.select.SelectAction;
import com.hag.ui.action.clear.ClearFieldAction;

// New — Mouse interactions
import com.hag.ui.action.hover.HoverAction;
import com.hag.ui.action.drag.DragDropAction;

// New — Page interactions
import com.hag.ui.action.scroll.ScrollAction;

// New — Assertions
import com.hag.ui.action.assertion.AssertTextAction;
import com.hag.ui.action.assertion.AssertVisibleAction;
import com.hag.ui.action.assertion.AssertElementCountAction;
import com.hag.ui.action.assertion.AssertAttributeAction;

// New — Frame & Window
import com.hag.ui.action.frame.SwitchFrameAction;
import com.hag.ui.action.window.SwitchWindowAction;

// New — JavaScript
import com.hag.ui.action.js.JsClickAction;
import com.hag.ui.action.js.JsExecuteAction;

// New — Data extraction
import com.hag.ui.action.store.GetTextAction;

public final class UiBootstrap {

    private UiBootstrap() {}

    public static void registerUiActions(
            ActionRegistry registry
    ) {

        // ── Navigation ─────────────────────────────────
        registry.register(new NavigateAction());

        // ── Mouse interactions ──────────────────────────
        registry.register(new ClickAction());
        registry.register(new HoverAction());
        registry.register(new DragDropAction());

        // ── Form controls ───────────────────────────────
        registry.register(new InputTextAction());
        registry.register(new SelectAction());
        registry.register(new ClearFieldAction());

        // ── Page interactions ───────────────────────────
        registry.register(new ScrollAction());

        // ── Waits ───────────────────────────────────────
        registry.register(new WaitAction());

        // ── Assertions ──────────────────────────────────
        registry.register(new AssertTextAction());
        registry.register(new AssertVisibleAction());
        registry.register(new AssertElementCountAction());
        registry.register(new AssertAttributeAction());

        // ── Frame & Window ──────────────────────────────
        registry.register(new SwitchFrameAction());
        registry.register(new SwitchWindowAction());

        // ── JavaScript ──────────────────────────────────
        registry.register(new JsClickAction());
        registry.register(new JsExecuteAction());

        // ── Data extraction ─────────────────────────────
        registry.register(new GetTextAction());
    }
}