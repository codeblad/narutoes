
package net.narutomod.gui;

import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import net.narutomod.item.ItemSuiton;
import net.narutomod.procedure.ProcedureUtils;
import net.narutomod.PlayerTracker;
import net.narutomod.ElementsNarutomodMod;

@ElementsNarutomodMod.ModElement.Tag
public class GuiScrollHidingInMistGui extends ElementsNarutomodMod.ModElement {
	public static int GUIID = 34;

	public GuiScrollHidingInMistGui(ElementsNarutomodMod instance) {
		super(instance, 502);
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
			ItemStack stack1 = ProcedureUtils.getMatchingItemStack(player, ItemSuiton.block);
			if (stack1 == null || !stack1.hasTagCompound() || !stack1.getTagCompound().getBoolean("IsNatureAffinityKey")) {
				player.sendStatusMessage(new TextComponentTranslation("This is not your affinity."), false);
				return;
			}
			ItemStack stack = GuiNinjaScroll.enableJutsu(player, (ItemSuiton.RangedItem)ItemSuiton.block, ItemSuiton.HIDINGINMIST, true);
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
			this.mc.renderEngine.bindTexture(new ResourceLocation("narutomod:textures/blocks/suiton.png"));
			this.drawModalRectWithCustomSizedTexture(this.guiLeft + 89, this.guiTop + 49, 0, 0, 48, 48, 48, 48);
			this.mc.renderEngine.bindTexture(new ResourceLocation("narutomod:textures/chou_.png"));
			this.drawModalRectWithCustomSizedTexture(this.guiLeft + 0, this.guiTop + 110, 0, 0, 42, 42, 42, 42);
			this.mc.renderEngine.bindTexture(new ResourceLocation("narutomod:textures/si_.png"));
			this.drawModalRectWithCustomSizedTexture(this.guiLeft + 50, this.guiTop + 110, 0, 0, 42, 42, 42, 42);
			this.mc.renderEngine.bindTexture(new ResourceLocation("narutomod:textures/wei_.png"));
			this.drawModalRectWithCustomSizedTexture(this.guiLeft + 100, this.guiTop + 110, 0, 0, 42, 42, 42, 42);
		}

		@Override
		protected void drawGuiContainerForegroundLayer(int par1, int par2) {
			this.fontRenderer.drawString(ItemSuiton.HIDINGINMIST.getName(), 38, 13, -16777216);
		}
	}
}
