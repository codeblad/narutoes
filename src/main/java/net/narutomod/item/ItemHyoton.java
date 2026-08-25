
package net.narutomod.item;

import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.items.ItemHandlerHelper;

import net.minecraft.world.World;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.Entity;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.EnchantmentFrostWalker;
import net.minecraft.nbt.NBTTagCompound;

import net.narutomod.Particles;
import net.narutomod.entity.EntityRendererRegister;
import net.narutomod.entity.EntitySpike;
import net.narutomod.entity.EntityIceSpear;
import net.narutomod.entity.EntityIceDome;
import net.narutomod.entity.EntityIcePrison;
import net.narutomod.procedure.ProcedureUtils;
import net.narutomod.creativetab.TabModTab;
import net.narutomod.EntityTracker;
import net.narutomod.ElementsNarutomodMod;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@ElementsNarutomodMod.ModElement.Tag
public class ItemHyoton extends ElementsNarutomodMod.ModElement {
	@GameRegistry.ObjectHolder("narutomod:hyoton")
	public static final Item block = null;
	public static final int ENTITYID = 219;
	public static final ItemJutsu.JutsuEnum KILLSPIKES = new ItemJutsu.JutsuEnum(0, "ice_spike", 'S', 150, 30d, new EntityIceSpike.Jutsu());
	public static final ItemJutsu.JutsuEnum ICESPEARS = new ItemJutsu.JutsuEnum(1, "ice_spear", 'S', 150, 30d, new EntityIceSpear.EC.Jutsu());
	public static final ItemJutsu.JutsuEnum ICEDOME = new ItemJutsu.JutsuEnum(2, "ice_dome", 'S', 200, 600d, new EntityIceDome.EC.Jutsu());
	public static final ItemJutsu.JutsuEnum ICEPRISON = new ItemJutsu.JutsuEnum(3, "ice_prison", 'S', 150, 150d, new EntityIcePrison.EC.Jutsu());

	public ItemHyoton(ElementsNarutomodMod instance) {
		super(instance, 531);
	}

	@Override
	public void initElements() {
		elements.items.add(() -> new RangedItem(KILLSPIKES, ICESPEARS, ICEDOME, ICEPRISON));
		elements.entities.add(() -> EntityEntryBuilder.create().entity(EntityIceSpike.class)
		 .id(new ResourceLocation("narutomod", "ice_spike"), ENTITYID).name("ice_spike").tracker(64, 1, true).build());
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerModels(ModelRegistryEvent event) {
		ModelLoader.setCustomModelResourceLocation(block, 0, new ModelResourceLocation("narutomod:hyoton", "inventory"));
	}

	@Override
	public void init(FMLInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(new RangedItem.DamageHook());
	}

	public static class RangedItem extends ItemJutsu.Base {
		public RangedItem(ItemJutsu.JutsuEnum... list) {
			super(ItemJutsu.JutsuEnum.Type.HYOTON, list);
			setUnlocalizedName("hyoton");
			setRegistryName("hyoton");
			setCreativeTab(TabModTab.tab);
			this.defaultCooldownMap[KILLSPIKES.index] = 0;
			this.defaultCooldownMap[ICESPEARS.index] = 0;
			this.defaultCooldownMap[ICEDOME.index] = 0;
			this.defaultCooldownMap[ICEPRISON.index] = 0;
		}

		@Override
		public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer entity, EnumHand hand) {
			if (entity.isCreative() || (ProcedureUtils.hasItemInInventory(entity, ItemFuton.block) 
			 && ProcedureUtils.hasItemInInventory(entity, ItemSuiton.block))) { 
				return super.onItemRightClick(world, entity, hand);
			}
			return new ActionResult<ItemStack>(EnumActionResult.FAIL, entity.getHeldItem(hand));
		}

		private void setlastTickPos(Entity entity, BlockPos pos) {
			NBTTagCompound compound = entity.getEntityData().getCompoundTag("lastTickBlockPos");
			if (compound == null) {
				compound = new NBTTagCompound();
			}
			compound.setInteger("X", pos.getX());
			compound.setInteger("Y", pos.getY());
			compound.setInteger("Z", pos.getZ());
			entity.getEntityData().setTag("lastTickBlockPos", compound);
		}

		private BlockPos getLastTickPos(Entity entity) {
			NBTTagCompound compound = entity.getEntityData().getCompoundTag("lastTickBlockPos");
			return compound != null
			 ? new BlockPos(compound.getInteger("X"), compound.getInteger("Y"), compound.getInteger("Z"))
			 : BlockPos.ORIGIN;
		}

		@Override
		public void onUpdate(ItemStack itemstack, World world, Entity entity, int par4, boolean par5) {
			super.onUpdate(itemstack, world, entity, par4, par5);
			if (!world.isRemote && entity instanceof EntityLivingBase) {
				//BlockPos pos = new BlockPos(entity);
				//EntityTracker.SessionDataHolder edh = EntityTracker.getOrCreate(entity);
				BlockPos pos = entity.getPosition();
				EntityLivingBase living = (EntityLivingBase)entity;
				living.extinguish();
				//if (!pos.equals(edh.prevBlockPos)) {
				//	edh.prevBlockPos = pos;
				
				ItemStack stacksenbon = ProcedureUtils.getMatchingItemStack((EntityPlayer) entity, ItemIceSenbon.block);
				if (stacksenbon != null && (living.getHeldItemMainhand().equals(itemstack) || living.getHeldItemOffhand().equals(itemstack)) &&  !pos.equals(this.getLastTickPos(living)) ) {
					this.setlastTickPos(entity, pos);
					EnchantmentFrostWalker.freezeNearby(living, world, pos, 1);
				}
				if (living.ticksExisted % 20 == 3) {
					//living.addPotionEffect(new PotionEffect(MobEffects.SPEED, 22, 4, false, false));
					if (entity instanceof EntityPlayer && !ProcedureUtils.hasItemInInventory((EntityPlayer)entity, ItemIceSenbon.block)) {
						ItemHandlerHelper.giveItemToPlayer((EntityPlayer)entity, new ItemStack(ItemIceSenbon.block));
					}
				}
			}
		}

		@Override
		public void addInformation(ItemStack itemstack, World world, List<String> list, ITooltipFlag flag) {
			super.addInformation(itemstack, world, list, flag);
			list.add(TextFormatting.GREEN + net.minecraft.util.text.translation.I18n.translateToLocal("tooltip.hyoton.musthave") + TextFormatting.RESET);
		}

		public static class DamageHook {
			@SubscribeEvent
			public void onDamage(LivingAttackEvent event) {
				EntityLivingBase entity = event.getEntityLiving();
				DamageSource source = event.getSource();
				if (source == DamageSource.IN_WALL && entity.isInsideOfMaterial(Material.ICE)
				 && entity instanceof EntityPlayer && ProcedureUtils.hasItemInInventory((EntityPlayer)entity, block)) {
					event.setCanceled(true);
				}
			}
		}
	}

	public static class IceSphere extends Entity  {
		private EntityLivingBase user;
		private Vec3d look;
		private Vec3d start;
		private int lifeTime = 20;
		private double power;
		List<String> targets = new ArrayList<String>();
		private RayTraceResult rtr;

		public IceSphere(World worldIn) {
			super(worldIn);
			this.setSize(0.01f, 0.01f);
			this.isImmuneToFire = true;
		}


		public IceSphere(EntityLivingBase user, double power, RayTraceResult rtr) {
			this(user.world);
			this.user = user;
			this.look = this.user.getLookVec();
			this.start = this.user.getPositionVector().addVector(0,1,0);
			this.setPosition(this.start.x,this.start.y,this.start.z);
			this.lifeTime = 50;
			this.power = power;
			this.rtr = rtr;
			double distance = this.getDistance(rtr.hitVec.x,rtr.hitVec.y,rtr.hitVec.z);
			for (double i = 0;i < distance; i+=0.5) {
				Vec3d point = this.start.add(this.look.scale(i));
				Particles.spawnParticle(this.world, Particles.Types.SMOKE, point.x, point.y, point.z,
						1, 0, 0d, 0, 0,0,0, 0x64B8F7FF, 30, 0);
				Particles.spawnParticle(this.world, Particles.Types.SMOKE, point.x, point.y, point.z,
						1, 0, 0d, 0, 0,0,0, 0xFFFFFFFF, 10, 0);
			}
		}


		@Override
		protected void entityInit() {
		}

		@Override
		public void onUpdate() {
			if (this.user != null && !this.world.isRemote) {
				if (this.ticksExisted == 5) {
					this.world.playSound(null, this.rtr.hitVec.x, this.rtr.hitVec.y, this.rtr.hitVec.z,
							net.minecraft.util.SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:spiked")),
							net.minecraft.util.SoundCategory.NEUTRAL, 5f, this.user.getRNG().nextFloat() * 0.4f + 0.8f);
					for (int i = 0; i < 60; i++) {
						EntityIceSpike entity1 = new EntityIceSpike(this.user);
						entity1.damage = 0;
						Vec3d vec = this.rtr.hitVec;
						entity1.setNoGravity(true);
						entity1.maxScale = (float) (1+this.power/5);
						entity1.setLocationAndAngles(vec.x, vec.y, vec.z, this.user.getRNG().nextFloat() * 360f, this.user.getRNG().nextFloat() * 360f);
						entity1.life = this.lifeTime;
						this.world.spawnEntity(entity1);
					}
					int smokeSize = (int) (15+this.power*2);
					for (int i = 0; i < 120; i++) {
						Vec3d point = this.rtr.hitVec.addVector(-this.power/2+this.rand.nextFloat()*this.power,-this.power/2+this.rand.nextFloat()*this.power,-this.power/2+this.rand.nextFloat()*this.power);
						Particles.spawnParticle(this.world, Particles.Types.SMOKE, point.x, point.y, point.z,
								1, 1d, 0d, 1d, 0,0,0, 0x64B8F7FF, smokeSize, 0);
					}
					AxisAlignedBB hitbox = new AxisAlignedBB(new BlockPos(this.rtr.hitVec)).grow(1+this.power*0.3f);
					for (Entity entity1 : this.world.getEntitiesWithinAABBExcludingEntity(this.user, hitbox)) {
						if (!(entity1 instanceof EntityLivingBase)) {
							continue;
						}
						boolean found = false;
						for (String enemy: this.targets) {
							if (Objects.equals(enemy, entity1.getUniqueID().toString())) {
								found = true;
							}
						}
						if (found) {
							continue;
						}
						this.targets.add(entity1.getUniqueID().toString());
						float damage = (float) (15+(5f*ItemJutsu.getDmgMult(this.user)*(0.75+2*this.power/25)));
						entity1.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, this.user),damage);

					}
				}
			}
			if (this.ticksExisted > this.lifeTime) {
				this.playSound(SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:ice_shoot_small")),
						1.2f, this.rand.nextFloat() * 0.4f + 0.8f);
				for (int i = 0; i < 20+this.power*2; i++) {
					Vec3d point = this.rtr.hitVec.addVector(-this.power/2+this.rand.nextFloat()*this.power,-this.power/2+this.rand.nextFloat()*this.power,-this.power/2+this.rand.nextFloat()*this.power);
					EntityIceSpear.EC shard = EntityIceSpear.EC.spawnShatteredShard(this.world, point.x,point.y,point.z,
							(this.rand.nextDouble()-0.5d) * 0.05d, 0d, (this.rand.nextDouble()-0.5d) * 0.05d);
					shard.baseImpactDamage = 0;
				}
				this.setDead();
			}
		}

		@Override
		protected void readEntityFromNBT(NBTTagCompound compound) {
		}

		@Override
		protected void writeEntityToNBT(NBTTagCompound compound) {
		}

	}

	public static class EntityIceSpike extends EntitySpike.Base implements ItemJutsu.IJutsu {
		public int growTime = 5;
		public float maxScale = 3.0f;
		public float damage = 20.0f;
		public int life = 50;
		private EntityLivingBase user;

		public EntityIceSpike(World worldIn) {
			super(worldIn);
			this.setColor(0xC0FFFFFF);
			this.isImmuneToFire = true;
		}

		public EntityIceSpike(EntityLivingBase userIn) {
			this(userIn.world);
			this.user = userIn;
		}

		@Override
		public ItemJutsu.JutsuEnum.Type getJutsuType() {
			return ItemJutsu.JutsuEnum.Type.HYOTON;
		}

		@Override
		public void onUpdate() {
			super.onUpdate();
			if (!this.world.isRemote && this.ticksExisted > this.life) {
				this.setDead();
			}
			if (!this.world.isRemote && this.ticksAlive <= this.growTime) {
				this.setEntityScale(MathHelper.clamp(this.maxScale * (float)this.ticksAlive / this.growTime, 0.0f, this.maxScale));
				if (this.damage > 0) {
					for (EntityLivingBase entity :
							this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().grow(1d, 0d, 1d))) {
						if (!entity.equals(this.user)) {
							entity.getEntityData().setBoolean("TempData_disableKnockback", true);
							entity.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, this.user),
									this.damage);
						}
					}
				}
			}
		}

		public static class Jutsu implements ItemJutsu.IJutsuCallback {
			@Override
			public boolean createJutsu(ItemStack stack, EntityLivingBase entity, float power) {
				Vec3d vec3d = entity.getPositionEyes(1f);
				RayTraceResult rtr = ProcedureUtils.objectEntityLookingAt(entity, 30);

				entity.world.spawnEntity(new IceSphere(entity,power,rtr));
				ItemJutsu.setCurrentJutsuCooldown(stack,20*5);
				return true;
			}

			@Override
			public float getPowerupDelay() {
				return 10f;
			}
	
			@Override
			public float getMaxPower() {
				return 25.0f;
			}
		}
	}

	@Override
	public void preInit(FMLPreInitializationEvent event) {
		new Renderer().register();
	}

	public static class Renderer extends EntityRendererRegister {
		@SideOnly(Side.CLIENT)
		@Override
		public void register() {
			RenderingRegistry.registerEntityRenderingHandler(EntityIceSpike.class, renderManager -> new CustomRender(renderManager));
		}

		@SideOnly(Side.CLIENT)
		public class CustomRender extends EntitySpike.ClientSide.Renderer<EntityIceSpike> {
			private final ResourceLocation texture = new ResourceLocation("narutomod:textures/spike_ice.png");
	
			public CustomRender(RenderManager renderManagerIn) {
				super(renderManagerIn);
			}
	
			@Override
			protected ResourceLocation getEntityTexture(EntityIceSpike entity) {
				return this.texture;
			}
		}
	}
}
