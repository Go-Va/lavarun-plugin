package me.gong.lavarun.plugin.game;

import me.gong.lavarun.plugin.InManager;
import me.gong.lavarun.plugin.Main;
import me.gong.lavarun.plugin.arena.Arena;
import me.gong.lavarun.plugin.arena.team.Team;
import me.gong.lavarun.plugin.command.CommandManager;
import me.gong.lavarun.plugin.game.events.GameBeginEvent;
import me.gong.lavarun.plugin.game.events.GameEndEvent;
import me.gong.lavarun.plugin.game.logic.GameEvents;
import me.gong.lavarun.plugin.game.logic.GameTicks;
import me.gong.lavarun.plugin.game.scoreboard.InGameBoard;
import me.gong.lavarun.plugin.game.scoreboard.LobbyBoard;
import me.gong.lavarun.plugin.powerup.PowerupManager;
import me.gong.lavarun.plugin.scoreboard.ScoreboardHandler;
import me.gong.lavarun.plugin.scoreboard.ScoreboardManager;
import me.gong.lavarun.plugin.shop.ShopManager;
import me.gong.lavarun.plugin.timer.Timers;
import me.gong.lavarun.plugin.util.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameManager implements Listener {

    public static final String[] DEATH = new String[] {"%player died to something called %death.",
            "%player learned that %death is bad.",
            "Ah, good old %death killed %player.",
            "Oh no.. %death! Not %death! Sorry %player.",
            "Heh, really? %death? You can do better than that %player.",
            "Have you died much to %death %player? I think you have.",
            "Oh boo hoo %player. Die to a player instead of %death.",
            "%death has a sassy message for %player.",
            "%player I don't think %death is very good for you.",
            "Hey %player, I hear if you die to %death 1,000,000 times you become immune.",
            "%player investigated %death and found out it hurts.",
            "Another death added to the tally for %player, by %death!",
            "Did you enjoy %death %player?",
            "%player, there are other ways to die other than %death.",
            "Are you proud of your accomplishment %player? Dying to %death is really hard.",
            "Embarrassing death is embarrassing. [%player rick-rolled by %death]",
            "Well shucks, %player died to %death.",
            "Rip. "+ Calendar.getInstance().get(Calendar.YEAR)+ " %player died to %death",
            "Hahahaha. +1 to %death, get your act together %player",
            "That was fun %player, I bet %death enjoyed it.",
            "%player, I'm not sassy, I'm a conversationalist. Also, watch out for %death next time.",
            "What's that, Sherlock? %death can hurt %player? I have no idea how you came to that conclusion.",
            "FRESH OUTTA DEATH LINES FOR %player",
            "%player isn't afraid of lava, seeing that he's already dead on the inside.",
            "%player, why don't you use some of that salt on your fries. You don't have to be blaming %death for everything.",
            "If at first you don't succeed %player, fail 5 more times trying to beat %death.",
            "If we removed %death, there wouldn't be a way to die %player.",
            "Did you think it would be funny dying to %death, %player?",
            "%player sometimes dreams about cheese and %death.",
            "After that...who knows? I might take up a hobby. Reanimating %player, maybe.(%death)"};

    public static final String[] BY_PLAYER = new String[] {"%victim was murdered gruesomely by %damager",
            "The savage %damager killed %victim", "%victim destroyed by %damager!", "Oh, oh OH! %victim SHREKT by %damager!",
            "%victim needs a break from %damager.", "%damager is not letting %victim have a good day",
            "%victim, at least a player (%damager) killed you. You could've died to lava.", "%victim, try not to get on %damager's bad side next time",
            "Try combating %damager with some powerups, %victim!",
            "No, %victim, the server did not lag while %damager killed you.",
            "Pushing others off makes you that guy %damager. Poor %victim didn't do anything wrong.",
            "If you just let %damager cap, you'll see fireworks at the end %victim!",
            "Make %damager rue the day it thought it could give %victim lemons! Do you know who I am? I'm the man who's going to burn your house down! With the lemons! I'm going to get my engineers to invent a combustible lemon that burns your house down.",
            "%damager, now please bring %victim some pain pills.",
            "%victim are you trying to test %damager?",
            "Well done. Here come the test results: You are a horrible person, %damager. I'm serious, that's what it says: You killer %victim. We weren't even testing for that",
            "You don't have to test with the %victim, %damager. It's garbage"};

    public static final long RESPAWN_TIME_PLAYER = 1000 * 10, RESPAWN_TIME_STUPID = 1000 * 4, ATTACK_CACHE_TIME = 1000 * 3;

    private List<Arena> arenas;
    private Arena currentArena;
    private boolean inGame, redReady, blueReady;

    public int redCaptureState, blueCaptureState, redCapAmount, blueCapAmount;
    private long lastRedCap, lastBlueCap;
    private File saveFile;
    private List<Material> foods = new ArrayList<>();
    private List<AttackData> attackData;
    private ScoreboardHandler inGameBoard, lobbyBoard;

    public GameManager() {
        inGameBoard = new InGameBoard();
        lobbyBoard = new LobbyBoard();
        attackData = new CopyOnWriteArrayList<>();
        foods.add(Material.COOKED_FISH);
        foods.add(Material.BREAD);
        foods.add(Material.APPLE);
        foods.add(Material.GOLDEN_CARROT);

        InManager.get().getInstance(CommandManager.class).addAllCommands(new GameCommands());
        arenas = new ArrayList<>();
        File dataFolder = InManager.get().getInstance(Main.class).getDataFolder();
        if(!dataFolder.exists()) dataFolder.mkdir();
        saveFile = new File(dataFolder, "arenas.dat");
        if(!saveFile.exists()) try {
            saveFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onEnable() {
        Timers.register(new GameTicks());
        Bukkit.getPluginManager().registerEvents(new GameEvents(), InManager.get().getInstance(Main.class));
        loadArenas();
    }

    public void onDisable() {
        if(inGame) stopGame((Team) null);

        try(BufferedWriter w = new BufferedWriter(new FileWriter(saveFile))) {
            for (Arena a : arenas) {
                w.write(a.toJSON().toString());
                w.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadArena(Arena arena) {
        currentArena = arena;
        resetAll();
        Bukkit.getOnlinePlayers().forEach(p -> {
            p.teleport(arena.getTeamChooseLocation());
            setupScoreboard(p);
        });
    }

    public List<Arena> getArenas() {
        return arenas;
    }

    public void addArena(Arena arena) {
        arenas.add(arena);
    }

    private void loadArenas() {
        arenas.clear();
        try(BufferedReader r = new BufferedReader(new FileReader(saveFile))) {
            String line;
            while((line = r.readLine()) != null) {
                if(!line.isEmpty()) arenas.add(Arena.fromJSON((JSONObject) JSONUtils.parser.parse(line)));
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    public void stopGame(Player player) {
        if(!inGame) player.sendMessage(ChatColor.GOLD+"No game is currently running.");
        else {
            stopGame((Team) null);
            Bukkit.broadcastMessage(ChatColor.RED.toString()+ChatColor.BOLD+"Game was forcefully stopped by "+ChatColor.YELLOW+player.getName());
        }
    }

    public void stopGame(Team winner) {
        if(inGame) {
            resetAll();
            Bukkit.getPluginManager().callEvent(new GameEndEvent());
            for (Player p : Bukkit.getOnlinePlayers()) {
                setupScoreboard(p);
                p.setGameMode(GameMode.SURVIVAL);
                p.setFoodLevel(20);
                setHunger(p, false);
                p.teleport(currentArena.getTeamChooseLocation());
                p.getInventory().clear();
                p.setHealth(p.getMaxHealth());
                p.setLevel(0);
            }
            InManager.get().getInstance(ShopManager.class).resetAllPlayers();
            if(winner != null) {
                Bukkit.broadcastMessage(winner.getColor()+winner.getName()+" has won the game!");
                currentArena.spawnFireworks(winner);
            }
            InManager.get().getInstance(PowerupManager.class).onGameEnd();
            new BukkitRunnable() {
                @Override
                public void run() {
                    Bukkit.getOnlinePlayers().forEach(p -> p.setFireTicks(0));
                }
            }.runTask(InManager.get().getInstance(Main.class));
        }
    }

    public Arena getCurrentArena() {
        return currentArena;
    }

    public boolean isInGame() {
        return inGame;
    }

    private void resetAll() {
        inGame = blueReady = redReady = false;
        redCaptureState = blueCaptureState = redCapAmount = blueCapAmount = 0;
        lastBlueCap = lastRedCap = 0;
        attackData.clear();
        currentArena.resetTeams();
        currentArena.resetArena(true, false);
    }

    public void beginGame() {
        Bukkit.broadcastMessage(ChatColor.YELLOW+"The game is now beginning!");
        inGame = true;
        Bukkit.getPluginManager().callEvent(new GameBeginEvent());
        blueReady = redReady = false;
        Bukkit.getOnlinePlayers().forEach(this::setupPlayer);
        currentArena.resetArena(true, true);
    }

    public void removeArena(Player player, Arena a) {
        if(a == currentArena && inGame) {
            player.sendMessage(ChatColor.RED+"Can't remove arena while it is in use");
            return;
        }
        if(a.equals(currentArena)) {
            ScoreboardManager m = InManager.get().getInstance(ScoreboardManager.class);
            Bukkit.getOnlinePlayers().forEach(s -> m.setScoreboard(s, null));
            currentArena = null;
        }
        arenas.remove(a);
        player.sendMessage(ChatColor.GOLD+"Arena '"+a.getName()+"' removed");
    }

    public void spawnPlayer(Player player, boolean resetHunger) {
        if(!currentArena.isPlaying(player, true)) return;
        if(player.getGameMode() != GameMode.SURVIVAL) player.setGameMode(GameMode.SURVIVAL);
        player.teleport(currentArena.getTeam(player).getSpawnLocation());

        if(resetHunger) {
            player.setHealth(player.getMaxHealth());
            player.setFoodLevel(20);
            setHunger(player, false);
        } else {
            player.setHealth(Math.max(player.getHealth(), player.getMaxHealth() / 2));
            setHunger(player, player.getFoodLevel() <= 7);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                player.setFireTicks(0);
            }
        }.runTaskLater(InManager.get().getInstance(Main.class), 2);
    }

    private void setupPlayer(Player player) {
        Block below = player.getLocation().add(0, -1, 0).getBlock();
        setupScoreboard(player);
        if(below.getType() == Material.STAINED_GLASS) {
            if(below.getData() == DyeColor.RED.getData()) {
                currentArena.getRed().addPlayer(player);
                currentArena.getTeam(player).giveKit(player);
                spawnPlayer(player, true);
                player.sendMessage(ChatColor.RED.toString()+ChatColor.BOLD+"You have joined red team.");
                return;
            } else if(below.getData() == DyeColor.LIGHT_BLUE.getData()) {
                currentArena.getBlue().addPlayer(player);
                currentArena.getTeam(player).giveKit(player);
                spawnPlayer(player, true);
                player.sendMessage(ChatColor.AQUA.toString()+ChatColor.BOLD+"You have joined blue team.");
                return;
            }
        }
        player.setGameMode(GameMode.SPECTATOR);
        player.sendMessage(ChatColor.GRAY+"You are spectating this game.");
    }

    public void setupScoreboard(Player player) {
        ScoreboardManager sc = InManager.get().getInstance(ScoreboardManager.class);
        if(!inGame && currentArena == null) sc.setScoreboard(player, null);
        else if(!inGame) sc.setScoreboard(player, lobbyBoard);
        else sc.setScoreboard(player, inGameBoard);
    }

    public void setHunger(Player player, boolean hunger) {
        BukkitUtils.Capabilities c = BukkitUtils.Capabilities.fromPlayer(player);
        if(hunger) c.walkSpeed = 100 * 100 *100f;
        else c.walkSpeed = 0.1f;
        BukkitUtils.sendCapabilities(player,  c);
    }

    public void handleDamage(Player victim) {
        AttackData d = attackData.stream().filter(e -> e.victim.equals(victim.getUniqueId())).findFirst().orElse(null);
        if(d != null) d.updateAttack();
    }

    public void handleAttack(Player victim, Player from) {
        if(from == null) return;
        AttackData d = attackData.stream().filter(e -> e.victim.equals(victim.getUniqueId())).findFirst().orElse(null);
        if(d != null) {
            d.updateAttack();
            d.damager = from.getUniqueId();
        }
        else {
            d = new AttackData(from, victim);
            attackData.add(d);
        }
    }

    public void handleKill(Player victim) {
        Player attacker = getLastAttacker(victim);
        removeDataFor(victim.getUniqueId());
        if(attacker != null) {
            String atS = currentArena.getTeam(attacker).getColor()+attacker.getName()+ChatColor.GREEN, vicS = currentArena.getTeam(victim).getColor()+victim.getName()+ChatColor.GREEN;
            Bukkit.broadcastMessage(ChatColor.GREEN+BY_PLAYER[NumberUtils.random.nextInt(BY_PLAYER.length)].replace("%victim", vicS).replace("%damager", atS));

        } else {
            EntityDamageEvent ev = victim.getLastDamageCause();
            String pl = victim.getName(), cause = ev.getCause().name().toLowerCase().replace("_", " ");
            Bukkit.broadcastMessage(ChatColor.GREEN + DEATH[NumberUtils.random.nextInt(GameManager.DEATH.length)]
                    .replace("%player", currentArena.getTeam(victim).getColor() + pl + ChatColor.GREEN).replace("%death", cause));
        }
        currentArena.getTeam(victim).addDamageCooldown(victim);
        victim.getWorld().spawnParticle(Particle.CRIT, victim.getLocation(), 75, 0.5, 1.2, 0.3, 0);

        for (Player pl : Bukkit.getOnlinePlayers()) {
            if (pl.getUniqueId().equals(victim.getUniqueId())) continue;
            pl.playSound(victim.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_DEATH, 2.0f, 2.0f);
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                handlePlayerDeath(victim, attacker != null);
                victim.playSound(victim.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_DEATH, 2.0f, 2.0f);
            }
        }.runTask(InManager.get().getInstance(Main.class));
    }

    public void invalidateAttackData() {
        attackData.stream().filter(a -> a.getTimeSinceLastHit() >= ATTACK_CACHE_TIME).forEach(attackData::remove);
    }

    public void removeDataFor(UUID player) {
        attackData.stream().filter(a -> a.victim.equals(player) || a.damager.equals(player)).forEach(attackData::remove);
    }

    public Player getLastAttacker(Player player) {
        AttackData d = attackData.stream().filter(e -> e.victim.equals(player.getUniqueId())).findFirst().orElse(null);
        return d == null ? null : d.getDamager();
    }

    public void handlePlayerDeath(Player player, boolean isPlayer) {
        GameManager gm = InManager.get().getInstance(GameManager.class);
        if(gm.isInGame()) {
            {
                Location pos1 = player.getLocation(), pos2 = pos1.add(0, 1, 0);
                int material = Material.STAINED_GLASS.getId(), data = gm.getCurrentArena().getTeam(player).getGlassColor();
                player.getWorld().playEffect(pos1, Effect.STEP_SOUND, material, data);
                player.getWorld().playEffect(pos2, Effect.STEP_SOUND, material, data);
            }
            gm.getCurrentArena().createRespawnData(player, isPlayer);
            player.setGameMode(GameMode.SPECTATOR);
            gm.sendRespawnTitleTo(player);
        }
    }

    public List<Material> getSpawnFoods() {
        return foods;
    }

    public boolean isBlueReady() {
        return blueReady;
    }

    public void toggleBlueReady() {
        blueReady =! blueReady;
    }

    public boolean isRedReady() {
        return redReady;
    }

    public void toggleRedReady() {
        redReady =! redReady;
    }

    public int getRedCaptures() {
        return redCapAmount;
    }

    public int getRedCaptureState() {
        return redCaptureState;
    }
    public int getBlueCaptures() {
        return blueCapAmount;
    }

    public int getBlueCaptureState() {
        return blueCaptureState;
    }

    public void setLastRedCap() {
        lastRedCap = System.currentTimeMillis();
    }

    public void setLastBlueCap() {
        lastBlueCap = System.currentTimeMillis();
    }

    public long getTimeSinceBlueCap() {
        return System.currentTimeMillis() - lastBlueCap;
    }

    public long getTimeSinceRedCap() {
        return System.currentTimeMillis() - lastRedCap;
    }

    public void sendRespawnTitleTo(Player player) {
        GameManager gm = InManager.get().getInstance(GameManager.class);
        GameManager.RespawnData d = gm.getCurrentArena().getRespawnData(player);
        if(d != null) {
            double percent = (d.getTimeSinceInitialized() * 1d / d.getTimeUsed() * 1d) * 100d;
            //red orange yellow green
            int id = percent <= 25 ? 1 : percent <= 50 ? 2 : percent <= 75 ? 3 : 4;
            String c = d.getTimeSinceInitialized() % 500 < 250 ? "c" : "e", c2 = id == 1 ? "c" : id == 2 ? "6" : id == 3 ? "e" : "a";
            if(d.getTimeSinceInitialized() % 1750 <= 1200) player.playSound(player.getLocation(), Sound.BLOCK_NOTE_PLING, 2.0f, 0.5f * id);

            BukkitUtils.Title title = new BukkitUtils.Title("&"+c+"&lYou have died!"),
                    subTitle = new BukkitUtils.Title("&"+c2+TimeUtils.convertToString(d.getTimeUsed() - d.getTimeSinceInitialized())+"&a left until respawn!", true,
                            d.hasTitle() ? 0 : 2, 20, 2);
            subTitle.sendTo(player, false, false);
            title.sendTo(player, false, false);

            d.markTitle();
        }
    }

    public static class AttackData {
        private UUID damager, victim;
        private long lastHit;

        public AttackData(Player damager, Player victim) {
            this.damager = damager.getUniqueId();
            this.victim = victim.getUniqueId();
            this.lastHit = System.currentTimeMillis();
        }

        public Player getDamager() {
            return Bukkit.getPlayer(damager);
        }

        public Player getVictim() {
            return Bukkit.getPlayer(victim);
        }

        public long getTimeSinceLastHit() {
            return System.currentTimeMillis() - lastHit;
        }

        public void updateAttack() {
            lastHit = System.currentTimeMillis();
        }
    }

    public static class RespawnData {
        private long init, lastTitle;
        private UUID uuid;
        private boolean isPlayer;

        public RespawnData(UUID uuid, boolean isPlayer) {
            this.uuid = uuid;
            this.isPlayer = isPlayer;
            this.init = System.currentTimeMillis();
        }

        //long ol name
        public long getTimeSinceInitialized() {
            return System.currentTimeMillis() - init;
        }

        public Player getPlayer() {
            return Bukkit.getPlayer(uuid);
        }

        public void markTitle() {
            lastTitle = System.currentTimeMillis();
        }

        public boolean hasTitle() {
            return System.currentTimeMillis() - lastTitle <= 5000;
        }

        public boolean shouldRespawn() {
            return getTimeSinceInitialized() >= getTimeUsed();
        }

        public long getTimeUsed() {
            return isPlayer ? RESPAWN_TIME_PLAYER : RESPAWN_TIME_STUPID;
        }

        public UUID getUUID() {
            return uuid;
        }

        public void setInit(long init) {
            this.init = init;
        }
    }
}
