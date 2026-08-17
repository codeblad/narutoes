package net.narutomod.procedure;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.narutomod.ElementsNarutomodMod;
import net.narutomod.NarutomodModVariables;
import net.narutomod.entity.EntityBijuManager;
import net.narutomod.entity.EntityTailedBeast;
import net.narutomod.item.ItemDojutsu;

import java.util.Map;

@ElementsNarutomodMod.ModElement.Tag
public class ProcedureSpecialJutsu4OnKeyPressed extends ElementsNarutomodMod.ModElement {
	public ProcedureSpecialJutsu4OnKeyPressed(ElementsNarutomodMod instance) {
		super(instance, 101);
	}
	public static boolean hasSlot(NBTTagCompound nbt, int targetSlot) {
    if (nbt.hasKey("ForgeCaps", Constants.NBT.TAG_COMPOUND)) {
        NBTTagCompound forgeCaps = nbt.getCompoundTag("ForgeCaps");
        if (forgeCaps.hasKey("knapm:container", Constants.NBT.TAG_COMPOUND)) {
            NBTTagCompound container = forgeCaps.getCompoundTag("knapm:container");
            if (container.hasKey("Items", Constants.NBT.TAG_LIST)) {
                NBTTagList items = container.getTagList("Items", Constants.NBT.TAG_COMPOUND);
                for (int i = 0; i < items.tagCount(); i++) {
                    NBTTagCompound itemEntry = items.getCompoundTagAt(i);
                        int slot = itemEntry.getInteger("Slot");
                        if (slot == targetSlot) {
                            return true;
                        }
                }
            }
        }
    }
    return false; 
	}

	public static void executeProcedure(Map<String, Object> dependencies) {
		if (dependencies.get("is_pressed") == null) {
			System.err.println("Failed to load dependency is_pressed for procedure SpecialJutsu4OnKeyPressed!");
			return;
		}
		if (dependencies.get("entity") == null) {
			System.err.println("Failed to load dependency entity for procedure SpecialJutsu4OnKeyPressed!");
			return;
		}
		if (dependencies.get("world") == null) {
			System.err.println("Failed to load dependency world for procedure SpecialJutsu4OnKeyPressed!");
			return;
		}
		boolean is_pressed = (boolean) dependencies.get("is_pressed");
		Entity entity = (Entity) dependencies.get("entity");
		entity.getEntityData().setBoolean((NarutomodModVariables.EYETOGGLE), (is_pressed));
		World world = (World) dependencies.get("world");
		ItemStack helmet = ItemStack.EMPTY;
		if (((world.isRemote) || ((EntityPlayer) entity).isSpectator())) {
			return;
		}

	}
}
