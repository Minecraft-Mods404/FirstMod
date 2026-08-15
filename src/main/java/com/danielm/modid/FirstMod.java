package com.danielm.modid;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Unit;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.HitResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class FirstMod implements ModInitializer {
	public static final String MOD_ID = "firstmod";

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
	);

	public static final ResourceKey<Item> EXPLOSIVE_SLIME_KEY = ResourceKey.create(
			Registries.ITEM, id("explosive_slime"));

	public static final Item EXPLOSIVE_SLIME = new Item(new Item.Properties()
			.setId(EXPLOSIVE_SLIME_KEY)
			.stacksTo(99)
	);

	public static final ResourceKey<Item> SLIME_SWORD_KEY = ResourceKey.create(
			Registries.ITEM, id("slime_sword"));

	public static final Item SLIME_SWORD = new Item(new Item.Properties()
			.setId(SLIME_SWORD_KEY)
			.sword(SLIME_TOOL_MATERIAL, 50f, -2.4f)
			.attributes(ItemAttributeModifiers.builder()
					.add(Attributes.ATTACK_DAMAGE,
							new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, 2048.0,
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
	);

	public static final ResourceKey<Item> SLIME_AXE_KEY = ResourceKey.create(
			Registries.ITEM, id("slime_axe"));

	public static final Item SLIME_AXE = new Item(new Item.Properties()
			.setId(SLIME_AXE_KEY)
			.axe(SLIME_TOOL_MATERIAL, 10f, 1f)
	);

	public static final ResourceKey<Item> SLIME_PICKAXE_KEY = ResourceKey.create(
			Registries.ITEM, id("slime_pickaxe"));

	public static final Item SLIME_PICKAXE = new Item(new Item.Properties()
			.setId(SLIME_PICKAXE_KEY)
			.pickaxe(SLIME_TOOL_MATERIAL, 7f, 1f)
	);

	public static final ResourceKey<Item> SLIME_SHOVEL_KEY = ResourceKey.create(
			Registries.ITEM, id("slime_shovel")
	);

	public static final Item SLIME_SHOVEL = new Item(new Item.Properties()
			.setId(SLIME_SHOVEL_KEY)
			.shovel(SLIME_TOOL_MATERIAL, 5f, 1f)
	);

	public static final ResourceKey<Item> SLIME_HOE_KEY = ResourceKey.create(
			Registries.ITEM, id("slime_hoe")
	);

	public static final Item SLIME_HOE = new Item(new Item.Properties()
			.setId(SLIME_HOE_KEY)
			.hoe(SLIME_TOOL_MATERIAL, 4f, 1f)
	);

	public static final ResourceKey<Item> SLIME_HELMET_KEY = ResourceKey.create(
			Registries.ITEM, id("slime_helmet"));

	public static final Item SLIME_HELMET = new Item(new Item.Properties()
			.setId(SLIME_HELMET_KEY)
			.humanoidArmor(SLIME_ARMOR_MATERIAL, ArmorType.HELMET)
	);

	public static final ResourceKey<Item> SLIME_CHESTPLATE_KEY = ResourceKey.create(
			Registries.ITEM, id("slime_chestplate"));

	public static final Item SLIME_CHESTPLATE = new Item(new Item.Properties()
			.setId(SLIME_CHESTPLATE_KEY)
			.humanoidArmor(SLIME_ARMOR_MATERIAL, ArmorType.CHESTPLATE)
			.component(DataComponents.GLIDER, Unit.INSTANCE)
	);

	public static final ResourceKey<Item> SLIME_LEGGINGS_KEY = ResourceKey.create(
			Registries.ITEM, id("slime_leggings"));

	public static final Item SLIME_LEGGINGS = new Item(new Item.Properties()
			.setId(SLIME_LEGGINGS_KEY)
			.humanoidArmor(SLIME_ARMOR_MATERIAL, ArmorType.LEGGINGS)
	);

	public static final ResourceKey<Item> SLIME_BOOTS_KEY = ResourceKey.create(
			Registries.ITEM, id("slime_boots"));

	public static final Item SLIME_BOOTS = new Item(new Item.Properties()
			.setId(SLIME_BOOTS_KEY)
			.humanoidArmor(SLIME_ARMOR_MATERIAL, ArmorType.BOOTS)
	);

	public static final ResourceKey<Item> SLIME_GRENADE_KEY = ResourceKey.create(
			Registries.ITEM, id("slime_grenade"));

	public static final Item SLIME_GRENADE = new Item(new Item.Properties()
			.setId(SLIME_GRENADE_KEY)
			.stacksTo(10)
	);

	public static final ResourceKey<EntityType<?>> SLIME_GRENADE_ENTITY_KEY =
			ResourceKey.create(Registries.ENTITY_TYPE, id("slime_grenade"));

	public static EntityType<SlimeGrenadeEntity> SLIME_GRENADE_ENTITY;

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

	public static final Map<UUID, Double> LAST_FALL_SPEED = new HashMap<>();

	// ─────────────────────────────────────────────
	//  INITIALIZATION
	// ─────────────────────────────────────────────

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing {}", MOD_ID);

		// --- items ---
		Registry.register(BuiltInRegistries.ITEM, SUPER_SLIME_KEY, SUPER_SLIME);
		Registry.register(BuiltInRegistries.ITEM, EXPLOSIVE_SLIME_KEY, EXPLOSIVE_SLIME);
		Registry.register(BuiltInRegistries.ITEM, SLIME_SWORD_KEY, SLIME_SWORD);
		Registry.register(BuiltInRegistries.ITEM, SLIME_AXE_KEY, SLIME_AXE);
		Registry.register(BuiltInRegistries.ITEM, SLIME_PICKAXE_KEY, SLIME_PICKAXE);
		Registry.register(BuiltInRegistries.ITEM, SLIME_SHOVEL_KEY, SLIME_SHOVEL);
		Registry.register(BuiltInRegistries.ITEM, SLIME_HOE_KEY, SLIME_HOE);
		Registry.register(BuiltInRegistries.ITEM, SLIME_HELMET_KEY, SLIME_HELMET);
		Registry.register(BuiltInRegistries.ITEM, SLIME_CHESTPLATE_KEY, SLIME_CHESTPLATE);
		Registry.register(BuiltInRegistries.ITEM, SLIME_LEGGINGS_KEY, SLIME_LEGGINGS);
		Registry.register(BuiltInRegistries.ITEM, SLIME_BOOTS_KEY, SLIME_BOOTS);
		Registry.register(BuiltInRegistries.ITEM, SLIME_GRENADE_KEY, SLIME_GRENADE);

		SLIME_GRENADE_ENTITY = Registry.register(
				BuiltInRegistries.ENTITY_TYPE,
				SLIME_GRENADE_ENTITY_KEY,
				EntityType.Builder.<SlimeGrenadeEntity>of(SlimeGrenadeEntity::new, MobCategory.MISC)
						.noLootTable()
						.sized(0.25F, 0.25F)
						.clientTrackingRange(4)
						.updateInterval(10)
						.build(SLIME_GRENADE_ENTITY_KEY));

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

		// --- Knockback Resist scales with how many slime armor pieces are worn ---
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
					player.setDeltaMovement(
							player.getDeltaMovement().x,
							-prevY * 0.8,
							player.getDeltaMovement().z);
					player.hurtMarked = true;   // forces the client to accept the new velocity
				}

				LAST_FALL_SPEED.put(player.getUUID(), nowY);
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

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}