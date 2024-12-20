
package net.narutomod.gui;

import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import net.narutomod.item.ItemFuton;
import net.narutomod.item.ItemRaiton;
import net.narutomod.procedure.ProcedureUtils;
import net.narutomod.PlayerTracker;
import net.narutomod.ElementsNarutomodMod;

@ElementsNarutomodMod.ModElement.Tag
public class GuiScrollRasenshurikenGui extends ElementsNarutomodMod.ModElement {
	public static int GUIID = 24;

	public GuiScrollRasenshurikenGui(ElementsNarutomodMod instance) {
		super(instance, 482);
	}

	public static class GuiContainerMod extends GuiNinjaScroll.GuiContainerMod {
		public GuiContainerMod(World world, int x, int y, int z, EntityPlayer player) {
			super(world, x, y, z, player, GUIID);
		}

		@Override
		protected void handleButtonAction(EntityPlayer player, int buttonID) {
			// security measure to prevent arbitrary chunk generation
			if (player.world.isRemote || !player.world.isBlockLoaded(new BlockPos(this.x, this.y, this.z)))
				return;
			ItemStack stack1 = ProcedureUtils.getMatchingItemStack(player, ItemFuton.block);
			if (stack1 == null || !stack1.hasTagCompound() || !stack1.getTagCompound().getBoolean("IsNatureAffinityKey")) {
				player.sendStatusMessage(new TextComponentTranslation("This is not your affinity."), false);
				return;
			}
			ItemStack stack = GuiNinjaScroll.enableJutsu(player, (ItemFuton.RangedItem)ItemFuton.block, ItemFuton.RASENSHURIKEN, true);
			if (stack != null) {
				super.handleButtonAction(player, buttonID);
			}
		}
	}

	public static class GuiWindow extends GuiNinjaScroll.GuiWindow {
		public GuiWindow(World world, int x, int y, int z, EntityPlayer entity) {
			super(new GuiContainerMod(world, x, y, z, entity));
		}

		@Override
		protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3) {
			super.drawGuiContainerBackgroundLayer(par1, par2, par3);
			this.mc.renderEngine.bindTexture(new ResourceLocation("narutomod:textures/blocks/futon.png"));
			this.drawModalRectWithCustomSizedTexture(this.guiLeft + 89, this.guiTop + 49, 0, 0, 48, 48, 48, 48);
		}

		@Override
		protected void drawGuiContainerForegroundLayer(int par1, int par2) {
			this.fontRenderer.drawString(ItemFuton.RASENSHURIKEN.getName(), 38, 13, -16777216);
		}
	}
}
