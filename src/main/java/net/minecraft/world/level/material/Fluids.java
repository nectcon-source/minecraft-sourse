package net.minecraft.world.level.material;

import com.google.common.collect.UnmodifiableIterator;
import java.util.Iterator;
import net.minecraft.core.Registry;
import net.minecraft.world.level.material.LavaFluid;
import net.minecraft.world.level.material.WaterFluid;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/material/Fluids.class */
public class Fluids {
    public static final Fluid EMPTY = register("empty", new EmptyFluid());
    public static final FlowingFluid FLOWING_WATER =  register("flowing_water", new WaterFluid.Flowing());
    public static final FlowingFluid WATER =  register("water", new WaterFluid.Source());
    public static final FlowingFluid FLOWING_LAVA =  register("flowing_lava", new LavaFluid.Flowing());
    public static final FlowingFluid LAVA =  register("lava", new LavaFluid.Source());

    static {
        Iterator<Fluid> it = Registry.FLUID.iterator();
        while (it.hasNext()) {
            UnmodifiableIterator it2 = it.next().getStateDefinition().getPossibleStates().iterator();
            while (it2.hasNext()) {
                Fluid.FLUID_STATE_REGISTRY.add((FluidState) it2.next());
            }
        }
    }

    private static <T extends Fluid> T register(String str, T t) {
        return  Registry.register(Registry.FLUID, str, t);
    }
}
