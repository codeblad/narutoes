
package net.narutomod.item;

import net.narutomod.creativetab.TabModTab;
import net.narutomod.ElementsNarutomodMod;

import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.event.ModelRegistryEvent;

import net.minecraft.item.Item;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;

@ElementsNarutomodMod.ModElement.Tag
public class ItemNinjaArmorSakura1 extends ElementsNarutomodMod.ModElement {
	@GameRegistry.ObjectHolder("narutomod:ninja_armor_sakura_1body")
	public static final Item body = null;
	@GameRegistry.ObjectHolder("narutomod:ninja_armor_sakura_1legs")
	public static final Item legs = null;

	public ItemNinjaArmorSakura1(ElementsNarutomodMod instance) {
		super(instance, 861);
	}

	@Override
	public void initElements() {
		elements.items.add(() -> new ItemNinjaArmor.Base(ItemNinjaArmor.Type.SAKURA_S, EntityEquipmentSlot.CHEST) {
			@Override
			protected ItemNinjaArmor.ArmorData setArmorData(ItemNinjaArmor.Type type, EntityEquipmentSlot slotIn) {
				return new Armor4Slot();
			}

			class Armor4Slot extends ItemNinjaArmor.ArmorData {
				@SideOnly(Side.CLIENT)
				@Override
				protected void init() {
					ItemNinjaArmor.ModelNinjaArmor model1 = new ItemNinjaArmor.ModelNinjaArmor(ItemNinjaArmor.Type.SAKURA_S);
					model1.shirt.showModel = false;
					model1.shirtRightArm.showModel = false;
					model1.shirtLeftArm.showModel = false;
					this.model = model1;
					this.texture = "narutomod:textures/sakurashippudenarmor.png";
				}
				@SideOnly(Side.CLIENT)
				@Override
				public void setSlotVisible() {
					//this.model.bipedHeadwear.showModel = true;
				}
			}
		}.setUnlocalizedName("ninja_armor_sakura_1body").setRegistryName("ninja_armor_sakura_1body").setCreativeTab(TabModTab.tab));
		elements.items.add(() -> new ItemNinjaArmor.Base(ItemNinjaArmor.Type.SAKURA_S, EntityEquipmentSlot.LEGS) {
			@Override
			protected ItemNinjaArmor.ArmorData setArmorData(ItemNinjaArmor.Type type, EntityEquipmentSlot slotIn) {
				return new Armor4Slot();
			}

			class Armor4Slot extends ItemNinjaArmor.ArmorData {
				@SideOnly(Side.CLIENT)
				@Override
				protected void init() {
					ItemNinjaArmor.ModelNinjaArmor model1 = new ItemNinjaArmor.ModelNinjaArmor(ItemNinjaArmor.Type.SAKURA_S);
					model1.vest.showModel = false;
					model1.rightArmVestLayer.showModel = false;
					model1.leftArmVestLayer.showModel = false;
					this.model = model1;
					this.texture = "narutomod:textures/sakurashippudenarmor.png";
				}
				@SideOnly(Side.CLIENT)
				@Override
				public void setSlotVisible() {
					this.model.bipedRightArm.showModel = true;
					this.model.bipedLeftArm.showModel = true;
				}
			}
		}.setUnlocalizedName("ninja_armor_sakura_1legs").setRegistryName("ninja_armor_sakura_1legs").setCreativeTab(TabModTab.tab));
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerModels(ModelRegistryEvent event) {
		ModelLoader.setCustomModelResourceLocation(body, 0, new ModelResourceLocation("narutomod:ninja_armor_sakura_1body", "inventory"));
		ModelLoader.setCustomModelResourceLocation(legs, 0, new ModelResourceLocation("narutomod:ninja_armor_sakura_1legs", "inventory"));
	}
}
