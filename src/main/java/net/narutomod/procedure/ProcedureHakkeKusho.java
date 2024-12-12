package net.narutomod.procedure;

import akka.japi.Procedure;
import net.minecraft.util.*;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.block.Block;

import net.minecraft.util.text.TextComponentString;
import net.narutomod.NarutomodModVariables;
import net.narutomod.item.ItemJutsu;
import net.narutomod.item.ItemByakugan;
import net.narutomod.Chakra;
import net.narutomod.PlayerTracker;
import net.narutomod.ElementsNarutomodMod;

@ElementsNarutomodMod.ModElement.Tag
public class ProcedureHakkeKusho extends ElementsNarutomodMod.ModElement {
	private static final double XP_REQUIRED = 500d;
	private static final AirPunch KUSHO = new AirPunch();
	
	public ProcedureHakkeKusho(ElementsNarutomodMod instance) {
		super(instance, 265);
	}
	
	public static class AirPunch extends ProcedureAirPunch {

		public AirPunch() {
			this.blockDropChance = 0F;
			this.blockHardnessLimit = 0f;
		}

		@Override
		protected double getRange(int duration) {
			return duration / 3.0D + 5.0D;
		}

		@Override
		protected double getFarRadius(int duration) {
			return duration / 20.0D;
		}

		@Override
		protected void attackEntityFrom(Entity player, Entity target) {
			super.attackEntityFrom(player, target);
			if (target instanceof EntityLivingBase && player instanceof EntityPlayer) {
				float ratio = (float) ProcedureAirPunch.getPressDuration(player)/50;
				float strength = 1+(2*ratio);
				target.attackEntityFrom(ItemJutsu.causeJutsuDamage(player, null), 5+(1.8f*ItemJutsu.getDmgMult(player)) * strength);
			}
		}


		@Override
		protected EntityItem processAffectedBlock(Entity player, BlockPos pos, EnumFacing facing) {
			/*if (player.world.getGameRules().getBoolean("mobGriefing") && player.world.getBlockState(pos).isFullBlock()
			 && player.world.getBlockState(pos.up()).getCollisionBoundingBox(player.world, pos.up()) == Block.NULL_AABB) {
				EntityFallingBlock entity = new EntityFallingBlock(player.world, 0.5d+pos.getX(), pos.getY(), 0.5d+pos.getZ(), player.world.getBlockState(pos));
				entity.motionY = 0.45d;
				player.world.spawnEntity(entity);
			}*/
			return super.processAffectedBlock(player, pos, facing);
		}

		@Override
		protected float getBreakChance(BlockPos pos, Entity player, double range) {
			return player.world.getGameRules().getBoolean("mobGriefing")
			 && player instanceof EntityPlayer && PlayerTracker.getBattleXp((EntityPlayer)player) >= XP_REQUIRED + 850d
					? (1.0F - (float) ((Math.sqrt(player.getDistanceSqToCenter(pos)) - 4.0D) / MathHelper.clamp(range, 0.0D, 30.0D)))
					: 0.0F;
		}
	}
	
	public static void executeProcedure(java.util.Map<String, Object> dependencies) {
		Entity entity = (Entity) dependencies.get("entity");
		if (!(entity instanceof EntityPlayer)) {
			System.err.println("Failed to load dependency entity for procedure MCreatorHakkeKusho!");
			return;
		}
		if (dependencies.get("is_pressed") == null) {
			System.err.println("Failed to load dependency is_pressed for procedure MCreatorHakkeKusho!");
			return;
		}
		EntityPlayer player = (EntityPlayer) entity;
		if (entity.getEntityData().getFloat("airPalmcd") > NarutomodModVariables.world_tick) {
			player.sendStatusMessage(new TextComponentString("cooldown: " + (entity.getEntityData().getFloat("airPalmcd")-NarutomodModVariables.world_tick)/20), true);
			return;
		}
		if (!player.isCreative()
		 && (PlayerTracker.getBattleXp(player) < XP_REQUIRED || !ProcedureUtils.isOriginalOwner(player, player.inventory.armorInventory.get(3))))
			return;
		boolean is_pressed = ((Boolean) dependencies.get("is_pressed")).booleanValue();
		int pressDuration = ProcedureAirPunch.getPressDuration(player);
		Chakra.Pathway cp = Chakra.pathway(player);

		if (cp.getAmount() >= ItemByakugan.getKushoChakraUsage(player) * (pressDuration + 1)) {
			if (pressDuration > 49) {
				ProcedureAirPunch.setPressDuration(player, 49);
			}
			KUSHO.execute(is_pressed, player);
		}
		if (!is_pressed && pressDuration > 0) {
			player.getEntityData().setFloat("airPalmcd", (float) (NarutomodModVariables.world_tick +5*20));
			ProcedureSync.SwingMainArm.send(player);
			// player.swingArm(EnumHand.MAIN_HAND);
			entity.world.playSound(null, entity.posX, entity.posY, entity.posZ,
					(net.minecraft.util.SoundEvent) net.minecraft.util.SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:HakkeKusho")),
					SoundCategory.PLAYERS, 1.0F, 1.0F);
			cp.consume(100+ItemByakugan.getKushoChakraUsage(player) * pressDuration);
		}
	}
}
