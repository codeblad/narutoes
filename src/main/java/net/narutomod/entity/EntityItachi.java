
package net.narutomod.entity;

import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
//import net.minecraftforge.common.DungeonHooks;

import net.minecraft.init.MobEffects;
import net.minecraft.init.Biomes;
import net.minecraft.init.SoundEvents;
import net.minecraft.world.World;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.BossInfo;
import net.minecraft.world.BossInfoServer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumHand;
import net.minecraft.item.ItemStack;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityAIWatchClosest2;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.Entity;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.Minecraft;
import net.minecraft.potion.PotionEffect;
import net.minecraft.nbt.NBTTagCompound;

import net.narutomod.potion.PotionAmaterasuFlame;
import net.narutomod.potion.PotionParalysis;
import net.narutomod.entity.EntitySusanooClothed;
import net.narutomod.entity.EntityCrow;
import net.narutomod.procedure.ProcedureUtils;
import net.narutomod.procedure.ProcedureBasicNinjaSkills;
import net.narutomod.procedure.ProcedureSync;
import net.narutomod.item.ItemSharingan;
import net.narutomod.item.ItemMangekyoSharingan;
import net.narutomod.item.ItemInton;
import net.narutomod.item.ItemKunai;
import net.narutomod.item.ItemKaton;
import net.narutomod.item.ItemAkatsukiRobe;
import net.narutomod.ModConfig;
import net.narutomod.ElementsNarutomodMod;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import javax.annotation.Nullable;

@ElementsNarutomodMod.ModElement.Tag
public class EntityItachi extends ElementsNarutomodMod.ModElement {
	public static final int ENTITYID = 117;
	public static final int ENTITYID_RANGED = 118;

	public EntityItachi(ElementsNarutomodMod instance) {
		super(instance, 336);
	}

	@Override
	public void initElements() {
		elements.entities.add(() -> EntityEntryBuilder.create().entity(EntityCustom.class)
				.id(new ResourceLocation("narutomod", "itachi"), ENTITYID)
				.name("itachi").tracker(64, 3, true).egg(-16777216, -65485).build());
		elements.entities.add(() -> EntityEntryBuilder.create().entity(Entity4MobAppearance.class)
				.id(new ResourceLocation("narutomod", "itachi_mob_appearance"), ENTITYID_RANGED)
				.name("itachi_mob_appearance").tracker(64, 1, true).build());
	}

	@Override
	public void init(FMLInitializationEvent event) {
		int i = MathHelper.clamp(ModConfig.SPAWN_WEIGHT_ITACHI, 0, 20);
		if (i > 0) {
			EntityRegistry.addSpawn(EntityCustom.class, i, 1, 1, EnumCreatureType.MONSTER,
					Biomes.FOREST, Biomes.TAIGA, Biomes.SWAMPLAND, Biomes.RIVER, Biomes.FOREST_HILLS,
					Biomes.TAIGA_HILLS, Biomes.JUNGLE, Biomes.JUNGLE_HILLS, Biomes.BIRCH_FOREST,
					Biomes.BIRCH_FOREST_HILLS, Biomes.ROOFED_FOREST, Biomes.SAVANNA, Biomes.EXTREME_HILLS,
					Biomes.MUTATED_FOREST, Biomes.MUTATED_TAIGA, Biomes.MUTATED_SWAMPLAND, Biomes.MUTATED_JUNGLE,
					Biomes.MUTATED_JUNGLE_EDGE, Biomes.MUTATED_BIRCH_FOREST, Biomes.MUTATED_BIRCH_FOREST_HILLS,
					Biomes.MUTATED_ROOFED_FOREST, Biomes.MUTATED_SAVANNA, Biomes.MUTATED_EXTREME_HILLS,
					Biomes.MUTATED_EXTREME_HILLS_WITH_TREES, Biomes.EXTREME_HILLS_WITH_TREES);
		}
	}

	public static class EntityCustom extends EntityNinjaMob.Base implements IMob, IRangedAttackMob {
		private final double GENJUTSU_CHAKRA = 100d;
		private final double FIREBALL_CHAKRA = 50d;
		private final double AMATERASU_CHAKRA = 50d;
		private final double SUSANOO_CHAKRA = 300d;
		private final double INVIS_CHAKRA = 20d;
		private static final int GENJUTSU_COOLDOWN = 100; // 5 seconds
		private boolean isReal;
		private int lookedAtTime;
		private final int genjutsuDuration = 120;
		private int lastGenjutsuTime;
		private final int invisCD = 200;
		private int lastInvisTime = -invisCD;
		private final int susanooCD = 600;
		private int lastSusanooTime = -susanooCD;
		private EntitySusanooClothed.EntityCustom susanooEntity;
		private int blockingTicks;

		private final BossInfoServer bossInfo = new BossInfoServer(this.getDisplayName(), BossInfo.Color.RED, BossInfo.Overlay.PROGRESS);

		public EntityCustom(World world) {
			super(world, 120, 7000d);
			//this.setItemToInventory(kunaiStack);
			this.isImmuneToFire = true;
			this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 10, true, false,
				Predicates.or(this.playerTargetSelectorAkatsuki, new Predicate<EntityPlayer>() {
					public boolean apply(@Nullable EntityPlayer p_apply_1_) {
						return p_apply_1_ != null && ItemSharingan.wearingAny(p_apply_1_);
					}
				})));
		}

		@Override
		public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
			ItemStack stack = new ItemStack(ItemMangekyoSharingan.helmet, 1);
			((ItemSharingan.Base)stack.getItem()).setOwner(stack, this);
			((ItemSharingan.Base)stack.getItem()).setColor(stack, 0x20ec1c24);
			this.setItemStackToSlot(EntityEquipmentSlot.HEAD, stack);
			this.setItemStackToSlot(EntityEquipmentSlot.CHEST, new ItemStack(ItemAkatsukiRobe.body, 1));
			this.setItemToInventory(new ItemStack(ItemKunai.block), 0);
			this.setItemToInventory(new ItemStack(ItemAkatsukiRobe.helmet), 1);
			this.setIsReal(this.rand.nextInt(ModConfig.ITACHI_REAL_CHANCE) == 0);
			return super.onInitialSpawn(difficulty, livingdata);
		}

		public void setIsReal(boolean real) {
			this.isReal = real;
		}

		@Override
		protected void applyEntityAttributes() {
			super.applyEntityAttributes();
			this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(100D);
			this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.5D);
			this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(10D);
		}

		@Override
		protected void initEntityAI() {
			super.initEntityAI();
			this.tasks.addTask(0, new EntityAISwimming(this));
			this.tasks.addTask(1, new EntityNinjaMob.AIAttackRangedTactical(this, 1.0D, 10, 50, 16.0F) {
				@Override
				public boolean shouldExecute() {
					return super.shouldExecute() && !EntityCustom.this.isSusanooActive();
							//&& EntityCustom.this.getAttackTarget().getDistance(EntityCustom.this) > 4d;
				}
			});
			this.tasks.addTask(2, new EntityNinjaMob.AILeapAtTarget(this, 1.0F) {
				@Override
				public boolean shouldExecute() {
					return super.shouldExecute() && !EntityCustom.this.isRiding()
							&& EntityCustom.this.getAttackTarget().posY - EntityCustom.this.posY > 3d;
				}
			});
			this.tasks.addTask(3, new EntityAIWatchClosest(this, null, 48.0F, 1.0F) {
				@Override
				public boolean shouldExecute() {
					if (EntityCustom.this.isRiding()
					 && EntityCustom.this.getAttackTarget() != null && EntityCustom.this.getAttackTarget().isEntityAlive()) {
						this.closestEntity = EntityCustom.this.getAttackTarget();
						return true;
					}
					return false;
				}
			});
			this.tasks.addTask(4, new EntityAIWatchClosest2(this, EntityPlayer.class, 15.0F, 1.0F));
			this.tasks.addTask(5, new EntityAIWander(this, 0.3));
			this.tasks.addTask(6, new EntityAILookIdle(this));
			this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
		}

		@Override
		protected void updateAITasks() {
			super.updateAITasks();
			EntityLivingBase target = this.getAttackTarget();
			if (target != null && target.isEntityAlive()) {
				if (this.isSusanooActive()) {
					this.susanooEntity.setAttackTarget(target);
				}
				if (this.lookedAtTime >= 5 && this.ticksExisted > this.lastGenjutsuTime + this.genjutsuDuration + GENJUTSU_COOLDOWN
				 && ((ItemInton.Genjutsu)ItemInton.GENJUTSU.jutsu).canTargetBeAffected(this, target) && this.consumeChakra(GENJUTSU_CHAKRA)) {
					if (target instanceof EntityPlayerMP) {
						ProcedureSync.MobAppearanceParticle.send((EntityPlayerMP)target, ENTITYID_RANGED);
					}
					this.world.playSound(null, target.posX, target.posY, target.posZ,
					 SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:genjutsu")), SoundCategory.NEUTRAL, 1f, 1f);
					if (!this.world.isRemote) {
						target.addPotionEffect(new PotionEffect(PotionParalysis.potion, this.genjutsuDuration, 1, false, false));
						target.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, this.genjutsuDuration+40, 0, false, true));
						target.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 60, 0, false, true));
					}
					this.lastGenjutsuTime = this.ticksExisted;
					this.lookedAtTime = 0;
				}
				if (this.equals(ProcedureUtils.objectEntityLookingAt(target, 24d).entityHit)) {
					++this.lookedAtTime;
				} else {
					this.lookedAtTime = 0;
				}
			} else {
				if (this.peacefulTicks > 200) {
					this.setAttackTarget(target = null);
				}
				if (this.isSusanooActive()) {
					this.susanooEntity.setDead();
				}
			}
			if ((this.getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem() == ItemAkatsukiRobe.helmet) != (target == null)) {
				this.swapWithInventory(EntityEquipmentSlot.HEAD, 1);
			}
		}

		@Override
		protected void dropLoot(boolean wasRecentlyHit, int lootingModifier, DamageSource source) {
			this.entityDropItem(new ItemStack(ItemKunai.block, 1), 0.0f);
			if (this.isReal) {
				ItemStack stack = this.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
				if (stack.getItem() != ItemMangekyoSharingan.helmet) {
					stack = this.getItemFromInventory(1);
				}
				if (stack.getItem() == ItemMangekyoSharingan.helmet) {
				 	((ItemSharingan.Base)stack.getItem()).forceDamage(stack, this.rand.nextInt(stack.getMaxDamage()));
					this.entityDropItem(stack, 0.0f);
				}
			}
		}

		@Override
		public SoundEvent getDeathSound() {
			return SoundEvents.ENTITY_ILLAGER_DEATH;
		}

		@Override
		public boolean isOnSameTeam(Entity entityIn) {
			return super.isOnSameTeam(entityIn) || EntityNinjaMob.TeamAkatsuki.contains(entityIn.getClass());
		}

		private boolean isSusanooActive() {
			return this.susanooEntity != null && this.susanooEntity.isEntityAlive();
		}

		@Override
		public boolean attackEntityFrom(DamageSource source, float amount) {
			if (source == DamageSource.FALL) {
				return false;
			}
			if ((float)this.hurtResistantTime > (float)this.maxHurtResistantTime / 2.0F && amount <= this.lastDamage) {
				return false;
			}
			Entity attacker = source.getTrueSource();
			if (!this.world.isRemote && !this.isAIDisabled() && source != ProcedureUtils.SPECIAL_DAMAGE && attacker instanceof EntityLivingBase) {
				boolean ret = true;
				if (this.rand.nextInt(3) != 0 && !source.isUnblockable()) {
					this.world.setEntityState(this, (byte)101);
					this.playSound(SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:ting")), 0.5f, this.rand.nextFloat() * 0.6f + 0.8f);
					ret = false;
				} else if (this.isReal && this.getHealth() > 0.0f && this.getHealth() - amount <= this.getMaxHealth() * 0.3333f
				 && !this.isRiding() && this.ticksExisted > this.lastSusanooTime + this.susanooCD && this.consumeChakra(SUSANOO_CHAKRA)) {
					this.susanooEntity = new EntitySusanooClothed.EntityCustom(this, false);
					this.susanooEntity.setLifeSpan(this.susanooCD);
					this.world.spawnEntity(this.susanooEntity);
					this.startRiding(this.susanooEntity);
					this.lastSusanooTime = this.ticksExisted;
					this.susanooEntity.attackEntityFrom(source, amount);
					ret = false;
				} else if (this.ticksExisted > this.lastInvisTime + this.invisCD && this.consumeChakra(INVIS_CHAKRA)) {
					this.addPotionEffect(new PotionEffect(MobEffects.INVISIBILITY, 100, 1, false, false));
					for (int i = 0; i < 100; i++) {
						Entity entityToSpawn = new EntityCrow.EntityCustom(this.world);
						entityToSpawn.setLocationAndAngles(this.posX, this.posY + 1.4, this.posZ, this.rand.nextFloat() * 360F, 0.0F);
						this.world.spawnEntity(entityToSpawn);
					}
					this.setPositionAndUpdate(this.posX + (this.rand.nextDouble() - 0.5) * 6, this.posY + 1, this.posZ + (this.rand.nextDouble() - 0.5) * 6);
					this.lastInvisTime = this.ticksExisted;
					ret = false;
				}
				if (!ret) {
					this.setRevengeTarget((EntityLivingBase)attacker);
					return false;
				}
			}
			return super.attackEntityFrom(source, amount);
		}

		@Override
		public boolean getCanSpawnHere() {
			return super.getCanSpawnHere()
			 && this.world.getEntities(EntityCustom.class, EntitySelectors.IS_ALIVE).isEmpty()
			 && !EntityNinjaMob.SpawnData.spawnedRecentlyHere(this, 36000);
			 //&& this.world.getEntitiesWithinAABB(EntityCustom.class, this.getEntityBoundingBox().grow(128.0D)).isEmpty();
			 //&& this.rand.nextInt(5) == 0;
		}

		@Override
		public void setSwingingArms(boolean swingingArms) {
		}

		@Override
		public void attackEntityWithRangedAttack(EntityLivingBase target, float distanceFactor) {
			int chance = this.rand.nextInt(12);
			if (this.getRidingEntity() instanceof EntitySusanooClothed.EntityCustom) {
				((EntitySusanooClothed.EntityCustom)this.getRidingEntity()).attackEntityWithRangedAttack(target, distanceFactor);
			} else {
				if (chance == 0 && distanceFactor > 0.3333f && this.consumeChakra(AMATERASU_CHAKRA)) {
					this.world.playSound(null, target.posX, target.posY, target.posZ, SoundEvent.REGISTRY
							.getObject(new ResourceLocation("narutomod:sharingansfx")), SoundCategory.NEUTRAL, 1f, 1f);
					PotionEffect effect = target.getActivePotionEffect(PotionAmaterasuFlame.potion);
					int amplifier = effect != null ? effect.getAmplifier() + 2 : 3;
					target.addPotionEffect(new PotionEffect(PotionAmaterasuFlame.potion, 60, amplifier, false, false));
				} else if (chance <= 2 && distanceFactor >= 0.5333f && this.consumeChakra(FIREBALL_CHAKRA)) {
					double d0 = target.posX - this.posX;
					double d1 = target.posY - (this.posY + this.getEyeHeight());
					double d2 = target.posZ - this.posZ;
					new ItemKaton.EntityBigFireball.Jutsu().createJutsu(this, d0, d1, d2, 5f, true);
				} else if (!this.isRiding()) {
					ItemKunai.EntityArrowCustom kunai = new ItemKunai.EntityArrowCustom(this.world, this);
					Vec3d vec = target.getPositionVector().addVector(0d, target.height * 0.333f, 0d).subtract(kunai.getPositionVector());
					kunai.shoot(vec.x, vec.y + MathHelper.sqrt(vec.x * vec.x + vec.z * vec.z) * 0.2d, vec.z, 1.6f, 0);
					kunai.setDamage(5);
					kunai.setKnockbackStrength(1);
					this.playSound(SoundEvents.ENTITY_ARROW_SHOOT, 1, 1f / (this.rand.nextFloat() * 0.5f + 1f) + 0.25f);
					this.world.spawnEntity(kunai);
				}
			}
		}

		@SideOnly(Side.CLIENT)
		@Override
		public void handleStatusUpdate(byte id) {
			if (id == 101) {
				this.blockingTicks = 10;
			} else {
				super.handleStatusUpdate(id);
			}
		}

		@Override
		public void removeTrackingPlayer(EntityPlayerMP player) {
			super.removeTrackingPlayer(player);

			if (this.bossInfo.getPlayers().contains(player)) {
				this.bossInfo.removePlayer(player);
			}
		}

		private void trackAttackedPlayers() {
			Entity entity = this.getAttackingEntity();
			if (entity instanceof EntityPlayerMP || (entity = this.getAttackTarget()) instanceof EntityPlayerMP) {
				this.bossInfo.addPlayer((EntityPlayerMP)entity);
			}
		}

		@Override
		public void onUpdate() {
			super.onUpdate();
			this.trackAttackedPlayers();
			this.bossInfo.setPercent(this.getHealth() / this.getMaxHealth());
			if (this.blockingTicks > 0) {
				--this.blockingTicks;
			}
		}

		@Override
		protected boolean canSeeInvisible(Entity entityIn) {
			return !entityIn.isInvisible() || this.getDistanceSq(entityIn) <= 400d;
		}

		@Override
		public void writeEntityToNBT(NBTTagCompound compound) {
			super.writeEntityToNBT(compound);
			compound.setBoolean("isReal", this.isReal);
		}

		@Override
		public void readEntityFromNBT(NBTTagCompound compound) {
			super.readEntityFromNBT(compound);
			this.setIsReal(compound.getBoolean("isReal"));
		}
	}

	public static class Entity4MobAppearance extends EntityCustom {
		public Entity4MobAppearance(World worldIn) {
			super(worldIn);
			this.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(ItemMangekyoSharingan.helmet));
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
			RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, renderManager ->
 {
				class ModelItachi extends EntityNinjaMob.ModelNinja {
					ModelItachi() {
						super(0.0F);
						ModelRenderer cube_r1 = new ModelRenderer(this);
						cube_r1.setRotationPoint(0.0F, 2.0F, 4.25F);
						bipedHeadwear.addChild(cube_r1);
						setRotationAngle(cube_r1, 0.0F, -0.1745F, 0.0F);
						cube_r1.cubeList.add(new ModelBox(cube_r1, 0, 0, -2.0F, -4.0F, 0.0F, 4, 8, 0, 0.0F, false));
						ModelRenderer cube_r2 = new ModelRenderer(this);
						cube_r2.setRotationPoint(0.0F, 2.0F, 4.25F);
						bipedHeadwear.addChild(cube_r2);
						setRotationAngle(cube_r2, 0.0F, 0.1745F, 0.0F);
						cube_r2.cubeList.add(new ModelBox(cube_r2, 0, 0, -2.0F, -4.0F, 0.0F, 4, 8, 0, 0.0F, false));
					}
					@Override
					public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
						super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);
						if (((EntityCustom)entityIn).blockingTicks > 0) {
							setRotationAngle(bipedRightArm, -1.2217F, -0.7854F, 0.0F);
						}
					}
				}
				return new RenderCustom(renderManager, new ModelItachi());
			});
		}

		@SideOnly(Side.CLIENT)
		public class RenderCustom extends EntityNinjaMob.RenderBase<EntityCustom> {
			private final ResourceLocation texture = new ResourceLocation("narutomod:textures/itachi.png");

			public RenderCustom(RenderManager renderManagerIn, ModelBiped modelIn) {
				super(renderManagerIn, modelIn);
			}

			@Override
			public void transformHeldFull3DItemLayer() {
				GlStateManager.translate(0.0F, 0.1875F, 0.0F);
			}

			@Override
			protected ResourceLocation getEntityTexture(EntityCustom entity) {
				return this.texture;
			}
		}
	}
}
