package net.fasttravel.block.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fasttravel.network.FastTravelClientPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

@Environment(EnvType.CLIENT)
public class MonolithOpScreen extends Screen {

    private static final Text MONOLITH_NAME_TEXT = Text.translatable("op_screen.fasttravel.monolith_name");
    private static final Text ITEM_ICON_TEXT = Text.translatable("op_screen.fasttravel.item_icon");

    private final BlockPos teleporterPos;
    private final Text name;
    private final ItemStack itemStack;

    private ButtonWidget doneButton;
    private TextFieldWidget teleporterNameWidget;
    private TextFieldWidget teleporterItemStackWidget;

    public MonolithOpScreen(BlockPos teleporterPos, Text name, ItemStack itemStack) {
        super(NarratorManager.EMPTY);
        this.teleporterPos = teleporterPos;
        this.name = name;
        this.itemStack = itemStack;
    }

    @Override
    protected void init() {
        this.teleporterNameWidget = new TextFieldWidget(this.textRenderer, this.width / 2 - 152, 50, 300, 20, MONOLITH_NAME_TEXT);
        this.teleporterNameWidget.setMaxLength(128);
        this.teleporterNameWidget.setText(this.name.getString());
        this.teleporterNameWidget.setChangedListener(pool -> this.updateDoneButtonState());
        this.addSelectableChild(this.teleporterNameWidget);

        this.teleporterItemStackWidget = new TextFieldWidget(this.textRenderer, this.width / 2 - 152, 85, 300, 20, ITEM_ICON_TEXT);
        this.teleporterItemStackWidget.setMaxLength(128);
        this.teleporterItemStackWidget.setText(Registries.ITEM.getId(this.itemStack.getItem()).toString());
        this.teleporterItemStackWidget.setChangedListener(name -> this.updateDoneButtonState());
        this.addSelectableChild(this.teleporterItemStackWidget);

        this.doneButton = this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> {
            this.onDone();
        }).dimensions(this.width / 2 - 75, 156, 150, 20).build());
        this.setInitialFocus(this.teleporterNameWidget);
        this.updateDoneButtonState();
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        String string = this.teleporterNameWidget.getText();
        String string2 = this.teleporterItemStackWidget.getText();

        this.init(client, width, height);
        this.teleporterNameWidget.setText(string);
        this.teleporterItemStackWidget.setText(string2);
    }

    @Override
    public void tick() {
        this.teleporterNameWidget.tick();
        this.teleporterItemStackWidget.tick();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        context.drawTextWithShadow(this.textRenderer, MONOLITH_NAME_TEXT, this.width / 2 - 153, 40, 0xA0A0A0);
        this.teleporterNameWidget.render(context, mouseX, mouseY, delta);
        context.drawTextWithShadow(this.textRenderer, ITEM_ICON_TEXT, this.width / 2 - 153, 75, 0xA0A0A0);
        this.teleporterItemStackWidget.render(context, mouseX, mouseY, delta);

        super.render(context, mouseX, mouseY, delta);
    }

    private void updateDoneButtonState() {
        if (!this.teleporterNameWidget.getText().isEmpty() && !Registries.ITEM.get(new Identifier(this.teleporterItemStackWidget.getText())).getDefaultStack().isEmpty()) {
            this.doneButton.active = true;
        } else {
            this.doneButton.active = false;
        }
    }

    private void onDone() {
        this.client.setScreen(null);
        FastTravelClientPacket.sendTeleporterOpPacket(this.teleporterPos, this.teleporterNameWidget.getText(), Registries.ITEM.get(new Identifier(this.teleporterItemStackWidget.getText())).getDefaultStack());
    }
}
