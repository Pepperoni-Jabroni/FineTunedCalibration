package pepjebs.fine_tuned_calibration.evaluation;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChiseledBookshelfBlockEntity;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.block.entity.LecternBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.PositionSource;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

class ConstantFrequencyResolver extends FrequencyResolver {

    public ConstantFrequencyResolver(GameEvent event) {
        super(event, 1);
    }

    @Override
    public Optional<Integer> getNewFrequencyForDetection(
            PositionSource positionSource,
            BlockPos positionBlockSource,
            SculkSensorBlock sculkSensorBlock,
            @Nullable Entity sourceEntity,
            World world,
            BlockPos sculkBlockPos,
            BlockState sculkState,
            int power,
            int frequency) {
        return Optional.of(0);
    }
}

class PlayerDependentFrequencyResolver extends FrequencyResolver {

    public PlayerDependentFrequencyResolver(GameEvent event) {
        super(event, 2);
    }

    @Override
    public Optional<Integer> getNewFrequencyForDetection(
            PositionSource positionSource,
            BlockPos positionBlockSource,
            SculkSensorBlock sculkSensorBlock,
            @Nullable Entity sourceEntity,
            World world,
            BlockPos sculkBlockPos,
            BlockState sculkState,
            int power,
            int frequency) {
        var optionalPlayer = FrequencyResolver.getServerPlayerFromEntity(sourceEntity);
        return optionalPlayer.isPresent() ? Optional.of(0) : Optional.of(1);
    }
}


class StepFrequencyResolver extends FrequencyResolver {

    static List<String> TIERS = List.of(
            "leather",
            "golden",
            "chainmail",
            "iron",
            "diamond",
            "netherite"
    );

    public StepFrequencyResolver() {
        super(GameEvent.STEP, TIERS.size() + 2);
    }

    @Override
    public Optional<Integer> getNewFrequencyForDetection(
            PositionSource positionSource,
            BlockPos positionBlockSource,
            SculkSensorBlock sculkSensorBlock,
            @Nullable Entity sourceEntity,
            World world,
            BlockPos sculkBlockPos,
            BlockState sculkState,
            int power,
            int frequency) {
        var optionalPlayer = FrequencyResolver.getServerPlayerFromEntity(sourceEntity);
        if (optionalPlayer.isEmpty()) {
            return Optional.of(7);
        }
        var armorIter = optionalPlayer.get().getArmorItems().iterator();
        var bootFreq = 0;
        while (armorIter.hasNext()) {
            var armor = armorIter.next();
            var armorStr = Registries.ITEM.getId(armor.getItem()).toString();
            if (armorStr.contains("boots") && TIERS.stream().anyMatch(armorStr::contains)) {
                bootFreq = TIERS.indexOf(TIERS.stream().filter(armorStr::contains).findFirst().get()) + 1;
            }
        }
        return Optional.of(bootFreq);
    }
}

class EntityDamageFrequencyResolver extends FrequencyResolver {

    public EntityDamageFrequencyResolver() {
        super(GameEvent.ENTITY_DAMAGE, 4);
    }

    @Override
    public Optional<Integer> getNewFrequencyForDetection(
            PositionSource positionSource,
            BlockPos positionBlockSource,
            SculkSensorBlock sculkSensorBlock,
            @Nullable Entity sourceEntity,
            World world,
            BlockPos sculkBlockPos,
            BlockState sculkState,
            int power,
            int frequency) {
        var optionalPlayer = FrequencyResolver.getServerPlayerFromEntity(sourceEntity);
        var entityDead = sourceEntity instanceof LivingEntity && ((LivingEntity) sourceEntity).isDead() ? 2 : 0;
        return optionalPlayer.isPresent() ? Optional.of(entityDead) : Optional.of(entityDead + 1);
    }
}

class NBTStringFrequencyResolver extends FrequencyResolver {

    Item targetItem;
    String nbtKey;
    List<String> nbtStringValues;

    public NBTStringFrequencyResolver(GameEvent event, Item item, String key, List<String> values) {
        super(event, values.size());
        this.nbtKey = key;
        this.nbtStringValues = values;
        this.targetItem = item;
    }

    @Override
    public Optional<Integer> getNewFrequencyForDetection(
            PositionSource positionSource,
            BlockPos positionBlockSource,
            SculkSensorBlock sculkSensorBlock,
            @Nullable Entity sourceEntity,
            World world,
            BlockPos sculkBlockPos,
            BlockState sculkState,
            int power,
            int frequency) {
        var optionalPlayer = FrequencyResolver.getServerPlayerFromEntity(sourceEntity);
        if (optionalPlayer.isPresent()
                && optionalPlayer.get().getStackInHand(Hand.MAIN_HAND).getItem() == this.targetItem) {
            var nbt = optionalPlayer.get().getStackInHand(Hand.MAIN_HAND).getNbt();
            if (nbt != null && nbt.contains(this.nbtKey)) {
                return Optional.of(this.nbtStringValues.indexOf(nbt.getString(this.nbtKey)));
            }
        }
        return Optional.empty();
    }
}

class GoatHornFrequencyResolver extends NBTStringFrequencyResolver {

    public static List<String> HORNS = Arrays.asList(
            "minecraft:ponder_goat_horn",
            "minecraft:sing_goat_horn",
            "minecraft:seek_goat_horn",
            "minecraft:feel_goat_horn",
            "minecraft:admire_goat_horn",
            "minecraft:call_goat_horn",
            "minecraft:yearn_goat_horn",
            "minecraft:dream_goat_horn"
    );

    public GoatHornFrequencyResolver() {
        super(GameEvent.INSTRUMENT_PLAY, Items.GOAT_HORN, "instrument", HORNS);
    }
}

class StackInHandFrequencyResolver extends FrequencyResolver {

    List<Item> targetHandStacks;

    public StackInHandFrequencyResolver(GameEvent event, List<Item> targetHandStacks) {
        super(event, targetHandStacks.size());
        this.targetHandStacks = targetHandStacks;
    }

    @Override
    Optional<Integer> getNewFrequencyForDetection(
            PositionSource positionSource,
            BlockPos positionBlockSource,
            SculkSensorBlock sculkSensorBlock,
            @Nullable Entity sourceEntity,
            World world,
            BlockPos sculkBlockPos,
            BlockState sculkState,
            int power,
            int frequency
    ) {
        var optionalPlayer = FrequencyResolver.getServerPlayerFromEntity(sourceEntity);
        var itemInHand = optionalPlayer
                .map(serverPlayerEntity -> serverPlayerEntity.getStackInHand(Hand.MAIN_HAND).getItem())
                .orElse(null);
        if (itemInHand != null && targetHandStacks.contains(itemInHand)) {
            return Optional.of(targetHandStacks.indexOf(itemInHand));
        }
        return Optional.empty();
    }
}

class BlockFrequencyResolver extends FrequencyResolver {

    Class<? extends Block> targetBlock;
    Class<? extends BlockEntity> targetBlockEntity;

    public BlockFrequencyResolver(GameEvent event, int range,
                                        Class<? extends Block> targetBlock,
                                        Class<? extends BlockEntity> targetBlockEntity) {
        super(event, range);
        this.targetBlock = targetBlock;
        this.targetBlockEntity = targetBlockEntity;
    }

    @Override
    Optional<Integer> getNewFrequencyForDetection(
            PositionSource positionSource,
            BlockPos positionBlockSource,
            SculkSensorBlock sculkSensorBlock,
            @Nullable Entity sourceEntity,
            World world,
            BlockPos sculkBlockPos,
            BlockState sculkState,
            int power,
            int frequency
    ) {
        Optional<BlockState> block = this.getBlockStateIfMatches(world, positionSource, positionBlockSource);
        Optional<BlockEntity> blockEntity = this.getBlockEntityIfMatches(world, positionSource, positionBlockSource);
        if (block.isPresent()) {
            if (this.targetBlockEntity == null) {
                return Optional.of(0);
            }
            if (blockEntity.isPresent()) {
                return Optional.of(0);
            }
        }
        return Optional.empty();
    }

    Optional<BlockState> getBlockStateIfMatches(
            World world,
            PositionSource positionSource,
            BlockPos positionBlockSource
    ) {
        if (positionSource.getPos(world).isPresent()) {
            BlockState b = world.getBlockState(positionBlockSource);
            if (b.getBlock().getClass() == this.targetBlock) {
                return Optional.of(b);
            }
        }
        return Optional.empty();
    }

    Optional<BlockEntity> getBlockEntityIfMatches(
            World world,
            PositionSource positionSource,
            BlockPos positionBlockSource
    ) {
        if (positionSource.getPos(world).isPresent()) {
            BlockEntity entity = world.getBlockEntity(positionBlockSource);
            if (entity != null && entity.getClass() == this.targetBlockEntity) {
                return Optional.of(entity);
            }
        }
        return Optional.empty();
    }
}

class ChiseledBookshelfFrequencyResolver extends BlockFrequencyResolver {

    public ChiseledBookshelfFrequencyResolver() {
        super(GameEvent.BLOCK_CHANGE, 6, ChiseledBookshelfBlock.class, ChiseledBookshelfBlockEntity.class);
    }

    @Override
    Optional<Integer> getNewFrequencyForDetection(
            PositionSource positionSource,
            BlockPos positionBlockSource,
            SculkSensorBlock sculkSensorBlock,
            @Nullable Entity sourceEntity,
            World world,
            BlockPos sculkBlockPos,
            BlockState sculkState,
            int power,
            int frequency
    ) {
        Optional<BlockState> block = this.getBlockStateIfMatches(world, positionSource, positionBlockSource);
        Optional<BlockEntity> blockEntity = this.getBlockEntityIfMatches(world, positionSource, positionBlockSource);
        if (block.isPresent() && blockEntity.isPresent() && blockEntity.get() instanceof ChiseledBookshelfBlockEntity) {
            return Optional.of(((ChiseledBookshelfBlockEntity) blockEntity.get()).getLastInteractedSlot());
        }
        return Optional.empty();
    }
}

class NoteBlockFrequencyResolver extends BlockFrequencyResolver {

    // Note Block use count range: 0-24, we will do half
    static int NOTE_BLOCK_SIZE = 13;

    public NoteBlockFrequencyResolver() {
        super(GameEvent.NOTE_BLOCK_PLAY, NOTE_BLOCK_SIZE, NoteBlock.class, null);
    }

    @Override
    Optional<Integer> getNewFrequencyForDetection(
            PositionSource positionSource,
            BlockPos positionBlockSource,
            SculkSensorBlock sculkSensorBlock,
            @Nullable Entity sourceEntity,
            World world,
            BlockPos sculkBlockPos,
            BlockState sculkState,
            int power,
            int frequency
    ) {
        Optional<BlockState> blockState = this.getBlockStateIfMatches(world, positionSource, positionBlockSource);
        return blockState.map(state -> state.get(NoteBlock.NOTE) / 2);
    }
}

class JukeboxFrequencyResolver extends BlockFrequencyResolver {

    static List<Item> RECORDS = List.of(
            Items.MUSIC_DISC_13,
            Items.MUSIC_DISC_CAT,
            Items.MUSIC_DISC_BLOCKS,
            Items.MUSIC_DISC_CHIRP,
            Items.MUSIC_DISC_FAR,
            Items.MUSIC_DISC_MALL,
            Items.MUSIC_DISC_MELLOHI,
            Items.MUSIC_DISC_STAL,
            Items.MUSIC_DISC_STRAD,
            Items.MUSIC_DISC_WARD,
            Items.MUSIC_DISC_11,
            Items.MUSIC_DISC_WAIT,
            Items.MUSIC_DISC_OTHERSIDE,
            Items.MUSIC_DISC_5,
            Items.MUSIC_DISC_PIGSTEP
    );

    public JukeboxFrequencyResolver() {
        super(GameEvent.BLOCK_CHANGE, RECORDS.size(), JukeboxBlock.class, JukeboxBlockEntity.class);
    }

    @Override
    Optional<Integer> getNewFrequencyForDetection(
            PositionSource positionSource,
            BlockPos positionBlockSource,
            SculkSensorBlock sculkSensorBlock,
            @Nullable Entity sourceEntity,
            World world,
            BlockPos sculkBlockPos,
            BlockState sculkState,
            int power,
            int frequency
    ) {
        Optional<BlockState> blockstate = this.getBlockStateIfMatches(world, positionSource, positionBlockSource);
        Optional<BlockEntity> blockEntity = this.getBlockEntityIfMatches(world, positionSource, positionBlockSource);
        if (blockstate.isPresent() && blockEntity.isPresent()) {
            Identifier record = Registries.ITEM.getId(((JukeboxBlockEntity) blockEntity.get()).getStack().getItem());
            int discFreq = RECORDS.stream().map(i -> Registries.ITEM.getId(i).toString())
                    .toList().indexOf(record.toString());
            if (discFreq != -1) {
                return Optional.of(discFreq);
            }
        }
        return Optional.empty();
    }
}

class MultiFrequencyResolver extends FrequencyResolver {

    List<FrequencyResolver> resolvers;

    public MultiFrequencyResolver(List<FrequencyResolver> resolvers) {
        super(
                resolvers.stream().map(r -> r.gameEvents).flatMap(Collection::stream)
                        .distinct().collect(Collectors.toList()),
                resolvers.stream().map(r -> r.range).max(Integer::compare).isPresent()
                        ? resolvers.stream().map(r -> r.range).max(Integer::compare).get()
                        : 1
        );
        this.resolvers = resolvers;
    }

    @Override
    Optional<Integer> getNewFrequencyForDetection(
            PositionSource positionSource,
            BlockPos positionBlockSource,
            SculkSensorBlock sculkSensorBlock,
            @Nullable Entity sourceEntity,
            World world,
            BlockPos sculkBlockPos,
            BlockState sculkState,
            int power,
            int frequency
    ) {
        for (var resolver : this.resolvers) {
            var result = resolver.getNewFrequencyForDetection(
                    positionSource, positionBlockSource, sculkSensorBlock, sourceEntity,
                    world, sculkBlockPos, sculkState, power, frequency
            );
            if (result.isPresent()) {
                return result;
            }
        }
        return Optional.empty();
    }
}

abstract class FrequencyResolver {

    public List<GameEvent> gameEvents;
    public int range;
    public boolean isConcurrent = false;

    public FrequencyResolver(List<GameEvent> events, int range) {
        this.gameEvents = events;
        this.range = range;
    }

    public FrequencyResolver(GameEvent event, int range) {
        this.gameEvents = List.of(event);
        this.range = range;
    }

    abstract Optional<Integer> getNewFrequencyForDetection(
            PositionSource positionSource,
            BlockPos positionBlockSource,
            SculkSensorBlock sculkSensorBlock,
            @Nullable Entity sourceEntity,
            World world,
            BlockPos sculkBlockPos,
            BlockState sculkState,
            int power,
            int frequency
    );

    static Optional<ServerPlayerEntity> getServerPlayerFromEntity(@Nullable Entity entity) {
        return entity instanceof ServerPlayerEntity ?
                Optional.of((ServerPlayerEntity) entity) : Optional.empty();
    }

    /**
     * This will disable incremental of frequency offset for this resolver.
     * NOTE: Concurrent Resolvers must always be the first elements of the list
     */
    public FrequencyResolver withConcurrency() {
        this.isConcurrent = true;
        return this;
    }
}

/**
 * Given the Calibrated Sculk Sensor event information, this class will try to assign a new Frequency
 * in Redstone range 1-15 for use with a Comparator which differentiates this Vibration from others
 * in the same Frequency.
 */
public class FrequencyEvaluator {

    /**
     * Map of Frequency values (1-15, but 0 indexed) to the frequency resolvers for that Sculk input Frequency
     */
    private static final List<List<FrequencyResolver>> RESOLVERS = new ArrayList<>(){{
        add(List.of(
                new StepFrequencyResolver(),
                new PlayerDependentFrequencyResolver(GameEvent.SWIM),
                new ConstantFrequencyResolver(GameEvent.FLAP)
        ));
        add(List.of(
                new ConstantFrequencyResolver(GameEvent.PROJECTILE_LAND),
                new PlayerDependentFrequencyResolver(GameEvent.HIT_GROUND),
                new PlayerDependentFrequencyResolver(GameEvent.SPLASH)
        ));
        add(List.of(
                new GoatHornFrequencyResolver(),
                new MultiFrequencyResolver(List.of(
                        new StackInHandFrequencyResolver(
                                GameEvent.ITEM_INTERACT_FINISH,
                                List.of(Items.SHIELD, Items.BOW, Items.CROSSBOW, Items.FISHING_ROD, Items.SPYGLASS)
                        ),
                        new StackInHandFrequencyResolver(
                                GameEvent.PROJECTILE_SHOOT,
                                List.of(Items.SNOWBALL, Items.BOW, Items.CROSSBOW, Items.FISHING_ROD, Items.FIREWORK_ROCKET)
                        )
                ))
        ));
        add(List.of(
                new ConstantFrequencyResolver(GameEvent.ENTITY_ACTION),
                new ConstantFrequencyResolver(GameEvent.ELYTRA_GLIDE),
                new ConstantFrequencyResolver(GameEvent.UNEQUIP),
                new ConstantFrequencyResolver(GameEvent.ELYTRA_GLIDE)
        ));
        add(List.of(
                new PlayerDependentFrequencyResolver(GameEvent.ENTITY_DISMOUNT),
                new PlayerDependentFrequencyResolver(GameEvent.EQUIP)
        ));
        add(List.of(
                new PlayerDependentFrequencyResolver(GameEvent.ENTITY_MOUNT),
                new ConstantFrequencyResolver(GameEvent.ENTITY_INTERACT),
                new ConstantFrequencyResolver(GameEvent.SHEAR)
        ));
        add(List.of(
                new EntityDamageFrequencyResolver()
        ));
        add(List.of(
                new PlayerDependentFrequencyResolver(GameEvent.DRINK),
                new PlayerDependentFrequencyResolver(GameEvent.EAT)
        ));
        add(List.of(
                new ConstantFrequencyResolver(GameEvent.CONTAINER_CLOSE),
                new ConstantFrequencyResolver(GameEvent.BLOCK_CLOSE),
                new ConstantFrequencyResolver(GameEvent.BLOCK_DEACTIVATE),
                new ConstantFrequencyResolver(GameEvent.BLOCK_DETACH)
        ));
        add(List.of(
                (new NoteBlockFrequencyResolver()).withConcurrency(),
                new ConstantFrequencyResolver(GameEvent.CONTAINER_OPEN),
                new ConstantFrequencyResolver(GameEvent.BLOCK_OPEN),
                new ConstantFrequencyResolver(GameEvent.BLOCK_ACTIVATE),
                new ConstantFrequencyResolver(GameEvent.BLOCK_ATTACH),
                new ConstantFrequencyResolver(GameEvent.PRIME_FUSE)
        ));
        add(List.of(
                (new JukeboxFrequencyResolver()).withConcurrency(),
                new ChiseledBookshelfFrequencyResolver(),
                new BlockFrequencyResolver(
                        GameEvent.BLOCK_CHANGE, 1, LecternBlock.class, LecternBlockEntity.class),
                new BlockFrequencyResolver(GameEvent.BLOCK_CHANGE, 1, ComposterBlock.class, null)
        ));
        add(List.of(
                new ConstantFrequencyResolver(GameEvent.BLOCK_DESTROY),
                new ConstantFrequencyResolver(GameEvent.FLUID_PICKUP)
        ));
        add(List.of(
                new ConstantFrequencyResolver(GameEvent.BLOCK_PLACE),
                new ConstantFrequencyResolver(GameEvent.FLUID_PLACE)
        ));
        add(List.of(
                new ConstantFrequencyResolver(GameEvent.ENTITY_PLACE),
                new ConstantFrequencyResolver(GameEvent.LIGHTNING_STRIKE),
                new ConstantFrequencyResolver(GameEvent.TELEPORT)
        ));
        add(List.of(
                new ConstantFrequencyResolver(GameEvent.ENTITY_DIE),
                new ConstantFrequencyResolver(GameEvent.EXPLODE)
        ));
    }};

    /**
     * Given a Calibrated Sculk Sensor to observe,
     * evaluate a new Frequency to assign the activation.
     */
    public static Integer evaluateFrequency(
            GameEvent triggerEvent,
            PositionSource positionSource,
            BlockPos positionBlockSource,
            SculkSensorBlock sculkSensorBlock,
            @Nullable Entity sourceEntity,
            World world,
            BlockPos sculkBlockPos,
            BlockState sculkState,
            int power,
            int frequency
    ) {
        // List is 0 idx
        var frequencyResolvers = RESOLVERS.get(frequency - 1);
        int frequencyOffset = 0;
        for (FrequencyResolver resolver : frequencyResolvers) {
            var result = resolver.getNewFrequencyForDetection(
                    positionSource, positionBlockSource, sculkSensorBlock, sourceEntity,
                    world, sculkBlockPos, sculkState, power, frequency
            );
            if (result.isPresent() && resolver.gameEvents.contains(triggerEvent)) {
                // 1 is lowest redstone output
                return result.get() + 1 + frequencyOffset;
            }
            if (!resolver.isConcurrent) {
                frequencyOffset = (frequencyOffset + resolver.range) % 15;
            }
        }
        // Fall-through behavior defaults to full signal strength
        return 15;
    }

    public static int getFrequencyTotalRange(){
        int freqCount = 0;
        for (var resolverChunk : RESOLVERS) {
            for (var resolver : resolverChunk) {
                freqCount += resolver.range;
            }
        }
        return freqCount;
    }
}