package net.minecraft.server.players;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.io.File;

public class UserWhiteList extends StoredUserList<GameProfile, UserWhiteListEntry> {
   public UserWhiteList(File var1) {
      super(var1);
   }

   @Override
   protected StoredUserEntry<GameProfile> createEntry(JsonObject var1) {
      return new UserWhiteListEntry(var1);
   }

   public boolean isWhiteListed(GameProfile var1) {
      return this.contains(var1);
   }

   @Override
   public String[] getUserList() {
      String[] var1 = new String[this.getEntries().size()];
      int var2 = 0;

      for(StoredUserEntry<GameProfile> var4 : this.getEntries()) {
         var1[var2++] = ((GameProfile)var4.getUser()).getName();
      }

      return var1;
   }

   protected String getKeyForUser(GameProfile var1) {
      return var1.getId().toString();
   }
}
