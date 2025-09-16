package net.minecraft.server.players;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.io.File;

public class ServerOpList extends StoredUserList<GameProfile, ServerOpListEntry> {
   public ServerOpList(File var1) {
      super(var1);
   }

   @Override
   protected StoredUserEntry<GameProfile> createEntry(JsonObject var1) {
      return new ServerOpListEntry(var1);
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

   public boolean canBypassPlayerLimit(GameProfile var1) {
      ServerOpListEntry var2 = this.get(var1);
      return var2 != null ? var2.getBypassesPlayerLimit() : false;
   }

   protected String getKeyForUser(GameProfile var1) {
      return var1.getId().toString();
   }
}
