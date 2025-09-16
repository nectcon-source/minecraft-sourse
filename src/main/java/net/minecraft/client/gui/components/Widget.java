package net.minecraft.client.gui.components;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

public interface Widget {
   void render(PoseStack var1, int var2, int var3, float var4) throws CommandSyntaxException;
}
