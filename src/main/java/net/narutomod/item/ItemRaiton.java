
package net.narutomod.item;

import net.minecraft.entity.EntityLiving;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.*;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.common.ticket.AABBTicket;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.event.ModelRegistryEvent;

import net.minecraft.world.World;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.ResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.Entity;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.init.MobEffects;

import net.narutomod.NarutomodModVariables;
import net.narutomod.entity.*;
import net.narutomod.potion.*;
import net.narutomod.procedure.ProcedureOnLivingUpdate;
import net.narutomod.procedure.ProcedureUtils;
import net.narutomod.procedure.ProcedureWhenPlayerAttcked;
import net.narutomod.creativetab.TabModTab;
import net.narutomod.Particles;
import net.narutomod.Chakra;
import net.narutomod.ElementsNarutomodMod;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@ElementsNarutomodMod.ModElement.Tag
public class ItemRaiton extends ElementsNarutomodMod.ModElement {
	@GameRegistry.ObjectHolder("narutomod:raiton")
	public static final Item block = null;
	public static final int ENTITYID = 129;
	public static final int ENTITY2ID = 10129;
	public static final ItemJutsu.JutsuEnum CHIDORI = new ItemJutsu.JutsuEnum(0, "chidori", 'A', EntityChidori.CHAKRA_USAGE, new EntityChidori.EC.Jutsu());
	public static final ItemJutsu.JutsuEnum CHAKRAMODE = new ItemJutsu.JutsuEnum(1, "raitonchakramode", 'B', 5d, new EntityChakraMode.Jutsu());
	public static final ItemJutsu.JutsuEnum CHASINGDOG = new ItemJutsu.JutsuEnum(2, "lightning_beast", 'C', 100d, new EntityLightningBeast.EC.Jutsu());
	public static final ItemJutsu.JutsuEnum GIAN = new ItemJutsu.JutsuEnum(3, "false_darkness", 'B', 50d, new EntityFalseDarkness.EC.Jutsu());
	public static final ItemJutsu.JutsuEnum KIRIN = new ItemJutsu.JutsuEnum(4, "kirin", 'S', 2000d, new EntityKirin.EC.Jutsu());
	public static final ItemJutsu.JutsuEnum BLACKPANTHER = new ItemJutsu.JutsuEnum(5, "lightning_panther", 'S', 200d, new EntityLightningPanther.EC.Jutsu());

	public ItemRaiton(ElementsNarutomodMod instance) {
		super(instance, 373);
	}

	@Override
	public void initElements() {
		elements.items.add(() -> new RangedItem(CHIDORI, CHAKRAMODE, CHASINGDOG, GIAN, KIRIN, BLACKPANTHER));
		elements.entities.add(() -> EntityEntryBuilder.create().entity(EntityChakraMode.class)
			.id(new ResourceLocation("narutomod", "raitonchakramode"), ENTITYID).name("raitonchakramode").tracker(64, 1, true).build());
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerModels(ModelRegistryEvent event) {
		ModelLoader.setCustomModelResourceLocation(block, 0, new ModelResourceLocation("narutomod:raiton", "inventory"));
	}

	public static class RangedItem extends ItemJutsu.Base {
		public RangedItem(ItemJutsu.JutsuEnum... list) {
			super(ItemJutsu.JutsuEnum.Type.RAITON, list);
			this.setUnlocalizedName("raiton");
			this.setRegistryName("raiton");
			this.setCreativeTab(TabModTab.tab);
		}


		@Override
		public void onUpdate(ItemStack itemstack, World world, Entity entity, int par4, boolean par5) {
			super.onUpdate(itemstack, world, entity, par4, par5);
			if (!world.isRemote && entity instanceof EntityLivingBase && entity.ticksExisted % 10 == 0) {
				boolean flag = false;
				if (entity.getEntityData().getInteger("KekkeiGenkai") == 22) {
					flag = true;
				}
				this.enableJutsu(itemstack, CHAKRAMODE, flag);
				if (!this.isAffinity(itemstack)) {
					this.enableJutsu(itemstack, KIRIN, false);
					this.enableJutsu(itemstack, BLACKPANTHER, false);
				}

			}
		}
	}




	public static class EntityChakraMode extends Entity implements ItemJutsu.IJutsu {
		private final double CHAKRA_BURN = CHAKRAMODE.chakraUsage; // per second
		private EntityLivingBase summoner;
		private ItemStack usingItemstack;
		private int strengthAmplifier = 12;
		private float modifier;

		public static class HellStab extends Entity  {
			private EntityLivingBase user;
			private Vec3d start;
			private Vec3d end;
			private int dist = 35;
			private int delay = 10;
			List<String> targets = new ArrayList<String>();

			public HellStab(World worldIn) {
				super(worldIn);
				this.setSize(0.01f, 0.01f);
				this.isImmuneToFire = true;
			}

			private void setLightningAt(Vec3d targetVec) {
				EntityLightningArc.Base entity2 = new EntityLightningArc.Base(this.world,
						this.start, targetVec, 0xc00000ff, 1, 0.15f);
				entity2.setThickness(.1f);
				entity2.setDamage(ItemJutsu.causeJutsuDamage(this, this.user), 0, this.user);
				this.world.spawnEntity(entity2);
			}

			public HellStab(EntityLivingBase user) {
				this(user.world);
				this.user = user;
				this.setPosition(this.user.posX, this.user.posY, this.user.posZ);
				this.user.addPotionEffect(new PotionEffect(PotionHeaviness.potion, this.delay, 6, false, false));
				this.playSound(SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:hell_stab")),
						6f, 1f);
				//this.playSound((SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:kairikimuso")), 1f, 1f);
			}


			@Override
			protected void entityInit() {
			}

			@Override
			public void onUpdate() {
				if (this.user != null) {
					this.setPosition(this.user.posX, this.user.posY+1, this.user.posZ);
					if (!this.world.isRemote) {
						this.user.addPotionEffect(new PotionEffect(PotionUsingJutsu.potion, 5, 1, false, false));
					}
					if (this.ticksExisted<delay) {
						EntityLightningArc.spawnAsParticle(this.world, this.posX + this.rand.nextGaussian() * 0.3d,
								this.posY + this.rand.nextDouble() * 1.3d, this.posZ + this.rand.nextGaussian() * 0.3d,
								3d, 0d, 0.1d, 0d);
						Particles.spawnParticle(world, Particles.Types.SMOKE, this.posX, this.posY, this.posZ,
								10, 0.3d, 0.0d, 0.3d, 0, 0.9d, 0, 0x2080D0FF, 140, 5, 0xF0, this.user.getEntityId());
					}
					if (this.ticksExisted == delay) {
						this.start = this.user.getPositionVector();
						Vec3d look = this.user.getLookVec();
						if (look.y > .5) {
							this.dist*=.75;
						}
						RayTraceResult rtr = ProcedureUtils.objectEntityLookingAt(this.user, this.dist);

						this.end = rtr.hitVec;
						this.user.setPositionAndUpdate(this.end.x,this.end.y,this.end.z);
						this.playSound(SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:electricity")),
								1.5f, this.rand.nextFloat() * 0.3f + .4f);
						this.playSound(SoundEvents.ENTITY_LIGHTNING_IMPACT, 1.5f, this.rand.nextFloat() * 0.2f + 1f);
						double distance = this.start.distanceTo(this.end);

						for (double i = 0; i < distance; i+=distance/8) {
							Vec3d point = this.start.add(look.scale(i));
							AxisAlignedBB hitbox = new AxisAlignedBB(new BlockPos(point)).grow(4);
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
								this.playSound(SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:bullet_impact")),
										4f, 3f);
								float damage = 16f+12*ItemJutsu.getDmgMult(this.user);
								if (entity1 instanceof EntityShieldBase) {
									damage*=3;
								}
								entity1.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, this.user),damage);

							}
						}
					}
					if (this.ticksExisted%2==0 && ( this.ticksExisted > this.delay && this.ticksExisted < this.delay+10)) {
						this.playSound(SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:electricity")),
								.5f, this.rand.nextFloat() * 0.5f + 1.4f);
						this.setLightningAt(this.end.addVector(0,1,0));
					}
				}
				if (this.ticksExisted > this.delay+10) {
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

		public static class GuillotineDrop extends Entity  {
			private EntityLivingBase user;
			private Vec3d start;
			private Vec3d ogStart;
			private Vec3d end;
			private int delay = 15;
			private boolean landed = false;
			private boolean used = false;
			private boolean air = false;
			List<String> targets = new ArrayList<String>();

			public GuillotineDrop(World worldIn) {
				super(worldIn);
				this.setSize(0.01f, 0.01f);
				this.isImmuneToFire = true;
			}

			private void setLightningAt(Vec3d targetVec) {
				EntityLightningArc.Base entity2 = new EntityLightningArc.Base(this.world,
						this.ogStart, targetVec, 0xc00000ff, 1, 0.15f);
				entity2.setThickness(.3f);
				entity2.setDamage(ItemJutsu.causeJutsuDamage(this, this.user), 0, this.user);
				this.world.spawnEntity(entity2);
			}

			public GuillotineDrop(EntityLivingBase user) {
				this(user.world);
				this.user = user;
				this.setPosition(this.user.posX, this.user.posY, this.user.posZ);
				if (this.user.onGround) {
					//Vec3d look = user.getLookVec();
					RayTraceResult rtr = ProcedureUtils.raytraceLook(this.user,new Vec3d(0,1,0),20);
					this.end = rtr.hitVec;
					this.user.setPositionAndUpdate(this.end.x,this.end.y,this.end.z);
					this.user.setVelocity(0,0,0);
				} else {
					this.delay = 0;
				}
				this.ogStart = this.user.getPositionVector();
				this.playSound(SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:guillotine_drop")),
						6f, 1f);
			}


			@Override
			protected void entityInit() {
			}

			@Override
			public void onUpdate() {
				if (this.user != null) {
					this.setPosition(this.user.posX, this.user.posY+1, this.user.posZ);
					if (!this.world.isRemote && !this.used) {
						this.user.addPotionEffect(new PotionEffect(PotionUsingJutsu.potion, 5, 1, false, false));
						if (this.ticksExisted < this.delay) {
							ProcedureUtils.setVelocity(this.user,0,0,0);
						}
					}
					if (this.ticksExisted > this.delay && this.ticksExisted < this.delay+10 && !this.landed) {
						this.start = this.user.getPositionVector();
						Vec3d look = this.user.getLookVec();
						if (true) {
							new Vec3d(look.x,-1,look.z);
						}
						RayTraceResult rtr = ProcedureUtils.raytraceLook(this.user,new Vec3d(look.x,-1,look.z),14);
						if (rtr.typeOfHit == RayTraceResult.Type.BLOCK) {
							this.landed = true;
						}
						this.end = rtr.hitVec;
						this.user.setPositionAndUpdate(this.end.x,this.end.y,this.end.z);
					}
					if (this.landed && !this.used) {
						this.used = true;
						this.playSound(SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:electricity")),
								2f, this.rand.nextFloat() * 0.3f + 1.2f);
						this.playSound(SoundEvents.ENTITY_LIGHTNING_IMPACT, 1.5f, this.rand.nextFloat() * 0.2f + 1.5f);


						Vec3d point = this.end;
						AxisAlignedBB hitbox = new AxisAlignedBB(new BlockPos(point)).grow(16);
						boolean flag = net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.world, this.user);
						this.world.newExplosion(this.user, this.end.x, this.end.y, this.end.z, 6, false, flag);
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

							float damage = 10f+7f*ItemJutsu.getDmgMult(this.user);
							entity1.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, this.user),damage);

						}
					}
					if (this.ticksExisted%2==0 && ( this.landed && this.ticksExisted < this.delay+40)) {
						for (int i = 0; i < 15; i++) {
							EntityLightningArc.spawnAsParticle(this.world, this.end.x + (-3+this.rand.nextDouble() * 6d),
									this.end.y + (-3+this.rand.nextDouble() * 6), this.end.z + (-3+this.rand.nextDouble() * 6d),
									2d, 0d, 0.1d, 0d);
						}
						this.setLightningAt(this.end.subtract(0,5,0));
					}
				}
				if (this.ticksExisted > this.delay+50) {
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

		public static class LigerBomb extends Entity  {
			private EntityLivingBase user;
			private int delay = 35;
			private EntityLivingBase target = null;
			List<String> targets = new ArrayList<String>();
			public LigerBomb(World worldIn) {
				super(worldIn);
				this.setSize(0.01f, 0.01f);
				this.isImmuneToFire = true;
			}

			private void setLightningAt(Vec3d startVec, Vec3d targetVec, float thickness, int duration) {
				EntityLightningArc.Base entity2 = new EntityLightningArc.Base(this.world,
						startVec, targetVec, 0xc00000ff, duration, 0.15f);
				entity2.setThickness(thickness);
				entity2.setDamage(ItemJutsu.causeJutsuDamage(this, this.user), 0, this.user);
				this.world.spawnEntity(entity2);
			}

			public LigerBomb(EntityLivingBase user, EntityLivingBase target) {
				this(user.world);
				this.user = user;
				this.setPosition(this.user.posX, this.user.posY, this.user.posZ);
				this.user.addPotionEffect(new PotionEffect(PotionHeaviness.potion, this.delay/3, 4, false, false));
				Vec3d point = this.user.getPositionVector().add(this.user.getLookVec().scale(4));
				AxisAlignedBB hitbox = new AxisAlignedBB(new BlockPos(point)).grow(4);
				this.target = target;
				this.target.addPotionEffect(new PotionEffect(PotionParalysis.potion, this.delay+10, 1, false, false));

			}


			@Override
			protected void entityInit() {
			}

			@Override
			public void onUpdate() {
				if (this.target == null) {
					this.setDead();
					return;
				}
				if (this.user != null) {
					this.setPosition(this.user.posX, this.user.posY+1, this.user.posZ);
					if (!this.world.isRemote) {
						this.user.addPotionEffect(new PotionEffect(PotionUsingJutsu.potion, 5, 1, false, false));
					}
					if (this.ticksExisted<delay) {
						Vec3d targetPos = this.user.getPositionVector().add(this.user.getLookVec().scale(2));
						this.target.setPositionAndUpdate(targetPos.x,targetPos.y+2,targetPos.z);
						if (this.target instanceof EntityPlayer) {
							this.target.addPotionEffect(new PotionEffect(PotionUsingJutsu.potion, 5, 1, false, false));
						}
					}
					if (this.ticksExisted == delay-15) {
						this.playSound(SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:liger_bomb")),
								6f, 1f);
					}
					if (this.ticksExisted == delay) {
						RayTraceResult rtr = ProcedureUtils.raytraceLook(this.user,new Vec3d(0,-1,0),30);
						Vec3d targetPos = rtr.hitVec.subtract(0,3,0);
						this.user.setPositionAndUpdate(targetPos.x,targetPos.y+3,targetPos.z);
						Vec3d tpos = this.user.getPositionVector().add(this.user.getLookVec().scale(2));
						this.target.setPositionAndUpdate(tpos.x,tpos.y,tpos.z);
						this.playSound(SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:electricity")),
								1.5f, this.rand.nextFloat() * 0.3f + .4f);
						this.playSound(SoundEvents.ENTITY_LIGHTNING_IMPACT, 1.5f, this.rand.nextFloat() * 0.2f + 1f);

						AxisAlignedBB hitbox = new AxisAlignedBB(new BlockPos(targetPos)).grow(15);
						boolean flag = net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.world, this.user);
						setLightningAt(targetPos.subtract(0,8,0),targetPos.addVector(0,40,0),0.5f,40);
						for (int i = 0; i < 12; i+=1) {
							Vec3d lightningStart = targetPos.subtract(0,8,0).addVector(-15+this.rand.nextFloat()*30,0,-15+this.rand.nextFloat()*30);
							Vec3d lightningEnd = lightningStart.addVector(-3+this.rand.nextFloat()*6,20+this.rand.nextFloat()*5,-3+this.rand.nextFloat()*6);
							setLightningAt(lightningStart,lightningEnd,0.1f,30);
						}
						this.world.newExplosion(this.user, targetPos.x, targetPos.y, targetPos.z, 30, false, flag);
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
							float damage = 16f+20f*ItemJutsu.getDmgMult(this.user);
							entity1.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, this.user),damage);

						}
					}
					if (this.ticksExisted%2==0 && ( this.ticksExisted > this.delay && this.ticksExisted < this.delay+10)) {
						this.playSound(SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:electricity")),
								.5f, this.rand.nextFloat() * 0.5f + 1.4f);
					}
				}
				if (this.ticksExisted > delay+10) {
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

		public EntityChakraMode(World a) {
			super(a);
			this.setSize(0.01f, 0.01f);
		}

		protected EntityChakraMode(EntityLivingBase summonerIn, ItemStack stack) {
			this(summonerIn.world);
			this.summoner = summonerIn;
			this.setPosition(summonerIn.posX, summonerIn.posY, summonerIn.posZ);
			if (stack.getItem() == block) {
				this.usingItemstack = stack;
				this.modifier = 0;
			}
			if (summonerIn.isPotionActive(MobEffects.STRENGTH)) {
				this.strengthAmplifier += summonerIn.getActivePotionEffect(MobEffects.STRENGTH).getAmplifier() + 1;
			}
		}

		@Override
		public ItemJutsu.JutsuEnum.Type getJutsuType() {
			return ItemJutsu.JutsuEnum.Type.RAITON;
		}

		@Override
		protected void entityInit() {
		}

		@Override
		public void setDead() {
			super.setDead();
			this.setNewCooldown();
		}

		float tpCool;
		float jutsu1Cool;
		float jutsu2Cool;
		float jutsu3Cool;

		private boolean jutsuKey1Pressed;
		private boolean jutsuKey2Pressed;
		private boolean jutsuKey3Pressed;

		@Override
		public void onUpdate() {
			super.onUpdate();
			if (this.summoner != null && this.summoner.isEntityAlive() && this.getStackFromInventory() != null) {

				if (ItemSenjutsu.isSageModeActivated(this.summoner)) {
					ItemSenjutsu.deactivateSageMode(this.summoner);
				}
				if ((EntityBijuManager.cloakLevel((EntityPlayer) this.summoner) > 0)) {
					EntityBijuManager.toggleBijuCloak((EntityPlayer) this.summoner);
				}

				--this.tpCool;
				--this.jutsu1Cool;
				--this.jutsu2Cool;
				--this.jutsu3Cool;
				ItemStack stack = this.getStackFromInventory();

				this.setPosition(this.summoner.posX, this.summoner.posY, this.summoner.posZ);
				EntityPlayer entity2 = (EntityPlayer) this.summoner;
				if (!entity2.isPotionActive(PotionUsingJutsu.potion) && (entity2.getHeldItemMainhand().equals(stack) || entity2.getHeldItemOffhand().equals(stack))) {

					boolean newPressed = this.summoner.getEntityData().getBoolean(NarutomodModVariables.JutsuKey1Pressed);
					if (this.jutsuKey1Pressed && !newPressed && this.jutsu1Cool <= 0 && Chakra.pathway(this.summoner).consume(500d)) {
						this.jutsu1Cool = 20*5;
						this.summoner.world.spawnEntity(new HellStab(this.summoner));
					}
					this.jutsuKey1Pressed = newPressed;

					boolean newPressed2 = this.summoner.getEntityData().getBoolean(NarutomodModVariables.JutsuKey2Pressed);
					if (this.jutsuKey2Pressed && !newPressed2 && this.jutsu2Cool <= 0 && Chakra.pathway(this.summoner).consume(500d)) {
						this.jutsu2Cool = 20*5;
						this.summoner.world.spawnEntity(new GuillotineDrop(this.summoner));
					}
					this.jutsuKey2Pressed = newPressed2;

					boolean newPressed3 = this.summoner.getEntityData().getBoolean(NarutomodModVariables.JutsuKey3Pressed);
					if (this.jutsuKey3Pressed && !newPressed3 && this.jutsu3Cool <= 0) {
						RayTraceResult result = ProcedureUtils.objectEntityLookingAt(this.summoner,6,5);
						if (result.entityHit instanceof EntityLivingBase && Chakra.pathway(this.summoner).consume(800d)) {
							this.jutsu3Cool = 20*15;
							this.summoner.world.spawnEntity(new LigerBomb(this.summoner, (EntityLivingBase) result.entityHit));
						}
					}
					this.jutsuKey3Pressed = newPressed3;

				}




				if (this.ticksExisted % 20 == 0) {
					Chakra.Pathway chakra = Chakra.pathway(this.summoner);
					if (!chakra.consume(this.CHAKRA_BURN)) {
						this.setDead();
					}
					//this.summoner.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 22, 3, false, false));
					this.summoner.addPotionEffect(new PotionEffect(MobEffects.SPEED, 22, 28, false, false));
					this.summoner.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, 22, 2+(int)(4*ItemJutsu.getDmgMult(this.summoner))/3, false, false));
					this.summoner.addPotionEffect(new PotionEffect(MobEffects.JUMP_BOOST, 22, 9, false, false));
				}
				if (this.modifier > 0.0f) {
					ProcedureWhenPlayerAttcked.setExtraDamageReduction(this.summoner, 1.0f - this.modifier);
				}
				/*if (this.summoner instanceof EntityPlayer) {
					if (!this.summoner.isSprinting()) {
						double d0 = this.posX - this.prevPosX;
						double d1 = this.posZ - this.prevPosZ;
						((EntityPlayer)this.summoner).addExhaustion(0.02f * (float)MathHelper.sqrt(d0 * d0 + d1 * d1) * this.modifier);
					}
					if (((EntityPlayer)this.summoner).getFoodStats().getFoodLevel() < 1.0f) {
						this.setDead();
					}
				}*/
				if (this.rand.nextInt(8) == 0) {
					this.playSound(SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:electricity")),
					 0.1f, this.rand.nextFloat() * 0.6f + 0.3f);
				}
				EntityLightningArc.spawnAsParticle(this.world, this.posX + this.rand.nextGaussian() * 0.3d,
				  this.posY + this.rand.nextDouble() * 1.3d, this.posZ + this.rand.nextGaussian() * 0.3d,
				  0.8d, 0d, 0.2d, 0d);
				Particles.spawnParticle(world, Particles.Types.SMOKE, this.posX, this.posY, this.posZ,
				  20, 0.3d, 0.0d, 0.3d, 0d, .7d, 0d, 0x2080D0FF, 70, 5, 0xF0, this.summoner.getEntityId());
				if (this.tpCool <= 0) {
					if (this.summoner.swingProgressInt == 1 && this.summoner instanceof EntityPlayer) {
						RayTraceResult result = ProcedureUtils.objectEntityLookingAt(this.summoner, 3d, this);
						if (result == null || result.entityHit == null) {
							this.tpCool = 50;
							result = ProcedureUtils.objectEntityLookingAt(this.summoner, 20d, 5d, this);
							if (result != null && result.entityHit instanceof EntityLivingBase) {
								Vec3d vec = result.entityHit.getPositionEyes(1f).subtract(this.summoner.getPositionEyes(1f)).normalize();
								this.summoner.rotationYaw = ProcedureUtils.getYawFromVec(vec);
								this.summoner.rotationPitch = ProcedureUtils.getPitchFromVec(vec);
								this.summoner.setPositionAndUpdate(result.entityHit.posX - vec.x, result.entityHit.posY - vec.y + 0.5d, result.entityHit.posZ - vec.z);
								((EntityPlayer)this.summoner).attackTargetEntityWithCurrentItem(result.entityHit);
							}
						}
						if (result != null && result.entityHit instanceof EntityLivingBase) {
							ProcedureUtils.pushEntity(this.summoner, result.entityHit, 12d, 1.5f);
						}
					}
				}

			} else if (!this.world.isRemote) {
				this.setDead();
			}
		}

		private void setNewCooldown() {
			ItemStack stack = this.getStackFromInventory();
			if (stack != null && stack.getItem() instanceof ItemJutsu.Base) {
				ItemJutsu.Base item = (ItemJutsu.Base)stack.getItem();
				/*long cooldown = (long)((float)this.ticksExisted * item.getModifier(stack, this.summoner)) + 40;
				if (cooldown > 1200) {
					cooldown = 1200;
				}*/
				item.setJutsuCooldown(stack, CHAKRAMODE, 20);
			}
		}

		@Nullable
		private ItemStack getStackFromInventory() {
			if (this.usingItemstack != null && this.summoner != null) {
				ItemStack stack = this.summoner instanceof EntityPlayer
				 ? ProcedureUtils.getMatchingItemStack((EntityPlayer)this.summoner, this.usingItemstack)
				 : this.usingItemstack;
				return stack;
			}
			return null;
		}

		@Override
		protected void readEntityFromNBT(NBTTagCompound compound) {
		}

		@Override
		protected void writeEntityToNBT(NBTTagCompound compound) {
		}

		public static class Jutsu implements ItemJutsu.IJutsuCallback {
			private static final String ID_KEY = "EntityChakraModeIdKey";
			@Override
			public boolean createJutsu(ItemStack stack, EntityLivingBase entity, float power) {
				ItemStack stack1 = ProcedureUtils.getMatchingItemStack(entity, ItemRaiton.block);
				if (stack1 == null || !stack1.hasTagCompound() || (entity.getEntityData().getInteger("KekkeiGenkai") != 22)) {
					return false;
				}
				Entity entity1 = entity.world.getEntityByID(stack.getTagCompound().getInteger(ID_KEY));
				if (entity1 instanceof EntityChakraMode && entity instanceof EntityPlayer) {
					entity1.setDead();
					stack.getTagCompound().removeTag(ID_KEY);
					return false;
				} else {
					if (ItemFuton.CHAKRAFLOW.jutsu.isActivated(entity)) {
						ItemFuton.CHAKRAFLOW.jutsu.deactivate(entity);
					}
					if (ItemKaton.FLAMESLICE.jutsu.isActivated(entity)) {
						ItemKaton.FLAMESLICE.jutsu.deactivate(entity);
					}
					if (ItemIryoJutsu.POWERMODE.jutsu.isActivated(entity)) {
						ItemIryoJutsu.POWERMODE.jutsu.deactivate(entity);
					}
					if (ItemRanton.CLOUD.jutsu.isActivated(entity)) {
						ItemRanton.CLOUD.jutsu.deactivate(entity);
					}
					if (ItemSenjutsu.isSageModeActivated(entity)) {
						ItemSenjutsu.deactivateSageMode(entity);
					}
					if ((EntityBijuManager.cloakLevel((EntityPlayer) entity) > 0)) {
						EntityBijuManager.toggleBijuCloak((EntityPlayer) entity);
					}
					entity1 = new EntityChakraMode(entity, stack);
					stack.getTagCompound().setInteger(ID_KEY, entity1.getEntityId());
					entity.world.spawnEntity(entity1);
					return true;
				}
			}

			@Override
			public boolean isActivated(ItemStack stack) {
				return stack.getTagCompound().hasKey(ID_KEY);
			}

			@Override
			public boolean isActivated(EntityLivingBase entity) {
				return this.getData(entity) != null;
			}

			@Override
			public void deactivate(EntityLivingBase entity) {
				ItemJutsu.IJutsuCallback.JutsuData jd = this.getData(entity);
				if (jd != null) {
					jd.entity.setDead();
					jd.stack.getTagCompound().removeTag(ID_KEY);
				}
			}

			@Override
			@Nullable
			public ItemJutsu.IJutsuCallback.JutsuData getData(EntityLivingBase entity) {
				if (entity instanceof EntityPlayer) {
					ItemStack stack = ProcedureUtils.getMatchingItemStack((EntityPlayer)entity, block);
					if (stack != null && stack.hasTagCompound() && stack.getTagCompound().hasKey(ID_KEY)) {
						Entity entity1 = entity.world.getEntityByID(stack.getTagCompound().getInteger(ID_KEY));
						return entity1 instanceof EntityChakraMode ? new JutsuData(entity1, stack) : null;
					}
				}
				return null;
			}
		}
	}
}
