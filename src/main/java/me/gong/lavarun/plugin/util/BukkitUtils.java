package me.gong.lavarun.plugin.util;

import com.google.common.collect.Multimap;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public class BukkitUtils {

    private static Class<?> CLnms_tagCompound, CL_enumItemSlot, CL_packetTitleType;
    private static Method asNMSCopy, asBukkitCopy,
            nms_itemStack__getTag, nms_itemStack__setTag, nms_itemStack__getItem,
            nms_item_getAttribs, set, add, attrib_to_compound, getHandle, sendPacket, serialize;
    private static Constructor<?> Cnbt_tagString, Cnbt_tagInt, Cnbt_tagDouble, Cnbt_tagList,
            CplayerAbilities, CabilitiesPacket, CchatPacket, CpacketPlayOutTitle, CcomponentText;
    private static Field disableDamage;
    private static Field isFlying;
    private static Field allowFlying;
    private static Field isCreativeMode;
    private static Field allowEdit;
    private static Field flySpeed;
    private static Field walkSpeed;
    private static Field playerConnection;
    private static Field networkManager;

    static {
        try {
            Class<?> CLnms_itemStack = getClass("ItemStack", true);
            Class<?> CLcraft_itemstack = getClass("inventory.CraftItemStack", false);
            CLnms_tagCompound = getClass("NBTTagCompound", true);
            CL_enumItemSlot = getClass("EnumItemSlot", true);
            CL_packetTitleType = getClass("PacketPlayOutTitle$EnumTitleAction", true);
            Class<?> CLnbt_tag_string = getClass("NBTTagString", true);
            Class<?> CLnbt_tag_double = getClass("NBTTagDouble", true);
            Class<?> CLnbt_tag_int = getClass("NBTTagInt", true);
            Class<?> CLnbt_tag_list = getClass("NBTTagList", true);
            Class<?> CL_nbtBase = getClass("NBTBase", true);
            Class<?> CL_item = getClass("Item", true);
            Class<?> CL_genericAttr = getClass("GenericAttributes", true);
            Class<?> CL_attrib_modifier = getClass("AttributeModifier", true);
            Class<?> CL_abilities = getClass("PlayerAbilities", true);
            Class<?> CL_packetAbilities = getClass("PacketPlayOutAbilities", true);
            Class<?> CL_craftPlayer = getClass("entity.CraftPlayer", false);
            Class<?> CL_packet = getClass("Packet", true);
            Class<?> CL_entityPlayer = getClass("EntityPlayer", true);
            Class<?> CL_playerConnection = getClass("PlayerConnection", true);
            Class<?> CL_networkManager = getClass("NetworkManager", true);
            Class<?> CL_chatPacket = getClass("PacketPlayOutChat", true);
            Class<?> CL_chatBase = getClass("IChatBaseComponent", true);
            Class<?> CL_chatSerializer = getClass("IChatBaseComponent$ChatSerializer", true);
            Class<?> CL_packetTitle = getClass("PacketPlayOutTitle", true);
            Class<?> CL_chatComponentText = getClass("ChatComponentText", true);

            asNMSCopy = CLcraft_itemstack.getMethod("asNMSCopy", org.bukkit.inventory.ItemStack.class);
            asBukkitCopy = CLcraft_itemstack.getMethod("asBukkitCopy", CLnms_itemStack);
            nms_itemStack__getTag = CLnms_itemStack.getMethod("getTag");
            nms_itemStack__setTag = CLnms_itemStack.getMethod("setTag", CLnms_tagCompound);
            nms_itemStack__getItem = CLnms_itemStack.getMethod("getItem");
            set = CLnms_tagCompound.getMethod("set", String.class, CL_nbtBase);
            add = CLnbt_tag_list.getMethod("add", CL_nbtBase);
            nms_item_getAttribs = CL_item.getMethod("a", CL_enumItemSlot);
            getHandle = CL_craftPlayer.getMethod("getHandle");
            sendPacket = CL_networkManager.getMethod("sendPacket", CL_packet);
            serialize = CL_chatSerializer.getMethod("a", String.class);

            Cnbt_tagString = CLnbt_tag_string.getConstructor(String.class);
            Cnbt_tagDouble = CLnbt_tag_double.getConstructor(double.class);
            Cnbt_tagInt = CLnbt_tag_int.getConstructor(int.class);
            Cnbt_tagList = CLnbt_tag_list.getConstructor();
            CplayerAbilities = CL_abilities.getConstructor();
            CabilitiesPacket = CL_packetAbilities.getConstructor(CL_abilities);
            attrib_to_compound = CL_genericAttr.getMethod("a", CL_attrib_modifier);
            CchatPacket = CL_chatPacket.getConstructor(CL_chatBase, byte.class);
            CpacketPlayOutTitle = CL_packetTitle.getConstructor(CL_packetTitleType, CL_chatBase, int.class, int.class, int.class);
            CcomponentText = CL_chatComponentText.getConstructor(String.class);

            disableDamage = CL_abilities.getField("isInvulnerable");
            isFlying = CL_abilities.getField("isFlying");
            allowFlying = CL_abilities.getField("canFly");
            isCreativeMode = CL_abilities.getField("canInstantlyBuild");
            allowEdit = CL_abilities.getField("mayBuild");
            flySpeed = CL_abilities.getField("flySpeed");
            walkSpeed = CL_abilities.getField("walkSpeed");
            playerConnection = CL_entityPlayer.getField("playerConnection");
            networkManager = CL_playerConnection.getField("networkManager");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static String getCraftVersion() {
        return org.bukkit.Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    }

    private static Object attributeToNBT(Map.Entry<Object, Object> entry) {
        try {
            Object nbt = attrib_to_compound.invoke(null, entry.getValue());

            set.invoke(nbt, "AttributeName", Cnbt_tagString.newInstance(entry.getKey()));
            set.invoke(nbt, "Slot", Cnbt_tagString.newInstance("mainhand"));
            return nbt;
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
            return null;
        }

    }

    public static class Capabilities {
        /**
         * Disables player damage.
         */
        public boolean disableDamage;

        /**
         * Sets/indicates whether the player is flying.
         */
        public boolean isFlying;

        /**
         * whether or not to allow the player to fly when they double jump.
         */
        public boolean allowFlying;

        /**
         * Used to determine if creative mode is enabled, and therefore if items should be depleted on usage
         */
        public boolean isCreativeMode;

        /**
         * Indicates whether the player is allowed to modify the surroundings
         */
        public boolean allowEdit = true;

        /**
         * Player flying speed
         */
        public float flySpeed = 0.05F;

        /**
         * Player walk speed
         */
        public float walkSpeed = 0.1F;

        public static Capabilities fromPlayer(Player player) {
            Capabilities ret = new Capabilities();
            ret.disableDamage = player.getGameMode() == GameMode.CREATIVE;
            ret.isFlying = player.isFlying();
            ret.allowFlying = player.getAllowFlight();
            ret.isCreativeMode = player.getGameMode() == GameMode.CREATIVE;
            ret.allowEdit = player.getGameMode() != GameMode.ADVENTURE;
            ret.flySpeed = player.getFlySpeed();
            ret.walkSpeed = player.getWalkSpeed();
            return ret;
        }

        private Object toUsable() {
            try {
                Object pA = CplayerAbilities.newInstance();
                BukkitUtils.disableDamage.set(pA, disableDamage);
                BukkitUtils.isFlying.set(pA, isFlying);
                BukkitUtils.allowFlying.set(pA, allowFlying);
                BukkitUtils.isCreativeMode.set(pA, isCreativeMode);
                BukkitUtils.allowEdit.set(pA, allowEdit);
                BukkitUtils.flySpeed.set(pA, flySpeed);
                BukkitUtils.walkSpeed.set(pA, walkSpeed);
                return pA;
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }

        private Object toPacket() {
            try {
                return CabilitiesPacket.newInstance(toUsable());

            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }
    }

    public enum TitleType {
        TITLE,
        SUBTITLE,
        TIMES,
        CLEAR,
        RESET;

        private Object toNMS() {
            try {
                return CL_packetTitleType.getEnumConstants()[ordinal()];
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }
    }

    public static class Title {
        protected boolean isSub;
        protected int fadeInTime, displayTime, fadeOutTime;
        protected String message;

        public Title(String title, boolean isSub, int fadeInTime, int displayTime, int fadeOutTime) {

            this.message = title;
            this.isSub = isSub;
            this.fadeInTime = fadeInTime;
            this.displayTime = displayTime;
            this.fadeOutTime = fadeOutTime;
        }

        public Title(String title, boolean isSub) {
            this(title, isSub, -1, -1, -1);
        }

        public Title(String title) {
            this(title, false);
        }

        public Title(int fadeInTime, int displayTime, int fadeOutTime) {
            this(null, false, fadeInTime, displayTime, fadeOutTime);
        }

        public Title() {
            this(null);
        }

        public void sendTo(Player player, boolean sendEmptyTitle, boolean sendResets) {
            if(sendResets) {
                sendPacket(player, createTitlePacket(TitleType.RESET, null));
                sendPacket(player, createTitlePacket(TitleType.CLEAR, null));
            }
            if(message == null) return;
            else if(fadeInTime != -1 || displayTime != -1 || fadeOutTime != -1)
                sendPacket(player, createTitlePacket(fadeInTime, displayTime, fadeOutTime));
            if(isSub && sendEmptyTitle) sendPacket(player, createTitlePacket(TitleType.TITLE, ""));
            sendPacket(player, createTitlePacket(isSub ? TitleType.SUBTITLE : TitleType.TITLE, message));
        }

        public void sendTo(Player player) {
            sendTo(player, true, true);
        }
    }

    public static Object createTitlePacket(TitleType type, String message, int fadeInTime, int displayTime, int fadeOutTime) {
        message = message == null ? null : StringUtils.format(message);
        Object base = message != null ? toBaseComponent(message) : null, nmsType = type.toNMS();
        try {
            return CpacketPlayOutTitle.newInstance(nmsType, base, fadeInTime, displayTime, fadeOutTime);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static Object createTitlePacket(TitleType type, String message) {
        return createTitlePacket(type, message, -1, -1, -1);
    }

    public static Object createTitlePacket(int fadeInTime, int displayTime, int fadeOutTime) {
        return createTitlePacket(TitleType.TIMES, null, fadeInTime, displayTime, fadeOutTime);
    }

    public static void sendGlobalAction(String message) {
        Bukkit.getOnlinePlayers().forEach(p -> sendActionMessage(p, message));
    }

    public static void sendActionMessage(Player player, String message) {
        try {
            sendPacket(player, CchatPacket.newInstance(CcomponentText.newInstance(message), (byte) 2));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static Object toBaseComponent(String message) {
        try {
            return serialize.invoke(null, toJSON(message));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String toJSON(String message) {
        try {
            return ComponentSerializer.toString(TextComponent.fromLegacyText(message));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void sendPacket(Player player, Object packet) {
        try {
            Object netMan = networkManager.get(playerConnection.get(getHandle.invoke(player)));
            sendPacket.invoke(netMan, packet);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void sendCapabilities(Player player, Capabilities capabilities) {
        try {
            sendPacket(player, capabilities.toPacket());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static ItemStack create1_8Pvp(ItemStack s) {

        try {
            Object nms_itemStack = asNMSCopy.invoke(null, s), item = nms_itemStack__getItem.invoke(nms_itemStack), tag = nms_itemStack__getTag.invoke(nms_itemStack),
                    mods = Cnbt_tagList.newInstance(), speed = CLnms_tagCompound.newInstance();

            set.invoke(speed, "AttributeName", Cnbt_tagString.newInstance("generic.attackSpeed"));
            set.invoke(speed, "Name", Cnbt_tagString.newInstance("generic.attackSpeed"));
            set.invoke(speed, "Amount", Cnbt_tagDouble.newInstance(100));
            set.invoke(speed, "Operation", Cnbt_tagInt.newInstance(AttributeModifier.Operation.ADD_NUMBER.ordinal()));
            set.invoke(speed, "UUIDLeast", Cnbt_tagInt.newInstance(1));
            set.invoke(speed, "UUIDMost", Cnbt_tagInt.newInstance(1));
            set.invoke(speed, "Slot", Cnbt_tagString.newInstance("mainhand"));

            Multimap<Object, Object> attribs = (Multimap<Object, Object>) nms_item_getAttribs.invoke(item, CL_enumItemSlot.getEnumConstants()[0]);

            //add back in all the attributes ( ;-; )
            for (Map.Entry<Object, Object> attrib : attribs.entries()) {
                if(((String) attrib.getKey()).equalsIgnoreCase("generic.attackSpeed")) continue; //ignore this one
                add.invoke(mods, attributeToNBT(attrib));
            }

            add.invoke(mods, speed);

            set.invoke(tag, "AttributeModifiers", mods);

            nms_itemStack__setTag.invoke(nms_itemStack, tag);

            return (ItemStack) asBukkitCopy.invoke(null, nms_itemStack);

        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static final BlockFace[] axis = { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST };
    public static final BlockFace[] radial = { BlockFace.NORTH, BlockFace.NORTH_EAST, BlockFace.EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH, BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST };

    /**
     * Gets the horizontal Block Face from a given yaw angle<br>
     * This includes the NORTH_WEST faces
     *
     * @param yaw angle
     * @return The Block Face of the angle
     */
    public static BlockFace yawToFace(float yaw) {
        return yawToFace(yaw, true);
    }

    /**
     * Gets the horizontal Block Face from a given yaw angle
     *
     * @param yaw angle
     * @param useSubCardinalDirections setting, True to allow NORTH_WEST to be returned
     * @return The Block Face of the angle
     */
    public static BlockFace yawToFace(float yaw, boolean useSubCardinalDirections) {
        if (useSubCardinalDirections) {
            return radial[Math.round(yaw / 45f) & 0x7];
        } else {
            return axis[Math.round(yaw / 90f) & 0x3];
        }
    }

    private static Class<?> getClass(String name, boolean nms) {
        try {
            return Class.forName(nms ? "net.minecraft.server."+getCraftVersion()+"."+name : "org.bukkit.craftbukkit."+getCraftVersion()+"."+name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
