package com.landmaster.divisionsigil.jei;

import com.landmaster.divisionsigil.DivisionSigil;
import com.landmaster.divisionsigil.stabilization_recipe.DirectionalElement;
import com.landmaster.divisionsigil.stabilization_recipe.StabilizationRitualRecipe;
import com.landmaster.divisionsigil.stabilization_recipe.StandardStabilizationRitualRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

public class StabilizationRitualCategory extends AbstractRecipeCategory<StabilizationRitualRecipe> {
    private final IGuiHelper guiHelper;

    public StabilizationRitualCategory(IGuiHelper helper) {
        super(DivisionSigilJEI.STABILIZATION_RITUAL,
                Component.translatable("item.divisionsigil.pseudo_inversion_sigil"),
                helper.createDrawableItemLike(DivisionSigil.PSEUDO_INVERSION_SIGIL),
                180,
                140);

        guiHelper = helper;
    }

    @Override
    public void setRecipe(@Nonnull IRecipeLayoutBuilder builder, @Nonnull StabilizationRitualRecipe recipe, @Nonnull IFocusGroup focuses) {
        if (recipe instanceof StandardStabilizationRitualRecipe stdRecipe) {
            for (DirectionalElement element: DirectionalElement.VALUES) {
                for (var ingredient: stdRecipe.ingredients().get(element)) {
                    builder.addInputSlot().addIngredients(ingredient);
                }
            }

            builder.addOutputSlot(getWidth() - 16, getHeight() - 16)
                    .setStandardSlotBackground()
                    .addItemLike(DivisionSigil.PSEUDO_INVERSION_SIGIL);
        }
    }

    @Override
    public void createRecipeExtras(@Nonnull IRecipeExtrasBuilder builder, @Nonnull StabilizationRitualRecipe recipe, @Nonnull IFocusGroup focuses) {
        if (recipe instanceof StandardStabilizationRitualRecipe(
                Map<DirectionalElement, List<Ingredient>> ingredients,
                Map<DirectionalElement, Integer> requirements
        )) {
            int offset = 0;
            var recipeSlots = builder.getRecipeSlots();
            var inputSlots = recipeSlots.getSlots(RecipeIngredientRole.INPUT);
            for (int i=0; i<DirectionalElement.VALUES.length; ++i) {
                var element = DirectionalElement.VALUES[i];
                int sz = ingredients.get(element).size();
                int x = (i % 2) * getWidth() / 2;
                int y = (i / 2) * 55;
                builder.addText(Component.translatable(element.getTranslateKey(), requirements.get(element)), 80, 10)
                        .setPosition(x, y)
                        .setColor(0xFF000000)
                        .setLineSpacing(0);
                builder.addScrollGridWidget(inputSlots.subList(offset, offset + sz), 4, 2)
                        .setPosition(x, y + 14);
                offset += sz;
            }
        }
    }

    @Override
    public boolean isHandled(@Nonnull StabilizationRitualRecipe recipe) {
        return recipe instanceof StandardStabilizationRitualRecipe;
    }
}
