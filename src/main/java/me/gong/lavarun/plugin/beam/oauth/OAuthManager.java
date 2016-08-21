package me.gong.lavarun.plugin.beam.oauth;

import me.gong.lavarun.plugin.InManager;
import me.gong.lavarun.plugin.Main;
import me.gong.lavarun.plugin.beam.BeamManager;
import me.gong.lavarun.plugin.timer.Timer;
import me.gong.lavarun.plugin.timer.Timers;

import java.io.IOException;
import java.util.List;

public class OAuthManager {

    private static final long WAIT_TIMEOUT = 5000;

    private OAuthListener listener;
    private AuthenticationWaiting waiting;
    private List<Timers.TimerObject> timers;

    public void onEnable(Main.Config config) {
        listener = new OAuthListener(config.getPort());
        timers = Timers.register(this);
    }

    public void onDisable() {
        try {
            listener.stop();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        waiting = null;
        InManager.get().getInstance(Timers.class).unregister(timers);
    }

    public void onResponse(AuthResponse response) {
        if(waiting != null) {
            if(!response.compareAgainst(waiting)) response.informInvalid();
            else InManager.get().getInstance(BeamManager.class).useConfiguration(new BeamManager.BeamConfiguration(response));
        }
    }

    public void checkAndRemoveOutdated() {
        if(waiting != null &&  waiting.getTimeSinceInitialization() >= WAIT_TIMEOUT) {
            waiting.informRemoved();
            waiting = null;
        }
    }

    public AuthenticationWaiting beginWaiting(String player) {
        return (waiting = new AuthenticationWaiting(player));
    }

    public AuthenticationWaiting getWaiting() {
        return waiting;
    }

    @Timer(runEvery = 1)
    public void checkForInvalids() {
        checkAndRemoveOutdated();
    }
}
