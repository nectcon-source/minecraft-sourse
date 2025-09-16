package net.minecraft.server.packs.repository;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public interface PackSource {
   PackSource DEFAULT = passThrough();
   PackSource BUILT_IN = decorating("pack.source.builtin");
   PackSource WORLD = decorating("pack.source.world");
   PackSource SERVER = decorating("pack.source.server");

   Component decorate(Component var1);

   static PackSource passThrough() {
      return var0 -> var0;
   }

   static PackSource decorating(String var0) {
      Component var1 = new TranslatableComponent(var0);
      return (var1x) -> (new TranslatableComponent("pack.nameAndSource", var1x, var1)).withStyle(ChatFormatting.GRAY);
   }
}
