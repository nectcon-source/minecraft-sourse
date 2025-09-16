package net.minecraft.data.info;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.CommandDispatcher;
import java.io.IOException;
import java.nio.file.Path;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.synchronization.ArgumentTypes;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;

/* loaded from: client_deobf_norm.jar:net/minecraft/data/info/CommandsReport.class */
public class CommandsReport implements DataProvider {
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
   private final DataGenerator generator;

   public CommandsReport(DataGenerator dataGenerator) {
      this.generator = dataGenerator;
   }

   @Override // net.minecraft.data.DataProvider
   public void run(HashCache hashCache) throws IOException {
      Path resolve = this.generator.getOutputFolder().resolve("reports/commands.json");
      CommandDispatcher<CommandSourceStack> dispatcher = new Commands(Commands.CommandSelection.ALL).getDispatcher();
      DataProvider.save(GSON, hashCache, ArgumentTypes.serializeNodeToJson(dispatcher, dispatcher.getRoot()), resolve);
   }

   @Override // net.minecraft.data.DataProvider
   public String getName() {
      return "Command Syntax";
   }
}
