package com.hag.ui.action;

import com.hag.core.executor.Action;
import com.hag.core.executor.ActionCategory;

public interface UiAction extends Action {

    @Override
    default ActionCategory category() {
        return ActionCategory.UI;
    }
}