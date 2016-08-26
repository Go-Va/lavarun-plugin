package me.gong.lavarun.plugin.timer;

import me.gong.lavarun.plugin.InManager;
import me.gong.lavarun.plugin.Main;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author WesJD https://github.com/WesJD
 * @modifier TheMrGong
 */
public class Timers {

    private boolean enabled = false;

    private List<PooledTimer> pool;

    public void onEnable() {
        this.enabled = true;
        this.pool = new ArrayList<>();
    }

    public void onDisable() {
        this.enabled = false;
        this.pool = null;
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

    public void addToPool(TimerObject object) {
        if(pool == null) return;
        PooledTimer t = pool.stream().filter(p -> p.runsEvery == object.data.runEvery() &&
                (object.data.millisTime() ? p instanceof PooledThreadTimer : p instanceof PooledBukkitTimer)).findFirst().orElse(null);
        if(t == null){
            t = object.data.millisTime() ? new PooledThreadTimer(object.data.runEvery()) : new PooledBukkitTimer(object.data.runEvery());
            pool.add(t);
        }
        t.runnables.add(object);
    }

    public void removeFromPool(TimerObject object) {
        if(pool == null) return;
        PooledTimer t = pool.stream().filter(p -> p.runsEvery == object.data.runEvery() &&
                (object.data.millisTime() ? p instanceof PooledThreadTimer : p instanceof PooledBukkitTimer)).findFirst().orElse(null);
        if(t != null) {
            TimerObject obj = t.runnables.stream().filter(o -> o.equals(object)).findFirst().orElse(null);
            if(obj != null) t.runnables.remove(obj);
        }
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

            if(data.pool()) {
                addToPool(this);
            } else {

                if (!data.millisTime()) {
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
                                    if (!enabled || !running) {
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
        }

        private void callMethod() {
            try {
                called.invoke(object);
            } catch (Exception e) {

                throw new TimerException(e, "Exception running timer in class "+object.getClass().getName()+" ("+e.getStackTrace()[1].getLineNumber()+")");
            }
        }

        public boolean isRunning() {
            return running;
        }

        public void setRunning(boolean running) {
            this.running = running;
            if(data.pool()) removeFromPool(this);
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

    private static class PooledTimer {
        private long runsEvery;
        protected Callable<Boolean> callAll;
        protected List<TimerObject> runnables;

        public PooledTimer(long runsEvery) {
            this.runsEvery = runsEvery;
            this.runnables = new CopyOnWriteArrayList<>();
            this.callAll = () -> {
                Timers tm = InManager.get().getInstance(Timers.class);
                if(tm == null || !tm.enabled) return false;
                try {
                    runnables.stream().filter(t -> t.running).forEach(l -> {
                        //long init = System.currentTimeMillis();
                        l.callMethod();

                        /*if(System.currentTimeMillis() - init > 1) System.out.println("Took ["+getClass().getSimpleName()+" ("+runsEvery+") "+
                                l.called.getName()+" "+l.object.getClass().getSimpleName()+"]: "+(System.currentTimeMillis() - init));*/
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return true;
            };
        }
    }

    private class PooledBukkitTimer extends PooledTimer {
        private BukkitTask runner;

        public PooledBukkitTimer(long runsEvery) {
            super(runsEvery);
            this.runner = new BukkitRunnable() {

                @Override
                public void run() {
                    try {
                        if(!callAll.call()) cancel();
                    } catch (Exception e) {
                        e.printStackTrace();
                        cancel();
                    }
                }
            }.runTaskTimer(InManager.get().getInstance(Main.class), 0, runsEvery);
        }
    }

    private class PooledThreadTimer extends PooledTimer {

        private Thread runner;

        public PooledThreadTimer(long runsEvery) {
            super(runsEvery);
            this.runner = new Thread() {
                @Override
                public void run() {
                    new java.util.Timer().scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            try {
                                if(!callAll.call()) cancel();
                            } catch (Exception e) {
                                e.printStackTrace();
                                cancel();
                            }
                        }
                    }, 0, runsEvery);
                }
            };
            runner.start();
        }
    }

}