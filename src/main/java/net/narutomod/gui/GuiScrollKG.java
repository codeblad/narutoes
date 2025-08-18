
package net.narutomod.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import net.narutomod.item.*;
import net.narutomod.procedure.ProcedureUtils;
import net.narutomod.PlayerTracker;
import net.narutomod.ElementsNarutomodMod;

@ElementsNarutomodMod.ModElement.Tag
public class GuiScrollKG extends ElementsNarutomodMod.ModElement {
	public static int GUIID = 1337;

	public GuiScrollKG(ElementsNarutomodMod instance) {
		super(instance, 2019);
	}


	public static class GuiContainerMod extends GuiNinjaScroll.GuiContainerMod {
		public GuiContainerMod(World world, int x, int y, int z, EntityPlayer player) {
			super(world, x, y, z, player, GUIID);
		}

		public static Item[] jutsuArray = {
				ItemKaton.block,
				ItemFuton.block,
				ItemRaiton.block,
				ItemDoton.block,
				ItemSuiton.block,
				ItemYoton.block,
				ItemInton.block
		};

		public static Item[] kgArray = {
				ItemFutton.block,
				ItemShikotsumyaku.block,
				ItemJinton.block,
				ItemBakuton.block,
				ItemHyoton.block,
				ItemYooton.block,
				ItemJiton.block,
				ItemShakuton.block,
				ItemRanton.block,
				ItemMokuton.block,
				ItemEightGates.block,
				ItemByakugan.helmet,
				ItemSharingan.helmet,
				ItemIryoJutsu.block,
				ItemMangekyoSharingan.helmet,
				ItemMangekyoSharinganObito.helmet,
				ItemMangekyoSharinganEternal.helmet,
				ItemRinnegan.helmet,
				ItemTenseigan.helmet,
				ItemKekkeiMora.block,
				ItemGourd.body,
				ItemShoton.block,
		};

		public boolean usable = true;

		public static void resetAffinity(EntityPlayer player) {
			for (Item item : jutsuArray) {
				ItemStack stack = ProcedureUtils.getMatchingItemStack(player, item);
				if (stack != null) {
					((ItemJutsu.Base)stack.getItem()).setIsAffinity(stack,false);
				}
			}
		}

		public static void destroyKG(EntityPlayer player) {
			player.getEntityData().removeTag("KekkeiGenkai");
			player.getEntityData().setBoolean("kgReceived",false);
			for (Item item : kgArray) {
				ItemStack stack = ProcedureUtils.getMatchingItemStack(player, item);
				if (stack != null) {
					((stack)).shrink(1);
				}
			}
		}

		public static void giveJutsu(Item item, EntityPlayer player) {
			ItemStack stack = ProcedureUtils.getMatchingItemStack(player, item);
			if (stack == null) {
				stack = new ItemStack(item, 1);
				ItemHandlerHelper.giveItemToPlayer(player, stack);
				stack.setCount(1);
			}
			if (item == ItemJiton.block) {
				giveJutsu(ItemGourd.body,player);
			}
			if (item != ItemEightGates.block && item != ItemByakugan.helmet && item != ItemSharingan.helmet && item != ItemGourd.body) {
				((ItemJutsu.Base)stack.getItem()).setIsAffinity(stack,true);
				((ItemJutsu.Base)stack.getItem()).setOwner(stack, player);
			}
			if (item == ItemByakugan.helmet || item == ItemSharingan.helmet) {
				((ItemDojutsu.Base)stack.getItem()).setOwner(stack, player);
			}
		}

		public static int randNum(EntityPlayer player, int num) {
			int num2 = player.world.rand.nextInt(5);
			if (num2 == num) {
				return randNum(player,num);
			}
			return num2;
		}
		@Override
		protected void handleButtonAction(EntityPlayer player, int buttonID) {
			// security measure to prevent arbitrary chunk generation
			if (player.world.isRemote || !player.world.isBlockLoaded(new BlockPos(this.x, this.y, this.z)))
				return;
			if (!this.usable) {
				return;
			}
			this.usable = false;
			//ItemStack stack = GuiNinjaScroll.enableJutsu(player, (ItemNinjutsu.RangedItem)ItemNinjutsu.block, ItemNinjutsu.RASENGAN, true);
			destroyKG(player);
			resetAffinity(player);
			int realNum = buttonID;
			if (buttonID == 0) {
				//boil
				giveJutsu(ItemKaton.block, player);
				giveJutsu(ItemSuiton.block, player);
			} else if (buttonID == 1) {
				//bone
				int num1 = player.world.rand.nextInt(5);
				int num2 = randNum(player,num1);
				giveJutsu(jutsuArray[num1], player);
				giveJutsu(jutsuArray[num2], player);
			} else if (buttonID == 2) {
				//dust
				giveJutsu(ItemDoton.block, player);
				giveJutsu(ItemFuton.block, player);
				giveJutsu(ItemKaton.block, player);
			} else if (buttonID == 3) {
				//explosion
				giveJutsu(ItemDoton.block, player);
				giveJutsu(ItemRaiton.block, player);
			} else if (buttonID == 4) {
				//ice
				giveJutsu(ItemSuiton.block, player);
				giveJutsu(ItemFuton.block, player);
			} else if (buttonID == 5) {
				//lava
				giveJutsu(ItemDoton.block, player);
				giveJutsu(ItemKaton.block, player);
			} else if (buttonID == 6) {
				//MAGNET
				giveJutsu(ItemDoton.block, player);
				giveJutsu(ItemFuton.block, player);
			} else if (buttonID == 7) {
				//scorch
				giveJutsu(ItemKaton.block, player);
				giveJutsu(ItemFuton.block, player);
			} else if (buttonID == 8) {
				//storm
				giveJutsu(ItemRaiton.block, player);
				giveJutsu(ItemSuiton.block, player);
			} else if (buttonID == 9) {
				//wood
				giveJutsu(ItemDoton.block, player);
				giveJutsu(ItemSuiton.block, player);
			} else if (buttonID == 10) {
				//gates
				int num1 = player.world.rand.nextInt(5);
				int num2 = randNum(player,num1);
				giveJutsu(jutsuArray[num1], player);
				giveJutsu(jutsuArray[num2], player);
			} else if (buttonID == 11) {
				//hyuga
				giveJutsu(ItemFuton.block, player);
				int num2 = randNum(player,1);
				giveJutsu(jutsuArray[num2], player);
			} else if (buttonID == 12) {
				//uchiha
				giveJutsu(ItemKaton.block, player);
				int num2 = randNum(player,0);
				giveJutsu(jutsuArray[num2], player);
			} else if (buttonID == 13) {
				//Medical
				int num1 = player.world.rand.nextInt(5);
				int num2 = randNum(player,num1);
				giveJutsu(jutsuArray[num1], player);
				giveJutsu(jutsuArray[num2], player);
				giveJutsu(ItemIryoJutsu.block, player);
				player.getEntityData().setBoolean("kgReceived",true);
			} else if (buttonID == 14) {
				//Cube
				giveJutsu(ItemFuton.block, player);
				giveJutsu(ItemDoton.block, player);
			} else if (buttonID == 15) {
				//Wood
				giveJutsu(ItemRaiton.block, player);
				giveJutsu(ItemFuton.block, player);
			} else if (buttonID == 16) {
				//Akimichi
				int num1 = player.world.rand.nextInt(5);
				int num2 = randNum(player, num1);
				giveJutsu(jutsuArray[num1], player);
				giveJutsu(jutsuArray[num2], player);
				giveJutsu(ItemYoton.block, player);
			} else if (buttonID == 17) {
				//Nara
				int num1 = player.world.rand.nextInt(5);
				int num2 = randNum(player, num1);
				giveJutsu(jutsuArray[num1], player);
				giveJutsu(jutsuArray[num2], player);
				giveJutsu(ItemInton.block, player);
			} else if (buttonID == 18) {
				//Aburame
				int num1 = player.world.rand.nextInt(5);
				int num2 = randNum(player, num1);
				giveJutsu(jutsuArray[num1], player);
				giveJutsu(jutsuArray[num2], player);
				giveJutsu(ItemInton.block, player);
			} else if (buttonID == 19) {
				//Hiruzen
				giveJutsu(jutsuArray[0], player);
				giveJutsu(jutsuArray[1], player);
				giveJutsu(jutsuArray[2], player);
				giveJutsu(jutsuArray[3], player);
				giveJutsu(jutsuArray[4], player);
			} else if (buttonID == 20) {
				//Crystal
				giveJutsu(ItemDoton.block, player);
				int num1 = player.world.rand.nextInt(5);
				giveJutsu(jutsuArray[num1], player);
			} else if (buttonID == 21) {
				//Shikigami Paper
				int num1 = player.world.rand.nextInt(5);
				int num2 = randNum(player, num1);
				giveJutsu(jutsuArray[num1], player);
				giveJutsu(jutsuArray[num2], player);
			}
			player.getEntityData().setInteger("KekkeiGenkai", realNum);

			super.handleButtonAction(player, buttonID);

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
			this.fontRenderer.drawString("Select Your Car.", 38, 13, -16777216);
			//this.buttonList.clear();
			this.buttonList.remove(0);
			this.buttonList.add(new GuiButton(0, this.guiLeft + 0, this.guiTop + 25, 39, 20, "Boil"));
			this.buttonList.add(new GuiButton(1, this.guiLeft + 50, this.guiTop + 25, 39, 20, "Bone"));
			this.buttonList.add(new GuiButton(2, this.guiLeft + 100, this.guiTop + 25, 39, 20, "Dust"));
			this.buttonList.add(new GuiButton(3, this.guiLeft + 150, this.guiTop + 25, 39, 20, "Explosion"));
			this.buttonList.add(new GuiButton(4, this.guiLeft + 200, this.guiTop + 25, 39, 20, "Ice"));
			this.buttonList.add(new GuiButton(5, this.guiLeft + 0, this.guiTop + 50, 39, 20, "Lava"));
			this.buttonList.add(new GuiButton(6, this.guiLeft + 50, this.guiTop + 50, 39, 20, "Magnet"));
			this.buttonList.add(new GuiButton(7, this.guiLeft + 100, this.guiTop + 50, 39, 20, "Scorch"));
			this.buttonList.add(new GuiButton(8, this.guiLeft + 150, this.guiTop + 50, 39, 20, "Storm"));
			this.buttonList.add(new GuiButton(9, this.guiLeft + 200, this.guiTop + 50, 39, 20, "codyblade"));
			this.buttonList.add(new GuiButton(10, this.guiLeft + 0, this.guiTop + 75, 39, 20, "Gates"));
			this.buttonList.add(new GuiButton(11, this.guiLeft + 50, this.guiTop + 75, 39, 20, "Hyuga"));
			this.buttonList.add(new GuiButton(12, this.guiLeft + 100, this.guiTop + 75, 39, 20, "Uchiha"));
			this.buttonList.add(new GuiButton(13, this.guiLeft + 150, this.guiTop + 75, 39, 20, "Medical"));
			this.buttonList.add(new GuiButton(14, this.guiLeft + 200, this.guiTop + 75, 39, 20, "loosenedcube"));
			this.buttonList.add(new GuiButton(15, this.guiLeft + 0, this.guiTop + 100, 39, 20, "Flying Raijin"));
			this.buttonList.add(new GuiButton(16, this.guiLeft + 50, this.guiTop + 100, 39, 20, "Akimichi"));
			this.buttonList.add(new GuiButton(17, this.guiLeft + 100, this.guiTop + 100, 39, 20, "Nara"));
			this.buttonList.add(new GuiButton(18, this.guiLeft + 200, this.guiTop + 100, 39, 20, "Aburame"));
			this.buttonList.add(new GuiButton(19, this.guiLeft + 0, this.guiTop + 125, 39, 20, "Hiruzen"));
			this.buttonList.add(new GuiButton(20, this.guiLeft + 50, this.guiTop + 125, 39, 20, "Crystal"));
			this.buttonList.add(new GuiButton(21, this.guiLeft + 100, this.guiTop + 125, 39, 20, "Paper"));
		}
	}
}
