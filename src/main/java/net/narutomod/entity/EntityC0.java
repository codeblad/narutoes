
package net.narutomod.entity;

import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

import net.minecraft.world.World;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.DamageSource;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundCategory;

import net.narutomod.item.ItemJutsu;
import net.narutomod.procedure.ProcedureAoeCommand;
import net.narutomod.event.EventSphericalExplosion;
import net.narutomod.ElementsNarutomodMod;

@ElementsNarutomodMod.ModElement.Tag
public class EntityC0 extends ElementsNarutomodMod.ModElement {
	public static final int ENTITYID = 9124;
	public static final int ENTITYID_RANGED = 9125;

	public EntityC0(ElementsNarutomodMod instance) {
		super(instance, 9546);
	}

	@Override
	public void initElements() {
		elements.entities.add(() -> EntityEntryBuilder.create().entity(EC.class)
		 .id(new ResourceLocation("narutomod", "c0"), ENTITYID)
.name("c0").tracker(64, 3, true).build());
	}

	public static class EC extends Entity implements ItemJutsu.IJutsu {
		private final int startupTime = 25;
		private final int growTime = 200;
		private final int fuseTime = 225;
		private final int explosionRange = 82;
		private final float explosionDamage = 50.0f;
		private EntitySpecialEffect.EntityCustom effectEntity;
		private EntitySpecialEffect.EntityCustom extraeffectEntity;
		private EntityLivingBase user;

		public EC(World world) {
			super(world);
			this.setSize(0f, 0f);
			//this.setInvisible(true);
			//this.setSize(0.6f, 1.8f);
			//this.isImmuneToFire = true;
			//this.setNoGravity(true);
		}

		public EC(EntityLivingBase userIn) {
			this(userIn.world);
			this.user = userIn;
		}

		@Override
		public ItemJutsu.JutsuEnum.Type getJutsuType() {
			return ItemJutsu.JutsuEnum.Type.BAKUTON;
		}

		@Override
		protected void readEntityFromNBT(NBTTagCompound compound) {
		}

		@Override
		protected void writeEntityToNBT(NBTTagCompound compound) {
		}

		@Override
		protected void entityInit() {
		}

		// @Override
		// protected void applyEntityAttributes() {
		// 	super.applyEntityAttributes();
		// 	this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3D);
		// 	this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(500D);
		// }

		@Override
		public boolean attackEntityFrom(DamageSource source, float amount) {
			//if (source.isExplosion() || source == DamageSource.FALL) {
				return false;
			//}
			//return super.attackEntityFrom(source, amount);
		}

		@Override
		public void onUpdate() {
			if (this.user != null) {
				this.setPosition(this.user.posX, this.user.posY, this.user.posZ);
			}
			if (!this.world.isRemote) {
				if (this.ticksExisted > this.startupTime ) {
				if (this.effectEntity == null) {
					world.playSound(null, this.user.posX, this.user.posY, this.user.posZ, net.minecraft.util.SoundEvent.REGISTRY
			 .getObject(new ResourceLocation("narutomod:c0")), SoundCategory.NEUTRAL, 10f, 1f);
					//this.playSound(SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:c0")), 50f, 1f);
					this.effectEntity = EntitySpecialEffect.spawn(
						this.user.world,
						EntitySpecialEffect.Type.LINES_COLOR_END,
						0x9FF8F0,
						6f,
						240,
						this.user.posX, this.user.posY + this.user.height * 0.4, this.user.posZ
					);
				} else {
					this.effectEntity.setPosition(this.user.posX, this.user.posY + this.user.height * 0.4, this.user.posZ);
				}
			}
			
			if (this.ticksExisted > this.growTime ) {
			if (this.extraeffectEntity == null) {
				this.extraeffectEntity = EntitySpecialEffect.spawn(
						this.user.world,
						EntitySpecialEffect.Type.LINES_BURST_COLOR_END,
						0x92dbff,
						7f,
						50,
						this.user.posX, this.user.posY + this.user.height * 0.4, this.user.posZ
					);
			} else {
				this.extraeffectEntity.setPosition(this.user.posX, this.user.posY + this.user.height * 0.4, this.user.posZ);
			}
		}
			//if (this.detonateTicks > 0) {
			//	this.detonate();
			//}
			
			if (this.ticksExisted > this.fuseTime ) {
				new EventSphericalExplosion(this.world, null, (int)this.posX, (int)this.posY + 5, (int)this.posZ, this.explosionRange, 0, 0.0f);
				ProcedureAoeCommand.set(this, 0d, this.explosionRange).damageEntities(ItemJutsu.causeJutsuDamage(this, null), this.explosionDamage+300f*ItemJutsu.getDmgMult(this.user));
				this.setDead();
				this.effectEntity.setDead();
				this.extraeffectEntity.setDead();
			}
			
			}
		}

		/*private void detonate() {
			if (this.detonateTicks > 20) {
				this.setDead();
				if (this.effectEntity != null) {
					this.effectEntity.setLifespan(50);
				}
			} else {
				boolean flag = net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.world, this);
				this.world.newExplosion(this, this.posX + (this.rand.nextDouble()-0.5d) * 30d,
				 this.posY, this.posZ + (this.rand.nextDouble()-0.5d) * 30d, 25f, flag, flag);
			}
			this.detonateTicks++;
		}*/
	}
}
