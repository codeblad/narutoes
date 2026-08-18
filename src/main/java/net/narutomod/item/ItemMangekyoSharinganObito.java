package net.narutomod.item;

import net.minecraft.init.MobEffects;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
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
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.text.translation.I18n;

import net.narutomod.NarutomodModVariables;
import net.narutomod.Particles;
import net.narutomod.gui.overlay.OverlayByakuganView;
import net.narutomod.potion.PotionUsingJutsu;
import net.narutomod.procedure.*;
import net.narutomod.world.WorldKamuiDimension;
import net.narutomod.creativetab.TabModTab;
import net.narutomod.entity.EntitySusanooBase;
import net.narutomod.Chakra;
import net.narutomod.ElementsNarutomodMod;

import com.google.common.collect.Maps;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@ElementsNarutomodMod.ModElement.Tag
public class ItemMangekyoSharinganObito extends ElementsNarutomodMod.ModElement {
	@ObjectHolder("narutomod:mangekyosharinganobitohelmet")
	public static final Item helmet = null;
	public static final double INTANGIBLE_CHAKRA_USAGE = 20d; // per tick
	public static final double TELEPORT_CHAKRA_USAGE = 20d; // per tick

	public ItemMangekyoSharinganObito(ElementsNarutomodMod instance) {
		super(instance, 118);
	}

	public static double getIntangibleChakraUsage(EntityLivingBase entity) {
		ItemStack stack = entity.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
		return stack.getItem() == helmet || stack.getItem() == ItemMangekyoSharinganEternal.helmet
				? ((ItemDojutsu.Base)helmet).isOwner(stack, entity) ? INTANGIBLE_CHAKRA_USAGE
				: INTANGIBLE_CHAKRA_USAGE * 3 : (Double.MAX_VALUE * 0.001d);
	}

	public static double getTeleportChakraUsage(EntityLivingBase entity) {
		ItemStack stack = entity.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
		return stack.getItem() == helmet || stack.getItem() == ItemMangekyoSharinganEternal.helmet
				? ((ItemDojutsu.Base)helmet).isOwner(stack, entity) ? TELEPORT_CHAKRA_USAGE
				: TELEPORT_CHAKRA_USAGE * 3 : (Double.MAX_VALUE * 0.001d);
	}

	public static void handleTeleport(EntityPlayer player, ItemStack itemstack, World world, boolean active) {
		NBTTagCompound values = itemstack.getTagCompound();
		RayTraceResult result = ProcedureUtils.objectEntityLookingAt(player, 50d, true);
		Entity target = result.entityHit;
		double maxSize = 6;
		player.getEntityData().setBoolean("kamui_teleport",active);
		if (active) {
			values.setInteger("kamuiTime",values.getInteger("kamuiTime")+1);
			Particles.spawnParticle(player.world, Particles.Types.PORTAL_SPIRAL, result.hitVec.x, result.hitVec.y, result.hitVec.z, 150, 0d, 0d, 0d, 0d, 0d, 0d,
					3, 0x32000000, 25);
			int kTim = values.getInteger("kamuiTime");
			player.addPotionEffect(new PotionEffect(PotionUsingJutsu.potion, 5, 1, false, false));
			//float fov = (float) (70 - (Math.log((player.getDistance(result.hitVec.x,result.hitVec.y,result.hitVec.z))) * 15));
			//OverlayByakuganView.sendCustomData(player, false, fov);
			if (kTim == 1) {
				world.playSound((EntityPlayer) null, player.posX, player.posY, player.posZ, (net.minecraft.util.SoundEvent) net.minecraft.util.SoundEvent.REGISTRY
						.getObject(new ResourceLocation("narutomod:dojutsu")), SoundCategory.NEUTRAL, (float) 1, (float) 1);
			}
			if (kTim%60 == 0 || kTim == 1) {
				world.playSound((EntityPlayer) null, result.hitVec.x, result.hitVec.y, result.hitVec.z, (net.minecraft.util.SoundEvent) net.minecraft.util.SoundEvent.REGISTRY
						.getObject(new ResourceLocation("narutomod:new_kamui")), SoundCategory.NEUTRAL, (float) 1, (float) 1);
			}


			Chakra.pathway(player).consume(getTeleportChakraUsage(player));
		} else {
			//OverlayByakuganView.sendCustomData(player, false, 70);
			if (target == null && player.getDistance(result.hitVec.x,result.hitVec.y,result.hitVec.z) < 1) {
				target = player;
			}
			if (target != null) {
				double size =  target.getEntityBoundingBox().getAverageEdgeLength();
				if (size < maxSize && values.getInteger("kamuiTime") >= size*8+1) {
					int dimid = (player.dimension != WorldKamuiDimension.DIMID) ? WorldKamuiDimension.DIMID : 0;
					ProcedureKamuiTeleportEntity.eEntity(target, (int) result.hitVec.x, (int) result.hitVec.z, dimid);
				}
			}
			values.setInteger("kamuiTime",0);
			values.setInteger("teleportCD",50);
		}
	}

	public static void handleKamui(EntityPlayer player, ItemStack itemstack, boolean active) {
		NBTTagCompound values = itemstack.getTagCompound();

		if (!active || values.getInteger("kamuiTime") == 0) {
			player.capabilities.allowEdit = !active;
			player.sendPlayerAbilities();
			player.capabilities.isFlying = active;
			player.sendPlayerAbilities();
			ProcedureOnLivingUpdate.setNoClip(player, active);
		}
		player.sendStatusMessage(new TextComponentString(
				((net.minecraft.util.text.translation.I18n.translateToLocal("chattext.intangible")) + "" + ((active)))), (true));
		player.getEntityData().setBoolean("kamui_intangible",active);
		if (active) {
			player.addPotionEffect(new PotionEffect(PotionUsingJutsu.potion, 5, 1, false, false));
			values.setInteger("kamuiTime",values.getInteger("kamuiTime")+1);
			ProcedureUtils.purgeHarmfulEffects(player);
			ProcedureOnLivingUpdate.setUntargetable(player, 3);
			player.fallDistance = (float) (0);
			Chakra.pathway(player).consume(getIntangibleChakraUsage(player));
			player.getEntityData().setDouble(NarutomodModVariables.InvulnerableTime, 2.0d);
		} else {
			if (values.getInteger("kamuiTime") > 0) {
				values.setBoolean("intangible", false);
				values.setInteger("kamuiCD", values.getInteger("kamuiTime")*2);
				values.setInteger("kamuiTime", 0);
			}
		}
	}

	public static void registerJutsu1(EntityLivingBase entity,World world, ItemStack itemstack, NBTTagCompound values, boolean usingJutsu) {
		boolean newPressed = entity.getEntityData().getBoolean(NarutomodModVariables.JutsuKey1Pressed);
		double chakraUsage = ItemMangekyoSharinganObito.getIntangibleChakraUsage(entity);
		values.setBoolean("jutsuKey1",newPressed);
		if (values.getBoolean("jutsuKey1")) {
			if (!usingJutsu || values.getInteger("kamuiTime") > 0) {

				if (values.getInteger("kamuiTime") <= 0) {
					if (!entity.isSneaking() && world.provider.getDimension() != (WorldKamuiDimension.DIMID)) {
						values.setBoolean("phase", true);
						values.setBoolean("teleport",false);
					} else {
						values.setBoolean("phase", false);
						values.setBoolean("teleport", true);
					}
				}

				if (values.getBoolean("phase") && entity instanceof  EntityPlayer) {
					boolean active = values.getInteger("kamuiTime") < 5*20
							&& Chakra.pathway((EntityLivingBase) entity).getAmount() >= chakraUsage && values.getInteger("kamuiCD") <= 0;
					handleKamui((EntityPlayer) entity, itemstack, active);
					if (!active) {
						((EntityPlayer) entity).sendStatusMessage(new TextComponentTranslation("chattext.cooldown.formatted",
								values.getInteger("kamuiCD") / 20), true);
					}
				}
				if (values.getBoolean("teleport")) {
					double chakraUsage2 = ItemMangekyoSharinganObito.getTeleportChakraUsage((EntityLivingBase) entity);
					boolean active = values.getInteger("kamuiTime") < 10*20
							&& Chakra.pathway((EntityLivingBase) entity).getAmount() >= chakraUsage2 && values.getInteger("teleportCD") <= 0;
					if (Chakra.pathway((EntityLivingBase) entity).getAmount() >= chakraUsage2 && values.getInteger("teleportCD") <= 0) {
						handleTeleport((EntityPlayer) entity, itemstack,world, active);
					}
					if (!active) {
						((EntityPlayer) entity).sendStatusMessage(new TextComponentTranslation("chattext.cooldown.formatted",
								values.getInteger("teleportCD") / 20), true);
					}
				}
			}

		} else {
			if (entity instanceof  EntityPlayer && values.getInteger("kamuiTime") > 0) {

				if (values.getBoolean("phase")) {
					handleKamui((EntityPlayer) entity, itemstack, false);
				} else {
					handleTeleport((EntityPlayer) entity, itemstack,world, false);
				}
			}
		}
	}

	public static void registerJutsu3(EntityLivingBase entity,World world, ItemStack itemstack, NBTTagCompound values, boolean usingJutsu) {
		boolean newPressed = entity.getEntityData().getBoolean(NarutomodModVariables.JutsuKey3Pressed);
		if (values.getBoolean("jutsuKey3") && !usingJutsu && !newPressed) {
			if (values.getInteger("godsmajestyCD") <= 0 && Chakra.pathway(entity).consume(300d)) {
				values.setInteger("godsmajestyCD", 20 * 11);
				ItemMangekyoSharingan.doDamage(itemstack,entity,5);
				entity.world.spawnEntity(new GodsMajesty(entity));
			}
		}
		values.setBoolean("jutsuKey3",newPressed);
	}

	public static class GodsMajesty extends Entity  {
		private EntityLivingBase user;
		private Vec3d look;
		private Vec3d start;
		List<String> targets = new ArrayList<String>();
		List<EntityLivingBase> trapped = new ArrayList<>();

		public GodsMajesty(World worldIn) {
			super(worldIn);
			this.setSize(0.01f, 0.01f);
			this.isImmuneToFire = true;
		}


		public GodsMajesty(EntityLivingBase user) {
			this(user.world);
			this.user = user;
			this.look = this.user.getLookVec();
			RayTraceResult result = ProcedureUtils.objectEntityLookingAt(user, 50d, true);
			this.start = result.hitVec;
			this.setPosition(this.start.x, this.start.y, this.start.z);
			this.user.world.playSound(null, this.user.posX, this.user.posY, this.user.posZ,
					(net.minecraft.util.SoundEvent) net.minecraft.util.SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:dojutsu")),
					SoundCategory.PLAYERS, 1.0F, 1.0F);
			this.user.world.playSound(null, this.posX, this.posY, this.posZ,
					(net.minecraft.util.SoundEvent) net.minecraft.util.SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:new_kamui")),
					SoundCategory.PLAYERS, 1.0F, 1.0F);

		}


		@Override
		protected void entityInit() {
		}

		@Override
		public void onUpdate() {
			if (this.user != null) {
				this.setPosition( this.start.x, this.start.y, this.start.z);
				Particles.spawnParticle(user.world, Particles.Types.PORTAL_SPIRAL, this.start.x, this.start.y, this.start.z, 150, 0d, 0d, 0d, 0d, 0d, 0d,
						5, 0x32000000, 50);
				if (this.ticksExisted%60==0) {
					this.user.world.playSound(null, this.posX, this.posY, this.posZ,
							(net.minecraft.util.SoundEvent) net.minecraft.util.SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:new_kamui")),
							SoundCategory.PLAYERS, 1.0F, 1.0F);
				}
				int maxDistance = 8;
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
					float damage = ((EntityLivingBase) entity1).getMaxHealth()*0.3f;
					float distance = this.getDistance(entity1);
					float mult = Math.max(0.1f,1-distance/maxDistance);
					damage *= mult;
					entity1.attackEntityFrom(DamageSource.OUT_OF_WORLD.setDamageIsAbsolute(),damage);

				}
				for (EntityLivingBase entity : this.trapped) {
					entity.setPositionAndUpdate(this.posX,this.posY,this.posZ);
				}
			}
			if (this.ticksExisted > 10) {
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
		ItemArmor.ArmorMaterial enuma = EnumHelper.addArmorMaterial("MANGEKYOSHARINGANOBITO", "narutomod:mangekyosharingan_obito_", 1024,
				new int[]{2, 5, 6, 10}, 0, null, 2.0F);
		this.elements.items.add(() -> new ItemSharingan.Base(enuma) {


			@Override
			public void onArmorTick(World world, EntityPlayer entity, ItemStack itemstack) {
				super.onArmorTick(world, entity, itemstack);
				if (!world.isRemote) {
					entity.addPotionEffect(new PotionEffect(MobEffects.SPEED, 2, 5, false, false));
					boolean flag = entity.isCreative() || entity.dimension == WorldKamuiDimension.DIMID;
					if (entity.capabilities.allowFlying != flag) {
						entity.capabilities.allowFlying = flag;
						entity.sendPlayerAbilities();
					}
					/*if (entity.getEntityData().getBoolean("kamui_teleport")) {
						Chakra.pathway(entity).consume(getTeleportChakraUsage(entity));
					}
					if (entity.getEntityData().getBoolean("kamui_intangible")) {
						Chakra.pathway(entity).consume(getIntangibleChakraUsage(entity));
						ProcedureWhenPlayerAttcked.setInvulnerable(entity, 2);
						//entity.getEntityData().setDouble(NarutomodModVariables.InvulnerableTime, 2.0d);
					}*/
				}
			}

			@Override
			public void onUpdate(ItemStack itemstack, World world, Entity entity, int par4, boolean par5) {
				super.onUpdate(itemstack, world, entity, par4, par5);
				if (!world.isRemote && entity instanceof EntityLivingBase) {

					NBTTagCompound values = itemstack.getTagCompound();
					ItemMangekyoSharingan.tickCooldowns(itemstack);

					NBTTagCompound nbt = new NBTTagCompound();
					entity.writeToNBT(nbt);



					ItemStack eye = ((entity instanceof EntityPlayer) ? ((EntityPlayer) entity).inventory.armorInventory.get(3) : ItemStack.EMPTY);
					boolean usingJutsu = ((EntityLivingBase)entity).isPotionActive(PotionUsingJutsu.potion);
					if (!ItemByakugan.hasSlot(nbt,2) && (eye.getTagCompound() != null && !eye.getTagCompound().getBoolean("sharingan_blinded"))) {
						if (eye.getItem() == new ItemStack(ItemMangekyoSharinganObito.helmet, (int) (1)).getItem()  && entity instanceof EntityPlayer) {


							registerJutsu1((EntityPlayer) entity,world, itemstack, values,usingJutsu);

							ItemMangekyoSharingan.registerJutsu2((EntityPlayer) entity,world, itemstack, values,usingJutsu);

							registerJutsu3((EntityPlayer) entity,world, itemstack, values,usingJutsu);

						}
					}
				}
			}

			@Override
			public ItemSharingan.Type getSubType() {
				return ItemSharingan.Type.KAMUI;
			}

			@Override
			public boolean isMangekyo() {
				return true;
			}

			@Override
			public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
				return "narutomod:textures/mangekyosharinganhelmet_obito.png";
			}

			@Override
			public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
				super.addInformation(stack, worldIn, tooltip, flagIn);
				tooltip.add(TextFormatting.ITALIC + I18n.translateToLocal("key.mcreator.specialjutsu1") + ": " + TextFormatting.GRAY + I18n.translateToLocal("tooltip.mangekyo.kamui.jutsu1"));
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
				if (entity.world.provider.getDimension() == WorldKamuiDimension.DIMID && !entity.isSneaking()) {
					ProcedureGrabEntity.executeProcedure($_dependencies);
				} else {
					$_dependencies.put("x", (int)entity.posX);
					$_dependencies.put("y", (int)entity.posY);
					$_dependencies.put("z", (int)entity.posZ);
					ProcedureKamuiJikukanIdo.executeProcedure($_dependencies);
				}
				return true;
			}

			@Override
			public boolean onJutsuKey2(boolean is_pressed, ItemStack stack, EntityPlayer entity) {
				if (!is_pressed) {
					Map<String, Object> $_dependencies = Maps.newHashMap();
					$_dependencies.put("entity", entity);
					$_dependencies.put("world", entity.world);
					ProcedureSusanoo.executeProcedure($_dependencies);
				}
				return true;
			}*/

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
		}.setUnlocalizedName("mangekyosharinganobitohelmet").setRegistryName("mangekyosharinganobitohelmet").setCreativeTab(TabModTab.tab));
	}

	@SideOnly(Side.CLIENT)
	public void registerModels(ModelRegistryEvent event) {
		ModelLoader.setCustomModelResourceLocation(helmet, 0, new ModelResourceLocation("narutomod:mangekyosharinganobitohelmet", "inventory"));
	}
}