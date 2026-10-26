package com.example.playerinfo.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    private PlayerEntity playerinfo$findTarget(MinecraftClient mc) {
        Vec3d eye = mc.player.getEyePos();
        Vec3d look = mc.player.getRotationVec(1.0F);
        PlayerEntity best = null;
        double bestCos = 0.9985;
        for (PlayerEntity p : mc.world.getPlayers()) {
            if (p == mc.player || p.isSpectator()) continue;
            Vec3d to = p.getBoundingBox().getCenter().subtract(eye);
            double dist = to.length();
            if (dist < 0.5 || dist > 128) continue;
            double cos = look.dotProduct(to) / dist;
            if (cos > bestCos) {
                bestCos = cos;
                best = p;
            }
        }
        return best;
    }

    private void playerinfo$slot(DrawContext ctx, ItemStack stack, int x, int y) {
        ctx.fill(x, y, x + 18, y + 18, 0x55FFFFFF);
        if (!stack.isEmpty()) {
            ctx.drawItem(stack, x + 1, y + 1);
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void playerinfo$hud(DrawContext ctx, RenderTickCounter tickCounter, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.options.hudHidden) return;

        PlayerEntity t = playerinfo$findTarget(mc);
        if (t == null) return;

        TextRenderer tr = mc.textRenderer;
        int w = 200;
        int h = 50;
        int x = (ctx.getScaledWindowWidth() - w) / 2;
        int y = 4;

        ctx.fill(x, y, x + w, y + h, 0xAA000000);
        ctx.fill(x, y, x + w, y + 1, 0xFFFF3030);
        ctx.fill(x, y + h - 1, x + w, y + h, 0xFFFF3030);
        ctx.fill(x, y, x + 1, y + h, 0xFFFF3030);
        ctx.fill(x + w - 1, y, x + w, y + h, 0xFFFF3030);

        String name = t.getName().getString();
        ctx.drawTextWithShadow(tr, name, x + 6, y + 5, 0xFFFFFFFF);
        String dist = Math.round(mc.player.distanceTo(t)) + "m";
        ctx.drawTextWithShadow(tr, dist, x + w - 6 - tr.getWidth(dist), y + 5, 0xFFAAAAAA);

        float hp = t.getHealth();
        float abs = t.getAbsorptionAmount();
        int hpColor = hp > 14 ? 0xFF55FF55 : (hp > 7 ? 0xFFFFFF55 : 0xFFFF5555);
        String hpText = "HP " + (Math.round(hp * 10) / 10.0)
                + (abs > 0 ? " +" + (Math.round(abs * 10) / 10.0) : "");
        ctx.drawTextWithShadow(tr, hpText, x + 6, y + 15, hpColor);
        String armText = "ARM " + t.getArmor();
        ctx.drawTextWithShadow(tr, armText, x + 100, y + 15, 0xFF99CCFF);

        int row = y + 27;
        ItemStack[] armor = {
                t.getEquippedStack(EquipmentSlot.HEAD),
                t.getEquippedStack(EquipmentSlot.CHEST),
                t.getEquippedStack(EquipmentSlot.LEGS),
                t.getEquippedStack(EquipmentSlot.FEET)
        };
        for (int i = 0; i < 4; i++) {
            playerinfo$slot(ctx, armor[i], x + 6 + i * 20, row);
        }

        boolean mainIsRight = t.getMainArm() == Arm.RIGHT;
        ItemStack right = mainIsRight ? t.getMainHandStack() : t.getOffHandStack();
        ItemStack left = mainIsRight ? t.getOffHandStack() : t.getMainHandStack();

        int hx = x + 106;
        ctx.drawTextWithShadow(tr, "L", hx, row + 5, 0xFFFFFFFF);
        playerinfo$slot(ctx, left, hx + 10, row);
        ctx.drawTextWithShadow(tr, "R", hx + 50, row + 5, 0xFFFFFFFF);
        playerinfo$slot(ctx, right, hx + 60, row);
    }
}
