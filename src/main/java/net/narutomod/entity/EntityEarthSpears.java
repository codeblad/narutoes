package net.narutomod.entity;

import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.ResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.client.renderer.entity.RenderManager;

import net.narutomod.item.ItemJutsu;
import net.narutomod.ElementsNarutomodMod;

@ElementsNarutomodMod.ModElement.Tag
public class EntityEarthSpears extends ElementsNarutomodMod.ModElement {

	public static final int ENTITYID = 243;
	public static final int ENTITYID_RANGED = 244;
	public static final float maxpower = 50.0f;
	public static final int maxwaves = 5;

	private static final List<WaveSpawn> pendingWaves = new ArrayList<WaveSpawn>();

	public EntityEarthSpears(ElementsNarutomodMod instance) {
		super(instance, 571);
		MinecraftForge.EVENT_BUS.register(new WaveTickHandler());
	}

	@Override
	public void initElements() {
		elements.entities.add(() -> EntityEntryBuilder.create().entity(EC.class)
			.id(new ResourceLocation("narutomod", "earth_spears"), ENTITYID)
			.name("earth_spears")
			.tracker(64, 3, true)
			.build());
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void preInit(FMLPreInitializationEvent event) {
		class CustomRender extends EntitySpike.ClientSide.Renderer<EC> {
			private final ResourceLocation texture = new ResourceLocation("narutomod:textures/spike_stone.png");

			public CustomRender(RenderManager renderManagerIn) {
				super(renderManagerIn);
			}

			@Override
			protected ResourceLocation getEntityTexture(EC entity) {
				return this.texture;
			}
		}

		RenderingRegistry.registerEntityRenderingHandler(EC.class, renderManager -> new CustomRender(renderManager));
	}

	private static class WaveSpawn {
		public final EntityLivingBase caster;
		public final ItemStack stack;
		public final float power;
		public final Vec3d hitVec;
		public final int wave;
		public int ticksLeft;

		public WaveSpawn(EntityLivingBase caster, ItemStack stack, float power, Vec3d hitVec, int wave, int ticksLeft) {
			this.caster = caster;
			this.stack = stack;
			this.power = power;
			this.hitVec = hitVec;
			this.wave = wave;
			this.ticksLeft = ticksLeft;
		}
	}

	public static class WaveTickHandler {

		@SubscribeEvent
		public void onServerTick(TickEvent.ServerTickEvent event) {
			if (event.phase != TickEvent.Phase.END) {
				return;
			}

			Iterator<WaveSpawn> iterator = pendingWaves.iterator();

			while (iterator.hasNext()) {
				WaveSpawn wave = iterator.next();
				wave.ticksLeft--;

				if (wave.ticksLeft <= 0) {
					if (wave.caster != null && !wave.caster.isDead && wave.caster.world != null && !wave.caster.world.isRemote) {
						spawnWave(wave.caster.world, wave.caster, wave.stack, wave.power, wave.hitVec, wave.wave);
					}

					iterator.remove();
				}
			}
		}
	}

	private static void spawnWave(World world, EntityLivingBase entity, ItemStack stack, float power, Vec3d hitVec, int wave) {
		float sizeMultiplier = 1.0f + (wave * 0.5f);
		float f = MathHelper.sqrt(power * 12f / 4f) * sizeMultiplier * ((wave + 1) / ((float) maxwaves));

		world.playSound(
			null,
			hitVec.x,
			hitVec.y,
			hitVec.z,
			net.minecraft.util.SoundEvent.REGISTRY.getObject(
				new ResourceLocation("narutomod:hand_press")
			),
			net.minecraft.util.SoundCategory.BLOCKS,
			5f,
			entity.getRNG().nextFloat() * 0.4f + 0.8f
		);
		
		Set<Entity> batchHitEntities = new HashSet<Entity>();
		int spikeCount = (int)Math.round(power * ((wave + 1) / ((float) maxwaves)));

		for (int i = 0; i < spikeCount; i++) {
			EC entity1 = new EC(entity, power, batchHitEntities);
			entity1.setEntityScale(0.0f);

			float castDamage = (2.0f + 1.45f * (1.0f + 2.0f * (power / maxpower))) * ItemJutsu.getDmgMult(entity);
			entity1.damage = castDamage;

			if (stack != null && stack.getTagCompound() != null && stack.getTagCompound().getBoolean("IsNatureAffinityKey")) {
				entity1.damage *= 1.35f;
			}

			Vec3d vec = hitVec.addVector(
				(entity.getRNG().nextDouble() - 0.5d) * f,
				0d,
				(entity.getRNG().nextDouble() - 0.5d) * f
			);

			for (; !world.getBlockState(new BlockPos(vec)).isTopSolid(); vec = vec.subtract(0d, 1d, 0d)) {
			}

			for (; world.getBlockState(new BlockPos(vec).up()).isTopSolid(); vec = vec.addVector(0d, 1d, 0d)) {
			}

			entity1.setLocationAndAngles(
				vec.x,
				vec.y + 0.5d,
				vec.z,
				entity.getRNG().nextFloat() * 360f,
				(entity.getRNG().nextFloat() - 0.5f) * 60f
			);

			world.spawnEntity(entity1);
		}
	}

	public static class EC extends EntitySpike.Base implements ItemJutsu.IJutsu {

		private final int initialDelay = 5;
		private static final int riseTime = 4;
		private static final int peakTime = 4;
		private static final int fallTime = 6;
		private final double riseHeight = 3.0d;
		private final float maxScale = 5.0f;

		public float damage = 5f;

		private double targetY;
		private boolean initialized = false;
		private int damageCount = 0;
		private Set<Entity> batchHitEntities = new HashSet<Entity>();

		public EC(World worldIn) {
			super(worldIn);
			this.setColor(0xFFFFFFFF);
		}

		public EC(EntityLivingBase userIn, float damageIn, Set<Entity> batchHitEntitiesIn) {
			this(userIn.world);
			this.shootingEntity = userIn;
			this.damage = damageIn;
			this.batchHitEntities = batchHitEntitiesIn;
		}

		public EC(EntityLivingBase userIn, float damageIn) {
			this(userIn.world);
			this.shootingEntity = userIn;
			this.damage = damageIn;
		}

		@Override
		public ItemJutsu.JutsuEnum.Type getJutsuType() {
			return ItemJutsu.JutsuEnum.Type.DOTON;
		}

		@Override
		public void onUpdate() {
			super.onUpdate();

			if (!this.initialized) {
				this.initialized = true;
				this.targetY = this.posY;
				this.setPosition(this.posX, this.targetY - this.riseHeight, this.posZ);
			}

			int animationTick = this.ticksAlive - this.initialDelay;

			if (animationTick < 0) {
				this.setEntityScale(0.0f);
			} else if (animationTick <= this.riseTime) {
				float progress = MathHelper.clamp((float)animationTick / this.riseTime, 0.0f, 1.0f);

				this.setPosition(
					this.posX,
					this.targetY - this.riseHeight + this.riseHeight * progress,
					this.posZ
				);

				this.setEntityScale(this.maxScale * progress);
			} else if (animationTick <= this.riseTime + this.peakTime) {
				this.setPosition(this.posX, this.targetY, this.posZ);
				this.setEntityScale(this.maxScale);
			} else if (animationTick <= this.riseTime + this.peakTime + this.fallTime) {
				int fallTick = animationTick - this.riseTime - this.peakTime;
				float progress = MathHelper.clamp((float)fallTick / this.fallTime, 0.0f, 1.0f);

				this.setPosition(
					this.posX,
					this.targetY - this.riseHeight * progress,
					this.posZ
				);

				this.setEntityScale(this.maxScale * (1.0f - progress));
			} else {
				this.setDead();
				return;
			}

			int damageTick1 = this.initialDelay + 3;
			int damageTick3 = this.initialDelay + this.riseTime + this.peakTime + 1;

			if ((this.ticksAlive == damageTick1 || this.ticksAlive == damageTick3) && this.damageCount < 2) {
				this.damageCount++;

				for (EntityLivingBase entity : this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().grow(1d, 0.5d, 1d))) {
					if (!entity.equals(this.shootingEntity) && !this.batchHitEntities.contains(entity)) {
						this.batchHitEntities.add(entity);

						entity.attackEntityFrom(
							ItemJutsu.causeJutsuDamage(this, this.shootingEntity),
							this.damage
						);

						entity.hurtResistantTime = 14;
					}
				}
			}

			if (this.world instanceof WorldServer) {
				((WorldServer)this.world).spawnParticle(
					EnumParticleTypes.BLOCK_DUST,
					this.posX,
					this.posY,
					this.posZ,
					6,
					0D,
					0D,
					0D,
					0.15D,
					Block.getIdFromBlock(Blocks.STONE)
				);
			}
		}

		public static class Jutsu implements ItemJutsu.IJutsuCallback {

			@Override
			public boolean createJutsu(ItemStack stack, EntityLivingBase entity, float power) {
				World world = entity.world;
				Vec3d vec3d = entity.getPositionEyes(1f);
				Vec3d vec3d2 = vec3d.add(entity.getLookVec().scale(200d));
				RayTraceResult res = world.rayTraceBlocks(vec3d, vec3d2, false, true, true);

				if (res != null && res.typeOfHit == RayTraceResult.Type.BLOCK) {
					world.playSound(
						null,
						res.getBlockPos(),
						net.minecraft.util.SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:hand_press")),
						net.minecraft.util.SoundCategory.BLOCKS,
						5f,
						entity.getRNG().nextFloat() * 0.4f + 0.8f
					);

					spawnWave(world, entity, stack, power, res.hitVec, 0);

					pendingWaves.add(new WaveSpawn(entity, stack, power, res.hitVec, 1, 15));
					pendingWaves.add(new WaveSpawn(entity, stack, power, res.hitVec, 2, 30));

					ItemJutsu.setCurrentJutsuCooldown(stack, 20 * 4);
					return true;
				}

				return false;
			}

			@Override
			public float getBasePower() {
				return 0.5f;
			}

			@Override
			public float getPowerupDelay() {
				return 10.0f;
			}

			@Override
			public float getMaxPower() {
				return maxpower;
			}
		}
	}
}