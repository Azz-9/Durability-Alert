package me.Azz_9.durability_alert;

import static me.Azz_9.durability_alert.Constants.MOD_ID;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;

import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import me.Azz_9.durability_alert.platform.Services;

public class Config {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeHierarchyAdapter(SoundEvent.class, new TypeAdapter<SoundEvent>() {
                @Override
                public void write(JsonWriter out, SoundEvent value) throws IOException {
                    if (value == null) out.nullValue();
                    else out.value(value.location().toString());
                }

                @Override
                public SoundEvent read(JsonReader in) throws IOException {
                    if (in.peek() == JsonToken.NULL) {
                        in.nextNull();
                        return SoundEvents.ANVIL_LAND;
                    }
                    SoundEvent soundEvent = null;
                    try {
                        soundEvent = BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse(in.nextString()));
                    } catch (Exception _) {
                    }
                    if (soundEvent == null) {
                        return SoundEvents.ANVIL_LAND;
                    }
                    return soundEvent;
                }
            })
            .registerTypeHierarchyAdapter(Item.class, new TypeAdapter<Item>() {
                @Override
                public void write(JsonWriter out, Item value) throws IOException {
                    if (value == null) out.nullValue();
                    else out.value(BuiltInRegistries.ITEM.getKey(value).toString());
                }

                @Override
                public Item read(JsonReader in) throws IOException {
                    if (in.peek() == JsonToken.NULL) {
                        in.nextNull();
                        return null;
                    }
                    return BuiltInRegistries.ITEM.getValue(Identifier.parse(in.nextString()));
                }
            })
            .create();
    private static final Path CONFIG_FILE = Services.PLATFORM.getConfigDir().resolve(MOD_ID + ".json");

    public boolean enabled = true;
    public int threshold = 10;
    public AlertType alertType = AlertType.SOUND_AND_MESSAGE;
    public SoundEvent sound = SoundEvents.ANVIL_LAND;
	public float pitch = 2.0f; // 0.5..2
	public float volume = 1.0f; // 0..1
    public boolean checkArmorPieces = true;
    public boolean checkElytraOnly = false;
    public int minAlertIntervalSeconds = 60;
    public ListType listType = ListType.BLACKLIST;
    public List<Item> itemList = new ArrayList<>();

    public static Config INSTANCE = new Config();

    public static void save() {
        DurabilityLogger.info("Saving config...");

        try (Writer writer = Files.newBufferedWriter(CONFIG_FILE)) {
            GSON.toJson(INSTANCE, writer);
        } catch (IOException e) {
            DurabilityLogger.error("Failed to save config file : {}", e.getMessage());
            return;
        }

        DurabilityLogger.info("Config successfully saved!");
    }

    public static void load() {
        DurabilityLogger.info("Loading config...");
        if (!Files.exists(CONFIG_FILE)) {
            DurabilityLogger.info("Config file does not exist!");
            Config.save();
            return;
        }

        try (Reader reader = Files.newBufferedReader(CONFIG_FILE)) {
            INSTANCE = GSON.fromJson(reader, Config.class);
        } catch (IOException e) {
            DurabilityLogger.error("Failed to load config file : {}", e.getMessage());
            return;
        }

        DurabilityLogger.info("Config successfully loaded!");
    }

    public enum AlertType {
        SOUND("durability_alert.alert_type.sound"),
        MESSAGE("durability_alert.alert_type.message"),
        SOUND_AND_MESSAGE("durability_alert.alert_type.sound_and_message");

        private final @NonNull String translationKey;

        AlertType(@NonNull String translationKey) {
            this.translationKey = translationKey;
        }

        public @NonNull String getTranslationKey() {
            return translationKey;
        }
    }

    public enum ListType {
        WHITELIST("durability_alert.list_type.whitelist"),
        BLACKLIST("durability_alert.list_type.blacklist");

        private final @NonNull String translationKey;

        ListType(@NonNull String translationKey) {
            this.translationKey = translationKey;
        }

        public @NonNull String getTranslationKey() {
            return translationKey;
        }
    }
}