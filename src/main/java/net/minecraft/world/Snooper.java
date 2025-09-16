package net.minecraft.world;

import com.google.common.collect.Maps;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Timer;
import java.util.UUID;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/Snooper.class */
public class Snooper {
    private final URL url;
    private final SnooperPopulator populator;
    private final long startupTime;
    private boolean started;
    private final Map<String, Object> fixedData = Maps.newHashMap();
    private final Map<String, Object> dynamicData = Maps.newHashMap();
    private final String token = UUID.randomUUID().toString();
    private final Timer timer = new Timer("Snooper Timer", true);
    private final Object lock = new Object();

    public Snooper(String str, SnooperPopulator snooperPopulator, long j) {
        try {
            this.url = new URL("http://snoop.minecraft.net/" + str + "?version=2");
            this.populator = snooperPopulator;
            this.startupTime = j;
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException();
        }
    }

    public void start() {
        if (!this.started) {
        }
    }

    public void prepare() {
        setFixedData("memory_total", Long.valueOf(Runtime.getRuntime().totalMemory()));
        setFixedData("memory_max", Long.valueOf(Runtime.getRuntime().maxMemory()));
        setFixedData("memory_free", Long.valueOf(Runtime.getRuntime().freeMemory()));
        setFixedData("cpu_cores", Integer.valueOf(Runtime.getRuntime().availableProcessors()));
        this.populator.populateSnooper(this);
    }

    public void setDynamicData(String str, Object obj) {
        synchronized (this.lock) {
            this.dynamicData.put(str, obj);
        }
    }

    public void setFixedData(String str, Object obj) {
        synchronized (this.lock) {
            this.fixedData.put(str, obj);
        }
    }

    public boolean isStarted() {
        return this.started;
    }

    public void interrupt() {
        this.timer.cancel();
    }

    public String getToken() {
        return this.token;
    }

    public long getStartupTime() {
        return this.startupTime;
    }
}
