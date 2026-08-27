package net.narutomod.command;

import net.minecraft.command.*;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

import net.narutomod.Chakra;
import net.narutomod.ElementsNarutomodMod;
import net.narutomod.entity.EntityHiraishin;
import net.narutomod.procedure.ProcedureOnLivingUpdate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ElementsNarutomodMod.ModElement.Tag
public class CommandRaishin extends ElementsNarutomodMod.ModElement {

    public CommandRaishin(ElementsNarutomodMod instance) {
        super(instance, 909);
    }

    @Override
    public void serverLoad(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandHandler());
    }

    public static class CommandHandler extends CommandBase implements ICommand {

        @Override
        public int compareTo(ICommand c) {
            return getName().compareTo(c.getName());
        }

        @Override
        public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
            return true;
        }

        @Override
        public int getRequiredPermissionLevel() {
            return 0;
        }

        @Override
        public List getAliases() {
            return new ArrayList();
        }

        @Override
        public List<String> getTabCompletions(
                MinecraftServer server,
                ICommandSender sender,
                String[] args,
                BlockPos pos) {
            return new ArrayList();
        }

        @Override
        public boolean isUsernameIndex(String[] args, int index) {
            return false;
        }

        @Override
        public String getName() {
            return "raishin";
        }

        @Override
        public String getUsage(ICommandSender sender) {
            return "/raishin <name>";
        }

        @Override
        public void execute(
                MinecraftServer server,
                ICommandSender sender,
                String[] args) throws CommandException {

            if (args.length == 0) {
                throw new WrongUsageException(getUsage(sender));
            }

            EntityPlayerMP player = getCommandSenderAsPlayer(sender);

            if (!EntityHiraishin.canUseJutsu(player)) {
                throw new CommandException("You must be holding the Flying Raijin jutsu.");
            }
            
            String name = String.join(" ", args);

            UUID ownerUuid = player.getUniqueID();

            Map<UUID, EntityHiraishin.MarkerData> markers =
                    EntityHiraishin.getServerMarkers(ownerUuid);

            if (markers == null || markers.isEmpty()) {
                throw new CommandException("You do not have any Hiraishin marks.");
            }

            EntityHiraishin.MarkerData markerData = null;

            for (EntityHiraishin.MarkerData data : markers.values()) {
                if (data != null
                        && data.name != null
                        && data.name.equalsIgnoreCase(name)
                        && data.vec != null) {
                    markerData = data;
                    break;
                }
            }

            if (markerData == null) {
                throw new CommandException("You do not have a Hiraishin mark named '" + name + "'.");
            }

            int markerDimension = (int) markerData.vec.w;

            if (player.dimension != markerDimension) {
                throw new CommandException(
                        "That Hiraishin mark is in another dimension.");
            }

            double x = markerData.vec.x;
            double y = markerData.vec.y;
            double z = markerData.vec.z;

            Chakra.Pathway chakra = Chakra.pathway(player);

            double dx = x - player.posX;
            double dy = y - player.posY;
            double dz = z - player.posZ;
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

            double chakraUsage = Math.sqrt(distance) * 10d;

            AxisAlignedBB oldPlayerBox = player.getEntityBoundingBox();

            List<EntityLivingBase> nearbyEntities =
                    player.world.getEntitiesWithinAABB(
                            EntityLivingBase.class,
                            oldPlayerBox.grow(1.0d)
                    );

            for (EntityLivingBase entity : nearbyEntities) {
                if (entity == null
                        || entity.isDead
                        || EntityHiraishin.isTeleportDebounced(entity)) {
                    continue;
                }

                chakraUsage += 2d;
            }

            if (chakra.getAmount() <= chakraUsage) {
                chakra.warningDisplay();
                return;
            }

            ProcedureOnLivingUpdate.setUntargetable(player, 5);

            player.setPositionAndUpdate(x, y, z);

            EntityHiraishin.setTeleportDebounce(player, 2);

            for (EntityLivingBase entity : nearbyEntities) {

                if (entity == null
                        || entity.isDead
                        || EntityHiraishin.isTeleportDebounced(entity)) {
                    continue;
                }

                ProcedureOnLivingUpdate.setUntargetable(entity, 5);

                entity.setPosition(x, y, z);

                EntityHiraishin.setTeleportDebounce(entity, 2);

                if (entity instanceof EntityPlayerMP) {
                    ((EntityPlayerMP) entity).connection.setPlayerLocation(
                            x,
                            y,
                            z,
                            entity.rotationYaw,
                            entity.rotationPitch
                    );
                }
            }

            chakra.consume(chakraUsage);

            net.minecraft.util.SoundEvent swoosh =
                    net.minecraft.util.SoundEvent.REGISTRY.getObject(
                            new ResourceLocation("narutomod:swoosh")
                    );

            if (swoosh != null) {
                player.world.playSound(
                        null,
                        oldPlayerBox.minX,
                        oldPlayerBox.minY,
                        oldPlayerBox.minZ,
                        swoosh,
                        net.minecraft.util.SoundCategory.NEUTRAL,
                        0.8f,
                        player.getRNG().nextFloat() * 0.4f + 0.8f
                );

                player.world.playSound(
                        null,
                        x,
                        y,
                        z,
                        swoosh,
                        net.minecraft.util.SoundCategory.NEUTRAL,
                        0.8f,
                        player.getRNG().nextFloat() * 0.4f + 0.8f
                );
            }
        }
    }
}