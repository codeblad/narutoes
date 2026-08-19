
package net.narutomod.entity;

import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

import net.minecraft.world.World;
import net.minecraft.util.ResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.block.state.IBlockState;

import net.narutomod.item.ItemHyoton;
import net.narutomod.potion.PotionHeaviness;
import net.narutomod.procedure.ProcedureUtils;
import net.narutomod.item.ItemJutsu;
import net.narutomod.ElementsNarutomodMod;

import java.util.Map;
import com.google.common.collect.Maps;

@ElementsNarutomodMod.ModElement.Tag
public class EntityIcePrison extends ElementsNarutomodMod.ModElement {
	public static final int ENTITYID = 226;
	public static final int ENTITYID_RANGED = 227;

	public EntityIcePrison(ElementsNarutomodMod instance) {
		super(instance, 538);
	}

	@Override
	public void initElements() {
		elements.entities.add(() -> EntityEntryBuilder.create().entity(EC.class)
		 .id(new ResourceLocation("narutomod", "ice_prison"), ENTITYID).name("ice_prison").tracker(64, 3, true).build());
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void preInit(FMLPreInitializationEvent event) {
		RenderingRegistry.registerEntityRenderingHandler(EC.class, renderManager -> new CustomRender(renderManager));
	}

	@SideOnly(Side.CLIENT)
	public class CustomRender extends Render<EC> {
		public CustomRender(RenderManager renderManagerIn) {
			super(renderManagerIn);
		}
		@Override
		public void doRender(EC entity, double x, double y, double z, float entityYaw, float partialTicks) {
		}
		@Override
		protected ResourceLocation getEntityTexture(EC entity) {
			return null;
		}
	}

	public static class EC extends Entity implements ItemJutsu.IJutsu {
		private EntityLivingBase user;
		private EntityLivingBase target;
		private BlockPos blockpos;
		private BlockPos tpos[] = new BlockPos[4];
		private int tx, ty, tz, tr;
		private int radius;
		private int tHeight;
		private final BlockPos plist[][] = {
			{ new BlockPos(1, 0, 1), new BlockPos(-1, 0, 1), new BlockPos(1, 0, -1), new BlockPos(-1, 0, -1) },
			{ new BlockPos(-1, 0, 0), new BlockPos(1, 0, 0), new BlockPos(-1, 0, 0), new BlockPos(1, 0, 0) },
			{ new BlockPos(0, 0, -1), new BlockPos(0, 0, -1), new BlockPos(0, 0, 1), new BlockPos(0, 0, 1) } };

		public EC(World world) {
			super(world);
			this.setSize(0.01f, 0.01f);
			this.isImmuneToFire = true;
		}

		public EC(EntityLivingBase userIn, EntityLivingBase targetIn) {
			this(userIn.world);
			this.user = userIn;
			this.target = targetIn;
			this.setPosition(targetIn.posX, targetIn.posY, targetIn.posZ);
			this.blockpos = new BlockPos(this);
			this.tpos[0] = this.blockpos;
			this.radius = (int)(targetIn.width * 0.5 + 1);
			this.tHeight = (int)(targetIn.height + 1);
		}

		@Override
		public ItemJutsu.JutsuEnum.Type getJutsuType() {
			return ItemJutsu.JutsuEnum.Type.HYOTON;
		}

		@Override
		protected void entityInit() {
		}

		@Override
		public void onUpdate() {
			if (this.user != null && ItemJutsu.canTarget(this.target)) {
				if (!this.world.isRemote) {
					this.target.setPositionAndUpdate(this.posX, this.posY + 0.1d, this.posZ);
					this.target.addPotionEffect(new PotionEffect(PotionHeaviness.potion, 20*6, 2));
					if (this.ticksExisted < 10) {
						for (int i = 0; i < 3; i++) {
							ItemHyoton.EntityIceSpike entity1 = new ItemHyoton.EntityIceSpike(this.user);
							entity1.damage = 0;
							Vec3d vec = this.getPositionVector().addVector(-this.radius+this.rand.nextFloat()*this.radius*2,this.tHeight*this.rand.nextFloat(),-this.radius+this.rand.nextFloat()*this.radius*2);
							entity1.setNoGravity(true);
							entity1.maxScale = 1F+this.tHeight;
							entity1.setLocationAndAngles(vec.x, vec.y, vec.z, this.user.getRNG().nextFloat() * 360f, this.user.getRNG().nextFloat() * 45f);
							entity1.growTime = 20;
							entity1.life = 20*4;
							this.world.spawnEntity(entity1);
						}
					}
				}

				//this.tpos[3] = this.tpos[0].add(this.plist[2][i]);
			}
			if (this.ticksExisted > 20*4) {

				this.setDead();
			}
		}

		@Override
		protected void readEntityFromNBT(NBTTagCompound compound) {
		}

		@Override
		protected void writeEntityToNBT(NBTTagCompound compound) {
		}

		public static class Jutsu implements ItemJutsu.IJutsuCallback {
			@Override
			public boolean createJutsu(ItemStack stack, EntityLivingBase entity, float power) {
				RayTraceResult result = ProcedureUtils.objectEntityLookingAt(entity, 10d, 3d, true);
				if (result != null && result.entityHit instanceof EntityLivingBase) {
					entity.world.playSound(null, result.entityHit.posX, result.entityHit.posY, result.entityHit.posZ, 
					 net.minecraft.util.SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:ice_shoot")),
					 net.minecraft.util.SoundCategory.NEUTRAL, 1f, entity.getRNG().nextFloat() * 0.4f + 0.8f);
					entity.world.spawnEntity(new EC(entity, (EntityLivingBase)result.entityHit));
					ItemJutsu.setCurrentJutsuCooldown(stack, 20*12);
					return true;
				}
				return false;
			}
		}

	}
}
