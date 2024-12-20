
package net.narutomod.entity;

import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

import net.minecraft.world.World;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.Entity;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.item.ItemStack;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.datasync.DataSerializers;

import net.narutomod.procedure.ProcedureAoeCommand;
import net.narutomod.procedure.ProcedureUtils;
import net.narutomod.item.ItemGourd;
import net.narutomod.item.ItemJiton;
import net.narutomod.item.ItemJutsu;
import net.narutomod.Particles;
import net.narutomod.ElementsNarutomodMod;

import java.util.*;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Maps;
import com.google.common.collect.Lists;
import javax.annotation.Nullable;

import static net.narutomod.item.ItemJiton.getSandType;

@ElementsNarutomodMod.ModElement.Tag
public class EntitySandBullet extends ElementsNarutomodMod.ModElement {
	public static final int ENTITYID = 202;
	public static final int ENTITYID_RANGED = 203;
	private static final Random rand = new Random();

	public EntitySandBullet(ElementsNarutomodMod instance) {
		super(instance, 519);
	}

	@Override
	public void initElements() {
		elements.entities.add(() -> EntityEntryBuilder.create().entity(EC.class)
			.id(new ResourceLocation("narutomod", "sand_bullet"), ENTITYID).name("sand_bullet").tracker(64, 3, true).build());
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
		public class CustomRender extends Render<EC> {
			public CustomRender(RenderManager renderManagerIn) {
				super(renderManagerIn);
			}
			@Override
			public void doRender(EC entity, double x, double y, double z, float entityYaw, float partialTicks) {
				Particles.spawnParticle(entity.world, Particles.Types.SUSPENDED,
				 x + this.renderManager.viewerPosX, y + this.renderManager.viewerPosY+0.1d, z + this.renderManager.viewerPosZ,
				 10, 0.1d, 0.1d, 0.1d, 0d, 0d, 0d, entity.getColor(), 15, 5);
			}
			@Override
			protected ResourceLocation getEntityTexture(EC entity) {
				return null;
			}
		}
	}

	public static class EC extends EntityScalableProjectile.Base implements ItemJutsu.IJutsu {
		private static final DataParameter<Integer> COLOR = EntityDataManager.<Integer>createKey(EC.class, DataSerializers.VARINT);
		private int delay;
		private final List<Entity> ignoreEntities = Lists.newArrayList();

		public EC(World worldIn) {
			super(worldIn);
			this.setOGSize(0.5f, 0.5f);
		}

		public EC(EntityLivingBase shooter, int sandColor) {
			this(shooter, sandColor, shooter.posX, shooter.posY + shooter.getEyeHeight(), shooter.posZ, 0);
		}

		public EC(EntityLivingBase shooter, int sandColor, double x, double y, double z, int delayTicks) {
			super(shooter);
			this.setOGSize(0.2f, 0.2f);
			this.setPosition(x, y, z);
			this.setColor(sandColor);
			this.delay = delayTicks;
			this.ignoreEntities.add(shooter);
		}

		@Override
		public ItemJutsu.JutsuEnum.Type getJutsuType() {
			return ItemJutsu.JutsuEnum.Type.JITON;
		}

		@Override
		protected void entityInit() {
			super.entityInit();
			this.dataManager.register(COLOR, Integer.valueOf(0));
		}

		public int getColor() {
			return ((Integer)this.dataManager.get(COLOR)).intValue();
		}

		protected void setColor(int color) {
			this.dataManager.set(COLOR, Integer.valueOf(color));
		}

		@Override
		public void onUpdate() {
			if (this.ticksAlive >= this.delay && this.shootingEntity != null) {
				Vec3d vec = this.shootingEntity instanceof EntityLiving && ((EntityLiving)this.shootingEntity).getAttackTarget() != null
				 ? ((EntityLiving)this.shootingEntity).getAttackTarget().getPositionEyes(1f).subtract(this.getPositionVector())
				 : this.shootingEntity.getLookVec();
				this.shoot(vec.x, vec.y, vec.z, 1.2f, 0.05f);
			}
			super.onUpdate();
			if (this.ticksAlive > this.delay + 80) {
				this.setDead();
			}
		}

		@Override
		protected void onImpact(RayTraceResult result) {
			if (!this.world.isRemote && (result.entityHit == null || !this.ignoreEntities.contains(result.entityHit))) {
				this.playSound(net.minecraft.util.SoundEvent.REGISTRY
				 .getObject(new ResourceLocation("narutomod:bullet_impact")), 1f, 0.4f + this.rand.nextFloat() * 0.6f);
				ProcedureAoeCommand bruh = ProcedureAoeCommand.set(this,0,4);
				for (Entity entity : bruh.getList()) {
					entity.hurtResistantTime = 10;
					entity.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, this.shootingEntity), 5+1.5f*ItemJutsu.getDmgMult(this.shootingEntity));
				}
				this.world.createExplosion(this.shootingEntity, result.hitVec.x, result.hitVec.y, result.hitVec.z, 2f,
						net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.world, this.shootingEntity));
				/*if (result.entityHit instanceof EntityLivingBase) {
					result.entityHit.hurtResistantTime = 10;
					result.entityHit.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, this.shootingEntity), 5+2*ItemJutsu.getDmgMult(this.shootingEntity));
					//ProcedureUtils.pushEntity(this, result.entityHit, 10d, 3.0f);
				}*/
				this.setDead();
			}
		}

		@Override
		protected void checkOnGround() {
		}

		@Override
		public void renderParticles() {
			//if (this.world.isRemote) {
			//	Particles.spawnParticle(this.world, Particles.Types.SUSPENDED, this.posX, this.posY+0.1d, this.posZ,
 			//	 10, 0.03d, 0.03d, 0.03d, 0d, 0d, 0d, this.getColor(), 10, 5);
			//}
		}

		protected void setIgnoreEntities(Entity... ignoreEntity) {
			for (int i = 0; i < ignoreEntity.length; i++) {
				this.ignoreEntities.add(ignoreEntity[i]);
			}
		}

		public static void delay(int ms)
		{
			try
			{
				Thread.sleep(ms);
			}
			catch(InterruptedException ex)
			{
				Thread.currentThread().interrupt();
			}
		}

		public static class Jutsu implements ItemJutsu.IJutsuCallback {
			@Override
			public boolean createJutsu(ItemStack stack, EntityLivingBase entity, float power) {
				for (int i = 0; i < 1+power*4; i++) {
					addPos(getSandType(stack), entity, power, ItemGourd.getMouthPos(entity));
                }
				List<ItemJiton.SwarmTarget> list = getStartPosList(entity);
				if (list != null) {
					Iterator<ItemJiton.SwarmTarget> iter = list.iterator();
					for (int i = 0; iter.hasNext(); i++) {
						ItemJiton.SwarmTarget st = iter.next();
						Vec3d vec = st.getTargetPos();
						this.createJutsu(st.getColor(), entity, vec.x, vec.y, vec.z, i);
						st.forceRemove();
						iter.remove();
					}
					if (entity instanceof EntityPuppet3rdKazekage.EntityCustom) {
						((EntityPuppet3rdKazekage.EntityCustom)entity).setMouthOpen(false);
					}
					ItemJutsu.setCurrentJutsuCooldown(stack,20);
					return true;
				}
				return false;
			}

			public void createJutsu(int color, EntityLivingBase entity, double x, double y, double z, int delay) {
				entity.world.playSound(null, x, y, z, SoundEvents.BLOCK_SAND_PLACE,
				 net.minecraft.util.SoundCategory.BLOCKS, 0.5f, entity.getRNG().nextFloat() * 0.4f + 0.6f);
				EC ecEntity = new EC(entity, color, x, y, z, delay);
				if (entity instanceof EntityPuppet3rdKazekage.EntityCustom) {
					ecEntity.setIgnoreEntities(((EntityPuppet3rdKazekage.EntityCustom)entity).getOwner());
				}
				entity.world.spawnEntity(ecEntity);
			}

			@Override
			public float getBasePower() {
				return 0.0f;
			}
	
			@Override
			public float getPowerupDelay() {
				return 40.0f;
			}
	
			@Override
			public float getMaxPower() {
				return 5.0f;
			}
		}
	}

	private static final Map<Integer, List<ItemJiton.SwarmTarget>> posMap = Maps.newHashMap();

	@Nullable
	private static List<ItemJiton.SwarmTarget> getStartPosList(EntityLivingBase entity) {
		List<ItemJiton.SwarmTarget> list = posMap.get(entity.getEntityId());
		if (list != null) {
			return list;
		} else {
			for (Map.Entry<Integer, List<ItemJiton.SwarmTarget>> entry : posMap.entrySet()) {
				if (entry.getKey().intValue() == entity.getEntityId()) {
				//if (ItemStack.areItemStacksEqual(entry.getKey(), stack)) {
					return entry.getValue();
				}
			}
		}
		return null;
	}

	public static void addPos(ItemJiton.Type sandType, EntityLivingBase entity, float power, Vec3d sandOrigin) {
		List<ItemJiton.SwarmTarget> list = getStartPosList(entity);
		if (list == null) {
			list = Lists.newArrayList();
			posMap.put(entity.getEntityId(), list);
		}
		list.add(new ItemJiton.SwarmTarget(entity.world, 1, sandOrigin, 
		 new Vec3d(entity.posX + (entity.getRNG().nextDouble()-0.5d) * power * 2, entity.posY + entity.getEyeHeight() + (entity.getRNG().nextDouble()-0.5d) * 2d, entity.posZ + (entity.getRNG().nextDouble()-0.5d) * power * 2),
		 new Vec3d(0.1d, 0.2d, 0.1d), 0.5f, 0.01f, false, 0.5f, sandType.getColor()));
	}

	public static void updateSwarms(EntityLivingBase entity) {
		List<ItemJiton.SwarmTarget> list = getStartPosList(entity);
		if (list != null && !list.isEmpty()) {
			for (ItemJiton.SwarmTarget st : list) {
				st.onUpdate();
			}
		}
	}
}
