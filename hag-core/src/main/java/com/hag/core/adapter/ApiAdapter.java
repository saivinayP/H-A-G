package com.hag.core.adapter;

/**
 * Marker interface for API capability provider.
 */
public interface ApiAdapter {

    /**
     * Executes a request object and returns raw response.
     * Concrete implementation defines request/response type.
     */
    Object execute(Object request);
}