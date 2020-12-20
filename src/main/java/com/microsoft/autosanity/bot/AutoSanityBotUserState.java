// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.autosanity.bot;

/**
 * This is the welcome state for this sample.
 *
 * <p>
 * NOTE: Standard Java getters/setters must be used for properties.
 * Alternatively, the Jackson JSON annotations could be used instead. If any
 * methods start with "get" but aren't a property, the Jackson JSON 'JsonIgnore'
 * annotation must be used.
 * </p>
 *
 * @see AutoSanityBot
 */
public class AutoSanityBotUserState {
    private boolean didBotWelcomeUser;

    public boolean getDidBotWelcomeUser() {
        return didBotWelcomeUser;
    }

    public void setDidBotWelcomeUser(boolean withDidWelcomUser) {
        didBotWelcomeUser = withDidWelcomUser;
    }
}
