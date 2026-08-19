
package net.narutomod.entity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.*;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

import net.minecraft.world.World;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
//import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundCategory;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;

import net.narutomod.Particles;
import net.narutomod.item.ItemByakugan;
import net.narutomod.item.ItemJutsu;
import net.narutomod.ElementsNarutomodMod;
import net.narutomod.potion.PotionUsingJutsu;
import net.narutomod.procedure.ProcedureSync;
import net.narutomod.procedure.ProcedureUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@ElementsNarutomodMod.ModElement.Tag
public class EntityIceSpear extends ElementsNarutomodMod.ModElement {
	public static final int ENTITYID = 222;
	public static final int ENTITYID_RANGED = 223;

	public EntityIceSpear(ElementsNarutomodMod instance) {
		super(instance, 534);
	}

	@Override
	public void initElements() {
		elements.entities.add(() -> EntityEntryBuilder.create().entity(EC.class)
		 .id(new ResourceLocation("narutomod", "ice_spear"), ENTITYID).name("ice_spear").tracker(64, 3, true).build());
	}

	@Override
	public void preInit(FMLPreInitializationEvent event) {
		new Renderer().register();
	}

	public static class Renderer extends EntityRendererRegister {
		@SideOnly(Side.CLIENT)
		@Override
		public void register() {
			RenderingRegistry.registerEntityRenderingHandler(EC.class, renderManager -> new CustomRender(renderManager));
		}

		@SideOnly(Side.CLIENT)
		public class CustomRender extends EntitySpike.ClientSide.Renderer<EC> {
			private final ResourceLocation texture = new ResourceLocation("narutomod:textures/spike_ice.png");
	
			public CustomRender(RenderManager renderManagerIn) {
				super(renderManagerIn);
			}
	
			@Override
			protected ResourceLocation getEntityTexture(EC entity) {
				return this.texture;
			}
		}
	}

	public static class EC extends EntitySpike.Base implements ItemJutsu.IJutsu {
		private static final DataParameter<Float> RAND_YAW = EntityDataManager.<Float>createKey(EC.class, DataSerializers.FLOAT);
		private static final DataParameter<Float> RAND_PITCH = EntityDataManager.<Float>createKey(EC.class, DataSerializers.FLOAT);

		public float baseImpactDamage = 10.0f;
		
		public EC(World world) {
			super(world);
			this.setColor(0xC0FFFFFF);
			this.setRandYawPitch();
		}

		public EC(EntityLivingBase userIn) {
			super(userIn, 0xC0FFFFFF);
			this.setRandYawPitch();
		}

		@Override
		public ItemJutsu.JutsuEnum.Type getJutsuType() {
			return ItemJutsu.JutsuEnum.Type.HYOTON;
		}

		@Override
		protected void entityInit() {
			super.entityInit();
			this.dataManager.register(RAND_YAW, Float.valueOf(0f));
			this.dataManager.register(RAND_PITCH, Float.valueOf(0f));
		}

		private float getRandYaw() {
			return ((Float)this.dataManager.get(RAND_YAW)).floatValue();
		}

		private float getRandPitch() {
			return ((Float)this.dataManager.get(RAND_PITCH)).floatValue();
		}

		private void setRandYawPitch() {
			this.dataManager.set(RAND_YAW, Float.valueOf((this.rand.nextFloat() - 0.5f) * 90f));
			this.dataManager.set(RAND_PITCH, Float.valueOf((this.rand.nextFloat() - 0.5f) * 60f));
		}

		@Override
		public void onUpdate() {
			super.onUpdate();
			if (!this.isLaunched() && !this.hasNoGravity() && !this.onGround) {
				this.rotationYaw += this.getRandYaw();
				this.rotationPitch += this.getRandPitch();
			}
			if (this.ticksExisted > 20*5) {
				this.setDead();
			}
		}

		@Override
		protected void onImpact(RayTraceResult result) {
			if (!this.world.isRemote 
			 && result.entityHit instanceof EntityLivingBase && !result.entityHit.equals(this.shootingEntity)) {
				if (result.entityHit.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, this.shootingEntity).setProjectile(), this.baseImpactDamage)) {
					//((EntityLivingBase)result.entityHit).addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 80, 1));
					this.setDead();
				} else if (!result.entityHit.noClip) {
					this.motionX *= -0.1d;
					this.motionY *= -0.1d;
					this.motionZ *= -0.1d;
					this.rotationYaw += 180.0F;
					this.prevRotationYaw += 180.0F;
				}
			}
		}

		public static class IceSpearMove extends Entity  {
			private EntityLivingBase user;
			private Vec3d look;
			private Vec3d start;
			private int lifeTime = 20;
			private double power;
			List<String> targets = new ArrayList<String>();

			public IceSpearMove(World worldIn) {
				super(worldIn);
				this.setSize(0.01f, 0.01f);
				this.isImmuneToFire = true;
			}


			public IceSpearMove(EntityLivingBase user, double power) {
				this(user.world);
				this.user = user;
				this.look = this.user.getLookVec();
				this.start = this.user.getPositionVector().addVector(0,1,0);
				this.setPosition(this.start.x,this.start.y,this.start.z);
				this.lifeTime = (int) (10+power*2);
				this.power = power;
			}


			@Override
			protected void entityInit() {
			}

			@Override
			public void onUpdate() {
				if (this.user != null) {
					this.start = this.user.getPositionVector().addVector(0,1,0);
					this.setPosition(this.start.x,this.start.y,this.start.z);
					float area = (float) (2+3*(this.power/20));
					int count = (int) (3+6*(this.power/20));
					if (!this.world.isRemote) {
						this.user.addPotionEffect(new PotionEffect(PotionUsingJutsu.potion, 5, 1, false, false));
					}
					world.playSound(null, this.start.x, this.start.y, this.start.z, SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:ice_shoot_small")),
							SoundCategory.NEUTRAL, 0.5f, world.rand.nextFloat() * 0.4f + 0.8f);
					for (double i = 0; i < count; i++) {
						this.look = this.user.getLookVec();
						Vec3d point = this.user.getPositionEyes(1).add(this.look.scale(2));
						point = point.addVector(-area+this.rand.nextFloat()*area*2,-area*.3+this.rand.nextFloat()*area*.6,-area+this.rand.nextFloat()*area*2);
						EC spear = new EC(this.user);
						spear.setEntityScale(0.5f);
						spear.setPositionAndUpdate(point.x,point.y,point.z);
						spear.shoot(this.look.x,this.look.y,this.look.z, 1.4f, 0);
						spear.setNoGravity(true);
						float mult = (float) (1+0.5*(this.power/20));
						spear.baseImpactDamage = 10f+(ItemJutsu.getDmgMult(this.user)*3f*mult);

						Particles.spawnParticle(this.world, Particles.Types.SMOKE, point.x, point.y, point.z,
								1, 1d, 0d, 1d, 0,0,0, 0x64B8F7FF, 35, 0);
						this.user.world.spawnEntity(spear);
					}
				}
				if (this.ticksExisted > this.lifeTime) {
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

		public static class Jutsu implements ItemJutsu.IJutsuCallback {
			@Override
			public boolean createJutsu(ItemStack stack, EntityLivingBase entity, float power) {
				/*Vec3d vec = entity.getLookVec();
				Vec3d vec1 = entity.getPositionEyes(1f).add(vec.scale(1.5d));
				double d = MathHelper.sqrt(power);
				for (int i = 0; i < (int)(power * 2f); i++) {
					Vec3d vec2 = vec1.addVector((entity.getRNG().nextDouble()-0.5d) * d, entity.getRNG().nextDouble()-0.5d,
					 (entity.getRNG().nextDouble()-0.5d) * d);
					Vec3d vec3 = vec2.add(vec);
					EC entity1 = this.createJutsu(entity.world, entity, vec2.x, vec2.y, vec2.z, vec3.x, vec3.y, vec3.z, 1.4f, 0.05f);
					entity1.baseImpactDamage = (4f+((2.0f*ItemJutsu.getDmgMult(entity)*(0.5f+1*(power/50)))));
				}*/
				entity.world.spawnEntity(new IceSpearMove(entity,power));
				ItemJutsu.setCurrentJutsuCooldown(stack, (long) (20*3+power*4));
				return true;
			}

			public void createJutsu(EntityLivingBase attacker, EntityLivingBase target, float power) {
				Vec3d vec1 = attacker.getPositionEyes(1f).add(attacker.getLookVec().scale(1.5d));
				for (int i = 0; i < (int)(power * 2f); i++) {
					Vec3d vec2 = vec1.addVector(attacker.getRNG().nextDouble()-0.5d, attacker.getRNG().nextDouble()-0.5d, attacker.getRNG().nextDouble()-0.5d);
					this.createJutsu(attacker.world, attacker, vec2.x, vec2.y, vec2.z, target.posX, target.posY + target.height/2, target.posZ, 1.4f, 0.05f);
				}
			}

			public void createJutsu(World world, int num, double fromX, double fromY, double fromZ, double toX, double toY, double toZ, float speed, float inaccuracy) {
				for (int i = 0; i < num; i++) {
					EC entity1 = this.createJutsu(world, null, fromX, fromY, fromZ, toX, toY, toZ, speed, inaccuracy);
					entity1.baseImpactDamage = 12f;
				}
			}

			public EC createJutsu(World world, @Nullable EntityLivingBase shooter,
			 double fromX, double fromY, double fromZ, double toX, double toY, double toZ, float speed, float inaccuracy) {
				world.playSound(null, fromX, fromY, fromZ, SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:ice_shoot_small")),
				 SoundCategory.NEUTRAL, 0.8f, world.rand.nextFloat() * 0.4f + 0.8f);
				EC entity1 = shooter != null ? new EC(shooter) : new EC(world);
				entity1.setEntityScale(0.5f);
				entity1.setPosition(fromX, fromY, fromZ);
				entity1.shoot(toX - fromX, toY - fromY, toZ - fromZ, speed, inaccuracy);
				entity1.setNoGravity(true);
				world.spawnEntity(entity1);
				return entity1;
			}

			@Override
			public float getPowerupDelay() {
				return 20.0f;
			}
	
			@Override
			public float getMaxPower() {
				return 20.0f;
			}
		}

		public static EC spawnShatteredShard(World worldIn, double x, double y, double z, double mX, double mY, double mZ) {
			EC entity = new EC(worldIn);
			entity.setEntityScale(worldIn.rand.nextFloat() * 0.5f + 0.05f);
			entity.setPositionAndRotation(x, y, z, entity.getRandYaw(), entity.getRandPitch());
			entity.motionX = mX;
			entity.motionY = mY;
			entity.motionZ = mZ;
			worldIn.spawnEntity(entity);
			return entity;
		}
	}
}
