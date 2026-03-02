package net.fasttravel.block.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fasttravel.FastTravelMain;
import net.fasttravel.map.ClientMapStorage;
import net.fasttravel.network.FastTravelClientPacket;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.world.border.WorldBorder;
import org.apache.commons.lang3.text.WordUtils;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class TeleporterScreen extends Screen {

    private static final Identifier PLAYER_TEXTURE = FastTravelMain.identifierOf("textures/gui/map/player.png");

    private double centreX = 0;
    private double centreZ = 0;

    private ClientMapStorage.TeleporterInfo hoveredTeleporter = null;
    private ClientMapStorage.TeleporterInfo selectedTeleporter = null;
    private double hoveredScreenX = 0;
    private double hoveredScreenY = 0;

    private int guiScale = 1;
    private int cursorFrame = 0;
    private float switchFade = 0.0F;

    private final BlockPos blockPos;

    public TeleporterScreen(BlockPos blockPos) {
        super(Text.translatable("screen.fasttravel.map"));
        this.blockPos = blockPos;
    }

    @Override
    protected void init() {
        this.centreX = this.client.player.getBlockX();
        this.centreZ = this.client.player.getBlockZ();
        this.client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.ITEM_BOOK_PAGE_TURN, 1.0F));
        super.init();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context);
        switchFade = Math.max(0, switchFade - delta);

        ClientMapStorage mapStorage = ClientMapStorage.get(client.world.getRegistryKey());

        context.getMatrices().push();
        float scaleFactor = getScaleFactor();
        context.getMatrices().scale(scaleFactor, scaleFactor, 1.0f);

        WorldBorder worldBorder = client.world.getWorldBorder();
        double borderSize = worldBorder.getSize();
        int borderX1 = (int) Math.floor(worldBorder.getCenterX() - borderSize / 2.0);
        int borderX2 = (int) Math.ceil(worldBorder.getCenterX() + borderSize / 2.0);
        int borderZ1 = (int) Math.floor(worldBorder.getCenterZ() - borderSize / 2.0);
        int borderZ2 = (int) Math.ceil(worldBorder.getCenterZ() + borderSize / 2.0);

        int renderX1 = Math.max((int) Math.floor(screenXToWorldX(0.0)), borderX1);
        int renderX2 = Math.min((int) Math.ceil(screenXToWorldX(width)), borderX2);
        int renderZ1 = Math.max((int) Math.floor(screenYToWorldZ(0.0)), borderZ1);
        int renderZ2 = Math.min((int) Math.ceil(screenYToWorldZ(height)), borderZ2);

        for (Map.Entry<Long, Identifier> entry : mapStorage.regionTextures.entrySet()) {
            long key = entry.getKey();
            Identifier texture = entry.getValue();

            int regionWorldX = ClientMapStorage.regionX(key) * 512;
            int regionWorldZ = ClientMapStorage.regionZ(key) * 512;

            int u = Math.max(0, renderX1 - regionWorldX);
            int v = Math.max(0, renderZ1 - regionWorldZ);
            int drawWidth = Math.min(512, renderX2 - regionWorldX) - u;
            int drawHeight = Math.min(512, renderZ2 - regionWorldZ) - v;
            if (drawWidth <= 0 || drawHeight <= 0) continue;

            context.getMatrices().push();
            context.getMatrices().translate(
                    worldXToRenderX(regionWorldX + u),
                    worldZToRenderY(regionWorldZ + v),
                    0
            );
            context.drawTexture(texture, 0, 0, drawWidth, drawHeight, u, v, drawWidth, drawHeight, 512, 512);
            context.getMatrices().pop();
        }

        context.getMatrices().pop();

        hoveredTeleporter = null;
        double bestDistance = Double.MAX_VALUE;

        for (ClientMapStorage.TeleporterInfo info : mapStorage.getTeleporters()) {
            if (!mapStorage.hasVisited(info.pos())) continue;

            double screenX = renderToScreen(worldXToRenderX(info.pos().getX()));
            double screenY = renderToScreen(worldZToRenderY(info.pos().getZ()));
            double dx = hoveredScreenX - screenX;
            double dy = hoveredScreenY - screenY;
            double dist = dx * dx + dy * dy;
            double threshold = 6 * 6 * client.getWindow().getScaleFactor();

            if (dist < threshold && dist < bestDistance) {
                hoveredTeleporter = info;
                bestDistance = dist;
            }
        }

        for (ClientMapStorage.TeleporterInfo info : mapStorage.getTeleporters()) {
            if (!mapStorage.hasVisited(info.pos())) continue;
            renderTeleporterIcon(context, info, scaleFactor);
        }

        if (selectedTeleporter != null) {
            renderTeleporterIcon(context, selectedTeleporter, scaleFactor);

            double sx = renderToScreen(worldXToRenderX(selectedTeleporter.pos().getX()));
            double sy = renderToScreen(worldZToRenderY(selectedTeleporter.pos().getZ()));
            context.getMatrices().push();
            context.getMatrices().translate(sx, sy, 0);
            context.drawTooltip(this.textRenderer, List.of(Text.translatable("info.fasttravel.teleport")), 0, 0);
            context.getMatrices().pop();

        } else if (hoveredTeleporter != null) {
            context.getMatrices().push();
            context.getMatrices().translate(hoveredScreenX, hoveredScreenY, 0);

            List<Text> tooltipLines = new ArrayList<>();
            tooltipLines.add(hoveredTeleporter.name());

            context.drawTooltip(this.textRenderer, tooltipLines, 0, 0);
            context.getMatrices().pop();
        }

        if (switchFade > 0) {
            String dimName = WordUtils.capitalizeFully(client.world.getRegistryKey().getValue().getPath().replaceAll("[/_-]", " "));
            context.drawText(this.textRenderer, Text.literal(dimName).formatted(Formatting.WHITE), 0, height - 10,
                    ColorHelper.Argb.getArgb(Math.min(255, (int) (255 * switchFade / 5.0)), 255, 255, 255), true);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    private void renderTeleporterIcon(DrawContext context, ClientMapStorage.TeleporterInfo info, float scaleFactor) {
        double screenX = renderToScreen(worldXToRenderX(info.pos().getX()));
        double screenY = renderToScreen(worldZToRenderY(info.pos().getZ()));

        boolean isHovered = info == hoveredTeleporter;
        boolean isCurrent = info.pos().equals(blockPos);
        boolean isSelected = info == selectedTeleporter;

        float tint = isHovered ? 0.7f : 1.0f;

        context.getMatrices().push();
        context.getMatrices().translate(screenX, screenY, 0);

        if (isCurrent) {
            context.getMatrices().scale(1.3f, 1.3f, 1.3f);
        }

        ItemStack icon = info.icon();
        if (!icon.isEmpty()) {
            RenderSystem.setShaderColor(tint, tint, tint, 1.0f);
            context.drawItem(icon, -8, -8);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        } else {
            RenderSystem.setShaderColor(tint, tint, tint, 1.0f);
            context.drawTexture(new Identifier("textures/map/map_icons.png"), -4, -8, 8, 8, 80, 0, 8, 8, 128, 128);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        }

        context.getMatrices().pop();
    }

    @Override
    public void tick() {
        cursorFrame = (cursorFrame + 1) % 40;
        super.tick();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.ITEM_BOOK_PAGE_TURN, 1.0F));
        super.close();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        switch (keyCode) {
            case GLFW.GLFW_KEY_UP -> centreZ--;
            case GLFW.GLFW_KEY_DOWN -> centreZ++;
            case GLFW.GLFW_KEY_LEFT -> centreX--;
            case GLFW.GLFW_KEY_RIGHT -> centreX++;
            case GLFW.GLFW_KEY_SPACE -> {
                if (centreX != client.player.getBlockX() || centreZ != client.player.getBlockZ()) {
                    client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_STONE_BUTTON_CLICK_OFF, 1.0F));
                    centreX = client.player.getBlockX();
                    centreZ = client.player.getBlockZ();
                }
            }
            default -> {
                return super.keyPressed(keyCode, scanCode, modifiers);
            }
        }
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_2) {
            if (selectedTeleporter != null) {
                selectedTeleporter = null;
                return true;
            }
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_1 && hoveredTeleporter != null) {
            if (hoveredTeleporter.pos().equals(blockPos)) return true;

            if (selectedTeleporter != null && selectedTeleporter.pos().equals(hoveredTeleporter.pos())) {
                FastTravelClientPacket.sendRequestTeleportPacket(selectedTeleporter.pos());
                this.close();
            } else {
                selectedTeleporter = hoveredTeleporter;
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        hoveredScreenX = mouseX;
        hoveredScreenY = mouseY;
        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (amount >= 1 && guiScale < 10) {
            centreX += (screenXToWorldX(mouseX) - centreX) / (guiScale + 1);
            centreZ += (screenYToWorldZ(mouseY) - centreZ) / (guiScale + 1);
            guiScale++;
            return true;
        }
        if (amount <= -1 && guiScale > 1) {
            guiScale--;
            centreX -= (screenXToWorldX(mouseX) - centreX) / (guiScale + 1);
            centreZ -= (screenYToWorldZ(mouseY) - centreZ) / (guiScale + 1);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        centreX -= deltaX / getScaleFactor();
        centreZ -= deltaY / getScaleFactor();
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    private double worldXToRenderX(double worldX) {
        return (getWidth() / 2.0) + worldX - centreX;
    }

    private double worldZToRenderY(double worldZ) {
        return (getHeight() / 2.0) + worldZ - centreZ;
    }

    private double screenXToWorldX(double screenX) {
        return (screenX / getScaleFactor()) + centreX - getWidth() / 2.0;
    }

    private double screenYToWorldZ(double screenY) {
        return (screenY / getScaleFactor()) + centreZ - getHeight() / 2.0;
    }

    private double renderToScreen(double renderPixels) {
        return renderPixels * getScaleFactor();
    }

    private float getScaleFactor() {
        return (float) (guiScale / client.getWindow().getScaleFactor());
    }

    private float getWidth() {
        return width / getScaleFactor();
    }

    private float getHeight() {
        return height / getScaleFactor();
    }
}