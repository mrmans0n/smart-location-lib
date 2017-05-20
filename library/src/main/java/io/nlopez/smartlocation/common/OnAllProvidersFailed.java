package io.nlopez.smartlocation.common;

/**
 * Listener for when all the providers in a controller have failed to initialize.
 */
public interface OnAllProvidersFailed {
    void onAllProvidersFailed();

    OnAllProvidersFailed EMPTY = new OnAllProvidersFailed() {
        @Override
        public void onAllProvidersFailed() {
            // no-op
        }
    };
}
