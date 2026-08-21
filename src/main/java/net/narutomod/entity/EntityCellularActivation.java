package net.narutomod.entity;

import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

import net.minecraft.world.World;
import net.minecraft.util.ResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.item.ItemStack;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.util.text.TextComponentString;

import net.narutomod.item.ItemIryoJutsu;
import net.narutomod.item.ItemJutsu;
import net.narutomod.Particles;
import net.narutomod.Chakra;
import net.narutomod.ElementsNarutomodMod;

import javax.annotation.Nullable;

@ElementsNarutomodMod.ModElement.Tag
public class EntityCellularActivation extends ElementsNarutomodMod.ModElement {
	public static final int ENTITYID = 213;
	public static final int ENTITYID_RANGED = 214;

	public EntityCellularActivation(ElementsNarutomodMod instance) {
		super(instance, 527);
	}

	@Override
	public void initElements() {
		elements.entities.add(() -> EntityEntryBuilder.create().entity(EC.class)
		 .id(new ResourceLocation("narutomod", "cellular_activation"), ENTITYID).name("cellular_activation").tracker(64, 3, true).build());
	}

	public static class EC extends Entity implements ItemJutsu.IJutsu {
		private static final int WAIT_PER_FULL_CHARGE = 100;
		private static final int HEAL_DURATION = 10;
		private final double chakraBurn = ItemIryoJutsu.MEDMODE.chakraUsage;
		private EntityLivingBase user;
		private float markedHealth;
		private float startingHealth;
		private float totalHealing;
		private int wait;
		private float power;
		private boolean activated;
		private boolean healing;
		private boolean chakraConsumed;

		public EC(World worldIn) {
			super(worldIn);
			this.setSize(0.01f, 0.01f);
		}

		public EC(EntityLivingBase userIn, float power) {
			this(userIn.world);
			this.user = userIn;
			this.markedHealth = userIn.getHealth();
			this.startingHealth = userIn.getHealth();
			this.wait = Math.max(1, Math.round(WAIT_PER_FULL_CHARGE * (power / 10.0f)));
			this.setPosition(userIn.posX, userIn.posY, userIn.posZ);
			this.power = power;
		}

		@Override
		public ItemJutsu.JutsuEnum.Type getJutsuType() {
			return ItemJutsu.JutsuEnum.Type.IRYO;
		}

		@Override
		protected void entityInit() {
		}

		@Nullable
		protected EntityLivingBase getUser() {
			if (this.user != null) {
				return this.user;
			}
			return null;
		}

		@Override
		public void onUpdate() {
			super.onUpdate();

			EntityLivingBase user = this.getUser();

			if (user == null || !user.isEntityAlive()) {
				if (!this.world.isRemote) {
					this.setDead();
				}
				return;
			}

			this.setPosition(user.posX, user.posY, user.posZ);

			if (!this.world.isRemote && !this.activated) {
				int healStart = Math.max(0, this.wait - HEAL_DURATION);

				if (!this.healing && this.ticksExisted >= healStart) {
					this.healing = true;
					this.startingHealth = user.getHealth();
					this.totalHealing = Math.max(0.0f, this.markedHealth - this.startingHealth);

					Chakra.Pathway cp = Chakra.pathway(user);
					if (!cp.consume(this.power * this.chakraBurn)) {
						this.activated = true;
						this.setDead();
						return;
					}

					this.chakraConsumed = true;
				}

				if (this.healing) {
					int healingTicks = this.ticksExisted - healStart + 1;
					float progress = Math.min(1.0f, (float)healingTicks / (float)HEAL_DURATION);
					float previousProgress = Math.min(1.0f, (float)(healingTicks - 1) / (float)HEAL_DURATION);

					float smoothProgress = progress * progress * (3.0f - 2.0f * progress);
					float previousSmoothProgress = previousProgress * previousProgress * (3.0f - 2.0f * previousProgress);

					float healAmount = this.totalHealing * (smoothProgress - previousSmoothProgress);

					if (healAmount > 0.0f) {
						user.heal(healAmount);

						Particles.spawnParticle(this.world, Particles.Types.SMOKE,
						 user.posX, user.posY + user.height / 2.0d, user.posZ,
						 14, user.width / 2.0d, user.height / 2.0d, user.width / 2.0d,
						 0d, 0d, 0d,
						 0x0000fff6 | ((0x30 + user.getRNG().nextInt(0x20)) << 24),
						 12 + user.getRNG().nextInt(18), 0, 0xF0, -1, 0);
					}

					if (this.ticksExisted >= this.wait) {
						float remaining = this.markedHealth - user.getHealth();

						if (remaining > 0.0f) {
							user.heal(remaining);
						}

						this.activated = true;

						if (user instanceof EntityPlayer) {
							((EntityPlayer)user).sendStatusMessage(new TextComponentString("Cellular Activation complete"), true);
						}

						this.setDead();
					}
				}
			}
		}

		@Override
		protected void readEntityFromNBT(NBTTagCompound compound) {
			this.markedHealth = compound.getFloat("MarkedHealth");
			this.startingHealth = compound.getFloat("StartingHealth");
			this.totalHealing = compound.getFloat("TotalHealing");
			this.wait = compound.getInteger("Wait");
			this.power = compound.getFloat("Power");
			this.activated = compound.getBoolean("Activated");
			this.healing = compound.getBoolean("Healing");
			this.chakraConsumed = compound.getBoolean("ChakraConsumed");
		}

		@Override
		protected void writeEntityToNBT(NBTTagCompound compound) {
			compound.setFloat("MarkedHealth", this.markedHealth);
			compound.setFloat("StartingHealth", this.startingHealth);
			compound.setFloat("TotalHealing", this.totalHealing);
			compound.setInteger("Wait", this.wait);
			compound.setFloat("Power", this.power);
			compound.setBoolean("Activated", this.activated);
			compound.setBoolean("Healing", this.healing);
			compound.setBoolean("ChakraConsumed", this.chakraConsumed);
		}

		public static class Jutsu implements ItemJutsu.IJutsuCallback {
			private static final String ID_KEY = "IryoCellularActivationEntityIdKey";

			@Override
			public boolean createJutsu(ItemStack stack, EntityLivingBase entity, float power) {
				Entity entity1 = entity.world.getEntityByID(entity.getEntityData().getInteger(ID_KEY));

				if (entity1 instanceof EC && entity1.isEntityAlive()) {
					entity1.setDead();

					if (entity instanceof EntityPlayer && !entity.world.isRemote) {
						((EntityPlayer)entity).sendStatusMessage(new TextComponentString("Off"), true);
					}

					return false;
				}

				if (power < 1.0f) {
					return false;
				}

				entity1 = new EC(entity, power);
				entity.world.spawnEntity(entity1);
				entity.getEntityData().setInteger(ID_KEY, entity1.getEntityId());
				ItemJutsu.setCurrentJutsuCooldown(stack, (long)(20f * (12f + power)));
				if (entity instanceof EntityPlayer && !entity.world.isRemote) {
					((EntityPlayer)entity).sendStatusMessage(new TextComponentString("On"), true);
				}

				return true;
			}

			@Override
			public float getBasePower() {
				return 1.0f;
			}

			@Override
			public float getPowerupDelay() {
				return WAIT_PER_FULL_CHARGE;
			}

			@Override
			public float getMaxPower() {
				return 10.0f;
			}
		}
	}

	@SideOnly(Side.CLIENT)
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
			public RenderCustom(RenderManager rendermanager) {
				super(rendermanager);
			}

			@Override
			public boolean shouldRender(EC livingEntity, ICamera camera, double camX, double camY, double camZ) {
				return true;
			}

			@Override
			public void doRender(EC entity, double x, double y, double z, float entityYaw, float partialTicks) {
			}

			@Override
			protected ResourceLocation getEntityTexture(EC entity) {
				return null;
			}
		}
	}
}