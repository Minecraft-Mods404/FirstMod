package com.danielm.modid;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import static net.minecraft.commands.Commands.literal;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Unit;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class SlimeMod implements ModInitializer {
	public static final String MOD_ID = "slimemod";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	// ─────────────────────────────────────────────
	//  TAGS
	// ─────────────────────────────────────────────

	public static final TagKey<Item> REPAIRS_SLIME_TOOLS = TagKey.create(
			Registries.ITEM,
			id("repairs_slime_tools")
	);

	public static final TagKey<Item> SLIME_ARMOR = TagKey.create(
			Registries.ITEM,
			id("slime_armor")
	);

	// ─────────────────────────────────────────────
	//  MATERIALS  (must come after the tags above)
	// ─────────────────────────────────────────────

	public static final ResourceKey<EquipmentAsset> SLIME_EQUIPMENT_ASSET =
			ResourceKey.create(EquipmentAssets.ROOT_ID, id("slime"));

	public static final ArmorMaterial SLIME_ARMOR_MATERIAL = new ArmorMaterial(
			100,                                    // durability multiplier
			Map.of(ArmorType.HELMET, 3,
					ArmorType.CHESTPLATE, 8,
					ArmorType.LEGGINGS, 6,
					ArmorType.BOOTS, 3),
			22,                                     // enchantability
			SoundEvents.ARMOR_EQUIP_NETHERITE,
			5.0f,                                   // toughness
			0.0f,                                   // knockback resistance
			REPAIRS_SLIME_TOOLS,
			SLIME_EQUIPMENT_ASSET
	);

	public static final ToolMaterial SLIME_TOOL_MATERIAL = new ToolMaterial(
			BlockTags.INCORRECT_FOR_NETHERITE_TOOL,
			100000000, 6.0f, 5.0f, 22,
			REPAIRS_SLIME_TOOLS
	);

	// ─────────────────────────────────────────────
	//  ITEMS  (key always immediately above its item)
	// ─────────────────────────────────────────────

	public static final ResourceKey<Item> SUPER_SLIME_KEY = ResourceKey.create(
			Registries.ITEM, id("super_slime"));

	public static final Item SUPER_SLIME = new Item(new Item.Properties()
			.setId(SUPER_SLIME_KEY)
			.stacksTo(99)
			.rarity(Rarity.COMMON)
	);

	public static final ResourceKey<Item> EXPLOSIVE_SLIME_KEY = ResourceKey.create(
			Registries.ITEM, id("explosive_slime"));

	public static final Item EXPLOSIVE_SLIME = new Item(new Item.Properties()
			.setId(EXPLOSIVE_SLIME_KEY)
			.stacksTo(99)
			.rarity(Rarity.COMMON)
	);

	public static final ResourceKey<Item> EXPLOSIVE_SLIME_STICK_KEY = ResourceKey.create(
			Registries.ITEM, id("explosive_slime_stick"));

	public static final Item EXPLOSIVE_SLIME_STICK = new Item(new Item.Properties()
			.setId(EXPLOSIVE_SLIME_STICK_KEY)
			.stacksTo(64)
			.rarity(Rarity.COMMON)
	);

	public static final ResourceKey<Item> END_BATTERY_KEY = ResourceKey.create(
			Registries.ITEM, id("end_battery"));

	public static final Item END_BATTERY = new Item(new Item.Properties()
			.setId(END_BATTERY_KEY)
			.stacksTo(64)
			.rarity(Rarity.RARE)
	);

	public static final ResourceKey<Item> NETHER_BATTERY_KEY = ResourceKey.create(
			Registries.ITEM, id("nether_battery"));

	public static final Item NETHER_BATTERY = new Item(new Item.Properties()
			.setId(NETHER_BATTERY_KEY)
			.stacksTo(64)
	);

	public static final ResourceKey<Item> SLIME_SWORD_KEY = ResourceKey.create(
			Registries.ITEM, id("slime_sword"));

	public static final Item SLIME_SWORD = new Item(new Item.Properties()
			.setId(SLIME_SWORD_KEY)
			.sword(SLIME_TOOL_MATERIAL, 50f, -2.4f)
			.attributes(ItemAttributeModifiers.builder()
					.add(Attributes.ATTACK_DAMAGE,
							new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, 100.0,
									AttributeModifier.Operation.ADD_VALUE),
							EquipmentSlotGroup.MAINHAND)
					.add(Attributes.ATTACK_SPEED,
							new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, -2.4,
									AttributeModifier.Operation.ADD_VALUE),
							EquipmentSlotGroup.MAINHAND)
					.add(Attributes.ATTACK_KNOCKBACK,
							new AttributeModifier(id("slime_knockback"), 5.0,
									AttributeModifier.Operation.ADD_VALUE),
							EquipmentSlotGroup.MAINHAND)
					.build())
			.rarity(Rarity.UNCOMMON)
	);

	public static final ResourceKey<Item> SLIME_AXE_KEY = ResourceKey.create(
			Registries.ITEM, id("slime_axe"));

	public static final Item SLIME_AXE = new Item(new Item.Properties()
			.setId(SLIME_AXE_KEY)
			.axe(SLIME_TOOL_MATERIAL, 10f, 1f)
			.rarity(Rarity.UNCOMMON)
	);

	public static final ResourceKey<Item> SLIME_PICKAXE_KEY = ResourceKey.create(
			Registries.ITEM, id("slime_pickaxe"));

	public static final Item SLIME_PICKAXE = new Item(new Item.Properties()
			.setId(SLIME_PICKAXE_KEY)
			.pickaxe(SLIME_TOOL_MATERIAL, 7f, 1f)
			.rarity(Rarity.UNCOMMON)
	);

	public static final ResourceKey<Item> SLIME_SHOVEL_KEY = ResourceKey.create(
			Registries.ITEM, id("slime_shovel"));

	public static final Item SLIME_SHOVEL = new Item(new Item.Properties()
			.setId(SLIME_SHOVEL_KEY)
			.shovel(SLIME_TOOL_MATERIAL, 5f, 1f)
			.rarity(Rarity.UNCOMMON)
	);

	public static final ResourceKey<Item> SLIME_HOE_KEY = ResourceKey.create(
			Registries.ITEM, id("slime_hoe"));

	public static final Item SLIME_HOE = new Item(new Item.Properties()
			.setId(SLIME_HOE_KEY)
			.hoe(SLIME_TOOL_MATERIAL, 4f, 1f)
			.rarity(Rarity.UNCOMMON)
	);

	public static final ResourceKey<Item> SLIME_SCYTHE_KEY = ResourceKey.create(
			Registries.ITEM, id("slime_scythe"));

	public static final Item SLIME_SCYTHE = new Item(new Item.Properties()
			.setId(SLIME_SCYTHE_KEY)
			.sword(SLIME_TOOL_MATERIAL, 2048f, -3.0f)
			.attributes(ItemAttributeModifiers.builder()
					.add(Attributes.ATTACK_DAMAGE,
							new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, 2048.0,
									AttributeModifier.Operation.ADD_VALUE),
							EquipmentSlotGroup.MAINHAND)
					.add(Attributes.ATTACK_SPEED,
							new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, -3.0,
									AttributeModifier.Operation.ADD_VALUE),
							EquipmentSlotGroup.MAINHAND)
					.build())
	);

		public static final ResourceKey<Item> NETHER_SCYTHE_KEY = ResourceKey.create(
			Registries.ITEM, id("nether_scythe"));

	public static final Item NETHER_SCYTHE = new NetherScytheItem(new Item.Properties()
			.setId(NETHER_SCYTHE_KEY)
			.sword(SLIME_TOOL_MATERIAL, 2048f, -3.2f)
			.attributes(ItemAttributeModifiers.builder()
					.add(Attributes.ATTACK_DAMAGE,
							new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, 2048.0,
									AttributeModifier.Operation.ADD_VALUE),
							EquipmentSlotGroup.MAINHAND)
					.add(Attributes.ATTACK_SPEED,
							new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, -3.2,
									AttributeModifier.Operation.ADD_VALUE),
							EquipmentSlotGroup.MAINHAND)
					.add(Attributes.ENTITY_INTERACTION_RANGE,
							new AttributeModifier(id("nether_scythe_reach"), 4.0,
									AttributeModifier.Operation.ADD_VALUE),
							EquipmentSlotGroup.MAINHAND)
					.build())
	);

	public static final ResourceKey<Item> SLIME_HELMET_KEY = ResourceKey.create(
			Registries.ITEM, id("slime_helmet"));

	public static final Item SLIME_HELMET = new Item(new Item.Properties()
			.setId(SLIME_HELMET_KEY)
			.humanoidArmor(SLIME_ARMOR_MATERIAL, ArmorType.HELMET)
			.rarity(Rarity.UNCOMMON)
	);

	public static final ResourceKey<Item> SLIME_CHESTPLATE_KEY = ResourceKey.create(
			Registries.ITEM, id("slime_chestplate"));

	public static final Item SLIME_CHESTPLATE = new Item(new Item.Properties()
			.setId(SLIME_CHESTPLATE_KEY)
			.humanoidArmor(SLIME_ARMOR_MATERIAL, ArmorType.CHESTPLATE)
			.component(DataComponents.GLIDER, Unit.INSTANCE)
			.rarity(Rarity.RARE)
	);

	public static final ResourceKey<Item> SLIME_LEGGINGS_KEY = ResourceKey.create(
			Registries.ITEM, id("slime_leggings"));

	public static final Item SLIME_LEGGINGS = new Item(new Item.Properties()
			.setId(SLIME_LEGGINGS_KEY)
			.humanoidArmor(SLIME_ARMOR_MATERIAL, ArmorType.LEGGINGS)
			.rarity(Rarity.UNCOMMON)
	);

	public static final ResourceKey<Item> SLIME_BOOTS_KEY = ResourceKey.create(
			Registries.ITEM, id("slime_boots"));

	public static final Item SLIME_BOOTS = new Item(new Item.Properties()
			.setId(SLIME_BOOTS_KEY)
			.humanoidArmor(SLIME_ARMOR_MATERIAL, ArmorType.BOOTS)
			.rarity(Rarity.UNCOMMON)
	);

	public static final ResourceKey<Item> SLIME_HORSE_ARMOR_KEY = ResourceKey.create(
			Registries.ITEM, id("slime_horse_armor"));

	public static final Item SLIME_HORSE_ARMOR = new Item(new Item.Properties()
			.setId(SLIME_HORSE_ARMOR_KEY)
			.horseArmor(SLIME_ARMOR_MATERIAL)
			.stacksTo(1)
			.rarity(Rarity.UNCOMMON)
	);

	public static final ResourceKey<Item> SLIME_GRENADE_KEY = ResourceKey.create(
			Registries.ITEM, id("slime_grenade"));

	public static final Item SLIME_GRENADE = new SlimeGrenadeItem(new Item.Properties()
			.setId(SLIME_GRENADE_KEY)
			.stacksTo(10)
			.rarity(Rarity.RARE)
	);

	// ─────────────────────────────────────────────
	//  ENTITY AND CREATIVE TAB
	// ─────────────────────────────────────────────

	public static final ResourceKey<EntityType<?>> SLIME_GRENADE_ENTITY_KEY =
			ResourceKey.create(Registries.ENTITY_TYPE, id("slime_grenade"));

	public static EntityType<SlimeGrenadeEntity> SLIME_GRENADE_ENTITY;

	public static final ResourceKey<Item> SLIME_MEGA_GRENADE_KEY = ResourceKey.create(
			Registries.ITEM, id("slime_mega_grenade"));

	public static final Item SLIME_MEGA_GRENADE = new SlimeMegaGrenadeItem(new Item.Properties()
			.setId(SLIME_MEGA_GRENADE_KEY)
			.stacksTo(8)
			.rarity(Rarity.EPIC)
	);

	public static final ResourceKey<EntityType<?>> SLIME_MEGA_GRENADE_ENTITY_KEY =
			ResourceKey.create(Registries.ENTITY_TYPE, id("slime_mega_grenade"));

	public static EntityType<SlimeMegaGrenadeEntity> SLIME_MEGA_GRENADE_ENTITY;

	public static final ResourceKey<CreativeModeTab> SLIME_TAB_KEY =
			ResourceKey.create(Registries.CREATIVE_MODE_TAB, id("slime"));

	// ─────────────────────────────────────────────
	//  EFFECTS AND POTIONS
	// ─────────────────────────────────────────────

	public static final ResourceKey<MobEffect> KNOCKBACK_RESIST_KEY =
			ResourceKey.create(Registries.MOB_EFFECT, id("knockback_resist"));

	public static final MobEffect KNOCKBACK_RESIST =
			new KnockbackResistEffect(MobEffectCategory.BENEFICIAL, 0x7FDD5A)
					.addAttributeModifier(
							Attributes.KNOCKBACK_RESISTANCE,
							id("knockback_resist"),
							0.25,
							AttributeModifier.Operation.ADD_VALUE
					);

	public static Holder<MobEffect> KNOCKBACK_RESIST_HOLDER;

	public static final ResourceKey<MobEffect> SLIME_LUCK_KEY =
			ResourceKey.create(Registries.MOB_EFFECT, id("slime_luck"));

	public static final MobEffect SLIME_LUCK =
			new KnockbackResistEffect(MobEffectCategory.BENEFICIAL, 0x9CE87A);

	public static Holder<MobEffect> SLIME_LUCK_HOLDER;

	public static final ResourceKey<MobEffect> STICKY_KEY =
			ResourceKey.create(Registries.MOB_EFFECT, id("sticky"));

	public static final MobEffect STICKY =
			new KnockbackResistEffect(MobEffectCategory.HARMFUL, 0x4A9638)
					.addAttributeModifier(
							Attributes.MOVEMENT_SPEED,
							id("sticky"),
							-1.0,
							AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
					);

	public static Holder<MobEffect> STICKY_HOLDER;

	public static final ResourceKey<Potion> SLIME_POTION_KEY =
			ResourceKey.create(Registries.POTION, id("slime_potion"));

	public static Holder<Potion> SLIME_POTION_HOLDER;

	public static final ResourceKey<MobEffect> VERY_STICKY_KEY =
			ResourceKey.create(Registries.MOB_EFFECT, id("very_sticky"));

	public static final MobEffect VERY_STICKY =
			new VeryStickyEffect(MobEffectCategory.HARMFUL, 0x2E5C24)
					.addAttributeModifier(
							Attributes.MOVEMENT_SPEED,
							id("very_sticky"),
							-1.0,
							AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
					);

	public static Holder<MobEffect> VERY_STICKY_HOLDER;

	public static final ResourceKey<Potion> SLIMY_MISFORTUNE_KEY =
			ResourceKey.create(Registries.POTION, id("slimy_misfortune"));

	public static Holder<Potion> SLIMY_MISFORTUNE_HOLDER;

	public static final ResourceKey<EntityType<?>> PRINCE_SLIME_KEY =
			ResourceKey.create(Registries.ENTITY_TYPE, id("prince_slime"));

	public static EntityType<PrinceSlimeEntity> PRINCE_SLIME;

	public static final ResourceKey<EntityType<?>> KING_SLIME_KEY =
			ResourceKey.create(Registries.ENTITY_TYPE, id("king_slime"));

	public static EntityType<KingSlimeEntity> KING_SLIME;

		public static final ResourceKey<EntityType<?>> EXPLOSIVE_SLIME_CUBE_KEY =
			ResourceKey.create(Registries.ENTITY_TYPE, id("explosive_slime_cube"));

	public static EntityType<ExplosiveSlimeEntity> EXPLOSIVE_SLIME_CUBE;

		public static final ResourceKey<Block> EXPLOSIVE_SLIME_BLOCK_KEY =
			ResourceKey.create(Registries.BLOCK, id("explosive_slime_block"));

	public static final Block EXPLOSIVE_SLIME_BLOCK = new ExplosiveSlimeBlock(
			BlockBehaviour.Properties.of()
					.setId(EXPLOSIVE_SLIME_BLOCK_KEY)
					.instabreak()
					.sound(SoundType.SLIME_BLOCK)
	);

	public static final ResourceKey<Item> EXPLOSIVE_SLIME_BLOCK_ITEM_KEY =
			ResourceKey.create(Registries.ITEM, id("explosive_slime_block"));

	public static final Item EXPLOSIVE_SLIME_BLOCK_ITEM = new BlockItem(
			EXPLOSIVE_SLIME_BLOCK,
			new Item.Properties().setId(EXPLOSIVE_SLIME_BLOCK_ITEM_KEY)
	);

		public static final ResourceKey<Block> SLIME_SPAWNER_KEY =
			ResourceKey.create(Registries.BLOCK, id("slime_spawner"));

	public static final Block SLIME_SPAWNER = new SlimeSpawnerBlock(
			BlockBehaviour.Properties.of()
					.setId(SLIME_SPAWNER_KEY)
					.strength(5.0F, 6.0F)
					.sound(SoundType.METAL)
					.requiresCorrectToolForDrops()
	);

	public static final ResourceKey<Item> SLIME_SPAWNER_ITEM_KEY =
			ResourceKey.create(Registries.ITEM, id("slime_spawner"));

	public static final Item SLIME_SPAWNER_ITEM = new BlockItem(
			SLIME_SPAWNER,
			new Item.Properties().setId(SLIME_SPAWNER_ITEM_KEY).rarity(Rarity.RARE)
	);

	public static BlockEntityType<SlimeSpawnerBlockEntity> SLIME_SPAWNER_ENTITY;

	public static final ResourceKey<Level> SLIME_DIMENSION =
			ResourceKey.create(Registries.DIMENSION, id("slime"));

	public static final ResourceKey<Item> SLIME_PEARL_KEY = ResourceKey.create(
			Registries.ITEM, id("slime_pearl"));

	public static final Item SLIME_PEARL = new TeleporterPearlItem(
			new Item.Properties().setId(SLIME_PEARL_KEY).stacksTo(16),
			SLIME_DIMENSION, "use_slime_pearl");

	public static final ResourceKey<Item> OVERWORLD_PEARL_KEY = ResourceKey.create(
			Registries.ITEM, id("overworld_pearl"));

	public static final Item OVERWORLD_PEARL = new TeleporterPearlItem(
			new Item.Properties().setId(OVERWORLD_PEARL_KEY).stacksTo(16),
			Level.OVERWORLD, "use_overworld_pearl");

	public static final ResourceKey<Item> NETHER_PEARL_KEY = ResourceKey.create(
			Registries.ITEM, id("nether_pearl"));

	public static final Item NETHER_PEARL = new TeleporterPearlItem(
			new Item.Properties().setId(NETHER_PEARL_KEY).stacksTo(16),
			Level.NETHER, "use_nether_pearl");

	public static final ResourceKey<Item> END_PEARL_KEY = ResourceKey.create(
			Registries.ITEM, id("end_pearl"));

	public static final Item END_PEARL = new TeleporterPearlItem(
			new Item.Properties().setId(END_PEARL_KEY).stacksTo(16),
			Level.END, "use_end_pearl");

		public static final ResourceKey<Item> KILL_STICK_KEY = ResourceKey.create(
			Registries.ITEM, id("kill_stick"));

	public static final Item KILL_STICK = new KillStickItem(new Item.Properties()
			.setId(KILL_STICK_KEY)
			.stacksTo(1)
			.rarity(Rarity.EPIC)
	);

	public static final ResourceKey<Item> RAGE_STICK_KEY = ResourceKey.create(
			Registries.ITEM, id("rage_stick"));

	public static final Item RAGE_STICK = new RageStickItem(new Item.Properties()
			.setId(RAGE_STICK_KEY)
			.stacksTo(1)
			.rarity(Rarity.EPIC)
	);

	/** Tracks each player's downward speed from the previous tick, for the boot bounce. */
	public static final Map<UUID, Double> LAST_FALL_SPEED = new HashMap<>();

	public static final Set<UUID> HAD_VERY_STICKY = new HashSet<>();

	public static boolean slimeRainActive = false;

	public static long slimeRainEndsAt = 0L;

	// ─────────────────────────────────────────────
	//  INITIALIZATION
	// ─────────────────────────────────────────────

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing {}", MOD_ID);

		// --- items ---
		Registry.register(BuiltInRegistries.ITEM, SUPER_SLIME_KEY, SUPER_SLIME);
		// Just get the super slime
		Registry.register(BuiltInRegistries.ITEM, EXPLOSIVE_SLIME_KEY, EXPLOSIVE_SLIME);
		// Need 1 Super Slime and 1 TNT
		Registry.register(BuiltInRegistries.ITEM, EXPLOSIVE_SLIME_STICK_KEY, EXPLOSIVE_SLIME_STICK);
		Registry.register(BuiltInRegistries.ITEM, END_BATTERY_KEY, END_BATTERY);
		Registry.register(BuiltInRegistries.ITEM, NETHER_BATTERY_KEY, NETHER_BATTERY);
		Registry.register(BuiltInRegistries.ITEM, SLIME_SWORD_KEY, SLIME_SWORD);
		Registry.register(BuiltInRegistries.ITEM, SLIME_AXE_KEY, SLIME_AXE);
		Registry.register(BuiltInRegistries.ITEM, SLIME_PICKAXE_KEY, SLIME_PICKAXE);
		Registry.register(BuiltInRegistries.ITEM, SLIME_SHOVEL_KEY, SLIME_SHOVEL);
		Registry.register(BuiltInRegistries.ITEM, SLIME_HOE_KEY, SLIME_HOE);
		Registry.register(BuiltInRegistries.ITEM, SLIME_SCYTHE_KEY, SLIME_SCYTHE);
		Registry.register(BuiltInRegistries.ITEM, NETHER_SCYTHE_KEY, NETHER_SCYTHE);
		Registry.register(BuiltInRegistries.ITEM, SLIME_HELMET_KEY, SLIME_HELMET);
		Registry.register(BuiltInRegistries.ITEM, SLIME_CHESTPLATE_KEY, SLIME_CHESTPLATE);
		Registry.register(BuiltInRegistries.ITEM, SLIME_LEGGINGS_KEY, SLIME_LEGGINGS);
		Registry.register(BuiltInRegistries.ITEM, SLIME_BOOTS_KEY, SLIME_BOOTS);
		Registry.register(BuiltInRegistries.ITEM, SLIME_HORSE_ARMOR_KEY, SLIME_HORSE_ARMOR);
		Registry.register(BuiltInRegistries.ITEM, SLIME_GRENADE_KEY, SLIME_GRENADE);
		Registry.register(BuiltInRegistries.ITEM, SLIME_MEGA_GRENADE_KEY, SLIME_MEGA_GRENADE);
		Registry.register(BuiltInRegistries.BLOCK, EXPLOSIVE_SLIME_BLOCK_KEY, EXPLOSIVE_SLIME_BLOCK);
		Registry.register(BuiltInRegistries.ITEM, EXPLOSIVE_SLIME_BLOCK_ITEM_KEY, EXPLOSIVE_SLIME_BLOCK_ITEM);
		Registry.register(BuiltInRegistries.ITEM, SLIME_PEARL_KEY, SLIME_PEARL);
		Registry.register(BuiltInRegistries.ITEM, OVERWORLD_PEARL_KEY, OVERWORLD_PEARL);
		Registry.register(BuiltInRegistries.ITEM, NETHER_PEARL_KEY, NETHER_PEARL);
		Registry.register(BuiltInRegistries.ITEM, END_PEARL_KEY, END_PEARL);
		Registry.register(BuiltInRegistries.ITEM, KILL_STICK_KEY, KILL_STICK);
		Registry.register(BuiltInRegistries.ITEM, RAGE_STICK_KEY, RAGE_STICK);
		Registry.register(BuiltInRegistries.BLOCK, SLIME_SPAWNER_KEY, SLIME_SPAWNER);
		Registry.register(BuiltInRegistries.ITEM, SLIME_SPAWNER_ITEM_KEY, SLIME_SPAWNER_ITEM);

		SLIME_SPAWNER_ENTITY = Registry.register(
				BuiltInRegistries.BLOCK_ENTITY_TYPE,
				id("slime_spawner"),
				FabricBlockEntityTypeBuilder.create(SlimeSpawnerBlockEntity::new, SLIME_SPAWNER).build()
		);

		// --- grenade entity ---
		SLIME_GRENADE_ENTITY = Registry.register(
				BuiltInRegistries.ENTITY_TYPE,
				SLIME_GRENADE_ENTITY_KEY,
				EntityType.Builder.<SlimeGrenadeEntity>of(SlimeGrenadeEntity::new, MobCategory.MISC)
						.noLootTable()
						.sized(0.25F, 0.25F)
						.clientTrackingRange(4)
						.updateInterval(10)
						.build(SLIME_GRENADE_ENTITY_KEY));

		SLIME_MEGA_GRENADE_ENTITY = Registry.register(
				BuiltInRegistries.ENTITY_TYPE,
				SLIME_MEGA_GRENADE_ENTITY_KEY,
				EntityType.Builder.<SlimeMegaGrenadeEntity>of(SlimeMegaGrenadeEntity::new, MobCategory.MISC)
						.noLootTable()
						.sized(0.25F, 0.25F)
						.clientTrackingRange(4)
						.updateInterval(10)
						.build(SLIME_MEGA_GRENADE_ENTITY_KEY));

		// --- effects (potion must come after SLIME_LUCK_HOLDER is assigned) ---
		KNOCKBACK_RESIST_HOLDER = Registry.registerForHolder(
				BuiltInRegistries.MOB_EFFECT, KNOCKBACK_RESIST_KEY, KNOCKBACK_RESIST);

		SLIME_LUCK_HOLDER = Registry.registerForHolder(
				BuiltInRegistries.MOB_EFFECT, SLIME_LUCK_KEY, SLIME_LUCK);

		STICKY_HOLDER = Registry.registerForHolder(
				BuiltInRegistries.MOB_EFFECT, STICKY_KEY, STICKY);

		SLIME_POTION_HOLDER = Registry.registerForHolder(
				BuiltInRegistries.POTION, SLIME_POTION_KEY,
				new Potion("slime_potion", new MobEffectInstance(SLIME_LUCK_HOLDER, 6000)));

		VERY_STICKY_HOLDER = Registry.registerForHolder(
				BuiltInRegistries.MOB_EFFECT, VERY_STICKY_KEY, VERY_STICKY);

		SLIMY_MISFORTUNE_HOLDER = Registry.registerForHolder(
				BuiltInRegistries.POTION, SLIMY_MISFORTUNE_KEY,
				new Potion("slimy_misfortune", new MobEffectInstance(VERY_STICKY_HOLDER, 100)));

		PRINCE_SLIME = Registry.register(
				BuiltInRegistries.ENTITY_TYPE,
				PRINCE_SLIME_KEY,
				EntityType.Builder.<PrinceSlimeEntity>of(PrinceSlimeEntity::new, MobCategory.MONSTER)
						.sized(0.52F, 0.52F)
						.spawnDimensionsScale(4.0F)
						.clientTrackingRange(10)
						.build(PRINCE_SLIME_KEY));

		FabricDefaultAttributeRegistry.register(PRINCE_SLIME, PrinceSlimeEntity.createAttributes());

		KING_SLIME = Registry.register(
				BuiltInRegistries.ENTITY_TYPE,
				KING_SLIME_KEY,
				EntityType.Builder.<KingSlimeEntity>of(KingSlimeEntity::new, MobCategory.MONSTER)
						.sized(0.52F, 0.52F)
						.spawnDimensionsScale(4.0F)
						.clientTrackingRange(10)
						.build(KING_SLIME_KEY));

		FabricDefaultAttributeRegistry.register(KING_SLIME, KingSlimeEntity.createAttributes());

				EXPLOSIVE_SLIME_CUBE = Registry.register(
				BuiltInRegistries.ENTITY_TYPE,
				EXPLOSIVE_SLIME_CUBE_KEY,
				EntityType.Builder.<ExplosiveSlimeEntity>of(ExplosiveSlimeEntity::new, MobCategory.MONSTER)
						.sized(0.52F, 0.52F)
						.spawnDimensionsScale(4.0F)
						.clientTrackingRange(8)
						.build(EXPLOSIVE_SLIME_CUBE_KEY));

		FabricDefaultAttributeRegistry.register(EXPLOSIVE_SLIME_CUBE, ExplosiveSlimeEntity.createAttributes());

		// --- creative tab ---
		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, SLIME_TAB_KEY,
				CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
						.title(Component.translatable("itemGroup.slimemod.slime"))
						.icon(() -> new ItemStack(SUPER_SLIME))
						.displayItems((params, output) -> {
							output.accept(SUPER_SLIME);
							output.accept(EXPLOSIVE_SLIME);
                            output.accept(EXPLOSIVE_SLIME_STICK);
							output.accept(END_BATTERY);
							output.accept(NETHER_BATTERY);
							output.accept(SLIME_SWORD);
							output.accept(SLIME_AXE);
							output.accept(SLIME_PICKAXE);
							output.accept(SLIME_SHOVEL);
							output.accept(SLIME_HOE);
                            output.accept(SLIME_SCYTHE);
							output.accept(NETHER_SCYTHE);
							output.accept(SLIME_HELMET);
							output.accept(SLIME_CHESTPLATE);
							output.accept(SLIME_LEGGINGS);
							output.accept(SLIME_BOOTS);
							output.accept(SLIME_HORSE_ARMOR);
							output.accept(SLIME_GRENADE);
							output.accept(SLIME_MEGA_GRENADE);
							output.accept(EXPLOSIVE_SLIME_BLOCK_ITEM);
							output.accept(SLIME_PEARL);
							output.accept(OVERWORLD_PEARL);
							output.accept(NETHER_PEARL);
							output.accept(END_PEARL);
							output.accept(SLIME_SPAWNER_ITEM);
						})
						.build());

		// --- Knockback Resist scales with armor worn; boots bounce on landing ---
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				int count = countSlimeArmor(player);
				if (count > 0) {
					player.addEffect(new MobEffectInstance(
							KNOCKBACK_RESIST_HOLDER,
							40,          // refreshed every tick, expires ~2s after removal
							count - 1,   // 1 piece -> level I, 4 pieces -> level IV
							true,
							false,
							true));
				}

				double prevY = LAST_FALL_SPEED.getOrDefault(player.getUUID(), 0.0);
				double nowY = player.getDeltaMovement().y;

				if (player.onGround() && prevY < -0.1
						&& player.getItemBySlot(EquipmentSlot.FEET).is(SLIME_ARMOR)
						&& !player.isShiftKeyDown()) {
										player.setDeltaMovement(new Vec3(
							player.getDeltaMovement().x,
							-prevY * 0.8,
							player.getDeltaMovement().z));
					player.hurtMarked = true;
					player.hurtMarked = true;   // forces the client to accept the new velocity
				}

				LAST_FALL_SPEED.put(player.getUUID(), nowY);


				if (player.getVehicle() instanceof AbstractHorse horse
						&& horse.getItemBySlot(EquipmentSlot.BODY).is(SLIME_HORSE_ARMOR)) {
					horse.addEffect(new MobEffectInstance(
							KNOCKBACK_RESIST_HOLDER, 40, 3, true, false, true));
				}

				if (player.hasEffect(VERY_STICKY_HOLDER)) {
					HAD_VERY_STICKY.add(player.getUUID());
				} else if (HAD_VERY_STICKY.remove(player.getUUID()) && player.isAlive()) {
					grantAdvancement(server, player, "survive_misfortune", "survived");
					startSlimeRain(server, 24000L);
				}






			}

			// ─── slime rain ───
			if (slimeRainActive) {
				ServerLevel overworld = server.getLevel(Level.OVERWORLD);
				if (overworld != null) {
					if (overworld.getGameTime() >= slimeRainEndsAt) {
						slimeRainActive = false;
						server.setWeatherParameters(6000, 0, false, false);
						for (ServerPlayer player : server.getPlayerList().getPlayers()) {
							if (player.isAlive()) {
								grantAdvancement(server, player, "survive_slime_rain", "survived");
							}
						}
					} else if (!overworld.getWeatherData().isRaining()) {
						server.setWeatherParameters(0, 24000, true, false);
					}

					// ─── green particles ───
					if (overworld.getGameTime() % 2 == 0) {
						for (ServerPlayer player : server.getPlayerList().getPlayers()) {
							overworld.sendParticles(player, ParticleTypes.ITEM_SLIME, true,
									false, player.getX(), player.getY() + 12, player.getZ(),
									30, 12.0, 3.0, 12.0, 0.1);
						}
					}
				}
			}
		});

		// --- Slime Luck: extra Super Slime from breaking blocks ---
		PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
			if (!(player instanceof ServerPlayer serverPlayer)) return;
			if (!serverPlayer.hasEffect(SLIME_LUCK_HOLDER)) return;
			if (world.getRandom().nextFloat() < 0.5f) {
				Block.popResource((Level) world, pos, new ItemStack(SUPER_SLIME));
			}
		});

		// --- Slime Luck: extra Super Slime from kills ---
		ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
			if (!(damageSource.getEntity() instanceof ServerPlayer player)) return;
			if (!player.hasEffect(SLIME_LUCK_HOLDER)) return;
			if (entity.getRandom().nextFloat() < 0.5f) {
				entity.spawnAtLocation((ServerLevel) entity.level(), new ItemStack(SUPER_SLIME));
			}
		});

		// --- boots stop fall damage; full set stops kinetic damage ---
		ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
			if (!(entity instanceof ServerPlayer player)) return true;
			if (source.is(DamageTypes.FLY_INTO_WALL)) {
				return countSlimeArmor(player) < 4;
			}
			if (source.is(DamageTypes.FALL)) {
				return !player.getItemBySlot(EquipmentSlot.FEET).is(SLIME_ARMOR);
			}
			return true;
		});

		CommandRegistrationCallback.EVENT.register((dispatcher, registry, env) -> {
			dispatcher.register(literal("slimerain")
						.requires(src -> src.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
					.executes(ctx -> {
						startSlimeRain(ctx.getSource().getServer(), 24000L);
						return 1;
					})
					.then(literal("stop")
							.executes(ctx -> {
								stopSlimeRain(ctx.getSource().getServer());
								return 1;
							})));
		});

	}

	// ─────────────────────────────────────────────
	//  HELPERS
	// ─────────────────────────────────────────────

	public static int countSlimeArmor(ServerPlayer player) {
		int count = 0;
		for (EquipmentSlot slot : new EquipmentSlot[]{
				EquipmentSlot.HEAD, EquipmentSlot.CHEST,
				EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
			if (player.getItemBySlot(slot).is(SLIME_ARMOR)) count++;
		}
		return count;
	}

		public static void grantAdvancement(MinecraftServer server, ServerPlayer player, String path, String criterion) {
		var advancement = server.getAdvancements().get(id(path));
		if (advancement != null) {
			player.getAdvancements().award(advancement, criterion);
		}
	}

	public static void startSlimeRain(MinecraftServer server, long durationTicks) {
		slimeRainActive = true;
		ServerLevel overworld = server.getLevel(Level.OVERWORLD);
		if (overworld == null) return;

		slimeRainEndsAt = overworld.getGameTime() + durationTicks;

		// one Prince Slime per player, 40 blocks out
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			spawnPrinceNear(overworld, player);
		}

		// 10% chance of a single King Slime
		if (overworld.getRandom().nextFloat() < 0.10f) {
			List<ServerPlayer> players = server.getPlayerList().getPlayers();
			if (!players.isEmpty()) {
				spawnKingNear(overworld, players.get(0));
			}
		}
	}

	public static void stopSlimeRain(MinecraftServer server) {
		slimeRainActive = false;
		slimeRainEndsAt = 0L;
		server.setWeatherParameters(6000, 0, false, false);
	}


	public static void spawnPrinceNear(ServerLevel level, ServerPlayer player) {
		double angle = level.getRandom().nextDouble() * Math.PI * 2;
		double x = player.getX() + Math.cos(angle) * 40.0;
		double z = player.getZ() + Math.sin(angle) * 40.0;
		int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int) x, (int) z);

		PrinceSlimeEntity prince = PRINCE_SLIME.create(level, EntitySpawnReason.EVENT);

		if (prince != null) {
			prince.snapTo(x, y + 1, z, 0.0F, 0.0F);
			level.addFreshEntity(prince);
		}
	}
	
	public static void spawnKingNear(ServerLevel level, ServerPlayer player) {
		double angle = level.getRandom().nextDouble() * Math.PI * 2;
		double x = player.getX() + Math.cos(angle) * 50.0;
		double z = player.getZ() + Math.sin(angle) * 50.0;
		int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int) x, (int) z);

		KingSlimeEntity king = KING_SLIME.create(level, EntitySpawnReason.EVENT);

		if (king != null) {
			king.snapTo(x, y + 1, z, 0.0F, 0.0F);
			level.addFreshEntity(king);
		}
	}




	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}