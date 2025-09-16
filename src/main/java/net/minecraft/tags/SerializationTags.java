//package net.minecraft.tags;
//
//import java.util.function.Function;
//import java.util.stream.Collectors;
//
//public class SerializationTags {
//   private static volatile TagContainer instance = TagContainer.of(
////      TagCollection.of(
////         BlockTags.getWrappers().stream().collect(Collectors.toMap(Tag.Named::getName, (Function<? super Tag.Named, ? extends Tag.Named>)(var0 -> var0)))
////      ),
////      TagCollection.of(
////         ItemTags.getWrappers().stream().collect(Collectors.toMap(Tag.Named::getName, (Function<? super Tag.Named, ? extends Tag.Named>)(var0 -> var0)))
////      ),
////      TagCollection.of(
////         FluidTags.getWrappers().stream().collect(Collectors.toMap(Tag.Named::getName, (Function<? super Tag.Named, ? extends Tag.Named>)(var0 -> var0)))
////      ),
////      TagCollection.of(
////         EntityTypeTags.getWrappers().stream().collect(Collectors.toMap(Tag.Named::getName, (Function<? super Tag.Named, ? extends Tag.Named>)(var0 -> var0)))
////      )
//           TagCollection.of(
//                   BlockTags.getWrappers().stream().collect(Collectors.toMap(
//                           Tag.Named::getName,
//                           Function.identity()
//                   ))
//           ),
//           TagCollection.of(
//                   ItemTags.getWrappers().stream().collect(Collectors.toMap(
//                           Tag.Named::getName,
//                           Function.identity()
//                   ))
//           ),
//           TagCollection.of(
//                   FluidTags.getWrappers().stream().collect(Collectors.toMap(
//                           Tag.Named::getName,
//                           Function.identity()
//                   ))
//           ),
//           TagCollection.of(
//                   EntityTypeTags.getWrappers().stream().collect(Collectors.toMap(
//                           Tag.Named::getName,
//                           Function.identity()
//                   ))
//           )
//   );
//
//   public static TagContainer getInstance() {
//      return instance;
//   }
//
//   public static void bind(TagContainer var0) {
//      instance = var0;
//   }
//}
package net.minecraft.tags;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

import java.util.function.Function;
import java.util.stream.Collectors;

public class SerializationTags {
    private static volatile TagContainer instance = TagContainer.of(
            TagCollection.of(
                    BlockTags.getWrappers().stream().collect(Collectors.toMap(
                            Tag.Named::getName,
                            named -> (Tag<Block>) named
                    ))
            ),
            TagCollection.of(
                    ItemTags.getWrappers().stream().collect(Collectors.toMap(
                            Tag.Named::getName,
                            named -> (Tag<Item>) named
                    ))
            ),
            TagCollection.of(
                    FluidTags.getWrappers().stream().collect(Collectors.toMap(
                            Tag.Named::getName,
                            named -> (Tag<Fluid>) named
                    ))
            ),
            TagCollection.of(
                    EntityTypeTags.getWrappers().stream().collect(Collectors.toMap(
                            Tag.Named::getName,
                            named -> (Tag<EntityType<?>>) named
                    ))
            )
    );

    public static TagContainer getInstance() {
        return instance;
    }

    public static void bind(TagContainer newInstance) {
        instance = newInstance;
    }
}
