package net.minecraft.world;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/InteractionResultHolder.class */
public class InteractionResultHolder<T> {
    private final InteractionResult result;
    private final T object;

    public InteractionResultHolder(InteractionResult interactionResult, T t) {
        this.result = interactionResult;
        this.object = t;
    }

    public InteractionResult getResult() {
        return this.result;
    }

    public T getObject() {
        return this.object;
    }

    public static <T> InteractionResultHolder<T> success(T t) {
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, t);
    }

    public static <T> InteractionResultHolder<T> consume(T t) {
        return new InteractionResultHolder<>(InteractionResult.CONSUME, t);
    }

    public static <T> InteractionResultHolder<T> pass(T t) {
        return new InteractionResultHolder<>(InteractionResult.PASS, t);
    }

    public static <T> InteractionResultHolder<T> fail(T t) {
        return new InteractionResultHolder<>(InteractionResult.FAIL, t);
    }

    public static <T> InteractionResultHolder<T> sidedSuccess(T t, boolean z) {
        return z ? success(t) : consume(t);
    }
}
