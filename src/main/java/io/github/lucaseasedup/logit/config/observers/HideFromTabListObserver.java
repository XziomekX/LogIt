package io.github.lucaseasedup.logit.config.observers;

import io.github.lucaseasedup.logit.config.Property;
import io.github.lucaseasedup.logit.config.PropertyObserver;

public final class HideFromTabListObserver extends PropertyObserver
{
    @Override
    public void update(Property p)
    {
        getCore().getTabListUpdater().updateAllTabLists();
    }
}
