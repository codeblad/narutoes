
package net.narutomod.entity;

import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

import net.minecraft.init.SoundEvents;
import net.minecraft.init.MobEffects;
import net.minecraft.world.World;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundCategory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.Entity;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelBase;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;

import net.narutomod.procedure.ProcedureAoeCommand;
import net.narutomod.procedure.ProcedureUtils;
import net.narutomod.item.ItemJutsu;
import net.narutomod.ElementsNarutomodMod;

import java.util.List;
import com.google.common.collect.Lists;

@ElementsNarutomodMod.ModElement.Tag
public class EntityKirin extends ElementsNarutomodMod.ModElement {
	public static final int ENTITYID = 373;
	public static final int ENTITYID_RANGED = 374;

	public EntityKirin(ElementsNarutomodMod instance) {
		super(instance, 734);
	}

	@Override
	public void initElements() {
		elements.entities.add(() -> EntityEntryBuilder.create().entity(EC.class).id(new ResourceLocation("narutomod", "kirin"), ENTITYID)
				.name("kirin").tracker(128, 3, true).build());
	}

	public static class EC extends EntityScalableProjectile.Base implements ItemJutsu.IJutsu {
		private final int wait = 60;
		private Vec3d shootVec;
		private float prevHeadYaw;
		private float prevHeadPitch;
		private Vec3d lastVec;
		private final List<ProcedureUtils.Vec2f> partRot = Lists.newArrayList(
			new ProcedureUtils.Vec2f(45.0f, 0.0f), new ProcedureUtils.Vec2f(-45.0f, 0.0f), new ProcedureUtils.Vec2f(-45.0f, 0.0f),
			new ProcedureUtils.Vec2f(-22.5f, 0.0f), new ProcedureUtils.Vec2f(45.0f, 0.0f), new ProcedureUtils.Vec2f(45.0f, 0.0f),
			new ProcedureUtils.Vec2f(45.0f, 0.0f), new ProcedureUtils.Vec2f(-45.0f, 0.0f)
		);

		public EC(World w) {
			super(w);
			this.setOGSize(1.0F, 1.0F);
			this.setEntityScale(10.0f);
		}

		public EC(EntityLivingBase shooter) {
			super(shooter);
			this.setOGSize(1.0F, 1.0F);
			this.setEntityScale(10.0f);
			this.setLocationAndAngles(shooter.posX, shooter.posY + 100d, shooter.posZ, shooter.rotationYaw, 80f);
			this.playSound(SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:dragon_roar")),
			 100f, this.rand.nextFloat() * 0.4f + 0.8f);
//this.shootVec = new Vec3d(0d, -1d, 0d);
		}

		@Override
		public ItemJutsu.JutsuEnum.Type getJutsuType() {
			return ItemJutsu.JutsuEnum.Type.RAITON;
		}

		private void setWaitPosition() {
			if (this.shootVec != null) {
				ProcedureUtils.Vec2f v2f = ProcedureUtils.getYawPitchFromVec(this.shootVec);
				this.setRotation(v2f.x, v2f.y);
			} else if (this.shootingEntity != null) {
				Vec3d vec = this.shootingEntity instanceof EntityLiving && ((EntityLiving)this.shootingEntity).getAttackTarget() != null
				 ? ((EntityLiving)this.shootingEntity).getAttackTarget().getPositionVector().subtract(this.getPositionVector())
				 : ProcedureUtils.objectEntityLookingAt(this.shootingEntity, 50d).hitVec.subtract(this.getPositionVector());
				ProcedureUtils.Vec2f v2f = ProcedureUtils.getYawPitchFromVec(vec);
				this.setRotation(v2f.x, v2f.y);
			}
			this.motionY -= this.ticksAlive <= this.wait / 2 ? 0.03d : 0.0d;
		}

		private void shoot(double x, double y, double z) {
			this.shoot(x, y, z, 1.2f, 0f);
			this.playSound(SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:lightning_shoot")),
			 100f, this.rand.nextFloat() * 0.4f + 0.8f);
		}

		@Override
		public void onUpdate() {
			super.onUpdate();
			if (!this.world.isRemote && (this.ticksAlive > 100 || this.shootingEntity == null || !this.shootingEntity.isEntityAlive())) {
				this.setDead();
			} else {
				if (this.ticksAlive <= this.wait) {
					if (this.lastVec == null) {
						this.lastVec = this.getPositionVector();
					}
					this.setWaitPosition();
				} else if (!this.isLaunched()) {
					this.motionY = 0.0d;
					if (this.shootVec != null) {
						this.shoot(this.shootVec.x, this.shootVec.y, this.shootVec.z);
					} else if (this.shootingEntity != null) {
						Vec3d vec = this.shootingEntity instanceof EntityLiving && ((EntityLiving)this.shootingEntity).getAttackTarget() != null
						 ? ((EntityLiving)this.shootingEntity).getAttackTarget().getPositionVector().subtract(this.getPositionVector())
						 : ProcedureUtils.objectEntityLookingAt(this.shootingEntity, 50d).hitVec.subtract(this.getPositionVector());
						this.shoot(vec.x, vec.y, vec.z);
					}
				}
				this.updateSegments();
				this.prevHeadYaw = this.rotationYaw;
				this.prevHeadPitch = this.rotationPitch;
			}
		}

		public void updateSegments() {
			Vec3d cposvec = this.getPositionVector();
			float slength = this.getEntityScale() * 11.0F * 0.0625F;
			ProcedureUtils.Vec2f vec = new ProcedureUtils.Vec2f(this.rotationYaw, this.rotationPitch)
			 .subtract(this.prevHeadYaw, this.prevHeadPitch);
			Vec3d vec4 = cposvec.subtract(this.lastVec);
			double d4 = vec4.lengthVector();
			if (d4 >= slength) {
				this.partRot.add(0, vec);
				int i = 1;
				for ( ; i < (int)(d4 / slength); i++) {
					this.partRot.add(0, ProcedureUtils.Vec2f.ZERO);
				}
				this.lastVec = vec4.normalize().scale(slength * i).add(this.lastVec);
			} else {
				this.partRot.set(0, this.partRot.get(0).add(vec));
			}
		}

		@Override
		public void renderParticles() {
			if (!this.world.isRemote) {
				AxisAlignedBB bb = this.getEntityBoundingBox();
				for (int i = 0; i < 1 + this.rand.nextInt(4); i++) {
					double x1, y1, z1, x2, y2, z2;
					switch (this.rand.nextInt(4)) {
						case 0:
							x1 = bb.minX;
							x2 = bb.minX - this.rand.nextDouble() * 25d;
							z1 = bb.minZ + this.rand.nextDouble() * this.width;
							z2 = z1 + (this.rand.nextDouble()-0.5d) * 25d;
							break;
						case 1:
							x1 = bb.maxX;
							x2 = bb.maxX + this.rand.nextDouble() * 25d;
							z1 = bb.minZ + this.rand.nextDouble() * this.width;
							z2 = z1 + (this.rand.nextDouble()-0.5d) * 25d;
							break;
						case 2:
							x1 = bb.minX + this.rand.nextDouble() * this.width;
							x2 = x1 + (this.rand.nextDouble()-0.5d) * 25d;
							z1 = bb.minZ;
							z2 = bb.minZ - this.rand.nextDouble() * 25d;
							break;
						default:
							x1 = bb.minX + this.rand.nextDouble() * this.width;
							x2 = x1 + (this.rand.nextDouble()-0.5d) * 25d;
							z1 = bb.maxZ;
							z2 = bb.maxZ + this.rand.nextDouble() * 25d;
							break;
					}
					y1 = bb.minY + this.rand.nextDouble() * this.height;
					y2 = y1 + this.rand.nextDouble() * 12.5d;
					EntityLightningArc.Base entity = new EntityLightningArc.Base(this.world, new Vec3d(x1, y1, z1), new Vec3d(x2, y2, z2), 0xc00000ff, 1, 0f, 0.3f).setStatic();
					entity.motionX = this.motionX;
					entity.motionY = this.motionY;
					entity.motionZ = this.motionZ;
					this.world.spawnEntity(entity);
				}
			}
		}

		@Override
		protected void onImpact(RayTraceResult result) {
			if (result.entityHit != null && result.entityHit.equals(this.shootingEntity))
				return;
			if (!this.world.isRemote) {
				this.playSound(SoundEvents.ENTITY_LIGHTNING_IMPACT, 5.0F, 0.5F + this.rand.nextFloat() * 0.2F);
				Vec3d vec = result.entityHit != null ? result.entityHit.getPositionVector() : result.hitVec;
				EntityLightningArc.Base entity = new EntityLightningArc.Base(this.world, vec, vec.addVector(0d, 150d, 0d), 0xc00000ff, 40, 0f, 6f);
				this.world.spawnEntity(entity);
				entity = new EntityLightningArc.Base(this.world, vec.subtract(0d, 4d, 0d), vec.addVector(0d, 150d, 0d), 0xc00000ff, 40, 0f, 12f);
				this.world.spawnEntity(entity);
				float size = this.getEntityScale();
				boolean flag = net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.world, this.shootingEntity);
				this.world.newExplosion(this.shootingEntity, vec.x, vec.y, vec.z, size, flag, flag);
				ProcedureAoeCommand.set(this.world, vec.x, vec.y, vec.z, 0.0D, 24.0D).exclude(this).exclude(this.shootingEntity)
				 .setFire(15).damageEntities(ItemJutsu.causeJutsuDamage(this, this.shootingEntity), (35f*ItemJutsu.getDmgMult(this.shootingEntity)+75));
			}
			//this.haltMotion();
			this.setDead();
		}

		@Override
		protected void checkOnGround() {
		}

		@Override
		public boolean isImmuneToExplosions() {
			return true;
		}

		public static class Jutsu implements ItemJutsu.IJutsuCallback {
			@Override
			public boolean createJutsu(ItemStack stack, EntityLivingBase entity, float power) {
				if (power >= 1.0f) {
				 	entity.world.spawnEntity(new EC(entity));
				 	if (entity instanceof EntityPlayer && !((EntityPlayer)entity).isCreative()) {
				 		entity.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 300, 0, false, false));
				 		ItemJutsu.setCurrentJutsuCooldown(stack, entity, 3600);
				 	}
					return true;
				}
				return false;
			}

			@Override
			public float getBasePower() {
				return 0.0f;
			}
	
			@Override
			public float getPowerupDelay() {
				return 400.0f;
			}
	
			@Override
			public float getMaxPower() {
				return 1.0f;
			}
		}
	}

	public static void chargingEffects(EntityLivingBase player, float pct) {
		if (pct > 0.8f && pct < 0.805f) {
			player.world.playSound(null, player.posX, player.posY, player.posZ,
			 SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:kirin_dialog")),
			 SoundCategory.PLAYERS, 5f, 1f);
		}
		for (int i = 0; i < player.getRNG().nextInt(11); i++) {
			Vec3d vec = player.getPositionVector().addVector((player.getRNG().nextDouble()-0.5d) * 120d,
			 95d + player.getRNG().nextDouble() * 10d, (player.getRNG().nextDouble()-0.5d) * 120d);
			EntityLightningArc.Base entity = new EntityLightningArc.Base(player.world, vec,
			 player.getRNG().nextDouble() * 120d + 10d, 0d, 0d, 0d, 0xc00000ff, 1, 0f, 0f).setStatic();
			player.world.spawnEntity(entity);
		}
	}

	public static void startWeatherThunder(Entity entity, int ticks) {
		entity.world.getWorldInfo().setCleanWeatherTime(0);
		entity.world.getWorldInfo().setRainTime(ticks);
		entity.world.getWorldInfo().setThunderTime(ticks);
		entity.world.getWorldInfo().setRaining(true);
		entity.world.getWorldInfo().setThundering(true);
		entity.world.playSound(null, entity.posX, entity.posY + 100d, entity.posZ,
		 SoundEvents.ENTITY_LIGHTNING_THUNDER, SoundCategory.WEATHER, 1000.0F, 0.8F + entity.world.rand.nextFloat() * 0.2F);
	}

	@Override
	public void preInit(FMLPreInitializationEvent event) {
		new Renderer().register();
	}

	public static class Renderer extends EntityRendererRegister {
		@SideOnly(Side.CLIENT)
		@Override
		public void register() {
			RenderingRegistry.registerEntityRenderingHandler(EC.class, renderManager -> {
				return new RenderDragon(renderManager);
			});
		}

		@SideOnly(Side.CLIENT)
		public class RenderDragon extends Render<EC> {
			private final ResourceLocation texture = new ResourceLocation("narutomod:textures/dragon_lightning.png");
			private final ResourceLocation texture2 = new ResourceLocation("narutomod:textures/electric_armor.png");
			private final ModelDragonHead model = new ModelDragonHead();
	
			public RenderDragon(RenderManager renderManager) {
				super(renderManager);
				//this.model = new ModelDragonHead();
				this.shadowSize = 0.0F;
			}
	
			@Override
			public boolean shouldRender(EC livingEntity, net.minecraft.client.renderer.culling.ICamera camera,
			 double camX, double camY, double camZ) {
				return true;
			}
	
			@Override
			public void doRender(EC entity, double x, double y, double z, float yaw, float pt) {
				float age = (float)entity.ticksExisted + pt;
				float f0 = Math.min(age / (float)entity.wait, 1.0f);
				float f1 = -entity.prevRotationYaw - MathHelper.wrapDegrees(entity.rotationYaw - entity.prevRotationYaw) * pt;
				float f2 = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * pt;
				boolean flag = entity.ticksAlive <= entity.wait;
				float scale = entity.getEntityScale();
				this.model.setRotationAngles(0f, 0f, age, 0f, 0f, 0.0625F, entity);
				GlStateManager.pushMatrix();
				GlStateManager.translate((float) x, (float) y + scale, (float) z);
				GlStateManager.rotate(f1, 0.0F, 1.0F, 0.0F);
				GlStateManager.rotate(f2 - 180F, 1.0F, 0.0F, 0.0F);
				GlStateManager.scale(scale, scale, scale);
				GlStateManager.enableBlend();
				GlStateManager.alphaFunc(0x204, 0.001f);
				GlStateManager.disableCull();
				GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
				GlStateManager.disableLighting();
				OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
				this.bindEntityTexture(entity);
				GlStateManager.color(1.0F, 1.0F, 1.0F, f0 * 0.5F);
				this.model.teethUpper.showModel = true;
				this.model.teethLower.showModel = true;
				this.model.eyes.showModel = true;
				this.model.render(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
				GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
				//if (entity.ticksExisted % 10 <= 5) {
					this.bindTexture(texture2);
					GlStateManager.matrixMode(5890);
					GlStateManager.loadIdentity();
					GlStateManager.translate(age * 0.01F, age * 0.01F, 0.0F);
					GlStateManager.matrixMode(5888);
					this.model.teethUpper.showModel = false;
					this.model.teethLower.showModel = false;
					this.model.eyes.showModel = false;
					GlStateManager.color(1.0F, 1.0F, 1.0F, f0 * 0.5F);
					this.model.render(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F * 1.05F);
					GlStateManager.matrixMode(5890);
					GlStateManager.loadIdentity();
					GlStateManager.matrixMode(5888);
				//}
				this.bindEntityTexture(entity);
				this.model.teethUpper.showModel = false;
				this.model.teethLower.showModel = false;
				this.model.eyes.showModel = false;
				GlStateManager.color(0.0F, 0.0F, 1.0F, f0 * 0.3F);
				this.model.render(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F * 1.1F);
				GlStateManager.enableLighting();
				GlStateManager.enableCull();
				GlStateManager.alphaFunc(0x204, 0.1f);
				GlStateManager.disableBlend();
				GlStateManager.popMatrix();
			}
	
			@Override
			protected ResourceLocation getEntityTexture(EC entity) {
				return texture;
			}
		}
	
		// Made with Blockbench 3.5.4
		// Exported for Minecraft version 1.12
		// Paste this class into your mod and generate all required imports
		@SideOnly(Side.CLIENT)
		public class ModelDragonHead extends ModelBase {
			private final ModelRenderer head;
			private final ModelRenderer bone;
			private final ModelRenderer bone2;
			private final ModelRenderer bone3;
			private final ModelRenderer teethUpper;
			private final ModelRenderer teethLower;
			private final ModelRenderer jaw;
			private final ModelRenderer hornRight;
			private final ModelRenderer hornRight0;
			private final ModelRenderer hornRight1;
			private final ModelRenderer hornRight2;
			private final ModelRenderer hornRight3;
			private final ModelRenderer hornRight4;
			private final ModelRenderer hornLeft;
			private final ModelRenderer hornLeft0;
			private final ModelRenderer hornLeft1;
			private final ModelRenderer hornLeft2;
			private final ModelRenderer hornLeft3;
			private final ModelRenderer hornLeft4;
			private final ModelRenderer[] whiskerLeft = new ModelRenderer[6];
			private final ModelRenderer[] whiskerRight = new ModelRenderer[6];
			private final ModelRenderer[] spine = new ModelRenderer[100];
			private final ModelRenderer eyes;
	
			public ModelDragonHead() {
				textureWidth = 256;
				textureHeight = 256;
	
				head = new ModelRenderer(this);
				head.setRotationPoint(0.0F, 0.0F, 0.0F);
				head.cubeList.add(new ModelBox(head, 176, 44, -6.0F, 6.0F, -26.0F, 12, 5, 16, 1.0F, false));
				head.cubeList.add(new ModelBox(head, 112, 30, -8.0F, -1.0F, -11.0F, 16, 16, 16, 1.0F, false));
				head.cubeList.add(new ModelBox(head, 112, 0, -5.0F, 5.0F, -26.0F, 2, 2, 4, 1.0F, false));
				head.cubeList.add(new ModelBox(head, 112, 0, 3.0F, 5.0F, -26.0F, 2, 2, 4, 1.0F, true));
		
				teethUpper = new ModelRenderer(this);
				teethUpper.setRotationPoint(0.0F, 24.0F, 0.0F);
				head.addChild(teethUpper);
				teethUpper.cubeList.add(new ModelBox(teethUpper, 152, 146, -6.0F, -12.0F, -26.0F, 12, 2, 16, 0.5F, false));
	
				bone = new ModelRenderer(this);
				bone.setRotationPoint(9.0F, 7.0F, -11.0F);
				head.addChild(bone);
				setRotationAngle(bone, 0.0F, -0.7854F, 0.0F);
				bone.cubeList.add(new ModelBox(bone, 0, 200, 0.0F, -8.0F, 0.0F, 8, 16, 0, 0.0F, false));
		
				bone2 = new ModelRenderer(this);
				bone2.setRotationPoint(-9.0F, 7.0F, -11.0F);
				head.addChild(bone2);
				setRotationAngle(bone2, 0.0F, 0.7854F, 0.0F);
				bone2.cubeList.add(new ModelBox(bone2, 0, 200, -8.0F, -8.0F, 0.0F, 8, 16, 0, 0.0F, true));
		
				bone3 = new ModelRenderer(this);
				bone3.setRotationPoint(0.0F, -2.0F, -11.0F);
				head.addChild(bone3);
				setRotationAngle(bone3, -0.8727F, 0.0F, 0.0F);
				bone3.cubeList.add(new ModelBox(bone3, 0, 50, -8.0F, -10.0F, 0.0F, 16, 10, 0, 0.0F, false));
	
				jaw = new ModelRenderer(this);
				jaw.setRotationPoint(0.0F, 11.0F, -9.0F);
				head.addChild(jaw);
				jaw.cubeList.add(new ModelBox(jaw, 176, 65, -6.0F, 0.0F, -16.75F, 12, 4, 16, 1.0F, false));
		
				teethLower = new ModelRenderer(this);
				teethLower.setRotationPoint(0.0F, 13.0F, 9.0F);
				jaw.addChild(teethLower);
				teethLower.cubeList.add(new ModelBox(teethLower, 112, 144, -6.0F, -16.0F, -25.75F, 12, 2, 16, 0.5F, false));
	
				hornRight = new ModelRenderer(this);
				hornRight.setRotationPoint(-6.0F, -2.0F, -13.0F);
				head.addChild(hornRight);
				setRotationAngle(hornRight, 0.0873F, -0.5236F, 0.0F);
				hornRight.cubeList.add(new ModelBox(hornRight, 0, 0, -1.0F, -2.0F, 0.0F, 2, 4, 6, 1.0F, false));
		
				hornRight0 = new ModelRenderer(this);
				hornRight0.setRotationPoint(0.0F, 0.0F, 7.0F);
				hornRight.addChild(hornRight0);
				setRotationAngle(hornRight0, 0.0873F, 0.0873F, 0.0F);
				hornRight0.cubeList.add(new ModelBox(hornRight0, 0, 0, -1.0F, -2.0F, 0.0F, 2, 4, 6, 0.8F, false));
		
				hornRight1 = new ModelRenderer(this);
				hornRight1.setRotationPoint(0.0F, 0.0F, 7.0F);
				hornRight0.addChild(hornRight1);
				setRotationAngle(hornRight1, 0.0873F, 0.0873F, 0.0F);
				hornRight1.cubeList.add(new ModelBox(hornRight1, 0, 0, -1.0F, -2.0F, 0.0F, 2, 4, 6, 0.6F, false));
		
				hornRight2 = new ModelRenderer(this);
				hornRight2.setRotationPoint(0.0F, 0.0F, 7.0F);
				hornRight1.addChild(hornRight2);
				setRotationAngle(hornRight2, 0.0873F, 0.0873F, 0.0F);
				hornRight2.cubeList.add(new ModelBox(hornRight2, 0, 0, -1.0F, -2.0F, 0.0F, 2, 4, 6, 0.4F, false));
		
				hornRight3 = new ModelRenderer(this);
				hornRight3.setRotationPoint(0.0F, 0.0F, 7.0F);
				hornRight2.addChild(hornRight3);
				setRotationAngle(hornRight3, 0.0873F, 0.0873F, 0.0F);
				hornRight3.cubeList.add(new ModelBox(hornRight3, 0, 0, -1.0F, -2.0F, 0.0F, 2, 4, 6, 0.2F, false));
		
				hornRight4 = new ModelRenderer(this);
				hornRight4.setRotationPoint(0.0F, 0.0F, 7.0F);
				hornRight3.addChild(hornRight4);
				setRotationAngle(hornRight4, 0.0873F, 0.0873F, 0.0F);
				hornRight4.cubeList.add(new ModelBox(hornRight4, 0, 0, -1.0F, -2.0F, 0.0F, 2, 4, 6, 0.0F, false));
		
				hornLeft = new ModelRenderer(this);
				hornLeft.setRotationPoint(6.0F, -2.0F, -13.0F);
				head.addChild(hornLeft);
				setRotationAngle(hornLeft, 0.0873F, 0.5236F, 0.0F);
				hornLeft.cubeList.add(new ModelBox(hornLeft, 0, 0, -1.0F, -2.0F, 0.0F, 2, 4, 6, 1.0F, true));
		
				hornLeft0 = new ModelRenderer(this);
				hornLeft0.setRotationPoint(0.0F, 0.0F, 7.0F);
				hornLeft.addChild(hornLeft0);
				setRotationAngle(hornLeft0, 0.0873F, -0.0873F, 0.0F);
				hornLeft0.cubeList.add(new ModelBox(hornLeft0, 0, 0, -1.0F, -2.0F, 0.0F, 2, 4, 6, 0.8F, true));
		
				hornLeft1 = new ModelRenderer(this);
				hornLeft1.setRotationPoint(0.0F, 0.0F, 7.0F);
				hornLeft0.addChild(hornLeft1);
				setRotationAngle(hornLeft1, 0.0873F, -0.0873F, 0.0F);
				hornLeft1.cubeList.add(new ModelBox(hornLeft1, 0, 0, -1.0F, -2.0F, 0.0F, 2, 4, 6, 0.6F, true));
		
				hornLeft2 = new ModelRenderer(this);
				hornLeft2.setRotationPoint(0.0F, 0.0F, 7.0F);
				hornLeft1.addChild(hornLeft2);
				setRotationAngle(hornLeft2, 0.0873F, -0.0873F, 0.0F);
				hornLeft2.cubeList.add(new ModelBox(hornLeft2, 0, 0, -1.0F, -2.0F, 0.0F, 2, 4, 6, 0.4F, true));
		
				hornLeft3 = new ModelRenderer(this);
				hornLeft3.setRotationPoint(0.0F, 0.0F, 7.0F);
				hornLeft2.addChild(hornLeft3);
				setRotationAngle(hornLeft3, 0.0873F, -0.0873F, 0.0F);
				hornLeft3.cubeList.add(new ModelBox(hornLeft3, 0, 0, -1.0F, -2.0F, 0.0F, 2, 4, 6, 0.2F, true));
		
				hornLeft4 = new ModelRenderer(this);
				hornLeft4.setRotationPoint(0.0F, 0.0F, 7.0F);
				hornLeft3.addChild(hornLeft4);
				setRotationAngle(hornLeft4, 0.0873F, -0.0873F, 0.0F);
				hornLeft4.cubeList.add(new ModelBox(hornLeft4, 0, 0, -1.0F, -2.0F, 0.0F, 2, 4, 6, 0.0F, true));
		
				whiskerLeft[0] = new ModelRenderer(this);
				whiskerLeft[0].setRotationPoint(6.0F, 6.0F, -24.0F);
				head.addChild(whiskerLeft[0]);
				setRotationAngle(whiskerLeft[0], 0.0F, 1.0472F, 0.0F);
				whiskerLeft[0].cubeList.add(new ModelBox(whiskerLeft[0], 0, 0, -1.0F, -1.0F, 0.0F, 2, 2, 6, 0.8F, true));
		
				whiskerLeft[1] = new ModelRenderer(this);
				whiskerLeft[1].setRotationPoint(0.0F, 0.0F, 6.0F);
				whiskerLeft[0].addChild(whiskerLeft[1]);
				setRotationAngle(whiskerLeft[1], -0.0873F, -0.1745F, 0.0F);
				whiskerLeft[1].cubeList.add(new ModelBox(whiskerLeft[1], 0, 0, -1.0F, -1.0F, 0.0F, 2, 2, 6, 0.7F, true));
		
				whiskerLeft[2] = new ModelRenderer(this);
				whiskerLeft[2].setRotationPoint(0.0F, 0.0F, 6.0F);
				whiskerLeft[1].addChild(whiskerLeft[2]);
				setRotationAngle(whiskerLeft[2], -0.0873F, -0.1745F, 0.0F);
				whiskerLeft[2].cubeList.add(new ModelBox(whiskerLeft[2], 0, 0, -1.0F, -1.0F, 0.0F, 2, 2, 6, 0.6F, true));
		
				whiskerLeft[3] = new ModelRenderer(this);
				whiskerLeft[3].setRotationPoint(0.0F, 0.0F, 6.0F);
				whiskerLeft[2].addChild(whiskerLeft[3]);
				setRotationAngle(whiskerLeft[3], -0.0873F, -0.1745F, 0.0F);
				whiskerLeft[3].cubeList.add(new ModelBox(whiskerLeft[3], 0, 0, -1.0F, -1.0F, 0.0F, 2, 2, 6, 0.5F, true));
		
				whiskerLeft[4] = new ModelRenderer(this);
				whiskerLeft[4].setRotationPoint(0.0F, 0.0F, 6.0F);
				whiskerLeft[3].addChild(whiskerLeft[4]);
				setRotationAngle(whiskerLeft[4], -0.0873F, -0.1745F, 0.0F);
				whiskerLeft[4].cubeList.add(new ModelBox(whiskerLeft[4], 0, 0, -1.0F, -1.0F, 0.0F, 2, 2, 6, 0.4F, true));
		
				whiskerLeft[5] = new ModelRenderer(this);
				whiskerLeft[5].setRotationPoint(0.0F, 0.0F, 6.0F);
				whiskerLeft[4].addChild(whiskerLeft[5]);
				setRotationAngle(whiskerLeft[5], -0.0873F, -0.1745F, 0.0F);
				whiskerLeft[5].cubeList.add(new ModelBox(whiskerLeft[5], 0, 0, -1.0F, -1.0F, 0.0F, 2, 2, 6, 0.2F, true));
		
				whiskerRight[0] = new ModelRenderer(this);
				whiskerRight[0].setRotationPoint(-6.0F, 6.0F, -24.0F);
				head.addChild(whiskerRight[0]);
				setRotationAngle(whiskerRight[0], 0.0F, -1.0472F, 0.0F);
				whiskerRight[0].cubeList.add(new ModelBox(whiskerRight[0], 0, 0, -1.0F, -1.0F, 0.0F, 2, 2, 6, 0.8F, false));
		
				whiskerRight[1] = new ModelRenderer(this);
				whiskerRight[1].setRotationPoint(0.0F, 0.0F, 6.0F);
				whiskerRight[0].addChild(whiskerRight[1]);
				setRotationAngle(whiskerRight[1], -0.0873F, 0.1745F, 0.0F);
				whiskerRight[1].cubeList.add(new ModelBox(whiskerRight[1], 0, 0, -1.0F, -1.0F, 0.0F, 2, 2, 6, 0.7F, false));
		
				whiskerRight[2] = new ModelRenderer(this);
				whiskerRight[2].setRotationPoint(0.0F, 0.0F, 6.0F);
				whiskerRight[1].addChild(whiskerRight[2]);
				setRotationAngle(whiskerRight[2], -0.0873F, 0.1745F, 0.0F);
				whiskerRight[2].cubeList.add(new ModelBox(whiskerRight[2], 0, 0, -1.0F, -1.0F, 0.0F, 2, 2, 6, 0.6F, false));
		
				whiskerRight[3] = new ModelRenderer(this);
				whiskerRight[3].setRotationPoint(0.0F, 0.0F, 6.0F);
				whiskerRight[2].addChild(whiskerRight[3]);
				setRotationAngle(whiskerRight[3], -0.0873F, 0.1745F, 0.0F);
				whiskerRight[3].cubeList.add(new ModelBox(whiskerRight[3], 0, 0, -1.0F, -1.0F, 0.0F, 2, 2, 6, 0.5F, false));
		
				whiskerRight[4] = new ModelRenderer(this);
				whiskerRight[4].setRotationPoint(0.0F, 0.0F, 6.0F);
				whiskerRight[3].addChild(whiskerRight[4]);
				setRotationAngle(whiskerRight[4], -0.0873F, 0.1745F, 0.0F);
				whiskerRight[4].cubeList.add(new ModelBox(whiskerRight[4], 0, 0, -1.0F, -1.0F, 0.0F, 2, 2, 6, 0.4F, false));
		
				whiskerRight[5] = new ModelRenderer(this);
				whiskerRight[5].setRotationPoint(0.0F, 0.0F, 6.0F);
				whiskerRight[4].addChild(whiskerRight[5]);
				setRotationAngle(whiskerRight[5], -0.0873F, 0.1745F, 0.0F);
				whiskerRight[5].cubeList.add(new ModelBox(whiskerRight[5], 0, 0, -1.0F, -1.0F, 0.0F, 2, 2, 6, 0.2F, false));
		
				for (int i = 0; i < spine.length; i++) {
					spine[i] = new ModelRenderer(this);
					spine[i].cubeList.add(new ModelBox(spine[i], 192, 104, -5.0F, -4.5F, 0.0F, 10, 10, 10, 2.0F, false));
					spine[i].cubeList.add(new ModelBox(spine[i], 48, 0, -1.0F, -10.5F, 2.0F, 2, 4, 6, 1.0F, false));
					if (i == 0) {
						spine[i].setRotationPoint(0.0F, 6.5F, 7.0F);
					} else {
						spine[i].setRotationPoint(0.0F, 0.0F, 11.0F);
						spine[i-1].addChild(spine[i]);
					}
				}
	
				eyes = new ModelRenderer(this);
				eyes.setRotationPoint(0.0F, 0.0F, 0.0F);
				eyes.cubeList.add(new ModelBox(eyes, 130, 50, -6.6F, 2.6F, -12.1F, 3, 2, 0, 0.0F, false));
				eyes.cubeList.add(new ModelBox(eyes, 130, 50, 3.6F, 2.6F, -12.1F, 3, 2, 0, 0.0F, true));
			}
	
			@Override
			public void render(Entity entityIn, float f, float f1, float f2, float f3, float f4, float f5) {
				this.head.render(f5);
				this.spine[0].render(f5);
				this.eyes.render(f5);
			}
	
			public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
				modelRenderer.rotateAngleX = x;
				modelRenderer.rotateAngleY = y;
				modelRenderer.rotateAngleZ = z;
			}
	
			@Override
			public void setRotationAngles(float f0, float f1, float ageInTicks, float f3, float headPitch, float f5, Entity e) {
				super.setRotationAngles(f0, f1, ageInTicks, f3, headPitch, f5, e);
				EC entity = (EC)e;
				float pt = ageInTicks - e.ticksExisted;
				float f6 = (float)Math.PI / 180.0F;
				this.head.rotateAngleX = headPitch * f6;
				if (entity.ticksAlive > entity.wait) {
					this.jaw.rotateAngleX = 0.5236F;
				}
				for (int i = 2; i < 6; i++) {
					whiskerLeft[i].rotateAngleZ = 0.2618F * ageInTicks;
					whiskerRight[i].rotateAngleZ = -0.2618F * ageInTicks;
				}
				for (int i = 0; i < this.spine.length; i++) {
					if (i < entity.partRot.size()) {
						this.spine[i].showModel = true;
						ProcedureUtils.Vec2f vec = entity.partRot.get(i);
						this.spine[i].rotateAngleX = -vec.y * f6;
						this.spine[i].rotateAngleY = -vec.x * f6;
					} else {
						this.spine[i].showModel = false;
					}
				}
			}
		}
	}
}
