//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;
import java.util.stream.Collectors;

public class OptionsKeyTranslationFix extends DataFix {
    public OptionsKeyTranslationFix(Schema var1, boolean var2) {
        super(var1, var2);
    }

//    public TypeRewriteRule makeRule() {
//        return this.fixTypeEverywhereTyped("OptionsKeyTranslationFix", this.getInputSchema().getType(References.OPTIONS), (var0) -> var0.update(DSL.remainderFinder(), (var0x) -> var0x.<com.mojang.serialization.Dynamic<?>>getMapValues().map((var1) -> var0x.createMap(var1.entrySet().stream().map((var1x) -> {
//            if ((var1x.getKey()).asString("").startsWith("key_")) {
//                String var2 = (var1x.getValue()).asString("");
//                if (!var2.startsWith("key.mouse") && !var2.startsWith("scancode.")) {
//                    return Pair.of(var1x.getKey(), var0x.createString("key.keyboard." + var2.substring("key.".length())));
//                }
//            }
//
//            return Pair.of(var1x.getKey(), var1x.getValue());
//        }).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond)))).result().orElse(var0x)));
//    }
public TypeRewriteRule makeRule() {
    return this.fixTypeEverywhereTyped("OptionsKeyTranslationFix", this.getInputSchema().getType(References.OPTIONS), (var0) ->
            var0.update(DSL.remainderFinder(), (var0x) ->
                    var0x.getMapValues().<com.mojang.serialization.Dynamic<?>>map((var1) ->  // <- ключевое изменение!
                            var0x.createMap(var1.entrySet().stream().map((var1x) -> {
                                if (var1x.getKey().asString("").startsWith("key_")) {
                                    String var2 = var1x.getValue().asString("");
                                    if (!var2.startsWith("key.mouse") && !var2.startsWith("scancode.")) {
                                        return Pair.of(var1x.getKey(), var0x.createString("key.keyboard." + var2.substring("key.".length())));
                                    }
                                }
                                return Pair.of(var1x.getKey(), var1x.getValue());
                            }).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond)))
                    ).result().orElse(var0x)  // Теперь типы согласованы
            )
    );
}
}
