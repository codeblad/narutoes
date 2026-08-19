
package net.narutomod.entity;

import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.common.MinecraftForge;

import net.minecraft.world.World;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.SoundEvent;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.Vec3d;

import net.narutomod.procedure.ProcedureUtils;
import net.narutomod.item.ItemJutsu;
import net.narutomod.potion.PotionHeaviness;
import net.narutomod.item.ItemInton;
import net.narutomod.PlayerInput;
import net.narutomod.Chakra;
import net.narutomod.NarutomodModVariables;
import net.narutomod.ElementsNarutomodMod;

import javax.annotation.Nullable;

@ElementsNarutomodMod.ModElement.Tag
public class EntityShadowImitation extends ElementsNarutomodMod.ModElement {
	public static final int ENTITYID = 293;
	public static final int ENTITYID_RANGED = 294;
	public static final String ENTITY_TAG = "shadow_imitation_entities";

	public EntityShadowImitation(ElementsNarutomodMod instance) {
		super(instance, 618);
	}

	@Override
	public void initElements() {
		elements.entities.add(() -> EntityEntryBuilder.create().entity(EC.class)
				.id(new ResourceLocation("narutomod", "shadow_imitation"), ENTITYID).name("shadow_imitation").tracker(64, 3, true).build());
	}
	

	public static class EC extends Entity implements PlayerInput.Hook.IHandler, ItemJutsu.IJutsu {
		private static final DataParameter<Integer> USER_ID = EntityDataManager.<Integer>createKey(EC.class, DataSerializers.VARINT);
		private static final DataParameter<Integer> TARGET_ID = EntityDataManager.<Integer>createKey(EC.class, DataSerializers.VARINT);
		private static final DataParameter<Boolean> AOE = EntityDataManager.<Boolean>createKey(EC.class, DataSerializers.BOOLEAN);
		private double chakraBurn;
		private PlayerInput.Hook userInput = new PlayerInput.Hook();
		private int strangleCooldown = 0;
		private int lifetimeReduction = 0;
		private boolean stitched = false;

		public EC(World world) {
			super(world);
			this.setSize(0.01f, 0.01f);
			this.isImmuneToFire = true;
		}

		public EC(EntityLivingBase userIn, double chakraUsagePerSec) {
			this(userIn.world);
			this.setUser(userIn);
			this.setTarget(null);
			this.setAOE(true);

			this.setPosition(userIn.posX, userIn.posY, userIn.posZ);

			this.chakraBurn = chakraUsagePerSec;
		}

		public EC(EntityLivingBase userIn, EntityLivingBase targetIn, double chakraUsagePerSec) {
			this(userIn.world);
			this.setUser(userIn);
			this.setTarget(targetIn);
			this.setAOE(false);

			this.setPosition(userIn.posX, userIn.posY, userIn.posZ);

			this.chakraBurn = chakraUsagePerSec
				+ Math.max(ProcedureUtils.getPunchDamage(targetIn) * 10d, 90d);
		}
		

		@Override
		public ItemJutsu.JutsuEnum.Type getJutsuType() {
			return ItemJutsu.JutsuEnum.Type.INTON;
		}

		@Override
		protected void entityInit() {
			this.getDataManager().register(USER_ID, Integer.valueOf(-1));
			this.getDataManager().register(TARGET_ID, Integer.valueOf(-1));
			this.getDataManager().register(AOE, Boolean.valueOf(false));
		}

		private void setUser(@Nullable EntityLivingBase entity) {
			this.getDataManager().set(USER_ID, Integer.valueOf(entity != null ? entity.getEntityId() : -1));
		}

		private void setAOE(boolean value) {
			this.getDataManager().set(AOE, Boolean.valueOf(value));
		}

		private boolean isAOE() {
			return this.getDataManager().get(AOE).booleanValue();
		}

		@Nullable
		private EntityLivingBase getUser() {
			Entity entity = this.world.getEntityByID(((Integer)this.getDataManager().get(USER_ID)).intValue());
			return entity instanceof EntityLivingBase ? (EntityLivingBase)entity : null;
		}

		private void setTarget(@Nullable EntityLivingBase entity) {
			this.getDataManager().set(TARGET_ID, Integer.valueOf(entity != null ? entity.getEntityId() : -1));
		}

		@Nullable
		private EntityLivingBase getTarget() {
			Entity entity = this.world.getEntityByID(((Integer)this.getDataManager().get(TARGET_ID)).intValue());
			return entity instanceof EntityLivingBase ? (EntityLivingBase)entity : null;
		}
		


		@Override
		public void handlePacket(@Nullable PlayerInput.Hook.MovementPacket movementPacket, @Nullable PlayerInput.Hook.MousePacket mousePacket) {
			if (movementPacket != null) {
				this.userInput.copyMovementInput(movementPacket);
			}
			if (mousePacket != null) {
				this.userInput.copyMouseInput(mousePacket);
			}
		}

	private void applyShadowToEntitiesOnBlock(BlockPos blockPos, EntityLivingBase user) {
	AxisAlignedBB blockBox = new AxisAlignedBB(
		blockPos.getX(),
		blockPos.getY() + 0.001D,
		blockPos.getZ(),
		blockPos.getX() + 1.0D,
		blockPos.getY() + 1.01D,
		blockPos.getZ() + 1.0D
	);

	java.util.List<EntityLivingBase> entities =
		this.world.getEntitiesWithinAABB(
			EntityLivingBase.class,
			blockBox
		);

	for (EntityLivingBase target : entities) {

		if (target == user) {
			continue;
		}

		if (!target.isEntityAlive()) {
			continue;
		}

		if (!target.onGround) {
			continue;
		}

		if (!ItemJutsu.canTarget(target)) {
			continue;
		}

		double feetY = target.getEntityBoundingBox().minY;

		if (Math.abs(feetY - (blockPos.getY() + 1.0D)) > 0.15D) {
			continue;
		}

		if (target.getEntityBoundingBox().maxX <= blockPos.getX()
				|| target.getEntityBoundingBox().minX >= blockPos.getX() + 1.0D
				|| target.getEntityBoundingBox().maxZ <= blockPos.getZ()
				|| target.getEntityBoundingBox().minZ >= blockPos.getZ() + 1.0D) {
			continue;
		}

		if (Jutsu.intarrayContains(
				this.world,
				user.getEntityData().getIntArray(Jutsu.ECENTITYID),
				target.getEntityId())) {
			continue;
		}

		EC possession = new EC(
			user,
			target,
			ItemInton.SHADOW_IMITATION.chakraUsage
		);

		this.world.spawnEntity(possession);

		Jutsu.addEntity(user, possession);
	}
}
		private void updateAOE(EntityLivingBase user) {
	if (user == null || !user.isEntityAlive()) {
		this.setDead();
		return;
	}

	this.setPosition(user.posX, user.posY, user.posZ);

	double radius = Math.min(this.ticksExisted * 0.35D, 6.0D);

	if (this.ticksExisted > 25) {
		this.setDead();
		return;
	}

	if (this.ticksExisted % 20 == 1) {
		if (!Chakra.pathway(user).consume(this.chakraBurn)) {
			this.setDead();
			return;
		}
	}

	if (this.ticksExisted == 1) {
		this.playSound(
			SoundEvent.REGISTRY.getObject(
				new ResourceLocation("narutomod:shadow_sfx")
			),
			1f,
			1f
		);
	}

	double previousRadius = Math.max(0.0D, radius - 0.35D);

	int minX = MathHelper.floor(user.posX - radius);
	int maxX = MathHelper.floor(user.posX + radius);
	int minZ = MathHelper.floor(user.posZ - radius);
	int maxZ = MathHelper.floor(user.posZ + radius);

	int centerY = MathHelper.floor(user.posY);

	for (int x = minX; x <= maxX; x++) {
		for (int z = minZ; z <= maxZ; z++) {

			double dx = (x + 0.5D) - user.posX;
			double dz = (z + 0.5D) - user.posZ;

			double distanceSq = dx * dx + dz * dz;

			if (distanceSq > radius * radius) {
				continue;
			}

			if (distanceSq <= previousRadius * previousRadius) {
				continue;
			}

			for (int y = centerY + 3; y >= centerY - 4; y--) {

				BlockPos blockPos = new BlockPos(x, y, z);
				IBlockState state = this.world.getBlockState(blockPos);

				if (state.getRenderType() == EnumBlockRenderType.INVISIBLE) {
					continue;
				}

				if (!state.isFullCube()) {
					continue;
				}

				if (!this.world.isAirBlock(blockPos.up())) {
					continue;
				}

				this.applyShadowToEntitiesOnBlock(blockPos, user);

				break;
			}
		}
	}
}
	@Override
	public void setDead() {
		super.setDead();

		if (!this.world.isRemote) {
			EntityLivingBase user = this.getUser();
			EntityLivingBase target = this.getTarget();

			if (!this.isAOE()
					&& user instanceof EntityPlayerMP
					&& user.isEntityAlive()) {
				PlayerInput.Hook.copyInputFrom(
					(EntityPlayerMP) user,
					this,
					false
				);
			}

			if (target != null) {
				PlayerInput.Hook.haltTargetInput(target, false);
			}

			if (user != null) {
				Jutsu.removeEntity(user, this.getEntityId());
			}
		}
	}

	@SubscribeEvent
public void onLivingDeath(LivingDeathEvent event) {
    EntityLivingBase entity = event.getEntityLiving();

    if (entity == null || entity.world.isRemote) {
        return;
    }

    int[] intarray = entity.getEntityData().getIntArray(Jutsu.ECENTITYID);

    if (intarray.length > 0) {
        int[] entities = intarray.clone();

        for (int i = 0; i < entities.length; i++) {
            Entity entity1 = entity.world.getEntityByID(entities[i]);

            if (entity1 instanceof EC && !entity1.isDead) {
                entity1.setDead();
            }
        }

        entity.getEntityData().removeTag(Jutsu.ECENTITYID);
    }
}

		@Override
		public void onUpdate() {
			EntityLivingBase user = this.getUser();

			if (!this.world.isRemote) {
				if (this.isAOE()) {
					this.updateAOE(user);
					return;
				}

				EntityLivingBase target = this.getTarget();

				if (user != null && user.isEntityAlive()
						&& ItemJutsu.canTarget(target)
						&& this.canTargetBeSeen()) {

					this.setPosition(user.posX, user.posY, user.posZ);

					if (user.getEntityData().getBoolean(NarutomodModVariables.JutsuKey1Pressed)
						|| (this.ticksExisted >= 20 * 7 - this.lifetimeReduction
						|| this.ticksExisted % 20 == 1 && !Chakra.pathway(user).consume(this.chakraBurn))) {

						this.setDead();

					} else {

						if (this.ticksExisted == 1) {
							this.playSound(
								SoundEvent.REGISTRY.getObject(
									new ResourceLocation("narutomod:shadow_sfx")
								),
								1f,
								1f
							);

							PlayerInput.Hook.haltTargetInput(target, true);

							if (user instanceof EntityPlayer) {
								PlayerInput.Hook.copyInputFrom(
									(EntityPlayerMP) user,
									this,
									true
								);
							}
						}

						if (this.strangleCooldown > 0) {
							this.strangleCooldown--;
						}

						if (user.getEntityData().getBoolean(NarutomodModVariables.EYETOGGLE)
								&& this.strangleCooldown <= 0) {

							if (this.shadowStrangle()) {
								this.strangleCooldown = 20;
							}
						}

						if (this.userInput.hasNewMovementInput()) {
							this.userInput.handleMovement(target);
						}

						if (this.userInput.hasNewMouseEvent()) {
							this.userInput.handleMouseEvent(target);
						}
					}

				} else {
					this.setDead();
				}
			}
		}

		public boolean shadowStrangle() {
			EntityLivingBase user = this.getUser();
			EntityLivingBase target = this.getTarget();

			if (user == null || target == null || !target.isEntityAlive()) {
				return false;
			}
			this.world.playSound(null, this.posX, this.posY, this.posZ,
				 SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:bonecrack")), SoundCategory.PLAYERS, 1f, 1f);

			float damage = 12f+2.9f*ItemJutsu.getDmgMult(user);

			target.attackEntityFrom(
				ItemJutsu.causeJutsuDamage(user, user),
				damage
			);

			this.lifetimeReduction += 20;

			return true;
		}
	

	    public boolean canTargetBeSeen() {
			EntityLivingBase user = this.getUser();
			EntityLivingBase target = this.getTarget();
	        return this.world.rayTraceBlocks(user.getPositionEyes(1f), target.getPositionEyes(1f), false, true, false) == null
	         || this.world.rayTraceBlocks(user.getPositionEyes(1f), target.getPositionVector().addVector(0d, 0.2d, 0d), false, true, false) == null;
	    }

		@Override
		protected void readEntityFromNBT(NBTTagCompound compound) {
		}

		@Override
		protected void writeEntityToNBT(NBTTagCompound compound) {
		}

		public static class Jutsu implements ItemJutsu.IJutsuCallback {
			private static final String ECENTITYID = "ShadowImitationEntityIdKey";

			
			
			private static void addEntity(EntityLivingBase user, EC entity) {
				int[] oldintarray = user.getEntityData().getIntArray(ECENTITYID);

				int[] newintarray = new int[oldintarray.length + 1];

				for (int i = 0; i < oldintarray.length; i++) {
					newintarray[i] = oldintarray[i];
				}

				newintarray[oldintarray.length] = entity.getEntityId();

				user.getEntityData().setIntArray(ECENTITYID, newintarray);
			}

			@Override
			public boolean createJutsu(ItemStack stack, EntityLivingBase entity, float power) {

			if (entity.isSneaking()) {

			int[] oldintarray =
				entity.getEntityData().getIntArray(ECENTITYID);

			for (int i = 0; i < oldintarray.length; i++) {
				Entity existing = entity.world.getEntityByID(oldintarray[i]);

				if (existing instanceof EC && ((EC) existing).isAOE()) {
					return false;
				}
			}

			EC shadowAOE = new EC(
				entity,
				ItemInton.SHADOW_IMITATION.chakraUsage
			);
			//apply heaviness
			entity.addPotionEffect(new PotionEffect(PotionHeaviness.potion, 24, 4, false, false));

			entity.world.spawnEntity(shadowAOE);

			addEntity(entity, shadowAOE);

			return true;
		}
			RayTraceResult res =
				ProcedureUtils.objectEntityLookingAt(entity, 30d);

			if (res != null && res.entityHit instanceof EntityLivingBase) {

				if (res.entityHit.onGround && entity.onGround) {

					int[] oldintarray =
						entity.getEntityData().getIntArray(ECENTITYID);

					if (!intarrayContains(
							entity.world,
							oldintarray,
							res.entityHit.getEntityId())) {

						EC entity1 = new EC(
							entity,
							(EntityLivingBase) res.entityHit,
							ItemInton.SHADOW_IMITATION.chakraUsage
						);

						entity.world.spawnEntity(entity1);

						addEntity(entity, entity1);

						return true;
					}
				}
			}

			return false;
		}

			private static boolean intarrayContains(World world, int[] intarray, int i) {
				for (int j = 0; j < intarray.length; j++) {
					Entity entity = world.getEntityByID(intarray[j]);
					if (entity instanceof EC) {
						EntityLivingBase target = ((EC)entity).getTarget();
						if (target != null && target.getEntityId() == i) {
							return true;
						}
					}
				}
				return false;
			}

			public static void removeEntity(EntityLivingBase user, int entityId) {
				int[] oldintarray = user.getEntityData().getIntArray(ECENTITYID);
				if (oldintarray.length > 1) {
					int[] newintarray = new int[oldintarray.length - 1];
					for (int i = 0, j = 0; j < oldintarray.length; j++) {
						if (oldintarray[j] != entityId) {
							newintarray[i++] = oldintarray[j];
						}
					}
					user.getEntityData().setIntArray(ECENTITYID, newintarray);
				} else {
					user.getEntityData().removeTag(ECENTITYID);
				}
			}

			private String intarrayTargets2String(World world, int[] intarray) {
				String s = "[";
				for (int i = 0; i < intarray.length; i++) {
					Entity entity = world.getEntityByID(intarray[i]);
					if (entity instanceof EC) {
						if (i > 0) {
							s += ", ";
						}
						s += ((EC)entity).getTarget().getEntityId();
					}
				}
				return s + "]";
			}

			@Override
			public boolean isActivated(EntityLivingBase entity) {
				int[] intarray = entity.getEntityData().getIntArray(ECENTITYID);

				if (intarray.length == 0) {
					return false;
				}

				boolean active = false;

				for (int i = 0; i < intarray.length; i++) {
					Entity entity1 = entity.world.getEntityByID(intarray[i]);

					if (entity1 instanceof EC && !entity1.isDead) {
						active = true;
						break;
					}
				}

				if (!active) {
					entity.getEntityData().removeTag(ECENTITYID);
				}

				return active;
			}
		}

		public static class PlayerHook {
			@SubscribeEvent
			public void onChangeDimension(EntityTravelToDimensionEvent event) {
				Entity entity = event.getEntity();
				if (entity instanceof EntityLivingBase) {
					int[] intarray = entity.getEntityData().getIntArray(Jutsu.ECENTITYID);
					if (intarray.length > 0) {
						for (int i = 0; i < intarray.length; i++) {
							Entity entity1 = entity.world.getEntityByID(intarray[i]);
							if (entity1 instanceof EC) {
								entity1.setDead();
							}
						}
						entity.getEntityData().removeTag(Jutsu.ECENTITYID);
						//event.setCanceled(true);
					}
				}
			}
		}
	}

	@Override
	public void init(FMLInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(new EC.PlayerHook());
	}

	@Override
	public void preInit(FMLPreInitializationEvent event) {
		new Renderer().register();
	}

	public static class Renderer extends EntityRendererRegister {
		@SideOnly(Side.CLIENT)
		@Override
		public void register() {
			RenderingRegistry.registerEntityRenderingHandler(EC.class, renderManager -> new RenderCustom(renderManager));
		}

		@SideOnly(Side.CLIENT)
public class RenderCustom extends Render<EC> {
	private final ResourceLocation texture =
		new ResourceLocation("narutomod:textures/black.png");

	public RenderCustom(RenderManager renderManagerIn) {
		super(renderManagerIn);
	}

	@Override
	public boolean shouldRender(EC entity, ICamera camera,
			double camX, double camY, double camZ) {
		return true;
	}

	@Override
	public void doRender(EC entity, double x, double y, double z,
			float entityYaw, float partialTicks) {

		if (entity.isAOE()) {
			renderAOEShadow(entity, partialTicks);
		} else {
			renderNormalShadow(entity, partialTicks);
		}
	}

	private void renderNormalShadow(EC entity, float partialTicks) {
		EntityLivingBase user = entity.getUser();
		EntityLivingBase target = entity.getTarget();

		if (user == null || target == null) {
			return;
		}

		double d0 = user.lastTickPosX
			+ (user.posX - user.lastTickPosX) * partialTicks;

		double d1 = user.lastTickPosY
			+ (user.posY - user.lastTickPosY) * partialTicks;

		double d2 = user.lastTickPosZ
			+ (user.posZ - user.lastTickPosZ) * partialTicks;

		double d3 = target.lastTickPosX
			+ (target.posX - target.lastTickPosX) * partialTicks;

		double d4 = target.lastTickPosY
			+ (target.posY - target.lastTickPosY) * partialTicks;

		double d5 = target.lastTickPosZ
			+ (target.posZ - target.lastTickPosZ) * partialTicks;

		int i0 = MathHelper.floor(d0);
		int i1 = MathHelper.floor(Math.min(d1, d4)) - 10;
		int i2 = MathHelper.floor(d2);

		int i3 = MathHelper.floor(d3);
		int i4 = MathHelper.floor(Math.max(d1, d4)) + 1;
		int i5 = MathHelper.floor(d5);

		World world = this.renderManager.world;

		this.renderManager.renderEngine.bindTexture(this.texture);

		GlStateManager.enableBlend();
		GlStateManager.blendFunc(
			GlStateManager.SourceFactor.SRC_ALPHA,
			GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
		);

		GlStateManager.depthMask(false);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();

		bufferbuilder.begin(
			7,
			DefaultVertexFormats.POSITION_TEX_COLOR
		);

		for (BlockPos blockpos :
			BlockPos.getAllInBoxMutable(
				new BlockPos(i0, i1, i2),
				new BlockPos(i3, i4, i5))) {

			IBlockState blockstate =
				world.getBlockState(blockpos);

			if (blockstate.getRenderType()
					!= EnumBlockRenderType.INVISIBLE
					&& blockstate.isFullCube()) {

				AxisAlignedBB axisalignedbb =
					blockstate.getBoundingBox(world, blockpos)
						.offset(blockpos);

				if (blockpos.distanceSqToCenter(
						d0, d1, d2)
						< 0.25d
							* entity.ticksExisted
							* entity.ticksExisted

					&& axisalignedbb
						.expand(
							0.0d,
							(double)i4 - axisalignedbb.maxY,
							0.0d
						)
						.calculateIntercept(
							new Vec3d(d0, d1, d2),
							new Vec3d(d3, d4, d5)
						) != null) {

					renderBlockShadow(
						bufferbuilder,
						axisalignedbb
					);
				}
			}
		}

		tessellator.draw();

		GlStateManager.color(
			1.0F, 1.0F, 1.0F, 1.0F
		);

		GlStateManager.disableBlend();
		GlStateManager.depthMask(true);
	}

	private void renderAOEShadow(EC entity, float partialTicks) {
		EntityLivingBase user = entity.getUser();

		if (user == null) {
			return;
		}

		double userX =
			user.lastTickPosX
			+ (user.posX - user.lastTickPosX)
			* partialTicks;

		double userY =
			user.lastTickPosY
			+ (user.posY - user.lastTickPosY)
			* partialTicks;

		double userZ =
			user.lastTickPosZ
			+ (user.posZ - user.lastTickPosZ)
			* partialTicks;

		double radius =
			Math.min(
				(entity.ticksExisted + partialTicks) * 0.35D, 6.0D
			);

		if (radius <= 0.0D) {
			return;
		}

		World world = this.renderManager.world;

		int minX = MathHelper.floor(userX - radius);
		int maxX = MathHelper.floor(userX + radius);

		int minZ = MathHelper.floor(userZ - radius);
		int maxZ = MathHelper.floor(userZ + radius);

		int centerY = MathHelper.floor(userY);

		this.renderManager.renderEngine.bindTexture(this.texture);

		GlStateManager.enableBlend();

		GlStateManager.blendFunc(
			GlStateManager.SourceFactor.SRC_ALPHA,
			GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
		);

		GlStateManager.depthMask(false);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();

		bufferbuilder.begin(
			7,
			DefaultVertexFormats.POSITION_TEX_COLOR
		);

		for (int blockX = minX;
				blockX <= maxX;
				blockX++) {

			for (int blockZ = minZ;
					blockZ <= maxZ;
					blockZ++) {

				double dx =
					(blockX + 0.5D) - userX;

				double dz =
					(blockZ + 0.5D) - userZ;

				if (dx * dx + dz * dz
						> radius * radius) {
					continue;
				}

				for (int y = centerY + 3;
						y >= centerY - 4;
						y--) {

					BlockPos blockPos =
						new BlockPos(
							blockX,
							y,
							blockZ
						);

					IBlockState blockstate =
						world.getBlockState(blockPos);

					if (blockstate.getRenderType()
							== EnumBlockRenderType.INVISIBLE) {
						continue;
					}

					if (!blockstate.isFullCube()) {
						continue;
					}

					if (!world.isAirBlock(
							blockPos.up())) {
						continue;
					}

					AxisAlignedBB box =
						blockstate
							.getBoundingBox(
								world,
								blockPos
							)
							.offset(blockPos);

					renderShadowTop(
						bufferbuilder,
						box
					);

					break;
				}
			}
		}

		tessellator.draw();

		GlStateManager.color(
			1.0F, 1.0F, 1.0F, 1.0F
		);

		GlStateManager.disableBlend();
		GlStateManager.depthMask(true);
	}

	private void renderShadowTop(
			BufferBuilder bufferbuilder,
			AxisAlignedBB box) {

		double minX =
			box.minX - this.renderManager.viewerPosX;

		double maxX =
			box.maxX - this.renderManager.viewerPosX;

		double minY =
			box.maxY - this.renderManager.viewerPosY
			+ 0.01D;

		double minZ =
			box.minZ - this.renderManager.viewerPosZ;

		double maxZ =
			box.maxZ - this.renderManager.viewerPosZ;

		bufferbuilder.pos(
				minX, minY, minZ)
			.tex(0.0D, 1.0D)
			.color(
				1.0F,
				1.0F,
				1.0F,
				0.5F
			)
			.endVertex();

		bufferbuilder.pos(
				minX, minY, maxZ)
			.tex(0.0D, 0.0D)
			.color(
				1.0F,
				1.0F,
				1.0F,
				0.5F
			)
			.endVertex();

		bufferbuilder.pos(
				maxX, minY, maxZ)
			.tex(1.0D, 0.0D)
			.color(
				1.0F,
				1.0F,
				1.0F,
				0.5F
			)
			.endVertex();

		bufferbuilder.pos(
				maxX, minY, minZ)
			.tex(1.0D, 1.0D)
			.color(
				1.0F,
				1.0F,
				1.0F,
				0.5F
			)
			.endVertex();
	}

	private void renderBlockShadow(
			BufferBuilder bufferbuilder,
			AxisAlignedBB box) {

		double d6 =
			box.minX - this.renderManager.viewerPosX;

		double d7 =
			box.maxX - this.renderManager.viewerPosX;

		double d8 =
			box.minY - this.renderManager.viewerPosY;

		double d9 =
			box.maxY - this.renderManager.viewerPosY;

		double d10 =
			box.minZ - this.renderManager.viewerPosZ;

		double d11 =
			box.maxZ - this.renderManager.viewerPosZ;

		float alpha = 0.5F;

		bufferbuilder.pos(
				d6, d9 + 0.01D, d10)
			.tex(0.0D, 1.0D)
			.color(1F, 1F, 1F, alpha)
			.endVertex();

		bufferbuilder.pos(
				d6, d9 + 0.01D, d11)
			.tex(0.0D, 0.0D)
			.color(1F, 1F, 1F, alpha)
			.endVertex();

		bufferbuilder.pos(
				d7, d9 + 0.01D, d11)
			.tex(1.0D, 0.0D)
			.color(1F, 1F, 1F, alpha)
			.endVertex();

		bufferbuilder.pos(
				d7, d9 + 0.01D, d10)
			.tex(1.0D, 1.0D)
			.color(1F, 1F, 1F, alpha)
			.endVertex();


		bufferbuilder.pos(
				d7 + 0.01D, d9, d10)
			.tex(0.0D, 1.0D)
			.color(1F, 1F, 1F, alpha)
			.endVertex();

		bufferbuilder.pos(
				d7 + 0.01D, d9, d11)
			.tex(0.0D, 0.0D)
			.color(1F, 1F, 1F, alpha)
			.endVertex();

		bufferbuilder.pos(
				d7 + 0.01D, d8, d11)
			.tex(1.0D, 0.0D)
			.color(1F, 1F, 1F, alpha)
			.endVertex();

		bufferbuilder.pos(
				d7 + 0.01D, d8, d10)
			.tex(1.0D, 1.0D)
			.color(1F, 1F, 1F, alpha)
			.endVertex();

		bufferbuilder.pos(
				d6 - 0.01D, d8, d10)
			.tex(0.0D, 1.0D)
			.color(1F, 1F, 1F, alpha)
			.endVertex();

		bufferbuilder.pos(
				d6 - 0.01D, d8, d11)
			.tex(0.0D, 0.0D)
			.color(1F, 1F, 1F, alpha)
			.endVertex();

		bufferbuilder.pos(
				d6 - 0.01D, d9, d11)
			.tex(1.0D, 0.0D)
			.color(1F, 1F, 1F, alpha)
			.endVertex();

		bufferbuilder.pos(
				d6 - 0.01D, d9, d10)
			.tex(1.0D, 1.0D)
			.color(1F, 1F, 1F, alpha)
			.endVertex();

		bufferbuilder.pos(
				d7, d8, d11 + 0.01D)
			.tex(0.0D, 1.0D)
			.color(1F, 1F, 1F, alpha)
			.endVertex();

		bufferbuilder.pos(
				d7, d9, d11 + 0.01D)
			.tex(0.0D, 0.0D)
			.color(1F, 1F, 1F, alpha)
			.endVertex();

		bufferbuilder.pos(
				d6, d9, d11 + 0.01D)
			.tex(1.0D, 0.0D)
			.color(1F, 1F, 1F, alpha)
			.endVertex();

		bufferbuilder.pos(
				d6, d8, d11 + 0.01D)
			.tex(1.0D, 1.0D)
			.color(1F, 1F, 1F, alpha)
			.endVertex();

		bufferbuilder.pos(
				d6, d8, d10 - 0.01D)
			.tex(0.0D, 1.0D)
			.color(1F, 1F, 1F, alpha)
			.endVertex();

		bufferbuilder.pos(
				d6, d9, d10 - 0.01D)
			.tex(0.0D, 0.0D)
			.color(1F, 1F, 1F, alpha)
			.endVertex();

		bufferbuilder.pos(
				d7, d9, d10 - 0.01D)
			.tex(1.0D, 0.0D)
			.color(1F, 1F, 1F, alpha)
			.endVertex();

		bufferbuilder.pos(
				d7, d8, d10 - 0.01D)
			.tex(1.0D, 1.0D)
			.color(1F, 1F, 1F, alpha)
			.endVertex();
	}

	@Override
	protected ResourceLocation getEntityTexture(EC entity) {
		return this.texture;
	}
}
	
		
		
	}
}
