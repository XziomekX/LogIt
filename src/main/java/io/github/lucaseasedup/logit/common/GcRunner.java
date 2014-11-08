package io.github.lucaseasedup.logit.common;

import org.bukkit.scheduler.BukkitRunnable;

public final class GcRunner extends BukkitRunnable
{
    @Override
    public void run()
    {
        System.gc();
    }
}
