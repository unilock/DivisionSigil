package com.landmaster.divisionsigil.jei;

import com.landmaster.divisionsigil.DivisionSigil;
import com.landmaster.divisionsigil.stabilization_recipe.StabilizationRitualRecipe;
import com.landmaster.divisionsigil.transmutation.HoeTransmutation;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;

import javax.annotation.Nonnull;

@JeiPlugin
public class DivisionSigilJEI implements IModPlugin {
    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(DivisionSigil.MODID, "jei_plugin");

    public static final RecipeType<HoeTransmutation> HOE_TRANSMUTATION = RecipeType.create(DivisionSigil.MODID, "hoe_transmutation", HoeTransmutation.class);
    public static final RecipeType<StabilizationRitualRecipe> STABILIZATION_RITUAL = RecipeType.create(DivisionSigil.MODID, "stabilization_ritual", StabilizationRitualRecipe.class);

    @Nonnull
    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        var helpers = registration.getJeiHelpers();
        registration.addRecipeCategories(new HoeTransmutationCategory(helpers.getGuiHelper()));
        registration.addRecipeCategories(new StabilizationRitualCategory(helpers.getGuiHelper()));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(DivisionSigil.REVERSING_HOE, HOE_TRANSMUTATION);
        registration.addRecipeCatalyst(DivisionSigil.DIVISION_SIGIL, STABILIZATION_RITUAL);
        registration.addRecipeCatalyst(DivisionSigil.PSEUDO_INVERSION_SIGIL, STABILIZATION_RITUAL);
    }

    @Override
    public void registerRecipes(@Nonnull IRecipeRegistration registration) {
        var level = Minecraft.getInstance().level;
        if (level == null) {
            throw new NullPointerException("Minecraft.getInstance().level is null");
        }
        registration.addRecipes(HOE_TRANSMUTATION, level.getRecipeManager()
                .getAllRecipesFor(DivisionSigil.HOE_TRANSMUTATION_TYPE.get())
                .stream().map(RecipeHolder::value).toList());

        registration.addRecipes(STABILIZATION_RITUAL, level.getRecipeManager()
                .getAllRecipesFor(DivisionSigil.STABILIZATION_RITUAL_TYPE.get())
                .stream().map(RecipeHolder::value).toList());
    }
}
