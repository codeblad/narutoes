package net.narutomod.procedure;

import net.narutomod.ElementsNarutomodMod;
import net.narutomod.Chakra;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.Entity;

import java.util.Map;

@ElementsNarutomodMod.ModElement.Tag
public class ProcedureChakraSaturationOnPotionActiveTick extends ElementsNarutomodMod.ModElement {
	public ProcedureChakraSaturationOnPotionActiveTick(ElementsNarutomodMod instance) {
		super(instance, 1997);
	}

	public static void executeProcedure(Map<String, Object> dependencies) {
		if (dependencies.get("entity") == null) {
			System.err.println("Failed to load dependency entity for procedure ChakraRegenerationOnPotionActiveTick!");
			return;
		}
		if (dependencies.get("amplifier") == null) {
			System.err.println("Failed to load dependency amplifier for procedure ChakraRegenerationOnPotionActiveTick!");
			return;
		}
		Entity entity = (Entity) dependencies.get("entity");
		if ((entity instanceof EntityPlayer)) {
			if (((entity.ticksExisted % 20) == 4)) {
				((EntityPlayer) entity).getFoodStats().addStats(1,0);
			}
		}
	}
}
