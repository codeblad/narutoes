package net.narutomod.procedure;

import net.narutomod.item.ItemJutsu;
import net.narutomod.item.ItemDojutsu;
import net.narutomod.item.ItemBijuCloak;
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
public class ProcedurePowerIncreaseOnKeyPressed extends ElementsNarutomodMod.ModElement {
	public ProcedurePowerIncreaseOnKeyPressed(ElementsNarutomodMod instance) {
		super(instance, 104);
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
			System.err.println("Failed to load dependency is_pressed for procedure PowerIncreaseOnKeyPressed!");
			return;
		}
		if (dependencies.get("entity") == null) {
			System.err.println("Failed to load dependency entity for procedure PowerIncreaseOnKeyPressed!");
			return;
		}
		if (dependencies.get("world") == null) {
			System.err.println("Failed to load dependency world for procedure PowerIncreaseOnKeyPressed!");
			return;
		}
		boolean is_pressed = (boolean) dependencies.get("is_pressed");
		Entity entity = (Entity) dependencies.get("entity");
		World world = (World) dependencies.get("world");
		double i = 0;
		ItemStack helmet = ItemStack.EMPTY;
		ItemStack itemmainhand = ItemStack.EMPTY;
		ItemStack itemoffhand = ItemStack.EMPTY;
		if ((!(world.isRemote))) {
			helmet = ((entity instanceof EntityPlayer) ? ((EntityPlayer) entity).inventory.armorInventory.get(3) : ItemStack.EMPTY);
			itemmainhand = ((entity instanceof EntityLivingBase) ? ((EntityLivingBase) entity).getHeldItemMainhand() : ItemStack.EMPTY);
			itemoffhand = ((entity instanceof EntityLivingBase) ? ((EntityLivingBase) entity).getHeldItemOffhand() : ItemStack.EMPTY);
			if (itemmainhand.getItem() instanceof ItemJutsu.Base) {
				if ((!(is_pressed))) {
					ItemJutsu.Base.switchNextJutsu(itemmainhand, (EntityLivingBase) entity);
				}
			} else if (itemoffhand.getItem() instanceof ItemJutsu.Base) {
				if ((!(is_pressed))) {
					ItemJutsu.Base.switchNextJutsu(itemoffhand, (EntityLivingBase) entity);
				}
			} else if ((helmet.getItem() instanceof ItemDojutsu.Base
					&& ((ItemDojutsu.Base) helmet.getItem()).onSwitchJutsuKey(is_pressed, helmet, (EntityPlayer) entity))) {
				return;
			} else if ((((helmet).getItem() == new ItemStack(ItemBijuCloak.helmet, (int) (1)).getItem())
					&& ((((entity instanceof EntityPlayer) ? ((EntityPlayer) entity).inventory.armorInventory.get(2) : ItemStack.EMPTY)
							.getItem() == new ItemStack(ItemBijuCloak.body, (int) (1)).getItem())
							&& (((entity instanceof EntityPlayer) ? ((EntityPlayer) entity).inventory.armorInventory.get(1) : ItemStack.EMPTY)
									.getItem() == new ItemStack(ItemBijuCloak.legs, (int) (1)).getItem())))) {
				if ((!(is_pressed))) {
					NBTTagCompound nbt = new NBTTagCompound();
					entity.writeToNBT(nbt);
					if (hasSlot(nbt, 0)) {
						ProcedureUtils.sendStatusMessage((EntityPlayer) entity, "You are bound.", false);
						return;
			}
					EntityBijuManager.increaseCloakLevel((EntityPlayer) entity);
				}
			}
		}
	}
}
