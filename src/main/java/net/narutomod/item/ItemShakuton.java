
package net.narutomod.item;

import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.event.ModelRegistryEvent;

import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.Entity;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;

import net.narutomod.potion.PotionUsingJutsu;
import net.narutomod.procedure.ProcedureSync;
import net.narutomod.procedure.ProcedureUtils;
import net.narutomod.procedure.ProcedureAoeCommand;
import net.narutomod.entity.EntityRendererRegister;
import net.narutomod.entity.EntityScalableProjectile;
import net.narutomod.Particles;
import net.narutomod.creativetab.TabModTab;
import net.narutomod.ElementsNarutomodMod;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;

@ElementsNarutomodMod.ModElement.Tag
public class ItemShakuton extends ElementsNarutomodMod.ModElement {
	@GameRegistry.ObjectHolder("narutomod:shakuton")
	public static final Item block = null;
	public static final int ENTITYID = 269;
	public static final ItemJutsu.JutsuEnum ORB = new ItemJutsu.JutsuEnum(0, "scorchorb", 'S', 150, 150d, new EntityScorchBall.Jutsu());
	public static final ItemJutsu.JutsuEnum SHOOT = new ItemJutsu.JutsuEnum(1, "tooltip.shakuton.scorchkill", 'S', 200, 100d, new SetOrbTarget());
	public static final ItemJutsu.JutsuEnum BLAST = new ItemJutsu.JutsuEnum(2, "tooltip.shakuton.scorchblast", 'S', 250, 500d, new SuperSteamBlast());
	public static final ItemJutsu.JutsuEnum HEATWAVE = new ItemJutsu.JutsuEnum(2, "tooltip.shakuton.scorchheatwave", 'S', 250, 130d, new EntityScorchBall.Jutsu2());
	public ItemShakuton(ElementsNarutomodMod instance) {
		super(instance, 589);
	}

	@Override
	public void initElements() {
		elements.items.add(() -> new RangedItem(ORB, SHOOT, BLAST, HEATWAVE));
		elements.entities.add(() -> EntityEntryBuilder.create().entity(EntityScorchBall.class)
				.id(new ResourceLocation("narutomod", "scorchorb"), ENTITYID).name("scorchorb").tracker(64, 1, true).build());
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerModels(ModelRegistryEvent event) {
		ModelLoader.setCustomModelResourceLocation(block, 0, new ModelResourceLocation("narutomod:shakuton", "inventory"));
	}

	public static class RangedItem extends ItemJutsu.Base {
		protected static final String spawnedBalls = "SpawnedBallsId";

		public RangedItem(ItemJutsu.JutsuEnum... list) {
			super(ItemJutsu.JutsuEnum.Type.SHAKUTON, list);
			setUnlocalizedName("shakuton");
			setRegistryName("shakuton");
			setCreativeTab(TabModTab.tab);
			this.defaultCooldownMap[ORB.index] = 0;
			this.defaultCooldownMap[SHOOT.index] = 0;
			this.defaultCooldownMap[BLAST.index] = 0;
			this.defaultCooldownMap[HEATWAVE.index] = 0;
		}

		protected void saveSpawnedBall(ItemStack stack, Entity entity) {
			if (!stack.hasTagCompound()) {
				stack.setTagCompound(new NBTTagCompound());
			}
			int[] oldarray = stack.getTagCompound().getIntArray(spawnedBalls);
			int[] newarray = new int[oldarray.length + 1];
			System.arraycopy(oldarray, 0, newarray, 0, oldarray.length);
			newarray[oldarray.length] = entity.getEntityId();
			stack.getTagCompound().setIntArray(spawnedBalls, newarray);
		}

		protected void saveSpawnedBall2(ItemStack stack, Entity entity) {
			if (!stack.hasTagCompound()) {
				stack.setTagCompound(new NBTTagCompound());
			}
			int[] oldarray = stack.getTagCompound().getIntArray(spawnedBalls);
			int[] newarray = new int[oldarray.length - 1];
			System.arraycopy(oldarray, 0, newarray, 0, oldarray.length);
			newarray[oldarray.length] = entity.getEntityId();
			stack.getTagCompound().setIntArray(spawnedBalls, newarray);
		}

		@Nullable
		protected EntityScorchBall get1stBallAndPutLast(World world, ItemStack stack) {
			if (stack.hasTagCompound()) {
				int[] balls = stack.getTagCompound().getIntArray(spawnedBalls);
				if (balls.length > 0) {
					Entity entity = world.getEntityByID(balls[0]);
					if (entity instanceof EntityScorchBall) {
						if (balls.length > 1) {
							System.arraycopy(balls, 1, balls, 0, balls.length - 1);
							balls[balls.length - 1] = entity.getEntityId();
						}
						if (entity.isEntityAlive()) {
							return (EntityScorchBall)entity;
						}
					}
				}
			}
			return null;
		}

		protected int getTotalBalls(ItemStack stack) {
			return stack.hasTagCompound() ? stack.getTagCompound().getIntArray(spawnedBalls).length : 0;
		}

		protected void clearBalls(ItemStack stack) {
			stack.getTagCompound().removeTag(spawnedBalls);
		}

		@Override
		public void onUpdate(ItemStack itemstack, World world, Entity entity, int par4, boolean par5) {
			super.onUpdate(itemstack, world, entity, par4, par5);
			if (entity instanceof EntityLivingBase) {
				EntityLivingBase livingEntity = (EntityLivingBase) entity;
				if (!livingEntity.getHeldItemMainhand().equals(itemstack) && !livingEntity.getHeldItemOffhand().equals(itemstack)) {
					this.clearBalls(itemstack);
				}
			}
		}

		@Override
		public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer entity, EnumHand hand) {
			if (entity.isCreative() || (ProcedureUtils.hasItemInInventory(entity, ItemFuton.block) 
			 && ProcedureUtils.hasItemInInventory(entity, ItemKaton.block))) {
				return super.onItemRightClick(world, entity, hand);
			}
			return new ActionResult<ItemStack>(EnumActionResult.FAIL, entity.getHeldItem(hand));
		}

		@Override
		public void addInformation(ItemStack itemstack, World world, List<String> list, ITooltipFlag flag) {
			super.addInformation(itemstack, world, list, flag);
			list.add(TextFormatting.GREEN + net.minecraft.util.text.translation.I18n.translateToLocal("tooltip.shakuton.musthave") + TextFormatting.RESET);
		}
	}

	public static class SetOrbTarget implements ItemJutsu.IJutsuCallback {
		@Override
		public boolean createJutsu(ItemStack stack, EntityLivingBase entity, float power) {
			if (entity.isSneaking()) {
				boolean worked = false;
				int j = ((RangedItem)block).getTotalBalls(stack);
				for (int i = 0; i < j; i++) {
					EntityScorchBall entity1 = ((RangedItem)block).get1stBallAndPutLast(entity.world, stack);
					if (entity1 != null && entity1.isEntityAlive() && entity1.targetTime <= 0) {
						Vec3d look = entity1.getPositionVector().subtract(0,0.1f,0).subtract(entity.getPositionEyes(1)).normalize();
						entity1.shootOrb(look);
						worked = true;
					}
				}
				return worked;
			} else {
				EntityScorchBall entity1 = ((RangedItem)block).get1stBallAndPutLast(entity.world, stack);
				if (entity1 != null && entity1.isEntityAlive() && entity1.targetTime <= 0 ) {
					entity1.shootOrb(entity.getLookVec());

					RayTraceResult res = ProcedureUtils.objectEntityLookingAt(entity, 100, 4d, EntityScorchBall.class);
					if (res != null && res.entityHit != null) {
						Vec3d look = res.entityHit.getPositionVector().addVector(0,res.entityHit.height/2,0).subtract(entity1.getPositionVector()).normalize();
						entity1.shootOrb(look);
					}
					return true;
				}
			}
			return false;
		}
	}

	public static class ScorchWave extends Entity  {
		private EntityLivingBase user;
		private Vec3d look;
		private float power = 1;
		private Vec3d start;
		List<String> targets = new ArrayList<String>();
		EntityScorch2 ball;
		private final int startup = 15;
		private final int duration = 15;

		public ScorchWave(World worldIn) {
			super(worldIn);
			this.setSize(0.01f, 0.01f);
			this.isImmuneToFire = true;
		}


		public ScorchWave(EntityLivingBase user, float power) {
			this(user.world);
			this.power = power;
			this.user = user;
			this.setPosition(this.user.posX, this.user.posY, this.user.posZ);
			//this.ball = new EntityScorch2(user);
			//user.world.spawnEntity(this.ball);
			this.playSound(SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:jutsu")),
					1f, 1f);
			this.playSound(SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:flamethrow")),
					1f, 2f);
		}


		@Override
		protected void entityInit() {
		}

		@Override
		public void onUpdate() {
			if (this.user != null) {

				if (!this.world.isRemote) {
					this.user.addPotionEffect(new PotionEffect(PotionUsingJutsu.potion, 5, 1, false, false));
				}

				Vec3d look = this.user.getLookVec().normalize();
				Vec3d point = this.user.getPositionVector().addVector(0,1,0).add(look.scale(1.5));
				//this.ball.setPosition(point.x,point.y,point.z);
				this.setPosition(point.x, point.y, point.z);
				float size = 1+(1*(this.power/10));
				int p1 = 60+(int) this.power;
				int p2 = 40+(int) this.power/2;
				if (this.ticksExisted <= this.startup) {
					size = size*((float) Math.min(this.ticksExisted, this.startup) /this.startup);
				} else {
					size*=0.25f;
					p1/=4;
					p2/=4;
				}
				for (int i = 0; i < 20; i++) {
					Vec3d a = point.addVector((-0.5+this.rand.nextFloat()*1)*size,(-0.5+this.rand.nextFloat()*1)*size,(-0.5+this.rand.nextFloat()*1)*size);
					Particles.spawnParticle(this.world, Particles.Types.SMOKE, a.x,a.y,a.z,
							1, 0,0,0, 0,0,0, 0xa6ff4e83, p1,3);
				}
				for (int i = 0; i < 20; i++) {
					Vec3d a = point.addVector((-0.25+this.rand.nextFloat()*0.5)*size,(-0.25+this.rand.nextFloat()*0.5)*size,(-0.25+this.rand.nextFloat()*0.5)*size);
					Particles.spawnParticle(this.world, Particles.Types.SMOKE, a.x,a.y,a.z,
							1, 0,0,0, 0,0,0, 0xa6ffc7d8, p2,3);
				}
				if (this.ticksExisted >= this.startup) {
					int p5 = (int) (15+this.power);
					int p6 = (int) (5+this.power*4);
					Vec3d speed = look.scale(4+8*this.power/10);
					Particles.spawnParticle(this.world, Particles.Types.SONIC_BOOM, point.x, point.y,point.z,
							1, 0,0,0, speed.x,speed.y,speed.z, 0xa6ff4e83, p6, 4);
					for (int i = 0; i < 20+this.power*2; i++) {
						Vec3d a = point.addVector((-this.power/2+this.rand.nextFloat()*this.power),(-this.power/2+this.rand.nextFloat()*this.power),(-this.power/2+this.rand.nextFloat()*this.power));
						Particles.spawnParticle(this.world, Particles.Types.SMOKE, a.x, a.y,a.z,
								1, 0,0,0, speed.x,speed.y,speed.z, 0xa6ff4e83, p5+5, 3);
					}
					for (int i = 0; i < 10+this.power; i++) {
						Vec3d a = point.addVector((-this.power/2+this.rand.nextFloat()*this.power),(-this.power/2+this.rand.nextFloat()*this.power),(-this.power/2+this.rand.nextFloat()*this.power));
						Particles.spawnParticle(this.world, Particles.Types.SMOKE, a.x, a.y,a.z,
								1, 0,0,0, speed.x,speed.y,speed.z, 0xa6ffc7d8, p5+5, 3);
					}

					this.playSound(SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:flamethrow")),
							1f, this.rand.nextFloat() * 0.6f + 0.7f);
					this.playSound(SoundEvents.BLOCK_FIRE_EXTINGUISH, 1f, this.rand.nextFloat() * 0.6f + 0.7f);
					this.targets = new ArrayList<String>();
					if (!this.world.isRemote) {
						for (int i = 0; i < 20+40*this.power/10; i++) {
							Vec3d end = this.getPositionVector().add(look.scale(1+i*0.5));
							AxisAlignedBB hitbox = new AxisAlignedBB(new BlockPos(end)).grow(0.5+this.power/10*3);
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
								float damage = 2+0.5f*ItemJutsu.getDmgMult(this.user)*(1+3*this.power/10);

								entity1.hurtResistantTime = 10;
								entity1.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, this.user),damage);
								Particles.spawnParticle(this.world, Particles.Types.SMOKE, entity1.posX, entity1.posY, entity1.posZ, 10,
										entity1.width, entity1.height, entity1.width, 0d, 0d, 0d, 0x40FFFFFF, 15);
							}
						}
					}
				}
			}
			if (this.ticksExisted > this.startup+this.duration) {
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

	public static class EntityScorchBall extends EntityScalableProjectile.Base implements ItemJutsu.IJutsu {
		private double idleHeight;
		private Entity target;
		//private final int growTime = 60;
		private final float inititalScale = 0.5f;
		public boolean attacking = false;
		private float maxScale = inititalScale;
		private int targetTime = -1;
		public Vec3d lookVec;
		public boolean isWave = false;
		public float power;
		public EntityLivingBase user;
		private boolean firstHit = false;

		public EntityScorchBall(World a) {
			super(a);
			this.setOGSize(1.0F, 1.0F);
		}


		public EntityScorchBall(EntityLivingBase shooter) {
			super(shooter);
			this.setEntityScale(this.inititalScale);
			this.setPosition(shooter.posX, shooter.posY + shooter.height, shooter.posZ);
			this.idleHeight = shooter.getEyeHeight();
		}


		@Override
		public ItemJutsu.JutsuEnum.Type getJutsuType() {
			return ItemJutsu.JutsuEnum.Type.SHAKUTON;
		}

		private Vec3d getIdlePosition() {
			if (this.shootingEntity != null) {
				Vec3d vec = Vec3d.fromPitchYaw(0f, this.ticksExisted * 9).addVector(0d, this.idleHeight, 0d);
				return this.shootingEntity.getPositionVector().add(vec);
			}
			return this.getPositionVector();
		}

		public void setNextPosition(Vec3d vec) {
			Vec3d look = vec.subtract(this.getPositionVector()).normalize();
			if (this.getDistance(vec.x, vec.y, vec.z) > 0.5d && this.targetTime >= 0) {
				this.setVelocity(look.scale(0.9d));
			} else {
				this.setVelocity(vec.subtract(this.getPositionVector()));
				if (this.getDistance(vec.x, vec.y, vec.z) > 2) {
					this.setVelocity(look.scale(2.5d));
				}
				if (vec.equals(this.getIdlePosition()) && this.targetTime >= 0) {
					this.setTarget(null);
				}
			}
		}

		protected void shootOrb(Vec3d look) {
			this.lookVec = look;
			this.targetTime = 10;
		}

		protected void setTarget(@Nullable Entity targetIn) {
			this.target = targetIn;
			this.firstHit = false;
			this.targetTime = targetIn != null ? 35: -1;
		}

		protected void setMaxScale(float scale) {
			this.maxScale = scale;
		}

		private void moveGrowAndShoot() {
			if (this.shootingEntity != null) {
				Vec3d vec = this.shootingEntity.getPositionVector().addVector(0d, this.shootingEntity.height + 1.5f, 0d);
				if (this.getDistance(vec.x, vec.y, vec.z) > 2d) {
					this.setVelocity(vec.subtract(this.getPositionVector()).normalize().scale(3d));
				} else if (this.maxScale > 0) {
					this.setVelocity(Vec3d.ZERO);
					float scale = this.getEntityScale();
					if (scale < this.maxScale) {
						this.setEntityScale(scale * 1.35f);
					} else {
						this.setEntityScale(this.maxScale);
						Vec3d vec2 = this.shootingEntity.getLookVec();
						this.shoot(vec2.x, vec2.y, vec2.z, 0.99f, 0f);
					}
				} else {
					this.setDead();
				}
			}
		}


		private void setVelocity(Vec3d vec) {
			this.motionX = vec.x;
			this.motionY = vec.y;
			this.motionZ = vec.z;
			this.isAirBorne = true;
		}

		@Override
		public void onUpdate() {
			super.onUpdate();

			if (!this.world.isRemote && (this.shootingEntity == null
					|| (this.shootingEntity.getHeldItemMainhand().getItem() != block
					&& this.shootingEntity.getHeldItemOffhand().getItem() != block))) {
				this.setDead();
				return;
			}

			if (this.isWave) {
				if (this.ticksExisted < 10) {
					this.setEntityScale(0.5f + (1.5f * ((this.power / 10) * this.ticksExisted / 10)));
				}
				Vec3d look = this.user.getLookVec().normalize();
				Vec3d point = this.user.getPositionVector().addVector(0,1,0).add(look.scale(1.5));
				this.setPosition(point.x, point.y, point.z);
				if (this.ticksExisted > 20) {
					Particles.spawnParticle(this.world, Particles.Types.SMOKE, this.posX, this.posY + this.height/2, this.posZ,
							1, this.width/2, this.height/2, this.width/2, look.x*2, look.y*2, look.z*2, 0x40ff4e83, 45, 0, 0xF0);
				}
				if (this.ticksExisted > 20 * 5) {
					this.setDead();
				}
				return;
			}
			if (!this.isLaunched() && !this.isWave) {
				--this.targetTime;

				if (this.maxScale != this.getEntityScale()) {
					this.moveGrowAndShoot();
				} else if (this.lookVec!= null && this.targetTime > 0) {
					this.setVelocity(this.lookVec.scale(3));
				} else {
					this.setNextPosition(this.getIdlePosition());
				}
				if (!this.world.isRemote && !this.isWave) {
					for (EntityLivingBase entity : this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().grow(1))) {
						if (!entity.equals(this.shootingEntity) && !entity.equals(this)) {
							/*if (this.target != null) {
								entity.hurtResistantTime = 10;
								if (!this.firstHit) {
									this.firstHit = true;
									this.targetTime = 25;
								}
							}*/
							entity.getEntityData().setBoolean("TempData_disableKnockback", true);
							float dmg = 4f+2f*ItemJutsu.getDmgMult(this.shootingEntity);
							if (this.targetTime > 0) {
								dmg = 10f+4.5f*ItemJutsu.getDmgMult(this.shootingEntity);
							}
							entity.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, this.shootingEntity),dmg);
							this.scorchEffects(entity.posX, entity.posY+entity.height/2, entity.posZ, entity.width/2, entity.height/2);
						}
					}
				}
			}


		}

		@Override
		protected void checkOnGround() {
			super.checkOnGround();
			if (!this.isWave) {
				if (this.onGround) {
					this.onGround = false;
					this.scorchEffects(this.posX, this.posY, this.posZ, 0.4d, 0.4d);
					this.lookVec = null;
					this.targetTime = 0;
				}
				if (this.isInWater()) {
					//this.setDead();
					this.lookVec = null;
					this.targetTime = 0;
				}
			}
		}

		@Override
		protected void onImpact(RayTraceResult result) {
			if (!this.world.isRemote) {
				if ((result.entityHit != null && result.entityHit.equals(this.shootingEntity))
				 || (result.typeOfHit == RayTraceResult.Type.BLOCK && this.ticksInAir <= 5)) {
					return;
				}
				boolean flag = net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.world, this.shootingEntity);
				new net.narutomod.event.EventSphericalExplosion(this.world, this.shootingEntity,
				 (int)this.posX, (int)this.posY + 5, (int)this.posZ, (int)this.maxScale, 0, 0.3333f);
				List<String> targets = new ArrayList<String>();
				AxisAlignedBB hitbox = new AxisAlignedBB(new BlockPos(this.getPositionVector())).grow(2+this.maxScale);
				for (Entity entity1 : this.world.getEntitiesWithinAABBExcludingEntity(this.user, hitbox)) {
					if (!(entity1 instanceof EntityLivingBase)) {
						continue;
					}
					boolean found = false;
					for (String enemy: targets) {
						if (Objects.equals(enemy, entity1.getUniqueID().toString())) {
							found = true;
						}
					}
					if (found) {
						continue;
					}
					float mult = (float) (0.5+1.5*(this.maxScale/30));
					float damage = 20f+ItemJutsu.getDmgMult(this.shootingEntity)*mult;

					entity1.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, this.user),damage);
				}

				//ProcedureAoeCommand.set(this, 0d, this.maxScale);
				//this.world.newExplosion(this.shootingEntity, this.posX, this.posY, this.posZ, this.maxScale * 5f, flag, flag);


				int p1 = (int) (15+this.maxScale*3);
				int p2 = (int) (30+this.maxScale*7);
				Particles.spawnParticle(this.world, Particles.Types.SMOKE, this.posX, this.posY, this.posZ, p1,
						this.maxScale/2, this.maxScale/2, this.maxScale/2, 0d, 0d, 0d, 0xBAFFFFFF, p2);
				this.scorchEffects(this.posX, this.posY, this.posZ, 2.5d * this.maxScale, 1d);
				this.setDead();
			}
		}

		/*@Override
		public boolean attackEntityFrom(DamageSource source, float amount) {
			if (!this.world.isRemote && ItemJutsu.isDamageSourceJutsu(source) && source.getImmediateSource() != null) {
				
			}
		}*/

		@Override
		public void renderParticles() {
			if (this.world.isRemote) {
				Particles.spawnParticle(this.world, Particles.Types.SMOKE, this.posX, this.posY + this.height/2, this.posZ, 
				 (int)(this.width*25f), this.width/2, 0d, this.width/2, 0d, 0d, 0d, 0x40ff4e83, 10, 0, 0xF0);
			}
		}

		private void scorchEffects(double x, double y, double z, double dx, double dy) {
			this.playSound(SoundEvents.BLOCK_FIRE_EXTINGUISH, 1f, this.rand.nextFloat() * 0.6f + 0.7f);
			Particles.spawnParticle(this.world, Particles.Types.SMOKE, x, y, z, (int)(dx * dy * 100d), 
			 dx, dy, dx, 0d, 0d, 0d, 0x40FFFFFF, 15);
		}

		public static class Jutsu implements ItemJutsu.IJutsuCallback {
			//private static final String ID_KEY = "JitonSandShieldEntityIdKey";
			@Override
			public boolean createJutsu(ItemStack stack, EntityLivingBase entity, float power) {
				if (((RangedItem)block).getTotalBalls(stack) < 12) {
					Entity entity1 = new EntityScorchBall(entity);
					entity.world.spawnEntity(entity1);
					((RangedItem)block).saveSpawnedBall(stack, entity1);
					return true;
				}
				return false;
			}
		}

		public static class Jutsu2 implements ItemJutsu.IJutsuCallback {
			//private static final String ID_KEY = "JitonSandShieldEntityIdKey";
			@Override
			public boolean createJutsu(ItemStack stack, EntityLivingBase entity, float power) {
				entity.world.spawnEntity(new ScorchWave(entity, power));
				ItemJutsu.setCurrentJutsuCooldown(stack, 20*9);
				return true;
			}

			@Override
			public float getMinPower() {
				return 1.0f;
			}

			@Override
			public float getPowerupDelay() {
				return 30.0f;
			}

			@Override
			public float getMaxPower() {
				return 10.0f;
			}
		}
	}

	public static class EntityScorch2 extends EntityScalableProjectile.Base {
		private final float inititalScale = 0.5f;
		public EntityLivingBase user;


		public EntityScorch2(EntityLivingBase shooter) {
			super(shooter);
			this.setEntityScale(this.inititalScale);
			this.setPosition(shooter.posX, shooter.posY + shooter.height, shooter.posZ);
		}




		@Override
		public void onUpdate() {
			super.onUpdate();

			if (this.shootingEntity == null || this.ticksExisted > 20*10) {
				this.setDead();
			}

		}

		@Override
		protected void onImpact(RayTraceResult param1RayTraceResult) {

		}


	}

	public static class SuperSteamBlast implements ItemJutsu.IJutsuCallback {
		@Override
		public boolean createJutsu(ItemStack stack, EntityLivingBase entity, float power) {
			int j = ((RangedItem)block).getTotalBalls(stack);
			for (int i = 0; i < j; i++) {
				EntityScorchBall entity1 = ((RangedItem)block).get1stBallAndPutLast(entity.world, stack);
				if (entity1 != null) {
					entity1.setMaxScale(i == 0 ? 2.5f * j : 0f);
				}
			}
			((RangedItem)block).clearBalls(stack);
			return j > 0;
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
			RenderingRegistry.registerEntityRenderingHandler(EntityScorchBall.class, renderManager -> {
				return new RenderCustom(renderManager);
			});
			RenderingRegistry.registerEntityRenderingHandler(EntityScorch2.class, renderManager -> {
				return new RenderCustom2(renderManager);
			});
		}

		@SideOnly(Side.CLIENT)
		public class RenderCustom2 extends Render<EntityScorch2> {
			private final ResourceLocation texture = new ResourceLocation("narutomod:textures/fireball2.png");

			public RenderCustom2(RenderManager renderManager) {
				super(renderManager);
				shadowSize = 0.1f;
			}

			@Override
			public void doRender(EntityScorch2 entity, double x, double y, double z, float entityYaw, float partialTicks) {
				GlStateManager.pushMatrix();
				this.bindEntityTexture(entity);
				float scale = entity.getEntityScale();
				GlStateManager.translate(x, y + 0.5d * scale, z);
				GlStateManager.enableRescaleNormal();
				GlStateManager.scale(scale, scale, scale);
				Tessellator tessellator = Tessellator.getInstance();
				BufferBuilder bufferbuilder = tessellator.getBuffer();
				GlStateManager.rotate(180F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
				GlStateManager.rotate((float)(this.renderManager.options.thirdPersonView == 2 ? -1 : 1) * -this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
				GlStateManager.rotate(9f * (partialTicks + entity.ticksExisted), 0.0F, 0.0F, 1.0F);
				GlStateManager.disableLighting();
				OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
				bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_NORMAL);
				bufferbuilder.pos(-0.5D, -0.5D, 0.0D).tex(0.0D, 1.0D).normal(0.0F, 1.0F, 0.0F).endVertex();
				bufferbuilder.pos(0.5D, -0.5D, 0.0D).tex(1.0D, 1.0D).normal(0.0F, 1.0F, 0.0F).endVertex();
				bufferbuilder.pos(0.5D, 0.5D, 0.0D).tex(1.0D, 0.0D).normal(0.0F, 1.0F, 0.0F).endVertex();
				bufferbuilder.pos(-0.5D, 0.5D, 0.0D).tex(0.0D, 0.0D).normal(0.0F, 1.0F, 0.0F).endVertex();
				tessellator.draw();
				GlStateManager.enableLighting();
				GlStateManager.disableRescaleNormal();
				GlStateManager.popMatrix();
			}

			@Override
			protected ResourceLocation getEntityTexture(EntityScorch2 entity) {
				return this.texture;
			}
		}

		@SideOnly(Side.CLIENT)
		public class RenderCustom extends Render<EntityScorchBall> {
			private final ResourceLocation texture = new ResourceLocation("narutomod:textures/fireball2.png");
	
			public RenderCustom(RenderManager renderManager) {
				super(renderManager);
				shadowSize = 0.1f;
			}
	
			@Override
			public void doRender(EntityScorchBall entity, double x, double y, double z, float entityYaw, float partialTicks) {
				GlStateManager.pushMatrix();
				this.bindEntityTexture(entity);
				float scale = entity.getEntityScale();
				GlStateManager.translate(x, y + 0.5d * scale, z);
				GlStateManager.enableRescaleNormal();
				GlStateManager.scale(scale, scale, scale);
				Tessellator tessellator = Tessellator.getInstance();
				BufferBuilder bufferbuilder = tessellator.getBuffer();
				GlStateManager.rotate(180F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
				GlStateManager.rotate((float)(this.renderManager.options.thirdPersonView == 2 ? -1 : 1) * -this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
				GlStateManager.rotate(9f * (partialTicks + entity.ticksExisted), 0.0F, 0.0F, 1.0F);
				GlStateManager.disableLighting();
				OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
				bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_NORMAL);
				bufferbuilder.pos(-0.5D, -0.5D, 0.0D).tex(0.0D, 1.0D).normal(0.0F, 1.0F, 0.0F).endVertex();
				bufferbuilder.pos(0.5D, -0.5D, 0.0D).tex(1.0D, 1.0D).normal(0.0F, 1.0F, 0.0F).endVertex();
				bufferbuilder.pos(0.5D, 0.5D, 0.0D).tex(1.0D, 0.0D).normal(0.0F, 1.0F, 0.0F).endVertex();
				bufferbuilder.pos(-0.5D, 0.5D, 0.0D).tex(0.0D, 0.0D).normal(0.0F, 1.0F, 0.0F).endVertex();
				tessellator.draw();
				GlStateManager.enableLighting();
				GlStateManager.disableRescaleNormal();
				GlStateManager.popMatrix();
			}
	
			@Override
			protected ResourceLocation getEntityTexture(EntityScorchBall entity) {
				return this.texture;
			}
		}
	}
}
