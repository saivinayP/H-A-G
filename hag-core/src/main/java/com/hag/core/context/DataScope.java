package com.hag.core.context;

/**
 * @deprecated As of CSV DSL v3, all scoped variables are replaced by a single flat global namespace.
 * Do not use. Preserved temporarily for compilation compatibility during transition.
 */
@Deprecated
public enum DataScope {
    UI, API, DB, GLOBAL
}
