package com.ryxn.autototem;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class AutoTotemModMenu implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new ConfigScreen(parent);
    }

    private static class ConfigScreen extends Screen {

        private final Screen parent;

        protected ConfigScreen(Screen parent) {
            super(Text.literal("Auto Totem"));
            this.parent = parent;
        }

        @Override
        protected void init() {

            int centerX = this.width / 2;

            /*
             * ENABLE / DISABLE
             */

            this.addDrawableChild(
                    CyclingButtonWidget.onOffBuilder(
                            AutoTotemClient.CONFIG.enabled
                    )
                    .omitKeyText()
                    .build(
                            centerX - 100,
                            60,
                            200,
                            20,
                            Text.literal("Auto Totem"),
                            (button, value) -> {
                                AutoTotemClient.CONFIG.enabled = value;
                                AutoTotemClient.CONFIG.save();
                            }
                    )
            );

            /*
             * BACKUP SLOT
             */

            this.addDrawableChild(
                    new SlotSlider(
                            centerX - 100,
                            100,
                            200,
                            20,
                            AutoTotemClient.CONFIG.backupSlot,
                            value -> {
                                AutoTotemClient.CONFIG.backupSlot = value;
                                AutoTotemClient.CONFIG.save();
                            }
                    )
            );

            /*
             * DONE
             */

            this.addDrawableChild(
                    ButtonWidget.builder(
                            Text.literal("Done"),
                            button -> close()
                    ).dimensions(
                            centerX - 100,
                            145,
                            200,
                            20
                    ).build()
            );
        }

        @Override
        public void render(
                DrawContext context,
                int mouseX,
                int mouseY,
                float delta
        ) {

            super.render(
                    context,
                    mouseX,
                    mouseY,
                    delta
            );

            context.drawCenteredTextWithShadow(
                    this.textRenderer,
                    Text.literal("Auto Totem Settings"),
                    this.width / 2,
                    30,
                    0xFFFFFF
            );

            context.drawCenteredTextWithShadow(
                    this.textRenderer,
                    Text.literal(
                            "Backup Totem Slot: "
                                    + (AutoTotemClient.CONFIG.backupSlot + 1)
                    ),
                    this.width / 2,
                    130,
                    0xFFFFFF
            );
        }

        @Override
        public void close() {

            AutoTotemClient.CONFIG.save();

            if (client != null) {
                client.setScreen(parent);
            }
        }
    }

    /*
     * Slider for hotbar slot 1-9.
     */

    private static class SlotSlider
            extends SliderWidget {

        private final java.util.function.IntConsumer onChange;

        protected SlotSlider(
                int x,
                int y,
                int width,
                int height,
                int value,
                java.util.function.IntConsumer onChange
        ) {

            super(
                    x,
                    y,
                    width,
                    height,
                    Text.literal(
                            "Slot "
                                    + (value + 1)
                    ),
                    value / 8.0
            );

            this.onChange = onChange;
        }

        @Override
        protected void updateMessage() {

            int slot =
                    (int) Math.round(
                            this.value * 8.0
                    );

            this.setMessage(
                    Text.literal(
                            "Slot "
                                    + (slot + 1)
                    )
            );
        }

        @Override
        protected void applyValue() {

            int slot =
                    (int) Math.round(
                            this.value * 8.0
                    );

            onChange.accept(slot);
        }
    }
          }
