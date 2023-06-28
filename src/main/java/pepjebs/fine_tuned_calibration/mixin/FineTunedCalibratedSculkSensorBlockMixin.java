package pepjebs.fine_tuned_calibration.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.CalibratedSculkSensorBlock;
import net.minecraft.block.ComposterBlock;
import net.minecraft.block.SculkSensorBlock;
import net.minecraft.block.entity.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.PositionSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pepjebs.fine_tuned_calibration.evaluation.FrequencyEvaluator;

import java.util.Arrays;

@Mixin(SculkSensorBlockEntity.VibrationCallback.class)
public class FineTunedCalibratedSculkSensorBlockMixin {

    private final Logger LOGGER = LogManager.getLogger("fine_tuned_calibration");
    private GameEvent triggerEvent = null;
    private BlockPos acceptPos = null;

    @Shadow
    private PositionSource positionSource;

    @Inject(method = "accept", at = @At("HEAD"))
    public void storeFineTuneData(
            ServerWorld world, BlockPos pos, GameEvent event, @Nullable Entity sourceEntity, @Nullable Entity entity, float distance,
            CallbackInfo ci
    ) {
        this.triggerEvent = event;
        this.acceptPos = pos;
    }

    @Redirect(
            method = "accept",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/block/SculkSensorBlock;setActive(Lnet/minecraft/entity/Entity;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;II)V")
    )
    public void injectFineTunedCalibration(
            SculkSensorBlock sculkSensorBlock, @Nullable Entity sourceEntity, World world, BlockPos sculkBlockPos, BlockState sculkState, int power, int frequency
    ) {
        if (!(sculkSensorBlock instanceof CalibratedSculkSensorBlock)) {
            sculkSensorBlock.setActive(sourceEntity, world, sculkBlockPos, sculkState, power, frequency);
            return;
        }
        var sculkSensorBlockEntity = (SculkSensorBlockEntity) world.getBlockEntity(sculkBlockPos);
        if (sculkSensorBlockEntity == null) {
            LOGGER.info("null entity!");
            sculkSensorBlock.setActive(sourceEntity, world, sculkBlockPos, sculkState, power, frequency);
            return;
        }

        LOGGER.info("vanilla freq: "+sculkSensorBlockEntity.getLastVibrationFrequency());
        int newFreq = FrequencyEvaluator.evaluateFrequency(
                this.triggerEvent, this.positionSource, this.acceptPos,
                sculkSensorBlock, sourceEntity, world, sculkBlockPos, sculkState, power, frequency
        );
        LOGGER.info("evaluated frequency: "+newFreq);

        sculkSensorBlock.setActive(sourceEntity, world, sculkBlockPos, sculkState, power, newFreq);
        sculkSensorBlockEntity.setLastVibrationFrequency(newFreq);
    }

    private int getFrequencyLegacy(
            SculkSensorBlock sculkSensorBlock,
            @Nullable Entity sourceEntity,
            World world,
            BlockPos sculkBlockPos,
            BlockState sculkState,
            int power,
            int frequency
    ) {
        int newFreq = 15;
        ServerPlayerEntity serverPlayer =
                (sourceEntity instanceof ServerPlayerEntity) ? ((ServerPlayerEntity) sourceEntity) : null;

        if (this.triggerEvent.getId().compareTo(GameEvent.INSTRUMENT_PLAY.getId()) == 0
                && serverPlayer != null) {
            if (serverPlayer.getStackInHand(Hand.MAIN_HAND).getItem() == Items.GOAT_HORN) {
                var nbt = ((ServerPlayerEntity) sourceEntity).getStackInHand(Hand.MAIN_HAND).getNbt();
                if (nbt != null && nbt.contains("instrument")) {
                    newFreq = Arrays.asList(new String[]{
                            "minecraft:ponder_goat_horn",
                            "minecraft:sing_goat_horn",
                            "minecraft:seek_goat_horn",
                            "minecraft:feel_goat_horn",
                            "minecraft:admire_goat_horn",
                            "minecraft:call_goat_horn",
                            "minecraft:yearn_goat_horn",
                            "minecraft:dream_goat_horn"
                    }).indexOf(nbt.getString("instrument")) + 1;
                }
            }
        } else if (this.triggerEvent.getId().compareTo(GameEvent.ITEM_INTERACT_FINISH.getId()) == 0
                && serverPlayer != null) {
            if (((ServerPlayerEntity) sourceEntity).getStackInHand(Hand.MAIN_HAND).getItem() == Items.SHIELD) {
                newFreq = 9;
            } else if (((ServerPlayerEntity) sourceEntity).getStackInHand(Hand.MAIN_HAND).getItem() == Items.SPYGLASS) {
                newFreq = 10;
            } else if (((ServerPlayerEntity) sourceEntity).getStackInHand(Hand.MAIN_HAND).getItem() == Items.BOW) {
                newFreq = 11;
            } else if (((ServerPlayerEntity) sourceEntity).getStackInHand(Hand.MAIN_HAND).getItem() == Items.CROSSBOW) {
                newFreq = 12;
            } else if (((ServerPlayerEntity) sourceEntity).getStackInHand(Hand.MAIN_HAND).getItem() == Items.FISHING_ROD) {
                newFreq = 13;
            }
        } else if (this.triggerEvent.getId().compareTo(GameEvent.PROJECTILE_SHOOT.getId()) == 0
                && serverPlayer != null) {
            if (((ServerPlayerEntity) sourceEntity).getStackInHand(Hand.MAIN_HAND).getItem() == Items.SNOWBALL) {
                newFreq = 9;
            } else if (((ServerPlayerEntity) sourceEntity).getStackInHand(Hand.MAIN_HAND).getItem() == Items.BOW) {
                newFreq = 11;
            } else if (((ServerPlayerEntity) sourceEntity).getStackInHand(Hand.MAIN_HAND).getItem() == Items.CROSSBOW) {
                newFreq = 12;
            } else if (((ServerPlayerEntity) sourceEntity).getStackInHand(Hand.MAIN_HAND).getItem() == Items.FISHING_ROD) {
                newFreq = 13;
            }
        } else if (this.triggerEvent.getId().compareTo(GameEvent.BLOCK_CHANGE.getId()) == 0
                && this.positionSource.getPos(world).isPresent()) {
            BlockEntity e = world.getBlockEntity(this.acceptPos);
            if (e instanceof JukeboxBlockEntity) {
                Identifier record = Registries.ITEM.getId(((JukeboxBlockEntity) e).getStack().getItem());
                int discFreq =
                        Arrays.asList(new String[]{
                                "minecraft:music_disc_13",
                                "minecraft:music_disc_cat",
                                "minecraft:music_disc_blocks",
                                "minecraft:music_disc_chirp",
                                "minecraft:music_disc_far",
                                "minecraft:music_disc_mall",
                                "minecraft:music_disc_mellohi",
                                "minecraft:music_disc_stal",
                                "minecraft:music_disc_strad",
                                "minecraft:music_disc_ward",
                                "minecraft:music_disc_11",
                                "minecraft:music_disc_wait",
                                "minecraft:music_disc_otherside",
                                "minecraft:music_disc_5",
                                "minecraft:music_disc_pigstep"
                        }).indexOf(record.toString());
                if (discFreq != -1) {
                    newFreq = discFreq + 1;
                }
            } else if (e instanceof ChiseledBookshelfBlockEntity) {
                newFreq = ((ChiseledBookshelfBlockEntity) e).getLastInteractedSlot() + 1;
            } else if (e instanceof LecternBlockEntity) {
                newFreq = 7;
            } else if (world.getBlockState(this.acceptPos).getBlock() instanceof ComposterBlock) {
                // TODO: 1-8 composter breakout?
                newFreq = 8;
            }
        } else if (this.triggerEvent.getId().compareTo(GameEvent.ENTITY_ROAR.getId()) == 0) {
            newFreq = 1;
        } else if (this.triggerEvent.getId().compareTo(GameEvent.ENTITY_SHAKE.getId()) == 0) {
            newFreq = 2;
        } else if (this.triggerEvent.getId().compareTo(GameEvent.ELYTRA_GLIDE.getId()) == 0) {
            newFreq = 3;
        } else if (this.triggerEvent.getId().compareTo(GameEvent.PROJECTILE_LAND.getId()) == 0) {
            newFreq = 1;
        } else if (this.triggerEvent.getId().compareTo(GameEvent.HIT_GROUND.getId()) == 0) {
            if (serverPlayer != null) {
                newFreq = 2;
            } else {
                newFreq = 3;
            }
        } else if (this.triggerEvent.getId().compareTo(GameEvent.SPLASH.getId()) == 0) {
            if (serverPlayer != null) {
                newFreq = 4;
            } else {
                newFreq = 5;
            }
        } else if (this.triggerEvent.getId().compareTo(GameEvent.STEP.getId()) == 0) {
            if (serverPlayer != null) {
                newFreq = 1;
            } else {
                newFreq = 2;
            }
        } else if (this.triggerEvent.getId().compareTo(GameEvent.SWIM.getId()) == 0) {
            if (serverPlayer != null) {
                newFreq = 3;
            } else {
                newFreq = 4;
            }
        } else if (this.triggerEvent.getId().compareTo(GameEvent.FLAP.getId()) == 0) {
            newFreq = 5;
        } else if (this.triggerEvent.getId().compareTo(GameEvent.ENTITY_DISMOUNT.getId()) == 0) {
            if (serverPlayer != null) {
                newFreq = 1;
            } else {
                newFreq = 2;
            }
        } else if (this.triggerEvent.getId().compareTo(GameEvent.EQUIP.getId()) == 0) {
            if (serverPlayer != null) {
                newFreq = 3;
            } else {
                newFreq = 4;
            }
        } else if (this.triggerEvent.getId().compareTo(GameEvent.ENTITY_MOUNT.getId()) == 0) {
            if (serverPlayer != null) {
                newFreq = 1;
            } else {
                newFreq = 2;
            }
        } else if (this.triggerEvent.getId().compareTo(GameEvent.ENTITY_INTERACT.getId()) == 0) {
            newFreq = 3;
        } else if (this.triggerEvent.getId().compareTo(GameEvent.SHEAR.getId()) == 0) {
            newFreq = 4;
        } else if (this.triggerEvent.getId().compareTo(GameEvent.ENTITY_DAMAGE.getId()) == 0) {
            if (serverPlayer != null && !serverPlayer.isDead()) {
                newFreq = 1;
            } else if (serverPlayer != null && serverPlayer.isDead()) {
                newFreq = 3;
            } else if (sourceEntity instanceof LivingEntity && ((LivingEntity) sourceEntity).isDead()) {
                newFreq = 4;
            } else {
                newFreq = 2;
            }
        } else if (this.triggerEvent.getId().compareTo(GameEvent.DRINK.getId()) == 0) {
            if (serverPlayer != null) {
                newFreq = 1;
            } else {
                newFreq = 2;
            }
        } else if (this.triggerEvent.getId().compareTo(GameEvent.EAT.getId()) == 0) {
            if (serverPlayer != null) {
                newFreq = 3;
            } else {
                newFreq = 4;
            }
        }
        return newFreq;
    }
}
