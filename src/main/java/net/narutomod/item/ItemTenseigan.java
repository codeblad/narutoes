
package net.narutomod.item;

import net.minecraft.init.MobEffects;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.event.ModelRegistryEvent;

import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.Item;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.Entity;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.util.ITooltipFlag;

import net.narutomod.*;
import net.narutomod.entity.EntityHakkeshoKeiten;
import net.narutomod.entity.EntityKingOfHell;
import net.narutomod.entity.EntityPretaShield;
import net.narutomod.entity.EntityTenTails;
import net.narutomod.event.SpecialEvent;
import net.narutomod.gui.GuiNinjaScroll;
import net.narutomod.potion.PotionChakraBlocked;
import net.narutomod.potion.PotionFlight;
import net.narutomod.potion.PotionHeaviness;
import net.narutomod.potion.PotionUsingJutsu;
import net.narutomod.procedure.*;
import net.narutomod.creativetab.TabModTab;

import java.util.*;
import javax.annotation.Nullable;
import com.google.common.collect.Multimap;
import com.google.common.collect.Maps;


@ElementsNarutomodMod.ModElement.Tag
public class ItemTenseigan extends ElementsNarutomodMod.ModElement {
	@GameRegistry.ObjectHolder("narutomod:tenseiganhelmet")
	public static final Item helmet = null;
	@GameRegistry.ObjectHolder("narutomod:tenseiganbody")
	public static final Item body = null;
	@GameRegistry.ObjectHolder("narutomod:tenseiganlegs")
	public static final Item legs = null;

	public ItemTenseigan(ElementsNarutomodMod instance) {
		super(instance, 692);
	}


	public static class AirPush extends Entity  {
		private EntityLivingBase user;
		private Vec3d look;
		private Vec3d start;
		List<String> targets = new ArrayList<String>();

		public AirPush(World worldIn) {
			super(worldIn);
			this.setSize(0.01f, 0.01f);
			this.isImmuneToFire = true;
		}


		public AirPush(EntityLivingBase user) {
			this(user.world);
			this.user = user;
			this.setPosition(this.user.posX, this.user.posY, this.user.posZ);
			this.look = this.user.getLookVec();
			this.start = this.user.getPositionVector().addVector(0,1,0);;
			ProcedureSync.SwingMainArm.send(user);
			for (int j = 0; j < (int) 40; j++) {
				Vec3d a = this.start.addVector(-1+this.rand.nextFloat()*2,-1+this.rand.nextFloat()*2,-1+this.rand.nextFloat()*2);
				Vec3d b = this.look.normalize().scale(2+2*this.rand.nextFloat());
				Particles.spawnParticle(this.world, Particles.Types.SMOKE, a.x, a.y, a.z,
						1, 1d, 0d, 1d, b.x,b.y,b.z, 0x64FFFFFF, 50, 0);
			}

			for (double i = 0; i < 40; i++) {
				Vec3d point = this.start.addVector(0,1,0).add(look.scale(i*1.5));
				AxisAlignedBB hitbox = new AxisAlignedBB(new BlockPos(point)).grow(2.5);
				//((WorldServer)this.world).spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, point.x, point.y, point.z, 1, 0d, 0d, 0d, 0d);
				boolean flag = net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.world, this.user);
				this.world.newExplosion(this.user, point.x, point.y, point.z, 2, false, flag);
				/*new net.narutomod.event.EventSphericalExplosion(this.world, this.user,
						(int) point.x,(int) point.y,(int) point.z, 1, 0, 0);*/
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

					float damage = (10+(4.5f*ItemJutsu.getDmgMult(this.user)))*3.0f;
					entity1.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, this.user),damage);
					ProcedureUtils.setVelocity(entity1, look.x*8, look.y*8, look.z*8);

				}
			}


			this.user.world.playSound(null, this.user.posX, this.user.posY, this.user.posZ,
					(net.minecraft.util.SoundEvent) net.minecraft.util.SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:BanshoTenin")),
					SoundCategory.PLAYERS, 1.0F, 1.0F);

		}


		@Override
		protected void entityInit() {
		}

		@Override
		public void onUpdate() {
			if (this.user != null) {
				this.setPosition(this.user.posX, this.user.posY+1, this.user.posZ);
				if (this.ticksExisted < 15) {

					//this.start = this.start.add(look.scale(3));
				}
			}
			if (this.ticksExisted > 20) {
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

	public static class GravityWell extends Entity  {
		private EntityLivingBase user;
		private Vec3d look;
		private Vec3d start;
		List<String> targets = new ArrayList<String>();
		List<EntityLivingBase> trapped = new ArrayList<>();

		public GravityWell(World worldIn) {
			super(worldIn);
			this.setSize(0.01f, 0.01f);
			this.isImmuneToFire = true;
		}


		public GravityWell(EntityLivingBase user) {
			this(user.world);
			this.user = user;
			this.look = this.user.getLookVec();
			RayTraceResult result = ProcedureUtils.objectEntityLookingAt(user, 20d, true);
			this.start = result.hitVec;
			this.setPosition(this.start.x, this.start.y, this.start.z);
			if (!this.world.isRemote) {
				this.user.addPotionEffect(new PotionEffect(PotionUsingJutsu.potion, 20, 1, false, false));
			}
		}


		@Override
		protected void entityInit() {
		}

		@Override
		public void onUpdate() {
			if (this.user != null) {
				this.setPosition( this.start.x, this.start.y, this.start.z);

				int maxDistance = 12;
				if (this.ticksExisted <= 20) {
					for (int j = 0; j < (int) 300; j++) {
						Vec3d a = this.start.addVector(0,1,0);
						Vec3d b = a.addVector(-1+this.rand.nextFloat()*2,-1+this.rand.nextFloat()*2,-1+this.rand.nextFloat()*2);
						b = b.subtract(a).normalize();
						Vec3d c = a.add(b.scale(maxDistance));
						Particles.spawnParticle(this.world, Particles.Types.SMOKE, c.x, c.y, c.z,
								1, 0,0,0, 0,0,0, 0xFFFFFFFF, 100, 5);
					}
				}
				if (this.ticksExisted <= 20 && (this.ticksExisted == 1 || this.ticksExisted%10 == 0)) {


					for (int j = 0; j < (int) 50; j++) {
						Vec3d a = this.start.addVector((double) -maxDistance+this.rand.nextFloat()*maxDistance*2,(double) -maxDistance+this.rand.nextFloat()*maxDistance*2,(double) -maxDistance+this.rand.nextFloat()*maxDistance*2);
						Vec3d b = start.subtract(a).normalize().scale(0.5+0.5*this.rand.nextFloat());
						Particles.spawnParticle(this.world, Particles.Types.SMOKE, a.x, a.y, a.z,
								1, 1d, 0d, 1d, b.x,b.y,b.z, 0x64FFFFFF, 30, 10);
					}
					this.user.world.playSound(null, this.start.x, this.start.y, this.start.z,
							(net.minecraft.util.SoundEvent) net.minecraft.util.SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:BanshoTenin")),
							SoundCategory.PLAYERS, 1.0F, 1.0F);


					AxisAlignedBB hitbox = new AxisAlignedBB(new BlockPos(this.start)).grow(maxDistance);
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
						this.trapped.add((EntityLivingBase) entity1);
						//float damage = 15+(10f*ItemJutsu.getDmgMult(this.user));

					}
				}

				for (EntityLivingBase entity : this.trapped) {
					entity.setPositionAndUpdate(this.posX,this.posY,this.posZ);
				}

				if (this.ticksExisted > 20 && this.ticksExisted < 30) {
					int it = this.ticksExisted-20;
					for (int j = 0; j < (int) 200-6*it; j++) {
						Vec3d a = this.start.addVector(0,1,0);
						Vec3d b = a.addVector(-1+this.rand.nextFloat()*2,-1+this.rand.nextFloat()*2,-1+this.rand.nextFloat()*2);
						b = b.subtract(a).normalize();
						Vec3d c = a.add(b.scale(maxDistance-it));
						Particles.spawnParticle(this.world, Particles.Types.SMOKE, c.x, c.y, c.z,
								1, 0,0,0, 0,0,0, 0xFFFFFFFF, 100, 1);
					}
				}

				if (this.ticksExisted == 30) {
					boolean flag = net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.world, this.user);
					this.world.newExplosion(this.user, this.start.x, this.start.y, this.start.z, 10, false, flag);
					for (EntityLivingBase entity : this.trapped) {
						float damage = (10 + (9f * ItemJutsu.getDmgMult(this.user)));
						entity.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, this.user), damage);
					}
					this.setDead();
				}
			}
			if (this.ticksExisted > 50) {
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

	public static class Denial extends Entity  {
		private EntityLivingBase user;
		private Vec3d look;
		private Vec3d start;
		private final int startup = 40;
		List<String> targets = new ArrayList<String>();

		public Denial(World worldIn) {
			super(worldIn);
			this.setSize(0.01f, 0.01f);
			this.isImmuneToFire = true;
		}


		public Denial(EntityLivingBase user) {
			this(user.world);
			this.user = user;
			this.look = this.user.getLookVec();
			this.setPosition(this.user.posX, this.user.posY, this.user.posZ);
			ProcedureWhenPlayerAttcked.setInvulnerable(user, 60);
			this.user.world.playSound(null, this.user.posX, this.user.posY, this.user.posZ,
					(net.minecraft.util.SoundEvent) net.minecraft.util.SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:windblast")),
					SoundCategory.PLAYERS, 2.0F, 0.8F);
		}


		@Override
		protected void entityInit() {
		}

		@Override
		public void onUpdate() {
			if (this.user != null) {
				this.setPosition( this.user.posX, this.user.posY, this.user.posZ);
				float size = 3;
				if (this.ticksExisted < startup) {
					if (!this.world.isRemote) {
						this.user.addPotionEffect(new PotionEffect(PotionFlight.potion, 2, 1, false, false));
						this.user.addPotionEffect(new PotionEffect(PotionUsingJutsu.potion, 5, 1, false, false));
					}
					for (int j = 0; j < (int) 500; j++) {
						Vec3d a = this.user.getPositionVector().addVector(0,1,0);
						Vec3d b = a.addVector(-1+this.rand.nextFloat()*2,-1+this.rand.nextFloat()*2,-1+this.rand.nextFloat()*2);
						b = b.subtract(a).normalize();
						Vec3d c = a.add(b.scale(size));
						Particles.spawnParticle(this.world, Particles.Types.SMOKE, c.x, c.y, c.z,
								1, 0,0,0, 0,0,0, 0xFFFFFFFF, 100, 1);
					}
					AxisAlignedBB hitbox = new AxisAlignedBB(new BlockPos(this.user.getPositionVector())).grow(size);
					for (Entity entity1 : this.world.getEntitiesWithinAABBExcludingEntity(this.user, hitbox)) {

						if (entity1 instanceof ItemJutsu.IJutsu && !(entity1 instanceof EntityLivingBase)) {
							entity1.setDead();
						}

						if (!(entity1 instanceof EntityLivingBase)) {
							continue;
						}
						Vec3d lookVec = entity1.getPositionVector().subtract(this.user.getPositionVector()).normalize().scale(0.5);
						//this.targets.add(entity1.getUniqueID().toString());
						float damage = 5+(3f*ItemJutsu.getDmgMult(this.user));
						entity1.setVelocity(0,0,0);
						ProcedureUtils.setVelocity(entity1, lookVec.x,lookVec.y,lookVec.z);
						entity1.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, this.user),damage);
					}
				}
				if (this.ticksExisted == startup) {
					size = 15;
					this.user.world.playSound(null, this.user.posX, this.user.posY, this.user.posZ,
							(net.minecraft.util.SoundEvent) net.minecraft.util.SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:BanshoTenin")),
							SoundCategory.PLAYERS, 1.0F, 1.0F);

					for (int j = 0; j < (int) 150; j++) {
						Vec3d a = this.user.getPositionVector().addVector(0,1,0);
						Vec3d b = a.addVector(-size/2+this.rand.nextFloat()*size,-size/2+this.rand.nextFloat()*size,-size/2+this.rand.nextFloat()*size);
						Vec3d c = b.subtract(a).normalize().scale(3+2*this.rand.nextFloat());
						Particles.spawnParticle(this.world, Particles.Types.SMOKE, a.x, a.y, a.z,
								1, 0,0,0, c.x,c.y,c.z, 0x64FFFFFF, 50, 0);
					}
					//SpecialEvent.setSphericalExplosionEvent(this.user.world, (int) this.user.posX, (int) this.user.posY, (int) this.user.posZ, (int) size/3, this.user);
					ProcedureUtils.purgeHarmfulEffects(this.user);
					this.user.extinguish();

					AxisAlignedBB hitbox = new AxisAlignedBB(new BlockPos(this.user.getPositionVector())).grow(size);
					for (Entity entity1 : this.world.getEntitiesWithinAABBExcludingEntity(this.user, hitbox)) {

						if (entity1 instanceof ItemJutsu.IJutsu && !(entity1 instanceof EntityLivingBase)) {
							entity1.setDead();
						}

						if (!(entity1 instanceof EntityLivingBase)) {
							continue;
						}
						/*boolean found = false;
						for (String enemy: this.targets) {
							if (Objects.equals(enemy, entity1.getUniqueID().toString())) {
								found = true;
							}
						}
						if (found) {
							continue;
						}*/
						Vec3d lookVec = entity1.getPositionVector().subtract(this.user.getPositionVector()).normalize().scale(10);
						//this.targets.add(entity1.getUniqueID().toString());
						float damage = 5+(6f*ItemJutsu.getDmgMult(this.user));
						ProcedureUtils.setVelocity(entity1, lookVec.x,lookVec.y,lookVec.z);
						entity1.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, this.user),damage);
					}
				}

			}
			if (this.ticksExisted > startup) {
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

	public static class Drain extends Entity  {
		private EntityLivingBase user;
		private EntityLivingBase target;
		private Vec3d start;
		private Vec3d end;
		private Vec3d look;
		private final int startup = 20;

		public Drain(World worldIn) {
			super(worldIn);
			this.setSize(0.01f, 0.01f);
			this.isImmuneToFire = true;
		}


		public Drain(EntityLivingBase user, EntityLivingBase target) {
			this(user.world);
			this.user = user;
			this.target = target;
			this.setPosition(this.user.posX, this.user.posY, this.user.posZ);
			this.start = this.user.getPositionVector();
			this.end = this.target.getPositionVector();

			this.user.world.playSound(null, this.user.posX, this.user.posY, this.user.posZ,
					(net.minecraft.util.SoundEvent) net.minecraft.util.SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:BanshoTenin")),
					SoundCategory.PLAYERS, 1.0F, 1.0F);
		}


		@Override
		protected void entityInit() {
		}

		@Override
		public void onUpdate() {
			if (this.target == null || this.user == null) {
				this.setDead();
				return;
			}
			if (this.ticksExisted <= this.startup+25) {
				if (!this.world.isRemote) {
					this.user.addPotionEffect(new PotionEffect(PotionFlight.potion, 2, 1, false, false));
					this.user.addPotionEffect(new PotionEffect(PotionUsingJutsu.potion, 5, 1, false, false));
					this.target.addPotionEffect(new PotionEffect(PotionUsingJutsu.potion, 5, 1, false, false));
				}
				this.start = this.user.getPositionVector().addVector(0,0.5,0).add(this.user.getLookVec().scale(1));
				this.setPosition( this.user.posX, this.user.posY, this.user.posZ);



				if (this.ticksExisted < startup) {
					this.look = this.end.subtract(this.start).normalize();
					double distance = this.start.distanceTo(this.end);
					Vec3d point = this.start.add(this.look.scale(distance-distance*this.ticksExisted/startup));
					this.target.setPositionAndUpdate(point.x,point.y,point.z);
				} else {
					this.target.setPositionAndUpdate(this.start.x, this.start.y, this.start.z);
				}

				for (int j = 0; j < (int) 300; j++) {
					Vec3d a = this.target.getPositionVector().addVector(0,1,0);
					Vec3d b = a.addVector(-1+this.rand.nextFloat()*2,-1+this.rand.nextFloat()*2,-1+this.rand.nextFloat()*2);
					b = b.subtract(a).normalize();
					Vec3d c = a.add(b.scale(2));
					Particles.spawnParticle(this.world, Particles.Types.SMOKE, c.x, c.y, c.z,
							1, 0,0,0, 0,0,0, 0xFFFFFFFF, 80, 1);
				}

				if (this.ticksExisted >= startup+5) {
					this.target.hurtResistantTime = 10;
					float damage = 2 + (0.75f * ItemJutsu.getDmgMult(this.user));
					this.target.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, this.user).setDamageBypassesArmor().setDamageIsAbsolute(), damage);
					Chakra.Pathway cp = Chakra.pathway(this.target);
					cp.consume(0.25f / 20);
					if (cp.getAmount() > cp.getMax()) {
						cp.consume(0.02f);
					}
				}
			}
			if (this.ticksExisted == this.startup+20) {
				Vec3d look = this.user.getLookVec();
				this.user.world.playSound(null, this.user.posX, this.user.posY, this.user.posZ,
						(net.minecraft.util.SoundEvent) net.minecraft.util.SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:BanshoTenin")),
						SoundCategory.PLAYERS, 1.0F, 1.0F);
				ProcedureUtils.setVelocity(this.target, look.x*4, look.y*4+1, look.z*4);
				if (!this.world.isRemote) {
					this.target.addPotionEffect(new PotionEffect(PotionChakraBlocked.potion, 8*20, 0, false, false));
					this.target.addPotionEffect(new PotionEffect(PotionHeaviness.potion, 20*5, 3, false, false));
					this.target.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 20 * 5, 4, false, false));
				}
			}
			if (this.ticksExisted > startup+20) {
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

	@Override
	public void initElements() {
		ItemArmor.ArmorMaterial enuma = EnumHelper.addArmorMaterial("TENSEIGAN", "narutomod:sasuke_", 500, new int[]{2, 10, 10, 10}, 0,
		 net.minecraft.util.SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:dojutsu")), 2.0f);

		elements.items.add(() -> new ItemRinnegan.Base(enuma) {
			@Override
			public boolean isTenseigan() {
				return true;
			}

			@Override
			public boolean onJutsuKey1(boolean is_pressed, ItemStack stack, EntityPlayer entity) {
				return false;
			}

			@Override
			public boolean onJutsuKey2(boolean is_pressed, ItemStack stack, EntityPlayer entity) {
				return false;
			}

			@Override
			public boolean onJutsuKey3(boolean is_pressed, ItemStack stack, EntityPlayer entity) {
				return false;
			}

			@Override
			public boolean onSwitchJutsuKey(boolean is_pressed, ItemStack stack, EntityPlayer entity) {
				return false;
			}

			public void tickCooldowns(ItemStack itemStack) {
				if (itemStack.getTagCompound() == null) {
					itemStack.setTagCompound(new NBTTagCompound());
				}
				NBTTagCompound values = itemStack.getTagCompound();
				values.setInteger("airPushCD", values.getInteger("airPushCD")-1);
				values.setInteger("gravWellCD", values.getInteger("gravWellCD")-1);
				values.setInteger("denialCD", values.getInteger("denialCD")-1);
				values.setInteger("drainCD", values.getInteger("drainCD")-1);
			}


			@Override
			public void onUpdate(ItemStack itemstack, World world, Entity entity, int par4, boolean par5) {
				super.onUpdate(itemstack, world, entity, par4, par5);
				if (!world.isRemote && entity instanceof EntityLivingBase) {
					NBTTagCompound nbt = new NBTTagCompound();
					entity.writeToNBT(nbt);

					boolean isOwner = ProcedureUtils.isOriginalOwner((EntityPlayer) entity, itemstack);


					tickCooldowns(itemstack);

					ItemStack eye = ((entity instanceof EntityPlayer) ? ((EntityPlayer) entity).inventory.armorInventory.get(3) : ItemStack.EMPTY);

					Map<String, Object> $_dependencies = Maps.newHashMap();
					$_dependencies.put("entity", entity);
					$_dependencies.put("x", (int) entity.posX);
					$_dependencies.put("y", (int) entity.posY);
					$_dependencies.put("z", (int) entity.posZ);
					$_dependencies.put("world", entity.world);

					NBTTagCompound values = itemstack.getTagCompound();

					if (!ItemByakugan.hasSlot(nbt, 2)) {
						boolean newPressed4 = entity.getEntityData().getBoolean(NarutomodModVariables.EYETOGGLE);
						$_dependencies.put("is_pressed", newPressed4);
						ProcedureByakuganActivate.executeProcedure($_dependencies);
					}
					boolean usingJutsu = ((EntityLivingBase) entity).isPotionActive(PotionUsingJutsu.potion);
					if (!ItemByakugan.hasSlot(nbt, 2)) {

						if (eye.getItem() == new ItemStack(ItemTenseigan.helmet, (int) (1)).getItem()) {
							boolean newPressed = entity.getEntityData().getBoolean(NarutomodModVariables.JutsuKey1Pressed);
							if (!usingJutsu && values.getBoolean("jutsuKey1") && !newPressed) {
								if (entity.isSneaking()) {
									if (values.getInteger("gravWellCD") <= 0 && Chakra.pathway((EntityLivingBase) entity).consume(750d)) {
										values.setInteger("gravWellCD", 20 * 13);
										entity.world.spawnEntity(new GravityWell((EntityLivingBase) entity));
									}
								} else {
									if (values.getInteger("airPushCD") <= 0 && Chakra.pathway((EntityLivingBase) entity).consume(350d)) {
										values.setInteger("airPushCD", 20 * 4);
										entity.world.spawnEntity(new AirPush((EntityLivingBase) entity));
									}
								}

							}

							values.setBoolean("jutsuKey1", newPressed);

							boolean newPressed2 = entity.getEntityData().getBoolean(NarutomodModVariables.JutsuKey2Pressed);
							if (!usingJutsu && values.getBoolean("jutsuKey2") && !newPressed) {
								if (values.getInteger("denialCD") <= 0 && Chakra.pathway((EntityLivingBase) entity).consume(800d)) {
									values.setInteger("denialCD", 20 * 15);
									entity.world.spawnEntity(new Denial((EntityLivingBase) entity));
								}

							}

							values.setBoolean("jutsuKey2", newPressed2);

							boolean newPressed3 = entity.getEntityData().getBoolean(NarutomodModVariables.JutsuKey3Pressed);

							if (!usingJutsu && values.getBoolean("jutsuKey3") && !newPressed3 && values.getInteger("drainCD") <= 0) {
								RayTraceResult result = ProcedureUtils.objectEntityLookingAt(entity,30,5);
								if (result.entityHit instanceof EntityLivingBase && Chakra.pathway((EntityLivingBase) entity).consume(800d)) {
									values.setInteger("drainCD", 20 * 15);
									entity.world.spawnEntity(new Drain((EntityLivingBase) entity, (EntityLivingBase) result.entityHit));
								}
							}

							values.setBoolean("jutsuKey3", newPressed3);
						}
					}
				}
			}

			@Override
			public void onUpdatePost(EntityPlayer player) {
				/*if (!player.world.isRemote && player.ticksExisted % 20 == 3) {
					ItemStack helmetStack = player.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
					GuiNinjaScroll.enableJutsu(player, (ItemJutsu.Base)ItemYoton.block, ItemYoton.SEALING9D, helmetStack.getItem() == helmet);
					GuiNinjaScroll.enableJutsu(player, (ItemJutsu.Base)ItemYoton.block,
					 ItemYoton.SEALING10, helmetStack.getItem() == helmet && EntityTenTails.getBijuManager().isAddedToWorld(player.world));
					if (!(helmetStack.getItem() instanceof ItemRinnegan.Base)) {
						player.inventory.clearMatchingItems(ItemAsuraCanon.block, -1, -1, null);
						if (player.getRidingEntity() instanceof EntityPretaShield.EntityCustom) {
							player.getRidingEntity().setDead();
						}
					}
				}*/
			}
			
			@SideOnly(Side.CLIENT)
			@Override
			public ModelBiped getArmorModel(EntityLivingBase living, ItemStack stack, EntityEquipmentSlot slot, ModelBiped defaultModel) {
				ItemDojutsu.ClientModel.ModelHelmetSnug armorModel = (ItemDojutsu.ClientModel.ModelHelmetSnug)super.getArmorModel(living, stack, slot, defaultModel);
				armorModel.headwearShine = true;
				armorModel.foreheadHide = !ItemRinnegan.isRinnesharinganActivated(stack);
				Item item = living.getHeldItemMainhand().getItem();
				armorModel.headwearHide = item != ItemTenseiganChakraMode.block || ((ItemTenseiganChakraMode.RangedItem)item).isOnCooldown(living);
				armorModel.headHide = !armorModel.headwearHide;
				armorModel.onface.showModel = false;
				armorModel.hornRight.showModel = armorModel.hornLeft.showModel = false;
				return armorModel;
			}

			@Override
			public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
				return "narutomod:textures/tenseiganhelmet.png";
			}

			@Override
			public String getItemStackDisplayName(ItemStack stack) {
				return TextFormatting.AQUA + super.getItemStackDisplayName(stack) + TextFormatting.WHITE;
			}
		}.setUnlocalizedName("tenseiganhelmet").setRegistryName("tenseiganhelmet").setCreativeTab(TabModTab.tab));
		
		elements.items.add(() -> new ItemArmor(enuma, 0, EntityEquipmentSlot.CHEST) {
			@SideOnly(Side.CLIENT)
			private ModelBiped armorModel;

			@Override
			@SideOnly(Side.CLIENT)
			public ModelBiped getArmorModel(EntityLivingBase living, ItemStack stack, EntityEquipmentSlot slot, ModelBiped defaultModel) {
				if (this.armorModel == null) {
					this.armorModel = new ModelSizPathRobe();
				}

				this.armorModel.isSneak = living.isSneaking();
				this.armorModel.isRiding = living.isRiding();
				this.armorModel.isChild = living.isChild();
				return this.armorModel;
			}

			@Override
			public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
				return "narutomod:textures/tenseigan_chakramode.png";
			}

			@Override
			public void onArmorTick(World world, EntityPlayer entity, ItemStack itemstack) {
				{
					Map<String, Object> $_dependencies = new HashMap<>();
					$_dependencies.put("entity", entity);
					$_dependencies.put("world", world);
					$_dependencies.put("itemstack", itemstack);
					ProcedureTenseiganBodyTickEvent.executeProcedure($_dependencies);
				}
			}

			@Override
			public int getDamage(ItemStack stack) {
				int itemDamage = this.getMetadata(stack);
				if (itemDamage > this.getMaxDamage()) {
					itemDamage = this.getMaxDamage();
				}
				return itemDamage;
			}
		}.setUnlocalizedName("tenseiganbody").setRegistryName("tenseiganbody").setCreativeTab(null));
		elements.items.add(() -> new ItemArmor(enuma, 0, EntityEquipmentSlot.LEGS) {
			@SideOnly(Side.CLIENT)
			private ModelBiped armorModel;

			@Override
			@SideOnly(Side.CLIENT)
			public ModelBiped getArmorModel(EntityLivingBase living, ItemStack stack, EntityEquipmentSlot slot, ModelBiped defaultModel) {
				if (this.armorModel == null) {
					this.armorModel = new ModelSizPathRobe();
				}

				this.armorModel.isSneak = living.isSneaking();
				this.armorModel.isRiding = living.isRiding();
				this.armorModel.isChild = living.isChild();
				return this.armorModel;
			}

			@Override
			public void onArmorTick(World world, EntityPlayer entity, ItemStack itemstack) {
				{
					Map<String, Object> $_dependencies = new HashMap<>();
					$_dependencies.put("entity", entity);
					$_dependencies.put("world", world);
					$_dependencies.put("itemstack", itemstack);
					ProcedureTenseiganBodyTickEvent.executeProcedure($_dependencies);
				}
			}

			@Override
			public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
				return "narutomod:textures/tenseigan_chakramode.png";
			}

			@Override
			public int getDamage(ItemStack stack) {
				int itemDamage = this.getMetadata(stack);
				if (itemDamage > this.getMaxDamage()) {
					itemDamage = this.getMaxDamage();
				}
				return itemDamage;
			}
		}.setUnlocalizedName("tenseiganlegs").setRegistryName("tenseiganlegs").setCreativeTab(null));
	}


	public static boolean isWearing(EntityLivingBase player) {
		return player.getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem() == helmet;
	}

	public static boolean canUseChakraMode(ItemStack stack, EntityPlayer player) {
		return stack.hasTagCompound() //&& stack.getTagCompound().getDouble("ByakuganCount") >= 5.0d
				&& stack.getTagCompound().getInteger("ZetsuFlesh") >= 3 && PlayerTracker.getBattleXp(player) >= 10000;
	}

	public static boolean isWearingFullArmor(EntityLivingBase entity) {
		return entity.getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem() == helmet
		 && entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() == body
		 && entity.getItemStackFromSlot(EntityEquipmentSlot.LEGS).getItem() == legs;
	}

	/*public class EventHook {
		@SubscribeEvent
		public void onTick(TickEvent.PlayerTickEvent event) {
			if (event.phase == TickEvent.Phase.END) {
				((ItemRinnegan.Base)helmet).onPlayerTickEventPost(event.player);
			}
		}
	}

	@Override
	public void init(FMLInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(new EventHook());
	}*/

	@SideOnly(Side.CLIENT)
	@Override
	public void registerModels(ModelRegistryEvent event) {
		ModelLoader.setCustomModelResourceLocation(helmet, 0, new ModelResourceLocation("narutomod:tenseiganhelmet", "inventory"));
		ModelLoader.setCustomModelResourceLocation(body, 0, new ModelResourceLocation("narutomod:tenseiganbody", "inventory"));
		ModelLoader.setCustomModelResourceLocation(legs, 0, new ModelResourceLocation("narutomod:tenseiganlegs", "inventory"));
	}
	// Made with Blockbench 4.1.5
	// Exported for Minecraft version 1.7 - 1.12
	// Paste this class into your mod and generate all required imports
	@SideOnly(Side.CLIENT)
	public class ModelSizPathRobe extends ModelBiped {
		//private final ModelRenderer bipedBody;
		private final ModelRenderer robe;
		private final ModelRenderer bone5;
		private final ModelRenderer bone6;
		private final ModelRenderer skirtRight;
		private final ModelRenderer bone2;
		private final ModelRenderer bone;
		private final ModelRenderer bone3;
		private final ModelRenderer bone4;
		private final ModelRenderer skirtLeft;
		private final ModelRenderer bone7;
		private final ModelRenderer bone8;
		private final ModelRenderer bone9;
		private final ModelRenderer bone10;
		//private final ModelRenderer bipedRightArm;
		//private final ModelRenderer bipedLeftArm;
		//private final ModelRenderer bipedRightLeg;
		//private final ModelRenderer bipedLeftLeg;
	
		public ModelSizPathRobe() {
			textureWidth = 64;
			textureHeight = 64;
	
			bipedBody = new ModelRenderer(this);
			bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
			bipedBody.cubeList.add(new ModelBox(bipedBody, 16, 16, -4.0F, 0.0F, -2.0F, 8, 12, 4, 0.05F, false));
	
			robe = new ModelRenderer(this);
			robe.setRotationPoint(0.0F, 24.0F, 0.0F);
			bipedBody.addChild(robe);
			robe.cubeList.add(new ModelBox(robe, 16, 32, -4.0F, -24.0F, -2.0F, 8, 12, 4, 0.25F, false));
	
			bone5 = new ModelRenderer(this);
			bone5.setRotationPoint(0.0F, -24.0F, -1.8F);
			robe.addChild(bone5);
			setRotationAngle(bone5, -0.3927F, 0.0F, 0.0F);
			bone5.cubeList.add(new ModelBox(bone5, 24, 48, -4.0F, -4.0F, 0.0F, 8, 4, 4, 1.0F, false));
	
			bone6 = new ModelRenderer(this);
			bone6.setRotationPoint(0.0F, 1.0F, 3.0F);
			bone5.addChild(bone6);
			setRotationAngle(bone6, -0.5236F, 0.0F, 0.0F);
			bone6.cubeList.add(new ModelBox(bone6, 18, 1, -4.0F, -6.0F, -1.0F, 8, 5, 2, 1.0F, false));
	
			skirtRight = new ModelRenderer(this);
			skirtRight.setRotationPoint(0.0F, -0.25F, 0.0F);
			robe.addChild(skirtRight);
			
	
			bone2 = new ModelRenderer(this);
			bone2.setRotationPoint(0.0F, -12.0F, -2.0F);
			skirtRight.addChild(bone2);
			setRotationAngle(bone2, -0.1745F, 0.0F, 0.1745F);
			bone2.cubeList.add(new ModelBox(bone2, 0, 48, -4.0F, 0.0F, 0.0F, 4, 8, 0, 0.0F, false));
	
			bone = new ModelRenderer(this);
			bone.setRotationPoint(-4.0F, -12.25F, 0.0F);
			skirtRight.addChild(bone);
			setRotationAngle(bone, -0.1745F, 0.0F, 0.1745F);
			bone.cubeList.add(new ModelBox(bone, 8, 48, 0.0F, 0.0F, -2.0F, 0, 8, 4, 0.0F, false));
	
			bone3 = new ModelRenderer(this);
			bone3.setRotationPoint(-4.0F, -12.25F, 0.0F);
			skirtRight.addChild(bone3);
			setRotationAngle(bone3, 0.1745F, 0.0F, 0.1745F);
			bone3.cubeList.add(new ModelBox(bone3, 16, 48, 0.0F, 0.0F, -2.0F, 0, 8, 4, 0.0F, false));
	
			bone4 = new ModelRenderer(this);
			bone4.setRotationPoint(0.0F, -12.0F, 2.0F);
			skirtRight.addChild(bone4);
			setRotationAngle(bone4, 0.1745F, 0.0F, 0.1745F);
			bone4.cubeList.add(new ModelBox(bone4, 0, 56, -4.0F, 0.0F, 0.0F, 4, 8, 0, 0.0F, false));
	
			skirtLeft = new ModelRenderer(this);
			skirtLeft.setRotationPoint(0.0F, -0.25F, 0.0F);
			robe.addChild(skirtLeft);
			
	
			bone7 = new ModelRenderer(this);
			bone7.setRotationPoint(0.0F, -12.0F, -2.0F);
			skirtLeft.addChild(bone7);
			setRotationAngle(bone7, -0.1745F, 0.0F, -0.1745F);
			bone7.cubeList.add(new ModelBox(bone7, 0, 48, 0.0F, 0.0F, 0.0F, 4, 8, 0, 0.0F, true));
	
			bone8 = new ModelRenderer(this);
			bone8.setRotationPoint(4.0F, -12.25F, 0.0F);
			skirtLeft.addChild(bone8);
			setRotationAngle(bone8, -0.1745F, 0.0F, -0.1745F);
			bone8.cubeList.add(new ModelBox(bone8, 8, 48, 0.0F, 0.0F, -2.0F, 0, 8, 4, 0.0F, true));
	
			bone9 = new ModelRenderer(this);
			bone9.setRotationPoint(4.0F, -12.25F, 0.0F);
			skirtLeft.addChild(bone9);
			setRotationAngle(bone9, 0.1745F, 0.0F, -0.1745F);
			bone9.cubeList.add(new ModelBox(bone9, 16, 48, 0.0F, 0.0F, -2.0F, 0, 8, 4, 0.0F, true));
	
			bone10 = new ModelRenderer(this);
			bone10.setRotationPoint(0.0F, -12.0F, 2.0F);
			skirtLeft.addChild(bone10);
			setRotationAngle(bone10, 0.1745F, 0.0F, -0.1745F);
			bone10.cubeList.add(new ModelBox(bone10, 0, 56, 0.0F, 0.0F, 0.0F, 4, 8, 0, 0.0F, true));
	
			bipedRightArm = new ModelRenderer(this);
			bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
			setRotationAngle(bipedRightArm, -0.1745F, 0.0F, 0.0F);
			bipedRightArm.cubeList.add(new ModelBox(bipedRightArm, 40, 16, -3.0F, -2.0F, -2.0F, 4, 12, 4, 0.05F, false));
			bipedRightArm.cubeList.add(new ModelBox(bipedRightArm, 40, 32, -3.0F, -2.0F, -2.0F, 4, 12, 4, 0.3F, false));
	
			bipedLeftArm = new ModelRenderer(this);
			bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
			setRotationAngle(bipedLeftArm, -0.1745F, 0.0F, 0.0F);
			bipedLeftArm.cubeList.add(new ModelBox(bipedLeftArm, 48, 0, -1.0F, -2.0F, -2.0F, 4, 12, 4, 0.05F, true));
			bipedLeftArm.cubeList.add(new ModelBox(bipedLeftArm, 40, 32, -1.0F, -2.0F, -2.0F, 4, 12, 4, 0.3F, true));
	
			bipedRightLeg = new ModelRenderer(this);
			bipedRightLeg.setRotationPoint(-1.9F, 12.0F, 0.0F);
			setRotationAngle(bipedRightLeg, 0.0F, 0.0F, 0.0349F);
			bipedRightLeg.cubeList.add(new ModelBox(bipedRightLeg, 0, 16, -2.0F, 0.0F, -2.0F, 4, 12, 4, 0.1F, false));
			bipedRightLeg.cubeList.add(new ModelBox(bipedRightLeg, 0, 32, -2.0F, 0.0F, -2.0F, 4, 12, 4, 0.3F, false));
	
			bipedLeftLeg = new ModelRenderer(this);
			bipedLeftLeg.setRotationPoint(1.9F, 12.0F, 0.0F);
			setRotationAngle(bipedLeftLeg, 0.0F, 0.0F, -0.0349F);
			bipedLeftLeg.cubeList.add(new ModelBox(bipedLeftLeg, 0, 16, -2.0F, 0.0F, -2.0F, 4, 12, 4, 0.1F, true));
			bipedLeftLeg.cubeList.add(new ModelBox(bipedLeftLeg, 0, 32, -2.0F, 0.0F, -2.0F, 4, 12, 4, 0.3F, true));
		}

		@Override
		public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
			if (entity instanceof AbstractClientPlayer && ((AbstractClientPlayer)entity).getSkinType().equals("slim")) {
				this.bipedLeftArm.setRotationPoint(5.0F, 2.5F, 0.0F);
				this.bipedRightArm.setRotationPoint(-5.0F, 2.5F, 0.0F);
			}
			if (this.bipedBody.showModel || this.bipedLeftLeg.showModel) {
				GlStateManager.disableLighting();
				OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
			}
			super.render(entity, f, f1, f2, f3, f4, f5);
			if (this.bipedBody.showModel || this.bipedLeftLeg.showModel) {
				int i = entity.getBrightnessForRender();
				OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)(i % 65536), (float)(i / 65536));
				GlStateManager.enableLighting();
			}
		}

		public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
			modelRenderer.rotateAngleX = x;
			modelRenderer.rotateAngleY = y;
			modelRenderer.rotateAngleZ = z;
		}

	}
}
