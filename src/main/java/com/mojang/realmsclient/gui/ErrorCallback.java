package com.mojang.realmsclient.gui;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public interface ErrorCallback {
   void error(Component var1);

   default void error(String var1) {
      this.error(new TextComponent(var1));
   }
}
