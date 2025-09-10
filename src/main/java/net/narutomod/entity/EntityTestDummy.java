
package net.narutomod.entity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.items.ItemHandlerHelper;
import net.narutomod.ElementsNarutomodMod;

import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.World;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.DamageSource;
import net.minecraft.item.Item;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelBiped;
import net.narutomod.PlayerTracker;
import net.narutomod.item.ItemJutsu;
import net.narutomod.item.ItemMilitaryRationsPillGold;
import net.narutomod.item.ItemSenjutsu;
import net.narutomod.item.ItemSummoningContract;
import net.narutomod.procedure.ProcedureUtils;
import net.minecraft.potion.PotionEffect;
import net.minecraft.init.MobEffects;

import net.narutomod.entity.EntityBijuManager;

import java.util.Iterator;
import java.util.ArrayList;

@ElementsNarutomodMod.ModElement.Tag
public class EntityTestDummy extends ElementsNarutomodMod.ModElement {
    public static final int ENTITYID = 9540;
    public static final int ENTITYID_RANGED = 9541;
    public EntityTestDummy(ElementsNarutomodMod instance) {
        super(instance, 9542);
    }

    @Override
    public void initElements() {
        elements.entities.add(() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("narutomod", "testdummy"), ENTITYID)
                        .name("Testdummy").tracker(64, 3, true).egg(-16777216, -10066330).build());
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
                    return new ResourceLocation("narutomod:textures/slugsage.png");
                }
            };
        });
    }
    public static class EntityCustom extends EntityMob {
        public EntityCustom(World world) {
            super(world);
            setSize(0.6f, 1.8f);
            experienceValue = 0;
            this.isImmuneToFire = true;
            setNoAI(!true);
            enablePersistence();
        }

        @Override
        protected void initEntityAI() {
            super.initEntityAI();
            this.tasks.addTask(1, new EntityAISwimming(this));
            this.tasks.addTask(2, new EntityAIAttackMelee(this, 1.2, false));
            this.tasks.addTask(3, new EntityAILookIdle(this));
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
        protected boolean processInteract(EntityPlayer player, EnumHand hand) {
            double exp = PlayerTracker.getBattleXp(player);
            float maxhealth = player.getMaxHealth();

            if (this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH) != null)
                this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(maxhealth);

            if (this.getEntityAttribute(SharedMonsterAttributes.ARMOR) != null)
                this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(20D);

            if (EntityBijuManager.cloakLevel((EntityPlayer) player) == 2) {
                this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(22D); //if biju cloak defense changes swap this to match
            }

            if (this.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS) != null)
                this.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).setBaseValue(8D);

            this.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 99999, 4, false, false));
            this.addTag("Dummy");
            return super.processInteract(player, hand);
        }

        @Override
        protected void applyEntityAttributes() {
            super.applyEntityAttributes();
            if (this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED) != null)
                this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.1D);
        }
    }

    // Made with Blockbench 3.7.4
    // Exported for Minecraft version 1.12
    // Paste this class into your mod and generate all required imports
    public static class ModelBiped64 extends ModelBiped {
        public ModelBiped64() {
            this.textureWidth = 64;
            this.textureHeight = 64;
            this.leftArmPose = ModelBiped.ArmPose.EMPTY;
            this.rightArmPose = ModelBiped.ArmPose.EMPTY;
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
