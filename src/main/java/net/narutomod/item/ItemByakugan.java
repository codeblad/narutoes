package net.narutomod.item;

import com.google.common.collect.Lists;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.MobEffects;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.items.ItemHandlerHelper;

import net.minecraft.world.World;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.Item;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.client.Minecraft;

import net.narutomod.Chakra;
import net.narutomod.Particles;
import net.narutomod.entity.*;
import net.narutomod.gui.overlay.OverlayByakuganView;
import net.narutomod.potion.PotionChakraBlocked;
import net.narutomod.potion.PotionHeaviness;
import net.narutomod.potion.PotionUsingJutsu;
import net.narutomod.procedure.*;
import net.narutomod.creativetab.TabModTab;
import net.narutomod.ElementsNarutomodMod;
import net.narutomod.NarutomodModVariables;

import javax.annotation.Nullable;
import java.util.*;

import com.google.common.collect.Multimap;
import com.google.common.collect.Maps;

@ElementsNarutomodMod.ModElement.Tag
public class ItemByakugan extends ElementsNarutomodMod.ModElement {
	@ObjectHolder("narutomod:byakuganhelmet")
	public static final Item helmet = null;
	private static final String RINNESHARINGAN_KEY = NarutomodModVariables.RINNESHARINGAN_ACTIVATED;
	private static final String TENSEIGANEVOLVEDTIME = NarutomodModVariables.tenseiganEvolvedTime;
	private final UUID RINNESHARINGAN_MODIFIER = UUID.fromString("c69907b2-2687-47ab-aca0-49898cd38463");
	private static final double BYAKUGAN_CHAKRA_USAGE = 5d; //per half sec
	private static final double ROKUJUYONSHO_CHAKRA_USAGE = 600d;
	private static final double KAITEN_CHAKRA_USAGE = 5d; // per tick
	private static final double KUSHO_CHAKRA_USAGE = 3d; // x pressDuration

	public static boolean hasSlot(NBTTagCompound nbt, int targetSlot) {
		if (nbt.hasKey("ForgeCaps", Constants.NBT.TAG_COMPOUND)) {
			NBTTagCompound forgeCaps = nbt.getCompoundTag("ForgeCaps");
			if (forgeCaps.hasKey("knapm:container", Constants.NBT.TAG_COMPOUND)) {
				NBTTagCompound container = forgeCaps.getCompoundTag("knapm:container");
				if (container.hasKey("Items", Constants.NBT.TAG_LIST)) {
					NBTTagList items = container.getTagList("Items", Constants.NBT.TAG_COMPOUND);
					for (int i = 0; i < items.tagCount(); i++) {
						NBTTagCompound itemEntry = items.getCompoundTagAt(i);
						int slot = itemEntry.getInteger("Slot");
						if (slot == targetSlot) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	public ItemByakugan(ElementsNarutomodMod instance) {
		super(instance, 98);
	}

	public static double getByakuganChakraUsage(EntityLivingBase entity) {
		ItemStack stack = entity.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
		return stack.getItem() == helmet ? ((ItemDojutsu.Base)helmet).isOwner(stack, entity) ? BYAKUGAN_CHAKRA_USAGE
				: BYAKUGAN_CHAKRA_USAGE * 2 : (Double.MAX_VALUE * 0.001d);
	}

	public static double getRokujuyonshoChakraUsage(EntityLivingBase entity) {
		ItemStack stack = entity.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
		return stack.getItem() == helmet && ((ItemDojutsu.Base)helmet).isOwner(stack, entity) ? ROKUJUYONSHO_CHAKRA_USAGE
				: (Double.MAX_VALUE * 0.001d);
	}

	public static double getKaitenChakraUsage(EntityLivingBase entity) {
		ItemStack stack = entity.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
		return stack.getItem() == helmet && ((ItemDojutsu.Base)helmet).isOwner(stack, entity) ? KAITEN_CHAKRA_USAGE
				: (Double.MAX_VALUE * 0.001d);
	}

	public static double getKushoChakraUsage(EntityLivingBase entity) {
		ItemStack stack = entity.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
		return stack.getItem() == helmet && ((ItemDojutsu.Base)helmet).isOwner(stack, entity) ? KUSHO_CHAKRA_USAGE
				: (Double.MAX_VALUE * 0.001d);
	}

	public static class AirPalm extends Entity  {
		private EntityLivingBase user;
		private Vec3d look;
		private Vec3d start;
		List<String> targets = new ArrayList<String>();

		public AirPalm(World worldIn) {
			super(worldIn);
			this.setSize(0.01f, 0.01f);
			this.isImmuneToFire = true;
		}


		public AirPalm(EntityLivingBase user) {
			this(user.world);
			this.user = user;
			this.setPosition(this.user.posX, this.user.posY, this.user.posZ);
			this.look = this.user.getLookVec();
			this.user.world.playSound(null, this.user.posX, this.user.posY, this.user.posZ,
					(net.minecraft.util.SoundEvent) net.minecraft.util.SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:HakkeKusho")),
					SoundCategory.PLAYERS, 1.0F, 1.0F);
			this.start = this.user.getPositionVector().addVector(0,1,0);;
			ProcedureSync.SwingMainArm.send(user);
			for (int j = 0; j < (int) 40; j++) {
				Vec3d a = this.start.addVector(-1+this.rand.nextFloat()*2,-1+this.rand.nextFloat()*2,-1+this.rand.nextFloat()*2);
				Vec3d b = this.look.normalize().scale(2+2*this.rand.nextFloat());
				Particles.spawnParticle(this.world, Particles.Types.SMOKE, a.x, a.y, a.z,
						1, 1d, 0d, 1d, b.x,b.y,b.z, 0x64FFFFFF, 25, 0);
			}

		}


		@Override
		protected void entityInit() {
		}

		@Override
		public void onUpdate() {
			if (this.user != null) {
				this.setPosition(this.user.posX, this.user.posY+1, this.user.posZ);
				if (this.ticksExisted < 15) {
					for (double i = 0; i < 3; i++) {
						Vec3d point = this.start.addVector(0,1,0).add(look.scale(i));
						AxisAlignedBB hitbox = new AxisAlignedBB(new BlockPos(point)).grow(2);
						((WorldServer)this.world).spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, point.x, point.y, point.z, 1, 0d, 0d, 0d, 0d);
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
							float damage = 10+(3f*ItemJutsu.getDmgMult(this.user));
							entity1.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, this.user),damage);
							ProcedureUtils.setVelocity(entity1, look.x*6, look.y*6, look.z*6);

						}
					}
					this.start = this.start.add(look.scale(3));
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

	public static class MountainCrusher extends Entity  {
		private EntityLivingBase user;
		private Vec3d look;
		private Vec3d start;
		List<String> targets = new ArrayList<String>();

		public MountainCrusher(World worldIn) {
			super(worldIn);
			this.setSize(0.01f, 0.01f);
			this.isImmuneToFire = true;
		}


		public MountainCrusher(EntityLivingBase user) {
			this(user.world);
			this.user = user;
			this.setPosition(this.user.posX, this.user.posY, this.user.posZ);
			this.look = this.user.getLookVec();
			this.user.world.playSound(null, this.user.posX, this.user.posY, this.user.posZ,
					(net.minecraft.util.SoundEvent) net.minecraft.util.SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:mountain_crusher")),
					SoundCategory.PLAYERS, 1.0F, 1.0F);
			this.start = this.user.getPositionVector().addVector(0,1,0);;
			ProcedureSync.SwingMainArm.send(user);
			Vec3d point = this.start.addVector(0,1,0).add(look.scale(5));
			for (int j = 0; j < (int) 400; j++) {
				Vec3d a = point.addVector(-5+this.rand.nextFloat()*10,-5+this.rand.nextFloat()*10,-5+this.rand.nextFloat()*10);
				Vec3d newLook = (a.subtract(point)).normalize();
				Vec3d b = newLook.normalize().scale(0.05+0.05*this.rand.nextFloat());
				Particles.spawnParticle(this.world, Particles.Types.SMOKE, a.x, a.y, a.z,
						1, 1d, 0d, 1d, b.x,b.y,b.z, 0xFFFFFFFF, 60, 0);
			}
			boolean flag = net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.world, this.user);
			this.world.newExplosion(this.user, point.x, point.y, point.z, 3, false, flag);
			AxisAlignedBB hitbox = new AxisAlignedBB(new BlockPos(point)).grow(5.5);
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
				float damage = 15+(7f*ItemJutsu.getDmgMult(this.user));
				entity1.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, this.user),damage);

			}

		}


		@Override
		protected void entityInit() {
		}

		@Override
		public void onUpdate() {
			if (this.user != null) {
				this.setPosition(this.user.posX, this.user.posY+1, this.user.posZ);
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

	public static class SixtyFourPalms extends Entity  {
		private EntityLivingBase user;
		private Vec3d start;
		private Vec3d userStart;
		private EntityLivingBase target;
		private Entity trigram;
		private final int delay = 20;
		private int finalDelay = 64;
		private int counter = 0;
		private int palmTracker = 0;
		private int hitDelay = 0;
		private boolean used = false;
		private int[] palmArray = {2, 2, 4, 8, 16, 32};

		public SixtyFourPalms(World worldIn) {
			super(worldIn);
			this.setSize(0.1F, 0.1F);
			this.isImmuneToFire = true;
			this.ignoreFrustumCheck = true;
			this.setEntityInvulnerable(true);
		}


		public SixtyFourPalms(EntityLivingBase user, EntityLivingBase target) {
			this(user.world);
			this.user = user;
			this.target = target;
			this.setLocationAndAngles(user.posX, user.posY, user.posZ, 0.0f, 0.0f);
			this.user.world.playSound(null, this.user.posX, this.user.posY, this.user.posZ,
					(net.minecraft.util.SoundEvent) net.minecraft.util.SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:HakkeRokujuuyonShou")),
					SoundCategory.PLAYERS, 1.0F, 1.0F);

			this.trigram = new EntityEightTrigrams.EntityCustom(this.user);
			this.user.world.spawnEntity(this.trigram);
			this.start = this.target.getPositionVector();
			this.userStart = this.user.getPositionVector();
			this.finalDelay = (4*6);
			ProcedureRenderView.changeFog(this.user, 1, this.delay, 0, 0.0F, 0.0F, 0.0F, 0.0F);

			ProcedureRenderView.changeFog(this.target, 1, this.delay, 0, 0.0F, 0.0F, 0.0F, 0.0F);
		}


		@Override
		protected void entityInit() {
		}

		@Override
		public void onUpdate() {
			if (this.user != null && this.target != null) {
				if (!this.world.isRemote) {
					this.user.addPotionEffect(new PotionEffect(PotionUsingJutsu.potion, 5, 1, false, false));
				}
				int palms = this.palmArray[this.counter];
				--this.hitDelay;
				if (this.ticksExisted > this.delay && this.hitDelay <= 0) {
					if (this.palmTracker < palms) {
						if (this.palmTracker == 0) {
							this.userStart = this.start.addVector(-2+this.rand.nextFloat()*4,0,-2+this.rand.nextFloat()*4);
							Vec3d vec = this.start.subtract(this.userStart).normalize();
							this.user.rotationYaw = ProcedureUtils.getYawFromVec(vec);
							this.user.rotationPitch = ProcedureUtils.getPitchFromVec(vec);
						}
						Chakra.Pathway cp = Chakra.pathway(this.target);
						cp.consume(0.1f/64);
						if (cp.getAmount() > cp.getMax()) {
							cp.consume(0.05f);
						}
						this.palmTracker++;
					} else {
						if (this.counter < 5) {
							this.counter++;
						} else {
							this.used = true;
						}
						this.palmTracker = 0;
						this.hitDelay = 8;
					}
					for (int j = 0; j < (int) 4; j++) {
						Vec3d look = this.start.subtract(this.userStart).normalize();
						Vec3d a = this.start.addVector(0,1,0).addVector(-1+this.rand.nextFloat()*2,-1+this.rand.nextFloat()*2,-1+this.rand.nextFloat()*2);;
						Vec3d b = look.scale(0.2+0.5*this.rand.nextFloat());
						Particles.spawnParticle(this.world, Particles.Types.SMOKE, a.x, a.y, a.z,
								1, 1d, 0d, 1d, b.x,b.y,b.z, 0xCC429BF5, 15, 0);


					}
					Vec3d c = this.start.addVector(0,1,0).addVector(-0.5+this.rand.nextFloat()*1,-0.5+this.rand.nextFloat()*1,-0.5+this.rand.nextFloat()*1);
					((WorldServer)this.world).spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, c.x, c.y, c.z, 1, 0d, 0d, 0d, 0d);
					this.target.hurtResistantTime = 10;
					float damage = 5+(0.3f*ItemJutsu.getDmgMult(this.user));
					this.target.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, this.user),damage);
					this.playSound(SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:bullet_impact")),
							1f, 0.6f+this.rand.nextFloat()*1.2f);
				}
				if (this.used) {
					Vec3d look = this.user.getLookVec();
					ProcedureUtils.setVelocity(this.target, look.x*3, look.y*3+1, look.z*3);
					if (!this.world.isRemote) {
						this.user.addPotionEffect(new PotionEffect(PotionChakraBlocked.potion, 8*20, 0, false, false));
						this.target.addPotionEffect(new PotionEffect(PotionHeaviness.potion, 20*5, 3, false, false));
					}
				}
				this.user.setPositionAndUpdate(this.userStart.x, this.userStart.y, this.userStart.z);
				this.target.setPositionAndUpdate(this.start.x, this.start.y, this.start.z);
			}
			if (this.ticksExisted > 500 || this.used) {
				this.trigram.setDead();
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
		ItemArmor.ArmorMaterial enuma = EnumHelper.addArmorMaterial("BYAKUGAN", "narutomod:byakugan_", 25, new int[]{2, 5, 6, 12}, 0, null, 2.0F);

		this.elements.items.add(() -> new ItemDojutsu.Base(enuma) {

			@Override
			public ItemDojutsu.Type getType() {
				return ItemDojutsu.Type.BYAKUGAN;
			}

			@SideOnly(Side.CLIENT)
			@Override
			public ModelBiped getArmorModel(EntityLivingBase living, ItemStack stack, EntityEquipmentSlot slot, ModelBiped defaultModel) {
				ItemDojutsu.ClientModel.ModelHelmetSnug armorModel = (ItemDojutsu.ClientModel.ModelHelmetSnug)super.getArmorModel(living, stack, slot, defaultModel);
				armorModel.headwearHide = true;
				armorModel.onface.showModel = living.getEntityData().getBoolean("byakugan_activated") || EntityEightTrigrams.EntityCustom.isActivated(living)
						|| living.getRidingEntity() instanceof EntityHakkeshoKeiten.EntityCustom;
				armorModel.highlightHide = !armorModel.onface.showModel;
				return armorModel;
			}

			@Override
			public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
				return isRinnesharinganActivated(stack)
						? "narutomod:textures/byakurinnesharingan_helmet.png" : "narutomod:textures/byakuganhelmet.png";
			}

			@Override
			public void onArmorTick(World world, EntityPlayer entity, ItemStack itemstack) {
				super.onArmorTick(world, entity, itemstack);
				this.isOwner(itemstack, entity);
				int x = (int) entity.posX;
				int y = (int) entity.posY;
				int z = (int) entity.posZ;
				if (!world.isRemote) {
					entity.addPotionEffect(new PotionEffect(MobEffects.SPEED, 2, 5, false, false));
				}
				HashMap<String, Object> $_dependencies = Maps.newHashMap();
				$_dependencies.put("entity", entity);
				$_dependencies.put("world", world);
				$_dependencies.put("itemstack", itemstack);
				ProcedureByakuganHelmetTickEvent.executeProcedure($_dependencies);
			}



			private boolean jutsuKey1Pressed;
			private boolean jutsuKey2Pressed;
			private boolean jutsuKey3Pressed;
			private int kaitenTime = 0;
			private boolean rotating = false;
			private int rotationCool = 0;
			private int airCool = 0;
			private int mountainCool = 0;
			private int palmsCool = 0;




			@Override
			public void onUpdate(ItemStack itemstack, World world, Entity entity, int par4, boolean par5) {
				super.onUpdate(itemstack, world, entity, par4, par5);
				if (!world.isRemote && entity instanceof EntityLivingBase) {
					NBTTagCompound nbt = new NBTTagCompound();
					entity.writeToNBT(nbt);
					--this.airCool;
					--this.mountainCool;
					--this.palmsCool;
					--this.rotationCool;
					boolean isOwner = ProcedureUtils.isOriginalOwner((EntityPlayer) entity, itemstack);

					ItemStack eye = ((entity instanceof EntityPlayer) ? ((EntityPlayer) entity).inventory.armorInventory.get(3) : ItemStack.EMPTY);

					Map<String, Object> $_dependencies = Maps.newHashMap();
					$_dependencies.put("entity", entity);
					$_dependencies.put("x", (int)entity.posX);
					$_dependencies.put("y", (int)entity.posY);
					$_dependencies.put("z", (int)entity.posZ);
					$_dependencies.put("world", entity.world);

					if (!hasSlot(nbt,2)) {
						boolean newPressed4 = entity.getEntityData().getBoolean(NarutomodModVariables.EYETOGGLE);
						$_dependencies.put("is_pressed", newPressed4);
						ProcedureByakuganActivate.executeProcedure($_dependencies);
					}
					boolean usingJutsu = ((EntityLivingBase)entity).isPotionActive(PotionUsingJutsu.potion);
					if (isOwner && !hasSlot(nbt,2)) {

						if (eye.getItem() == new ItemStack(ItemByakugan.helmet, (int) (1)).getItem()) {
							boolean newPressed = entity.getEntityData().getBoolean(NarutomodModVariables.JutsuKey1Pressed);
							if (!usingJutsu && this.jutsuKey1Pressed && !newPressed) {
								if (itemstack.hasTagCompound() && itemstack.getTagCompound().getBoolean(NarutomodModVariables.RINNESHARINGAN_ACTIVATED)) {
									$_dependencies.put("is_pressed", newPressed);
									ProcedureYomotsuHirasaka.executeProcedure($_dependencies);
								} else {
									if (entity.isSneaking()) {
										if (this.mountainCool <= 0 && Chakra.pathway((EntityLivingBase) entity).consume(400d)) {
											this.mountainCool = 20*7;
											entity.world.spawnEntity(new ItemByakugan.MountainCrusher((EntityLivingBase) entity));
										}
									} else {
										if (this.airCool <= 0 && Chakra.pathway((EntityLivingBase) entity).consume(200d)) {
											this.airCool = 20*3;
											entity.world.spawnEntity(new ItemByakugan.AirPalm((EntityLivingBase) entity));
										}
									}
								}
							}

							this.jutsuKey1Pressed = newPressed;

							boolean newPressed2 = entity.getEntityData().getBoolean(NarutomodModVariables.JutsuKey2Pressed);


							this.jutsuKey2Pressed = newPressed2;
							if (this.jutsuKey2Pressed) {
								if ( (!usingJutsu || this.rotating) &&
										this.rotationCool <= 0 && Chakra.pathway((EntityLivingBase) entity).consume(KAITEN_CHAKRA_USAGE)) {
									Entity entitySpawned = entity.getRidingEntity();
									if (!(entity.getRidingEntity() instanceof EntityHakkeshoKeiten.EntityCustom)) {
										this.rotating = true;
										world.playSound((EntityPlayer) null, (entity.posX), (entity.posY), (entity.posZ),
												(net.minecraft.util.SoundEvent) net.minecraft.util.SoundEvent.REGISTRY
														.getObject(new ResourceLocation("narutomod:HakkeshoKaiten")),
												SoundCategory.NEUTRAL, (float) 1, (float) 1);
										entity.world.spawnEntity(new EntityHakkeshoKeiten.EntityCustom((EntityPlayer) entity));
									}
									if (!entity.world.isRemote) {
										((EntityLivingBase)entity).addPotionEffect(new PotionEffect(PotionUsingJutsu.potion, 5, 1, false, false));
									}
									this.kaitenTime++;
									if (this.palmsCool <= 0) {
										this.palmsCool = 1;
									}
									if (this.kaitenTime > 5+20*5) {
										this.kaitenTime = 5+20*5;
									}
								}
							} else {
								if (this.kaitenTime > 0) {
									this.rotating = false;
									Entity entitySpawned = entity.getRidingEntity();
									if (entitySpawned instanceof EntityHakkeshoKeiten.EntityCustom) {
										entitySpawned.setDead();
									}
									this.rotationCool = Math.max(20,this.kaitenTime);
									this.kaitenTime = 0;
								}
							}

							boolean newPressed3 = entity.getEntityData().getBoolean(NarutomodModVariables.JutsuKey3Pressed);
							if (!usingJutsu && this.jutsuKey3Pressed && !newPressed3 && this.palmsCool <= 0) {
								RayTraceResult result = ProcedureUtils.objectEntityLookingAt(entity,7,5);
								if (!usingJutsu && result.entityHit instanceof EntityLivingBase
										&& Chakra.pathway((EntityLivingBase) entity).consume(ROKUJUYONSHO_CHAKRA_USAGE)) {
									this.palmsCool = 20*20;
									entity.world.spawnEntity(new SixtyFourPalms((EntityLivingBase) entity, (EntityLivingBase) result.entityHit));
								}
							}

							this.jutsuKey3Pressed = newPressed3;
						}
					}
				}

				if (!world.isRemote && entity instanceof EntityLivingBase && entity.ticksExisted % 20 == 0
						&& this.isOwner(itemstack, (EntityLivingBase)entity)
						&& itemstack.hasTagCompound() && itemstack.getTagCompound().hasKey(TENSEIGANEVOLVEDTIME)) {
					double d = itemstack.getTagCompound().getDouble(TENSEIGANEVOLVEDTIME) - 20d;
					itemstack.getTagCompound().setDouble(TENSEIGANEVOLVEDTIME, d);
					if (d <= 0.0d && entity instanceof EntityPlayerMP) {
						ItemStack oldstack = itemstack.copy();
						ItemStack newstack = new ItemStack(ItemTenseigan.helmet);
						((ItemDojutsu.Base)newstack.getItem()).setOwner(newstack, (EntityLivingBase)entity);
						newstack.getTagCompound().setDouble("ByakuganCount", itemstack.getTagCompound().getDouble("ByakuganCount"));
						((EntityPlayer)entity).inventory.setInventorySlotContents(getSlotId((EntityPlayer)entity, itemstack), newstack);
						oldstack.getTagCompound().removeTag("ByakuganCount");
						oldstack.getTagCompound().removeTag(TENSEIGANEVOLVEDTIME);
						ItemHandlerHelper.giveItemToPlayer((EntityPlayer)entity, oldstack);
						ProcedureUtils.grantAdvancement((EntityPlayerMP)entity, "narutomod:tenseigan_achieved", true);
					}
				}
			}

			@Override
			public void setOwner(ItemStack stack, EntityLivingBase entityIn) {
				super.setOwner(stack, entityIn);
				stack.getTagCompound().setDouble("ByakuganCount", 1.0d);
			}

			@Override
			public int getMaxDamage() {
				return 0;
			}

			@Override
			public boolean isDamageable() {
				return false;
			}

			@Override
			public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack) {
				Multimap<String, AttributeModifier> multimap = super.getAttributeModifiers(slot, stack);
				/*if (slot == EntityEquipmentSlot.HEAD && isRinnesharinganActivated(stack)) {
					multimap.put(SharedMonsterAttributes.MAX_HEALTH.getName(),
					 new AttributeModifier(RINNESHARINGAN_MODIFIER, "byakurinnesharingan.maxhealth", 380d, 0));
				}*/
				return multimap;
			}

			@SideOnly(Side.CLIENT)
			@Override
			public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
				super.addInformation(stack, worldIn, tooltip, flagIn);
				if (isRinnesharinganActivated(stack))
				{
					tooltip.add(TextFormatting.RED + I18n.translateToLocal("advancements.rinnesharinganactivated.title") + TextFormatting.WHITE);
					tooltip.add(TextFormatting.ITALIC + I18n.translateToLocal("key.mcreator.specialjutsu1") + ": " + TextFormatting.GRAY + I18n.translateToLocal("tooltip.byakugan.jutsu1") + " (NXP:500)");
					tooltip.add(TextFormatting.ITALIC + I18n.translateToLocal("key.mcreator.specialjutsu2") + ": " + TextFormatting.GRAY + I18n.translateToLocal("tooltip.byakurinnesharingan.jutsu2"));
					tooltip.add(TextFormatting.ITALIC + I18n.translateToLocal("key.mcreator.specialjutsu3") + ": " + TextFormatting.GRAY + I18n.translateToLocal("entity.hakkeshokeiten.name") + " (NXP:1500)");
				} else {
					tooltip.add(TextFormatting.ITALIC + I18n.translateToLocal("key.mcreator.specialjutsu1") + ": " + TextFormatting.GRAY + I18n.translateToLocal("tooltip.byakugan.jutsu1") + " (NXP:500)");
					if (Minecraft.getMinecraft().player != null && this.isOwner(stack, Minecraft.getMinecraft().player)) {
						tooltip.add(TextFormatting.ITALIC + I18n.translateToLocal("key.mcreator.specialjutsu2") + ": " + TextFormatting.GRAY + I18n.translateToLocal("entity.hakkeshokeiten.name") + " (NXP:1000)");
						tooltip.add(TextFormatting.ITALIC + I18n.translateToLocal("key.mcreator.specialjutsu3") + ": " + TextFormatting.GRAY + I18n.translateToLocal("tooltip.byakugan.jutsu2") + " (NXP:1500)");
					}
				}
				if (stack.hasTagCompound()) {
					double d = stack.getTagCompound().getDouble(TENSEIGANEVOLVEDTIME);
					if (d > 0.0d) {
						tooltip.add(I18n.translateToLocal(TextFormatting.AQUA + I18n.translateToLocal("tooltip.byakugan.tenseigantime")
								+ (long)(d / 20d) + TextFormatting.WHITE));
					}
				}
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
				Map<String, Object> $_dependencies = Maps.newHashMap();
				$_dependencies.put("is_pressed", is_pressed);
				$_dependencies.put("entity", entity);
				$_dependencies.put("world", entity.world);
				return true;
			}

			@Override
			public boolean onSwitchJutsuKey(boolean is_pressed, ItemStack stack, EntityPlayer entity) {
				if (entity.getEntityData().getBoolean("byakugan_activated")) {
					if (is_pressed) {
						entity.getEntityData().setDouble("byakugan_fov", entity.getEntityData().getDouble("byakugan_fov") - 1);
						OverlayByakuganView.sendCustomData(entity, true, (float) entity.getEntityData().getDouble("byakugan_fov"));
					}
					return true;
				}
				return false;
			}
		}.setUnlocalizedName("byakuganhelmet").setRegistryName("byakuganhelmet").setCreativeTab(TabModTab.tab));
	}

	private static int getSlotId(EntityPlayer entity, ItemStack stack) {
		for (int i = 0; i < entity.inventory.getSizeInventory(); i++) {
			ItemStack stack1 = entity.inventory.getStackInSlot(i);
			if (stack != null && stack.equals(stack1)) {
				return i;
			}
		}
		return -1;
	}

	public static boolean wearingAny(EntityLivingBase entity) {
		return entity.getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem() == helmet;
	}

	public static boolean isRinnesharinganActivated(ItemStack stack) {
		return stack.hasTagCompound() && stack.getTagCompound().getBoolean(RINNESHARINGAN_KEY);
	}

	public static boolean wearingRinnesharingan(EntityPlayer player) {
		ItemStack itemstack = player.inventory.armorInventory.get(3);
		return itemstack.getItem() == helmet && isRinnesharinganActivated(itemstack);
	}

	public static boolean hasRinnesharingan(EntityPlayer player) {
		ItemStack stack = ProcedureUtils.getItemStackIgnoreDurability(player.inventory, new ItemStack(helmet));
		return (stack != null && isRinnesharinganActivated(stack));
	}

	@SideOnly(Side.CLIENT)
	public void registerModels(ModelRegistryEvent event) {
		ModelLoader.setCustomModelResourceLocation(helmet, 0, new ModelResourceLocation("narutomod:byakuganhelmet", "inventory"));
	}
}
