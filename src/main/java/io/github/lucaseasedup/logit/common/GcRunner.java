package io.github.lucaseasedup.logit.common;

import io.github.lucaseasedup.logit.LogItCore;
import java.lang.management.ManagementFactory;
import java.util.logging.Level;
import org.bukkit.scheduler.BukkitRunnable;

public final class GcRunner extends BukkitRunnable
{
    @Override
    public void run()
    {
        LogItCore.getInstance().log(Level.FINE, "Garbage collection requested.");
        LogItCore.getInstance().log(Level.FINE, " Heap before: "
                + ManagementFactory.getMemoryMXBean().getHeapMemoryUsage());
        LogItCore.getInstance().log(Level.FINE, " Non-heap before: "
                + ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage());
        LogItCore.getInstance().log(Level.FINE, "Running GC...");
        
        System.gc();
        
        LogItCore.getInstance().log(Level.FINE, "GC finished.");
        LogItCore.getInstance().log(Level.FINE, " Heap after: "
                + ManagementFactory.getMemoryMXBean().getHeapMemoryUsage());
        LogItCore.getInstance().log(Level.FINE, " Non-heap after: "
                + ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage());
    }
}
