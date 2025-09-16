package net.minecraft.world.level.storage.loot;

import java.util.Random;
import net.minecraft.resources.ResourceLocation;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/RandomIntGenerator.class */
public interface RandomIntGenerator {
    public static final ResourceLocation CONSTANT = new ResourceLocation("constant");
    public static final ResourceLocation UNIFORM = new ResourceLocation("uniform");
    public static final ResourceLocation BINOMIAL = new ResourceLocation("binomial");

    int getInt(Random random);

    ResourceLocation getType();
}
