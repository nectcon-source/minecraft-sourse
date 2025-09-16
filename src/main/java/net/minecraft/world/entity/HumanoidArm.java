package net.minecraft.world.entity;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/HumanoidArm.class */
public enum HumanoidArm {
    LEFT(new TranslatableComponent("options.mainHand.left")),
    RIGHT(new TranslatableComponent("options.mainHand.right"));

    private final Component name;

    HumanoidArm(Component component) {
        this.name = component;
    }

    public HumanoidArm getOpposite() {
        if (this == LEFT) {
            return RIGHT;
        }
        return LEFT;
    }

    @Override // java.lang.Enum
    public String toString() {
        return this.name.getString();
    }

    public Component getName() {
        return this.name;
    }
}
