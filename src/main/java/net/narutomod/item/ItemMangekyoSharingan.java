package net.narutomod.item;

import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLiving;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.event.ModelRegistryEvent;

import net.minecraft.world.World;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.Item;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.Entity;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.text.translation.I18n;

import net.narutomod.*;
import net.narutomod.block.BlockAmaterasuBlock;
import net.narutomod.creativetab.TabModTab;
import net.narutomod.entity.EntitySusanooBase;
import net.narutomod.entity.EntitySusanooSkeleton;
import net.narutomod.potion.PotionAmaterasuFlame;
import net.narutomod.potion.PotionChakraEnhancedStrength;
import net.narutomod.potion.PotionFeatherFalling;
import net.narutomod.potion.PotionUsingJutsu;
import net.narutomod.procedure.ProcedureAmaterasu;
import net.narutomod.procedure.ProcedureOnLivingUpdate;
import net.narutomod.procedure.ProcedureSusanoo;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.common.collect.Maps;
import net.narutomod.procedure.ProcedureUtils;

@ElementsNarutomodMod.ModElement.Tag
public class ItemMangekyoSharingan extends ElementsNarutomodMod.ModElement {
	@ObjectHolder("narutomod:mangekyosharinganhelmet")
	public static final Item helmet = null;
	public static final double AMATERASU_CHAKRA_USAGE = 25d;
	
	public ItemMangekyoSharingan(ElementsNarutomodMod instance) {
		super(instance, 69);
	}

	public static double getAmaterasuChakraUsage(EntityLivingBase entity) {
		ItemStack stack = entity.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
		return stack.getItem() == helmet || stack.getItem() == ItemMangekyoSharinganEternal.helmet
		 ? ((ItemDojutsu.Base)helmet).isOwner(stack, entity) ? AMATERASU_CHAKRA_USAGE 
		 : AMATERASU_CHAKRA_USAGE * 3 : (Double.MAX_VALUE * 0.001d);
	}


	public static void doDamage(ItemStack itemstack, EntityLivingBase entity, int damage) {
		((ItemSharingan.Base)itemstack.getItem()).canDamage = true;
		itemstack.damageItem(damage, entity);
		((ItemSharingan.Base)itemstack.getItem()).canDamage = false;
	}


	public static void tickCooldowns(ItemStack itemStack) {
		if (itemStack.getTagCompound() == null) {
			itemStack.setTagCompound(new NBTTagCompound());
		}
		NBTTagCompound values = itemStack.getTagCompound();

		values.setInteger("amaCool", values.getInteger("amaCool")-1);
		values.setInteger("susanCD", values.getInteger("susanCD")-1);
		values.setInteger("kamuiCD", values.getInteger("kamuiCD")-1);
		values.setInteger("wormholeCD", values.getInteger("wormholeCD")-1);
		values.setInteger("teleportCD", values.getInteger("teleportCD")-1);
		values.setInteger("kagutsuchiCD", values.getInteger("kagutsuchiCD")-1);
		values.setInteger("honoikazuchiCD", values.getInteger("honoikazuchiCD")-1);
		values.setInteger("hienCD", values.getInteger("hienCD")-1);
		values.setInteger("godsmajestyCD", values.getInteger("godsmajestyCD")-1);
	}

	public static void handleSusanoo(ItemStack itemstack, EntityPlayer player, World world) {
		if (!player.getEntityData().getBoolean("susanoo_activated")) {
			NBTTagCompound values = itemstack.getTagCompound();
			if (values.getInteger("susanCD") <= 0) {
				if (PlayerTracker.getBattleXp(player) >= EntitySusanooBase.BXP_REQUIRED_L0
						&& Chakra.pathway(player).consume(ProcedureSusanoo.BASE_CHAKRA_USAGE)) {
					player.getEntityData().setBoolean("susanoo_activated", true);
					EntitySusanooBase entityCustom = new EntitySusanooSkeleton.EntityCustom(player);
					world.spawnEntity(entityCustom);
					player.getEntityData().setInteger(ProcedureSusanoo.SUMMONED_SUSANOO, entityCustom.getEntityId());
				}
			} else {
				player.sendStatusMessage(
					new TextComponentString(
						"cooldown: " + String.format("%.2f", values.getInteger("susanCD") / 20.0D)
					),
					true
				);
			}
		} else {
			Entity entitySpawned = world.getEntityByID(ProcedureSusanoo.getSummonedSusanooId(player));
			ProcedureSusanoo.handleSusanCD(player, entitySpawned);
			if (entitySpawned != null) {
				entitySpawned.setDead();
				boolean flag = (player.isCreative() || ProcedureUtils.hasItemInInventory(player, ItemRinnegan.helmet));
				ItemStack helmet = player.inventory.armorInventory.get(3);
				if (!flag && (!(helmet.getItem() instanceof ItemSharingan.Base) || !((ItemSharingan.Base)helmet.getItem()).isEternal())) {
					player.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, (int)8, 3));
					player.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, (int)8, 2));
				}
				player.addPotionEffect(new PotionEffect(PotionFeatherFalling.potion, 60, 5));
			}
		}
	}

	public static void extinguishAmaterasu(World world, int x, int y, int z) {
		for (int x_area = 15; x_area > -15; --x_area) {
			for (int y_area = 15; y_area > -15; --y_area) {
				for (int z_area = 15; z_area > -15; --z_area) {
					BlockPos pos = new BlockPos(x + x_area, y + y_area, z + z_area);
					if (world.getBlockState(pos).getMaterial() == Material.FIRE
							|| world.getBlockState(pos).getMaterial() == BlockAmaterasuBlock.AMATERASU) {
						world.setBlockToAir(pos);
					}
				}
			}
		}
		if (!world.isRemote) {
			List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(null,
					(new AxisAlignedBB((double) x, (double) y, (double) z, (double) x, (double) y, (double) z)).grow(15));
			for (int i = 0; i < list.size(); ++i) {
				Entity entity = list.get(i);
				if (entity instanceof EntityLivingBase) {
					((EntityLivingBase) entity).removePotionEffect(PotionAmaterasuFlame.potion);
					entity.extinguish();
				}
			}
		}
	}


	public static void handleAmaterasu(EntityLivingBase user) {
		EntityLivingBase target;
		RayTraceResult t = ProcedureUtils.objectEntityLookingAt(user, 30d);
		double strength = (double) 2+ItemJutsu.getDmgMult(user)*1.5;
		user.addPotionEffect(new PotionEffect(PotionUsingJutsu.potion, 5, 1, false, false));
		if (t.typeOfHit == RayTraceResult.Type.ENTITY) {
			if (t.entityHit instanceof EntityLivingBase) {
				((EntityLivingBase) t.entityHit).setRevengeTarget(user);
				target = (EntityLivingBase) t.entityHit;
				int duration = 10;
				if (target.isPotionActive(PotionAmaterasuFlame.potion)) {
					duration = target.getActivePotionEffect(PotionAmaterasuFlame.potion).getDuration()+10;
				}
				target.addPotionEffect(new PotionEffect(PotionAmaterasuFlame.potion, duration, (int) (strength), (false), (false)));
			}
		} else {
			int x = (int) t.getBlockPos().getX() + t.sideHit.getDirectionVec().getX();
			int y = (int) t.getBlockPos().getY() + t.sideHit.getDirectionVec().getY();
			int z = (int) t.getBlockPos().getZ() + t.sideHit.getDirectionVec().getZ();
			BlockAmaterasuBlock.placeBlock(user.world, new BlockPos(x, y, z), (int) strength);
		}
	}

	public static void registerJutsu1(EntityLivingBase entity,World world, ItemStack itemstack, NBTTagCompound values, boolean usingJutsu) {
		boolean newPressed = entity.getEntityData().getBoolean(NarutomodModVariables.JutsuKey1Pressed);
		double chakraUsage = ItemMangekyoSharingan.getAmaterasuChakraUsage((EntityLivingBase) entity);
		values.setBoolean("jutsuKey1",newPressed);
		if (values.getBoolean("jutsuKey1")) {
			if (entity.isSneaking()) {
				extinguishAmaterasu(world, (entity.world.rayTraceBlocks(entity.getPositionEyes(1f), entity.getPositionEyes(1f).addVector(
								entity.getLook(1f).x * 50, entity.getLook(1f).y * 50, entity.getLook(1f).z * 50), false, false, true)
						.getBlockPos().getX()), (entity.world.rayTraceBlocks(entity.getPositionEyes(1f), entity.getPositionEyes(1f).addVector(
								entity.getLook(1f).x * 50, entity.getLook(1f).y * 50, entity.getLook(1f).z * 50), false, false, true)
						.getBlockPos().getY()), (entity.world.rayTraceBlocks(entity.getPositionEyes(1f), entity.getPositionEyes(1f).addVector(
								entity.getLook(1f).x * 50, entity.getLook(1f).y * 50, entity.getLook(1f).z * 50), false, false, true)
						.getBlockPos().getZ()));
			} else if (values.getInteger("amaTime") < 20*5) {
				if ((!usingJutsu || values.getBoolean("amaterasu"))) {
					if (values.getInteger("amaCool") <= 0 && Chakra.pathway((EntityLivingBase) entity).consume(chakraUsage * 0.25d)) {
						values.setInteger("amaTime", values.getInteger("amaTime") + 1);
						if (values.getBoolean("amaterasu") == false) {
							values.setBoolean("amaterasu", true);
							entity.getEntityData().setBoolean("amaterasu_active",true);
							world.playSound((EntityPlayer) null, entity.posX, entity.posY, entity.posZ, (net.minecraft.util.SoundEvent) net.minecraft.util.SoundEvent.REGISTRY
									.getObject(new ResourceLocation("narutomod:amaterasu2")), SoundCategory.NEUTRAL, (float) 1, (float) 1);
						}
						handleAmaterasu(entity);
					} else {
						((EntityPlayer) entity).sendStatusMessage(new TextComponentTranslation("chattext.cooldown.formatted",
								values.getInteger("amaCool") / 20), true);
					}
				}
			}

		} else {
			if (values.getInteger("amaTime") > 0) {
				values.setBoolean("amaterasu", false);
				values.setInteger("amaCool", 20 * 5 + values.getInteger("amaTime") / 2);
				values.setInteger("amaTime", 0);
				entity.getEntityData().setBoolean("amaterasu_active",false);
			}
		}
	}

	public static void registerJutsu2(EntityLivingBase entity,World world, ItemStack itemstack, NBTTagCompound values, boolean usingJutsu) {
		boolean newPressed2 = entity.getEntityData().getBoolean(NarutomodModVariables.JutsuKey2Pressed);
		if (!usingJutsu && values.getBoolean("jutsuKey2") && !newPressed2) {
			if (entity instanceof EntityPlayer) {
				handleSusanoo(itemstack, (EntityPlayer) entity, world);
			}
		}
		values.setBoolean("jutsuKey2",newPressed2);
	}

	public static void registerJutsu3(EntityLivingBase entity,World world, ItemStack itemstack, NBTTagCompound values, boolean usingJutsu) {
		boolean newPressed = entity.getEntityData().getBoolean(NarutomodModVariables.JutsuKey3Pressed);
		if (values.getBoolean("jutsuKey3") && !usingJutsu && !newPressed) {
			if (entity.isSneaking()) {
				if (values.getInteger("honoikazuchiCD") <= 0 && Chakra.pathway(entity).consume(400d)) {
					values.setInteger("honoikazuchiCD", 20 * 12);
					ItemMangekyoSharingan.doDamage(itemstack,entity,3);
					entity.world.spawnEntity(new HonoIkazuchi(entity));
				}
			} else {
				if (values.getInteger("hienCD") <= 0 && Chakra.pathway(entity).consume(300d)) {
					values.setInteger("hienCD", 20 * 10);
					ItemMangekyoSharingan.doDamage(itemstack,entity,3);
					entity.world.spawnEntity(new HiEn(entity));
				}
			}
		}
		values.setBoolean("jutsuKey3",newPressed);
	}

	public static class HonoIkazuchi extends Entity  {
		private EntityLivingBase user;
		private Vec3d look;
		private Vec3d start;
		List<String> targets = new ArrayList<String>();
		List<Vec3d> points = new ArrayList<>();

		public HonoIkazuchi(World worldIn) {
			super(worldIn);
			this.setSize(0.01f, 0.01f);
			this.isImmuneToFire = true;
		}


		public HonoIkazuchi(EntityLivingBase user) {
			this(user.world);
			this.user = user;
			this.look = this.user.getLookVec();
			this.start = user.getPositionVector();
			this.setPosition(this.start.x, this.start.y, this.start.z);
			this.user.world.playSound(null, this.user.posX, this.user.posY, this.user.posZ,
					(net.minecraft.util.SoundEvent) net.minecraft.util.SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:honoikazuchi")),
					SoundCategory.PLAYERS, 1.0F, 1.0F);
			this.user.world.playSound(null, this.user.posX, this.user.posY, this.user.posZ,
					(net.minecraft.util.SoundEvent) net.minecraft.util.SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:flamethrow")),
					SoundCategory.PLAYERS, 2.0F, 1.2F);
			for (int i = 0; i< 16; i++) {
				this.points.add(this.start.addVector(-1+this.rand.nextFloat()*2,-0.2+this.rand.nextFloat()*1.2,-1+this.rand.nextFloat()*2));
			}
			ProcedureOnLivingUpdate.setUntargetable(user, 10);
		}


		@Override
		protected void entityInit() {
		}

		@Override
		public void onUpdate() {
			if (this.user != null) {
				this.setPosition( this.start.x, this.start.y, this.start.z);
				int maxDistance = 6;
				for (Vec3d end : this.points) {
					Vec3d look = end.subtract(this.start).normalize();
					for (int i = 0; i< 4; i++) {
						Vec3d point = this.start.add(look.scale(i));
						Particles.spawnParticle(this.user.world, Particles.Types.FLAME, point.x, point.y, point.z,
								1, 0,0,0, look.x*0.1, look.y*0.1, look.z*0.1, 0xA0000000, 40-i*8);
					}

				}
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
					this.playSound(SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:bullet_impact")),
							1f, 0.6f+this.rand.nextFloat()*1.2f);
					float damage = 15+(10f*ItemJutsu.getDmgMult(this.user));
					entity1.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, this.user).setDamageBypassesArmor(),damage);

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

	public static class HiEn extends Entity  {
		private EntityLivingBase user;
		private Vec3d look;
		private Vec3d start;
		List<String> targets = new ArrayList<String>();
		List<Vec3d> points = new ArrayList<>();
		private EntityLivingBase target;
		private boolean hit = false;
		private int life = 40;

		public HiEn(World worldIn) {
			super(worldIn);
			this.setSize(0.01f, 0.01f);
			this.isImmuneToFire = true;
		}


		public HiEn(EntityLivingBase user) {
			this(user.world);
			this.user = user;
			this.look = this.user.getLookVec();
			this.start = user.getPositionEyes(1);
			this.setPosition(this.start.x, this.start.y, this.start.z);
			this.user.world.playSound(null, this.user.posX, this.user.posY, this.user.posZ,
					(net.minecraft.util.SoundEvent) net.minecraft.util.SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:dojutsu")),
					SoundCategory.PLAYERS, 1.0F, 1.0F);
			this.user.world.playSound(null, this.user.posX, this.user.posY, this.user.posZ,
					(net.minecraft.util.SoundEvent) net.minecraft.util.SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:flamethrow")),
					SoundCategory.PLAYERS, 2.0F, 1.2F);
		}


		@Override
		protected void entityInit() {
		}

		@Override
		public void onUpdate() {
			if (this.user != null) {
				this.setPosition( this.start.x, this.start.y, this.start.z);
				int maxDistance = 6;
				if (!this.hit) {
					Vec3d point = this.start.add(this.look.scale(this.ticksExisted*2.5));
					Particles.spawnParticle(this.user.world, Particles.Types.FLAME, point.x, point.y, point.z,
							2, 0,0,0, look.x*0.2, look.y*0.2, look.z*0.2, 0xA0000000, 20);
					AxisAlignedBB hitbox = new AxisAlignedBB(new BlockPos(point)).grow(3);
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
						if (found || this.hit) {
							continue;
						}
						this.hit = true;
						this.targets.add(entity1.getUniqueID().toString());
						this.playSound(SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:bullet_impact")),
								1f, 0.6f+this.rand.nextFloat()*1.2f);
						float damage = 15+(10f*ItemJutsu.getDmgMult(this.user));
						entity1.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, this.user).setDamageBypassesArmor(),damage);
						this.target = (EntityLivingBase) entity1;
						this.start = entity1.getPositionVector();
						//below changes spike lifetime
						this.life = this.ticksExisted+25;
					}

				} else {
					this.look = new Vec3d(0,1,0);
					this.target.setPositionAndUpdate(this.start.x,this.start.y,this.start.z);
					for (int i = 0; i< 8; i++) {
						Vec3d point = this.start.add(this.look.scale(i));
						Particles.spawnParticle(this.user.world, Particles.Types.FLAME, point.x, point.y, point.z,
								1, 0,0,0, look.x*0.1, look.y*0.1, look.z*0.1, 0xA0000000, 60-7*i);
					}
				}

			}
			if (this.ticksExisted > this.life) {
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

	public void initElements() {
		ItemArmor.ArmorMaterial enuma = EnumHelper.addArmorMaterial("MANGEKYOSHARINGAN", "narutomod:mangekyosharingan_sasuke_", 2048,
				new int[]{2, 5, 6, 10}, 0, null, 2.0F);
		this.elements.items.add(() -> new ItemSharingan.Base(enuma) {
			@Override
			public void onArmorTick(World world, EntityPlayer entity, ItemStack itemstack) {
				super.onArmorTick(world, entity, itemstack);
				if (!world.isRemote) {
					entity.addPotionEffect(new PotionEffect(MobEffects.SPEED, 2, 6, false, false));
				}
			}


			@Override
			public void onUpdate(ItemStack itemstack, World world, Entity entity, int par4, boolean par5) {
				super.onUpdate(itemstack, world, entity, par4, par5);
				if (!world.isRemote && entity instanceof EntityLivingBase) {

					NBTTagCompound values = itemstack.getTagCompound();
					tickCooldowns(itemstack);

					NBTTagCompound nbt = new NBTTagCompound();
					entity.writeToNBT(nbt);

					ItemStack eye = ((entity instanceof EntityPlayer) ? ((EntityPlayer) entity).inventory.armorInventory.get(3) : ItemStack.EMPTY);
					boolean usingJutsu = ((EntityLivingBase)entity).isPotionActive(PotionUsingJutsu.potion);
					if (!ItemByakugan.hasSlot(nbt,2) && (eye.getTagCompound() != null && !eye.getTagCompound().getBoolean("sharingan_blinded"))) {
						if (eye.getItem() == new ItemStack(ItemMangekyoSharingan.helmet, (int) (1)).getItem() && entity instanceof EntityPlayer) {

							registerJutsu1((EntityPlayer) entity,world, itemstack, values,usingJutsu);

							registerJutsu2((EntityPlayer) entity,world, itemstack, values,usingJutsu);

							registerJutsu3((EntityPlayer) entity,world, itemstack, values,usingJutsu);

						}
					}
				}
			}

			@Override
			public ItemSharingan.Type getSubType() {
				return ItemSharingan.Type.AMATERASU;
			}

			@Override
			public boolean isMangekyo() {
				return true;
			}

			@Override
			public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
				return "narutomod:textures/mangekyosharinganhelmet_sasuke.png";
			}

			@Override
			public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
				super.addInformation(stack, worldIn, tooltip, flagIn);
				tooltip.add(TextFormatting.ITALIC + I18n.translateToLocal("key.mcreator.specialjutsu1") + ": " + TextFormatting.GRAY + I18n.translateToLocal("tooltip.mangekyo.amaterasu.jutsu1"));
				tooltip.add(TextFormatting.ITALIC + I18n.translateToLocal("key.mcreator.specialjutsu2") + ": " + TextFormatting.GRAY + I18n.translateToLocal("entity.susanooclothed.name"));
			}

			@Override
			public String getItemStackDisplayName(ItemStack stack) {
				return TextFormatting.RED + super.getItemStackDisplayName(stack) + TextFormatting.WHITE;
			}

			/*@Override
			public boolean onJutsuKey1(boolean is_pressed, ItemStack stack, EntityPlayer entity) {
				Map<String, Object> $_dependencies = Maps.newHashMap();
				$_dependencies.put("is_pressed", is_pressed);
				$_dependencies.put("entity", entity);
				$_dependencies.put("world", entity.world);
				$_dependencies.put("x", (int)entity.posX);
				$_dependencies.put("y", (int)entity.posY);
				$_dependencies.put("z", (int)entity.posZ);
				ProcedureAmaterasu.executeProcedure($_dependencies);
				return true;
			}*/

			@Override
			public boolean onJutsuKey2(boolean is_pressed, ItemStack stack, EntityPlayer entity) {
				if (!is_pressed) {
					Map<String, Object> $_dependencies = Maps.newHashMap();
					$_dependencies.put("entity", entity);
					$_dependencies.put("world", entity.world);
					//ProcedureSusanoo.executeProcedure($_dependencies);
				}
				return true;
			}

			@Override
			public boolean onSwitchJutsuKey(boolean is_pressed, ItemStack stack, EntityPlayer entity) {
				if (entity.getRidingEntity() instanceof EntitySusanooBase) {
					if (!is_pressed) {
						ProcedureSusanoo.upgrade(entity);
					}
					return true;
				}
				return false;
			}
		}.setUnlocalizedName("mangekyosharinganhelmet").setRegistryName("mangekyosharinganhelmet").setCreativeTab(TabModTab.tab));
	}

	@SideOnly(Side.CLIENT)
	public void registerModels(ModelRegistryEvent event) {
		ModelLoader.setCustomModelResourceLocation(helmet, 0, new ModelResourceLocation("narutomod:mangekyosharinganhelmet", "inventory"));
	}
}
