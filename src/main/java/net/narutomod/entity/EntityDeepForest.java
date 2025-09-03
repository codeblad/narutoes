
package net.narutomod.entity;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.narutomod.ElementsNarutomodMod;
import net.narutomod.item.ItemJutsu;
import net.narutomod.item.ItemMokuton;
import net.narutomod.procedure.ProcedureUtils;

import javax.annotation.Nullable;
import java.math.MathContext;

@ElementsNarutomodMod.ModElement.Tag
public class EntityDeepForest extends ElementsNarutomodMod.ModElement {
	public static final int ENTITYID = 1738;
	public static final int ENTITYID_RANGED = 1739;

	public EntityDeepForest(ElementsNarutomodMod instance) {
		super(instance, 679);
	}

	@Override
	public void initElements() {
		elements.entities.add(() -> EntityEntryBuilder.create().entity(EC.class)
		 .id(new ResourceLocation("narutomod", "deep_forest"), ENTITYID).name("deep_forest").tracker(64, 3, true).build());
	}


	public static class EC extends ItemMokuton.WoodSegment implements ItemJutsu.IJutsu {
		private int lifespan = 1200;
		private EC prevSegment;
		private EC firstSegment;
		private boolean canSplit = false;
		private Entity target;
		private Vec3d targetVec;

		@Override
		public AxisAlignedBB getCollisionBoundingBox() {
			return this.getEntityBoundingBox().expand(1,-1,1);
		}

		public EC(World world) {
			super(world);
		}

		public EC(Entity targetIn) {
			this(targetIn.world);
			this.setParent(this);
			this.setLocationAndAngles(targetIn.posX, targetIn.posY-3d, targetIn.posZ, 0f, 0f);
			Vec3d vec = new Vec3d(0,2,0);
			Vec3d newVec = targetIn.getPositionVector().add(vec);
			RayTraceResult result = this.world.rayTraceBlocks(newVec, newVec.addVector(0,-100,0), false, false, true);
			if (result != null && result.typeOfHit != RayTraceResult.Type.MISS) {
				vec = result.hitVec;
				//System.out.println(String.format("HITVEC: %s,", result.hitVec));
				this.setLocationAndAngles(vec.x, vec.y-3d, vec.z, 0f, 0f);
			}
			this.setPositionAndRotationFromParent(1f);
			this.prevSegment = this;
			this.target = targetIn;
			this.targetVec = this.getPositionVector().addVector(0,20,0);
		}

		public EC(EC segment, float yawOffset, float pitchOffset) {
			super(segment, yawOffset, pitchOffset);
			this.target = segment.target;
			this.targetVec = segment.targetVec;
		}


		public EC(EC segment, double offsetX, double offsetY, double offsetZ, float yawOffset, float pitchOffset) {
			super(segment, offsetX, offsetY, offsetZ, yawOffset, pitchOffset);
			this.target = segment.target;
			this.targetVec = segment.targetVec;
		}


		@Override
		public ItemJutsu.JutsuEnum.Type getJutsuType() {
			return ItemJutsu.JutsuEnum.Type.MOKUTON;
		}

		private void setLifespan(int ticks) {
			this.lifespan = ticks;
		}

		@Override
		public void onUpdate() {
			super.onUpdate();
			if (this.ticksExisted == 1 && this.rand.nextFloat() < 0.05f) {
				this.playSound(net.minecraft.util.SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:woodgrow")),
				 1.0f, this.rand.nextFloat() * 0.4f + 0.6f);
			}
			if (this.getParent() != null && this.ticksExisted < this.lifespan) {
				if (!this.world.isRemote && this.getIndex() == 0 && this.ticksExisted == 1 && this.hasLivingTarget()) {

					for (int i = 0; i < 36; i++) {
						Vec3d vec = new Vec3d((0.5f-this.rand.nextFloat())*60, +2d, (0.5f-this.rand.nextFloat())*60);
						Vec3d newVec = this.target.getPositionVector().add(vec);
						RayTraceResult result = this.world.rayTraceBlocks(newVec, newVec.addVector(0,-100,0), false, false, true);
						if (result != null && result.typeOfHit != RayTraceResult.Type.MISS) {
							vec = result.hitVec.subtract(this.getPositionVector()).subtract(0,2,0);
							//System.out.println(String.format("HITVEC: %s,", result.hitVec));
						}
						this.targetVec = vec.addVector(0,30,0);
						float f = ProcedureUtils.getYawFromVec(this.targetVec.subtract(this.getPositionVector().add(vec)));
						EC segment = new EC(this, vec.x, vec.y, vec.z, f + ((this.rand.nextFloat()-0.5f) * 360f), (0.5f-this.rand.nextFloat())*12);
						segment.canSplit = true;
						segment.setSize(2f,2f);
						segment.setLifespan(this.lifespan - this.ticksExisted * 2);
						segment.prevSegment = segment;
						//segment.setPosition(newVec.x,newVec.y,newVec.z);
						this.world.spawnEntity(segment);
					}
				}
				if (!this.world.isRemote && this.getIndex() == 1 && this.ticksExisted > 1 && this.ticksExisted <= 22) {
					float yaw = (this.rand.nextFloat()-0.5f) * 50f;
					int i = this.prevSegment.getIndex();
					/*if (this.hasLivingTarget() && i > 1) {
						yaw = MathHelper.wrapDegrees(ProcedureUtils.getYawFromVec(this.targetVec
						 .subtract(this.prevSegment.getPositionVector()).scale(0.01)) - this.prevSegment.rotationYaw);
						yaw /= Math.max(4.4f - (float)i * 0.075f, 1f);
						yaw *= 5;
					}*/
					//this.prevSegment = new EC(this.prevSegment, yaw, (float)Math.cos(this.rand.nextFloat()*4)*8+1f);
					/*float pitchOffset = -10;
					if (this.ticksExisted > 9) {
						pitchOffset = 0;
					}*/
					float pitch = (float) (30*Math.cos(this.ticksExisted));
					if (this.ticksExisted > 18) {
						pitch = -10;
					}
					this.prevSegment = new EC(this.prevSegment, (0.5f-this.rand.nextFloat())*80, (0.5f-this.rand.nextFloat())*70);
					//this.prevSegment = new EC(this.prevSegment, 15,5);
					this.prevSegment.setSize(2f,2f);
					this.prevSegment.setLifespan(this.lifespan - this.ticksExisted * 2);
					this.world.spawnEntity(this.prevSegment);

				}
				if (!this.world.isRemote && this.getIndex() >= 2 && this.rand.nextFloat() < .03 && this.ticksExisted < 30) {

					BlockPos pos = new BlockPos(this);
					for (; !this.world.isAirBlock(pos); pos = pos.offset(EnumFacing.random(this.rand), this.rand.nextInt(2)));

					new net.narutomod.event.EventSetBlocks(this.world,
					 ImmutableMap.of(pos, Blocks.LEAVES.getStateFromMeta(0)), 0, this.lifespan - this.ticksExisted, false, false);

				}
				if (!this.world.isRemote && this.getIndex() >= 21 && this.ticksExisted < 50) {
					BlockPos pos = new BlockPos(this);
					for (; !this.world.isAirBlock(pos); pos = pos.offset(EnumFacing.random(this.rand), this.rand.nextInt(5)));
					new net.narutomod.event.EventSetBlocks(this.world,
							ImmutableMap.of(pos, Blocks.LEAVES.getStateFromMeta(0)), 0, this.lifespan - this.ticksExisted, false, false);
				}
				/*if (this.targetVec != null && this.targetTargetable()) {
					if (this.ticksExisted > 20) {
						this.target.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, null), 10.0f);
					}
					this.target.setPositionAndUpdate(this.targetVec.x, this.targetVec.y, this.targetVec.z);
				}*/
			} else if (!this.world.isRemote) {
				this.setDead();
			}
		}

		private boolean hasLivingTarget() {
			return this.target != null && this.target.isEntityAlive();
		}

		private boolean targetTargetable() {
			if (!ItemJutsu.canTarget(this.target)) {
				this.target = null;
				return false;
			}
			return true;
		}

		public static class Jutsu implements ItemJutsu.IJutsuCallback {
			@Override
			public boolean createJutsu(ItemStack stack, EntityLivingBase entity, float power) {
				entity.world.playSound(null, entity.posX, entity.posY, entity.posZ, (net.minecraft.util.SoundEvent)
								net.minecraft.util.SoundEvent.REGISTRY.getObject(new ResourceLocation(("narutomod:deep_forest"))),
						SoundCategory.NEUTRAL, 3, 1f);
				entity.world.spawnEntity(new EC(entity));
				((ItemJutsu.Base)stack.getItem()).setCurrentJutsuCooldown(stack, 20*20);
				return true;
			}
		}
	}
}
