package net.narutomod.procedure;

import net.narutomod.item.ItemTeamScroll;
import net.narutomod.ElementsNarutomodMod;

import net.minecraft.item.ItemStack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.Entity;

import java.util.Map;

@ElementsNarutomodMod.ModElement.Tag
public class ProcedureTeamManagerJoin extends ElementsNarutomodMod.ModElement {
	public ProcedureTeamManagerJoin(ElementsNarutomodMod instance) {
		super(instance, 554);
	}

	public static void executeProcedure(Map<String, Object> dependencies) {
		if (dependencies.get("entity") == null) {
			System.err.println("Failed to load dependency entity for procedure TeamManagerJoin!");
			return;
		}
		Entity entity = (Entity) dependencies.get("entity");
		String teamName = "";
		String playerName = "";
		ItemStack helditem = ItemStack.EMPTY;
		helditem = ((entity instanceof EntityLivingBase) ? ((EntityLivingBase) entity).getHeldItemMainhand() : ItemStack.EMPTY);
		if (((helditem).getItem() == new ItemStack(ItemTeamScroll.block, (int) (1)).getItem())) {
			ItemTeamScroll.ItemCustom.addTeamMember(helditem, (EntityPlayer) entity);
		}
	}
}
