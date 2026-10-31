package br.com.cronicasdafronteira.client;

import br.com.cronicasdafronteira.CronicasMod;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.villager.Villager;

public final class CronicasClient implements ClientModInitializer {
    @Override public void onInitializeClient() {
        UseEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
            if (!level.isClientSide() || hand != InteractionHand.MAIN_HAND || player.isSpectator())
                return InteractionResult.PASS;
            Minecraft minecraft = Minecraft.getInstance();
            if (entity instanceof Villager villager && !player.isShiftKeyDown()) {
                if (minecraft.getConnection() != null) {
                    minecraft.getConnection().sendCommand("cronicas iniciar " + villager.getId());
                    minecraft.gui.setScreen(new DialogueScreen(villager));
                }
                return InteractionResult.SUCCESS;
            }
            if (entity instanceof LivingEntity && player.isShiftKeyDown() && !(entity instanceof Villager)) {
                if (minecraft.getConnection() != null)
                    minecraft.getConnection().sendCommand("cronicas criatura " + entity.getId());
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        });
    }
}
