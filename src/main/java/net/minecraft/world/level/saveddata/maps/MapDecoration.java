package net.minecraft.world.level.saveddata.maps;

import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/saveddata/maps/MapDecoration.class */
public class MapDecoration {
    private final Type type;

    /* renamed from: x */
    private byte x;

    /* renamed from: y */
    private byte y;
    private byte rot;
    private final Component name;

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/saveddata/maps/MapDecoration$Type.class */
    public enum Type {
        PLAYER(false),
        FRAME(true),
        RED_MARKER(false),
        BLUE_MARKER(false),
        TARGET_X(true),
        TARGET_POINT(true),
        PLAYER_OFF_MAP(false),
        PLAYER_OFF_LIMITS(false),
        MANSION(true, 5393476),
        MONUMENT(true, 3830373),
        BANNER_WHITE(true),
        BANNER_ORANGE(true),
        BANNER_MAGENTA(true),
        BANNER_LIGHT_BLUE(true),
        BANNER_YELLOW(true),
        BANNER_LIME(true),
        BANNER_PINK(true),
        BANNER_GRAY(true),
        BANNER_LIGHT_GRAY(true),
        BANNER_CYAN(true),
        BANNER_PURPLE(true),
        BANNER_BLUE(true),
        BANNER_BROWN(true),
        BANNER_GREEN(true),
        BANNER_RED(true),
        BANNER_BLACK(true),
        RED_X(true);

        private final byte icon;
        private final boolean renderedOnFrame;
        private final int mapColor;

        Type(boolean z) {
            this(z, -1);
        }

        Type(boolean z, int i) {
            this.icon = (byte) ordinal();
            this.renderedOnFrame = z;
            this.mapColor = i;
        }

        public byte getIcon() {
            return this.icon;
        }

        public boolean isRenderedOnFrame() {
            return this.renderedOnFrame;
        }

        public boolean hasMapColor() {
            return this.mapColor >= 0;
        }

        public int getMapColor() {
            return this.mapColor;
        }

        public static Type byIcon(byte b) {
            return values()[Mth.clamp((int) b, 0, values().length - 1)];
        }
    }

    public MapDecoration(Type type, byte b, byte b2, byte b3, @Nullable Component component) {
        this.type = type;
        this.x = b;
        this.y = b2;
        this.rot = b3;
        this.name = component;
    }

    public byte getImage() {
        return this.type.getIcon();
    }

    public Type getType() {
        return this.type;
    }

    public byte getX() {
        return this.x;
    }

    public byte getY() {
        return this.y;
    }

    public byte getRot() {
        return this.rot;
    }

    public boolean renderOnFrame() {
        return this.type.isRenderedOnFrame();
    }

    @Nullable
    public Component getName() {
        return this.name;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MapDecoration)) {
            return false;
        }
        MapDecoration mapDecoration = (MapDecoration) obj;
        if (this.type != mapDecoration.type || this.rot != mapDecoration.rot || this.x != mapDecoration.x || this.y != mapDecoration.y || !Objects.equals(this.name, mapDecoration.name)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return (31 * ((31 * ((31 * ((31 * this.type.getIcon()) + this.x)) + this.y)) + this.rot)) + Objects.hashCode(this.name);
    }
}
