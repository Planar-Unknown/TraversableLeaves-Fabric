package com.dreu.traversableleaves.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.dreu.traversableleaves.TraversableLeaves.LOGGER;
import static com.dreu.traversableleaves.TraversableLeaves.MODID;

@SuppressWarnings({"SameParameterValue", "OptionalGetWithoutIsPresent"})
public class TLConfig {
  public static boolean configNeedsRepair = false;
  static final String fileName = "config/" + MODID + "/general.json";
  static final String DEFAULT_CONFIG_STRING = """
      # To reset this config to default, delete this file and rerun the game.
      {
      # Movement Speed penalty while traversing leaves, 0 = no penalty, 99 = 99% slower (Range: 0 - 99) | Default: 50
        "SpeedPenalty": 50,
      
      # Whether Armor value reduces movement penalty | Default: true
        "ArmorHelps": true,
      
      # How much armor is required to completely negate the SpeedPenalty (Range: 0 - 255) | Default: 20
        "ArmorCap": 20,
      
      # Whether leaves behave like ladders | Default: true
        "CanClimb": true,
      
      # List of traversable blocks. | Default: ["#minecraft:leaves"]
        "Traversable": [
          "#minecraft:leaves"
        ],
      
      # Whether Entities is a "WHITELIST" or a "BLACKLIST" | Default: "BLACKLIST"
        "Entities-Whitelist-Blacklist": "BLACKLIST",
      
      # List of Entities that can/cannot traverse blocks | Default: []
        "Entities": []
      }
      """;

  public static void repairConfig() {
    LOGGER.info("An issue was found with config: {} | You can find a copy of faulty config at: {} | Repairing...", fileName, fileName.replace(".json", "_faulty.json"));
    Path sourcePath = Paths.get(fileName);
    Path destinationPath = Paths.get(fileName.replace(".json", "_faulty.json"));
    try {
      Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      LOGGER.warn("Exception during caching of faulty Traversable Leaves config | Exception: {}", e.getMessage());
    }
    try (FileWriter writer = new FileWriter(new File(fileName).getAbsolutePath())) {
      StringBuilder contents = new StringBuilder()
          .append("# To reset this config to default, delete this file and rerun the game.\n")
          .append("{\n")
          .append("# Movement Speed penalty while traversing leaves, 0 = no penalty, 99 = 99% slower (Range: 0 - 99) | Default: 50\n")
          .append("  \"SpeedPenalty\": ").append(CACHED_SPEED_PENALTY).append(",\n")
          .append("# Whether Armor value reduces movement penalty | Default: true\n")
          .append("  \"ArmorHelps\": ").append(CACHED_ARMOR_HELPS).append(",\n")
          .append("# How much armor is required to completely negate the SpeedPenalty (Range: 0 - 255) | Default: 20\n")
          .append("  \"ArmorCap\": ").append(CACHED_ARMOR_CAP).append(",\n")
          .append("# Whether leaves behave like ladders | Default: true\n")
          .append("  \"CanClimb\": ").append(CAN_CLIMB).append(",\n")
          .append("# List of traversable blocks. | Default: [\"#minecraft:leaves\"]\n")
          .append("  \"Traversable\": [\n");

      int i = 0;
      for (String key : BLOCKS_CACHE) {
        contents.append("    \"").append(key).append("\"");
        if (i != BLOCKS_CACHE.size() - 1) contents.append(",");
        contents.append("\n");
        i++;
      }

      contents.append("  ],\n")
          .append("# Whether Entities is a \"WHITELIST\" or a \"BLACKLIST\" | Default: \"BLACKLIST\"\n")
          .append("  \"Entities-Whitelist-Blacklist\": ")
          .append(IS_ENTITIES_WHITELIST ? "\"WHITELIST\"" : "\"BLACKLIST\"").append(",\n")
          .append("# List of Entities that can/cannot traverse blocks | Default: []\n")
          .append("  \"Entities\": [\n");

      i = 0;
      for (String key : ENTITIES_CACHE) {
        contents.append("    \"").append(key).append("\"");
        if (i != ENTITIES_CACHE.size() - 1) contents.append(",");
        contents.append("\n");
        i++;
      }

      contents.append("  ]\n")
          .append("}\n");

      writer.write(contents.toString());
    } catch (IOException e) {
      LOGGER.warn("Exception during config repair: {}", e.getMessage());
    }
  }

  private static final JsonObject DEFAULT_CONFIG = JsonParser.parseString(DEFAULT_CONFIG_STRING).getAsJsonObject();
  private static JsonObject CONFIG;

  public static final Set<ResourceLocation> TL_BLOCKS = new HashSet<>();
  public static final Set<ResourceLocation> TL_ENTITIES = new HashSet<>();

  private static final Set<String> BLOCKS_CACHE = new HashSet<>();
  private static final Set<String> ENTITIES_CACHE = new HashSet<>();

  public static boolean IS_ENTITIES_WHITELIST;
  private static int CACHED_SPEED_PENALTY;
  public static float MOVEMENT_MULTIPLIER;
  public static boolean CACHED_ARMOR_HELPS;
  private static int CACHED_ARMOR_CAP;
  public static float ARMOR_SCALE_FACTOR;
  public static boolean CAN_CLIMB;

  public static void parse() {
    CONFIG = parseConfigOrDefault();
  }

  public static void populate() {
    ENTITIES_CACHE.clear();
    BLOCKS_CACHE.clear();
    TL_BLOCKS.clear();
    TL_ENTITIES.clear();
    CACHED_SPEED_PENALTY = getClampedSpeedPenalty();
    MOVEMENT_MULTIPLIER = (100 - CACHED_SPEED_PENALTY) * 0.01f;
    CACHED_ARMOR_HELPS = getBooleanOrDefault("ArmorHelps");
    CACHED_ARMOR_CAP = getClampedArmorCap();
    ARMOR_SCALE_FACTOR = CACHED_ARMOR_HELPS ? (1 - MOVEMENT_MULTIPLIER) * (1.0f / CACHED_ARMOR_CAP) : 0;
    IS_ENTITIES_WHITELIST = getWhitelistBlacklist();
    CAN_CLIMB = getBooleanOrDefault("CanClimb");

    Set<ResourceLocation> toRemove = new HashSet<>();
    getStringListOrDefault("Traversable").forEach((configKey) -> {
      BLOCKS_CACHE.add(configKey);
      if (configKey.startsWith("-")) {
        if (configKey.charAt(1) == '#') {
          if (isValidBlockTag(configKey.substring(2))) {
            for (Holder<Block> block : BuiltInRegistries.BLOCK.getTag(TagKey.create(BuiltInRegistries.BLOCK.key(), new ResourceLocation(configKey.substring(2)))).get().stream().toList())
              toRemove.add(BuiltInRegistries.BLOCK.getKey(block.value()));
          }
        } else if (isValidBlock(configKey.substring(1))) {
          toRemove.add(new ResourceLocation(configKey.substring(1)));
        }
      } else if (configKey.startsWith("#")) {
        if (isValidBlockTag(configKey.substring(1))) {
          for (Holder<Block> block : BuiltInRegistries.BLOCK.getTag(TagKey.create(Registries.BLOCK, new ResourceLocation(configKey.substring(1)))).get())
            TL_BLOCKS.add(BuiltInRegistries.BLOCK.getKey(block.value()));
        }
      } else if (isValidBlock(configKey)) {
        TL_BLOCKS.add(new ResourceLocation(configKey));
      }
    });
    TL_BLOCKS.removeAll(toRemove);

    toRemove.clear();
    getStringListOrDefault("Entities").forEach((configKey) -> {
      ENTITIES_CACHE.add(configKey);
      if (configKey.startsWith("-")) {
        if (configKey.charAt(1) == '#') {
          if (isValidEntityTag(configKey.substring(2))) {
            for (Holder<EntityType<?>> entityType : BuiltInRegistries.ENTITY_TYPE.getTag(TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(configKey.substring(2)))).get())
              toRemove.add(BuiltInRegistries.ENTITY_TYPE.getKey(entityType.value()));
          }
        } else if (isValidEntity(configKey.substring(1))) {
          toRemove.add(new ResourceLocation(configKey.substring(1)));
        }
      } else if (configKey.startsWith("#")) {
        if (isValidEntityTag(configKey.substring(1))) {
          for (Holder<EntityType<?>> entityType : BuiltInRegistries.ENTITY_TYPE.getTag(TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(configKey.substring(1)))).get())
            TL_ENTITIES.add(BuiltInRegistries.ENTITY_TYPE.getKey(entityType.value()));
        }
      } else if (isValidEntity(configKey)) {
        TL_ENTITIES.add(new ResourceLocation(configKey));
      }
    });
    TL_ENTITIES.removeAll(toRemove);

    if (configNeedsRepair) repairConfig();
  }

  private static List<String> getStringListOrDefault(String key) {
    JsonElement jsonElement = CONFIG.get(key);
    if (jsonElement.isJsonArray())
      return CONFIG.get(key).getAsJsonArray().asList().stream().map(JsonElement::getAsString).toList();
    else {
      LOGGER.warn("Invalid value for \"{}\" in Config: {{}} | Expected: [List], but got: [{}] | Using default...", key, fileName, CONFIG.get(key).getClass().getTypeName());
      configNeedsRepair = true;
    }
    return DEFAULT_CONFIG.getAsJsonArray(key).asList().stream().map(JsonElement::getAsString).toList();
  }

  private static boolean getWhitelistBlacklist() {
    String whitelistBlacklist = getStringOrDefault("Entities-Whitelist-Blacklist").toUpperCase();
    if (whitelistBlacklist.equals("WHITELIST")) return true;
    else if (whitelistBlacklist.equals("BLACKLIST")) return false;
    else {
      LOGGER.warn("Invalid value for Entities-Whitelist-Blacklist in Config: {} | Expected: \"WHITELIST\" or \"BLACKLIST\", but got: \"{}\" | Using default (\"BLACKLIST\")...", fileName, whitelistBlacklist);
      return false;
    }
  }

  private static int getClampedArmorCap() {
    int armorCap = getIntOrDefault("ArmorCap");
    if (armorCap < 0) {
      LOGGER.warn("Invalid ArmorCap: '{}' in Config: {{}} | Must be greater than 0 | Setting to 0...", armorCap, fileName);
      armorCap = 0;
    } else if (armorCap > 255) {
      LOGGER.warn("Invalid ArmorCap: '{}' in Config: {{}} | Must be less than 255 | Setting to 255...", armorCap, fileName);
      armorCap = 255;
    }
    return armorCap;
  }

  private static int getClampedSpeedPenalty() {
    int speedPenalty = getIntOrDefault("SpeedPenalty");
    if (speedPenalty < 0) {
      LOGGER.warn("Invalid SpeedPenalty: '{}' in Config: {{}} | Must be greater than 0 | Setting to 0...", speedPenalty, fileName);
      speedPenalty = 0;
    } else if (speedPenalty > 99) {
      LOGGER.warn("Invalid SpeedPenalty: '{}' in Config: {{}} | Must be less than 99 | Setting to 99...", speedPenalty, fileName);
      speedPenalty = 99;
    }
    return speedPenalty;
  }

  private static boolean isValidEntityTag(String tagId) {
    if (!ResourceLocation.isValidResourceLocation(tagId)) {
      LOGGER.warn("Not a valid Entity Tag ResourceLocation: <{}> declared in Config: [{}] | Skipping Tag...", tagId, fileName);
      return false;
    }
    if (BuiltInRegistries.ENTITY_TYPE.getTag(TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(tagId))).isEmpty()) {
      LOGGER.warn("Not an existing Entity Tag: <{}> declared in Config: [{}] | Skipping Tag...", tagId, fileName);
      return false;
    }
    return true;
  }

  private static boolean isValidEntity(String entityId) {
    if (!ResourceLocation.isValidResourceLocation(entityId)) {
      LOGGER.warn("Not a valid Entity ResourceLocation: <{}> declared in Config: [{}] | Skipping Block...", entityId, fileName);
      return false;
    }
    if (!FabricLoader.getInstance().isModLoaded(entityId.split(":")[0])) {
      LOGGER.warn("Config: [{}] declared Entity: <{}> but Mod: '{{}}' is not loaded | Skipping Block...", fileName, entityId, entityId.split(":")[0]);
      return false;
    }
    if (!BuiltInRegistries.ENTITY_TYPE.containsKey(new ResourceLocation(entityId))) {
      LOGGER.warn("Config: [{}] declared Entity: <{}> which does not exist, check for typos! | Skipping Block...", fileName, entityId);
      return false;
    }
    return true;
  }

  private static boolean isValidBlockTag(String tagId) {
    if (!ResourceLocation.isValidResourceLocation(tagId)) {
      LOGGER.warn("Not a valid Block Tag ResourceLocation: <{}> declared in Config: [{}] | Skipping Tag...", tagId, fileName);
      return false;
    }
    if (BuiltInRegistries.BLOCK.getTag(TagKey.create(Registries.BLOCK, new ResourceLocation(tagId))).isEmpty()) {
      LOGGER.warn("Not an existing Block Tag: <{}> declared in Config: [{}] | Skipping Tag...", tagId, fileName);
      return false;
    }
    return true;
  }

  private static boolean isValidBlock(String blockId) {
    if (!ResourceLocation.isValidResourceLocation(blockId)) {
      LOGGER.warn("Not a valid Block ResourceLocation: <{}> declared in Config: [{}] | Skipping Block...", blockId, fileName);
      return false;
    }
    if (!FabricLoader.getInstance().isModLoaded(blockId.split(":")[0])) {
      LOGGER.warn("Config: [{}] declared Block: <{}> but Mod: '{{}}' is not loaded | Skipping Block...", fileName, blockId, blockId.split(":")[0]);
      return false;
    }
    if (!BuiltInRegistries.BLOCK.containsKey(new ResourceLocation(blockId))) {
      LOGGER.warn("Config: [{}] declared Block: <{}> which does not exist, check for typos! | Skipping Block...", fileName, blockId);
      return false;
    }
    return true;
  }

  static boolean getBooleanOrDefault(String key) {
    try {
      if ((CONFIG.get(key) == null)) {
        keyNotFound(key);
        configNeedsRepair = true;
        return DEFAULT_CONFIG.get(key).getAsBoolean();
      }
      return CONFIG.get(key).getAsBoolean();
    } catch (Exception e) {
      LOGGER.error("Value: [{}] for [{}] is an invalid type in Config: {} | Expected: [Boolean] but got: [{}] | Marking config file for repair...", CONFIG.get(key), key, fileName, CONFIG.get(key).getClass().getTypeName());
      configNeedsRepair = true;
      return DEFAULT_CONFIG.get(key).getAsBoolean();
    }
  }

  private static void keyNotFound(String key) {
    LOGGER.error("Key [{}] is missing from Config: [{}] | Marking config file for repair...", key, fileName);
  }

  static int getIntOrDefault(String key) {
    try {
      if ((CONFIG.get(key) == null)) {
        keyNotFound(key);
        configNeedsRepair = true;
        return DEFAULT_CONFIG.get(key).getAsInt();
      }
      return CONFIG.get(key).getAsInt();
    } catch (Exception e) {
      LOGGER.error("Value: [{}] for [{}] is an invalid type in Config: {} | Expected: [Integer] but got: [{}] | Marking config file for repair...", CONFIG.get(key), key, fileName, CONFIG.get(key).getClass().getTypeName());
      configNeedsRepair = true;
      return DEFAULT_CONFIG.get(key).getAsInt();
    }
  }

  static String getStringOrDefault(String key) {
    try {
      if ((CONFIG.get(key) == null)) {
        keyNotFound(key);
        configNeedsRepair = true;
        return DEFAULT_CONFIG.get(key).getAsString();
      }
      return CONFIG.get(key).getAsString();
    } catch (Exception e) {
      LOGGER.error("Value: [{}] for [{}] is an invalid type in Config: {} | Expected: [String] but got: [{}] | Marking config file for repair...", CONFIG.get(key), key, fileName, CONFIG.get(key).getClass().getTypeName());
      configNeedsRepair = true;
      return DEFAULT_CONFIG.get(key).getAsString();
    }
  }

  static JsonObject parseConfigOrDefault() {
    try {
      Files.createDirectories(Path.of("config/" + MODID));
    } catch (Exception ignored) {
    }

    Path path = Path.of(fileName).toAbsolutePath();
    if (Files.exists(path)) {
      try (Reader reader = Files.newBufferedReader(path)) {
        return JsonParser.parseReader(reader).getAsJsonObject();
      } catch (Exception e) {
        LOGGER.error("Encountered exception during parsing of Config: {{}}, moving config to {{}} and rewriting to builtin default instead", fileName, fileName.replace(".json", "_faulty.json"));
        configNeedsRepair = true;
        return DEFAULT_CONFIG;
      }
    } else {
      LOGGER.info("No config found at: {} | Generating new one...", fileName);
      try (FileWriter fileWriter = new FileWriter(path.toFile())) {
        fileWriter.write(DEFAULT_CONFIG_STRING);
      } catch (Exception ignored) {
      }
      return DEFAULT_CONFIG;
    }
  }
}