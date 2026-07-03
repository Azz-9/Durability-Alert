package me.Azz_9.durability_alert.compat;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;

import java.util.ArrayList;
import java.util.Optional;

import me.Azz_9.durability_alert.Config;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.*;
import me.shedaniel.clothconfig2.impl.builders.DropdownMenuBuilder;

public class ClothConfigCompat {

	public static Screen buildClothConfigScreen(Screen parent) {
		ConfigBuilder builder = ConfigBuilder.create()
				.setParentScreen(parent)
				.setTitle(Component.translatable("durability_alert.config.title"))
				.setSavingRunnable(Config::save);

		ConfigCategory general = builder.getOrCreateCategory(Component.translatable("durability_alert.config.category.general"));

		ConfigEntryBuilder entryBuilder = builder.entryBuilder();

		BooleanListEntry enabledEntry = entryBuilder
				.startBooleanToggle(
						Component.translatable("durability_alert.config.enabled"),
						Config.INSTANCE.enabled
				)
				.setDefaultValue(true)
				.setSaveConsumer(b -> Config.INSTANCE.enabled = b)
				.build();

		IntegerSliderEntry thresholdSlider = entryBuilder
				.startIntSlider(
						Component.translatable("durability_alert.config.threshold"),
						Config.INSTANCE.threshold, 0, 100
				)
				.setDefaultValue(10)
				.setSaveConsumer(i -> Config.INSTANCE.threshold = i)
				.setRequirement(enabledEntry::getValue)
				.build();

		EnumListEntry<Config.AlertType> alertTypeEntry = entryBuilder
				.startEnumSelector(
						Component.translatable("durability_alert.config.alert_type"),
						Config.AlertType.class,
						Config.INSTANCE.alertType
				)
				.setDefaultValue(Config.AlertType.SOUND_AND_MESSAGE)
				.setEnumNameProvider(type -> Component.translatable(((Config.AlertType) type).getTranslationKey()))
				.setSaveConsumer(type -> Config.INSTANCE.alertType = type)
				.setRequirement(enabledEntry::getValue)
				.build();

		DropdownBoxEntry<Identifier> soundEntry = entryBuilder
				.startDropdownMenu(
						Component.translatable("durability_alert.config.sound"),
						DropdownMenuBuilder.TopCellElementBuilder.of(Config.INSTANCE.sound.location(), string -> {
							try {
								Identifier identifier = Identifier.parse(string);
								if (BuiltInRegistries.SOUND_EVENT.getOptional(identifier).isPresent()) {
									return identifier;
								}
							} catch (Exception _) {
							}

							return null;
						}, identifier -> Component.literal(identifier.toString())),
						DropdownMenuBuilder.CellCreatorBuilder.of(160, 10)
				)
				.setDefaultValue(SoundEvents.ANVIL_LAND.location())
				.setSelections(() -> BuiltInRegistries.SOUND_EVENT.keySet().iterator())
				.setRequirement(() -> enabledEntry.getValue() && !alertTypeEntry.getValue().equals(Config.AlertType.MESSAGE))
				.setSaveConsumer(value -> Config.INSTANCE.sound = BuiltInRegistries.SOUND_EVENT.getValue(value))
				.build();

		BooleanListEntry checkArmorPiecesEntry = entryBuilder
				.startBooleanToggle(
						Component.translatable("durability_alert.config.check_armor_pieces"),
						Config.INSTANCE.checkArmorPieces
				)
				.setDefaultValue(true)
				.setSaveConsumer(b -> Config.INSTANCE.checkArmorPieces = b)
				.setRequirement(enabledEntry::getValue)
				.build();

		BooleanListEntry checkElytraOnlyEntry = entryBuilder
				.startBooleanToggle(
						Component.translatable("durability_alert.config.check_elytra_only"),
						Config.INSTANCE.checkElytraOnly
				)
				.setDefaultValue(true)
				.setSaveConsumer(b -> Config.INSTANCE.checkElytraOnly = b)
				.setRequirement(() -> enabledEntry.getValue() && !checkArmorPiecesEntry.getValue())
				.build();

		IntegerListEntry minAlertIntervalSecondsField = entryBuilder
				.startIntField(
						Component.translatable("durability_alert.config.min_alert_interval"),
						Config.INSTANCE.minAlertIntervalSeconds
				)
				.setDefaultValue(60)
				.setMin(0)
				.setMax(600)
				.setSaveConsumer(i -> Config.INSTANCE.minAlertIntervalSeconds = i)
				.setRequirement(enabledEntry::getValue)
				.build();

		EnumListEntry<Config.ListType> listTypeEntry = entryBuilder.startEnumSelector(
						Component.translatable("durability_alert.config.list_type"),
						Config.ListType.class,
						Config.INSTANCE.listType
				)
				.setEnumNameProvider(type -> Component.translatable(((Config.ListType) type).getTranslationKey()))
				.setDefaultValue(Config.ListType.BLACKLIST)
				.setSaveConsumer(listType -> Config.INSTANCE.listType = listType)
				.setRequirement(enabledEntry::getValue)
				.build();

		StringListListEntry itemListEntry = entryBuilder.startStrList(
						Component.translatable("durability_alert.config.item_list"),
						Config.INSTANCE.itemList.stream().map(item -> BuiltInRegistries.ITEM.getKey(item).toString()).toList()
				)
				.setDefaultValue(new ArrayList<>())
				.setSaveConsumer(strings ->
						Config.INSTANCE.itemList = strings
								.stream()
								.filter(string -> {
									try {
										Identifier.parse(string);
										return true;
									} catch (Exception _) {
										return false;
									}
								})
								.map(string -> BuiltInRegistries.ITEM.getValue(Identifier.parse(string)))
								.toList())
				.setCellErrorSupplier(string -> {
					try {
						if (BuiltInRegistries.ITEM.containsKey(Identifier.parse(string))) {
							return Optional.empty();
						}
					} catch (Exception _) {
					}
					return Optional.of(Component.translatable("durability_alert.config.item_list.invalid_item"));
				})
				.setRequirement(enabledEntry::getValue)
				.build();

		general.addEntry(enabledEntry);
		general.addEntry(thresholdSlider);
		general.addEntry(alertTypeEntry);
		general.addEntry(soundEntry);
		general.addEntry(checkArmorPiecesEntry);
		general.addEntry(checkElytraOnlyEntry);
		general.addEntry(minAlertIntervalSecondsField);
		general.addEntry(listTypeEntry);
		general.addEntry(itemListEntry);

		return builder.build();
	}
}
