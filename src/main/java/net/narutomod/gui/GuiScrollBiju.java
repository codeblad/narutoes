
package net.narutomod.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemHandlerHelper;
import net.narutomod.ElementsNarutomodMod;
import net.narutomod.entity.EntityBijuManager;
import net.narutomod.item.*;
import net.narutomod.procedure.ProcedureUtils;

@ElementsNarutomodMod.ModElement.Tag
public class GuiScrollBiju extends ElementsNarutomodMod.ModElement {
	public static int GUIID = 6942;

	public GuiScrollBiju(ElementsNarutomodMod instance) {
		super(instance, 4897);
	}


	public static class GuiContainerMod extends GuiNinjaScroll.GuiContainerMod {
		public GuiContainerMod(World world, int x, int y, int z, EntityPlayer player) {
			super(world, x, y, z, player, GUIID);
		}



		public boolean usable = true;





		@Override
		protected void handleButtonAction(EntityPlayer player, int buttonID) {
			// security measure to prevent arbitrary chunk generation
			if (player.world.isRemote || !player.world.isBlockLoaded(new BlockPos(this.x, this.y, this.z)))
				return;
			if (!this.usable) {
				return;
			}

			this.usable = false;

			if (player.getDistance(0,0,0) > 3500) {
				player.sendStatusMessage(new TextComponentString("Please stay within 3000 blocks of spawn"), true);
				player.closeScreen();
				return;
			}

			int tail = buttonID+1;
			EntityBijuManager bm = EntityBijuManager.mapByTailnum.get(tail);

			if (!bm.isSealed() && !bm.isAddedToWorld()) {
				Entity beast = bm.spawnEntity(player.world,player.posX,player.posY+20,player.posZ,0);
				beast.setPosition(player.posX,player.posY+20,player.posZ);
				String pos = (int) player.posX+", "+(int) player.posY+", "+(int) player.posZ;
				ProcedureUtils.sendChatAll(I18n.translateToLocalFormatted("chattext.tailedbeast.arrival", tail, pos));
				super.handleButtonAction(player, buttonID);
			} else {
				if (bm.isAddedToWorld()) {
					Entity beast = bm.getEntity();
					BlockPos pos = bm.getPosition();
					String abc = pos.getX()+" "+pos.getY()+" "+pos.getZ();
					player.sendStatusMessage(new TextComponentString("The biju is already at "+abc), true);
				}
				if (bm.isSealed()) {
					player.sendStatusMessage(new TextComponentString("The biju is already at sealed"), true);
				}
			}



			player.closeScreen();
				//}
			/*if (stack != null) {
			}*/
		}
	}

	public static class GuiWindow extends GuiNinjaScroll.GuiWindow {
		public GuiWindow(World world, int x, int y, int z, EntityPlayer entity) {
			super(new GuiContainerMod(world, x, y, z, entity));
		}

		@Override
		protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3) {
			super.drawGuiContainerBackgroundLayer(par1, par2, par3);
			this.mc.renderEngine.bindTexture(new ResourceLocation("narutomod:textures/blocks/ninjutsu.png"));
			this.drawModalRectWithCustomSizedTexture(this.guiLeft + 89, this.guiTop + 49, 0, 0, 48, 48, 48, 48);
		}

		@Override
		protected void drawGuiContainerForegroundLayer(int par1, int par2) {
			this.fontRenderer.drawString("Spawn Your Beast.", 38, 13, -16777216);
			//this.buttonList.clear();
			this.buttonList.remove(0);
			this.buttonList.add(new GuiButton(0, this.guiLeft + 0, this.guiTop + 25, 39, 20, "Shukaku"));
			this.buttonList.add(new GuiButton(1, this.guiLeft + 50, this.guiTop + 25, 39, 20, "Matatabi"));
			this.buttonList.add(new GuiButton(2, this.guiLeft + 100, this.guiTop + 25, 39, 20, "Isobu"));
			this.buttonList.add(new GuiButton(3, this.guiLeft + 150, this.guiTop + 25, 39, 20, "Son Goku"));
			this.buttonList.add(new GuiButton(4, this.guiLeft + 200, this.guiTop + 25, 39, 20, "Kokuo"));
			this.buttonList.add(new GuiButton(5, this.guiLeft + 0, this.guiTop + 50, 39, 20, "Saiken"));
			this.buttonList.add(new GuiButton(6, this.guiLeft + 50, this.guiTop + 50, 39, 20, "Chomei"));
			this.buttonList.add(new GuiButton(7, this.guiLeft + 100, this.guiTop + 50, 39, 20, "Gyuki"));
			this.buttonList.add(new GuiButton(8, this.guiLeft + 150, this.guiTop + 50, 39, 20, "Kurama"));
		}
	}
}
