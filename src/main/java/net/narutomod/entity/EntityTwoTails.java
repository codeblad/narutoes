
package net.narutomod.entity;

import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import net.minecraft.world.World;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.DamageSource;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.Entity;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.storage.MapStorage;
//import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.model.ModelQuadruped;
import net.minecraft.client.Minecraft;

//import net.narutomod.item.ItemBijuCloak;
import net.narutomod.item.ItemKaton;
import net.narutomod.procedure.ProcedureUtils;
import net.narutomod.Particles;
import net.narutomod.ElementsNarutomodMod;

import java.util.Random;
import javax.annotation.Nullable;

@ElementsNarutomodMod.ModElement.Tag
public class EntityTwoTails extends ElementsNarutomodMod.ModElement {
	public static final int ENTITYID = 265;
	public static final int ENTITYID_RANGED = 266;
	private static final float MODELSCALE = 10.0F;
	private static final TailBeastManager tailBeastManager = new TailBeastManager();

	public EntityTwoTails(ElementsNarutomodMod instance) {
		super(instance, 587);
	}

	@Override
	public void initElements() {
		elements.entities
				.add(() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("narutomod", "two_tails"), ENTITYID)
						.name("two_tails").tracker(96, 3, true).egg(-16737793, -16777216).build());
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void preInit(FMLPreInitializationEvent event) {
		RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, renderManager -> 
			new RenderCustom(renderManager, new ModelTwoTails())
		);
	}

	public static TailBeastManager getBijuManager() {
		return tailBeastManager;
	}

	public static class TailBeastManager extends EntityBijuManager<EntityCustom> {
		public TailBeastManager() {
			super(EntityCustom.class, 2);
		}

		@Override
		public void setJinchurikiPlayer(@Nullable EntityPlayer player) {
			super.setJinchurikiPlayer(player);
			if (player != null) {
				ItemStack stack = ProcedureUtils.getMatchingItemStack(player, ItemKaton.block);
				if (stack == null) {
					stack = new ItemStack(ItemKaton.block);
					((ItemKaton.RangedItem)stack.getItem()).setOwner(stack, player);
					ItemHandlerHelper.giveItemToPlayer(player, stack);
				}
				if (stack != null) {
					((ItemKaton.RangedItem)stack.getItem()).enableJutsu(stack, ItemKaton.GREATFIREBALL, true);
				}
			}
		}

		/*@Override
		public void toggleBijuCloak() {
			super.toggleBijuCloak();
			EntityPlayer jinchuriki = this.getJinchurikiPlayer();
			if (this.getCloakLevel() > 0) {
				if (jinchuriki.inventory.armorInventory.get(3).getItem() != ItemBijuCloak.helmet) {
					ProcedureUtils.swapItemToSlot(jinchuriki, EntityEquipmentSlot.HEAD, new ItemStack(ItemBijuCloak.helmet));
				}
				if (jinchuriki.inventory.armorInventory.get(2).getItem() != ItemBijuCloak.body) {
					ProcedureUtils.swapItemToSlot(jinchuriki, EntityEquipmentSlot.CHEST, new ItemStack(ItemBijuCloak.body, 1, 2));
				}
				if (jinchuriki.inventory.armorInventory.get(1).getItem() != ItemBijuCloak.legs) {
					ProcedureUtils.swapItemToSlot(jinchuriki, EntityEquipmentSlot.LEGS, new ItemStack(ItemBijuCloak.legs));
				}
			} else if (jinchuriki != null) {
				jinchuriki.inventory.clearMatchingItems(ItemBijuCloak.helmet, -1, -1, null);
				jinchuriki.inventory.clearMatchingItems(ItemBijuCloak.body, -1, -1, null);
				jinchuriki.inventory.clearMatchingItems(ItemBijuCloak.legs, -1, -1, null);
				EntityCustom entity = this.getEntityInWorld(jinchuriki.world);
				if (entity != null) {
					entity.setDead();
				}
			}
		}

		@Override
		public int increaseCloakLevel() {
			int ret = super.increaseCloakLevel();
			if (ret == 3) {
				EntityPlayer jinchuriki = this.getJinchurikiPlayer();
				jinchuriki.inventory.clearMatchingItems(ItemBijuCloak.helmet, -1, -1, null);
				jinchuriki.inventory.clearMatchingItems(ItemBijuCloak.body, -1, -1, null);
				jinchuriki.inventory.clearMatchingItems(ItemBijuCloak.legs, -1, -1, null);
				Entity biju = new EntityCustom(jinchuriki);
				biju.forceSpawn = true;
				jinchuriki.world.spawnEntity(biju);
				biju.forceSpawn = false;
			}
			return ret;
		}*/

		@Override
		public void markDirty() {
			Save.getInstance().markDirty();
		}
	}

	public static class Save extends EntityTailedBeast.SaveBase {
		private static final String DATA_NAME = net.narutomod.NarutomodMod.MODID + "_twotails";
		private static Save instance = null;

		public Save() {
			super(DATA_NAME);
		}

		public Save(String name) {
			super(name);
		}

	 	@Override
	 	public Save loadData() {
	 		instance = null;
	 		return this.getInstance();
	 	}

	 	@Override
	 	public void resetData() {
	 		instance = null;
	 	}

	 	public static Save getInstance() {
	 		if (instance == null) {
	 			MapStorage storage = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(0).getMapStorage();
	 			instance = (Save) storage.getOrLoadData(Save.class, DATA_NAME);
	 			if (instance == null) {
	 				instance = new Save(); 
	 				storage.setData(DATA_NAME, instance);
	 			} 
	 		} 
	 		return instance;
	 	}

	 	@Override
	 	protected EntityBijuManager getBijuManager() {
	 		return tailBeastManager;
	 	}

	 	@Override
	 	protected EntityTailedBeast.Base createEntity(World world) {
	 		return new EntityCustom(world);
	 	}
	}

	public static class EntityCustom extends EntityTailedBeast.Base {
		public EntityCustom(World world) {
			super(world);
			this.setSize(MODELSCALE * 1.0F, MODELSCALE * 1.6F);
			this.experienceValue = 12000;
			this.stepHeight = this.height / 3.0F;
		}

		public EntityCustom(EntityPlayer player) {
			super(player);
			this.setSize(MODELSCALE * 1.0F, MODELSCALE * 1.6F);
			this.experienceValue = 12000;
			this.stepHeight = this.height / 3.0F;
		}

		@Override
		protected void applyEntityAttributes() {
			super.applyEntityAttributes();
			this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(100.0D);
			this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.8D);
			this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10000.0D);
			this.getEntityAttribute(EntityPlayer.REACH_DISTANCE).setBaseValue(30.0D);
		}

		@Override
		public EntityBijuManager getBijuManager() {
			return tailBeastManager;
		}

		@Override
		public double getMountedYOffset() {
			return (double)this.height + 0.35D;
		}

		@Override
		public void updatePassenger(Entity passenger) {
			Vec3d vec[] = { new Vec3d(0.25d * MODELSCALE, 0d, 0d) };
			if (this.isPassenger(passenger)) {
				int i = this.getPassengers().indexOf(passenger);
				Vec3d vec2 = vec[i].rotateYaw(-this.rotationYaw * 0.017453292F - ((float)Math.PI / 2F));
				passenger.setPosition(this.posX + vec2.x, this.posY + this.getMountedYOffset() + passenger.getYOffset(), this.posZ + vec2.z);
			}
		}

		@Override
		public net.minecraft.util.SoundEvent getAmbientSound() {
			return (net.minecraft.util.SoundEvent) net.minecraft.util.SoundEvent.REGISTRY.getObject(new ResourceLocation(""));
		}

		@Override
		public net.minecraft.util.SoundEvent getHurtSound(DamageSource ds) {
			return (net.minecraft.util.SoundEvent) net.minecraft.util.SoundEvent.REGISTRY.getObject(new ResourceLocation(""));
		}

		@Override
		public net.minecraft.util.SoundEvent getDeathSound() {
			return (net.minecraft.util.SoundEvent) net.minecraft.util.SoundEvent.REGISTRY.getObject(new ResourceLocation(""));
		}

		@Override
		public void onUpdate() {
			super.onUpdate();
			for (int i = 0; i < 8; i++) {
				double d0 = this.posX + (this.rand.nextFloat() - 0.5D) * (this.width + 2.0D);
				double d1 = this.posY + this.height + this.rand.nextFloat() * 6.0f - 3.0f;
				double d2 = this.posZ + (this.rand.nextFloat() - 0.5D) * (this.width + 2.0D);
				this.world.spawnAlwaysVisibleParticle(Particles.Types.FLAME.getID(), d0, d1, d2, 0.0D, 0.0D, 0.0D, 0x201e61b5, (int)(this.width * 10f));
			}
		}
	}

	@SideOnly(Side.CLIENT)
	public class RenderCustom extends EntityTailedBeast.Renderer<EntityCustom> {
		private final ResourceLocation MAIN_TEXTURE = new ResourceLocation("narutomod:textures/twotails.png");

		public RenderCustom(RenderManager renderManagerIn, ModelTwoTails modelIn) {
			super(renderManagerIn, modelIn, MODELSCALE * 0.5F);
			this.addLayer(new LayerFlames(this, modelIn));
		}

		@Override
		public void doRender(EntityCustom entity, double x, double y, double z, float entityYaw, float partialTicks) {
			((ModelTwoTails)this.getMainModel()).setFlamedVisible(false);
			super.doRender(entity, x, y, z, entityYaw, partialTicks);
		}

	 	@Override
		protected ResourceLocation getEntityTexture(EntityCustom entity) {
			return MAIN_TEXTURE;
		}
	}

	@SideOnly(Side.CLIENT)
	public class LayerFlames implements LayerRenderer<EntityCustom> {
		private final ResourceLocation FLAME_TEXTURE = new ResourceLocation("narutomod:textures/twotailsflames.png");
		private final RenderCustom renderer;
		private final ModelTwoTails renderModel;

		public LayerFlames(RenderCustom rendererIn, ModelTwoTails modelIn) {
			this.renderer = rendererIn;
			this.renderModel = modelIn;
		}

		@Override
		public void doRenderLayer(EntityCustom entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
				float netHeadYaw, float headPitch, float scale) {
			this.renderModel.setFlamedVisible(true);
			GlStateManager.pushMatrix();
			GlStateManager.depthMask(true);
			this.renderer.bindTexture(FLAME_TEXTURE);
			//GlStateManager.scale(0.984375F, 0.984375F, 0.984375F);
			//GlStateManager.translate(0.0F, -0.1F, 0.1F);
			GlStateManager.matrixMode(5890);
			GlStateManager.loadIdentity();
			//float f = entitylivingbaseIn.ticksExisted + partialTicks;
			GlStateManager.translate(0.0F, ageInTicks * 0.01F, 0.0F);
			GlStateManager.matrixMode(5888);
			GlStateManager.enableBlend();
			GlStateManager.color(1.0F, 1.0F, 1.0F, 0.9F);
			GlStateManager.disableLighting();
			GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			//GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
			this.renderModel.setModelAttributes(this.renderer.getMainModel());
			Minecraft.getMinecraft().entityRenderer.setupFogColor(true);
			this.renderModel.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entitylivingbaseIn);
			this.renderModel.render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
			Minecraft.getMinecraft().entityRenderer.setupFogColor(false);
			GlStateManager.enableLighting();
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			GlStateManager.matrixMode(5890);
			GlStateManager.loadIdentity();
			GlStateManager.matrixMode(5888);
			GlStateManager.disableBlend();
			GlStateManager.depthMask(false);
			GlStateManager.popMatrix();
			this.renderModel.setFlamedVisible(false);
		}

		@Override
		public boolean shouldCombineTextures() {
			return false;
		}
	}

	// Made with Blockbench 3.8.4
	// Exported for Minecraft version 1.7 - 1.12
	// Paste this class into your mod and generate all required imports
	@SideOnly(Side.CLIENT)
	public class ModelTwoTails extends ModelQuadruped {
		//private final ModelRenderer head;
		private final ModelRenderer cube_r1;
		private final ModelRenderer cube_r2;
		private final ModelRenderer Ears;
		private final ModelRenderer RightEar;
		private final ModelRenderer cube_r3;
		private final ModelRenderer LeftEar;
		private final ModelRenderer cube_r4;
		private final ModelRenderer Hair;
		private final ModelRenderer Hair4;
		private final ModelRenderer cube_r5;
		private final ModelRenderer Hair3;
		private final ModelRenderer cube_r6;
		private final ModelRenderer Hair2;
		private final ModelRenderer cube_r7;
		private final ModelRenderer Hair7;
		private final ModelRenderer cube_r8;
		private final ModelRenderer Hair8;
		private final ModelRenderer cube_r9;
		private final ModelRenderer Hair9;
		private final ModelRenderer cube_r10;
		private final ModelRenderer Hair10;
		private final ModelRenderer cube_r11;
		private final ModelRenderer cube_r12;
		private final ModelRenderer Hair5;
		private final ModelRenderer cube_r13;
		private final ModelRenderer Hair6;
		private final ModelRenderer cube_r14;
		private final ModelRenderer TopMouth;
		private final ModelRenderer cube_r15;
		private final ModelRenderer cube_r16;
		private final ModelRenderer cube_r17;
		private final ModelRenderer nose;
		private final ModelRenderer cube_r18;
		private final ModelRenderer UpperTeeth;
		private final ModelRenderer cube_r19;
		private final ModelRenderer cube_r20;
		private final ModelRenderer cube_r21;
		private final ModelRenderer cube_r22;
		private final ModelRenderer cube_r23;
		private final ModelRenderer Jaw;
		private final ModelRenderer cube_r24;
		private final ModelRenderer cube_r25;
		private final ModelRenderer cube_r26;
		private final ModelRenderer cube_r27;
		private final ModelRenderer cube_r28;
		private final ModelRenderer cube_r29;
		private final ModelRenderer bone;
		private final ModelRenderer cube_r30;
		private final ModelRenderer cube_r31;
		private final ModelRenderer bone2;
		private final ModelRenderer cube_r32;
		private final ModelRenderer cube_r33;
		private final ModelRenderer bone3;
		private final ModelRenderer cube_r34;
		private final ModelRenderer cube_r35;
		private final ModelRenderer bone4;
		private final ModelRenderer cube_r36;
		private final ModelRenderer cube_r37;
		private final ModelRenderer LowerTeeth;
		private final ModelRenderer cube_r38;
		private final ModelRenderer eyes;
		//private final ModelRenderer body;
		private final ModelRenderer cube_r39;
		private final ModelRenderer cube_r40;
		private final ModelRenderer cube_r41;
		private final ModelRenderer[][] Tail = new ModelRenderer[2][8];
		private final float tailSwayX[][] = new float[2][8];
		private final float tailSwayY[][] = new float[2][8];
		private final float tailSwayZ[][] = new float[2][8];
		//private final ModelRenderer leg1;
		private final ModelRenderer Joint7;
		private final ModelRenderer cube_r42;
		private final ModelRenderer Joint8;
		private final ModelRenderer cube_r43;
		private final ModelRenderer Foot1;
		private final ModelRenderer cube_r44;
		private final ModelRenderer cube_r45;
		private final ModelRenderer cube_r46;
		private final ModelRenderer cube_r47;
		//private final ModelRenderer leg2;
		private final ModelRenderer Joint2;
		private final ModelRenderer cube_r48;
		private final ModelRenderer Joint5;
		private final ModelRenderer cube_r49;
		private final ModelRenderer Foot2;
		private final ModelRenderer cube_r50;
		private final ModelRenderer cube_r51;
		private final ModelRenderer cube_r52;
		private final ModelRenderer cube_r53;
		//private final ModelRenderer leg3;
		private final ModelRenderer Joint3;
		private final ModelRenderer cube_r54;
		private final ModelRenderer Joint4;
		private final ModelRenderer cube_r55;
		private final ModelRenderer Foot3;
		private final ModelRenderer cube_r56;
		private final ModelRenderer cube_r57;
		private final ModelRenderer cube_r58;
		private final ModelRenderer cube_r59;
		//private final ModelRenderer leg4;
		private final ModelRenderer Joint6;
		private final ModelRenderer cube_r60;
		private final ModelRenderer Joint9;
		private final ModelRenderer cube_r61;
		private final ModelRenderer Foot4;
		private final ModelRenderer cube_r62;
		private final ModelRenderer cube_r63;
		private final ModelRenderer cube_r64;
		private final ModelRenderer cube_r65;

		private final ModelRenderer headFlamed;
		private final ModelRenderer cube_r66;
		private final ModelRenderer cube_r67;
		private final ModelRenderer Ears2;
		private final ModelRenderer RightEar2;
		private final ModelRenderer cube_r68;
		private final ModelRenderer LeftEar2;
		private final ModelRenderer cube_r69;
		private final ModelRenderer Hair11;
		private final ModelRenderer Hair12;
		private final ModelRenderer cube_r70;
		private final ModelRenderer Hair13;
		private final ModelRenderer cube_r71;
		private final ModelRenderer Hair14;
		private final ModelRenderer cube_r72;
		private final ModelRenderer Hair15;
		private final ModelRenderer cube_r73;
		private final ModelRenderer Hair16;
		private final ModelRenderer cube_r74;
		private final ModelRenderer Hair17;
		private final ModelRenderer cube_r75;
		private final ModelRenderer Hair18;
		private final ModelRenderer cube_r76;
		private final ModelRenderer cube_r77;
		private final ModelRenderer Hair19;
		private final ModelRenderer cube_r78;
		private final ModelRenderer Hair20;
		private final ModelRenderer cube_r79;
		private final ModelRenderer TopMouth2;
		private final ModelRenderer cube_r80;
		private final ModelRenderer cube_r81;
		private final ModelRenderer cube_r82;
		private final ModelRenderer nose2;
		private final ModelRenderer cube_r83;
		//private final ModelRenderer UpperTeeth2;
		//private final ModelRenderer cube_r84;
		//private final ModelRenderer cube_r85;
		//private final ModelRenderer cube_r86;
		//private final ModelRenderer cube_r87;
		//private final ModelRenderer cube_r88;
		private final ModelRenderer Jaw2;
		private final ModelRenderer cube_r89;
		private final ModelRenderer cube_r90;
		private final ModelRenderer cube_r91;
		private final ModelRenderer cube_r92;
		private final ModelRenderer cube_r93;
		private final ModelRenderer cube_r94;
		private final ModelRenderer bone5;
		private final ModelRenderer cube_r95;
		private final ModelRenderer cube_r96;
		private final ModelRenderer bone6;
		private final ModelRenderer cube_r97;
		private final ModelRenderer cube_r98;
		private final ModelRenderer bone7;
		private final ModelRenderer cube_r99;
		private final ModelRenderer cube_r100;
		private final ModelRenderer bone8;
		private final ModelRenderer cube_r101;
		private final ModelRenderer cube_r102;
		//private final ModelRenderer LowerTeeth2;
		//private final ModelRenderer cube_r103;
		private final ModelRenderer bodyFlamed;
		private final ModelRenderer cube_r104;
		private final ModelRenderer cube_r105;
		private final ModelRenderer cube_r106;
		private final ModelRenderer[][] TailFlamed = new ModelRenderer[2][8];
		private final ModelRenderer leg1Flamed;
		private final ModelRenderer Joint10;
		private final ModelRenderer cube_r107;
		private final ModelRenderer Joint11;
		private final ModelRenderer cube_r108;
		private final ModelRenderer Foot5;
		private final ModelRenderer cube_r109;
		private final ModelRenderer cube_r110;
		private final ModelRenderer cube_r111;
		private final ModelRenderer cube_r112;
		private final ModelRenderer leg2Flamed;
		private final ModelRenderer Joint12;
		private final ModelRenderer cube_r113;
		private final ModelRenderer Joint13;
		private final ModelRenderer cube_r114;
		private final ModelRenderer Foot6;
		private final ModelRenderer cube_r115;
		private final ModelRenderer cube_r116;
		private final ModelRenderer cube_r117;
		private final ModelRenderer cube_r118;
		private final ModelRenderer leg3Flamed;
		private final ModelRenderer Joint14;
		private final ModelRenderer cube_r119;
		private final ModelRenderer Joint15;
		private final ModelRenderer cube_r120;
		private final ModelRenderer Foot7;
		private final ModelRenderer cube_r121;
		private final ModelRenderer cube_r122;
		private final ModelRenderer cube_r123;
		private final ModelRenderer cube_r124;
		private final ModelRenderer leg4Flamed;
		private final ModelRenderer Joint16;
		private final ModelRenderer cube_r125;
		private final ModelRenderer Joint17;
		private final ModelRenderer cube_r126;
		private final ModelRenderer Foot8;
		private final ModelRenderer cube_r127;
		private final ModelRenderer cube_r128;
		private final ModelRenderer cube_r129;
		private final ModelRenderer cube_r130;

		private final Random rand = new Random();
	
		public ModelTwoTails() {
			super(12, 0.0F);
			textureWidth = 128;
			textureHeight = 128;
	
			head = new ModelRenderer(this);
			head.setRotationPoint(0.0F, 3.0F, -6.0F);
			cube_r1 = new ModelRenderer(this);
			cube_r1.setRotationPoint(6.0F, 2.75F, 4.0F);
			head.addChild(cube_r1);
			setRotationAngle(cube_r1, 0.0F, 0.0F, 0.0F);
			cube_r1.cubeList.add(new ModelBox(cube_r1, 45, 43, -11.0F, -8.0F, -14.0F, 10, 2, 1, 0.0F, false));
			cube_r2 = new ModelRenderer(this);
			cube_r2.setRotationPoint(6.0F, 9.75F, 4.25F);
			head.addChild(cube_r2);
			setRotationAngle(cube_r2, 0.0F, 0.0F, 0.0F);
			cube_r2.cubeList.add(new ModelBox(cube_r2, 57, 20, -11.0F, -15.0F, -14.0F, 10, 10, 11, 0.0F, false));
			Ears = new ModelRenderer(this);
			Ears.setRotationPoint(11.75F, 5.25F, 0.25F);
			head.addChild(Ears);
			RightEar = new ModelRenderer(this);
			RightEar.setRotationPoint(-12.75F, 6.0F, -11.0F);
			Ears.addChild(RightEar);
			setRotationAngle(RightEar, -0.5744F, -0.3332F, -0.468F);
			RightEar.cubeList.add(new ModelBox(RightEar, 0, 8, 3.5F, -18.5F, -8.25F, 2, 6, 1, 0.0F, false));
			RightEar.cubeList.add(new ModelBox(RightEar, 0, 0, 4.3284F, -18.5F, -8.25F, 2, 6, 1, 0.0F, false));
			cube_r3 = new ModelRenderer(this);
			cube_r3.setRotationPoint(16.4047F, -0.6456F, 0.0F);
			RightEar.addChild(cube_r3);
			setRotationAngle(cube_r3, 0.0F, 0.0F, -0.7854F);
			cube_r3.cubeList.add(new ModelBox(cube_r3, 28, 70, 3.5F, -21.75F, -8.25F, 2, 2, 1, 0.0F, false));
			LeftEar = new ModelRenderer(this);
			LeftEar.setRotationPoint(-10.75F, 6.0F, -11.0F);
			Ears.addChild(LeftEar);
			setRotationAngle(LeftEar, -0.5744F, 0.3332F, 0.468F);
			LeftEar.cubeList.add(new ModelBox(LeftEar, 0, 8, -5.5F, -18.5F, -8.25F, 2, 6, 1, 0.0F, true));
			LeftEar.cubeList.add(new ModelBox(LeftEar, 0, 0, -6.3284F, -18.5F, -8.25F, 2, 6, 1, 0.0F, true));
			cube_r4 = new ModelRenderer(this);
			cube_r4.setRotationPoint(-16.4047F, -0.6456F, 0.0F);
			LeftEar.addChild(cube_r4);
			setRotationAngle(cube_r4, 0.0F, 0.0F, 0.7854F);
			cube_r4.cubeList.add(new ModelBox(cube_r4, 28, 70, -5.5F, -21.75F, -8.25F, 2, 2, 1, 0.0F, true));
			Hair = new ModelRenderer(this);
			Hair.setRotationPoint(11.75F, 5.25F, 0.25F);
			head.addChild(Hair);
			Hair4 = new ModelRenderer(this);
			Hair4.setRotationPoint(3.5F, 10.25F, -19.25F);
			Hair.addChild(Hair4);
			setRotationAngle(Hair4, -1.1345F, 0.0436F, -1.0036F);
			Hair4.cubeList.add(new ModelBox(Hair4, 28, 66, -1.6651F, -21.6317F, -16.75F, 2, 3, 1, 0.0F, false));
			Hair4.cubeList.add(new ModelBox(Hair4, 42, 60, -0.8367F, -21.6317F, -16.75F, 2, 3, 1, 0.0F, false));
			cube_r5 = new ModelRenderer(this);
			cube_r5.setRotationPoint(-15.8072F, -6.0753F, 0.0F);
			Hair4.addChild(cube_r5);
			setRotationAngle(cube_r5, 0.0F, 0.0F, 0.7854F);
			cube_r5.cubeList.add(new ModelBox(cube_r5, 70, 0, -1.0F, -23.0F, -16.75F, 2, 2, 1, 0.0F, false));
			Hair3 = new ModelRenderer(this);
			Hair3.setRotationPoint(-27.0F, 10.25F, -19.25F);
			Hair.addChild(Hair3);
			setRotationAngle(Hair3, -1.1345F, -0.0436F, 1.0036F);
			Hair3.cubeList.add(new ModelBox(Hair3, 28, 66, -0.3349F, -21.6317F, -16.75F, 2, 3, 1, 0.0F, true));
			Hair3.cubeList.add(new ModelBox(Hair3, 42, 60, -1.1633F, -21.6317F, -16.75F, 2, 3, 1, 0.0F, true));
			cube_r6 = new ModelRenderer(this);
			cube_r6.setRotationPoint(15.8072F, -6.0753F, 0.0F);
			Hair3.addChild(cube_r6);
			setRotationAngle(cube_r6, 0.0F, 0.0F, -0.7854F);
			cube_r6.cubeList.add(new ModelBox(cube_r6, 70, 0, -1.0F, -23.0F, -16.75F, 2, 2, 1, 0.0F, true));
			Hair2 = new ModelRenderer(this);
			Hair2.setRotationPoint(3.5F, 8.25F, -10.5F);
			Hair.addChild(Hair2);
			setRotationAngle(Hair2, -0.829F, 0.0436F, -0.9163F);
			Hair2.cubeList.add(new ModelBox(Hair2, 0, 60, -1.6651F, -21.6317F, -16.75F, 2, 4, 1, 0.0F, false));
			Hair2.cubeList.add(new ModelBox(Hair2, 6, 42, -0.8367F, -21.6317F, -16.75F, 2, 5, 1, 0.0F, false));
			cube_r7 = new ModelRenderer(this);
			cube_r7.setRotationPoint(-15.8072F, -6.0753F, 0.0F);
			Hair2.addChild(cube_r7);
			setRotationAngle(cube_r7, 0.0F, 0.0F, 0.7854F);
			cube_r7.cubeList.add(new ModelBox(cube_r7, 70, 3, -1.0F, -23.0F, -16.75F, 2, 2, 1, 0.0F, false));
			Hair7 = new ModelRenderer(this);
			Hair7.setRotationPoint(-27.0F, 8.25F, -10.5F);
			Hair.addChild(Hair7);
			setRotationAngle(Hair7, -0.829F, -0.0436F, 0.9163F);
			Hair7.cubeList.add(new ModelBox(Hair7, 0, 60, -0.3349F, -21.6317F, -16.75F, 2, 4, 1, 0.0F, true));
			Hair7.cubeList.add(new ModelBox(Hair7, 6, 42, -1.1633F, -21.6317F, -16.75F, 2, 5, 1, 0.0F, true));
			cube_r8 = new ModelRenderer(this);
			cube_r8.setRotationPoint(15.8072F, -6.0753F, 0.0F);
			Hair7.addChild(cube_r8);
			setRotationAngle(cube_r8, 0.0F, 0.0F, -0.7854F);
			cube_r8.cubeList.add(new ModelBox(cube_r8, 70, 3, -1.0F, -23.0F, -16.75F, 2, 2, 1, 0.0F, true));
			Hair8 = new ModelRenderer(this);
			Hair8.setRotationPoint(5.75F, 5.25F, -10.5F);
			Hair.addChild(Hair8);
			setRotationAngle(Hair8, -0.829F, 0.0436F, -0.9163F);
			Hair8.cubeList.add(new ModelBox(Hair8, 45, 33, -1.6651F, -21.6317F, -16.75F, 2, 4, 1, 0.0F, false));
			Hair8.cubeList.add(new ModelBox(Hair8, 10, 36, -0.8367F, -21.6317F, -16.75F, 2, 5, 1, 0.0F, false));
			cube_r9 = new ModelRenderer(this);
			cube_r9.setRotationPoint(-15.8072F, -6.0753F, 0.0F);
			Hair8.addChild(cube_r9);
			setRotationAngle(cube_r9, 0.0F, 0.0F, 0.7854F);
			cube_r9.cubeList.add(new ModelBox(cube_r9, 66, 41, -1.0F, -23.0F, -16.75F, 2, 2, 1, 0.0F, false));
			Hair9 = new ModelRenderer(this);
			Hair9.setRotationPoint(-29.25F, 5.25F, -10.5F);
			Hair.addChild(Hair9);
			setRotationAngle(Hair9, -0.829F, -0.0436F, 0.9163F);
			Hair9.cubeList.add(new ModelBox(Hair9, 45, 33, -0.3349F, -21.6317F, -16.75F, 2, 4, 1, 0.0F, true));
			Hair9.cubeList.add(new ModelBox(Hair9, 10, 36, -1.1633F, -21.6317F, -16.75F, 2, 5, 1, 0.0F, true));
			cube_r10 = new ModelRenderer(this);
			cube_r10.setRotationPoint(15.8072F, -6.0753F, 0.0F);
			Hair9.addChild(cube_r10);
			setRotationAngle(cube_r10, 0.0F, 0.0F, -0.7854F);
			cube_r10.cubeList.add(new ModelBox(cube_r10, 66, 41, -1.0F, -23.0F, -16.75F, 2, 2, 1, 0.0F, true));
			Hair10 = new ModelRenderer(this);
			Hair10.setRotationPoint(-9.0F, -34.75F, 12.0F);
			Hair.addChild(Hair10);
			setRotationAngle(Hair10, -1.1781F, 0.0F, 0.0F);
			Hair10.cubeList.add(new ModelBox(Hair10, 9, 8, -3.3358F, 24.2726F, 14.5601F, 2, 5, 1, 0.0F, false));
			Hair10.cubeList.add(new ModelBox(Hair10, 9, 0, -4.1642F, 24.2726F, 14.5601F, 2, 5, 1, 0.0F, false));
			cube_r11 = new ModelRenderer(this);
			cube_r11.setRotationPoint(12.8063F, 39.8289F, 31.3101F);
			Hair10.addChild(cube_r11);
			setRotationAngle(cube_r11, 0.0F, 0.0F, -0.7854F);
			cube_r11.cubeList.add(new ModelBox(cube_r11, 0, 45, -0.6569F, -23.3431F, -14.6549F, 2, 2, 1, 0.0F, false));
			cube_r11.cubeList.add(new ModelBox(cube_r11, 45, 38, -3.1318F, -25.8179F, -14.6549F, 2, 2, 1, 0.0F, false));
			cube_r11.cubeList.add(new ModelBox(cube_r11, 35, 62, -2.6433F, -23.8315F, -15.3642F, 2, 2, 1, 0.0F, false));
			cube_r11.cubeList.add(new ModelBox(cube_r11, 0, 65, -1.0F, -23.0F, -16.75F, 2, 2, 1, 0.0F, false));
			cube_r12 = new ModelRenderer(this);
			cube_r12.setRotationPoint(-18.3063F, 39.8289F, 31.3101F);
			Hair10.addChild(cube_r12);
			setRotationAngle(cube_r12, 0.0F, 0.0F, 0.7854F);
			cube_r12.cubeList.add(new ModelBox(cube_r12, 45, 38, 1.1318F, -25.8179F, -14.6549F, 2, 2, 1, 0.0F, true));
			cube_r12.cubeList.add(new ModelBox(cube_r12, 35, 62, 0.6433F, -23.8315F, -15.3642F, 2, 2, 1, 0.0F, true));
			Hair5 = new ModelRenderer(this);
			Hair5.setRotationPoint(4.25F, 9.75F, -10.5F);
			Hair.addChild(Hair5);
			setRotationAngle(Hair5, -0.829F, -0.3491F, -0.9163F);
			Hair5.cubeList.add(new ModelBox(Hair5, 52, 0, -1.6651F, -21.6317F, -16.75F, 2, 4, 1, 0.0F, false));
			Hair5.cubeList.add(new ModelBox(Hair5, 0, 39, -0.8367F, -21.6317F, -16.75F, 2, 5, 1, 0.0F, false));
			cube_r13 = new ModelRenderer(this);
			cube_r13.setRotationPoint(-15.8072F, -6.0753F, 0.0F);
			Hair5.addChild(cube_r13);
			setRotationAngle(cube_r13, 0.0F, 0.0F, 0.7854F);
			cube_r13.cubeList.add(new ModelBox(cube_r13, 68, 17, -1.0F, -23.0F, -16.75F, 2, 2, 1, 0.0F, false));
			Hair6 = new ModelRenderer(this);
			Hair6.setRotationPoint(-27.75F, 9.75F, -10.5F);
			Hair.addChild(Hair6);
			setRotationAngle(Hair6, -0.829F, 0.3491F, 0.9163F);
			Hair6.cubeList.add(new ModelBox(Hair6, 52, 0, -0.3349F, -21.6317F, -16.75F, 2, 4, 1, 0.0F, true));
			Hair6.cubeList.add(new ModelBox(Hair6, 0, 39, -1.1633F, -21.6317F, -16.75F, 2, 5, 1, 0.0F, true));
			cube_r14 = new ModelRenderer(this);
			cube_r14.setRotationPoint(15.8072F, -6.0753F, 0.0F);
			Hair6.addChild(cube_r14);
			setRotationAngle(cube_r14, 0.0F, 0.0F, -0.7854F);
			cube_r14.cubeList.add(new ModelBox(cube_r14, 68, 17, -1.0F, -23.0F, -16.75F, 2, 2, 1, 0.0F, true));
			TopMouth = new ModelRenderer(this);
			TopMouth.setRotationPoint(-0.25F, 20.5F, 14.25F);
			head.addChild(TopMouth);
			cube_r15 = new ModelRenderer(this);
			cube_r15.setRotationPoint(7.5815F, -13.5F, -19.9805F);
			TopMouth.addChild(cube_r15);
			setRotationAngle(cube_r15, 0.0873F, 0.1309F, 0.0F);
			cube_r15.cubeList.add(new ModelBox(cube_r15, 88, 0, -5.0F, -8.0F, -10.0F, 2, 3, 7, 0.0F, true));
			cube_r16 = new ModelRenderer(this);
			cube_r16.setRotationPoint(-7.0815F, -13.5F, -19.9805F);
			TopMouth.addChild(cube_r16);
			setRotationAngle(cube_r16, 0.0873F, -0.1309F, 0.0F);
			cube_r16.cubeList.add(new ModelBox(cube_r16, 88, 0, 3.0F, -8.0F, -10.0F, 2, 3, 7, 0.0F, false));
			cube_r17 = new ModelRenderer(this);
			cube_r17.setRotationPoint(0.25F, -20.4088F, -30.0418F);
			TopMouth.addChild(cube_r17);
			setRotationAngle(cube_r17, 0.1745F, 0.0F, 0.0F);
			cube_r17.cubeList.add(new ModelBox(cube_r17, 69, 7, -3.0F, -0.1988F, -0.1005F, 6, 3, 7, 0.0F, false));
			nose = new ModelRenderer(this);
			nose.setRotationPoint(6.25F, -13.5F, -20.75F);
			TopMouth.addChild(nose);
			cube_r18 = new ModelRenderer(this);
			cube_r18.setRotationPoint(0.0F, 0.0F, 0.0F);
			nose.addChild(cube_r18);
			setRotationAngle(cube_r18, 0.0873F, 0.0F, 0.0F);
			cube_r18.cubeList.add(new ModelBox(cube_r18, 55, 46, -7.0F, -8.0F, -9.0F, 2, 1, 1, 0.0F, false));
			UpperTeeth = new ModelRenderer(this);
			UpperTeeth.setRotationPoint(0.0F, -0.5F, -9.25F);
			TopMouth.addChild(UpperTeeth);
			cube_r19 = new ModelRenderer(this);
			cube_r19.setRotationPoint(3.556F, -17.25F, -1.7657F);
			UpperTeeth.addChild(cube_r19);
			setRotationAngle(cube_r19, 3.1416F, 0.0F, -3.1416F);
			cube_r19.cubeList.add(new ModelBox(cube_r19, 0, 31, 0.807F, -0.5F, 18.4824F, 1, 2, 0, 0.0F, false));
			cube_r20 = new ModelRenderer(this);
			cube_r20.setRotationPoint(16.2956F, -17.0F, -19.249F);
			UpperTeeth.addChild(cube_r20);
			setRotationAngle(cube_r20, 0.0F, -1.5272F, 0.0F);
			cube_r20.cubeList.add(new ModelBox(cube_r20, 45, 46, -1.807F, -0.5F, 18.4824F, 5, 1, 0, 0.0F, false));
			cube_r21 = new ModelRenderer(this);
			cube_r21.setRotationPoint(-15.7956F, -17.0F, -19.249F);
			UpperTeeth.addChild(cube_r21);
			setRotationAngle(cube_r21, 0.0F, 1.5272F, 0.0F);
			cube_r21.cubeList.add(new ModelBox(cube_r21, 45, 47, -3.193F, -0.5F, 18.4824F, 5, 1, 0, 0.0F, false));
			cube_r22 = new ModelRenderer(this);
			cube_r22.setRotationPoint(0.556F, -17.0F, -1.7657F);
			UpperTeeth.addChild(cube_r22);
			setRotationAngle(cube_r22, 3.1416F, 0.0F, -3.1416F);
			cube_r22.cubeList.add(new ModelBox(cube_r22, 9, 6, -1.193F, -0.5F, 18.4824F, 3, 1, 0, 0.0F, false));
			cube_r23 = new ModelRenderer(this);
			cube_r23.setRotationPoint(-0.444F, -17.25F, -1.7657F);
			UpperTeeth.addChild(cube_r23);
			setRotationAngle(cube_r23, 3.1416F, 0.0F, -3.1416F);
			cube_r23.cubeList.add(new ModelBox(cube_r23, 14, 31, 0.807F, -0.5F, 18.4824F, 1, 2, 0, 0.0F, false));
			Jaw = new ModelRenderer(this);
			Jaw.setRotationPoint(-0.0749F, 3.6113F, -9.4922F);
			head.addChild(Jaw);
			setRotationAngle(Jaw, 0.5236F, 0.0F, 0.0F);
			cube_r24 = new ModelRenderer(this);
			cube_r24.setRotationPoint(6.0749F, 8.1425F, -10.4545F);
			Jaw.addChild(cube_r24);
			setRotationAngle(cube_r24, 0.1745F, 0.0F, 0.0F);
			cube_r24.cubeList.add(new ModelBox(cube_r24, 20, 62, -9.0F, -6.0F, 9.0F, 6, 1, 3, 0.0F, false));
			cube_r25 = new ModelRenderer(this);
			cube_r25.setRotationPoint(6.0749F, 9.3049F, 2.6281F);
			Jaw.addChild(cube_r25);
			setRotationAngle(cube_r25, -0.3054F, 0.0F, 0.0F);
			cube_r25.cubeList.add(new ModelBox(cube_r25, 0, 34, -9.0F, -6.0F, -10.0F, 6, 1, 1, 0.0F, false));
			cube_r26 = new ModelRenderer(this);
			cube_r26.setRotationPoint(6.0749F, 5.9394F, 5.9459F);
			Jaw.addChild(cube_r26);
			setRotationAngle(cube_r26, 0.0873F, 0.0F, 0.0F);
			cube_r26.cubeList.add(new ModelBox(cube_r26, 0, 31, -9.0F, -6.0F, -10.0F, 6, 1, 2, 0.0F, false));
			cube_r27 = new ModelRenderer(this);
			cube_r27.setRotationPoint(-6.8275F, 5.6387F, 4.6908F);
			Jaw.addChild(cube_r27);
			setRotationAngle(cube_r27, 0.0873F, -0.0873F, 0.0F);
			cube_r27.cubeList.add(new ModelBox(cube_r27, 0, 0, 3.0F, -6.0F, -10.0F, 1, 1, 7, 0.0F, false));
			cube_r28 = new ModelRenderer(this);
			cube_r28.setRotationPoint(6.9773F, 5.6387F, 4.6908F);
			Jaw.addChild(cube_r28);
			setRotationAngle(cube_r28, 0.0873F, 0.0873F, 0.0F);
			cube_r28.cubeList.add(new ModelBox(cube_r28, 0, 8, -4.0F, -6.0F, -10.0F, 1, 1, 7, 0.0F, false));
			cube_r29 = new ModelRenderer(this);
			cube_r29.setRotationPoint(6.0749F, 5.6387F, 4.9922F);
			Jaw.addChild(cube_r29);
			setRotationAngle(cube_r29, 0.0873F, 0.0F, 0.0F);
			cube_r29.cubeList.add(new ModelBox(cube_r29, 69, 84, -9.0F, -6.0F, -10.0F, 6, 1, 7, 0.0F, false));
			bone = new ModelRenderer(this);
			bone.setRotationPoint(8.2288F, -3.2172F, 4.8801F);
			Jaw.addChild(bone);
			cube_r30 = new ModelRenderer(this);
			cube_r30.setRotationPoint(0.0F, 0.0F, 0.0F);
			bone.addChild(cube_r30);
			setRotationAngle(cube_r30, 0.1809F, 0.1538F, -0.7744F);
			cube_r30.cubeList.add(new ModelBox(cube_r30, 40, 64, -9.0F, -6.0F, -10.0F, 1, 1, 1, 0.0F, false));
			cube_r31 = new ModelRenderer(this);
			cube_r31.setRotationPoint(0.0181F, 1.7427F, -0.2458F);
			bone.addChild(cube_r31);
			setRotationAngle(cube_r31, 0.0564F, 0.0308F, -0.7859F);
			cube_r31.cubeList.add(new ModelBox(cube_r31, 44, 64, -9.0F, -6.0F, -10.0F, 1, 1, 1, 0.0F, false));
			bone2 = new ModelRenderer(this);
			bone2.setRotationPoint(9.2288F, -3.2172F, 4.8801F);
			Jaw.addChild(bone2);
			cube_r32 = new ModelRenderer(this);
			cube_r32.setRotationPoint(0.0F, 0.0F, 0.0F);
			bone2.addChild(cube_r32);
			setRotationAngle(cube_r32, 0.1809F, 0.1538F, -0.7744F);
			cube_r32.cubeList.add(new ModelBox(cube_r32, 40, 64, -9.0F, -6.0F, -10.0F, 1, 1, 1, 0.0F, false));
			cube_r33 = new ModelRenderer(this);
			cube_r33.setRotationPoint(0.0181F, 1.7427F, -0.2458F);
			bone2.addChild(cube_r33);
			setRotationAngle(cube_r33, 0.0564F, 0.0308F, -0.7859F);
			cube_r33.cubeList.add(new ModelBox(cube_r33, 44, 64, -9.0F, -6.0F, -10.0F, 1, 1, 1, 0.0F, false));
			bone3 = new ModelRenderer(this);
			bone3.setRotationPoint(10.2288F, -3.2172F, 4.8801F);
			Jaw.addChild(bone3);
			cube_r34 = new ModelRenderer(this);
			cube_r34.setRotationPoint(0.0F, 0.0F, 0.0F);
			bone3.addChild(cube_r34);
			setRotationAngle(cube_r34, 0.1809F, 0.1538F, -0.7744F);
			cube_r34.cubeList.add(new ModelBox(cube_r34, 40, 64, -9.0F, -6.0F, -10.0F, 1, 1, 1, 0.0F, false));
			cube_r35 = new ModelRenderer(this);
			cube_r35.setRotationPoint(0.0181F, 1.7427F, -0.2458F);
			bone3.addChild(cube_r35);
			setRotationAngle(cube_r35, 0.0564F, 0.0308F, -0.7859F);
			cube_r35.cubeList.add(new ModelBox(cube_r35, 44, 64, -9.0F, -6.0F, -10.0F, 1, 1, 1, 0.0F, false));
			bone4 = new ModelRenderer(this);
			bone4.setRotationPoint(11.2288F, -3.2172F, 4.8801F);
			Jaw.addChild(bone4);
			cube_r36 = new ModelRenderer(this);
			cube_r36.setRotationPoint(0.0F, 0.0F, 0.0F);
			bone4.addChild(cube_r36);
			setRotationAngle(cube_r36, 0.1809F, 0.1538F, -0.7744F);
			cube_r36.cubeList.add(new ModelBox(cube_r36, 40, 64, -9.0F, -6.0F, -10.0F, 1, 1, 1, 0.0F, false));
			cube_r37 = new ModelRenderer(this);
			cube_r37.setRotationPoint(0.0181F, 1.7427F, -0.2458F);
			bone4.addChild(cube_r37);
			setRotationAngle(cube_r37, 0.0564F, 0.0308F, -0.7859F);
			cube_r37.cubeList.add(new ModelBox(cube_r37, 44, 64, -9.0F, -6.0F, -10.0F, 1, 1, 1, 0.0F, false));
			LowerTeeth = new ModelRenderer(this);
			LowerTeeth.setRotationPoint(-18.1751F, 15.1387F, -4.0078F);
			Jaw.addChild(LowerTeeth);
			setRotationAngle(LowerTeeth, -3.1416F, -1.5272F, 3.1416F);
			LowerTeeth.cubeList.add(new ModelBox(LowerTeeth, 60, 15, -2.1529F, -15.5F, -20.6758F, 4, 1, 0, 0.0F, false));
			LowerTeeth.cubeList.add(new ModelBox(LowerTeeth, 52, 15, -1.9348F, -15.5F, -15.6805F, 4, 1, 0, 0.0F, false));
			cube_r38 = new ModelRenderer(this);
			cube_r38.setRotationPoint(16.4522F, -15.25F, -18.291F);
			LowerTeeth.addChild(cube_r38);
			setRotationAngle(cube_r38, 0.0F, -1.5272F, 0.0F);
			cube_r38.cubeList.add(new ModelBox(cube_r38, 52, 14, -3.193F, -0.25F, 18.4824F, 5, 1, 0, 0.0F, false));
	
			eyes = new ModelRenderer(this);
			eyes.setRotationPoint(0.0F, 3.0F, -6.0F);
			eyes.cubeList.add(new ModelBox(eyes, 76, 0, -4.0F, -3.0F, -9.8F, 8, 2, 0, 0.0F, false));
	
			body = new ModelRenderer(this);
			body.setRotationPoint(0.0F, 3.0F, 0.0F);
			cube_r39 = new ModelRenderer(this);
			cube_r39.setRotationPoint(6.0F, 8.0F, 4.25F);
			body.addChild(cube_r39);
			setRotationAngle(cube_r39, 0.0F, 0.0F, 0.0F);
			cube_r39.cubeList.add(new ModelBox(cube_r39, 0, 31, -13.0F, -12.0F, 5.0F, 14, 12, 17, 0.0F, false));
			cube_r40 = new ModelRenderer(this);
			cube_r40.setRotationPoint(6.0F, 8.0F, -8.0F);
			body.addChild(cube_r40);
			setRotationAngle(cube_r40, -0.0436F, 0.0F, 0.0F);
			cube_r40.cubeList.add(new ModelBox(cube_r40, 48, 46, -14.0F, -14.0F, 14.0F, 16, 14, 14, 0.0F, false));
			cube_r41 = new ModelRenderer(this);
			cube_r41.setRotationPoint(6.0F, 8.0F, -13.5F);
			body.addChild(cube_r41);
			setRotationAngle(cube_r41, -0.0873F, 0.0F, 0.0F);
			cube_r41.cubeList.add(new ModelBox(cube_r41, 0, 0, -15.0F, -16.0F, 5.0F, 18, 15, 16, 0.0F, false));
			Tail[0][0] = new ModelRenderer(this);
			Tail[0][0].setRotationPoint(-3.0F, 1.0F, 26.0F);
			body.addChild(Tail[0][0]);
			setRotationAngle(Tail[0][0], -1.309F, -0.7854F, 0.0F);
			Tail[0][0].cubeList.add(new ModelBox(Tail[0][0], 52, 0, -3.0F, -7.0F, -3.0F, 6, 8, 6, 0.0F, false));
			Tail[0][1] = new ModelRenderer(this);
			Tail[0][1].setRotationPoint(0.0F, -6.0F, 0.0F);
			Tail[0][0].addChild(Tail[0][1]);
			setRotationAngle(Tail[0][1], 0.2618F, 0.0F, 0.0873F);
			Tail[0][1].cubeList.add(new ModelBox(Tail[0][1], 52, 0, -3.0F, -7.0F, -3.0F, 6, 8, 6, 0.2F, false));
			Tail[0][2] = new ModelRenderer(this);
			Tail[0][2].setRotationPoint(0.0F, -6.0F, 0.0F);
			Tail[0][1].addChild(Tail[0][2]);
			setRotationAngle(Tail[0][2], 0.2618F, 0.0F, 0.0873F);
			Tail[0][2].cubeList.add(new ModelBox(Tail[0][2], 52, 0, -3.0F, -7.0F, -3.0F, 6, 8, 6, 0.4F, false));
			Tail[0][3] = new ModelRenderer(this);
			Tail[0][3].setRotationPoint(0.0F, -6.0F, 0.0F);
			Tail[0][2].addChild(Tail[0][3]);
			setRotationAngle(Tail[0][3], 0.2618F, 0.0F, 0.0873F);
			Tail[0][3].cubeList.add(new ModelBox(Tail[0][3], 52, 0, -3.0F, -7.0F, -3.0F, 6, 8, 6, 0.5F, false));
			Tail[0][4] = new ModelRenderer(this);
			Tail[0][4].setRotationPoint(0.0F, -6.0F, 0.0F);
			Tail[0][3].addChild(Tail[0][4]);
			setRotationAngle(Tail[0][4], -0.2618F, 0.0F, 0.0873F);
			Tail[0][4].cubeList.add(new ModelBox(Tail[0][4], 52, 0, -3.0F, -7.0F, -3.0F, 6, 8, 6, 0.3F, false));
			Tail[0][5] = new ModelRenderer(this);
			Tail[0][5].setRotationPoint(0.0F, -6.0F, 0.0F);
			Tail[0][4].addChild(Tail[0][5]);
			setRotationAngle(Tail[0][5], -0.2618F, 0.0F, 0.0873F);
			Tail[0][5].cubeList.add(new ModelBox(Tail[0][5], 52, 0, -3.0F, -7.0F, -3.0F, 6, 8, 6, 0.1F, false));
			Tail[0][6] = new ModelRenderer(this);
			Tail[0][6].setRotationPoint(0.0F, -6.0F, 0.0F);
			Tail[0][5].addChild(Tail[0][6]);
			setRotationAngle(Tail[0][6], -0.2618F, 0.0F, 0.0873F);
			Tail[0][6].cubeList.add(new ModelBox(Tail[0][6], 52, 0, -3.0F, -7.0F, -3.0F, 6, 8, 6, -0.3F, false));
			Tail[0][7] = new ModelRenderer(this);
			Tail[0][7].setRotationPoint(0.0F, -6.0F, 0.0F);
			Tail[0][6].addChild(Tail[0][7]);
			setRotationAngle(Tail[0][7], -0.2618F, 0.0F, 0.0873F);
			Tail[0][7].cubeList.add(new ModelBox(Tail[0][7], 52, 0, -3.0F, -7.0F, -3.0F, 6, 8, 6, -0.8F, false));
			Tail[1][0] = new ModelRenderer(this);
			Tail[1][0].setRotationPoint(3.0F, 1.0F, 26.0F);
			body.addChild(Tail[1][0]);
			setRotationAngle(Tail[1][0], -1.309F, 0.7854F, 0.0F);
			Tail[1][0].cubeList.add(new ModelBox(Tail[1][0], 52, 0, -3.0F, -7.0F, -3.0F, 6, 8, 6, 0.0F, true));
			Tail[1][1] = new ModelRenderer(this);
			Tail[1][1].setRotationPoint(0.0F, -6.0F, 0.0F);
			Tail[1][0].addChild(Tail[1][1]);
			setRotationAngle(Tail[1][1], 0.2618F, 0.0F, -0.0873F);
			Tail[1][1].cubeList.add(new ModelBox(Tail[1][1], 52, 0, -3.0F, -7.0F, -3.0F, 6, 8, 6, 0.2F, true));
			Tail[1][2] = new ModelRenderer(this);
			Tail[1][2].setRotationPoint(0.0F, -6.0F, 0.0F);
			Tail[1][1].addChild(Tail[1][2]);
			setRotationAngle(Tail[1][2], 0.2618F, 0.0F, -0.0873F);
			Tail[1][2].cubeList.add(new ModelBox(Tail[1][2], 52, 0, -3.0F, -7.0F, -3.0F, 6, 8, 6, 0.4F, true));
			Tail[1][3] = new ModelRenderer(this);
			Tail[1][3].setRotationPoint(0.0F, -6.0F, 0.0F);
			Tail[1][2].addChild(Tail[1][3]);
			setRotationAngle(Tail[1][3], 0.2618F, 0.0F, -0.0873F);
			Tail[1][3].cubeList.add(new ModelBox(Tail[1][3], 52, 0, -3.0F, -7.0F, -3.0F, 6, 8, 6, 0.5F, true));
			Tail[1][4] = new ModelRenderer(this);
			Tail[1][4].setRotationPoint(0.0F, -6.0F, 0.0F);
			Tail[1][3].addChild(Tail[1][4]);
			setRotationAngle(Tail[1][4], -0.2618F, 0.0F, -0.0873F);
			Tail[1][4].cubeList.add(new ModelBox(Tail[1][4], 52, 0, -3.0F, -7.0F, -3.0F, 6, 8, 6, 0.3F, true));
			Tail[1][5] = new ModelRenderer(this);
			Tail[1][5].setRotationPoint(0.0F, -6.0F, 0.0F);
			Tail[1][4].addChild(Tail[1][5]);
			setRotationAngle(Tail[1][5], -0.2618F, 0.0F, -0.0873F);
			Tail[1][5].cubeList.add(new ModelBox(Tail[1][5], 52, 0, -3.0F, -7.0F, -3.0F, 6, 8, 6, 0.1F, true));
			Tail[1][6] = new ModelRenderer(this);
			Tail[1][6].setRotationPoint(0.0F, -6.0F, 0.0F);
			Tail[1][5].addChild(Tail[1][6]);
			setRotationAngle(Tail[1][6], -0.2618F, 0.0F, -0.0873F);
			Tail[1][6].cubeList.add(new ModelBox(Tail[1][6], 52, 0, -3.0F, -7.0F, -3.0F, 6, 8, 6, -0.3F, true));
			Tail[1][7] = new ModelRenderer(this);
			Tail[1][7].setRotationPoint(0.0F, -6.0F, 0.0F);
			Tail[1][6].addChild(Tail[1][7]);
			setRotationAngle(Tail[1][7], -0.2618F, 0.0F, -0.0873F);
			Tail[1][7].cubeList.add(new ModelBox(Tail[1][7], 52, 0, -3.0F, -7.0F, -3.0F, 6, 8, 6, -0.8F, true));
	
			leg1 = new ModelRenderer(this);
			leg1.setRotationPoint(-4.75F, 4.0F, -1.0F);
			Joint7 = new ModelRenderer(this);
			Joint7.setRotationPoint(0.0F, -2.0F, -1.0F);
			leg1.addChild(Joint7);
			setRotationAngle(Joint7, 0.1745F, 0.0F, 0.0F);
			cube_r42 = new ModelRenderer(this);
			cube_r42.setRotationPoint(2.0F, 12.0F, -11.25F);
			Joint7.addChild(cube_r42);
			setRotationAngle(cube_r42, -0.0436F, 0.0F, 0.0F);
			cube_r42.cubeList.add(new ModelBox(cube_r42, 28, 66, -8.0F, -12.0F, 7.0F, 6, 14, 8, 0.0F, false));
			Joint8 = new ModelRenderer(this);
			Joint8.setRotationPoint(-2.875F, 15.25F, 0.75F);
			Joint7.addChild(Joint8);
			setRotationAngle(Joint8, -0.5236F, 0.0F, 0.0F);
			cube_r43 = new ModelRenderer(this);
			cube_r43.setRotationPoint(4.375F, 5.75F, -14.75F);
			Joint8.addChild(cube_r43);
			setRotationAngle(cube_r43, 0.0F, 0.0F, 0.0F);
			cube_r43.cubeList.add(new ModelBox(cube_r43, 0, 82, -7.0F, -8.0F, 11.0F, 5, 10, 5, 0.0F, false));
			Foot1 = new ModelRenderer(this);
			Foot1.setRotationPoint(-0.125F, 5.9375F, 0.4375F);
			Joint8.addChild(Foot1);
			setRotationAngle(Foot1, 0.3491F, 0.0F, 0.0F);
			cube_r44 = new ModelRenderer(this);
			cube_r44.setRotationPoint(6.5F, 2.0625F, -22.4375F);
			Foot1.addChild(cube_r44);
			setRotationAngle(cube_r44, 0.0F, 0.0F, 0.0F);
			cube_r44.cubeList.add(new ModelBox(cube_r44, 6, 39, -7.0F, -2.0F, 15.0F, 1, 2, 1, 0.0F, false));
			cube_r45 = new ModelRenderer(this);
			cube_r45.setRotationPoint(8.0F, 2.0625F, -22.4375F);
			Foot1.addChild(cube_r45);
			setRotationAngle(cube_r45, 0.0F, 0.0F, 0.0F);
			cube_r45.cubeList.add(new ModelBox(cube_r45, 12, 42, -7.0F, -2.0F, 15.0F, 1, 2, 1, 0.0F, false));
			cube_r46 = new ModelRenderer(this);
			cube_r46.setRotationPoint(5.0F, 2.0625F, -22.4375F);
			Foot1.addChild(cube_r46);
			setRotationAngle(cube_r46, 0.0F, 0.0F, 0.0F);
			cube_r46.cubeList.add(new ModelBox(cube_r46, 12, 45, -7.0F, -2.0F, 15.0F, 1, 2, 1, 0.0F, false));
			cube_r47 = new ModelRenderer(this);
			cube_r47.setRotationPoint(4.5F, 1.8125F, -15.1875F);
			Foot1.addChild(cube_r47);
			setRotationAngle(cube_r47, 0.0F, 0.0F, 0.0F);
			cube_r47.cubeList.add(new ModelBox(cube_r47, 76, 74, -7.0F, -2.0F, 8.0F, 5, 2, 8, 0.0F, false));
	
			leg2 = new ModelRenderer(this);
			leg2.setRotationPoint(4.75F, 4.0F, -1.0F);
			Joint2 = new ModelRenderer(this);
			Joint2.setRotationPoint(0.0F, -2.0F, -1.0F);
			leg2.addChild(Joint2);
			setRotationAngle(Joint2, 0.1745F, 0.0F, 0.0F);
			cube_r48 = new ModelRenderer(this);
			cube_r48.setRotationPoint(-2.0F, 12.0F, -11.25F);
			Joint2.addChild(cube_r48);
			setRotationAngle(cube_r48, -0.0436F, 0.0F, 0.0F);
			cube_r48.cubeList.add(new ModelBox(cube_r48, 28, 66, 2.0F, -12.0F, 7.0F, 6, 14, 8, 0.0F, true));
			Joint5 = new ModelRenderer(this);
			Joint5.setRotationPoint(2.875F, 15.25F, 0.75F);
			Joint2.addChild(Joint5);
			setRotationAngle(Joint5, -0.5236F, 0.0F, 0.0F);
			cube_r49 = new ModelRenderer(this);
			cube_r49.setRotationPoint(-4.375F, 5.75F, -14.75F);
			Joint5.addChild(cube_r49);
			setRotationAngle(cube_r49, 0.0F, 0.0F, 0.0F);
			cube_r49.cubeList.add(new ModelBox(cube_r49, 0, 82, 2.0F, -8.0F, 11.0F, 5, 10, 5, 0.0F, true));
			Foot2 = new ModelRenderer(this);
			Foot2.setRotationPoint(0.125F, 5.9375F, 0.4375F);
			Joint5.addChild(Foot2);
			setRotationAngle(Foot2, 0.3491F, 0.0F, 0.0F);
			cube_r50 = new ModelRenderer(this);
			cube_r50.setRotationPoint(-6.5F, 2.0625F, -22.4375F);
			Foot2.addChild(cube_r50);
			setRotationAngle(cube_r50, 0.0F, 0.0F, 0.0F);
			cube_r50.cubeList.add(new ModelBox(cube_r50, 6, 39, 6.0F, -2.0F, 15.0F, 1, 2, 1, 0.0F, true));
			cube_r51 = new ModelRenderer(this);
			cube_r51.setRotationPoint(-8.0F, 2.0625F, -22.4375F);
			Foot2.addChild(cube_r51);
			setRotationAngle(cube_r51, 0.0F, 0.0F, 0.0F);
			cube_r51.cubeList.add(new ModelBox(cube_r51, 12, 42, 6.0F, -2.0F, 15.0F, 1, 2, 1, 0.0F, true));
			cube_r52 = new ModelRenderer(this);
			cube_r52.setRotationPoint(-5.0F, 2.0625F, -22.4375F);
			Foot2.addChild(cube_r52);
			setRotationAngle(cube_r52, 0.0F, 0.0F, 0.0F);
			cube_r52.cubeList.add(new ModelBox(cube_r52, 12, 45, 6.0F, -2.0F, 15.0F, 1, 2, 1, 0.0F, true));
			cube_r53 = new ModelRenderer(this);
			cube_r53.setRotationPoint(-4.5F, 1.8125F, -15.1875F);
			Foot2.addChild(cube_r53);
			setRotationAngle(cube_r53, 0.0F, 0.0F, 0.0F);
			cube_r53.cubeList.add(new ModelBox(cube_r53, 76, 74, 2.0F, -2.0F, 8.0F, 5, 2, 8, 0.0F, true));
	
			leg3 = new ModelRenderer(this);
			leg3.setRotationPoint(-5.5F, 8.0F, 21.5F);
			Joint3 = new ModelRenderer(this);
			Joint3.setRotationPoint(0.0F, 0.0F, 0.0F);
			leg3.addChild(Joint3);
			setRotationAngle(Joint3, -1.5708F, 0.0F, 0.0F);
			cube_r54 = new ModelRenderer(this);
			cube_r54.setRotationPoint(2.25F, 17.5F, -2.75F);
			Joint3.addChild(cube_r54);
			setRotationAngle(cube_r54, 0.7854F, 0.0F, 0.0F);
			cube_r54.cubeList.add(new ModelBox(cube_r54, 0, 60, -8.0F, -14.3536F, 10.182F, 6, 14, 8, 0.2F, false));
			Joint4 = new ModelRenderer(this);
			Joint4.setRotationPoint(1.25F, 17.5F, -2.75F);
			Joint3.addChild(Joint4);
			cube_r55 = new ModelRenderer(this);
			cube_r55.setRotationPoint(-10.0F, 27.5103F, 8.3355F);
			Joint4.addChild(cube_r55);
			setRotationAngle(cube_r55, -1.1781F, 0.0F, 0.0F);
			cube_r55.cubeList.add(new ModelBox(cube_r55, 56, 74, 3.0F, -24.6324F, -38.7863F, 5, 11, 5, 0.0F, false));
			Foot3 = new ModelRenderer(this);
			Foot3.setRotationPoint(-4.5F, -16.9625F, 16.4375F);
			Joint4.addChild(Foot3);
			setRotationAngle(Foot3, 1.5708F, 0.0F, 0.0F);
			cube_r56 = new ModelRenderer(this);
			cube_r56.setRotationPoint(6.5F, 2.0625F, -22.4375F);
			Foot3.addChild(cube_r56);
			setRotationAngle(cube_r56, 0.0F, 0.0F, 0.0F);
			cube_r56.cubeList.add(new ModelBox(cube_r56, 6, 39, -7.0F, -2.0F, 15.0F, 1, 2, 1, 0.0F, false));
			cube_r57 = new ModelRenderer(this);
			cube_r57.setRotationPoint(8.0F, 2.0625F, -22.4375F);
			Foot3.addChild(cube_r57);
			setRotationAngle(cube_r57, 0.0F, 0.0F, 0.0F);
			cube_r57.cubeList.add(new ModelBox(cube_r57, 12, 42, -7.0F, -2.0F, 15.0F, 1, 2, 1, 0.0F, false));
			cube_r58 = new ModelRenderer(this);
			cube_r58.setRotationPoint(5.0F, 2.0625F, -22.4375F);
			Foot3.addChild(cube_r58);
			setRotationAngle(cube_r58, 0.0F, 0.0F, 0.0F);
			cube_r58.cubeList.add(new ModelBox(cube_r58, 12, 45, -7.0F, -2.0F, 15.0F, 1, 2, 1, 0.0F, false));
			cube_r59 = new ModelRenderer(this);
			cube_r59.setRotationPoint(4.5F, 1.8125F, -15.1875F);
			Foot3.addChild(cube_r59);
			setRotationAngle(cube_r59, 0.0F, 0.0F, 0.0F);
			cube_r59.cubeList.add(new ModelBox(cube_r59, 76, 74, -7.0F, -2.0F, 8.0F, 5, 2, 8, 0.0F, false));
	
			leg4 = new ModelRenderer(this);
			leg4.setRotationPoint(5.5F, 8.0F, 21.5F);
			Joint6 = new ModelRenderer(this);
			Joint6.setRotationPoint(0.0F, 0.0F, 0.0F);
			leg4.addChild(Joint6);
			setRotationAngle(Joint6, -1.5708F, 0.0F, 0.0F);
			cube_r60 = new ModelRenderer(this);
			cube_r60.setRotationPoint(-2.25F, 17.5F, -2.75F);
			Joint6.addChild(cube_r60);
			setRotationAngle(cube_r60, 0.7854F, 0.0F, 0.0F);
			cube_r60.cubeList.add(new ModelBox(cube_r60, 0, 60, 2.0F, -14.3536F, 10.182F, 6, 14, 8, 0.2F, true));
			Joint9 = new ModelRenderer(this);
			Joint9.setRotationPoint(-1.25F, 17.5F, -2.75F);
			Joint6.addChild(Joint9);
			cube_r61 = new ModelRenderer(this);
			cube_r61.setRotationPoint(10.0F, 27.5103F, 8.3355F);
			Joint9.addChild(cube_r61);
			setRotationAngle(cube_r61, -1.1781F, 0.0F, 0.0F);
			cube_r61.cubeList.add(new ModelBox(cube_r61, 56, 74, -8.0F, -24.6324F, -38.7863F, 5, 11, 5, 0.0F, true));
			Foot4 = new ModelRenderer(this);
			Foot4.setRotationPoint(4.5F, -16.9625F, 16.4375F);
			Joint9.addChild(Foot4);
			setRotationAngle(Foot4, 1.5708F, 0.0F, 0.0F);
			cube_r62 = new ModelRenderer(this);
			cube_r62.setRotationPoint(-6.5F, 2.0625F, -22.4375F);
			Foot4.addChild(cube_r62);
			setRotationAngle(cube_r62, 0.0F, 0.0F, 0.0F);
			cube_r62.cubeList.add(new ModelBox(cube_r62, 6, 39, 6.0F, -2.0F, 15.0F, 1, 2, 1, 0.0F, true));
			cube_r63 = new ModelRenderer(this);
			cube_r63.setRotationPoint(-8.0F, 2.0625F, -22.4375F);
			Foot4.addChild(cube_r63);
			setRotationAngle(cube_r63, 0.0F, 0.0F, 0.0F);
			cube_r63.cubeList.add(new ModelBox(cube_r63, 12, 42, 6.0F, -2.0F, 15.0F, 1, 2, 1, 0.0F, true));
			cube_r64 = new ModelRenderer(this);
			cube_r64.setRotationPoint(-5.0F, 2.0625F, -22.4375F);
			Foot4.addChild(cube_r64);
			setRotationAngle(cube_r64, 0.0F, 0.0F, 0.0F);
			cube_r64.cubeList.add(new ModelBox(cube_r64, 12, 45, 6.0F, -2.0F, 15.0F, 1, 2, 1, 0.0F, true));
			cube_r65 = new ModelRenderer(this);
			cube_r65.setRotationPoint(-4.5F, 1.8125F, -15.1875F);
			Foot4.addChild(cube_r65);
			setRotationAngle(cube_r65, 0.0F, 0.0F, 0.0F);
			cube_r65.cubeList.add(new ModelBox(cube_r65, 76, 74, 2.0F, -2.0F, 8.0F, 5, 2, 8, 0.0F, true));
		
			headFlamed = new ModelRenderer(this);
			headFlamed.setRotationPoint(0.0F, 3.0F, -6.0F);
			
	
			cube_r66 = new ModelRenderer(this);
			cube_r66.setRotationPoint(6.0F, 2.75F, 4.0F);
			headFlamed.addChild(cube_r66);
			setRotationAngle(cube_r66, 0.0F, 0.0F, 0.0F);
			cube_r66.cubeList.add(new ModelBox(cube_r66, 45, 43, -11.0F, -8.0F, -14.0F, 10, 2, 1, -0.01F, false));
	
			cube_r67 = new ModelRenderer(this);
			cube_r67.setRotationPoint(6.0F, 9.75F, 4.25F);
			headFlamed.addChild(cube_r67);
			setRotationAngle(cube_r67, 0.0F, 0.0F, 0.0F);
			cube_r67.cubeList.add(new ModelBox(cube_r67, 57, 20, -11.0F, -15.0F, -14.0F, 10, 10, 11, -0.01F, false));
	
			Ears2 = new ModelRenderer(this);
			Ears2.setRotationPoint(11.75F, 5.25F, 0.25F);
			headFlamed.addChild(Ears2);
			
	
			RightEar2 = new ModelRenderer(this);
			RightEar2.setRotationPoint(-12.75F, 6.0F, -11.0F);
			Ears2.addChild(RightEar2);
			setRotationAngle(RightEar2, -0.5744F, -0.3332F, -0.468F);
			RightEar2.cubeList.add(new ModelBox(RightEar2, 0, 8, 3.5F, -18.5F, -8.25F, 2, 6, 1, -0.01F, false));
			RightEar2.cubeList.add(new ModelBox(RightEar2, 0, 0, 4.3284F, -18.5F, -8.25F, 2, 6, 1, -0.01F, false));
	
			cube_r68 = new ModelRenderer(this);
			cube_r68.setRotationPoint(16.4047F, -0.6456F, 0.0F);
			RightEar2.addChild(cube_r68);
			setRotationAngle(cube_r68, 0.0F, 0.0F, -0.7854F);
			cube_r68.cubeList.add(new ModelBox(cube_r68, 28, 70, 3.5F, -21.75F, -8.25F, 2, 2, 1, -0.01F, false));
	
			LeftEar2 = new ModelRenderer(this);
			LeftEar2.setRotationPoint(-10.75F, 6.0F, -11.0F);
			Ears2.addChild(LeftEar2);
			setRotationAngle(LeftEar2, -0.5744F, 0.3332F, 0.468F);
			LeftEar2.cubeList.add(new ModelBox(LeftEar2, 0, 8, -5.5F, -18.5F, -8.25F, 2, 6, 1, -0.01F, true));
			LeftEar2.cubeList.add(new ModelBox(LeftEar2, 0, 0, -6.3284F, -18.5F, -8.25F, 2, 6, 1, -0.01F, true));
	
			cube_r69 = new ModelRenderer(this);
			cube_r69.setRotationPoint(-16.4047F, -0.6456F, 0.0F);
			LeftEar2.addChild(cube_r69);
			setRotationAngle(cube_r69, 0.0F, 0.0F, 0.7854F);
			cube_r69.cubeList.add(new ModelBox(cube_r69, 28, 70, -5.5F, -21.75F, -8.25F, 2, 2, 1, -0.01F, true));
	
			Hair11 = new ModelRenderer(this);
			Hair11.setRotationPoint(11.75F, 5.25F, 0.25F);
			headFlamed.addChild(Hair11);
			
	
			Hair12 = new ModelRenderer(this);
			Hair12.setRotationPoint(3.5F, 10.25F, -19.25F);
			Hair11.addChild(Hair12);
			setRotationAngle(Hair12, -1.1345F, 0.0436F, -1.0036F);
			Hair12.cubeList.add(new ModelBox(Hair12, 28, 66, -1.6651F, -21.6317F, -16.75F, 2, 3, 1, -0.01F, false));
			Hair12.cubeList.add(new ModelBox(Hair12, 42, 60, -0.8367F, -21.6317F, -16.75F, 2, 3, 1, -0.01F, false));
	
			cube_r70 = new ModelRenderer(this);
			cube_r70.setRotationPoint(-15.8072F, -6.0753F, 0.0F);
			Hair12.addChild(cube_r70);
			setRotationAngle(cube_r70, 0.0F, 0.0F, 0.7854F);
			cube_r70.cubeList.add(new ModelBox(cube_r70, 70, 0, -1.0F, -23.0F, -16.75F, 2, 2, 1, -0.01F, false));
	
			Hair13 = new ModelRenderer(this);
			Hair13.setRotationPoint(-27.0F, 10.25F, -19.25F);
			Hair11.addChild(Hair13);
			setRotationAngle(Hair13, -1.1345F, -0.0436F, 1.0036F);
			Hair13.cubeList.add(new ModelBox(Hair13, 28, 66, -0.3349F, -21.6317F, -16.75F, 2, 3, 1, -0.01F, true));
			Hair13.cubeList.add(new ModelBox(Hair13, 42, 60, -1.1633F, -21.6317F, -16.75F, 2, 3, 1, -0.01F, true));
	
			cube_r71 = new ModelRenderer(this);
			cube_r71.setRotationPoint(15.8072F, -6.0753F, 0.0F);
			Hair13.addChild(cube_r71);
			setRotationAngle(cube_r71, 0.0F, 0.0F, -0.7854F);
			cube_r71.cubeList.add(new ModelBox(cube_r71, 70, 0, -1.0F, -23.0F, -16.75F, 2, 2, 1, -0.01F, true));
	
			Hair14 = new ModelRenderer(this);
			Hair14.setRotationPoint(3.5F, 8.25F, -10.5F);
			Hair11.addChild(Hair14);
			setRotationAngle(Hair14, -0.829F, 0.0436F, -0.9163F);
			Hair14.cubeList.add(new ModelBox(Hair14, 0, 60, -1.6651F, -21.6317F, -16.75F, 2, 4, 1, -0.01F, false));
			Hair14.cubeList.add(new ModelBox(Hair14, 6, 42, -0.8367F, -21.6317F, -16.75F, 2, 5, 1, -0.01F, false));
	
			cube_r72 = new ModelRenderer(this);
			cube_r72.setRotationPoint(-15.8072F, -6.0753F, 0.0F);
			Hair14.addChild(cube_r72);
			setRotationAngle(cube_r72, 0.0F, 0.0F, 0.7854F);
			cube_r72.cubeList.add(new ModelBox(cube_r72, 70, 3, -1.0F, -23.0F, -16.75F, 2, 2, 1, -0.01F, false));
	
			Hair15 = new ModelRenderer(this);
			Hair15.setRotationPoint(-27.0F, 8.25F, -10.5F);
			Hair11.addChild(Hair15);
			setRotationAngle(Hair15, -0.829F, -0.0436F, 0.9163F);
			Hair15.cubeList.add(new ModelBox(Hair15, 0, 60, -0.3349F, -21.6317F, -16.75F, 2, 4, 1, -0.01F, true));
			Hair15.cubeList.add(new ModelBox(Hair15, 6, 42, -1.1633F, -21.6317F, -16.75F, 2, 5, 1, -0.01F, true));
	
			cube_r73 = new ModelRenderer(this);
			cube_r73.setRotationPoint(15.8072F, -6.0753F, 0.0F);
			Hair15.addChild(cube_r73);
			setRotationAngle(cube_r73, 0.0F, 0.0F, -0.7854F);
			cube_r73.cubeList.add(new ModelBox(cube_r73, 70, 3, -1.0F, -23.0F, -16.75F, 2, 2, 1, -0.01F, true));
	
			Hair16 = new ModelRenderer(this);
			Hair16.setRotationPoint(5.75F, 5.25F, -10.5F);
			Hair11.addChild(Hair16);
			setRotationAngle(Hair16, -0.829F, 0.0436F, -0.9163F);
			Hair16.cubeList.add(new ModelBox(Hair16, 45, 33, -1.6651F, -21.6317F, -16.75F, 2, 4, 1, -0.01F, false));
			Hair16.cubeList.add(new ModelBox(Hair16, 10, 36, -0.8367F, -21.6317F, -16.75F, 2, 5, 1, -0.01F, false));
	
			cube_r74 = new ModelRenderer(this);
			cube_r74.setRotationPoint(-15.8072F, -6.0753F, 0.0F);
			Hair16.addChild(cube_r74);
			setRotationAngle(cube_r74, 0.0F, 0.0F, 0.7854F);
			cube_r74.cubeList.add(new ModelBox(cube_r74, 66, 41, -1.0F, -23.0F, -16.75F, 2, 2, 1, -0.01F, false));
	
			Hair17 = new ModelRenderer(this);
			Hair17.setRotationPoint(-29.25F, 5.25F, -10.5F);
			Hair11.addChild(Hair17);
			setRotationAngle(Hair17, -0.829F, -0.0436F, 0.9163F);
			Hair17.cubeList.add(new ModelBox(Hair17, 45, 33, -0.3349F, -21.6317F, -16.75F, 2, 4, 1, -0.01F, true));
			Hair17.cubeList.add(new ModelBox(Hair17, 10, 36, -1.1633F, -21.6317F, -16.75F, 2, 5, 1, -0.01F, true));
	
			cube_r75 = new ModelRenderer(this);
			cube_r75.setRotationPoint(15.8072F, -6.0753F, 0.0F);
			Hair17.addChild(cube_r75);
			setRotationAngle(cube_r75, 0.0F, 0.0F, -0.7854F);
			cube_r75.cubeList.add(new ModelBox(cube_r75, 66, 41, -1.0F, -23.0F, -16.75F, 2, 2, 1, -0.01F, true));
	
			Hair18 = new ModelRenderer(this);
			Hair18.setRotationPoint(-9.0F, -34.75F, 12.0F);
			Hair11.addChild(Hair18);
			setRotationAngle(Hair18, -1.1781F, 0.0F, 0.0F);
			Hair18.cubeList.add(new ModelBox(Hair18, 9, 8, -3.3358F, 24.2726F, 14.5601F, 2, 5, 1, -0.01F, false));
			Hair18.cubeList.add(new ModelBox(Hair18, 9, 0, -4.1642F, 24.2726F, 14.5601F, 2, 5, 1, -0.01F, false));
	
			cube_r76 = new ModelRenderer(this);
			cube_r76.setRotationPoint(12.8063F, 39.8289F, 31.3101F);
			Hair18.addChild(cube_r76);
			setRotationAngle(cube_r76, 0.0F, 0.0F, -0.7854F);
			cube_r76.cubeList.add(new ModelBox(cube_r76, 0, 45, -0.6569F, -23.3431F, -14.6549F, 2, 2, 1, -0.01F, false));
			cube_r76.cubeList.add(new ModelBox(cube_r76, 45, 38, -3.1318F, -25.8179F, -14.6549F, 2, 2, 1, -0.01F, false));
			cube_r76.cubeList.add(new ModelBox(cube_r76, 35, 62, -2.6433F, -23.8315F, -15.3642F, 2, 2, 1, -0.01F, false));
			cube_r76.cubeList.add(new ModelBox(cube_r76, 0, 65, -1.0F, -23.0F, -16.75F, 2, 2, 1, -0.01F, false));
	
			cube_r77 = new ModelRenderer(this);
			cube_r77.setRotationPoint(-18.3063F, 39.8289F, 31.3101F);
			Hair18.addChild(cube_r77);
			setRotationAngle(cube_r77, 0.0F, 0.0F, 0.7854F);
			cube_r77.cubeList.add(new ModelBox(cube_r77, 45, 38, 1.1318F, -25.8179F, -14.6549F, 2, 2, 1, -0.01F, true));
			cube_r77.cubeList.add(new ModelBox(cube_r77, 35, 62, 0.6433F, -23.8315F, -15.3642F, 2, 2, 1, -0.01F, true));
	
			Hair19 = new ModelRenderer(this);
			Hair19.setRotationPoint(4.25F, 9.75F, -10.5F);
			Hair11.addChild(Hair19);
			setRotationAngle(Hair19, -0.829F, -0.3491F, -0.9163F);
			Hair19.cubeList.add(new ModelBox(Hair19, 52, 0, -1.6651F, -21.6317F, -16.75F, 2, 4, 1, -0.01F, false));
			Hair19.cubeList.add(new ModelBox(Hair19, 0, 39, -0.8367F, -21.6317F, -16.75F, 2, 5, 1, -0.01F, false));
	
			cube_r78 = new ModelRenderer(this);
			cube_r78.setRotationPoint(-15.8072F, -6.0753F, 0.0F);
			Hair19.addChild(cube_r78);
			setRotationAngle(cube_r78, 0.0F, 0.0F, 0.7854F);
			cube_r78.cubeList.add(new ModelBox(cube_r78, 68, 17, -1.0F, -23.0F, -16.75F, 2, 2, 1, -0.01F, false));
	
			Hair20 = new ModelRenderer(this);
			Hair20.setRotationPoint(-27.75F, 9.75F, -10.5F);
			Hair11.addChild(Hair20);
			setRotationAngle(Hair20, -0.829F, 0.3491F, 0.9163F);
			Hair20.cubeList.add(new ModelBox(Hair20, 52, 0, -0.3349F, -21.6317F, -16.75F, 2, 4, 1, -0.01F, true));
			Hair20.cubeList.add(new ModelBox(Hair20, 0, 39, -1.1633F, -21.6317F, -16.75F, 2, 5, 1, -0.01F, true));
	
			cube_r79 = new ModelRenderer(this);
			cube_r79.setRotationPoint(15.8072F, -6.0753F, 0.0F);
			Hair20.addChild(cube_r79);
			setRotationAngle(cube_r79, 0.0F, 0.0F, -0.7854F);
			cube_r79.cubeList.add(new ModelBox(cube_r79, 68, 17, -1.0F, -23.0F, -16.75F, 2, 2, 1, -0.01F, true));
	
			TopMouth2 = new ModelRenderer(this);
			TopMouth2.setRotationPoint(-0.25F, 20.5F, 14.25F);
			headFlamed.addChild(TopMouth2);
			
	
			cube_r80 = new ModelRenderer(this);
			cube_r80.setRotationPoint(7.5815F, -13.5F, -19.9805F);
			TopMouth2.addChild(cube_r80);
			setRotationAngle(cube_r80, 0.0873F, 0.1309F, 0.0F);
			cube_r80.cubeList.add(new ModelBox(cube_r80, 88, 0, -5.0F, -8.0F, -10.0F, 2, 3, 7, -0.01F, true));
	
			cube_r81 = new ModelRenderer(this);
			cube_r81.setRotationPoint(-7.0815F, -13.5F, -19.9805F);
			TopMouth2.addChild(cube_r81);
			setRotationAngle(cube_r81, 0.0873F, -0.1309F, 0.0F);
			cube_r81.cubeList.add(new ModelBox(cube_r81, 88, 0, 3.0F, -8.0F, -10.0F, 2, 3, 7, -0.01F, false));
	
			cube_r82 = new ModelRenderer(this);
			cube_r82.setRotationPoint(0.25F, -20.4088F, -30.0418F);
			TopMouth2.addChild(cube_r82);
			setRotationAngle(cube_r82, 0.1745F, 0.0F, 0.0F);
			cube_r82.cubeList.add(new ModelBox(cube_r82, 69, 7, -3.0F, -0.1988F, -0.1005F, 6, 3, 7, -0.01F, false));
	
			nose2 = new ModelRenderer(this);
			nose2.setRotationPoint(6.25F, -13.5F, -20.75F);
			TopMouth2.addChild(nose2);
			
	
			cube_r83 = new ModelRenderer(this);
			cube_r83.setRotationPoint(0.0F, 0.0F, 0.0F);
			nose2.addChild(cube_r83);
			setRotationAngle(cube_r83, 0.0873F, 0.0F, 0.0F);
			cube_r83.cubeList.add(new ModelBox(cube_r83, 55, 46, -7.0F, -8.0F, -9.0F, 2, 1, 1, -0.01F, false));
	
			/*UpperTeeth2 = new ModelRenderer(this);
			UpperTeeth2.setRotationPoint(0.0F, -0.5F, -9.25F);
			TopMouth2.addChild(UpperTeeth2);
			
	
			cube_r84 = new ModelRenderer(this);
			cube_r84.setRotationPoint(3.556F, -17.25F, -1.7657F);
			UpperTeeth2.addChild(cube_r84);
			setRotationAngle(cube_r84, 3.1416F, 0.0F, -3.1416F);
			cube_r84.cubeList.add(new ModelBox(cube_r84, 0, 31, 0.807F, -0.5F, 18.4824F, 1, 2, 0, -0.01F, false));
	
			cube_r85 = new ModelRenderer(this);
			cube_r85.setRotationPoint(16.2956F, -17.0F, -19.249F);
			UpperTeeth2.addChild(cube_r85);
			setRotationAngle(cube_r85, 0.0F, -1.5272F, 0.0F);
			cube_r85.cubeList.add(new ModelBox(cube_r85, 45, 46, -1.807F, -0.5F, 18.4824F, 5, 1, 0, -0.01F, false));
	
			cube_r86 = new ModelRenderer(this);
			cube_r86.setRotationPoint(-15.7956F, -17.0F, -19.249F);
			UpperTeeth2.addChild(cube_r86);
			setRotationAngle(cube_r86, 0.0F, 1.5272F, 0.0F);
			cube_r86.cubeList.add(new ModelBox(cube_r86, 45, 47, -3.193F, -0.5F, 18.4824F, 5, 1, 0, -0.01F, false));
	
			cube_r87 = new ModelRenderer(this);
			cube_r87.setRotationPoint(0.556F, -17.0F, -1.7657F);
			UpperTeeth2.addChild(cube_r87);
			setRotationAngle(cube_r87, 3.1416F, 0.0F, -3.1416F);
			cube_r87.cubeList.add(new ModelBox(cube_r87, 9, 6, -1.193F, -0.5F, 18.4824F, 3, 1, 0, -0.01F, false));
	
			cube_r88 = new ModelRenderer(this);
			cube_r88.setRotationPoint(-0.444F, -17.25F, -1.7657F);
			UpperTeeth2.addChild(cube_r88);
			setRotationAngle(cube_r88, 3.1416F, 0.0F, -3.1416F);
			cube_r88.cubeList.add(new ModelBox(cube_r88, 14, 31, 0.807F, -0.5F, 18.4824F, 1, 2, 0, -0.01F, false));*/
	
			Jaw2 = new ModelRenderer(this);
			Jaw2.setRotationPoint(-0.0749F, 3.6113F, -9.4922F);
			headFlamed.addChild(Jaw2);
			setRotationAngle(Jaw2, 0.5236F, 0.0F, 0.0F);
			
	
			cube_r89 = new ModelRenderer(this);
			cube_r89.setRotationPoint(6.0749F, 8.1425F, -10.4545F);
			Jaw2.addChild(cube_r89);
			setRotationAngle(cube_r89, 0.1745F, 0.0F, 0.0F);
			cube_r89.cubeList.add(new ModelBox(cube_r89, 20, 62, -9.0F, -6.0F, 9.0F, 6, 1, 3, -0.01F, false));
	
			cube_r90 = new ModelRenderer(this);
			cube_r90.setRotationPoint(6.0749F, 9.3049F, 2.6281F);
			Jaw2.addChild(cube_r90);
			setRotationAngle(cube_r90, -0.3054F, 0.0F, 0.0F);
			cube_r90.cubeList.add(new ModelBox(cube_r90, 0, 34, -9.0F, -6.0F, -10.0F, 6, 1, 1, -0.01F, false));
	
			cube_r91 = new ModelRenderer(this);
			cube_r91.setRotationPoint(6.0749F, 5.9394F, 5.9459F);
			Jaw2.addChild(cube_r91);
			setRotationAngle(cube_r91, 0.0873F, 0.0F, 0.0F);
			cube_r91.cubeList.add(new ModelBox(cube_r91, 0, 31, -9.0F, -6.0F, -10.0F, 6, 1, 2, -0.01F, false));
	
			cube_r92 = new ModelRenderer(this);
			cube_r92.setRotationPoint(-6.8275F, 5.6387F, 4.6908F);
			Jaw2.addChild(cube_r92);
			setRotationAngle(cube_r92, 0.0873F, -0.0873F, 0.0F);
			cube_r92.cubeList.add(new ModelBox(cube_r92, 0, 0, 3.0F, -6.0F, -10.0F, 1, 1, 7, -0.01F, false));
	
			cube_r93 = new ModelRenderer(this);
			cube_r93.setRotationPoint(6.9773F, 5.6387F, 4.6908F);
			Jaw2.addChild(cube_r93);
			setRotationAngle(cube_r93, 0.0873F, 0.0873F, 0.0F);
			cube_r93.cubeList.add(new ModelBox(cube_r93, 0, 8, -4.0F, -6.0F, -10.0F, 1, 1, 7, -0.01F, false));
	
			cube_r94 = new ModelRenderer(this);
			cube_r94.setRotationPoint(6.0749F, 5.6387F, 4.9922F);
			Jaw2.addChild(cube_r94);
			setRotationAngle(cube_r94, 0.0873F, 0.0F, 0.0F);
			cube_r94.cubeList.add(new ModelBox(cube_r94, 69, 84, -9.0F, -6.0F, -10.0F, 6, 1, 7, -0.01F, false));
	
			bone5 = new ModelRenderer(this);
			bone5.setRotationPoint(8.2288F, -3.2172F, 4.8801F);
			Jaw2.addChild(bone5);
			
	
			cube_r95 = new ModelRenderer(this);
			cube_r95.setRotationPoint(0.0F, 0.0F, 0.0F);
			bone5.addChild(cube_r95);
			setRotationAngle(cube_r95, 0.1809F, 0.1538F, -0.7744F);
			cube_r95.cubeList.add(new ModelBox(cube_r95, 40, 64, -9.0F, -6.0F, -10.0F, 1, 1, 1, -0.01F, false));
	
			cube_r96 = new ModelRenderer(this);
			cube_r96.setRotationPoint(0.0181F, 1.7427F, -0.2458F);
			bone5.addChild(cube_r96);
			setRotationAngle(cube_r96, 0.0564F, 0.0308F, -0.7859F);
			cube_r96.cubeList.add(new ModelBox(cube_r96, 44, 64, -9.0F, -6.0F, -10.0F, 1, 1, 1, -0.01F, false));
	
			bone6 = new ModelRenderer(this);
			bone6.setRotationPoint(9.2288F, -3.2172F, 4.8801F);
			Jaw2.addChild(bone6);
			
	
			cube_r97 = new ModelRenderer(this);
			cube_r97.setRotationPoint(0.0F, 0.0F, 0.0F);
			bone6.addChild(cube_r97);
			setRotationAngle(cube_r97, 0.1809F, 0.1538F, -0.7744F);
			cube_r97.cubeList.add(new ModelBox(cube_r97, 40, 64, -9.0F, -6.0F, -10.0F, 1, 1, 1, -0.01F, false));
	
			cube_r98 = new ModelRenderer(this);
			cube_r98.setRotationPoint(0.0181F, 1.7427F, -0.2458F);
			bone6.addChild(cube_r98);
			setRotationAngle(cube_r98, 0.0564F, 0.0308F, -0.7859F);
			cube_r98.cubeList.add(new ModelBox(cube_r98, 44, 64, -9.0F, -6.0F, -10.0F, 1, 1, 1, -0.01F, false));
	
			bone7 = new ModelRenderer(this);
			bone7.setRotationPoint(10.2288F, -3.2172F, 4.8801F);
			Jaw2.addChild(bone7);
			
	
			cube_r99 = new ModelRenderer(this);
			cube_r99.setRotationPoint(0.0F, 0.0F, 0.0F);
			bone7.addChild(cube_r99);
			setRotationAngle(cube_r99, 0.1809F, 0.1538F, -0.7744F);
			cube_r99.cubeList.add(new ModelBox(cube_r99, 40, 64, -9.0F, -6.0F, -10.0F, 1, 1, 1, -0.01F, false));
	
			cube_r100 = new ModelRenderer(this);
			cube_r100.setRotationPoint(0.0181F, 1.7427F, -0.2458F);
			bone7.addChild(cube_r100);
			setRotationAngle(cube_r100, 0.0564F, 0.0308F, -0.7859F);
			cube_r100.cubeList.add(new ModelBox(cube_r100, 44, 64, -9.0F, -6.0F, -10.0F, 1, 1, 1, -0.01F, false));
	
			bone8 = new ModelRenderer(this);
			bone8.setRotationPoint(11.2288F, -3.2172F, 4.8801F);
			Jaw2.addChild(bone8);
			
	
			cube_r101 = new ModelRenderer(this);
			cube_r101.setRotationPoint(0.0F, 0.0F, 0.0F);
			bone8.addChild(cube_r101);
			setRotationAngle(cube_r101, 0.1809F, 0.1538F, -0.7744F);
			cube_r101.cubeList.add(new ModelBox(cube_r101, 40, 64, -9.0F, -6.0F, -10.0F, 1, 1, 1, -0.01F, false));
	
			cube_r102 = new ModelRenderer(this);
			cube_r102.setRotationPoint(0.0181F, 1.7427F, -0.2458F);
			bone8.addChild(cube_r102);
			setRotationAngle(cube_r102, 0.0564F, 0.0308F, -0.7859F);
			cube_r102.cubeList.add(new ModelBox(cube_r102, 44, 64, -9.0F, -6.0F, -10.0F, 1, 1, 1, -0.01F, false));
	
			/*LowerTeeth2 = new ModelRenderer(this);
			LowerTeeth2.setRotationPoint(-18.1751F, 15.1387F, -4.0078F);
			Jaw2.addChild(LowerTeeth2);
			setRotationAngle(LowerTeeth2, -3.1416F, -1.5272F, 3.1416F);
			LowerTeeth2.cubeList.add(new ModelBox(LowerTeeth2, 60, 15, -2.1529F, -15.5F, -20.6758F, 4, 1, 0, -0.01F, false));
			LowerTeeth2.cubeList.add(new ModelBox(LowerTeeth2, 52, 15, -1.9348F, -15.5F, -15.6805F, 4, 1, 0, -0.01F, false));
	
			cube_r103 = new ModelRenderer(this);
			cube_r103.setRotationPoint(16.4522F, -15.25F, -18.291F);
			LowerTeeth2.addChild(cube_r103);
			setRotationAngle(cube_r103, 0.0F, -1.5272F, 0.0F);
			cube_r103.cubeList.add(new ModelBox(cube_r103, 52, 14, -3.193F, -0.25F, 18.4824F, 5, 1, 0, -0.01F, false));*/
	
			bodyFlamed = new ModelRenderer(this);
			bodyFlamed.setRotationPoint(0.0F, 3.0F, 0.0F);
			
	
			cube_r104 = new ModelRenderer(this);
			cube_r104.setRotationPoint(6.0F, 8.0F, 4.25F);
			bodyFlamed.addChild(cube_r104);
			setRotationAngle(cube_r104, 0.0F, 0.0F, 0.0F);
			cube_r104.cubeList.add(new ModelBox(cube_r104, 0, 31, -13.0F, -12.0F, 5.0F, 14, 12, 17, -0.01F, false));
	
			cube_r105 = new ModelRenderer(this);
			cube_r105.setRotationPoint(6.0F, 8.0F, -8.0F);
			bodyFlamed.addChild(cube_r105);
			setRotationAngle(cube_r105, -0.0436F, 0.0F, 0.0F);
			cube_r105.cubeList.add(new ModelBox(cube_r105, 48, 46, -14.0F, -14.0F, 14.0F, 16, 14, 14, -0.01F, false));
	
			cube_r106 = new ModelRenderer(this);
			cube_r106.setRotationPoint(6.0F, 8.0F, -13.5F);
			bodyFlamed.addChild(cube_r106);
			setRotationAngle(cube_r106, -0.0873F, 0.0F, 0.0F);
			cube_r106.cubeList.add(new ModelBox(cube_r106, 0, 0, -15.0F, -16.0F, 5.0F, 18, 15, 16, -0.01F, false));
	
			TailFlamed[0][0] = new ModelRenderer(this);
			TailFlamed[0][0].setRotationPoint(-3.0F, 1.0F, 26.0F);
			bodyFlamed.addChild(TailFlamed[0][0]);
			setRotationAngle(TailFlamed[0][0], -1.309F, -0.7854F, 0.0F);
			TailFlamed[0][0].cubeList.add(new ModelBox(TailFlamed[0][0], 52, 0, -3.0F, -7.0F, -3.0F, 6, 8, 6, -0.01F, false));
	
			TailFlamed[0][1] = new ModelRenderer(this);
			TailFlamed[0][1].setRotationPoint(0.0F, -6.0F, 0.0F);
			TailFlamed[0][0].addChild(TailFlamed[0][1]);
			setRotationAngle(TailFlamed[0][1], 0.2618F, 0.0F, 0.0873F);
			TailFlamed[0][1].cubeList.add(new ModelBox(TailFlamed[0][1], 52, 0, -3.0F, -7.0F, -3.0F, 6, 8, 6, 0.19F, false));
	
			TailFlamed[0][2] = new ModelRenderer(this);
			TailFlamed[0][2].setRotationPoint(0.0F, -6.0F, 0.0F);
			TailFlamed[0][1].addChild(TailFlamed[0][2]);
			setRotationAngle(TailFlamed[0][2], 0.2618F, 0.0F, 0.0873F);
			TailFlamed[0][2].cubeList.add(new ModelBox(TailFlamed[0][2], 52, 0, -3.0F, -7.0F, -3.0F, 6, 8, 6, 0.39F, false));
	
			TailFlamed[0][3] = new ModelRenderer(this);
			TailFlamed[0][3].setRotationPoint(0.0F, -6.0F, 0.0F);
			TailFlamed[0][2].addChild(TailFlamed[0][3]);
			setRotationAngle(TailFlamed[0][3], 0.2618F, 0.0F, 0.0873F);
			TailFlamed[0][3].cubeList.add(new ModelBox(TailFlamed[0][3], 52, 0, -3.0F, -7.0F, -3.0F, 6, 8, 6, 0.49F, false));
	
			TailFlamed[0][4] = new ModelRenderer(this);
			TailFlamed[0][4].setRotationPoint(0.0F, -6.0F, 0.0F);
			TailFlamed[0][3].addChild(TailFlamed[0][4]);
			setRotationAngle(TailFlamed[0][4], -0.2618F, 0.0F, 0.0873F);
			TailFlamed[0][4].cubeList.add(new ModelBox(TailFlamed[0][4], 52, 0, -3.0F, -7.0F, -3.0F, 6, 8, 6, 0.29F, false));
	
			TailFlamed[0][5] = new ModelRenderer(this);
			TailFlamed[0][5].setRotationPoint(0.0F, -6.0F, 0.0F);
			TailFlamed[0][4].addChild(TailFlamed[0][5]);
			setRotationAngle(TailFlamed[0][5], -0.2618F, 0.0F, 0.0873F);
			TailFlamed[0][5].cubeList.add(new ModelBox(TailFlamed[0][5], 52, 0, -3.0F, -7.0F, -3.0F, 6, 8, 6, 0.09F, false));
	
			TailFlamed[0][6] = new ModelRenderer(this);
			TailFlamed[0][6].setRotationPoint(0.0F, -6.0F, 0.0F);
			TailFlamed[0][5].addChild(TailFlamed[0][6]);
			setRotationAngle(TailFlamed[0][6], -0.2618F, 0.0F, 0.0873F);
			TailFlamed[0][6].cubeList.add(new ModelBox(TailFlamed[0][6], 52, 0, -3.0F, -7.0F, -3.0F, 6, 8, 6, -0.31F, false));
	
			TailFlamed[0][7] = new ModelRenderer(this);
			TailFlamed[0][7].setRotationPoint(0.0F, -6.0F, 0.0F);
			TailFlamed[0][6].addChild(TailFlamed[0][7]);
			setRotationAngle(TailFlamed[0][7], -0.2618F, 0.0F, 0.0873F);
			TailFlamed[0][7].cubeList.add(new ModelBox(TailFlamed[0][7], 52, 0, -3.0F, -7.0F, -3.0F, 6, 8, 6, -0.81F, false));
	
			TailFlamed[1][0] = new ModelRenderer(this);
			TailFlamed[1][0].setRotationPoint(3.0F, 1.0F, 26.0F);
			bodyFlamed.addChild(TailFlamed[1][0]);
			setRotationAngle(TailFlamed[1][0], -1.309F, 0.7854F, 0.0F);
			TailFlamed[1][0].cubeList.add(new ModelBox(TailFlamed[1][0], 52, 0, -3.0F, -7.0F, -3.0F, 6, 8, 6, -0.01F, true));
	
			TailFlamed[1][1] = new ModelRenderer(this);
			TailFlamed[1][1].setRotationPoint(0.0F, -6.0F, 0.0F);
			TailFlamed[1][0].addChild(TailFlamed[1][1]);
			setRotationAngle(TailFlamed[1][1], 0.2618F, 0.0F, -0.0873F);
			TailFlamed[1][1].cubeList.add(new ModelBox(TailFlamed[1][1], 52, 0, -3.0F, -7.0F, -3.0F, 6, 8, 6, 0.19F, true));
	
			TailFlamed[1][2] = new ModelRenderer(this);
			TailFlamed[1][2].setRotationPoint(0.0F, -6.0F, 0.0F);
			TailFlamed[1][1].addChild(TailFlamed[1][2]);
			setRotationAngle(TailFlamed[1][2], 0.2618F, 0.0F, -0.0873F);
			TailFlamed[1][2].cubeList.add(new ModelBox(TailFlamed[1][2], 52, 0, -3.0F, -7.0F, -3.0F, 6, 8, 6, 0.39F, true));
	
			TailFlamed[1][3] = new ModelRenderer(this);
			TailFlamed[1][3].setRotationPoint(0.0F, -6.0F, 0.0F);
			TailFlamed[1][2].addChild(TailFlamed[1][3]);
			setRotationAngle(TailFlamed[1][3], 0.2618F, 0.0F, -0.0873F);
			TailFlamed[1][3].cubeList.add(new ModelBox(TailFlamed[1][3], 52, 0, -3.0F, -7.0F, -3.0F, 6, 8, 6, 0.49F, true));
	
			TailFlamed[1][4] = new ModelRenderer(this);
			TailFlamed[1][4].setRotationPoint(0.0F, -6.0F, 0.0F);
			TailFlamed[1][3].addChild(TailFlamed[1][4]);
			setRotationAngle(TailFlamed[1][4], -0.2618F, 0.0F, -0.0873F);
			TailFlamed[1][4].cubeList.add(new ModelBox(TailFlamed[1][4], 52, 0, -3.0F, -7.0F, -3.0F, 6, 8, 6, 0.29F, true));
	
			TailFlamed[1][5] = new ModelRenderer(this);
			TailFlamed[1][5].setRotationPoint(0.0F, -6.0F, 0.0F);
			TailFlamed[1][4].addChild(TailFlamed[1][5]);
			setRotationAngle(TailFlamed[1][5], -0.2618F, 0.0F, -0.0873F);
			TailFlamed[1][5].cubeList.add(new ModelBox(TailFlamed[1][5], 52, 0, -3.0F, -7.0F, -3.0F, 6, 8, 6, 0.09F, true));
	
			TailFlamed[1][6] = new ModelRenderer(this);
			TailFlamed[1][6].setRotationPoint(0.0F, -6.0F, 0.0F);
			TailFlamed[1][5].addChild(TailFlamed[1][6]);
			setRotationAngle(TailFlamed[1][6], -0.2618F, 0.0F, -0.0873F);
			TailFlamed[1][6].cubeList.add(new ModelBox(TailFlamed[1][6], 52, 0, -3.0F, -7.0F, -3.0F, 6, 8, 6, -0.31F, true));
	
			TailFlamed[1][7] = new ModelRenderer(this);
			TailFlamed[1][7].setRotationPoint(0.0F, -6.0F, 0.0F);
			TailFlamed[1][6].addChild(TailFlamed[1][7]);
			setRotationAngle(TailFlamed[1][7], -0.2618F, 0.0F, -0.0873F);
			TailFlamed[1][7].cubeList.add(new ModelBox(TailFlamed[1][7], 52, 0, -3.0F, -7.0F, -3.0F, 6, 8, 6, -0.81F, true));
	
			leg1Flamed = new ModelRenderer(this);
			leg1Flamed.setRotationPoint(-4.75F, 4.0F, -1.0F);
			
	
			Joint10 = new ModelRenderer(this);
			Joint10.setRotationPoint(0.0F, -2.0F, -1.0F);
			leg1Flamed.addChild(Joint10);
			setRotationAngle(Joint10, 0.1745F, 0.0F, 0.0F);
			
	
			cube_r107 = new ModelRenderer(this);
			cube_r107.setRotationPoint(2.0F, 12.0F, -11.25F);
			Joint10.addChild(cube_r107);
			setRotationAngle(cube_r107, -0.0436F, 0.0F, 0.0F);
			cube_r107.cubeList.add(new ModelBox(cube_r107, 28, 66, -8.0F, -12.0F, 7.0F, 6, 14, 8, -0.01F, false));
	
			Joint11 = new ModelRenderer(this);
			Joint11.setRotationPoint(-2.875F, 15.25F, 0.75F);
			Joint10.addChild(Joint11);
			setRotationAngle(Joint11, -0.5236F, 0.0F, 0.0F);
			
	
			cube_r108 = new ModelRenderer(this);
			cube_r108.setRotationPoint(4.375F, 5.75F, -14.75F);
			Joint11.addChild(cube_r108);
			setRotationAngle(cube_r108, 0.0F, 0.0F, 0.0F);
			cube_r108.cubeList.add(new ModelBox(cube_r108, 0, 82, -7.0F, -8.0F, 11.0F, 5, 10, 5, -0.01F, false));
	
			Foot5 = new ModelRenderer(this);
			Foot5.setRotationPoint(-0.125F, 5.9375F, 0.4375F);
			Joint11.addChild(Foot5);
			setRotationAngle(Foot5, 0.3491F, 0.0F, 0.0F);
			
	
			cube_r109 = new ModelRenderer(this);
			cube_r109.setRotationPoint(6.5F, 2.0625F, -22.4375F);
			Foot5.addChild(cube_r109);
			setRotationAngle(cube_r109, 0.0F, 0.0F, 0.0F);
			cube_r109.cubeList.add(new ModelBox(cube_r109, 6, 39, -7.0F, -2.0F, 15.0F, 1, 2, 1, -0.01F, false));
	
			cube_r110 = new ModelRenderer(this);
			cube_r110.setRotationPoint(8.0F, 2.0625F, -22.4375F);
			Foot5.addChild(cube_r110);
			setRotationAngle(cube_r110, 0.0F, 0.0F, 0.0F);
			cube_r110.cubeList.add(new ModelBox(cube_r110, 12, 42, -7.0F, -2.0F, 15.0F, 1, 2, 1, -0.01F, false));
	
			cube_r111 = new ModelRenderer(this);
			cube_r111.setRotationPoint(5.0F, 2.0625F, -22.4375F);
			Foot5.addChild(cube_r111);
			setRotationAngle(cube_r111, 0.0F, 0.0F, 0.0F);
			cube_r111.cubeList.add(new ModelBox(cube_r111, 12, 45, -7.0F, -2.0F, 15.0F, 1, 2, 1, -0.01F, false));
	
			cube_r112 = new ModelRenderer(this);
			cube_r112.setRotationPoint(4.5F, 1.8125F, -15.1875F);
			Foot5.addChild(cube_r112);
			setRotationAngle(cube_r112, 0.0F, 0.0F, 0.0F);
			cube_r112.cubeList.add(new ModelBox(cube_r112, 76, 74, -7.0F, -2.0F, 8.0F, 5, 2, 8, -0.01F, false));
	
			leg2Flamed = new ModelRenderer(this);
			leg2Flamed.setRotationPoint(4.75F, 4.0F, -1.0F);
			
	
			Joint12 = new ModelRenderer(this);
			Joint12.setRotationPoint(0.0F, -2.0F, -1.0F);
			leg2Flamed.addChild(Joint12);
			setRotationAngle(Joint12, 0.1745F, 0.0F, 0.0F);
			
	
			cube_r113 = new ModelRenderer(this);
			cube_r113.setRotationPoint(-2.0F, 12.0F, -11.25F);
			Joint12.addChild(cube_r113);
			setRotationAngle(cube_r113, -0.0436F, 0.0F, 0.0F);
			cube_r113.cubeList.add(new ModelBox(cube_r113, 28, 66, 2.0F, -12.0F, 7.0F, 6, 14, 8, -0.01F, true));
	
			Joint13 = new ModelRenderer(this);
			Joint13.setRotationPoint(2.875F, 15.25F, 0.75F);
			Joint12.addChild(Joint13);
			setRotationAngle(Joint13, -0.5236F, 0.0F, 0.0F);
			
	
			cube_r114 = new ModelRenderer(this);
			cube_r114.setRotationPoint(-4.375F, 5.75F, -14.75F);
			Joint13.addChild(cube_r114);
			setRotationAngle(cube_r114, 0.0F, 0.0F, 0.0F);
			cube_r114.cubeList.add(new ModelBox(cube_r114, 0, 82, 2.0F, -8.0F, 11.0F, 5, 10, 5, -0.01F, true));
	
			Foot6 = new ModelRenderer(this);
			Foot6.setRotationPoint(0.125F, 5.9375F, 0.4375F);
			Joint13.addChild(Foot6);
			setRotationAngle(Foot6, 0.3491F, 0.0F, 0.0F);
			
	
			cube_r115 = new ModelRenderer(this);
			cube_r115.setRotationPoint(-6.5F, 2.0625F, -22.4375F);
			Foot6.addChild(cube_r115);
			setRotationAngle(cube_r115, 0.0F, 0.0F, 0.0F);
			cube_r115.cubeList.add(new ModelBox(cube_r115, 6, 39, 6.0F, -2.0F, 15.0F, 1, 2, 1, -0.01F, true));
	
			cube_r116 = new ModelRenderer(this);
			cube_r116.setRotationPoint(-8.0F, 2.0625F, -22.4375F);
			Foot6.addChild(cube_r116);
			setRotationAngle(cube_r116, 0.0F, 0.0F, 0.0F);
			cube_r116.cubeList.add(new ModelBox(cube_r116, 12, 42, 6.0F, -2.0F, 15.0F, 1, 2, 1, -0.01F, true));
	
			cube_r117 = new ModelRenderer(this);
			cube_r117.setRotationPoint(-5.0F, 2.0625F, -22.4375F);
			Foot6.addChild(cube_r117);
			setRotationAngle(cube_r117, 0.0F, 0.0F, 0.0F);
			cube_r117.cubeList.add(new ModelBox(cube_r117, 12, 45, 6.0F, -2.0F, 15.0F, 1, 2, 1, -0.01F, true));
	
			cube_r118 = new ModelRenderer(this);
			cube_r118.setRotationPoint(-4.5F, 1.8125F, -15.1875F);
			Foot6.addChild(cube_r118);
			setRotationAngle(cube_r118, 0.0F, 0.0F, 0.0F);
			cube_r118.cubeList.add(new ModelBox(cube_r118, 76, 74, 2.0F, -2.0F, 8.0F, 5, 2, 8, -0.01F, true));
	
			leg3Flamed = new ModelRenderer(this);
			leg3Flamed.setRotationPoint(-5.5F, 8.0F, 21.5F);
			
	
			Joint14 = new ModelRenderer(this);
			Joint14.setRotationPoint(0.0F, 0.0F, 0.0F);
			leg3Flamed.addChild(Joint14);
			setRotationAngle(Joint14, -1.5708F, 0.0F, 0.0F);
			
	
			cube_r119 = new ModelRenderer(this);
			cube_r119.setRotationPoint(2.25F, 17.5F, -2.75F);
			Joint14.addChild(cube_r119);
			setRotationAngle(cube_r119, 0.7854F, 0.0F, 0.0F);
			cube_r119.cubeList.add(new ModelBox(cube_r119, 0, 60, -8.0F, -14.3536F, 10.182F, 6, 14, 8, 0.19F, false));
	
			Joint15 = new ModelRenderer(this);
			Joint15.setRotationPoint(1.25F, 17.5F, -2.75F);
			Joint14.addChild(Joint15);
			
	
			cube_r120 = new ModelRenderer(this);
			cube_r120.setRotationPoint(-10.0F, 27.5103F, 8.3355F);
			Joint15.addChild(cube_r120);
			setRotationAngle(cube_r120, -1.1781F, 0.0F, 0.0F);
			cube_r120.cubeList.add(new ModelBox(cube_r120, 56, 74, 3.0F, -24.6324F, -38.7863F, 5, 11, 5, -0.01F, false));
	
			Foot7 = new ModelRenderer(this);
			Foot7.setRotationPoint(-4.5F, -16.9625F, 16.4375F);
			Joint15.addChild(Foot7);
			setRotationAngle(Foot7, 1.5708F, 0.0F, 0.0F);
			
	
			cube_r121 = new ModelRenderer(this);
			cube_r121.setRotationPoint(6.5F, 2.0625F, -22.4375F);
			Foot7.addChild(cube_r121);
			setRotationAngle(cube_r121, 0.0F, 0.0F, 0.0F);
			cube_r121.cubeList.add(new ModelBox(cube_r121, 6, 39, -7.0F, -2.0F, 15.0F, 1, 2, 1, -0.01F, false));
	
			cube_r122 = new ModelRenderer(this);
			cube_r122.setRotationPoint(8.0F, 2.0625F, -22.4375F);
			Foot7.addChild(cube_r122);
			setRotationAngle(cube_r122, 0.0F, 0.0F, 0.0F);
			cube_r122.cubeList.add(new ModelBox(cube_r122, 12, 42, -7.0F, -2.0F, 15.0F, 1, 2, 1, -0.01F, false));
	
			cube_r123 = new ModelRenderer(this);
			cube_r123.setRotationPoint(5.0F, 2.0625F, -22.4375F);
			Foot7.addChild(cube_r123);
			setRotationAngle(cube_r123, 0.0F, 0.0F, 0.0F);
			cube_r123.cubeList.add(new ModelBox(cube_r123, 12, 45, -7.0F, -2.0F, 15.0F, 1, 2, 1, -0.01F, false));
	
			cube_r124 = new ModelRenderer(this);
			cube_r124.setRotationPoint(4.5F, 1.8125F, -15.1875F);
			Foot7.addChild(cube_r124);
			setRotationAngle(cube_r124, 0.0F, 0.0F, 0.0F);
			cube_r124.cubeList.add(new ModelBox(cube_r124, 76, 74, -7.0F, -2.0F, 8.0F, 5, 2, 8, -0.01F, false));
	
			leg4Flamed = new ModelRenderer(this);
			leg4Flamed.setRotationPoint(5.5F, 8.0F, 21.5F);
			
	
			Joint16 = new ModelRenderer(this);
			Joint16.setRotationPoint(0.0F, 0.0F, 0.0F);
			leg4Flamed.addChild(Joint16);
			setRotationAngle(Joint16, -1.5708F, 0.0F, 0.0F);
			
	
			cube_r125 = new ModelRenderer(this);
			cube_r125.setRotationPoint(-2.25F, 17.5F, -2.75F);
			Joint16.addChild(cube_r125);
			setRotationAngle(cube_r125, 0.7854F, 0.0F, 0.0F);
			cube_r125.cubeList.add(new ModelBox(cube_r125, 0, 60, 2.0F, -14.3536F, 10.182F, 6, 14, 8, 0.19F, true));
	
			Joint17 = new ModelRenderer(this);
			Joint17.setRotationPoint(-1.25F, 17.5F, -2.75F);
			Joint16.addChild(Joint17);
			
	
			cube_r126 = new ModelRenderer(this);
			cube_r126.setRotationPoint(10.0F, 27.5103F, 8.3355F);
			Joint17.addChild(cube_r126);
			setRotationAngle(cube_r126, -1.1781F, 0.0F, 0.0F);
			cube_r126.cubeList.add(new ModelBox(cube_r126, 56, 74, -8.0F, -24.6324F, -38.7863F, 5, 11, 5, -0.01F, true));
	
			Foot8 = new ModelRenderer(this);
			Foot8.setRotationPoint(4.5F, -16.9625F, 16.4375F);
			Joint17.addChild(Foot8);
			setRotationAngle(Foot8, 1.5708F, 0.0F, 0.0F);
			
	
			cube_r127 = new ModelRenderer(this);
			cube_r127.setRotationPoint(-6.5F, 2.0625F, -22.4375F);
			Foot8.addChild(cube_r127);
			setRotationAngle(cube_r127, 0.0F, 0.0F, 0.0F);
			cube_r127.cubeList.add(new ModelBox(cube_r127, 6, 39, 6.0F, -2.0F, 15.0F, 1, 2, 1, -0.01F, true));
	
			cube_r128 = new ModelRenderer(this);
			cube_r128.setRotationPoint(-8.0F, 2.0625F, -22.4375F);
			Foot8.addChild(cube_r128);
			setRotationAngle(cube_r128, 0.0F, 0.0F, 0.0F);
			cube_r128.cubeList.add(new ModelBox(cube_r128, 12, 42, 6.0F, -2.0F, 15.0F, 1, 2, 1, -0.01F, true));
	
			cube_r129 = new ModelRenderer(this);
			cube_r129.setRotationPoint(-5.0F, 2.0625F, -22.4375F);
			Foot8.addChild(cube_r129);
			setRotationAngle(cube_r129, 0.0F, 0.0F, 0.0F);
			cube_r129.cubeList.add(new ModelBox(cube_r129, 12, 45, 6.0F, -2.0F, 15.0F, 1, 2, 1, -0.01F, true));
	
			cube_r130 = new ModelRenderer(this);
			cube_r130.setRotationPoint(-4.5F, 1.8125F, -15.1875F);
			Foot8.addChild(cube_r130);
			setRotationAngle(cube_r130, 0.0F, 0.0F, 0.0F);
			cube_r130.cubeList.add(new ModelBox(cube_r130, 76, 74, 2.0F, -2.0F, 8.0F, 5, 2, 8, -0.01F, true));

			for (int i = 0; i < 2; i++) {
				for (int j = 1; j < 8; j++) {
					tailSwayX[i][j] = (rand.nextFloat() * 0.2618F + 0.1745F) * (rand.nextBoolean() ? -1F : 1F);
					tailSwayZ[i][j] = (rand.nextFloat() * 0.1745F + 0.1745F) * (rand.nextBoolean() ? -1F : 1F);
					tailSwayY[i][j] = (rand.nextFloat() * 0.1745F + 0.1745F);
				}
			}
		}

		@Override
		public void render(Entity entity, float f0, float f1, float f2, float f3, float f4, float f5) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(0.0F, 1.5F - 1.5F * MODELSCALE, 0.0F);
			//GlStateManager.translate(0.0F, 0.0F, 0.375F * MODELSCALE);
			GlStateManager.scale(MODELSCALE, MODELSCALE, MODELSCALE);
			//GlStateManager.enableBlend();
			//GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			super.render(entity, f0, f1, f2, f3, f4, f5);
			headFlamed.render(f5);
			bodyFlamed.render(f5);
			leg1Flamed.render(f5);
			leg2Flamed.render(f5);
			leg3Flamed.render(f5);
			leg4Flamed.render(f5);
			this.copyModelAngles(head, eyes);
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			GlStateManager.disableLighting();
			eyes.render(f5);
			GlStateManager.enableLighting();
			//GlStateManager.disableBlend();
			GlStateManager.popMatrix();
		}

		public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
			modelRenderer.rotateAngleX = x;
			modelRenderer.rotateAngleY = y;
			modelRenderer.rotateAngleZ = z;
		}

		@Override
		public void setRotationAngles(float f0, float f1, float f2, float f3, float f4, float f5, Entity e) {
			super.setRotationAngles(f0 * 2.0F / e.height, f1, f2, f3, f4, f5, e);
			body.rotateAngleX -= ((float)Math.PI / 2F);
			this.copyModelAngles(head, headFlamed);
			this.copyModelAngles(body, bodyFlamed);
			this.copyModelAngles(leg1, leg1Flamed);
			this.copyModelAngles(leg2, leg2Flamed);
			this.copyModelAngles(leg3, leg3Flamed);
			this.copyModelAngles(leg4, leg4Flamed);
			for (int i = 0; i < 2; i++) {
				for (int j = 1; j < 8; j++) {
					TailFlamed[i][j].rotateAngleX = Tail[i][j].rotateAngleX = MathHelper.sin((f2 - j) * 0.1F) * tailSwayX[i][j];
					TailFlamed[i][j].rotateAngleZ = Tail[i][j].rotateAngleZ = MathHelper.cos((f2 - j) * 0.1F) * tailSwayZ[i][j];
					TailFlamed[i][j].rotateAngleY = Tail[i][j].rotateAngleY = MathHelper.sin((f2 - j) * 0.1F) * tailSwayY[i][j];
				}
			}
			if (((EntityCustom)e).isShooting()) {
				head.rotateAngleX += -0.1745F;
				eyes.rotateAngleX += -0.1745F;
				Jaw.rotateAngleX = 0.7854F;
				headFlamed.rotateAngleX += -0.1745F;
				Jaw2.rotateAngleX = 0.7854F;
			} else {
				Jaw.rotateAngleX = 0.0F;
				Jaw2.rotateAngleX = 0.0F;
			}
		}

		public void setFlamedVisible(boolean visible) {
			headFlamed.showModel = visible;
			bodyFlamed.showModel = visible;
			leg1Flamed.showModel = visible;
			leg2Flamed.showModel = visible;
			leg3Flamed.showModel = visible;
			leg4Flamed.showModel = visible;
			head.showModel = !visible;
			body.showModel = !visible;
			leg1.showModel = !visible;
			leg2.showModel = !visible;
			leg3.showModel = !visible;
			leg4.showModel = !visible;
			eyes.showModel = !visible;
			//UpperTeeth2.showModel = false;
			//LowerTeeth2.showModel = false;
		}
	}
}
