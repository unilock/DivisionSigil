package com.landmaster.divisionsigil.jei;

import com.landmaster.divisionsigil.DivisionSigil;
import com.landmaster.divisionsigil.transmutation.HoeTransmutation;
import com.landmaster.divisionsigil.transmutation.StandardHoeTransmutation;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class HoeTransmutationCategory extends AbstractRecipeCategory<HoeTransmutation> {
    private final IGuiHelper guiHelper;

    public HoeTransmutationCategory(IGuiHelper helper) {
        super(DivisionSigilJEI.HOE_TRANSMUTATION,
                Component.translatable("item.divisionsigil.reversing_hoe"),
                helper.createDrawableItemLike(DivisionSigil.REVERSING_HOE),
                130,
                30);

        guiHelper = helper;
    }

    @Override
    public void setRecipe(@Nonnull IRecipeLayoutBuilder builder, @Nonnull HoeTransmutation recipe, @Nonnull IFocusGroup focuses) {
        if (recipe instanceof StandardHoeTransmutation transmutation) {
            transmutation.inputBlock().blocks().ifPresent(blockSet -> {
                builder.addInputSlot(0, 0).addItemStacks(
                    blockSet.stream().map(blockHolder -> new ItemStack(blockHolder.value().asItem())).toList()
                );
                builder.addOutputSlot(getWidth() - 16, 0).addItemStacks(
                        transmutation.outputBlock().unwrap().stream()
                                .map(state -> new ItemStack(state.data().getBlock().asItem()))
                                .toList()
                );
            });
        }
    }

    @Override
    public boolean isHandled(@Nonnull HoeTransmutation recipe) {
        return recipe instanceof StandardHoeTransmutation;
    }

    @Override
    public void createRecipeExtras(@Nonnull IRecipeExtrasBuilder builder, @Nonnull HoeTransmutation recipe, @Nonnull IFocusGroup focuses) {
        if (recipe instanceof StandardHoeTransmutation standardRecipe && standardRecipe.biomeList().size() > 0) {
            List<FormattedText> text = new ArrayList<>();
            text.add(Component.translatable(standardRecipe.isBiomeAllowlist() ? "gui.divisionsigil.biome_allowlist" : "gui.divisionsigil.biome_denylist"));
            for (var biome: standardRecipe.biomeList()) {
                biome.unwrapKey().ifPresent(key ->
                        text.add(Component.translatable(Util.makeDescriptionId("biome", key.location())).withStyle(ChatFormatting.GRAY)));
            }
            builder.addText(text, getWidth(), 10)
                    .setPosition(0, 20)
                    .setColor(0xFF505050)
                    .setLineSpacing(0);
        }
    }

    @Override
    public void draw(@Nonnull HoeTransmutation recipe, @Nonnull IRecipeSlotsView recipeSlotsView, @Nonnull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        var recipeArrow = guiHelper.getRecipeArrow();
        recipeArrow.draw(guiGraphics, (getWidth() - recipeArrow.getWidth()) / 2, 0);
    }
}
