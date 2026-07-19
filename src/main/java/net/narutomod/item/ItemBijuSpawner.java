
package net.narutomod.item;

import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.village.Village;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.event.ModelRegistryEvent;

import net.minecraft.world.World;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.item.EnumAction;
import net.minecraft.init.Enchantments;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.Entity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.client.renderer.entity.RenderSnowball;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.Vec3d;

import net.narutomod.NarutomodModVariables;
import net.narutomod.entity.EntityBijuManager;
import net.narutomod.entity.EntitySusanooBase;
import net.narutomod.procedure.ProcedureShurikenBulletHitsBlock;
import net.narutomod.creativetab.TabModTab;
import net.narutomod.ElementsNarutomodMod;
import net.narutomod.procedure.ProcedureUtils;

import java.util.Map;
import java.util.HashMap;

@ElementsNarutomodMod.ModElement.Tag
public class ItemBijuSpawner extends ElementsNarutomodMod.ModElement {
	@GameRegistry.ObjectHolder("narutomod:bijuspawner")
	public static final Item block = null;
	public static final int ENTITYID = 11355;

	public ItemBijuSpawner(ElementsNarutomodMod instance) {
		super(instance, 32455);
	}

	@Override
	public void initElements() {
		elements.items.add(() -> new RangedItem());
		elements.entities.add(() -> EntityEntryBuilder.create().entity(EntityArrowCustom.class)
				.id(new ResourceLocation("narutomod", "bijuspawner"), ENTITYID).name("bijuspawner").tracker(64, 1, true).build());
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerModels(ModelRegistryEvent event) {
		ModelLoader.setCustomModelResourceLocation(block, 0, new ModelResourceLocation("narutomod:scroll_selectkg", "inventory"));
	}


	@SideOnly(Side.CLIENT)
	@Override
	public void preInit(FMLPreInitializationEvent event) {
		RenderingRegistry.registerEntityRenderingHandler(EntityArrowCustom.class, renderManager -> {
			return new RenderSnowball(renderManager, new ItemStack(ItemShuriken.block, (int) (1)).getItem(),
					Minecraft.getMinecraft().getRenderItem());
		});
	}

	public static class RangedItem extends Item implements ItemOnBody.Interface {
		public RangedItem() {
			super();
			setMaxDamage(0);
			setFull3D();
			setUnlocalizedName("bijuspawner");
			setRegistryName("bijuspawner");
			maxStackSize = 1;
			setCreativeTab(TabModTab.tab);
		}

		@Override
		public void onPlayerStoppedUsing(ItemStack itemstack, World world, EntityLivingBase entityLivingBase, int timeLeft) {
			if (!world.isRemote && entityLivingBase instanceof EntityPlayerMP) {
				EntityPlayerMP entity = (EntityPlayerMP) entityLivingBase;
				Vec3d vec = entity.getLookVec();
				Village village = entity.world.getVillageCollection().getNearestVillage(new BlockPos(entity.getPosition()), 32);
				if (village == null || village.getNumVillageDoors() < 5 || village.getNumVillagers() < 4
				) {

					entity.sendStatusMessage(new TextComponentString("not a valid village"), true);
					return;
				} else {
					double posX = Math.ceil(entity.posX);
					double posY = Math.ceil(entity.posY);
					double posZ = Math.ceil(entity.posZ);

					final EntityBijuManager bm = EntityBijuManager.getRandomAvailableBiju();
					final EntityBijuManager abm = EntityBijuManager.anyBijuAddedToWorld();
					if (abm == null) {
						Entity beast = bm.getEntity();
						beast = bm.spawnEntity(world,posX+20,posY,posZ,0);
						beast.setPosition(posX,posY+20,posZ);

						String pos = posX+", "+posY+", "+posZ;
						ProcedureUtils.sendChatAll(I18n.translateToLocalFormatted("chattext.tailedbeast.arrival", bm.getTails(), pos));

						if (!entity.capabilities.isCreativeMode) {
							itemstack.shrink(1);
						}
					} else if (abm != null) {
						Entity beast = abm.getEntity();
						BlockPos pos = abm.getPosition();
						String abc = pos.getX()+" "+pos.getY()+" "+pos.getZ();
						entity.sendStatusMessage(new TextComponentString("The biju is already at "+abc), true);
					}
				}

			}
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
			return 72000;
		}

		@Override
		public ItemOnBody.BodyPart showOnBody() {
			return ItemOnBody.BodyPart.LEFT_LEG;
		}
	}

	public static class EntityArrowCustom extends EntityTippedArrow {
		public EntityArrowCustom(World a) {
			super(a);
		}

		public EntityArrowCustom(World worldIn, double x, double y, double z) {
			super(worldIn, x, y, z);
		}

		public EntityArrowCustom(World worldIn, EntityLivingBase shooter) {
			super(worldIn, shooter);
		}

		@Override
		protected void arrowHit(EntityLivingBase entity) {
			super.arrowHit(entity);
			entity.setArrowCountInEntity(entity.getArrowCountInEntity() - 1);
		}

		@Override
		public void onUpdate() {
			super.onUpdate();
			int x = (int) this.posX;
			int y = (int) this.posY;
			int z = (int) this.posZ;
			World world = this.world;
			Entity entity = (Entity) shootingEntity;
			if (this.inGround) {
				{
					Map<String, Object> $_dependencies = new HashMap<>();
					$_dependencies.put("entity", this);
					$_dependencies.put("x", x);
					$_dependencies.put("y", y);
					$_dependencies.put("z", z);
					$_dependencies.put("world", world);
					ProcedureShurikenBulletHitsBlock.executeProcedure($_dependencies);
				}
				this.world.removeEntity(this);
			} else if (this.ticksExisted % 2 == 0) {
				this.world.playSound(null, this.posX, this.posY, this.posZ,
						SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:shuriken1")),
						SoundCategory.NEUTRAL, 0.5f, 1f / (this.rand.nextFloat() * 0.5f + 1f) + 0.4f);
			}
		}
	}
}
