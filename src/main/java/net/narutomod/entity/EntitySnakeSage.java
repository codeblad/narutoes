
package net.narutomod.entity;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemHandlerHelper;
import net.narutomod.ElementsNarutomodMod;
import net.narutomod.PlayerTracker;
import net.narutomod.item.*;
import net.narutomod.procedure.ProcedureUtils;

import java.util.ArrayList;
import java.util.Iterator;

@ElementsNarutomodMod.ModElement.Tag
public class EntitySnakeSage extends ElementsNarutomodMod.ModElement {
    public static final int ENTITYID = 9006;
    public static final int ENTITYID_RANGED = 9007;
    public EntitySnakeSage(ElementsNarutomodMod instance) {
        super(instance, 9008);
    }

    @Override
    public void initElements() {
        elements.entities
                .add(() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("narutomod", "snake_sage"), ENTITYID)
                        .name("snake_sage").tracker(64, 3, true).egg(-16777216, -10066330).build());
    }

    private Biome[] allbiomes(net.minecraft.util.registry.RegistryNamespaced<ResourceLocation, Biome> in) {
        Iterator<Biome> itr = in.iterator();
        ArrayList<Biome> ls = new ArrayList<Biome>();
        while (itr.hasNext())
            ls.add(itr.next());
        return ls.toArray(new Biome[ls.size()]);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, renderManager -> {
            return new RenderLiving(renderManager, new ModelBiped64(), 0.5f) {
                protected ResourceLocation getEntityTexture(Entity entity) {
                    return new ResourceLocation("narutomod:textures/snakesage.png");
                }
            };
        });
    }
    public static class EntityCustom extends EntityMob {
        public EntityCustom(World world) {
            super(world);
            setSize(0.6f, 1.8f);
            experienceValue = 0;
            this.isImmuneToFire = false;
            setNoAI(!true);
            enablePersistence();
        }

        @Override
        protected void initEntityAI() {
            super.initEntityAI();
            this.tasks.addTask(1, new EntityAISwimming(this));
            this.tasks.addTask(2, new EntityAIAttackMelee(this, 1.2, false));
            this.tasks.addTask(3, new EntityAIWander(this, 1));
            this.tasks.addTask(4, new EntityAILookIdle(this));
            this.targetTasks.addTask(5, new EntityAIHurtByTarget(this, false));
        }

        @Override
        public EnumCreatureAttribute getCreatureAttribute() {
            return EnumCreatureAttribute.UNDEFINED;
        }

        @Override
        protected boolean canDespawn() {
            return false;
        }

        @Override
        protected Item getDropItem() {
            return null;
        }

        @Override
        public net.minecraft.util.SoundEvent getAmbientSound() {
            return (net.minecraft.util.SoundEvent) net.minecraft.util.SoundEvent.REGISTRY
                    .getObject(new ResourceLocation("entity.illusion_illager.ambient"));
        }

        @Override
        public net.minecraft.util.SoundEvent getHurtSound(DamageSource ds) {
            return (net.minecraft.util.SoundEvent) net.minecraft.util.SoundEvent.REGISTRY.getObject(new ResourceLocation("entity.generic.hurt"));
        }

        @Override
        public net.minecraft.util.SoundEvent getDeathSound() {
            return (net.minecraft.util.SoundEvent) net.minecraft.util.SoundEvent.REGISTRY.getObject(new ResourceLocation("entity.generic.death"));
        }

        @Override
        protected float getSoundVolume() {
            return 1.0F;
        }

        @Override
        protected void dropLoot(boolean wasRecentlyHit, int lootingModifier, DamageSource source) {
            this.entityDropItem(new ItemStack(ItemKusanagiSword.block, 1), 0.0f);
        }

        @Override
        protected boolean processInteract(EntityPlayer player, EnumHand hand) {
            if (!this.world.isRemote && ProcedureUtils.getMatchingItemStack(player,ItemSenjutsu.block) == null) {
                ItemStack pill = ProcedureUtils.getMatchingItemStack(player, ItemMilitaryRationsPillGold.block);
                boolean giveItem = true;
                if (pill == null) {
                    giveItem = false;
                    ProcedureUtils.sendChat(player, TextFormatting.GREEN + "Snake Sage" + ": "
                            + TextFormatting.WHITE + "Why do you not have the perc on you?");
                }
                if (PlayerTracker.getBattleXp(player) < 10000) {
                    giveItem = false;
                    ProcedureUtils.sendChat(player, TextFormatting.GREEN + "Snake Sage" + ": "
                            + TextFormatting.WHITE + "You are too weak to become a sage!");
                }
                if (giveItem) {
                    pill.shrink(1);
                    ItemStack stack = new ItemStack(ItemSenjutsu.block, 1);
                    ItemHandlerHelper.giveItemToPlayer(player, stack);
                    stack.setCount(1);
                    if (!stack.hasTagCompound()) {
                        stack.setTagCompound(new NBTTagCompound());
                    }
                    stack.getTagCompound().setString("Type", "snake");
                    ItemStack stack2 = new ItemStack(ItemSummoningContract.block, 1);
                    ItemHandlerHelper.giveItemToPlayer(player, stack2);
                    stack2.setCount(1);
                    if (!stack2.hasTagCompound()) {
                        stack2.setTagCompound(new NBTTagCompound());
                    }
                    stack2.getTagCompound().setString("Type", "snake");
                    this.setDead();
                }
            }
            return super.processInteract(player, hand);
        }

        @Override
        protected void applyEntityAttributes() {
            super.applyEntityAttributes();
            if (this.getEntityAttribute(SharedMonsterAttributes.ARMOR) != null)
                this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(100D);
            if (this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED) != null)
                this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.4D);
            if (this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH) != null)
                this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(500D);
            if (this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE) != null)
                this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(200D);
        }
    }

    // Made with Blockbench 3.7.4
    // Exported for Minecraft version 1.12
    // Paste this class into your mod and generate all required imports
    public static class ModelBiped64 extends ModelBiped {
        public ModelBiped64() {
            this.textureWidth = 64;
            this.textureHeight = 64;
            this.leftArmPose = ArmPose.EMPTY;
            this.rightArmPose = ArmPose.EMPTY;
            this.bipedHead = new ModelRenderer(this);
            this.bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
            this.bipedHead.cubeList.add(new ModelBox(this.bipedHead, 0, 0, -4.0F, -8.0F, -4.0F, 8, 8, 8, 0.0F, false));
            this.bipedHeadwear = new ModelRenderer(this);
            this.bipedHeadwear.setRotationPoint(0.0F, 0.0F, 0.0F);
            this.bipedHeadwear.cubeList.add(new ModelBox(this.bipedHeadwear, 32, 0, -4.0F, -8.0F, -4.0F, 8, 8, 8, 0.25F, false));
            this.bipedBody = new ModelRenderer(this);
            this.bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
            this.bipedBody.cubeList.add(new ModelBox(this.bipedBody, 16, 16, -4.0F, 0.0F, -2.0F, 8, 12, 4, 0.0F, false));
            this.bipedBody.cubeList.add(new ModelBox(this.bipedBody, 16, 32, -4.0F, 0.0F, -2.0F, 8, 12, 4, 0.25F, false));
            this.bipedRightArm = new ModelRenderer(this);
            this.bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
            this.bipedRightArm.cubeList.add(new ModelBox(this.bipedRightArm, 40, 16, -3.0F, -2.0F, -2.0F, 4, 12, 4, 0.0F, false));
            this.bipedRightArm.cubeList.add(new ModelBox(this.bipedRightArm, 40, 32, -3.0F, -2.0F, -2.0F, 4, 12, 4, 0.25F, false));
            this.bipedLeftArm = new ModelRenderer(this);
            this.bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
            this.bipedLeftArm.cubeList.add(new ModelBox(this.bipedLeftArm, 32, 48, -1.0F, -2.0F, -2.0F, 4, 12, 4, 0.0F, false));
            this.bipedLeftArm.cubeList.add(new ModelBox(this.bipedLeftArm, 48, 48, -1.0F, -2.0F, -2.0F, 4, 12, 4, 0.25F, false));
            this.bipedRightLeg = new ModelRenderer(this);
            this.bipedRightLeg.setRotationPoint(-1.9F, 12.0F, 0.0F);
            this.bipedRightLeg.cubeList.add(new ModelBox(this.bipedRightLeg, 0, 16, -2.0F, 0.0F, -2.0F, 4, 12, 4, 0.0F, false));
            this.bipedRightLeg.cubeList.add(new ModelBox(this.bipedRightLeg, 0, 32, -2.0F, 0.0F, -2.0F, 4, 12, 4, 0.25F, false));
            this.bipedLeftLeg = new ModelRenderer(this);
            this.bipedLeftLeg.setRotationPoint(1.9F, 12.0F, 0.0F);
            this.bipedLeftLeg.cubeList.add(new ModelBox(this.bipedLeftLeg, 16, 48, -2.0F, 0.0F, -2.0F, 4, 12, 4, 0.0F, false));
            this.bipedLeftLeg.cubeList.add(new ModelBox(this.bipedLeftLeg, 0, 48, -2.0F, 0.0F, -2.0F, 4, 12, 4, 0.25F, false));
        }

        public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity e) {
            super.setRotationAngles(f, f1, f2, f3, f4, f5, e);
        }
    }
}
