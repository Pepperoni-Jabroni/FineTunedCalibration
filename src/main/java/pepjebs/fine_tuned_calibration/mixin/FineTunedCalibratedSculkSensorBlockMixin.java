package pepjebs.fine_tuned_calibration.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.CalibratedSculkSensorBlock;
import net.minecraft.block.SculkSensorBlock;
import net.minecraft.block.entity.*;
import net.minecraft.entity.Entity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.PositionSource;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pepjebs.fine_tuned_calibration.evaluation.FrequencyEvaluator;

@Mixin(SculkSensorBlockEntity.VibrationCallback.class)
public class FineTunedCalibratedSculkSensorBlockMixin {

    private GameEvent triggerEvent = null;
    private BlockPos acceptPos = null;

    @Shadow
    private PositionSource positionSource;

    @Inject(method = "accept", at = @At("HEAD"))
    public void storeFineTuneData(
            ServerWorld world,
            BlockPos pos,
            RegistryEntry<GameEvent> event,
            @Nullable Entity sourceEntity,
            @Nullable Entity entity,
            float distance,
            CallbackInfo ci
    ) {
        this.triggerEvent = event.value();
        this.acceptPos = pos;
    }

    @Redirect(
            method = "accept",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/block/SculkSensorBlock;setActive(Lnet/minecraft/entity/Entity;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;II)V")
    )
    public void injectFineTunedCalibration(
            SculkSensorBlock sculkSensorBlock,
            @Nullable Entity sourceEntity,
            World world,
            BlockPos sculkBlockPos,
            BlockState sculkState,
            int power,
            int frequency
    ) {
        if (!(sculkSensorBlock instanceof CalibratedSculkSensorBlock)) {
            sculkSensorBlock.setActive(sourceEntity, world, sculkBlockPos, sculkState, power, frequency);
            return;
        }
        var sculkSensorBlockEntity = (SculkSensorBlockEntity) world.getBlockEntity(sculkBlockPos);
        if (sculkSensorBlockEntity == null) {
            sculkSensorBlock.setActive(sourceEntity, world, sculkBlockPos, sculkState, power, frequency);
            return;
        }

        int newFreq = FrequencyEvaluator.evaluateFrequency(
                this.triggerEvent, this.positionSource, this.acceptPos,
                sculkSensorBlock, sourceEntity, world, sculkBlockPos, sculkState, power, frequency
        );

        sculkSensorBlock.setActive(sourceEntity, world, sculkBlockPos, sculkState, power, newFreq);
        sculkSensorBlockEntity.setLastVibrationFrequency(newFreq);
    }
}
