package cum.xiaomao.zerohack.util.inventory;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.Slot;
import net.minecraft.item.*;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static baritone.api.utils.Helper.mc;

public class InventoryUtil extends Util {

    public static void switchToHotbarSlot(int slot, boolean silent) {
        if (mc.player.inventory.currentItem == slot || slot < 0) {
            return;
        }
        if (silent) {
            mc.player.connection.sendPacket((Packet)new CPacketHeldItemChange(slot));
            mc.playerController.updateController();
        } else {
            mc.player.inventory.currentItem = slot;
            mc.playerController.updateController();
        }
    }

    public static void switchToHotbarSlot(Class clazz, boolean silent) {
        int slot = InventoryUtil.findHotbarBlock(clazz);
        if (slot > -1) {
            InventoryUtil.switchToHotbarSlot(slot, silent);
        }
    }

    public static boolean isNull(ItemStack stack) {
        return stack == null || stack.getItem() instanceof ItemAir;
    }

    public static int findHotbarBlock(Class clazz) {
        for (int i = 0; i < 9; ++i) {
            Block block;
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack == ItemStack.EMPTY) continue;
            if (clazz.isInstance(stack.getItem())) {
                return i;
            }
            if (!(stack.getItem() instanceof ItemBlock) || !clazz.isInstance(block = ((ItemBlock)stack.getItem()).getBlock())) continue;
            return i;
        }
        return -1;
    }

    public static int findHotbarBlock(Block blockIn) {
        for (int i = 0; i < 9; ++i) {
            Block block;
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack == ItemStack.EMPTY || !(stack.getItem() instanceof ItemBlock) || (block = ((ItemBlock)stack.getItem()).getBlock()) != blockIn) continue;
            return i;
        }
        return -1;
    }

    public static int getItemHotbar(Item input) {
        for (int i = 0; i < 9; ++i) {
            Item item = mc.player.inventory.getStackInSlot(i).getItem();
            if (Item.getIdFromItem(item) != Item.getIdFromItem(input)) continue;
            return i;
        }
        return -1;
    }

    public static void switchToSlot(int slot) {
        if (mc.player.inventory.currentItem == slot || slot < 0) {
            return;
        }
        mc.player.inventory.currentItem = slot;
        mc.playerController.updateController();
    }

    public static int getItemHotbars(Item input) {
        for (int i = 0; i < 36; ++i) {
            Item item = mc.player.inventory.getStackInSlot(i).getItem();
            if (Item.getIdFromItem(item) != Item.getIdFromItem((Item)input)) continue;
            return i;
        }
        return -1;
    }

    public static int findStackInventory(Item input) {
        return InventoryUtil.findStackInventory(input, false);
    }

    public static int findStackInventory(Item input, boolean withHotbar) {
        int i = 0;
        int n = i;
        for (i = withHotbar ? 0 : 9; i < 36; ++i) {
            Item item = mc.player.inventory.getStackInSlot(i).getItem();
            if (Item.getIdFromItem((Item)input) != Item.getIdFromItem((Item)item)) continue;
            return i + (i < 9 ? 36 : 0);
        }
        return -1;
    }

    public static int findItemInventorySlot(Item item, boolean offHand) {
        AtomicInteger slot = new AtomicInteger();
        slot.set(-1);
        for (Map.Entry<Integer, ItemStack> entry : InventoryUtil.getInventoryAndHotbarSlots().entrySet()) {
            if (entry.getValue().getItem() != item || entry.getKey() == 45 && !offHand) continue;
            slot.set(entry.getKey());
            return slot.get();
        }
        return slot.get();
    }

    public static int findItemInHotbar(Item item) {
        int index = -1;
        for (int i = 0; i < 9; ++i) {
            if (mc.player.inventory.getStackInSlot(i).getItem() != item) continue;
            index = i;
            break;
        }
        return index;
    }

    public static List<Integer> getItemInventory(Item item) {
        ArrayList<Integer> ints = new ArrayList<Integer>();
        for (int i = 9; i < 36; ++i) {
            Item target = mc.player.inventory.getStackInSlot(i).getItem();
            if (!(item instanceof ItemBlock) || !((ItemBlock)item).getBlock().equals(item)) continue;
            ints.add(i);
        }
        if (ints.size() == 0) {
            ints.add(-1);
        }
        return ints;
    }

    public static boolean isBlock(Item item, Class clazz) {
        if (item instanceof ItemBlock) {
            Block block = ((ItemBlock)item).getBlock();
            return clazz.isInstance(block);
        }
        return false;
    }

    public static Map<Integer, ItemStack> getInventoryAndHotbarSlots() {
        return InventoryUtil.getInventorySlots(9, 44);
    }

    private static Map<Integer, ItemStack> getInventorySlots(int currentI, int last) {
        HashMap<Integer, ItemStack> fullInventorySlots = new HashMap<Integer, ItemStack>();
        for (int current = currentI; current <= last; ++current) {
            fullInventorySlots.put(current, (ItemStack) mc.player.inventoryContainer.getInventory().get(current));
        }
        return fullInventorySlots;
    }

    public static boolean holdingItem(Class clazz) {
        boolean result = false;
        ItemStack stack = mc.player.getHeldItemMainhand();
        result = InventoryUtil.isInstanceOf(stack, clazz);
        if (!result) {
            ItemStack offhand = mc.player.getHeldItemOffhand();
            result = InventoryUtil.isInstanceOf(stack, clazz);
        }
        return result;
    }

    public static boolean isInstanceOf(ItemStack stack, Class clazz) {
        if (stack == null) {
            return false;
        }
        Item item = stack.getItem();
        if (clazz.isInstance(item)) {
            return true;
        }
        if (item instanceof ItemBlock) {
            Block block = Block.getBlockFromItem((Item)item);
            return clazz.isInstance(block);
        }
        return false;
    }

    public static int findArmorSlot(EntityEquipmentSlot type, boolean binding) {
        int slot = -1;
        float damage = 0.0f;
        for (int i = 9; i < 45; ++i) {
            ItemArmor armor;
            ItemStack s = Minecraft.getMinecraft().player.inventoryContainer.getSlot(i).getStack();
            if (s.getItem() == Items.AIR || !(s.getItem() instanceof ItemArmor) || (armor = (ItemArmor)s.getItem()).getEquipmentSlot() != type) continue;
            float currentDamage = armor.damageReduceAmount + EnchantmentHelper.getEnchantmentLevel((Enchantment)Enchantments.PROTECTION, (ItemStack)s);
            boolean cursed = binding && EnchantmentHelper.hasBindingCurse((ItemStack)s);
            boolean bl = cursed;
            if (!(currentDamage > damage) || cursed) continue;
            damage = currentDamage;
            slot = i;
        }
        return slot;
    }

    public static int findArmorSlot(EntityEquipmentSlot type, boolean binding, boolean withXCarry) {
        int slot = InventoryUtil.findArmorSlot(type, binding);
        if (slot == -1 && withXCarry) {
            float damage = 0.0f;
            for (int i = 1; i < 5; ++i) {
                ItemArmor armor;
                Slot craftingSlot = (Slot) mc.player.inventoryContainer.inventorySlots.get(i);
                ItemStack craftingStack = craftingSlot.getStack();
                if (craftingStack.getItem() == Items.AIR || !(craftingStack.getItem() instanceof ItemArmor) || (armor = (ItemArmor)craftingStack.getItem()).getEquipmentSlot() != type) continue;
                float currentDamage = armor.damageReduceAmount + EnchantmentHelper.getEnchantmentLevel((Enchantment)Enchantments.PROTECTION, (ItemStack)craftingStack);
                boolean cursed = binding && EnchantmentHelper.hasBindingCurse((ItemStack)craftingStack);
                boolean bl = cursed;
                if (!(currentDamage > damage) || cursed) continue;
                damage = currentDamage;
                slot = i;
            }
        }
        return slot;
    }

    public static int findItemInventorySlot(Item item, boolean offHand, boolean withXCarry) {
        int slot = InventoryUtil.findItemInventorySlot(item, offHand);
        if (slot == -1 && withXCarry) {
            for (int i = 1; i < 5; ++i) {
                Item craftingStackItem;
                Slot craftingSlot = (Slot) mc.player.inventoryContainer.inventorySlots.get(i);
                ItemStack craftingStack = craftingSlot.getStack();
                if (craftingStack.getItem() == Items.AIR || (craftingStackItem = craftingStack.getItem()) != item) continue;
                slot = i;
            }
        }
        return slot;
    }

    public static boolean findHotbarBlock(boolean b, @NotNull Function0<Unit> function) {
            return false;
    }

    public static class Task {
        private final int slot;
        private final boolean update;
        private final boolean quickClick;

        public Task() {
            this.update = true;
            this.slot = -1;
            this.quickClick = false;
        }

        public Task(int slot) {
            this.slot = slot;
            this.quickClick = false;
            this.update = false;
        }

        public Task(int slot, boolean quickClick) {
            this.slot = slot;
            this.quickClick = quickClick;
            this.update = false;
        }

        public void run() {
            if (this.update) {
                mc.playerController.updateController();
            }
            if (this.slot != -1) {
                mc.playerController.windowClick(0, this.slot, 0, this.quickClick ? ClickType.QUICK_MOVE : ClickType.PICKUP, (EntityPlayer) mc.player);
            }
        }

    }

    public static enum Switch {
        NORMAL,
        SILENT,
        NONE;

    }
}

