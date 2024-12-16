package net.narutomod.procedure;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.Vec3d;
import net.narutomod.Chakra;
import net.narutomod.entity.*;
import net.narutomod.item.*;
import net.narutomod.world.WorldKamuiDimension;
import net.narutomod.ElementsNarutomodMod;

import net.minecraft.world.World;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.Entity;

import java.util.Map;
import java.util.HashMap;

@ElementsNarutomodMod.ModElement.Tag
public class ProcedureSpecialJutsu1OnKeyPressed extends ElementsNarutomodMod.ModElement {
	public ProcedureSpecialJutsu1OnKeyPressed(ElementsNarutomodMod instance) {
		super(instance, 64);
	}

	public static void executeProcedure(Map<String, Object> dependencies) {
		if (dependencies.get("is_pressed") == null) {
			System.err.println("Failed to load dependency is_pressed for procedure SpecialJutsu1OnKeyPressed!");
			return;
		}
		if (dependencies.get("entity") == null) {
			System.err.println("Failed to load dependency entity for procedure SpecialJutsu1OnKeyPressed!");
			return;
		}
		if (dependencies.get("x") == null) {
			System.err.println("Failed to load dependency x for procedure SpecialJutsu1OnKeyPressed!");
			return;
		}
		if (dependencies.get("y") == null) {
			System.err.println("Failed to load dependency y for procedure SpecialJutsu1OnKeyPressed!");
			return;
		}
		if (dependencies.get("z") == null) {
			System.err.println("Failed to load dependency z for procedure SpecialJutsu1OnKeyPressed!");
			return;
		}
		if (dependencies.get("world") == null) {
			System.err.println("Failed to load dependency world for procedure SpecialJutsu1OnKeyPressed!");
			return;
		}
		boolean is_pressed = (boolean) dependencies.get("is_pressed");
		Entity entity = (Entity) dependencies.get("entity");
		int x = (int) dependencies.get("x");
		int y = (int) dependencies.get("y");
		int z = (int) dependencies.get("z");
		World world = (World) dependencies.get("world");
		ItemStack helmet = ItemStack.EMPTY;
		if (((world.isRemote) || ((EntityPlayer) entity).isSpectator())) {
			return;
		}
		helmet = ((entity instanceof EntityPlayer) ? ((EntityPlayer) entity).inventory.armorInventory.get(3) : ItemStack.EMPTY);
		if ((((helmet).getItem() == new ItemStack(ItemRinnegan.helmet, (int) (1)).getItem())
				|| ((helmet).getItem() == new ItemStack(ItemTenseigan.helmet, (int) (1)).getItem()))) {
			{
				Map<String, Object> $_dependencies = new HashMap<>();
				$_dependencies.put("is_pressed", is_pressed);
				$_dependencies.put("entity", entity);
				$_dependencies.put("world", world);
				$_dependencies.put("x", x);
				$_dependencies.put("y", y);
				$_dependencies.put("z", z);
				ProcedureShinraTenseiOnKeyPressed.executeProcedure($_dependencies);
			}
		} else if ((((helmet).getItem() == new ItemStack(ItemMangekyoSharingan.helmet, (int) (1)).getItem())
				|| ((helmet).getItem() == new ItemStack(ItemMangekyoSharinganEternal.helmet, (int) (1)).getItem()))) {
			{
				Map<String, Object> $_dependencies = new HashMap<>();
				$_dependencies.put("is_pressed", is_pressed);
				$_dependencies.put("entity", entity);
				$_dependencies.put("world", world);
				$_dependencies.put("x", x);
				$_dependencies.put("y", y);
				$_dependencies.put("z", z);
				ProcedureAmaterasu.executeProcedure($_dependencies);
			}
		} else if (((helmet).getItem() == new ItemStack(ItemMangekyoSharinganObito.helmet, (int) (1)).getItem())) {
			if ((((world.provider.getDimension()) == (WorldKamuiDimension.DIMID)) && (!(entity.isSneaking())))) {
				{
					Map<String, Object> $_dependencies = new HashMap<>();
					$_dependencies.put("is_pressed", is_pressed);
					$_dependencies.put("entity", entity);
					$_dependencies.put("world", world);
					ProcedureGrabEntity.executeProcedure($_dependencies);
				}
			} else {
				{
					Map<String, Object> $_dependencies = new HashMap<>();
					$_dependencies.put("is_pressed", is_pressed);
					$_dependencies.put("entity", entity);
					$_dependencies.put("world", world);
					$_dependencies.put("x", x);
					$_dependencies.put("y", y);
					$_dependencies.put("z", z);
					ProcedureKamuiJikukanIdo.executeProcedure($_dependencies);
				}
			}
		} else if (((helmet).getItem() == new ItemStack(ItemByakugan.helmet, (int) (1)).getItem())) {
			{
				Map<String, Object> $_dependencies = new HashMap<>();
				$_dependencies.put("is_pressed", is_pressed);
				$_dependencies.put("entity", entity);
				$_dependencies.put("x", x);
				$_dependencies.put("y", y);
				$_dependencies.put("z", z);
				$_dependencies.put("world", world);
				ProcedureByakuganActivate.executeProcedure($_dependencies);
			}
		} else if (EntityBijuManager.cloakLevel((EntityPlayer) entity) == 3) {
			EntityTailedBeast.Base biju = EntityBijuManager.getBijuOfPlayerInWorld((EntityPlayer) entity);
			if (((is_pressed))) {
				biju.setSwingingArms(true);
				int tails = EntityBijuManager.getTails((EntityPlayer) entity);
				Chakra.Pathway cp = Chakra.pathway((EntityLivingBase) entity);
				float cd = 20;
				if (world.getTotalWorldTime() > biju.getEntityData().getFloat("bijuACD") || ((EntityPlayer)entity).isCreative()   ) {
					if (tails == 9) {
						cd = 100;
						if (cp.consume(1000d)) {
							biju.mouthShootingJutsu = EntityNineTails.EntityBeam.shoot((EntityLivingBase) entity, 0.6f, 0.8f);
							biju.getEntityData().setFloat("bijuACD",world.getTotalWorldTime()+cd);
						}
					} else if (tails == 8 ){
						cd = 150;
						if (biju.getEntityData().getInteger("bijuBlast")  < 7) {
							if (cp.consume(200d) && biju.ticksExisted%5 == 0) {
								biju.getEntityData().setInteger("bijuBlast", biju.getEntityData().getInteger("bijuBlast")+1);
								biju.mouthShootingJutsu = EntityEightTails.EntitySmallBijudama.shoot(biju, 1, 1.2f, 0.025f);
							}
						} else {
							biju.getEntityData().setInteger("bijuBlast", 0);
							biju.getEntityData().setFloat("bijuACD",world.getTotalWorldTime()+cd);
						}
					}else if (tails == 7 ){
						cd = 150;
						if (biju instanceof EntitySevenTails.EntityCustom) {
							if (cp.consume(30d)) {
								((EntitySevenTails.EntityCustom) biju).blindUse++;
								((EntitySevenTails.EntityCustom) biju).blindPowder();
							} else {
								((EntitySevenTails.EntityCustom) biju).blindUse=100;
							}
							if (((EntitySevenTails.EntityCustom) biju).blindUse > 40 ){
								biju.getEntityData().setFloat("bijuACD",world.getTotalWorldTime()+cd);
								((EntitySevenTails.EntityCustom) biju).blindUse=0;
							}
						}
					}else if (tails == 6) {
						cd = 200;
						if (cp.consume(500d)) {
							biju.mouthShootingJutsu = EntityAcidScattering.EC.Jutsu.createJutsu(biju, 100);
							((EntityAcidScattering.EC)biju.mouthShootingJutsu).setPotionAmplifier(40);
							((EntityAcidScattering.EC)biju.mouthShootingJutsu).setDamage(120);
							biju.getEntityData().setFloat("bijuACD",world.getTotalWorldTime()+cd);
						}
					}else if (tails == 4) {
						cd = 80+100;
						if (cp.consume(850d)) {
							biju.mouthShootingJutsu = new EntityFirestream.EC.Jutsu2().createJutsu(biju, (float)biju.getBijudamaMinRange(), 80, 0xff00ff80);
							((EntityFirestream.EC)biju.mouthShootingJutsu).setTrueDamage(130f);
							biju.getEntityData().setFloat("bijuACD",world.getTotalWorldTime()+cd);
						}
					}else if (tails == 3) {
						cd = 100;
						if (cp.consume(500d)) {
							biju.mouthShootingJutsu = EntityWaterCanonball.EC.Jutsu.createJutsu(biju, 10.0f);
							((EntityWaterCanonball.EC)biju.mouthShootingJutsu).setDamage(400f);
							biju.getEntityData().setFloat("bijuACD",world.getTotalWorldTime()+cd);
						}
					}else if (tails == 2) {
						cd = 40;
						if (cp.consume(350d)) {
							biju.mouthShootingJutsu = new ItemKaton.EntityBigFireball(biju, 10.0f, false);
							Vec3d vec = biju.getLookVec();
							((ItemKaton.EntityBigFireball)biju.mouthShootingJutsu).shoot(vec.x,vec.y,vec.z, 1.2f, 0);
							((ItemKaton.EntityBigFireball)biju.mouthShootingJutsu).setDamage(250.0f);
							biju.world.spawnEntity(biju.mouthShootingJutsu);
							biju.getEntityData().setFloat("bijuACD",world.getTotalWorldTime()+cd);
						}
					}else if (tails == 1) {
						cd = 120;
						if (cp.consume(800d)) {
							biju.mouthShootingJutsu = new EntityFutonVacuum.EC.Jutsu().createJutsu(biju, (float)biju.getBijudamaMinRange(), 40);
							((EntityFutonVacuum.EC)biju.mouthShootingJutsu).setDamage(150.0f);
							((EntityFutonVacuum.EC)biju.mouthShootingJutsu).setBulletSize(40.0f);
							biju.getEntityData().setFloat("bijuACD",world.getTotalWorldTime()+cd);
						}
					}
				}
			} else {
				biju.setSwingingArms(false);
			}
		}
	}
}
