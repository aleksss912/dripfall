package com.example.dripfall.client;

import com.example.dripfall.SettingsPayload;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class ConfigScreen extends Screen {
    private final Screen parent;

    private boolean enabled;
    private float speed;
    private int radius;
    private int interval;
    private boolean onlyOpen;
    private int height;

    public ConfigScreen(Screen parent) {
        super(Text.literal("DripFall"));
        this.parent = parent;
        SettingsPayload c = DripFallClient.current;
        enabled = c.enabled();
        speed = c.speed();
        radius = c.radius();
        interval = c.interval();
        onlyOpen = c.onlyOpen();
        height = c.height();
    }

    @Override
    protected void init() {
        int w = 200;
        int x = width / 2 - w / 2;
        int y = height / 4 - 10;
        int step = 24;

        addDrawableChild(ButtonWidget.builder(enabledText(), b -> {
            enabled = !enabled;
            b.setMessage(enabledText());
        }).dimensions(x, y, w, 20).build());

        addDrawableChild(new Slider(x, y + step, w, "Prędkość (bloki/tick)", 0.5, 8.0, speed, false,
                v -> speed = v.floatValue()));
        addDrawableChild(new Slider(x, y + step * 2, w, "Zasięg wokół Ciebie", 2, 16, radius, true,
                v -> radius = v.intValue()));
        addDrawableChild(new Slider(x, y + step * 3, w, "Odstęp (ticki)", 1, 40, interval, true,
                v -> interval = v.intValue()));
        addDrawableChild(new Slider(x, y + step * 4, w, "Wysokość spadania", 3, 20, height, true,
                v -> height = v.intValue()));

        addDrawableChild(ButtonWidget.builder(onlyOpenText(), b -> {
            onlyOpen = !onlyOpen;
            b.setMessage(onlyOpenText());
        }).dimensions(x, y + step * 5, w, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Gotowe"), b -> close())
                .dimensions(x, y + step * 6 + 6, w, 20).build());
    }

    private Text enabledText() {
        return Text.literal("Mod: " + (enabled ? "WŁĄCZONY" : "wyłączony"));
    }

    private Text onlyOpenText() {
        return Text.literal("Tylko otwarte trapdory: " + (onlyOpen ? "TAK" : "NIE"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 15, 0xFFFFFF);
    }

    @Override
    public void removed() {
        DripFallClient.current = new SettingsPayload(enabled, speed, radius, interval, onlyOpen, height).clamped();
        DripFallClient.send();
    }

    @Override
    public void close() {
        client.setScreen(parent);
    }

    private static class Slider extends SliderWidget {
        private final String label;
        private final double min, max;
        private final boolean integer;
        private final Consumer<Double> setter;

        Slider(int x, int y, int w, String label, double min, double max, double current,
               boolean integer, Consumer<Double> setter) {
            super(x, y, w, 20, Text.empty(), (current - min) / (max - min));
            this.label = label;
            this.min = min;
            this.max = max;
            this.integer = integer;
            this.setter = setter;
            updateMessage();
        }

        private double real() {
            double v = min + value * (max - min);
            return integer ? Math.round(v) : Math.round(v * 10.0) / 10.0;
        }

        @Override
        protected void updateMessage() {
            double v = real();
            setMessage(Text.literal(label + ": " + (integer ? String.valueOf((int) v) : String.valueOf(v))));
        }

        @Override
        protected void applyValue() {
            setter.accept(real());
        }
    }
}
