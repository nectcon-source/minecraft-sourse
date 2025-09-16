package net.minecraft.world.item.crafting;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/crafting/RecipeManager.class */
public class RecipeManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Logger LOGGER = LogManager.getLogger();
    private Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> recipes;
    private boolean hasErrors;

    public RecipeManager() {
        super(GSON, "recipes");
        this.recipes = ImmutableMap.of();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // net.minecraft.server.packs.resources.SimplePreparableReloadListener
    public void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        this.hasErrors = false;
        Map<RecipeType<?>, ImmutableMap.Builder<ResourceLocation, Recipe<?>>> newHashMap = Maps.newHashMap();
        for (Map.Entry<ResourceLocation, JsonElement> entry : map.entrySet()) {
            ResourceLocation key = entry.getKey();
            try {
                Recipe<?> fromJson = fromJson(key, GsonHelper.convertToJsonObject(entry.getValue(), "top element"));
                newHashMap.computeIfAbsent(fromJson.getType(), recipeType -> {
                    return ImmutableMap.builder();
                }).put(key, fromJson);
            } catch (JsonParseException | IllegalArgumentException e) {
                LOGGER.error("Parsing error loading recipe {}", key, e);
            }
        }
        this.recipes = (Map) newHashMap.entrySet().stream().collect(ImmutableMap.toImmutableMap((v0) -> {
            return v0.getKey();
        }, entry2 -> {
            return ((ImmutableMap.Builder) entry2.getValue()).build();
        }));
        LOGGER.info("Loaded {} recipes", Integer.valueOf(newHashMap.size()));
    }

    public <C extends Container, T extends Recipe<C>> Optional<T> getRecipeFor(RecipeType<T> recipeType, C c, Level level) {
        return byType(recipeType).values().stream().flatMap(recipe -> {
            return Util.toStream(recipeType.tryMatch(recipe, level, c));
        }).findFirst();
    }

    public <C extends Container, T extends Recipe<C>> List<T> getAllRecipesFor(RecipeType<T> recipeType) {
        return (List) byType(recipeType).values().stream().map(recipe -> {
            return recipe;
        }).collect(Collectors.toList());
    }

    public <C extends Container, T extends Recipe<C>> List<T> getRecipesFor(RecipeType<T> recipeType, C c, Level level) {
        return (List) byType(recipeType).values().stream().flatMap(recipe -> {
            return Util.toStream(recipeType.tryMatch(recipe, level, c));
        }).sorted(Comparator.comparing(recipe2 -> {
            return recipe2.getResultItem().getDescriptionId();
        })).collect(Collectors.toList());
    }

    private <C extends Container, T extends Recipe<C>> Map<ResourceLocation, Recipe<C>> byType(RecipeType<T> recipeType) {
        return (Map) this.recipes.getOrDefault(recipeType, Collections.emptyMap());
    }

    public <C extends Container, T extends Recipe<C>> NonNullList<ItemStack> getRemainingItemsFor(RecipeType<T> recipeType, C c, Level level) {
        Optional<T> recipeFor = getRecipeFor(recipeType, c, level);
        if (recipeFor.isPresent()) {
            return recipeFor.get().getRemainingItems(c);
        }
        NonNullList<ItemStack> withSize = NonNullList.withSize(c.getContainerSize(), ItemStack.EMPTY);
        for (int i = 0; i < withSize.size(); i++) {
            withSize.set(i, c.getItem(i));
        }
        return withSize;
    }

    public Optional<? extends Recipe<?>> byKey(ResourceLocation resourceLocation) {
        return (Optional<? extends Recipe<?>>) this.recipes.values().stream().map(map -> {
            return  map.get(resourceLocation);
        }).filter((v0) -> {
            return Objects.nonNull(v0);
        }).findFirst();
    }

    public Collection<Recipe<?>> getRecipes() {
        return (Collection) this.recipes.values().stream().flatMap(map -> {
            return map.values().stream();
        }).collect(Collectors.toSet());
    }

    public Stream<ResourceLocation> getRecipeIds() {
        return this.recipes.values().stream().flatMap(map -> {
            return map.keySet().stream();
        });
    }

    /* JADX WARN: Type inference failed for: r0v6, types: [net.minecraft.world.item.crafting.Recipe, net.minecraft.world.item.crafting.Recipe<?>] */
    public static Recipe<?> fromJson(ResourceLocation resourceLocation, JsonObject jsonObject) {
        String asString = GsonHelper.getAsString(jsonObject, "type");
        return Registry.RECIPE_SERIALIZER.getOptional(new ResourceLocation(asString)).orElseThrow(() -> {
            return new JsonSyntaxException("Invalid or unsupported recipe type '" + asString + "'");
        }).fromJson(resourceLocation, jsonObject);
    }

    public void replaceRecipes(Iterable<Recipe<?>> iterable) {
        this.hasErrors = false;
        Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> newHashMap = Maps.newHashMap();
        iterable.forEach(recipe -> {
            if (((Map) newHashMap.computeIfAbsent(recipe.getType(), recipeType -> {
                return Maps.newHashMap();
            })).put(recipe.getId(), recipe) != null) {
                throw new IllegalStateException("Duplicate recipe ignored with ID " + recipe.getId());
            }
        });
        this.recipes = ImmutableMap.copyOf(newHashMap);
    }
}
