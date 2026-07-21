package net.tech.cortisolmod.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;
import net.tech.cortisolmod.CortisolMod;
import net.tech.cortisolmod.item.ModItems;

import java.util.List;
import java.util.function.Consumer;

public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {

    public ModRecipeProvider(PackOutput pOutput) {
        super(pOutput);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> pWriter) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SCROLLING_PHONE.get())
                .pattern("III")
                .pattern("ICI")
                .pattern("IRI")
                .define('I', Items.IRON_INGOT)
                .define('C', ModItems.CORTILIUM.get())
                .define('R', Items.REDSTONE)
                .unlockedBy(getHasName(ModItems.CORTILIUM.get()),has(ModItems.CORTILIUM.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.CORTILIUM_INGOT.get())
                .pattern("CCC")
                .pattern("CSS")
                .pattern("SS ")
                .define('S', Items.NETHERITE_SCRAP)
                .define('C', ModItems.CORTILIUM.get())
                .unlockedBy(getHasName(ModItems.CORTILIUM.get()),has(ModItems.CORTILIUM.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.CORTISOL_SWORD.get())
                .pattern("I")
                .pattern("I")
                .pattern("S")
                .define('S', Items.STICK)
                .define('I', ModItems.CORTILIUM_INGOT.get())
                .unlockedBy(getHasName(ModItems.CORTILIUM_INGOT.get()),has(ModItems.CORTILIUM_INGOT.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.LOW_CORTISOL_BOW.get())
                .pattern("I")
                .pattern("B")
                .pattern("I")
                .define('B', Items.BOW)
                .define('I', ModItems.CORTILIUM_INGOT.get())
                .unlockedBy(getHasName(ModItems.CORTILIUM_INGOT.get()),has(ModItems.CORTILIUM_INGOT.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.LOW_CORTISOL_INJECTOR.get())
            .pattern("GLG")
            .pattern("GCG")
            .pattern(" I ")
            .define('G', Items.GLASS)
            .define('C', ModItems.CORTILIUM.get())
            .define('I', Items.IRON_INGOT)
            .define('L', Items.GRASS)
            .unlockedBy(getHasName(ModItems.CORTILIUM.get()),has(ModItems.CORTILIUM.get()))
            .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.HIGH_CORTISOL_INJECTOR.get())
                .pattern("GBG")
                .pattern("GCG")
                .pattern(" I ")
                .define('G', Items.GLASS)
                .define('C', ModItems.CORTILIUM.get())
                .define('I', Items.IRON_INGOT)
                .define('B', Items.BOOK)
                .unlockedBy(getHasName(ModItems.CORTILIUM.get()),has(ModItems.CORTILIUM.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.CORTISOL_STABILIZER_SERINGE.get())
                .pattern("GCG")
                .pattern("GCG")
                .pattern(" I ")
                .define('G', Items.GLASS)
                .define('C', ModItems.CORTILIUM_INGOT.get())
                .define('I', Items.IRON_INGOT)

                .unlockedBy(getHasName(ModItems.CORTILIUM.get()),has(ModItems.CORTILIUM.get()))
                .save(pWriter);

}



    protected static void oreSmelting(Consumer<FinishedRecipe> pFinishedRecipeConsumer, List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult, float pExperience, int pCookingTIme, String pGroup) {
        oreCooking(pFinishedRecipeConsumer, RecipeSerializer.SMELTING_RECIPE, pIngredients, pCategory, pResult, pExperience, pCookingTIme, pGroup, "_from_smelting");
    }

    protected static void oreBlasting(Consumer<FinishedRecipe> pFinishedRecipeConsumer, List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult, float pExperience, int pCookingTime, String pGroup) {
        oreCooking(pFinishedRecipeConsumer, RecipeSerializer.BLASTING_RECIPE, pIngredients, pCategory, pResult, pExperience, pCookingTime, pGroup, "_from_blasting");
    }

    protected static void oreCooking(Consumer<FinishedRecipe> pFinishedRecipeConsumer, RecipeSerializer<? extends AbstractCookingRecipe> pCookingSerializer, List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult, float pExperience, int pCookingTime, String pGroup, String pRecipeName) {

        for (ItemLike itemlike : pIngredients) {
            SimpleCookingRecipeBuilder.generic(Ingredient.of(new ItemLike[]{itemlike}), pCategory, pResult, pExperience, pCookingTime, pCookingSerializer)
                    .group(pGroup).unlockedBy(getHasName(itemlike), has(itemlike))
                    .save(pFinishedRecipeConsumer,  CortisolMod.MOD_ID + getItemName(pResult) + pRecipeName + "_" + getItemName(itemlike));
        }

    }


}
