package net.minecraft.network.chat;

public enum ChatType {
   CHAT((byte)0, false),
   SYSTEM((byte)1, true),
   GAME_INFO((byte)2, true);

   private final byte index;
   private final boolean interrupt;

   private ChatType(byte var3, boolean var4) {
      this.index = var3;
      this.interrupt = var4;
   }

   public byte getIndex() {
      return this.index;
   }

   public static ChatType getForIndex(byte var0) {
      for(ChatType var1 : values()) {
         if (var0 == var1.index) {
            return var1;
         }
      }

      return CHAT;
   }

   public boolean shouldInterrupt() {
      return this.interrupt;
   }
}
