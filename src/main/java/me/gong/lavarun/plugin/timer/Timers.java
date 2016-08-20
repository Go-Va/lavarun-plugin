package me.gong.lavarun.plugin.timer;

import me.gong.lavarun.plugin.InManager;
import me.gong.lavarun.plugin.Main;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Method;
import java.util.*;

/**
 * @author WesJD https://github.com/WesJD
 */
public class Timers {

    private boolean enabled = false;

    public void onEnable() {
        this.enabled = true;
    }

    public void onDisable() {
        this.enabled = false;
    }

    public List<TimerObject> createTimers(Object b) {
        List<TimerObject> ret = new ArrayList<>();
        Arrays.stream(b.getClass().getMethods())
                .filter(m -> m.isAnnotationPresent(Timer.class) && m.getParameterTypes().length == 0)
                .forEach(m -> {
                    m.setAccessible(true);
                    ret.add(new TimerObject(b, m));
                });
        return ret;
    }

    public void unregister(List<TimerObject> t) {
        t.forEach(d -> d.setRunning(false));
    }

    public static List<TimerObject> register(Object b) {
        return InManager.get().getInstance(Timers.class).createTimers(b);
    }

    public class TimerObject {
        private Method called;
        private Timer data;
        private Object object;
        private boolean running = true;

        public TimerObject(Object ob, Method m) {
            this.called = m;
            this.object = ob;
            this.data = m.getAnnotation(Timer.class);

            if(!data.millisTime()) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (!enabled || !running) {
                            cancel();
                            return;
                        }
                        callMethod();
                    }
                }.runTaskTimer(InManager.get().getInstance(Main.class), 0, data.runEvery());
            } else {
                new Thread() {
                    public void run() {
                        new java.util.Timer().scheduleAtFixedRate(new TimerTask() {
                            @Override
                            public void run() {
                                if(!enabled || !running) {
                                    cancel();
                                    return;
                                }
                                callMethod();
                            }
                        }, 0, data.runEvery());
                    }
                }.start();
            }
        }

        private void callMethod() {
            try {
                called.invoke(object);
            } catch (Exception e) {
                throw new TimerException(e, "Exception running timer in class "+object.getClass().getName());
            }
        }

        public boolean isRunning() {
            return running;
        }

        public void setRunning(boolean running) {
            this.running = running;
        }
    }
    
    private static class TimerException extends RuntimeException {
        private final Throwable cause;

        public TimerException(Throwable throwable) {
            this.cause = throwable;
        }

        public TimerException() {
            this.cause = null;
        }

        public TimerException(Throwable cause, String message) {
            super(message);
            this.cause = cause;
        }

        public TimerException(String message) {
            super(message);
            this.cause = null;
        }

        public Throwable getCause() {
            return this.cause;
        }
    }

}