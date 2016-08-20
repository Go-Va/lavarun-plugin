package me.gong.lavarun.plugin.command;

import me.gong.lavarun.plugin.Main;
import me.gong.lavarun.plugin.command.annotation.Command;
import me.gong.lavarun.plugin.command.annotation.SubCommand;
import me.gong.lavarun.plugin.InManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CommandManager implements Listener {


    private List<CommandData> cmds;

    public CommandManager() {
        Bukkit.getPluginManager().registerEvents(this, InManager.get().getInstance(Main.class));
        cmds = new ArrayList<>();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChat(PlayerCommandPreprocessEvent ev) {
        if(parse(ev.getMessage().substring(1), ev.getPlayer())) ev.setCancelled(true);
    }

    public boolean parse(String line, Player player) {
        line = line.trim();
        String cmd;
        String[] allData = line.split(" ");
        String[] args = new String[allData.length - 1];

        if (line.contains(" ")) for (int i = -1; i < allData.length; i++) {
            if (i < 0 || i >= allData.length - 1) {
                continue;
            }

            args[i] = allData[i + 1];
        }
        else {
            args = new String[0];
        }

        cmd = line.contains(" ") ? allData[0] : line;
        CommandData cmdData = getCommand(cmd);

        if (cmdData != null) {
            try {
                cmdData.execute(args, player);
                return true;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return false;
    }

    public CommandData getCommand(String name) {
        return cmds.stream().filter(c -> c.isThis(name)).findFirst().orElse(null);
    }

    private Object passClassChecks(Class<?> clazz) {
        try {
            Constructor<?> c = clazz.getDeclaredConstructor();
            c.setAccessible(true);
            return c.newInstance();
        } catch (Exception ex) {
            InManager.get().getInstance(Main.class).getLogger().warning("Class " + clazz.getName() + " didn't pass class checks");
        }

        return null;
    }

    public void addAllCommandsFromClass(Class<?> clazz) {
        getCommandsFromClass(clazz).forEach(cmds::add);
    }

    public void addAllCommands(Object obj) {
        getCommands(obj).forEach(cmds::add);
    }

    public void addCommand(CommandData data) {
        if (getCommand(data.getName()) != null ||
                Arrays.stream(data.aliases).filter(a -> getCommand(a) !=
                        null).findFirst().orElse(null) != null) {
            InManager.get().getInstance(Main.class).getLogger().warning("Tried to register command " + data.getName() + " but already registered");
            return;
        }

        cmds.add(data);
    }

    public void addCommands(List<CommandData> data) {
        data.forEach(this::addCommand);
    }

    public void addCommands(CommandData... data) {
        addCommands(Arrays.stream(data).collect(Collectors.toList()));
    }

    public List<CommandData> getCommands() {
        return cmds;
    }

    public List<CommandData> getCommandsFromClass(Class<?> clazz) {
        Object instance = passClassChecks(clazz);
        if(instance == null) return null;
        return getCommands(instance);
    }

    public List<CommandData> getCommands(Object instance) {
        List<String> takenMainNames = new ArrayList<>();

        List<CommandData> d = new ArrayList<>();

        for (Method m : instance.getClass().getDeclaredMethods()) {
            if (m.isAnnotationPresent(Command.class)) {
                m.setAccessible(true);
                Command cData = m.getAnnotation(Command.class);
                String name = cData.name().toLowerCase().trim();

                if (takenMainNames.contains(name)) {
                    InManager.get().getInstance(Main.class).getLogger().warning("Command " + name + " was already taken, skipping command");
                    continue;
                } else {
                    takenMainNames.add(name);
                }

                String[] aliases = processAliases(takenMainNames, cData.alias());

                if (aliases == null) {
                    InManager.get().getInstance(Main.class).getLogger().warning("Command " + name + " had a taken alias, skipping command");
                    continue;
                }

                CommandData c;

                if ((c = processCommand(cData, name, aliases, m, instance)) != null) {
                    d.add(c);
                } else {
                    InManager.get().getInstance(Main.class).getLogger().warning("Command " + name + " was invalid.");
                }
            }
        }

        return d;
    }

    private CommandData processCommand(Command c, String name, String[] aliases, Method m, Object instance) {
        if (m.getParameterTypes().length == 2 && Player.class.isAssignableFrom(m.getParameterTypes()[0]) &&
                CharSequence[].class.isAssignableFrom(m.getParameterTypes()[1]) &&
                boolean.class.isAssignableFrom(m.getReturnType())) {
            CommandDataBuilder b = new CommandDataBuilder().aliases(aliases).help(c.help()).instance(instance).method(m).syntax(c.syntax()).name(name);
            CommandData cmdData = b.build();
            Arrays.stream(getSubCommands(cmdData, name, instance)).forEach(cmdData::addSubCommand);
            return cmdData;
        }

        return null;
    }

    private SubCommandData[] getSubCommands(CommandData cmdData, String centralName, Object centralInstance) {
        List<SubCommandData> ret = new ArrayList<>();
        List<String> takenSubNames = new ArrayList<>();

        for (Method m : centralInstance.getClass().getMethods()) {
            if (m.isAnnotationPresent(SubCommand.class)) {
                SubCommand sub = m.getAnnotation(SubCommand.class);
                String name = sub.name().toLowerCase().trim();

                if (takenSubNames.contains(name)) {
                    InManager.get().getInstance(Main.class).getLogger().warning("Sub command " + name + " for command " + centralName +
                            " had a taken name, skipping sub command");
                    continue;
                } else {
                    takenSubNames.add(name);
                }

                String[] aliases = processAliases(takenSubNames, sub.alias());

                if (aliases == null) {
                    InManager.get().getInstance(Main.class).getLogger().warning("Sub command " + name + " for command " + centralName +
                            " had a taken alias, skipping sub command");
                    continue;
                }

                SubCommandData c;

                if ((c = processSubCommand(cmdData, sub.name(), aliases, sub, m)) != null) {
                    ret.add(c);
                } else {
                    InManager.get().getInstance(Main.class).getLogger().warning("Sub command " + name + " for command " + centralName + " was invalid.");
                }
            }
        }

        return ret.toArray(new SubCommandData[ret.size()]);
    }

    private SubCommandData processSubCommand(CommandData cmdData, String name, String[] aliases, SubCommand c, Method m) {
        if (m.getParameterTypes().length == 2 && Player.class.isAssignableFrom(m.getParameterTypes()[0]) &&
                CharSequence[].class.isAssignableFrom(m.getParameterTypes()[1]) &&
                c.name().toLowerCase().trim().equalsIgnoreCase(name) &&
                boolean.class.isAssignableFrom(m.getReturnType())) {
            return new SubCommandDataBuilder().name(name).aliases(aliases).syntax(c.syntax()).method(m).help(c.help()).central(cmdData).build();
        }

        return null;
    }

    public String[] processAliases(List<String> takenNames, String alias) {
        if (alias.isEmpty()) {
            return new String[0];
        }

        String[] found = alias.contains(",") ? alias.split(",") : new String[]{alias};
        String[] ret = new String[found.length];
        List<String> taken = new ArrayList<>();

        for (int i = 0; i < found.length; i++) {
            String s = found[i].trim();

            if (!takenNames.contains(s.toLowerCase()) && !taken.contains(s.toLowerCase())) {
                taken.add(s.toLowerCase());
            } else {
                return null;
            }

            ret[i] = s;
        }

        takenNames.addAll(taken); //don't add to main list before checking if name not taken
        return ret;
    }

    public static class CommandDataBuilder {
        protected String name, syntax = "", help;
        protected String[] aliases = new String[0];
        protected Method method;
        protected Object instance;

        public CommandDataBuilder name(String name) {
            this.name = name;
            return this;
        }

        public CommandDataBuilder syntax(String syntax) {
            this.syntax = syntax;
            return this;
        }

        public CommandDataBuilder help(String help) {
            this.help = help;
            return this;
        }

        public CommandDataBuilder aliases(String[] aliases) {
            this.aliases = aliases;
            return this;
        }

        public CommandDataBuilder method(Method method) {
            method.setAccessible(true);
            this.method = method;
            return this;
        }


        public CommandDataBuilder instance(Object instance) {
            this.instance = instance;
            return this;
        }

        private boolean verify() {
            return name != null && syntax != null && help != null && method != null && instance != null;
        }

        public CommandData build() {
            if (!verify()) {
                throw new RuntimeException("Not all arguments filled");
            }

            return new CommandData(name, syntax, help, aliases, method, instance, new SubCommandData[0]);
        }
    }

    public static class SubCommandDataBuilder {
        private CommandData central;
        protected String name, syntax, help;
        private String[] aliases = new String[0];
        private Method method;

        public SubCommandDataBuilder name(String name) {
            this.name = name;
            return this;
        }

        public SubCommandDataBuilder syntax(String syntax) {
            this.syntax = syntax;
            return this;
        }

        public SubCommandDataBuilder help(String help) {
            this.help = help;
            return this;
        }

        public SubCommandDataBuilder aliases(String[] aliases) {
            this.aliases = aliases;
            return this;
        }

        public SubCommandDataBuilder method(Method method) {
            this.method = method;
            return this;
        }

        public SubCommandDataBuilder central(CommandData central) {
            this.central = central;
            return this;
        }

        private boolean verify() {
            return name != null && syntax != null && help != null && method != null && central != null;
        }

        public SubCommandData build() {
            if (!verify()) {
                throw new RuntimeException("Not all arguments filled");
            }

            return new SubCommandData(central, name, syntax, help, aliases, method);
        }
    }

    public static class CommandData {
        private String name, syntax, help;
        private String[] aliases;
        private Method method;
        private Object instance;
        private List<SubCommandData> subCommands;

        public CommandData(String name, String syntax, String help, String[] aliases, Method method, Object instance, SubCommandData[] subCommands) {
            this.name = name;
            this.syntax = syntax;
            this.help = help;
            this.aliases = aliases;
            this.method = method;
            this.instance = instance;
            this.subCommands = new ArrayList<>();
            Arrays.stream(subCommands).forEach(this.subCommands::add);
        }

        public String getName() {
            return name;
        }

        public String getSyntax() {
            return syntax;
        }

        public String getHelp() {
            return help;
        }

        public String[] getAliases() {
            return aliases;
        }

        public Method getMethod() {
            return method;
        }

        public Object getInstance(boolean isSub) {
            return instance;
        }

        public SubCommandData[] getSubCommands() {
            return subCommands.toArray(new SubCommandData[subCommands.size()]);
        }

        public void addSubCommand(SubCommandData data) {
            subCommands.add(data);
        }

        public boolean isThis(String name) {
            if (name.toLowerCase().trim().equalsIgnoreCase(this.name.toLowerCase().trim())) {
                return true;
            }

            for (String alias : aliases)
                if (alias.toLowerCase().trim().equalsIgnoreCase(name.toLowerCase().trim())) {
                    return true;
                }

            return false;
        }

        public SubCommandData getSubCommand(String name) {
            return subCommands.stream().filter(s -> s.isThis(name)).findFirst().orElse(null);
        }

        public void execute(String[] args, Player player) {
            try {
                if (args.length > 0 && getSubCommand(args[0]) != null) {
                    getSubCommand(args[0]).execute(Arrays.copyOfRange(args, 1, args.length), player);
                    return;
                }

                if (!(boolean) method.invoke(instance, player, args)) {
                    if (!syntax.isEmpty()) {
                        player.sendMessage(ChatColor.GREEN+"Usage: " + name + " " + syntax);
                    } else if (subCommands.isEmpty()) {
                        player.sendMessage(ChatColor.GREEN+"Invalid usage for " + name);
                    } else {
                        String[] subs = new String[] {""};
                        boolean[] extra = new boolean[1];
                        subCommands.forEach(s -> {
                            subs[0] += ", " + s.name;
                            if (!s.syntax.isEmpty()) extra[0] = true;
                        });
                        String gen = "<" + subs[0].substring(2) + ">" + (extra[0] ? " [args...]" : "");
                        player.sendMessage(ChatColor.GREEN+"Usage: " + name + " " + gen);
                    }
                }
            } catch (Exception ex) {
                throw new CommandExecutionException("Error calling "+method.getName()+" in "+instance.getClass().getSimpleName()+" ", ex);
            }
        }

        @Override
        public String toString() {
            return "CommandData{" + "name='" + name + '\'' +
                    ", syntax='" + syntax + '\'' +
                    ", help='" + help + '\'' +
                    ", aliases=" + Arrays.toString(aliases) +
                    ", method=" + method +
                    ", instance=" + instance +
                    ", subCommands=" + subCommands +
                    '}';
        }
    }

    public static class SubCommandData {
        private CommandData central;
        private String name, syntax, help;
        private String[] aliases;
        private Method method;

        public SubCommandData(CommandData central, String name, String syntax, String help, String[] aliases, Method method) {
            this.central = central;
            this.name = name;
            this.syntax = syntax;
            this.help = help;
            this.aliases = aliases;
            this.method = method;
        }

        public CommandData getCentral() {
            return central;
        }

        public String getName() {
            return name;
        }

        public String getSyntax() {
            return syntax;
        }

        public String getHelp() {
            return help;
        }

        public String[] getAliases() {
            return aliases;
        }

        public Method getMethod() {
            return method;
        }
        
        public void setCentral(CommandData central) {
            this.central = central;
        }

        public boolean isThis(String name) {
            if (name.toLowerCase().trim().equalsIgnoreCase(this.name.toLowerCase().trim())) {
                return true;
            }

            for (String alias : aliases)
                if (alias.toLowerCase().trim().equalsIgnoreCase(name.toLowerCase().trim())) {
                    return true;
                }

            return false;
        }

        public void execute(String[] args, Player player) {
            try {
                if (!(boolean) method.invoke(central.getInstance(true), player, args)) {
                    if (!syntax.isEmpty()) {
                        player.sendMessage(ChatColor.GREEN+"Usage: " + central.name + " " + name + " " + syntax);
                    } else {
                        player.sendMessage(ChatColor.GREEN+"Invalid usage for sub-command " + name + " in command " + central.name);
                    }
                }
            } catch (Exception ex) {
                throw new SubCommandExecutionException("Error calling "+method.getName()+" in "+central.getInstance(true).getClass().getSimpleName()+" ", ex);
            }
        }

        @Override
        public String toString() {
            return "SubCommandData{" + "central=" + central +
                    ", name='" + name + '\'' +
                    ", syntax='" + syntax + '\'' +
                    ", help='" + help + '\'' +
                    ", aliases=" + Arrays.toString(aliases) +
                    ", method=" + method +
                    '}';
        }
    }

    private static class CommandExecutionException extends RuntimeException {
        public CommandExecutionException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private static class SubCommandExecutionException extends RuntimeException {
        public SubCommandExecutionException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class TabCompletionException extends RuntimeException {
        private boolean isSub;

        public TabCompletionException(Throwable cause, boolean isSub) {
            super(cause);
            this.isSub = isSub;
        }

        public boolean isSub() {
            return isSub;
        }
    }

    private static String stringifyObject(Object o) {
        if (o == null) {
            return "null";
        }

        if (o instanceof Object[]) {
            return Arrays.toString((Object[]) o);
        }

        return o.toString();
    }
}
