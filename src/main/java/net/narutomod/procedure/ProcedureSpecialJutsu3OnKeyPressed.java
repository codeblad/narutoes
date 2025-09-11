package net.narutomod.procedure;

import net.narutomod.NarutomodModVariables;
import net.narutomod.item.ItemDojutsu;
import net.narutomod.entity.EntityTailedBeast;
import net.narutomod.entity.EntityBijuManager;
import net.narutomod.ElementsNarutomodMod;

import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.Entity;

import java.util.Map;

@ElementsNarutomodMod.ModElement.Tag
public class ProcedureSpecialJutsu3OnKeyPressed extends ElementsNarutomodMod.ModElement {
	public ProcedureSpecialJutsu3OnKeyPressed(ElementsNarutomodMod instance) {
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
			System.err.println("Failed to load dependency is_pressed for procedure SpecialJutsu3OnKeyPressed!");
			return;
		}
		if (dependencies.get("entity") == null) {
			System.err.println("Failed to load dependency entity for procedure SpecialJutsu3OnKeyPressed!");
			return;
		}
		if (dependencies.get("world") == null) {
			System.err.println("Failed to load dependency world for procedure SpecialJutsu3OnKeyPressed!");
			return;
		}
		boolean is_pressed = (boolean) dependencies.get("is_pressed");
		Entity entity = (Entity) dependencies.get("entity");
		entity.getEntityData().setBoolean((NarutomodModVariables.JutsuKey3Pressed), (is_pressed));
		World world = (World) dependencies.get("world");
		ItemStack helmet = ItemStack.EMPTY;
		if (((world.isRemote) || ((EntityPlayer) entity).isSpectator())) {
			return;
		}
		helmet = ((entity instanceof EntityPlayer) ? ((EntityPlayer) entity).inventory.armorInventory.get(3) : ItemStack.EMPTY);
		if (helmet.getItem() instanceof ItemDojutsu.Base) {
			NBTTagCompound nbt = new NBTTagCompound();
			entity.writeToNBT(nbt);

			if (hasSlot(nbt, 2)) {
				ProcedureUtils.sendStatusMessage((EntityPlayer) entity, "You are blindfolded.", false);
				return;
			}

			if (((ItemDojutsu.Base) helmet.getItem()).onJutsuKey3(is_pressed, helmet, (EntityPlayer) entity)) {
				return;
			}
		} else if (EntityBijuManager.cloakLevel((EntityPlayer) entity) == 3) {
			if ((!(is_pressed))) {
				EntityBijuManager.getBijuOfPlayerInWorld((EntityPlayer) entity).attackEntityWithRangedAttack((EntityLivingBase) entity, 0f);
		}
		} else if (EntityBijuManager.cloakLevel((EntityPlayer) entity) == 2) {
			NBTTagCompound nbt = new NBTTagCompound();
			entity.writeToNBT(nbt);
			if (hasSlot(nbt, 1)) {
				ProcedureUtils.sendStatusMessage((EntityPlayer) entity, "You are gagged.", false);
			} else {
				EntityTailedBeast.EntityTailBeastBall.create((EntityLivingBase) entity, is_pressed);
			}
		}
	}
}
