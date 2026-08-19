
package net.narutomod.item;

import com.google.common.collect.Multimap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Enchantments;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.narutomod.Chakra;
import net.narutomod.ElementsNarutomodMod;
import net.narutomod.creativetab.TabModTab;
import net.narutomod.entity.EntityRendererRegister;
import net.narutomod.procedure.ProcedureUtils;

@ElementsNarutomodMod.ModElement.Tag
public class ItemEyeScooper extends ElementsNarutomodMod.ModElement {
	@GameRegistry.ObjectHolder("narutomod:eye_scooper")
	public static final Item block = null;
	public static final int ENTITYID = 20177;

	public ItemEyeScooper(ElementsNarutomodMod instance) {
		super(instance, 20177);
	}

	@Override
	public void initElements() {
		elements.items.add(() -> new RangedItem());
		}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerModels(ModelRegistryEvent event) {
		ModelLoader.setCustomModelResourceLocation(block, 0, new ModelResourceLocation("narutomod:eye_scooper", "inventory"));
	}


	public static class RangedItem extends Item {
		public RangedItem() {
			super();
			setMaxDamage(0);
			setUnlocalizedName("eye_scooper");
			setRegistryName("eye_scooper");
			setMaxStackSize(1);
			setCreativeTab(TabModTab.tab);
		}


		public static Item[] scoopArray = {
				ItemByakugan.helmet,
				ItemSharingan.helmet,
				ItemMangekyoSharingan.helmet,
				ItemMangekyoSharinganObito.helmet,
		};

		@Override
		public void onPlayerStoppedUsing(ItemStack itemstack, World world, EntityLivingBase entityLivingBase, int timeLeft) {

			if (timeLeft > 0) {
				//return;
			}

		}

		@Override
		public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
			super.onUpdate(stack, worldIn, entityIn, itemSlot, isSelected);

		}


		@Override
		public ItemStack onItemUseFinish(ItemStack itemstack, World world, EntityLivingBase entityLivingBase) {
			if (!world.isRemote && entityLivingBase instanceof EntityPlayerMP) {
				RayTraceResult result = ProcedureUtils.objectEntityLookingAt(entityLivingBase,6,3);
				if (result.entityHit instanceof  EntityPlayerMP) {
					EntityPlayerMP entity = (EntityPlayerMP) result.entityHit;
					if (entity.getHealth() < entity.getMaxHealth()*0.1f) {
						ItemStack stack = ProcedureUtils.getMatchingItemStack(entity, ItemByakugan.helmet);
						if (stack != null) {
							(entity).dropItem(stack.copy(), true, true);
						}

						stack = ProcedureUtils.getMatchingItemStack(entity, ItemSharingan.helmet);
						if (stack != null) {
							(entity).dropItem(stack.copy(), true, true);
						}


						stack = ProcedureUtils.getMatchingItemStack(entity, ItemMangekyoSharingan.helmet);
						if (stack != null) {
							(entity).dropItem(stack.copy(), true, true);
						}

						stack = ProcedureUtils.getMatchingItemStack(entity, ItemMangekyoSharinganObito.helmet);
						if (stack != null) {
							(entity).dropItem(stack.copy(), true, true);
						}

						entity.inventory.clearMatchingItems(new ItemStack(ItemByakugan.helmet, (int) (1)).getItem(), -1, (int) (-1),
								null);
						entity.inventory.clearMatchingItems(new ItemStack(ItemSharingan.helmet, (int) (1)).getItem(), -1, (int) (-1),
								null);
						entity.inventory.clearMatchingItems(new ItemStack(ItemMangekyoSharingan.helmet, (int) (1)).getItem(), -1, (int) (-1),
								null);
						entity.inventory.clearMatchingItems(new ItemStack(ItemMangekyoSharinganObito.helmet, (int) (1)).getItem(), -1, (int) (-1),
								null);
					}

				}
			}

			return super.onItemUseFinish(itemstack, world, entityLivingBase);
		}

		@Override
		public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer entity, EnumHand hand) {
			entity.setActiveHand(hand);
			return new ActionResult(EnumActionResult.SUCCESS, entity.getHeldItem(hand));
		}

		@Override
		public EnumAction getItemUseAction(ItemStack itemstack) {
			return EnumAction.BOW;
		}

		@Override
		public int getMaxItemUseDuration(ItemStack itemstack) {
			return 20*5;
		}

	}


}
